package com.github.noahnoah711.toainvocationdisplay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class ToaInvocationDisplayPanel extends PluginPanel
{
    private static final int ROW_HEIGHT = 20;
    private static final Color PLACEHOLDER_COLOR = new Color(170, 170, 170);

    private final ToaInvocationDisplayConfig config;

    @Inject
    ToaInvocationDisplayPanel(ToaInvocationDisplayConfig config)
    {
        super(false);
        this.config = config;
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        update(ToaRoom.NONE, Collections.emptyMap());
    }

    public void update(ToaRoom currentRoom, Map<Invocation, Boolean> invocations)
    {
        removeAll();

        if (currentRoom == ToaRoom.NONE || currentRoom == ToaRoom.TOMB || currentRoom == ToaRoom.GLOBAL)
        {
            add(placeholder("Not in a raid"));
            revalidate();
            repaint();
            return;
        }

        addTitle(currentRoom.getDisplayName());

        if (invocations.isEmpty())
        {
            add(Box.createVerticalStrut(4));
            add(placeholder("Open invocation panel to load"));
            revalidate();
            repaint();
            return;
        }

        add(Box.createVerticalStrut(4));

        boolean anyRoom = false;
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
            add(invocationRow(inv, active));
            anyRoom = true;
        }
        if (!anyRoom && config.showOnlyActive())
        {
            add(placeholder("No active invocations"));
        }

        boolean anyGlobal = false;
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
            if (!anyGlobal)
            {
                add(Box.createVerticalStrut(6));
                add(separator());
                add(Box.createVerticalStrut(4));
            }
            add(invocationRow(inv, active));
            anyGlobal = true;
        }

        revalidate();
        repaint();
    }

    private void addTitle(String text)
    {
        JLabel title = new JLabel(text);
        title.setForeground(Color.YELLOW);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);
    }

    private JPanel invocationRow(Invocation inv, boolean active)
    {
        Color color = active ? config.activeColor() : config.inactiveColor();
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));

        JLabel name = new JLabel(inv.getDisplayName());
        name.setForeground(color);
        row.add(name, BorderLayout.WEST);

        if (config.showRaidLevel())
        {
            JLabel bonus = new JLabel("+" + inv.getRaidLevelBonus());
            bonus.setForeground(color);
            bonus.setHorizontalAlignment(SwingConstants.RIGHT);
            row.add(bonus, BorderLayout.EAST);
        }
        return row;
    }

    private JLabel placeholder(String text)
    {
        JLabel label = new JLabel(text);
        label.setForeground(PLACEHOLDER_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JSeparator separator()
    {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }
}
