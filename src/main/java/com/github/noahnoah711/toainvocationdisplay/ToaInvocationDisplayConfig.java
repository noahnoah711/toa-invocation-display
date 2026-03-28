package com.github.noahnoah711.toainvocationdisplay;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ToaInvocationDisplayConfig.GROUP)
public interface ToaInvocationDisplayConfig extends Config
{
    String GROUP = "toainvocationdisplay";

    @ConfigItem(
        keyName = "showOnlyActive",
        name = "Show only active invocations",
        description = "Only list invocations that are turned on. When off, all room invocations are listed with active ones highlighted.",
        position = 0
    )
    default boolean showOnlyActive()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showRaidLevel",
        name = "Show raid level bonus",
        description = "Show the raid level contribution (+X) next to each invocation.",
        position = 1
    )
    default boolean showRaidLevel()
    {
        return false;
    }

    @ConfigItem(
        keyName = "activeColor",
        name = "Active invocation color",
        description = "Color used for active invocations.",
        position = 2
    )
    default Color activeColor()
    {
        return new Color(0, 200, 83);
    }

    @ConfigItem(
        keyName = "inactiveColor",
        name = "Inactive invocation color",
        description = "Color used for inactive invocations (only visible when 'Show only active' is off).",
        position = 3
    )
    default Color inactiveColor()
    {
        return new Color(150, 150, 150);
    }
}
