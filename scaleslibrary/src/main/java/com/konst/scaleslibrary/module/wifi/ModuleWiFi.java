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
        super(context,version,event);
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
        isAttach = false;
        scalesProcessEnable(false);
        if (clientWiFi != null){
            clientWiFi.killWorkingThread();
        }
    }

    public void attach(InetSocketAddress ipAddress) {
        super.attach();
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
    protected void connect() {

    }

    @Override
    public void scalesProcessEnable(boolean process) {
        try {
            if (process)
                Commands.SPE.setParam(1);
            else
                Commands.SPE.setParam(0);
        }catch (NullPointerException e){
            Log.e(TAG, " "+e.getMessage());
        }
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
