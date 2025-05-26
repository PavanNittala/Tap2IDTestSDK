package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bluez.GattCharacteristic1;
import org.bluez.GattManager1;
import org.bluez.LEAdvertisingManager1;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.interfaces.ObjectManager;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt.BLEPath.*;

public class GattServer {
    private static final Logger LOG = LoggerFactory.getLogger(GattServer.class);
    private static GattManager1 serviceManager = null;
    private static LEAdvertisingManager1 advertisingManager = null;

    private static BleApplication application = null;
    private static BleAdvertisement advertisement = null;
    private static DBusConnection dbusConn = null;
    private static byte[] identValue;

    public static DBusConnection GetConnection() throws DBusException {
        if (dbusConn == null || !dbusConn.isConnected()) {
            dbusConn = DBusConnectionBuilder.forSystemBus().build();
        }
        return dbusConn;
    }

    private static void SetConnetion(DBusConnection dbusConn)
    {
        GattServer.dbusConn = dbusConn;
    }

    public static byte[] GetIdentValue()
    {
        return GattServer.identValue;
    }

    private static void SetIdentValue(byte[] identValue)
    {
        GattServer.identValue = identValue;
    }

    public void SetupGattServer(String serviceUUID, byte[] identValue, BLECallback bleCallback) throws DBusException {
        try {
            dbusConn = DBusConnectionBuilder.forSystemBus().build();
            ObjectManager adapter = dbusConn.getRemoteObject("org.bluez", "/", ObjectManager.class);
            if (adapter == null || adapter.GetManagedObjects() == null) {
                LOG.error("cannot find bluez");
                return;
            }
            DBusPath serviceManagerPath = getServiceManagerPath(adapter);
            if (serviceManagerPath == null) {
                LOG.error("cannot find bluez service manager");
                return;
            }
            LOG.info("ServiceManagerPath 111 : " + serviceManagerPath);
            serviceManager = dbusConn.getRemoteObject("org.bluez", serviceManagerPath.getPath(), GattManager1.class);
            advertisingManager = dbusConn.getRemoteObject("org.bluez", serviceManagerPath.getPath(), LEAdvertisingManager1.class);

            SetIdentValue(identValue);
            SetConnetion(dbusConn);

            dbusConn.addSigHandler(Properties.PropertiesChanged.class, new DBusSigHandler<Properties.PropertiesChanged>() {
                @Override
                public void handle(Properties.PropertiesChanged signal) {
                    String iface = signal.getInterfaceName();
                    if (!"org.bluez.Device1".equals(iface)) return;

                    Map<String, Variant<?>> props = signal.getPropertiesChanged();
                    if (props.containsKey("Connected")) {
                        boolean connected = (Boolean) props.get("Connected").getValue();
                        if(!connected){
                            bleCallback.onDisconnected();
                        }
                    }else if(props.containsKey("ServicesResolved")){
                        boolean ServicesResolved = (Boolean) props.get("ServicesResolved").getValue();
                        if(ServicesResolved){
                            bleCallback.onConnected();
                        }
                    }
                }
            });

            // State
            StateCharacteristic stateCharacteristic = new StateCharacteristic(STATE_PATH,
                    new String[]{"notify", "write-without-response"},
                    isoUUID.stateUuid,
                    SERVICE_PATH, bleCallback);

            Client2ServerCharacteristic client2ServerCharacteristic = new Client2ServerCharacteristic(CLIENT_TO_SERVER_PATH,
                    new String[]{"write-without-response"},
                    isoUUID.client2ServerUuid,
                    SERVICE_PATH, bleCallback);

            // Server2Client
            Server2ClientCharacteristic server2ClientCharacteristic = new Server2ClientCharacteristic(SERVER_TO_CLIENT_PATH,
                    new String[]{"notify"},
                    isoUUID.server2ClientUuid,
                    SERVICE_PATH);

            // Ident
            IdentCharacteristic identCharacteristic = new IdentCharacteristic(IDENT_PATH,
                    new String[]{"read", "write", "notify"},
                    isoUUID.identUuid,
                    SERVICE_PATH, bleCallback);

            List<BleCharacteristic> characteristics = new ArrayList<>();

            characteristics.add(stateCharacteristic);
            characteristics.add(client2ServerCharacteristic);
            characteristics.add(server2ClientCharacteristic);
            characteristics.add(identCharacteristic);

            BleService service = new BleService(SERVICE_PATH, serviceUUID, true, characteristics);
            List<BleService> allServices = new ArrayList<>();
            allServices.add(service);
            application = new BleApplication(APPLICATION_PATH, allServices);

            LOG.info("Now Doing Export All");

            exportAll(dbusConn, application);
            LOG.info("App Path: " + application.getObjectPath());
            serviceManager.RegisterApplication(new DBusPath(application.getObjectPath()), new HashMap<>());

            advertisement = new BleAdvertisement(ADVERTISEMENT_PATH, PERIPHERAL_NAME, DEVICE_MODE, serviceUUID);
            dbusConn.exportObject(advertisement);
            LOG.info("Gatt application registered");

            advertisingManager.RegisterAdvertisement(new DBusPath(advertisement.getObjectPath()), new HashMap<>());
            LOG.info("Gatt application advertised");

        } catch (DBusException e) {
            throw new RuntimeException(e);
        }
    }

    private static DBusPath getServiceManagerPath(ObjectManager adapter) {
        for (Map.Entry<DBusPath, Map<String, Map<String, Variant<?>>>> cur : adapter.GetManagedObjects().entrySet()) {
            if (!cur.getValue().containsKey("org.bluez.GattManager1")) {
                continue;
            }
            return cur.getKey();
        }
        return null;
    }
//    private static void exportAll(DBusConnection dbusConn, BleApplication application) throws DBusException {
//        for (BleService cur : application.getServices()) {
//            for (BleCharacteristic curChar : cur.getCharacteristics()) {
//                LOG.info("Exporting curChar: " + curChar.getObjectPath());
//                dbusConn.exportObject(curChar);
//            }
//            dbusConn.exportObject(cur);
//        }
//        dbusConn.exportObject(application);
//    }

    private static void exportAll(DBusConnection dbusConn, BleApplication application) throws DBusException {
        for (BleService cur : application.getServices()) {
            for (BleCharacteristic curChar : cur.getCharacteristics()) {
                try {
                    dbusConn.exportObject(curChar);
                } catch (DBusException e) {
                    LOG.error("Failed to export characteristic: " + curChar.getObjectPath(), e);
                    // Log introspection XML if possible
                    throw e;
                }
            }
            dbusConn.exportObject(cur);
        }
        dbusConn.exportObject(application);
    }

    private static void unExportAll(DBusConnection dbusConn, BleApplication application) {
        for (BleService cur : application.getServices()) {
            for (BleCharacteristic curChar : cur.getCharacteristics()) {
                dbusConn.unExportObject(curChar.getObjectPath());
            }
            dbusConn.unExportObject(cur.getObjectPath());
        }
        dbusConn.unExportObject(application.getObjectPath());
    }


    public CompletableFuture<Void> SendDataAsync(byte[] data) {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Pavankn Sending Some Data");
                Map<String, Variant<?>> val = new HashMap<>();
                val.put("Value", new Variant<>(data));

                org.freedesktop.dbus.interfaces.Properties.PropertiesChanged signal =
                        new org.freedesktop.dbus.interfaces.Properties.PropertiesChanged(SERVER_TO_CLIENT_PATH,
                                GattCharacteristic1.class.getName(),
                                val, new ArrayList<>());

                /*Get the existing dbus connection and send a Message*/
                DBusConnection connection = GetConnection();
                if(connection != null){
                    LOG.info("Sending Data on ServerToClient");
                    connection.sendMessage(signal);
                }
            } catch (DBusException e) {
                e.printStackTrace();
            }
        });
    }

//    public void SendData(byte[] data) {
//        try {
//            System.out.println("Pavankn Sending Some Data");
//            Map<String, Variant<?>> val = new HashMap<>();
//            val.put("Value", new Variant<>(data));
//
//            org.freedesktop.dbus.interfaces.Properties.PropertiesChanged signal =
//                    new org.freedesktop.dbus.interfaces.Properties.PropertiesChanged(SERVER_TO_CLIENT_PATH,
//                            GattCharacteristic1.class.getName(),
//                            val, new ArrayList<>());
//
//            /*Get the existing dbus connection and send a Message*/
//            DBusConnection connection = GetConnection();
//            if(connection != null){
//                LOG.info("Sending Data on the Bus");
//                connection.sendMessage(signal);
//            }
//        } catch (DBusException e) {
//            e.printStackTrace();
//        }
//    }

    public CompletableFuture<Void> sendSessionTerminationAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                byte[] terminationData = new byte[]{0x02};
                System.out.println("Pavankn Sending Some Data");
                Map<String, Variant<?>> val = new HashMap<>();
                val.put("Value", new Variant<>(terminationData));

                org.freedesktop.dbus.interfaces.Properties.PropertiesChanged signal =
                        new org.freedesktop.dbus.interfaces.Properties.PropertiesChanged(STATE_PATH,
                                GattCharacteristic1.class.getName(),
                                val, new ArrayList<>());

                /*Get the existing dbus connection and send a Message*/
                DBusConnection connection = GetConnection();
                if(connection != null){
                    LOG.info("Sending Disconnect on State");
                    connection.sendMessage(signal);
                }
            } catch (DBusException e) {
                e.printStackTrace();
            }
        });
    }

//    public void sendSessionTermination()
//    {
//        LOG.info("Pavankn Inside Send Termination");
//        try {
//            byte[] terminationData = new byte[0x02];
//            System.out.println("Pavankn Sending Some Data");
//            Map<String, Variant<?>> val = new HashMap<>();
//            val.put("Value", new Variant<>(terminationData));
//
//            org.freedesktop.dbus.interfaces.Properties.PropertiesChanged signal =
//                    new org.freedesktop.dbus.interfaces.Properties.PropertiesChanged(STATE_PATH,
//                            GattCharacteristic1.class.getName(),
//                            val, new ArrayList<>());
//
//            /*Get the existing dbus connection and send a Message*/
//            DBusConnection connection = GetConnection();
//            if(connection != null){
//                LOG.info("Sending Data on the Bus");
//                connection.sendMessage(signal);
//            }
//        } catch (DBusException e) {
//            e.printStackTrace();
//        }
//    }

    public CompletableFuture<Void> Disconnect() {
        return CompletableFuture.runAsync(() -> {
            try {
                if (dbusConn != null) {
                    LOG.info("Disconnecting The Last GattServer...");

                    if (advertisingManager != null && advertisement != null) {
                        try {
                            advertisingManager.UnregisterAdvertisement(new DBusPath(advertisement.getObjectPath()));
                            LOG.info("Advertisement unregistered");
                        } catch (Exception e) {
                            LOG.warn("Failed to unregister advertisement: " + e.getMessage());
                        }
                    }

                    if (serviceManager != null && application != null) {
                        try {
                            serviceManager.UnregisterApplication(new DBusPath(application.getObjectPath()));
                            LOG.info("Application unregistered");
                        } catch (Exception e) {
                            LOG.warn("Failed to unregister application: " + e.getMessage());
                        }
                    }

                    try {
                        dbusConn.disconnect();
                        LOG.info("DBus connection closed");
                    } catch (Exception e) {
                        LOG.warn("Failed to disconnect DBus: " + e.getMessage());
                    }

                    dbusConn = null;
                    serviceManager = null;
                    advertisingManager = null;
                    application = null;
                    advertisement = null;

                    LOG.info("GattServer Disconnected Successfully");
                }
            } catch (Exception e) {
                LOG.error("Error during GattServer disconnect", e);
            }
        });
    }
}
