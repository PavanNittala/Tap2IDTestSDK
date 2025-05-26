package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

public class Utils {
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}
