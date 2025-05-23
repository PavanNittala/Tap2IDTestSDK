package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentCharacteristic extends BleCharacteristic {
    private BLECallback bleCallback;
    private static final Logger LOG = LoggerFactory.getLogger(IdentCharacteristic.class);

    public IdentCharacteristic(String objectPath, String[] flags, String uuid, String servicePath, BLECallback callback) {
        super(objectPath, flags, uuid, servicePath);
        this.bleCallback = callback;
    }

    @Override
    public byte[] read(String bluetoothAddress) {
        LOG.info("Inside Ident ReadValue");
        return GattServer.GetIdentValue();
    }

    @Override
    public void write(byte[] value, String bluetoothAddress) {
        LOG.info("Inside Ident WriteValue");
    }
}
