package com.github.noahnoah711.toainvocationdisplay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ToaRoom
{
    NONE("None", new int[]{}),
    NEXUS("Nexus", new int[]{14160}),
    KEPHRI("Kephri", new int[]{14162, 14164}),   // Scabaras puzzle + Kephri boss
    AKKHA("Akkha", new int[]{14674, 14676}),       // Het puzzle + Akkha boss
    ZEBAK("Zebak", new int[]{15698, 15700}),       // Crondis puzzle + Zebak boss
    BABA("Ba-Ba", new int[]{15186, 15188}),        // Apmeken puzzle + Ba-Ba boss
    WARDENS("Wardens", new int[]{15184, 15696}),   // Wardens P1/P2 + Wardens P3
    TOMB("Tomb", new int[]{14672});                // End-of-raid lobby

    private final String displayName;
    private final int[] regionIds;

    public static ToaRoom forRegionId(int regionId)
    {
        for (ToaRoom room : values())
        {
            for (int id : room.regionIds)
            {
                if (id == regionId)
                {
                    return room;
                }
            }
        }
        return NONE;
    }
}
