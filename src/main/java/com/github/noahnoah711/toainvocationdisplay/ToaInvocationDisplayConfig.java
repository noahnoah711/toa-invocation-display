package com.github.noahnoah711.toainvocationdisplay;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ToaInvocationDisplayConfig.GROUP)
public interface ToaInvocationDisplayConfig extends Config
{
    String GROUP = "toainvocationdisplay";

    enum DisplayMode
    {
        OVERLAY,
        SIDE_PANEL
    }

    @ConfigItem(
        keyName = "displayMode",
        name = "Display mode",
        description = "Where to show the invocation list. Overlay draws on top of the game; Side panel docks into RuneLite's right toolbar.",
        position = 0
    )
    default DisplayMode displayMode()
    {
        return DisplayMode.OVERLAY;
    }

    @ConfigItem(
        keyName = "showOnlyActive",
        name = "Show only active invocations",
        description = "Only list invocations that are turned on. When off, all room invocations are listed with active ones highlighted.",
        position = 1
    )
    default boolean showOnlyActive()
    {
        return true;
    }

    @ConfigItem(
        keyName = "showRaidLevel",
        name = "Show raid level bonus",
        description = "Show the raid level contribution (+X) next to each invocation.",
        position = 2
    )
    default boolean showRaidLevel()
    {
        return false;
    }

    @ConfigItem(
        keyName = "activeColor",
        name = "Active invocation color",
        description = "Color used for active invocations.",
        position = 3
    )
    default Color activeColor()
    {
        return new Color(0, 200, 83);
    }

    @ConfigItem(
        keyName = "inactiveColor",
        name = "Inactive invocation color",
        description = "Color used for inactive invocations (only visible when 'Show only active' is off).",
        position = 4
    )
    default Color inactiveColor()
    {
        return new Color(150, 150, 150);
    }

    @ConfigItem(
        keyName = "debugLogging",
        name = "Debug widget logging",
        description = "Logs ToA invocation widget state to the client log every 10 seconds while in a raid. Enable only if asked to help diagnose a bug; disable afterwards. Logs are at C:\\Users\\<you>\\.runelite\\logs\\client.log.",
        position = 10
    )
    default boolean debugLogging()
    {
        return false;
    }
}
