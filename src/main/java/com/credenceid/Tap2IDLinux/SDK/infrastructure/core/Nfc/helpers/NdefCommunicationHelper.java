package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers;
import com.credenceid.identity.iso18013.nfc.ndef.Ndef;
import io.reactivex.rxjava3.core.Flowable;

public interface NdefCommunicationHelper {
    /**
     * Returns a flow emitting NDEF device engagement
     *
     * @return Flowable<Ndef>
     */
    Flowable<Ndef> readNdef();

    /**
     * Writes the content of the Ndef in the current selected file
     *
     * @param ndef Data to be written in the selected file
     */
    void writeSelectedFile(Ndef ndef);

    /**
     * Writes the content of the current selected file
     *
     * @return ndef Data read
     */
    Ndef readSelectedFile();

    /**
     * Pauses the NDEF flow
     *
     * @param pause If true, the flow will emit when a tag is detected, else not
     */
    void pauseReading(boolean pause);
}
