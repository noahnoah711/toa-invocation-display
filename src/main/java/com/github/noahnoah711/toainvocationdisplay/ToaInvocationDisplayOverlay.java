package com.github.noahnoah711.toainvocationdisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class ToaInvocationDisplayOverlay extends OverlayPanel
{
    private final ToaInvocationDisplayPlugin plugin;
    private final ToaInvocationDisplayConfig config;

    @Inject
    ToaInvocationDisplayOverlay(ToaInvocationDisplayPlugin plugin, ToaInvocationDisplayConfig config)
    {
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.displayMode() != ToaInvocationDisplayConfig.DisplayMode.OVERLAY)
        {
            return null;
        }

        ToaRoom currentRoom = plugin.getCurrentRoom();

        if (currentRoom == ToaRoom.NONE || currentRoom == ToaRoom.TOMB || currentRoom == ToaRoom.GLOBAL)
        {
            return null;
        }

        Map<Invocation, Boolean> invocations = plugin.getActiveInvocations();

        panelComponent.getChildren().add(TitleComponent.builder()
            .text(currentRoom.getDisplayName())
            .color(Color.YELLOW)
            .build());

        if (invocations.isEmpty())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("Open invocation panel to load")
                .leftColor(Color.GRAY)
                .build());
            return super.render(graphics);
        }

        // --- Room-specific invocations ---
        boolean anyRoomRendered = false;
        for (Invocation inv : Invocation.values())
        {
            if (inv.getRoom() != currentRoom)
            {
                continue;
            }

            Boolean active = invocations.get(inv);
            if (active == null || (config.showOnlyActive() && !active))
            {
                continue;
            }

            panelComponent.getChildren().add(lineFor(inv, active));
            anyRoomRendered = true;
        }

        if (!anyRoomRendered && config.showOnlyActive())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("No active invocations")
                .leftColor(Color.GRAY)
                .build());
        }

        // --- Global invocations (attempts, time, path, prayer) ---
        {
            boolean anyGlobalRendered = false;
            for (Invocation inv : Invocation.values())
            {
                if (inv.getRoom() != ToaRoom.GLOBAL)
                {
                    continue;
                }

                Boolean active = invocations.get(inv);
                if (active == null || (config.showOnlyActive() && !active))
                {
                    continue;
                }

                if (!anyGlobalRendered)
                {
                    panelComponent.getChildren().add(new SeparatorComponent());
                }

                panelComponent.getChildren().add(lineFor(inv, active));
                anyGlobalRendered = true;
            }
        }

        return super.render(graphics);
    }

    private LineComponent lineFor(Invocation inv, boolean active)
    {
        Color color = active ? config.activeColor() : config.inactiveColor();
        String right = config.showRaidLevel() ? "+" + inv.getRaidLevelBonus() : "";
        return LineComponent.builder()
            .left(inv.getDisplayName())
            .leftColor(color)
            .right(right)
            .rightColor(color)
            .build();
    }
}
