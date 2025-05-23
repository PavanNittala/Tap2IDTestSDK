package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.List;

public class NfcWifiPairing extends Structure {
    public static class ByReference extends NfcWifiPairing implements Structure.ByReference {}
    public static class ByValue extends NfcWifiPairing implements Structure.ByValue {}

    /** Handover Carrier Power State (assumed enum as int) */
    public int power_state;

    /** Pointer to Handover Carrier Configuration record */
    public Pointer ndef;

    /** Length of the NDEF configuration record */
    public int ndef_length;

    /** Pointer to SSID */
    public Pointer ssid;

    /** Length of SSID */
    public int ssid_length;

    /** Pointer to network key */
    public Pointer key;

    /** Length of key */
    public int key_length;

    @Override
    protected List<String> getFieldOrder() {
        return List.of(
                "power_state",
                "ndef",
                "ndef_length",
                "ssid",
                "ssid_length",
                "key",
                "key_length"
        );
    }
}
