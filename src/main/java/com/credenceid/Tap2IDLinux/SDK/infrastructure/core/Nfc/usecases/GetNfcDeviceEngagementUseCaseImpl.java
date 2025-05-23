package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.usecases;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.data.NdefRepository;
import com.credenceid.identity.iso18013.DeviceEngagement;
import com.credenceid.identity.iso18013.nfc.handover.HandoverRequest;
import com.credenceid.identity.iso18013.nfc.ndef.Ndef;
import com.credenceid.identity.transactionPerf.DeviceEngagementPerfLogger;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class GetNfcDeviceEngagementUseCaseImpl implements GetNfcDeviceEngagementUseCase {
    private final NdefRepository ndefRepository;
    private final DeviceEngagementPerfLogger deviceEngagementPerfLogger;
    private static final Logger LOG = LoggerFactory.getLogger(GetNfcDeviceEngagementUseCaseImpl.class);

    public GetNfcDeviceEngagementUseCaseImpl(
            NdefRepository ndefRepository,
            DeviceEngagementPerfLogger deviceEngagementPerfLogger
    ) {
        this.ndefRepository = ndefRepository;
        this.deviceEngagementPerfLogger = deviceEngagementPerfLogger;
    }
    @Override
    public  @NonNull Flowable<DeviceEngagement> invoke() {
        LOG.info("Pavankn Reading NDEF");
        return ndefRepository.readNdef()
                .map(pair -> ndefToDeviceEngagement(pair.first, pair.second));
    }
    /**
     * Converts a Ndef to a DeviceEngagement
     *
     * @param ndef containing Device engagement
     * @param handoverRequest HandoverRequest
     * @return Device engagement
     */
    private DeviceEngagement ndefToDeviceEngagement(Ndef ndef, HandoverRequest handoverRequest) {
        if (ndef != null) {
            byte[] handoverRequestMessage = handoverRequest != null ? handoverRequest.getNdef().toByteArray() : null;

            try {
                // NFC engagement data parsing starts
                deviceEngagementPerfLogger.setEngagementParseStartTimeInMillis(System.currentTimeMillis());
                DeviceEngagement deviceEngagement = new DeviceEngagement(ndef, handoverRequestMessage);

                UUID uuid = ndefRepository.getUuid();
                if (uuid != null) {
                    setUuidIfEmpty(deviceEngagement.getAddresses(), uuid);
                }

                LOG.debug("Device engagement: " + deviceEngagement);
                return deviceEngagement;
            } catch (Exception e) {
                // If an error happens during DeviceEngagement instantiation, just returns null
                // The app is in charge of informing user to tap again
                return null;
            }
        } else {
            LOG.info("NDEF is null");
            return null;
        }
    }

    private void setUuidIfEmpty(Object addresses, UUID uuid) {
        // Implementation depends on the structure of addresses
        // This is a placeholder for the actual logic
    }
}
