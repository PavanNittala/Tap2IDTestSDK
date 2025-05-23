package com.credenceid.Tap2IDLinux.SDK.Utils;

import java.util.ArrayList;
import java.util.List;

public class JavaUtils {
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
    public static List<byte[]> splitByteArray(byte[] input) {
        List<byte[]> result = new ArrayList<>();
        final int maxFrameSize = 512;
        int index = 0;

        while (index < input.length) {
            int remainingBytes = input.length - index;
            int frameSize = Math.min(maxFrameSize, remainingBytes + 1);
            byte[] frame = new byte[frameSize];

            // Determine if this is the last frame
            if (remainingBytes + 1 <= maxFrameSize) {
                frame[0] = 0x00; // Last frame starts with 0x00
            } else {
                frame[0] = 0x01; // All other frames start with 0x01
            }

            // Copy the bytes from input array to the frame
            System.arraycopy(input, index, frame, 1, frameSize - 1);

            result.add(frame);
            index += frameSize - 1;
        }

        return result;
    }
}
