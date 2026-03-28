package com.github.noahnoah711.toainvocationdisplay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All 44 ToA invocations in widget ordinal order.
 *
 * The widget child index for each invocation is: ordinal() * 3
 * Parent widget: group=774 (TOA_PARTYDETAILS), child=52
 *
 * DO NOT reorder enum constants — ordinal() must match the widget child index.
 * To reassign an invocation to a different room, change only the ToaRoom argument.
 */
@Getter
@RequiredArgsConstructor
public enum Invocation
{
    // --- Attempts (0-3) --- shown in all boss rooms
    TRY_AGAIN(ToaRoom.GLOBAL, "Try Again", 5),
    PERSISTENCE(ToaRoom.GLOBAL, "Persistence", 10),
    SOFTCORE_RUN(ToaRoom.GLOBAL, "Softcore Run", 15),
    HARDCORE_RUN(ToaRoom.GLOBAL, "Hardcore Run", 25),

    // --- Time Limit (4-7) --- shown in all boss rooms
    WALK_FOR_IT(ToaRoom.GLOBAL, "Walk for It", 10),
    JOG_FOR_IT(ToaRoom.GLOBAL, "Jog for It", 15),
    RUN_FOR_IT(ToaRoom.GLOBAL, "Run for It", 20),
    SPRINT_FOR_IT(ToaRoom.GLOBAL, "Sprint for It", 25),

    // --- Helpful Spirit (8-10) --- not displayed
    NEED_SOME_HELP(ToaRoom.NONE, "Need Some Help?", 15),
    NEED_LESS_HELP(ToaRoom.NONE, "Need Less Help?", 25),
    NO_HELP_NEEDED(ToaRoom.NONE, "No Help Needed", 40),

    // --- Path Level (11-14) --- shown in all boss rooms
    WALK_THE_PATH(ToaRoom.GLOBAL, "Walk the Path", 50),
    PATHSEEKER(ToaRoom.GLOBAL, "Pathseeker", 15),
    PATHFINDER(ToaRoom.GLOBAL, "Pathfinder", 40),
    PATHMASTER(ToaRoom.GLOBAL, "Pathmaster", 50),

    // --- Prayer (15-16) --- shown in all boss rooms
    QUIET_PRAYERS(ToaRoom.GLOBAL, "Quiet Prayers", 20),
    DEADLY_PRAYERS(ToaRoom.GLOBAL, "Deadly Prayers", 20),

    // --- Restoration (17-19) --- shown in Nexus
    ON_A_DIET(ToaRoom.NEXUS, "On a Diet", 15),
    DEHYDRATION(ToaRoom.NEXUS, "Dehydration", 30),
    OVERLY_DRAINING(ToaRoom.NEXUS, "Overly Draining", 15),

    // --- Kephri (20-24) ---
    LIVELY_LARVAE(ToaRoom.KEPHRI, "Lively Larvae", 5),
    MORE_OVERLORDS(ToaRoom.KEPHRI, "More Overlords", 15),
    BLOWING_MUD(ToaRoom.KEPHRI, "Blowing Mud", 10),
    MEDIC(ToaRoom.KEPHRI, "Medic!", 15),
    AERIAL_ASSAULT(ToaRoom.KEPHRI, "Aerial Assault", 10),

    // --- Zebak (25-28) ---
    NOT_JUST_A_HEAD(ToaRoom.ZEBAK, "Not Just a Head", 15),
    ARTERIAL_SPRAY(ToaRoom.ZEBAK, "Arterial Spray", 10),
    BLOOD_THINNERS(ToaRoom.ZEBAK, "Blood Thinners", 5),
    UPSET_STOMACH(ToaRoom.ZEBAK, "Upset Stomach", 15),

    // --- Akkha (29-32) ---
    DOUBLE_TROUBLE(ToaRoom.AKKHA, "Double Trouble", 20),
    KEEP_BACK(ToaRoom.AKKHA, "Keep Back", 10),
    STAY_VIGILANT(ToaRoom.AKKHA, "Stay Vigilant", 15),
    FEELING_SPECIAL(ToaRoom.AKKHA, "Feeling Special?", 20),

    // --- Ba-Ba (33-37) ---
    MIND_THE_GAP(ToaRoom.BABA, "Mind the Gap!", 10),
    GOTTA_HAVE_FAITH(ToaRoom.BABA, "Gotta Have Faith", 10),
    JUNGLE_JAPES(ToaRoom.BABA, "Jungle Japes", 5),
    SHAKING_THINGS_UP(ToaRoom.BABA, "Shaking Things Up", 10),
    BOULDERDASH(ToaRoom.BABA, "Boulderdash", 10),

    // --- Wardens (38-43) ---
    ANCIENT_HASTE(ToaRoom.WARDENS, "Ancient Haste", 10),
    ACCELERATION(ToaRoom.WARDENS, "Acceleration", 10),
    PENETRATION(ToaRoom.WARDENS, "Penetration", 10),
    OVERCLOCKED(ToaRoom.WARDENS, "Overclocked", 10),
    OVERCLOCKED_2(ToaRoom.WARDENS, "Overclocked 2", 10),
    INSANITY(ToaRoom.WARDENS, "Insanity", 50);

    private final ToaRoom room;
    private final String displayName;
    private final int raidLevelBonus;
}
