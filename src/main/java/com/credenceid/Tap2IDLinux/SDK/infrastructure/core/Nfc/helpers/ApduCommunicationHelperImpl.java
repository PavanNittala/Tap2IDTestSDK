package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.helpers;

import com.credenceid.identity.iso18013.Util;
import com.credenceid.identity.iso7816.Apdu;
import com.credenceid.identity.iso7816.ApduResponse;
import com.credenceid.identity.utils.MathUtilities;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ApduCommunicationHelperImpl implements ApduCommunicationHelper{
    private static final Logger LOG = LoggerFactory.getLogger(ApduCommunicationHelperImpl.class);

    private final NfcCommunication nfcCommunication;

    public ApduCommunicationHelperImpl(NfcCommunication nfcCommunication) {
        this.nfcCommunication = nfcCommunication;
    }

    @Override
    public @NonNull Flowable<Object> startMonitoring() {
        return nfcCommunication.startMonitoring();
    }

    @Override
    public void pauseReading(boolean pause) {
        nfcCommunication.pauseReading(pause);
    }

    @Override
    public void selectDfByName(byte[] dfName) {
        try {
            byte[] command = Apdu.selectDfByName(dfName);
            LOG.info("selectDfByName write >> " + Util.toHex(command));
            ApduResponse apduResponse = new ApduResponse(nfcCommunication.transceiveDataToTag(command));
            logApduResponse(apduResponse);

            if (!apduResponse.isStatusOK()) {
                throw new IllegalStateException(
                        "APDU status is: " + Util.toHex(apduResponse.getStatus()) + ", expected 9000"
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void selectMfDfOrEfById(byte[] id) {
        byte[] command = Apdu.selectMfDfOrEfById(id);
        LOG.info("write >> " + Util.toHex(command));
        ApduResponse apduResponse = new ApduResponse(nfcCommunication.transceiveDataToTag(command));
        logApduResponse(apduResponse);

        if (!apduResponse.isStatusOK()) {
            throw new IllegalStateException(
                    "APDU status is: " + Util.toHex(apduResponse.getStatus()) + ", expected 9000"
            );
        }
    }

    @Override
    public int getSelectedFileSize() {
        byte[] command = Apdu.readBinaryFile((short) 0x00, (byte) 0x02);
        LOG.info("write >> " + Util.toHex(command));
        ApduResponse apduResponse = new ApduResponse(nfcCommunication.transceiveDataToTag(command));
        logApduResponse(apduResponse);

        if (!apduResponse.isStatusOK()) {
            throw new IllegalStateException(
                    "APDU status is: " + Util.toHex(apduResponse.getStatus()) + ", expected 9000"
            );
        }

        byte[] data = apduResponse.getData();
        return (data[0] & 0xFF) * 256 + (data[1] & 0xFF);
    }

    @Override
    public byte[] readSelectedFile(byte offset, int fileSize) {
        LOG.info("File to read size: " + fileSize);

        if (fileSize > 0) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            short offsetInFile = offset;
            int numberOfChunks = MathUtilities.getNumberOfChunks(fileSize);
            int remainingBytes = fileSize;

            for (int i = 0; i < numberOfChunks; i++) {
                byte toRead = remainingBytes >= 256 ? (byte) 0xFF : (byte) remainingBytes;
                byte[] command = Apdu.readBinaryFile(offsetInFile, toRead);
                LOG.info("readSelectedFile: >> " + Util.toHex(command));
                ApduResponse apduResponse = new ApduResponse(nfcCommunication.transceiveDataToTag(command));
                logApduResponse(apduResponse);

                if (!apduResponse.isStatusOK()) {
                    throw new IllegalStateException(
                            "APDU status is: " + Util.toHex(apduResponse.getStatus()) + ", expected 9000"
                    );
                }

                try {
                    baos.write(apduResponse.getData());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                remainingBytes -= toRead & 0xFF;
                offsetInFile += toRead & 0xFF;
            }

            return baos.toByteArray();
        } else {
            try {
                Thread.sleep(16);
            } catch (Exception e) {
                LOG.info("Error during sleep", e);
            }
        }

        throw new IllegalStateException("Impossible to read NDEF file");
    }

    @Override
    public void updateFile(short offset, byte[] data) {
        byte[] apdu = Apdu.updateBinaryFile(offset, data);
        LOG.info("write >> " + Util.toHex(data));
        ApduResponse apduResponse = new ApduResponse(nfcCommunication.transceiveDataToTag(apdu));

        if (!apduResponse.isStatusOK()) {
            throw new IllegalStateException(
                    "APDU status is: " + Util.toHex(apduResponse.getStatus()) + ", expected 9000"
            );
        }
    }
    private void logApduResponse(ApduResponse apduResponse) {
        if (apduResponse.getStatus() != null) {
            LOG.info(String.format("read: << status %s", Util.toHex(apduResponse.getStatus())));
        }
        if (apduResponse.getData() != null) {
            LOG.info(String.format("read: << data %s", Util.toHex(apduResponse.getData())));
        }
    }
}
