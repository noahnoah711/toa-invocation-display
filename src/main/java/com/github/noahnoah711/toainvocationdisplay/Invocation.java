package com.github.noahnoah711.toainvocationdisplay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All 44 ToA invocations in widget ordinal order.
 *
 * The widget child index for each invocation is: ordinal() * 3
 * Parent widget: group=774 (TOA_PARTYDETAILS), child=52
 *
 * Room assignments for ordinals 17-19 (ON_A_DIET, DEHYDRATION, OVERLY_DRAINING)
 * and ordinal 25 (NOT_JUST_A_HEAD) are based on widget ordering and may need
 * verification in-game. Adjust the ToaRoom argument to move them if needed.
 */
@Getter
@RequiredArgsConstructor
public enum Invocation
{
    // --- General (0-14) ---
    TRY_AGAIN(ToaRoom.NONE, "Try Again", 5),
    PERSISTENCE(ToaRoom.NONE, "Persistence", 10),
    SOFTCORE_RUN(ToaRoom.NONE, "Softcore Run", 15),
    HARDCORE_RUN(ToaRoom.NONE, "Hardcore Run", 25),
    WALK_FOR_IT(ToaRoom.NONE, "Walk for It", 10),
    JOG_FOR_IT(ToaRoom.NONE, "Jog for It", 15),
    RUN_FOR_IT(ToaRoom.NONE, "Run for It", 20),
    SPRINT_FOR_IT(ToaRoom.NONE, "Sprint for It", 25),
    NEED_SOME_HELP(ToaRoom.NONE, "Need Some Help", 15),
    NEED_LESS_HELP(ToaRoom.NONE, "Need Less Help", 25),
    NO_HELP_NEEDED(ToaRoom.NONE, "No Help Needed", 40),
    WALK_THE_PATH(ToaRoom.NONE, "Walk the Path", 50),
    PATHSEEKER(ToaRoom.NONE, "Pathseeker", 15),
    PATHFINDER(ToaRoom.NONE, "Pathfinder", 40),
    PATHMASTER(ToaRoom.NONE, "Pathmaster", 50),

    // --- Kephri (15-21) ---
    QUIET_PRAYERS(ToaRoom.KEPHRI, "Quiet Prayers", 20),
    DEADLY_PRAYERS(ToaRoom.KEPHRI, "Deadly Prayers", 20),
    ON_A_DIET(ToaRoom.NEXUS, "On a Diet", 15),
    DEHYDRATION(ToaRoom.NEXUS, "Dehydration", 30),
    OVERLY_DRAINING(ToaRoom.NEXUS, "Overly Draining", 15),
    LIVELY_LARVAE(ToaRoom.KEPHRI, "Lively Larvae", 5),
    MORE_OVERLORDS(ToaRoom.KEPHRI, "More Overlords", 15),

    // --- Akkha (22-24) ---
    BLOWING_MUD(ToaRoom.AKKHA, "Blowing Mud", 10),
    MEDIC(ToaRoom.AKKHA, "Medic!", 15),
    AERIAL_ASSAULT(ToaRoom.AKKHA, "Aerial Assault", 10),

    // --- Zebak (25-30) ---
    NOT_JUST_A_HEAD(ToaRoom.ZEBAK, "Not Just a Head", 15),
    ARTERIAL_SPRAY(ToaRoom.ZEBAK, "Arterial Spray", 10),
    BLOOD_THINNERS(ToaRoom.ZEBAK, "Blood Thinners", 5),
    UPSET_STOMACH(ToaRoom.ZEBAK, "Upset Stomach", 15),
    DOUBLE_TROUBLE(ToaRoom.ZEBAK, "Double Trouble", 20),
    KEEP_BACK(ToaRoom.ZEBAK, "Keep Back", 10),

    // --- Ba-Ba (31-38) ---
    STAY_VIGILANT(ToaRoom.BABA, "Stay Vigilant", 15),
    FEELING_SPECIAL(ToaRoom.BABA, "Feeling Special", 20),
    MIND_THE_GAP(ToaRoom.BABA, "Mind the Gap", 10),
    GOTTA_HAVE_FAITH(ToaRoom.BABA, "Gotta Have Faith", 10),
    JUNGLE_JAPES(ToaRoom.BABA, "Jungle Japes", 5),
    SHAKING_THINGS_UP(ToaRoom.BABA, "Shaking Things Up", 10),
    BOULDERDASH(ToaRoom.BABA, "Boulderdash", 10),
    ANCIENT_HASTE(ToaRoom.BABA, "Ancient Haste", 10),

    // --- Wardens (39-43) ---
    ACCELERATION(ToaRoom.WARDENS, "Acceleration", 10),
    PENETRATION(ToaRoom.WARDENS, "Penetration", 10),
    OVERCLOCKED(ToaRoom.WARDENS, "Overclocked", 10),
    OVERCLOCKED_2(ToaRoom.WARDENS, "Overclocked 2", 10),
    INSANITY(ToaRoom.WARDENS, "Insanity", 50);

    private final ToaRoom room;
    private final String displayName;
    private final int raidLevelBonus;
}
