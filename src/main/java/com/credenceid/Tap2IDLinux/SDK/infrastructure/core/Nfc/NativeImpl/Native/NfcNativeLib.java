package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Native;

import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NdefInfo;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcFriendlyTypeRef;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.Nfc.NativeImpl.Structs.NfcTagCallback;
import com.sun.jna.Library;

public interface NfcNativeLib extends Library {
    //void nfcManager_registerTagCallback(nfcTagCallback_t.ByReference callback);
    int  doInitialize();
    int  doDeinitialize();
    void disableDiscovery();
    void doEnableDiscovery(int technologies_masks, int reader_only_mode, int enable_host_routing, int restart);
    void registerTagCallback(NfcTagCallback.ByReference callback);
    int  nfcHce_sendCommand(byte[] command, int command_length);
    int  nfcTag_transceive (int handle, byte[] tx_buffer, int tx_buffer_length,
                            byte[] rx_buffer, int rx_buffer_length, int timeout);
    int  nfcTag_doHandleReconnect(int handle);
    void deregisterTagCallback();
    int nfcTag_writeNdef(int handle, byte[] ndef_buffer, int ndef_buffer_length);
    int nfcTag_isNdef(int handle, NdefInfo.ByReference ndefInfo);
    int nfcTag_readNdef(int handle, byte[] ndef_buffer, int ndef_buffer_length, NfcFriendlyTypeRef.ByReference friendly_ndef_type);

}
