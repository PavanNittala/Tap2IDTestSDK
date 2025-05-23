package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Enums;

public enum NfcFriendlyType {
    TEXT(0),
    URL(1),
    HS(2),
    HR(3),
    OTHER(4);

    private final int value;

    NfcFriendlyType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static NfcFriendlyType fromValue(int value) {
        for (NfcFriendlyType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null; // or throw exception
    }
}
