package com.github.noahnoah711.toainvocationdisplay;

import com.google.inject.Provides;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
    name = "ToA Invocation Display",
    description = "Shows the active Tombs of Amascut invocations for the room you are currently in",
    tags = {"toa", "tombs of amascut", "invocations", "raids"}
)
public class ToaInvocationDisplayPlugin extends Plugin
{
    // Widget group IDs for the invocations interface
    private static final int TOA_PARTY_DETAILS_GROUP = 774;  // InterfaceID.TOA_PARTYDETAILS
    private static final int TOA_INVOCATIONS_GROUP = 776;    // InterfaceID.TOA_INVOCATIONS
    private static final int INVOCATIONS_CONTAINER_CHILD = 52;

    // Script IDs that fire when the invocations panel is built
    private static final int SCRIPT_BUILD_TOA_PARTY = 6617;
    private static final int SCRIPT_BUILD_TOA_INVOCATIONS = 6729;

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ToaInvocationDisplayOverlay overlay;

    @Inject
    private ToaInvocationDisplayConfig config;

    @Getter
    private ToaRoom currentRoom = ToaRoom.NONE;

    @Getter
    private Map<Invocation, Boolean> activeInvocations = Collections.emptyMap();

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
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

        if (currentRoom != ToaRoom.NONE)
        {
            readFromWidget();
        }
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event)
    {
        int scriptId = event.getScriptId();
        if (scriptId == SCRIPT_BUILD_TOA_PARTY || scriptId == SCRIPT_BUILD_TOA_INVOCATIONS)
        {
            readFromWidget();
        }
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
     * Widget 774:52 (TOA_PARTYDETAILS invocation container) persists in memory
     * throughout the raid even when the panel is not visible on screen, so we
     * skip the isHidden() check and poll it every game tick.
     *
     * Each invocation child: parent.getChild(ordinal * 3)
     * Active state: child.getOnOpListener()[3] == 1
     */
    private void readFromWidget()
    {
        Widget parent = client.getWidget(TOA_PARTY_DETAILS_GROUP, INVOCATIONS_CONTAINER_CHILD);
        if (parent == null)
        {
            parent = client.getWidget(TOA_INVOCATIONS_GROUP, INVOCATIONS_CONTAINER_CHILD);
        }
        if (parent == null)
        {
            return;
        }

        Map<Invocation, Boolean> result = new EnumMap<>(Invocation.class);
        for (Invocation inv : Invocation.values())
        {
            Widget child = parent.getChild(inv.ordinal() * 3);
            if (child == null)
            {
                continue;
            }

            Object[] ops = child.getOnOpListener();
            boolean active = ops != null && ops.length > 3 && Integer.valueOf(1).equals(ops[3]);
            result.put(inv, active);
        }

        if (!result.isEmpty())
        {
            activeInvocations = Collections.unmodifiableMap(result);
        }
    }
}
