package com.konst.scaleslibrary.module.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Kostya  on 21.07.2016.
 */
class BluetoothClientThread extends Thread {
    private Context mContext;
    //public final BluetoothHandler handler;
    private BluetoothClientConnect bluetoothClientConnect;
    private final BluetoothSocket mmSocket;
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = BluetoothClientThread.class.getName();

    public BluetoothClientThread(Context context, BluetoothDevice device) {
        mContext = context;
        BluetoothSocket tmp = null;
        //mmDevice = device;
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
            else
                tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    @Override
    public void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {mmSocket.close();} catch (IOException closeException) { }
            return;
        }

        bluetoothClientConnect = new BluetoothClientConnect(mContext, mmSocket);
        bluetoothClientConnect.start();
    }



    public void cancel() {
        if (bluetoothClientConnect != null){
            bluetoothClientConnect.interrupt();
            bluetoothClientConnect.cancel();
        }
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }


}
