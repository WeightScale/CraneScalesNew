package com.konst.scaleslibrary.module.usb;

import android.content.Context;
import android.util.Log;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.ObjectCommand;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.wifi.ClientWiFi;
import com.konst.scaleslibrary.module.wifi.WifiBaseManager;

import java.net.InetSocketAddress;

/**
 * @author Kostya on 26.12.2016.
 */
public class ModuleComPort extends Module {
    private static ModuleComPort instance;
    private final SerialPort serialPort;

    protected ModuleComPort(Context context, String version, InterfaceCallbackScales event) throws Exception{
        super(context,version,event);
        versionName = version;
        serialPort = new SerialPort(context);
    }

    public static void create(Context context, String moduleVersion, InterfaceCallbackScales event) throws Exception {
        instance = new ModuleComPort(context, moduleVersion, event);
    }

    @Override
    public void write(String command) {
        serialPort.sendTextPort(command);
    }

    @Override
    public ObjectCommand sendCommand(Commands commands) {
        return serialPort.sendCommand(commands);
    }

    @Override
    public void dettach() {
        super.dettach();
        isAttach = false;
        scalesProcessEnable(false);
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

    public static ModuleComPort getInstance() {return instance;}

}
