package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.data;

import com.credenceid.identity.iso18013.Pair;
import com.credenceid.identity.iso18013.nfc.handover.HandoverRequest;
import com.credenceid.identity.iso18013.nfc.ndef.Ndef;
import io.reactivex.rxjava3.core.Flowable;


import java.util.UUID;

public interface NdefRepository {
    /**
     * Reads the Device engagement NDEF file
     *
     * @return Flow of Pair containing the final Device Engagement NDEF
     * and a handover request which is either negotiated or static (null)
     */
    Flowable<Pair<Ndef, HandoverRequest>> readNdef();

    /**
     * Pauses the NDEF flow
     *
     * @param pause If true, the flow will emit when a tag is detected, else not
     */
    void pauseReading(boolean pause);

    /**
     * Gets the UUID
     *
     * @return The UUID or null
     */
    UUID getUuid();
}
