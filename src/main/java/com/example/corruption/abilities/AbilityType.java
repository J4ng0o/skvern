package com.example.corruption.abilities;

public enum AbilityType {
    RAGE("rage"),
    DOORS("doors"),
    TIME_STOP("timestop"),
    DASH("dash");

    private final String id;

    AbilityType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static AbilityType fromId(String id) {
        for (AbilityType type : values()) {
            if (type.id.equalsIgnoreCase(id)) return type;
        }
        return null;
    }
}
