package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Native;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NdefInfo;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcFriendlyTypeRef;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagCallback;
import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class NfcManager implements NfcNativeLib {
    private final NfcNativeLib nfcLib;
    private final String filePath = "/usr/lib/" + "libnfc_nci_linux.so";
    private static final Logger LOG = LoggerFactory.getLogger(NfcManager.class);
    private static final int MAX_BUFFER_SIZE = 1024;

    public NfcManager() throws IOException {
        nfcLib = Native.load(filePath, NfcNativeLib.class);
    }

    @Override
    public int doInitialize() {
        return nfcLib.doInitialize();
    }

    @Override
    public int doDeinitialize() {
        return nfcLib.doDeinitialize();
    }

    @Override
    public void disableDiscovery() {
        nfcLib.disableDiscovery();
    }

    public void doEnableDiscovery(int technologies_masks, int reader_only_mode,
                                  int enable_host_routing, int restart)
    {
        nfcLib.doEnableDiscovery(technologies_masks, reader_only_mode, enable_host_routing, restart);
    }

    @Override
    public void registerTagCallback(NfcTagCallback.ByReference callback) {
        nfcLib.registerTagCallback(callback);
    }

    @Override
    public int nfcHce_sendCommand(byte[] command, int command_length) {
        return nfcLib.nfcHce_sendCommand(command, command_length);
    }

    @Override
    public int nfcTag_transceive(int handle, byte[] tx_buffer, int tx_buffer_length, byte[] rx_buffer, int rx_buffer_length, int timeout) {
        return nfcLib.nfcTag_transceive(handle, tx_buffer, tx_buffer_length, rx_buffer, rx_buffer_length, timeout);
    }

    @Override
    public int nfcTag_doHandleReconnect(int handle) {
        return nfcLib.nfcTag_doHandleReconnect(handle);
    }

    @Override
    public void deregisterTagCallback() {
        nfcLib.deregisterTagCallback();
    }

    @Override
    public int nfcTag_writeNdef(int handle, byte[] ndef_buffer, int ndef_buffer_length) {
        return nfcLib.nfcTag_writeNdef(handle, ndef_buffer, ndef_buffer_length);
    }

    public byte[] transceiveDataToTag(int handle, int timeout, byte[] command)
    {
        byte[] rxBuf = new byte[MAX_BUFFER_SIZE];
        int ret = nfcTag_transceive(handle, command, command.length, rxBuf, rxBuf.length, timeout);

        if (ret <= 0 || ret > rxBuf.length) {
            // Handle error or invalid return length
            throw new RuntimeException("nfcTag_transceive failed or returned invalid length: " + ret);
        }

        byte[] result = Arrays.copyOf(rxBuf, ret);
        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    @Override
    public int nfcTag_isNdef(int handle, NdefInfo.ByReference ndefInfo) {
        LOG.info("Inside nfcTag-IsNdef");
        int ret = nfcLib.nfcTag_isNdef(handle, ndefInfo);
        return ret;
    }

    @Override
    public int nfcTag_readNdef(int handle, byte[] ndef_buffer, int ndef_buffer_length, NfcFriendlyTypeRef.ByReference friendly_ndef_type) {
        return nfcLib.nfcTag_readNdef(handle, ndef_buffer, ndef_buffer_length, friendly_ndef_type);
    }
}
