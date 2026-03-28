# ToA Invocation Display

A [RuneLite](https://runelite.net) plugin that shows the active invocations for whichever room you are currently in during a [Tombs of Amascut](https://oldschool.runescape.wiki/w/Tombs_of_Amascut) raid.

## The problem

The invocation screen is only easily visible before the raid starts. Once you're inside, there's no quick way to remind yourself which invocations are active for the room you're fighting in.

## What it does

An overlay appears automatically when you enter a raid room and lists only the invocations relevant to that room. When you move to the next room, the overlay updates to match.

| Room | Invocations shown |
|------|------------------|
| Nexus | On a Diet, Dehydration, Overly Draining |
| Kephri | Quiet Prayers, Deadly Prayers, Lively Larvae, More Overlords |
| Akkha | Blowing Mud, Medic!, Aerial Assault |
| Zebak | Not Just a Head, Arterial Spray, Blood Thinners, Upset Stomach, Double Trouble, Keep Back |
| Ba-Ba | Stay Vigilant, Feeling Special, Mind the Gap, Gotta Have Faith, Jungle Japes, Shaking Things Up, Boulderdash, Ancient Haste |
| Wardens | Acceleration, Penetration, Overclocked, Overclocked 2, Insanity |

Works whether you created the raid or joined someone else's.

## Configuration

| Option | Default | Description |
|--------|---------|-------------|
| Show only active invocations | On | When enabled, only invocations that are turned on are listed. Turn off to see all room invocations with active ones highlighted. |
| Show raid level bonus | Off | Appends the raid level contribution (e.g. +10) next to each invocation. |
| Active invocation color | Green | Color used for active invocations. |
| Inactive invocation color | Gray | Color used for inactive invocations (only visible when "Show only active" is off). |
