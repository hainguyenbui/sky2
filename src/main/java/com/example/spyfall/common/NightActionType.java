package com.example.spyfall.common;

public enum NightActionType {
    SOI("soi"),
    KILL("kill"),
    PROTECT("prot"),
    CONNECT("conn"),
    RECRUIT("recruit"),
    SUPER_PROTECT("superProt"),
    UNKNOWN("");

    private final String code;

    NightActionType(String code) {
        this.code = code;
    }

    public static NightActionType fromCode(String code) {
        for (NightActionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
