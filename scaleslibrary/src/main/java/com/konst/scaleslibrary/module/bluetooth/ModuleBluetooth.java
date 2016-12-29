package com.konst.scaleslibrary.module.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.ErrorDeviceException;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.ObjectCommand;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;

/**
 * @author Kostya on 26.12.2016.
 */
public class ModuleBluetooth extends Module {
    protected ModuleBluetooth(Context context, BluetoothDevice device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context, event);
    }

    @Override
    public void write(String command) {

    }

    @Override
    public ObjectCommand sendCommand(Commands commands) {
        return null;
    }

    @Override
    protected void dettach() {

    }

    protected void attach() {

    }

    @Override
    protected boolean isVersion() throws Exception {
        return false;
    }

    @Override
    protected void reconnect() {

    }

    @Override
    protected void load() {

    }

    @Override
    protected void connect() {

    }
}
