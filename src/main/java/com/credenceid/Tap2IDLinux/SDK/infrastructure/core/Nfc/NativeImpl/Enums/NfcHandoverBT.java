package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Enums;

public enum NfcHandoverBT {
    UNKNOWN(0),
    BT(1),
    BLE(2);

    private final int value;
    NfcHandoverBT(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }

    public static NfcHandoverBT fromValue(int value) {
        for (NfcHandoverBT type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null; // or throw exception
    }
}
