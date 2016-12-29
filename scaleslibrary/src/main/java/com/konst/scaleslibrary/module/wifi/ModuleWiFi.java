package com.konst.scaleslibrary.module.wifi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.konst.scaleslibrary.module.*;
import com.konst.scaleslibrary.module.bluetooth.BluetoothProcessManager;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.scale.ObjectScales;
import com.konst.scaleslibrary.module.scale.ScaleModule;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Kostya on 26.12.2016.
 */
public class ModuleWiFi extends Module {
    private static ModuleWiFi instance;
    private WifiBaseManager wifiBaseManager;
    private ClientWiFi clientWiFi;
    private static InterfaceCallbackScales interfaceCallbackScales;
    private static final String SSID = "SCALES.ESP.36.6.4";
    private static final String KEY = "12345678";

    protected ModuleWiFi(Context context, String version, InterfaceCallbackScales event) throws Exception{
        super(context, version);
        interfaceCallbackScales = event;
        wifiBaseManager = new WifiBaseManager(context,SSID,KEY, onWifiBaseManagerListener);
    }

    public static void create(Context context, String moduleVersion, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        instance = new ModuleWiFi(context, moduleVersion, event);
    }

    @Override
    public void write(String command) {
        clientWiFi.write(command);
    }

    @Override
    public ObjectCommand sendCommand(Commands commands) {
        return clientWiFi.sendCommand(commands);
    }

    @Override
    public void dettach() {

    }

    protected void attach(InetSocketAddress ipAddress) {
        if (clientWiFi !=null){
            clientWiFi.killWorkingThread();
        }
        try {
            clientWiFi = new ClientWiFi(getContext(), ipAddress);
            //threadAttach.setPriority(Thread.MAX_PRIORITY);
            clientWiFi.restartWorkingThread();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected boolean isVersion() throws Exception {
        Commands.setInterfaceCommand(this);
        String vrs = getModuleVersion(); //Получаем версию весов
        if (vrs.startsWith(versionName)) {
            try {
                String s = vrs.replace(versionName, "");
                versionNum = Integer.valueOf(s);
                //setVersion(fetchVersion(numVersion));
                version = fetchVersion(versionNum);
            } catch (Exception e) {
                throw new Exception(e);
            }
            /* Если версия правильная создаем обьек и посылаем сообщения. */
            objectScales = new ObjectScales();
            return true;
        }
        throw new Exception("Это не весы или неправильная версия!!!");
    }

    @Override
    protected void reconnect() {

    }

    @Override
    protected void load() {
        try {
            version.extract();
            interfaceCallbackScales.onCreate(instance);
        }  catch (ErrorTerminalException e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_TERMINAL_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        } catch (Exception e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_MODULE_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        }
    }

    @Override
    protected void connect() {

    }

    WifiBaseManager.OnWifiBaseManagerListener onWifiBaseManagerListener = new WifiBaseManager.OnWifiBaseManagerListener() {
        @Override
        public void onConnect(String ssid, InetSocketAddress ipAddress) {
            attach(ipAddress);
        }

        @Override
        public void onDisconnect() {
            dettach();
        }
    };


}
