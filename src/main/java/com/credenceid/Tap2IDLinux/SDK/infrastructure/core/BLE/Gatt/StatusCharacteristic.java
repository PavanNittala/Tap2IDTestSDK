package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusCharacteristic extends BleCharacteristic {

    private static final Logger LOG = LoggerFactory.getLogger(StatusCharacteristic.class);
    private static final int PROTOCOL_VERSION = 2;

    public StatusCharacteristic(String objectPath, String[] flags, String uuId, String servicePath) {
        super(objectPath, flags, uuId, servicePath);
    }

    @Override
    public byte[] read(String bluetoothAddress) {
        // unsupported
        return new byte[0];
    }

    @Override
    public void write(byte[] value, String bluetoothAddress) {
        if (value.length < 2) {
            LOG.info("[{}] not enough bytes. expected 2, got: {}", bluetoothAddress, value.length);
            return;
        }
    }
}
