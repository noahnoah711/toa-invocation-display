package com.github.noahnoah711.toainvocationdisplay;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
    name = "ToA Invocation Display",
    description = "Shows the active Tombs of Amascut invocations for the room you are currently in",
    tags = {"toa", "tombs of amascut", "invocations", "raids"}
)
public class ToaInvocationDisplayPlugin extends Plugin
{
    private static final int TOA_PARTY_DETAILS_GROUP = 774;
    private static final int TOA_INVOCATIONS_GROUP = 776;

    // InterfaceID.ToaPartydetails child indices (verified against runelite gameval enum)
    private static final int INVOCATIONS_CONTENT_CHILD = 52;        // leader's editable grid
    private static final int RAID_INVOCATIONS_ACTIVE_CHILD = 92;    // read-only active list, populated for all party members
    private static final int INVOCATIONS_SIDEPANEL_CHILD = 57;      // sidepanel summary
    private static final int TOA_INVOCATIONS_CONTAINER_CHILD = 5;   // ToaInvocations.INVOCATIONS

    private static final int SCRIPT_BUILD_TOA_PARTY = 6617;
    private static final int SCRIPT_BUILD_TOA_INVOCATIONS = 6729;

    private static final long DEBUG_DUMP_INTERVAL_MS = 10_000L;

    // Text color of the invocation name label (child ordinal*3+2) when the
    // invocation is active. Same encoding for leaders and non-leaders.
    // Inactive labels are gray (0x9F9F9F = 10461087); active are orange
    // (0xD39034 = 13868852). Verified empirically against a non-leader's
    // widget dump while the party panel was open in the raid lobby.
    private static final int ACTIVE_TEXT_COLOR = 0xD39034;

    // Active sprite IDs for invocation icons fall in the 4587-4598 range; inactive
    // sprites are 4611+. We treat any sprite below this threshold as "active icon".
    // Verified across multiple non-leader dumps where dumpWidget showed the colored
    // state correctly. This is the most reliable signal because sprite IDs appear
    // stable across the rebuild cycles that plague text color reads.
    private static final int ACTIVE_SPRITE_MAX = 4600;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ToaInvocationDisplayOverlay overlay;

    @Inject
    private ToaInvocationDisplayConfig config;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ToaInvocationDisplayPanel panel;

    private NavigationButton navButton;

    @Getter
    private ToaRoom currentRoom = ToaRoom.NONE;

    @Getter
    private Map<Invocation, Boolean> activeInvocations = Collections.emptyMap();

    private long lastDebugDumpMs = 0L;
    private long lastReadGridLogMs = 0L;

    // Unthrottled per-child dump of widget 774:52, fires only the first N times the
    // widget is observed loaded. Resets on plugin startup. Lets us see what the
    // grid actually contains during the brief moments it loads on a non-leader's
    // client (the regular 10s-throttled dump always misses it).
    private int detailedGridDumpsRemaining = 0;

    // Per-label signal dump: fires the first few times readGrid runs with
    // debugLogging on. Shows what OUR read sees for each invocation's
    // sprite/textColor/ops, side-by-side with the throttled dumpWidget output.
    private int perLabelDumpsRemaining = 0;

    @Override
    protected void startUp()
    {
        navButton = NavigationButton.builder()
            .tooltip("ToA Invocation Display")
            .icon(makeIcon())
            .priority(7)
            .panel(panel)
            .build();
        // Re-arm detailed dumps on every startup so toggling the plugin off/on
        // gives us a fresh batch when diagnosing the non-leader bug.
        detailedGridDumpsRemaining = 5;
        perLabelDumpsRemaining = 3;
        applyDisplayMode();
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
            navButton = null;
        }
        currentRoom = ToaRoom.NONE;
        activeInvocations = Collections.emptyMap();
    }

    @Provides
    ToaInvocationDisplayConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ToaInvocationDisplayConfig.class);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        updateCurrentRoom();

        // Defer the widget read via clientThread.invokeLater. Empirically observed:
        // when reading inline on the game-tick thread, getDynamicChildren() returned
        // widgets in their pre-script-coloring state (all-gray) even though the
        // throttled dumpWidget call moments later showed the same widget with
        // proper orange text colors. invokeLater queues our read for a later point
        // in the client cycle, after pending CS2 scripts have settled — which is
        // the same pattern the ToACustomPresets plugin uses successfully for its
        // widget mutations. Both leader and non-leader benefit.
        clientThread.invokeLater(this::readFromWidget);

        if (config.displayMode() == ToaInvocationDisplayConfig.DisplayMode.SIDE_PANEL)
        {
            pushPanelUpdate();
        }
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event)
    {
        int scriptId = event.getScriptId();
        if (scriptId == SCRIPT_BUILD_TOA_PARTY || scriptId == SCRIPT_BUILD_TOA_INVOCATIONS)
        {
            clientThread.invokeLater(this::readFromWidget);
            if (config.displayMode() == ToaInvocationDisplayConfig.DisplayMode.SIDE_PANEL)
            {
                pushPanelUpdate();
            }
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (!ToaInvocationDisplayConfig.GROUP.equals(event.getGroup()))
        {
            return;
        }
        if ("displayMode".equals(event.getKey()))
        {
            applyDisplayMode();
            return;
        }
        if (config.displayMode() == ToaInvocationDisplayConfig.DisplayMode.SIDE_PANEL)
        {
            pushPanelUpdate();
        }
    }

    private void applyDisplayMode()
    {
        if (config.displayMode() == ToaInvocationDisplayConfig.DisplayMode.OVERLAY)
        {
            overlayManager.add(overlay);
            if (navButton != null)
            {
                clientToolbar.removeNavigation(navButton);
            }
        }
        else
        {
            overlayManager.remove(overlay);
            if (navButton != null)
            {
                clientToolbar.addNavigation(navButton);
            }
            pushPanelUpdate();
        }
    }

    private void pushPanelUpdate()
    {
        ToaRoom room = currentRoom;
        Map<Invocation, Boolean> snapshot = activeInvocations;
        SwingUtilities.invokeLater(() -> panel.update(room, snapshot));
    }

    private void updateCurrentRoom()
    {
        if (client.getLocalPlayer() == null)
        {
            currentRoom = ToaRoom.NONE;
            return;
        }

        LocalPoint lp = client.getLocalPlayer().getLocalLocation();
        if (lp == null)
        {
            currentRoom = ToaRoom.NONE;
            return;
        }

        int regionId = WorldPoint.fromLocalInstance(client, lp).getRegionID();
        currentRoom = ToaRoom.forRegionId(regionId);
    }

    /**
     * Reads active invocations from the widget tree.
     *
     * Two sources are tried, in order:
     *
     * 1. Widget 774:92 (RAID_INVOCATIONS_ACTIVE) — the read-only "active invocations"
     *    list visible to all party members. Children are matched by text content
     *    against {@link Invocation#getDisplayName()}. Works for non-leaders.
     *
     * 2. Widget 774:52 (INVOCATIONS_CONTENT) — the leader's editable grid. Each
     *    invocation lives at child index {@code ordinal * 3}; active state is
     *    encoded in {@code getOnOpListener()[3]}. Only works for the raid leader,
     *    because non-leaders have no interactive op listeners.
     */
    private void readFromWidget()
    {
        if (config.debugLogging())
        {
            maybeDumpDebug();
        }

        Map<Invocation, Boolean> fromActiveList = readFromRaidActiveSummary();
        if (fromActiveList != null && !fromActiveList.isEmpty())
        {
            activeInvocations = Collections.unmodifiableMap(fromActiveList);
            logCacheUpdate("RAID_INVOCATIONS_ACTIVE", fromActiveList);
            return;
        }

        Map<Invocation, Boolean> fromGrid = readFromInvocationGrid();
        if (fromGrid != null && !fromGrid.isEmpty())
        {
            // Defensive: refuse to overwrite the cache with an all-inactive read.
            // The widget loads/unloads in two passes — children appear with default
            // (gray) text colors first, then the scripts color the active ones
            // orange. My code can catch the in-between state and clobber a good
            // cache. Real ToA raids always have ≥1 active invocation (path level,
            // prayer, time limit, etc.), so an all-inactive result is essentially
            // always transient garbage.
            boolean anyActive = false;
            for (Boolean v : fromGrid.values())
            {
                if (Boolean.TRUE.equals(v))
                {
                    anyActive = true;
                    break;
                }
            }
            if (anyActive)
            {
                activeInvocations = Collections.unmodifiableMap(fromGrid);
                logCacheUpdate("INVOCATIONS_CONTENT", fromGrid);
            }
            else if (config.debugLogging())
            {
                log.info("Skipping cache update: read found 0 actives — likely transient widget state during load/unload (room={})",
                    currentRoom);
            }
        }
    }

    private int lastLoggedActiveCount = -1;

    private void logCacheUpdate(String source, Map<Invocation, Boolean> result)
    {
        if (!config.debugLogging())
        {
            return;
        }
        // Log only when the active count CHANGES (vs. throttling on time) so the
        // log shows the full sequence of cache transitions clearly.
        int activeCount = 0;
        for (Boolean v : result.values())
        {
            if (Boolean.TRUE.equals(v))
            {
                activeCount++;
            }
        }
        if (activeCount == lastLoggedActiveCount)
        {
            return;
        }
        lastLoggedActiveCount = activeCount;
        log.info("activeInvocations cache updated from {}: {} active out of {} entries (room={})",
            source, activeCount, result.size(), currentRoom);
    }

    /**
     * Hypothesis: widget 774:92 (RAID_INVOCATIONS_ACTIVE) holds a read-only list
     * of currently-active invocations, populated for all party members. Each child
     * presumably renders a single active invocation's name.
     *
     * Returns a map of {@code Invocation -> true} for every match, with all other
     * invocations defaulted to {@code false}. Returns {@code null} if the widget
     * is missing or unparseable.
     */
    private Map<Invocation, Boolean> readFromRaidActiveSummary()
    {
        Widget parent = client.getWidget(TOA_PARTY_DETAILS_GROUP, RAID_INVOCATIONS_ACTIVE_CHILD);
        if (parent == null)
        {
            return null;
        }

        Widget[] children = parent.getDynamicChildren();
        if (children == null || children.length == 0)
        {
            children = parent.getChildren();
        }
        if (children == null || children.length == 0)
        {
            return null;
        }

        Map<String, Invocation> byName = new HashMap<>();
        for (Invocation inv : Invocation.values())
        {
            byName.put(normalizeName(inv.getDisplayName()), inv);
        }

        Map<Invocation, Boolean> result = new EnumMap<>(Invocation.class);
        for (Invocation inv : Invocation.values())
        {
            result.put(inv, false);
        }

        boolean anyMatched = false;
        for (Widget child : children)
        {
            if (child == null)
            {
                continue;
            }
            String text = child.getText();
            if (text == null || text.isEmpty())
            {
                continue;
            }
            Invocation matched = byName.get(normalizeName(text));
            if (matched != null)
            {
                result.put(matched, true);
                anyMatched = true;
            }
        }

        return anyMatched ? result : null;
    }

    /**
     * Strips RuneScape color tags (e.g. {@code <col=ff0000>...</col>}), bonus
     * suffixes, surrounding whitespace, and lowercases. Lets us match the
     * displayed widget text against {@link Invocation#getDisplayName()} robustly.
     */
    private static String normalizeName(String text)
    {
        return text.replaceAll("<[^>]+>", "")
            .replaceAll("\\s*\\(?\\+\\d+\\)?\\s*$", "")
            .trim()
            .toLowerCase();
    }

    /**
     * Reads active state from widget 774:52 (INVOCATIONS_CONTENT). Each invocation
     * occupies three sibling children: a background rectangle (ordinal*3), an icon
     * sprite (ordinal*3+1), and a name label (ordinal*3+2).
     *
     * Two independent signals are checked. If EITHER says active, the invocation is
     * considered active.
     *
     *   1. Label text color (ordinal*3+2). Active labels render in orange
     *      ({@link #ACTIVE_TEXT_COLOR}); inactive in gray. Verified empirically
     *      against a non-leader's lobby widget dump. This is the primary signal
     *      because it does not depend on click handlers — works for both roles.
     *
     *   2. Background rect op listener (ordinal*3), {@code getOnOpListener()[3] == 1}.
     *      Only present for the raid leader (non-leaders cannot toggle invocations,
     *      so no click handler is bound). Kept as a defensive fallback in case the
     *      label color palette ever shifts upstream.
     *
     * Caveat: for non-leaders the widget only exists in the raid lobby. Once they
     * enter the raid the entire 774 group unloads. The cached {@code activeInvocations}
     * map (populated from the last successful lobby read) is what backs the in-room
     * overlay/panel.
     */
    private Map<Invocation, Boolean> readFromInvocationGrid()
    {
        Widget parent = client.getWidget(TOA_PARTY_DETAILS_GROUP, INVOCATIONS_CONTENT_CHILD);
        String parentSource = "774:52";
        if (parent == null)
        {
            parent = client.getWidget(TOA_INVOCATIONS_GROUP, TOA_INVOCATIONS_CONTAINER_CHILD);
            parentSource = "776:5";
        }
        if (parent == null)
        {
            return null;
        }

        // Robust read: scan BOTH dynamic and static children arrays, match labels
        // by name (not by index), and treat an invocation as active if either
        // array shows it orange. This is defensive against:
        //   - The colors being populated on only one of the arrays at any moment
        //     (we've observed this empirically on non-leader clients).
        //   - The widget being torn down/rebuilt mid-tick so a single array read
        //     catches a transient gray state.
        //   - Any future index-vs-name drift if Jagex reshuffles the grid.
        Widget[] dyn = parent.getDynamicChildren();
        Widget[] stat = parent.getChildren();
        boolean dynUsable = dyn != null && dyn.length > 0;
        boolean statUsable = stat != null && stat.length > 0;
        if (!dynUsable && !statUsable)
        {
            return null;
        }

        // Unthrottled diagnostic: dump the first ~9 children (3 invocations worth
        // of bg/icon/label) the first few times we catch the grid loaded. We've
        // confirmed the grid loads briefly on non-leaders but the regular 10s
        // dump cycle always misses it, so this fires inline when we first see it.
        if (config.debugLogging() && detailedGridDumpsRemaining > 0)
        {
            detailedGridDumpsRemaining--;
            Widget[] dynForDump = parent.getDynamicChildren();
            Widget[] statForDump = parent.getChildren();
            int dynLen = dynForDump == null ? 0 : dynForDump.length;
            int statLen = statForDump == null ? 0 : statForDump.length;
            log.info("=== GRID LOADED ({}) — detailed dump (remaining={}) — dynChildren={}, staticChildren={} ===",
                parentSource, detailedGridDumpsRemaining, dynLen, statLen);
            // Dump first 9 children of whichever array has data, so we see 3
            // invocations × (bg, icon, label) and can compare what fields differ
            // between active vs inactive on this client.
            Widget[] toDump = dynLen > 0 ? dynForDump : statForDump;
            if (toDump != null)
            {
                int limit = Math.min(toDump.length, 9);
                for (int i = 0; i < limit; i++)
                {
                    Widget c = toDump[i];
                    if (c == null)
                    {
                        log.info("  child[{}] = null", i);
                        continue;
                    }
                    Object[] ops = c.getOnOpListener();
                    log.info("  child[{}] type={} sprite={} item={} model={} opacity={} hidden={} text='{}' textColor=0x{} ops={}",
                        i, c.getType(), c.getSpriteId(), c.getItemId(), c.getModelId(),
                        c.getOpacity(), c.isHidden(), c.getText(),
                        Integer.toHexString(c.getTextColor()),
                        ops == null ? "null" : Arrays.toString(ops));
                }
            }
        }

        // Build a name-keyed lookup for invocations.
        Map<String, Invocation> byName = new HashMap<>();
        for (Invocation inv : Invocation.values())
        {
            byName.put(inv.getDisplayName().toLowerCase(), inv);
        }

        Map<Invocation, Boolean> result = new EnumMap<>(Invocation.class);
        for (Invocation inv : Invocation.values())
        {
            result.put(inv, false);
        }

        // Multi-signal active detection. For each matched label widget, we also
        // pull the icon (index-1) and background (index-2) from the SAME children
        // array, then check three independent signals. Any one being "active"
        // marks the invocation active. This is defensive against any one signal
        // being missing or stale on the non-leader's client.
        //
        //   1. Sprite ID at icon (index-1): active sprites < 4600, inactive ≥ 4611.
        //      Empirically the most reliable signal — sprite IDs appear stable
        //      across the rebuild cycles that have plagued textColor reads.
        //   2. Text color at label (index): 0xD39034 = active.
        //   3. Op listener at background (index-2): ops[3] == 1 = active.
        //      Only present when the leader's interactive UI ops are bound to
        //      the children, which varies; treated as bonus signal.
        int[] counts = new int[6]; // [matched, spriteHit, textHit, opsHit, anyHit, total]
        Widget[] arrayUsed = dynUsable ? dyn : stat;
        boolean usedDynamic = dynUsable;
        scanArrayForActives(arrayUsed, byName, result, counts);

        // If we got 0 hits with dynamic, also try static (and vice versa). Some
        // states have the colored data in only one of the two arrays.
        if (counts[4] == 0 && dynUsable && statUsable && arrayUsed != stat)
        {
            int[] counts2 = new int[6];
            scanArrayForActives(stat, byName, result, counts2);
            if (counts2[4] > 0)
            {
                arrayUsed = stat;
                usedDynamic = false;
                counts = counts2;
            }
        }

        // Throttled per-call diagnostic. Logs once every 3s when debugLogging is
        // on so we can see what the read actually produces without spamming.
        if (config.debugLogging())
        {
            long now = System.currentTimeMillis();
            if (now - lastReadGridLogMs >= 3_000L)
            {
                lastReadGridLogMs = now;
                log.info("readGrid({} {}): matched={} spriteHit={} textHit={} opsHit={} anyHit={} dynLen={} statLen={}",
                    parentSource, usedDynamic ? "dyn" : "stat",
                    counts[0], counts[1], counts[2], counts[3], counts[4],
                    dyn == null ? -1 : dyn.length,
                    stat == null ? -1 : stat.length);

                // First time we get a successful detailed read on each side, dump
                // every matched invocation's per-signal raw values so we can see
                // what's actually being read — this is the diagnostic we need to
                // explain why dumpWidget sees orange but our reads sometimes don't.
                if (perLabelDumpsRemaining > 0)
                {
                    perLabelDumpsRemaining--;
                    dumpPerLabelSignals(arrayUsed, byName);
                }
            }
        }

        if (counts[0] == 0)
        {
            return null;
        }
        return result;
    }

    /**
     * Single-pass scan of one widget array. For each label that matches an
     * invocation by text, checks the three active signals on the icon at
     * (i-1) and background at (i-2) within the SAME array. Updates result
     * (OR-merging into existing values) and increments counters in counts[]:
     *   [0]=matched, [1]=spriteHit, [2]=textHit, [3]=opsHit, [4]=anyHit, [5]=unused.
     */
    private void scanArrayForActives(Widget[] children, Map<String, Invocation> byName,
                                     Map<Invocation, Boolean> result, int[] counts)
    {
        if (children == null) return;
        for (int i = 0; i < children.length; i++)
        {
            Widget label = children[i];
            if (label == null) continue;
            String text = label.getText();
            if (text == null || text.isEmpty()) continue;
            Invocation matched = byName.get(text.toLowerCase());
            if (matched == null) continue;
            counts[0]++;

            boolean active = false;

            // Signal 2: label text color.
            if (label.getTextColor() == ACTIVE_TEXT_COLOR)
            {
                counts[2]++;
                active = true;
            }

            // Signal 1: icon sprite at i-1.
            if (i >= 1)
            {
                Widget icon = children[i - 1];
                if (icon != null)
                {
                    int sprite = icon.getSpriteId();
                    if (sprite > 0 && sprite < ACTIVE_SPRITE_MAX)
                    {
                        counts[1]++;
                        active = true;
                    }
                }
            }

            // Signal 3: background op listener at i-2.
            if (i >= 2)
            {
                Widget bg = children[i - 2];
                if (bg != null)
                {
                    Object[] ops = bg.getOnOpListener();
                    if (ops != null && ops.length > 3 && Integer.valueOf(1).equals(ops[3]))
                    {
                        counts[3]++;
                        active = true;
                    }
                }
            }

            if (active)
            {
                counts[4]++;
                result.put(matched, true);
            }
        }
    }

    /**
     * Diagnostic: for the first few times we run with debugLogging on, dump
     * every matched label's per-signal raw values from the SAME array we just
     * iterated. This is what tells us whether our read is actually seeing the
     * same data as dumpWidget.
     */
    private void dumpPerLabelSignals(Widget[] children, Map<String, Invocation> byName)
    {
        if (children == null) return;
        log.info("--- per-label signal dump (will compare with throttled dumpWidget) ---");
        for (int i = 0; i < children.length; i++)
        {
            Widget label = children[i];
            if (label == null) continue;
            String text = label.getText();
            if (text == null || text.isEmpty()) continue;
            if (byName.get(text.toLowerCase()) == null) continue;
            int textColor = label.getTextColor();
            int sprite = i >= 1 && children[i - 1] != null ? children[i - 1].getSpriteId() : -2;
            Object[] ops = i >= 2 && children[i - 2] != null ? children[i - 2].getOnOpListener() : null;
            String opsStr = ops == null ? "null" : ("len=" + ops.length + " [3]=" + (ops.length > 3 ? ops[3] : "?"));
            log.info("  i={} '{}' textColor=0x{} icon@{}-1=sprite{} bg@{}-2=ops{}",
                i, text, Integer.toHexString(textColor),
                i, sprite, i, opsStr);
        }
    }

    private static final int[] TOA_INTERFACE_GROUPS = {
        481, 482, 771, 772, 773, 774, 775, 776, 777, 778
    };

    private void maybeDumpDebug()
    {
        long now = System.currentTimeMillis();
        if (now - lastDebugDumpMs < DEBUG_DUMP_INTERVAL_MS)
        {
            return;
        }
        lastDebugDumpMs = now;

        String player = client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : "?";
        log.info("=== TOA WIDGET DUMP START (player={}, room={}) ===", player, currentRoom);

        log.info("--- Interface group existence ---");
        for (int group : TOA_INTERFACE_GROUPS)
        {
            Widget root = client.getWidget(group, 0);
            if (root == null)
            {
                log.info("Widget {}:0 = NULL (group not loaded)", group);
            }
            else
            {
                Widget[] dyn = root.getDynamicChildren();
                Widget[] stat = root.getChildren();
                log.info("Widget {}:0 = LOADED, hidden={}, dynChildren={}, staticChildren={}",
                    group, root.isHidden(),
                    dyn == null ? 0 : dyn.length,
                    stat == null ? 0 : stat.length);
            }
        }

        log.info("--- TOA_PARTYDETAILS (774) candidate children ---");
        dumpWidget("INVOCATIONS_CONTENT", TOA_PARTY_DETAILS_GROUP, INVOCATIONS_CONTENT_CHILD);
        dumpWidget("RAID_INVOCATIONS_ACTIVE", TOA_PARTY_DETAILS_GROUP, RAID_INVOCATIONS_ACTIVE_CHILD);
        dumpWidget("INVOCATIONS_SIDEPANEL", TOA_PARTY_DETAILS_GROUP, INVOCATIONS_SIDEPANEL_CHILD);
        dumpWidget("TOA_INVOCATIONS:5", TOA_INVOCATIONS_GROUP, TOA_INVOCATIONS_CONTAINER_CHILD);

        log.info("--- TOA_HUD (481) full dump ---");
        dumpWidget("TOA_HUD:0", 481, 0);

        log.info("--- VarPlayers 3580-3620 (non-zero only) ---");
        for (int vp = 3580; vp <= 3620; vp++)
        {
            int v;
            try
            {
                v = client.getVarpValue(vp);
            }
            catch (Exception e)
            {
                continue;
            }
            if (v != 0)
            {
                log.info("VarPlayer {} = {} (0x{})", vp, v, Integer.toHexString(v));
            }
        }

        log.info("=== TOA WIDGET DUMP END ===");
    }

    private void dumpWidget(String label, int group, int child)
    {
        Widget w = client.getWidget(group, child);
        if (w == null)
        {
            log.info("{} ({}:{}): NULL", label, group, child);
            return;
        }
        Widget[] dynamic = w.getDynamicChildren();
        Widget[] staticChildren = w.getChildren();
        int dynLen = dynamic == null ? 0 : dynamic.length;
        int statLen = staticChildren == null ? 0 : staticChildren.length;
        log.info("{} ({}:{}): hidden={}, type={}, dynChildren={}, staticChildren={}, text='{}'",
            label, group, child, w.isHidden(), w.getType(), dynLen, statLen, w.getText());

        Widget[] toDump = dynLen > 0 ? dynamic : staticChildren;
        if (toDump == null)
        {
            return;
        }
        int limit = Math.min(toDump.length, 144);
        for (int i = 0; i < limit; i++)
        {
            Widget c = toDump[i];
            if (c == null)
            {
                continue;
            }
            Object[] ops = c.getOnOpListener();
            log.info("  [{}] type={} sprite={} item={} model={} opacity={} text='{}' textColor={} ops={}",
                i, c.getType(), c.getSpriteId(), c.getItemId(), c.getModelId(),
                c.getOpacity(), c.getText(), c.getTextColor(),
                ops == null ? "null" : Arrays.toString(ops));
        }
    }

    private static BufferedImage makeIcon()
    {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(40, 40, 40));
        g.fillRoundRect(0, 0, 16, 16, 4, 4);
        g.setColor(new Color(0, 200, 83));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        g.drawString("ToA", 1, 11);
        g.dispose();
        return img;
    }
}
