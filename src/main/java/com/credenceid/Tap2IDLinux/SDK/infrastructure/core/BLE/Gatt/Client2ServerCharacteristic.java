package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client2ServerCharacteristic extends BleCharacteristic implements AutoCloseable{
    private BLECallback bleCallback;
    private static final Logger LOG = LoggerFactory.getLogger(Client2ServerCharacteristic.class);
    private static ByteArrayOutputStream incomingMessage = new ByteArrayOutputStream();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Client2ServerCharacteristic(String objectPath, String[] flags, String uuid, String servicePath, BLECallback callback) {
        super(objectPath, flags, uuid, servicePath);
        this.bleCallback = callback;
    }

    @Override
    public byte[] read(String bluetoothAddress) {
        return new byte[0];
    }

    @Override
    public void write(byte[] value, String bluetoothAddress) {
        executor.execute(() -> {
            // Append value excluding the first byte
            if (value.length > 1) {
                incomingMessage.write(value, 1, value.length - 1);
            }

            // If first byte is 0x00, it's the last chunk
            if (value[0] == (byte) 0x00) {
                byte[] entireMessage = incomingMessage.toByteArray();
                incomingMessage.reset();
                if (bleCallback != null) {
                    bleCallback.onClient2ServerRead(entireMessage);
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        executor.shutdown(); // allow current tasks to finish
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // force shutdown if not done
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // preserve interrupt
        }
    }
}
