package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;

public interface ApduCommunicationHelper {
    /**
     * Starts monitoring for a NFC tag to send APDU to
     */
    @NonNull Flowable<Object> startMonitoring();

    /**
     * Pauses the startMonitoring flow
     *
     * @param pause If true, the startTagMonitoring flow will emit when a tag is detected
     */
    void pauseReading(boolean pause);

    /**
     * Selects a DF by its name
     *
     * @param dfName DF name
     * @throws IllegalStateException in case of an incorrect APDU response
     */
    void selectDfByName(byte[] dfName);

    /**
     * Selects a file by id
     *
     * @param id ID
     * @throws IllegalStateException in case of an incorrect APDU response
     */
    void selectMfDfOrEfById(byte[] id);

    /**
     * Returns selected file size
     *
     * @return Selected file size
     * @throws IllegalStateException in case of APDU status different from 90 00
     */
    int getSelectedFileSize();

    /**
     * Reads a file
     *
     * @param offset   Offset at which the file starts
     * @param fileSize Nb bytes to read
     * @return File content as byte array
     */
    byte[] readSelectedFile(byte offset, int fileSize);

    /**
     * Updates a file with data starting at offset
     */
    void updateFile(short offset, byte[] data);
}
