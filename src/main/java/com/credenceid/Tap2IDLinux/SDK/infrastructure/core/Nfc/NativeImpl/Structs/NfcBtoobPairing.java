package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.List;

public class NfcBtoobPairing extends Structure{
    public static class ByReference extends NfcBtoobPairing implements Structure.ByReference {}
    public static class ByValue extends NfcBtoobPairing implements Structure.ByValue {}

    /** Handover Carrier Power State (assumed enum as int) */
    public int power_state;

    /** Handover Bluetooth Carrier configuration record type (assumed enum as int) */
    public int type;

    /** Pointer to Handover Carrier Configuration record */
    public Pointer ndef;

    /** Length of the NDEF configuration record */
    public int ndef_length;

    /** Bluetooth address (6 bytes) */
    public byte[] address = new byte[6];

    /** Pointer to device name string */
    public Pointer device_name;

    /** Length of the device name */
    public int device_name_length;

    @Override
    protected List<String> getFieldOrder() {
        return List.of(
                "power_state",
                "type",
                "ndef",
                "ndef_length",
                "address",
                "device_name",
                "device_name_length"
        );
    }
}
