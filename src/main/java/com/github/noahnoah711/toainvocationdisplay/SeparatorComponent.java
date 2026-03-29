package com.github.noahnoah711.toainvocationdisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

class SeparatorComponent implements LayoutableRenderableEntity
{
    private static final Color LINE_COLOR = new Color(80, 80, 80);
    private static final int HEIGHT = 3;

    private Point preferredLocation = new Point();
    private Dimension preferredSize = new Dimension(ComponentConstants.STANDARD_WIDTH, HEIGHT);

    @Override
    public Dimension render(Graphics2D graphics)
    {
        int width = preferredSize.width;
        int midY = preferredLocation.y + HEIGHT / 2;
        graphics.setColor(LINE_COLOR);
        graphics.drawLine(preferredLocation.x, midY, preferredLocation.x + width, midY);
        return new Dimension(width, HEIGHT);
    }

    @Override
    public void setPreferredLocation(Point location)
    {
        this.preferredLocation = location;
    }

    @Override
    public void setPreferredSize(Dimension dimension)
    {
        this.preferredSize = dimension;
    }

    @Override
    public Rectangle getBounds()
    {
        return new Rectangle(preferredLocation.x, preferredLocation.y, preferredSize.width, HEIGHT);
    }
}
