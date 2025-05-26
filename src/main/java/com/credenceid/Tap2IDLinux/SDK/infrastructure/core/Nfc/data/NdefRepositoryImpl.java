package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.data;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers.NdefCommunicationHelper;
import com.credenceid.identity.iso18013.BleAlternativeCarrierConfiguration;
import com.credenceid.identity.iso18013.CollisionRecord;
import com.credenceid.identity.iso18013.Pair;
import com.credenceid.identity.iso18013.nfc.handover.HandoverRequest;
import com.credenceid.identity.iso18013.nfc.handover.HandoverRequestRecord;
import com.credenceid.identity.iso18013.nfc.ndef.Ndef;
import com.credenceid.identity.iso18013.tnep.*;
import com.credenceid.identity.transactionPerf.DeviceEngagementPerfLogger;
import com.credenceid.identity.utils.ExceptionHandler;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

public class NdefRepositoryImpl implements NdefRepository{
    private UUID _uuid;
    private static final Logger LOG = LoggerFactory.getLogger(NdefRepositoryImpl.class);
    private final NdefCommunicationHelper ndefCommunicationHelper;
    private final DeviceEngagementPerfLogger deviceEngagementPerfLogger;
    private UUID uuid;

    public NdefRepositoryImpl(
            NdefCommunicationHelper ndefCommunicationHelper,
            DeviceEngagementPerfLogger deviceEngagementPerfLogger
    ) {
        this.ndefCommunicationHelper = ndefCommunicationHelper;
        this.deviceEngagementPerfLogger = deviceEngagementPerfLogger;
    }

    public HandoverRequest generateHandoverRequest() {
        _uuid = UUID.randomUUID();
        BleAlternativeCarrierConfiguration bleAlternativeCarrierConfiguration =
                new BleAlternativeCarrierConfiguration((byte) 0x02, _uuid);

        byte[] random = new byte[2];
        new Random().nextBytes(random);

        HandoverRequestRecord handoverRequestRecord = new HandoverRequestRecord();
        handoverRequestRecord.addCollisionRecord(new CollisionRecord(random));

        HandoverRequest handoverRequest = new HandoverRequest(handoverRequestRecord);
        handoverRequest.addAlternativeCarrierConfiguration(bleAlternativeCarrierConfiguration);

        return handoverRequest;
    }

    @Override
    public  @NonNull Flowable<Pair<Ndef, HandoverRequest>> readNdef() {
        LOG.info("Pavankn ReadNdef 1111");
        return ndefCommunicationHelper.readNdef()
                .map(ndef -> {
                    if (ndef != null) {
                        if (TnepMessage.isTnep(ndef)) {
                            // Negotiated handover
                            LOG.debug("Negotiated handover detected");
                            HandoverRequest handoverRequest = generateHandoverRequest();
                            Ndef deNdef = null;
                            try {
                                deNdef = performNegotiatedHandover(ndef, handoverRequest);
                            } catch (Exception e) {
                                // Handle exception silently as per original logic
                            }
                            // NFC engagement ends here
                            deviceEngagementPerfLogger.setEngagementEndTimeInMillis(System.currentTimeMillis());
                            return new Pair<>(deNdef, handoverRequest);
                        } else {
                            // Static handover
                            LOG.debug("Static handover detected");
                            // NFC engagement ends here
                            deviceEngagementPerfLogger.setEngagementEndTimeInMillis(System.currentTimeMillis());
                            return new Pair<>(ndef, null);
                        }
                    }
                    return new Pair<>(null, null);
                });
    }

    @Override
    public void pauseReading(boolean pause) {
        ndefCommunicationHelper.pauseReading(pause);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Performs a NFC negotiated handover
     *
     * @param ndef Read NDEF containing the first negotiated handover message
     * @param handoverRequest Handover request containing the alternative carrier configurations
     * @return Device engagement Ndef or null
     */
    private Ndef performNegotiatedHandover(Ndef ndef, HandoverRequest handoverRequest) {
        // Checks that the current TNEP message is a service parameters record
        LOG.debug("Checks that the current TNEP message is a service parameters record");
        if (TnepMessage.getType(ndef) != TnepMessageType.ServiceParametersRecord) {
            return null;
        }

        // Gets Service Parameters Record
        ServiceParametersRecord serviceParametersRecord = new ServiceParametersRecord(ndef);

        // Builds Service Select to be written back
        Ndef serviceSelectNdef = ServiceSelect.getNdef(serviceParametersRecord.getService());

        // Writes the service select NDEF message
        try {
            LOG.debug("Writes the service select NDEF message");
            ndefCommunicationHelper.writeSelectedFile(serviceSelectNdef);
        } catch (Exception e) {
            return null;
        }

        LOG.debug("Reads status");
        Ndef ndefStatus = ndefCommunicationHelper.readSelectedFile();

        if (ndefStatus != null) {
            if (TnepMessage.getType(ndefStatus) != TnepMessageType.Status) {
                throw new IllegalStateException(
                        "TnepMessage type is " + TnepMessage.getType(ndefStatus) + ", expected Status"
                );
            }
        } else {
            return null;
        }

        Status status = new Status(ndefStatus);

        if (status.getCode() != 0x00) {
            try {
                throw ExceptionHandler.getInstance().getException(
                        ExceptionHandler.ExceptionTypes.EXCEPTION_TYPE_NFC_HANDOVER,
                        "Incorrect status " + status.getCode() + ", expected 0",
                        null
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ndefCommunicationHelper.writeSelectedFile(handoverRequest.getNdef());

        return ndefCommunicationHelper.readSelectedFile();
    }

}
