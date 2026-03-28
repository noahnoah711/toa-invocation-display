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
        ToaRoom currentRoom = plugin.getCurrentRoom();

        // Only render inside ToA (not outside the raid or in the end tomb)
        if (currentRoom == ToaRoom.NONE || currentRoom == ToaRoom.TOMB)
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

        boolean anyRendered = false;
        for (Invocation inv : Invocation.values())
        {
            if (inv.getRoom() != currentRoom)
            {
                continue;
            }

            Boolean active = invocations.get(inv);
            if (active == null)
            {
                continue;
            }

            if (config.showOnlyActive() && !active)
            {
                continue;
            }

            Color color = active ? config.activeColor() : config.inactiveColor();
            String label = inv.getDisplayName();
            String right = config.showRaidLevel() ? "+" + inv.getRaidLevelBonus() : "";

            panelComponent.getChildren().add(LineComponent.builder()
                .left(label)
                .leftColor(color)
                .right(right)
                .rightColor(color)
                .build());

            anyRendered = true;
        }

        // If showOnlyActive and nothing is on, say so
        if (!anyRendered && config.showOnlyActive())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("No active invocations")
                .leftColor(Color.GRAY)
                .build());
        }

        return super.render(graphics);
    }
}
