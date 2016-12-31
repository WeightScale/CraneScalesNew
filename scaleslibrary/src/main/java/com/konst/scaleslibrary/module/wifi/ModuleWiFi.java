package com.konst.scaleslibrary.module.wifi;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.konst.scaleslibrary.module.*;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;

import java.net.InetSocketAddress;

/**
 * @author Kostya on 26.12.2016.
 */
public class ModuleWiFi extends Module {
    private static ModuleWiFi instance;
    private final WifiBaseManager wifiBaseManager;
    private ClientWiFi clientWiFi;
    private static final String SSID = "SCALES.ESP.36.6.4";
    private static final String KEY = "12345678";

    protected ModuleWiFi(Context context, String version, InterfaceCallbackScales event) throws Exception{
        super(context, event);
        versionName = version;
        wifiBaseManager = new WifiBaseManager(context,SSID,KEY, onWifiBaseManagerListener);
    }

    public static void create(Context context, String moduleVersion, InterfaceCallbackScales event) throws Exception {
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
        super.dettach();
        wifiBaseManager.terminate();
        if (clientWiFi != null){
            clientWiFi.killWorkingThread();
        }
    }

    protected void attach(InetSocketAddress ipAddress) {
        if (clientWiFi !=null){
            clientWiFi.killWorkingThread();
        }
        try {
            clientWiFi = new ClientWiFi(getContext(), ipAddress);
            clientWiFi.restartWorkingThread();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void reconnect() {

    }

    @Override
    protected void load() {
        try {
            version.extract();
            resultCallback.onCreate(instance);
        }  catch (ErrorTerminalException e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_TERMINAL_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        } catch (Exception e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_MODULE_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        }
    }

    @Override
    protected void connect() {

    }

    public static ModuleWiFi getInstance() {return instance;}

    final WifiBaseManager.OnWifiBaseManagerListener onWifiBaseManagerListener = new WifiBaseManager.OnWifiBaseManagerListener() {
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
