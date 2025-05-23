package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.interfaces.ObjectManager;
import org.freedesktop.dbus.types.Variant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleApplication implements ObjectManager {

    private final String objectPath;
    private final List<BleService> services;

    public BleApplication(String objectPath, List<BleService> services) {
        this.objectPath = objectPath;
        this.services = services;
    }

    @Override
    public Map<DBusPath, Map<String, Map<String, Variant<?>>>> GetManagedObjects() {
        Map<DBusPath, Map<String, Map<String, Variant<?>>>> result = new HashMap<>();
        for (BleService service : services) {
            Map<String, Map<String, Variant<?>>> value = new HashMap<>();
            value.put("org.bluez.GattService1", service.GetAll(null));
            result.put(new DBusPath(service.getObjectPath()), value);
            for (BleCharacteristic characteristic : service.getCharacteristics()) {
                value = new HashMap<>();
                value.put("org.bluez.GattCharacteristic1", characteristic.GetAll(null));
                result.put(new DBusPath(characteristic.getObjectPath()), value);
            }
        }
        return result;
    }

    @Override
    public String getObjectPath() {
        return objectPath;
    }

    public List<BleService> getServices() {
        return services;
    }

}
