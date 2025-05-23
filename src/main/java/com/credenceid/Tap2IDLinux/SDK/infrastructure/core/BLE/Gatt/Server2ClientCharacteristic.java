package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server2ClientCharacteristic extends BleCharacteristic{
    private static final Logger LOG = LoggerFactory.getLogger(Server2ClientCharacteristic.class);

    public Server2ClientCharacteristic(String objectPath, String[] flags, String uuid, String servicePath) {
        super(objectPath, flags, uuid, servicePath);
    }

    @Override
    public byte[] read(String bluetoothAddress) {
        return new byte[0];
    }

    @Override
    public void write(byte[] value, String bluetoothAddress) {
        LOG.info("Inside Write, Send bytes on the bus");
    }
}
