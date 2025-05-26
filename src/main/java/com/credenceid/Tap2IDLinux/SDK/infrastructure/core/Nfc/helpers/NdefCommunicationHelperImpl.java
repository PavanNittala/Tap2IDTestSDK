package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers;

import com.credenceid.identity.iso18013.nfc.ndef.CapabilityFile;
import com.credenceid.identity.iso18013.nfc.ndef.Ndef;
import com.credenceid.identity.iso18013.nfc.ndef.NdefConstants;
import com.credenceid.identity.utils.MathUtilities;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NdefCommunicationHelperImpl implements NdefCommunicationHelper{
    private static final byte[] TAG_4_TYPE_DF_NAME = new byte[]{
            (byte) 0xD2, 0x76, 0x00, 0x00, (byte) 0x85, 0x01, 0x01
    };
    private static final byte CAPABILITY_FILE_START_OFFSET = 0x00;
    private static final byte NDEF_MESSAGE_START_OFFSET = 0x02;
    private static final int CAPABILITY_FILE_SIZE = 0x0F;
    private static final Logger LOG = LoggerFactory.getLogger(NdefCommunicationHelperImpl.class);

    private final ApduCommunicationHelper apduCommunicationHelper;

    public NdefCommunicationHelperImpl(ApduCommunicationHelper apduCommunicationHelper) {
        this.apduCommunicationHelper = apduCommunicationHelper;
    }

    @Override
    public Flowable<Ndef> readNdef() {
        return apduCommunicationHelper.startMonitoring()
                .map(ignored -> read());
    }

    private Ndef read() {
        try {
            apduCommunicationHelper.selectDfByName(TAG_4_TYPE_DF_NAME);
            // 2 - Selects capability file
            apduCommunicationHelper.selectMfDfOrEfById(NdefConstants.CAPABILITY_FILE_ID);
            // 3 - Reads capability container file
            CapabilityFile capabilityFile = new CapabilityFile(
                    apduCommunicationHelper.readSelectedFile(CAPABILITY_FILE_START_OFFSET, CAPABILITY_FILE_SIZE)
            );
            // 4 - Selects Ndef file
            apduCommunicationHelper.selectMfDfOrEfById(capabilityFile.getFileIdentifier());
            // 5 - Reads Ndef file
            byte[] ndefContent = readSelectedFile(NDEF_MESSAGE_START_OFFSET);

            return new Ndef(ndefContent);
        } catch (RuntimeException e) {
            LOG.info("Error reading NDEF", e);
            return null;
        }
    }

    private byte[] readSelectedFile(byte offset) {
        int nbBytesToRead = 0;
        int retries = 15;
        while (retries > 0) {
            // 1 - Read file size
            if (nbBytesToRead == 0) {
                nbBytesToRead = apduCommunicationHelper.getSelectedFileSize();
            }
            // 2 - Read file content
            if (nbBytesToRead > 0) {
                return apduCommunicationHelper.readSelectedFile(offset, nbBytesToRead);
            } else {
                retries--;
                try {
                    Thread.sleep(16);
                } catch (Exception e) {
                    LOG.info("Error during retry sleep", e);
                }
            }
        }
        throw new IllegalStateException("Impossible to read NDEF file");
    }


    @Override
    public void writeSelectedFile(Ndef ndef) {
        // According to Type 4 Tag Operation Specification 5.4.7 Update procedure, the update
        // occurs in 3 steps:
        // 1 - Writes 0x0000 in NLEN (first 2 bytes of the record, the size of the NDEF file)
        byte[] zeroNlen = new byte[]{0x00, 0x00};
        apduCommunicationHelper.updateFile((short) 0x00, zeroNlen);

        // 2 - Writes the Ndef message at offset 2
        byte[] ndefBytes = ndef.toByteArray();
        apduCommunicationHelper.updateFile((short) 0x02, ndefBytes);

        // 3 - Writes size in NLEN
        byte[] size = MathUtilities.toByteArray((short) ndefBytes.length);
        apduCommunicationHelper.updateFile((short) 0x00, size);
    }

    @Override
    public Ndef readSelectedFile() {
        try {
            return new Ndef(readSelectedFile(NDEF_MESSAGE_START_OFFSET));
        } catch (Exception e) {
            LOG.info("Error reading selected file", e);
            return null;
        }
    }

    @Override
    public void pauseReading(boolean pause) {
        apduCommunicationHelper.pauseReading(pause);
    }
}
