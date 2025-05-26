package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;

public interface NfcCommunication {
    /**
     * Starts monitoring for NFC tags
     *
     * @return Flowable of boolean
     */
    @NonNull Flowable<Object> startMonitoring();

    /**
     * Sends APDU to tag, and reads response
     *
     * @param command A byte array containing an APDU
     * @return A byte array containing answer from tag
     */
    byte[] transceiveDataToTag(byte[] command);

    /**
     * Pauses the startMonitoring flow
     *
     * @param pause If true, the startTagMonitoring flow will emit when a tag is detected
     */
    void pauseReading(boolean pause);

    /** Disposes the NfcCommunication object */
    void dispose();
}
