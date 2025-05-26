package com.credenceid.Tap2IDLinux.SDK.application.ISO18013;

import co.nstant.in.cbor.model.DataItem;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt.BLECallback;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt.GattServer;
import com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt.Utils;
import com.credenceid.identity.iso18013.BleDataRetrievalAddress;
import com.credenceid.identity.iso18013.DataRetrievalAddress;
import com.credenceid.identity.iso18013.SessionEncryptionReader;
import com.credenceid.identity.iso18013.Util;
import com.credenceid.identity.iso18013.communication.Communication;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static com.credenceid.Tap2IDLinux.SDK.Utils.JavaUtils.splitByteArray;


public class CommunicationBleCentralClientMode extends  Communication{
    private static final Logger LOG = LoggerFactory.getLogger(CommunicationBleCentralClientMode.class);
    private GattServer gattServer = new GattServer();

    @Override
    public Completable connect(DataRetrievalAddress address, byte[] encodedDeviceEngagement) {
        return Completable.create(new CompletableOnSubscribe() {

            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                System.out.println("Pavankn I am inside Subscribe");
                DataItem deDataItem = Util.cborDecode(encodedDeviceEngagement);
                System.out.println("Pavankn deDataItem : " + deDataItem);

                setConnectEmitter(emitter);

                List<DataItem> extractedList = Util.cborMapExtractArray(deDataItem, 1);

                System.out.println("Pavankn extractedListSize : " + extractedList.size());
                System.out.println("Pavankn extractedList : " + extractedList);

                if(extractedList.size() <= 1){
                    throw new IllegalStateException("Expected at least 2 elements in CBOR array, found: " + extractedList.size());
                }

                DataItem eDeviceKeyBytesDataItem = extractedList.get(1);
                byte[] encodedEDeviceKeyBytes = Util.cborEncode(eDeviceKeyBytesDataItem);
                byte[] ikm = encodedEDeviceKeyBytes;
                byte[] info = new byte[]{
                        (byte) 'B', (byte) 'L', (byte) 'E', (byte) 'I',
                        (byte) 'd', (byte) 'e', (byte) 'n', (byte) 't'
                };
                byte[] salt = new byte[0];
                byte[] identValue = Util.computeHkdf("HmacSha256", ikm, salt, info, 16);

                System.out.println("Pavankn identValue : " +  identValue);

                System.out.println("Pavankn Reached This State : ");

                BleDataRetrievalAddress bleDataRetrievalAddress = (BleDataRetrievalAddress) address;
                UUID serviceUuid = bleDataRetrievalAddress.getUuid();

                System.out.println("Hurray, Received ServiceUUID: " + serviceUuid);

                LOG.info("Nittala Java Thread: " + Thread.currentThread().getName() +
                        " [ID=" + Thread.currentThread().getId() + "]");

                gattServer.SetupGattServer(serviceUuid.toString(), identValue, new BLECallback() {
                    @Override
                    public void onSuccess() {
                        LOG.info("Setup BluetoothServer Success");
                        connectEmitter.onComplete();
                    }

                    @Override
                    public void onConnected() {
                        LOG.info("OnDeviceConnected");
                    }

                    @Override
                    public void onIdentRead() {
                        LOG.info("OnIdentRead");
                    }

                    @Override
                    public void onClient2ServerRead(byte[] received) {
                        LOG.info("onClient2ServerRead");
                        sendReceiveEmitter.onSuccess(received);
                        sendSessionTermination();
                    }

                    @Override
                    public void onDisconnected() {
                        LOG.info("onDisconnected");
                        gattServer.Disconnect().thenRun(() -> {
                            LOG.info("GattServer Disconnected Successfully");
                        });
                    }
                });
            }
        }).timeout(50, java.util.concurrent.TimeUnit.SECONDS, Completable.error(new TimeoutException("Bluetooth setup timed out")))
                .doOnError(error -> LOG.error("Connect failed: {}", error.getMessage(), error))
                .doOnComplete(() -> LOG.info("Connect completed"));

    }

    @Override
    public void disconnect() {
        LOG.info("Disconnect BluetoothServer");
        gattServer.sendSessionTerminationAsync();
    }

    @Override
    public void send(SessionEncryptionReader sessionEncryptionReader, byte[] bytes) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<byte[]> splitFrames = splitByteArray(bytes);

        for(byte[] frame: splitFrames){
            LOG.info("Sending Frame: " + Utils.bytesToHex(frame));
            CompletableFuture<Void> future = gattServer.SendDataAsync(frame);
            futures.add(future);
        }
        // Optionally wait for all frames to finish sending
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> LOG.info("All frames sent successfully"));
    }

    @Override
    public void sendSessionTermination() {
        gattServer.sendSessionTerminationAsync().thenRun(() -> {
            LOG.info("Termination Done Successfully, Now Disconnect");
            gattServer.Disconnect().thenRun(() -> {
                LOG.info("GattServer Disconnected Successfully");
            });
        });
    }
}
