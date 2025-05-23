package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Enums;

public enum NfcHandoverCPS {
    INACTIVE(0),
    ACTIVE(1),
    ACTIVATING(2),
    UNKNOWN(3);

    private final int value;
    NfcHandoverCPS(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }

    public static NfcHandoverCPS fromValue(int value) {
        for (NfcHandoverCPS type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null; // or throw exception
    }
}
