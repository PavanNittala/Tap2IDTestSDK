package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt.Utils.*;

public class StateCharacteristic extends BleCharacteristic {
    private static final Logger LOG = LoggerFactory.getLogger(StateCharacteristic.class);
    private BLECallback bleCallback;

    public StateCharacteristic(String objectPath, String[] flags, String uuid, String servicePath, BLECallback callback) {
        super(objectPath, flags, uuid, servicePath);
        this.bleCallback = callback;
    }

    @Override
    public byte[] read(String bluetoothAddress) {
        LOG.info("Inside Read Statechar");
        return new byte[0];
    }

    @Override
    public void write(byte[] value, String bluetoothAddress) {
        LOG.info("Received Write, Value: " + bytesToHex(value));
        if(value[0] == 0x01 && bleCallback!= null){
            bleCallback.onSuccess();
        }
    }
}
