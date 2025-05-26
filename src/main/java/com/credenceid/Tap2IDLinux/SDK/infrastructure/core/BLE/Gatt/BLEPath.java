package com.credenceid.Tap2IDLinux.SDK.infrastructure.core.BLE.Gatt;

public class BLEPath {

    public static final String TESTING_ONLY_SERVICE_UUID = "3f5f0b4d-e311-4921-b29d-936afb8734cc";
    public static final String APPLICATION_PATH = "/org/bluez/credenceid";
    public static final String SERVICE_PATH = APPLICATION_PATH + "/service0";

    public static final String ADVERTISEMENT_PATH = APPLICATION_PATH + "/advertisement0";
    public static final String STATUS_PATH = SERVICE_PATH + "/char1";

    public static final String STATE_PATH = SERVICE_PATH + "/state";
    public static final String CLIENT_TO_SERVER_PATH = SERVICE_PATH + "/clientToServer";
    public static final String SERVER_TO_CLIENT_PATH = SERVICE_PATH + "/serverToClient";
    public static final String IDENT_PATH = SERVICE_PATH + "/bleident";
    public static final String PERIPHERAL_NAME = "Tap2IDLite";
    public static final String DEVICE_MODE = "peripheral";
}
