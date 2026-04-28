# ToA Invocation Display

A [RuneLite](https://runelite.net) plugin that shows the active invocations for whichever room you are currently in during a [Tombs of Amascut](https://oldschool.runescape.wiki/w/Tombs_of_Amascut) raid.

## The problem

The invocation screen is only easily visible before the raid starts. Once you're inside, there's no quick way to remind yourself which invocations are active for the room you're fighting in.

## What it does

An overlay appears automatically when you enter a raid room. Each room shows two sections separated by a divider:

- **Top** — invocations specific to that room's boss
- **Bottom** — invocations that apply to the whole raid (attempts, time limit, path level, prayer)

When you move to the next room, the overlay updates to match.

| Room | Boss invocations |
|------|-----------------|
| Nexus | On a Diet, Dehydration, Overly Draining |
| Kephri | Lively Larvae, More Overlords, Blowing Mud, Medic!, Aerial Assault |
| Akkha | Double Trouble, Keep Back, Stay Vigilant, Feeling Special? |
| Zebak | Not Just a Head, Arterial Spray, Blood Thinners, Upset Stomach |
| Ba-Ba | Mind the Gap!, Gotta Have Faith, Jungle Japes, Shaking Things Up, Boulderdash |
| Wardens | Ancient Haste, Acceleration, Penetration, Overclocked, Overclocked 2, Insanity |

All rooms also show the following below the divider (if active): Try Again, Persistence, Softcore Run, Hardcore Run, Walk for It, Jog for It, Run for It, Sprint for It, Walk the Path, Pathseeker, Pathfinder, Pathmaster, Quiet Prayers, Deadly Prayers.

Works whether you created the raid or joined someone else's.

If you have limited screen space, you can switch the display from an overlay to a docked side panel via the Display mode option below.

## Configuration

| Option | Default | Description |
|--------|---------|-------------|
| Display mode | Overlay | Choose between Overlay (drawn on top of the game) or Side panel (docked into RuneLite's right toolbar). |
| Show only active invocations | On | Only list invocations that are turned on. Turn off to see all room invocations with active ones highlighted. |
| Show raid level bonus | Off | Appends the raid level contribution (e.g. +10) next to each invocation. |
| Active invocation color | Green | Color used for active invocations. |
| Inactive invocation color | Gray | Color used for inactive invocations (only visible when "Show only active" is off). |
