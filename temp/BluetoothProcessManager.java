package com.konst.scaleslibrary.module.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.InterfaceTransferClient;
import com.konst.scaleslibrary.module.ObjectCommand;

/**
 * @author Kostya on 21.07.2016.
 */
public class BluetoothProcessManager {
    private final BluetoothSocket mmSocket;
    private BluetoothClientConnect bluetoothClientConnect;
    private Context mContext;
    final InterfaceTransferClient interfaceBluetoothClient;

    public BluetoothProcessManager(Context context, BluetoothSocket socket){
        mContext = context;
        mmSocket = socket;
        bluetoothClientConnect = new BluetoothClientConnect(context, mmSocket);
        interfaceBluetoothClient = bluetoothClientConnect;
        bluetoothClientConnect.start();
    }

   /* public BluetoothProcessManager(BluetoothSocket socket, InterfaceBluetoothClient interfaceClient){
        this(socket);
        interfaceBluetoothClient = interfaceClient;
    }*/

    public void connect(){
        if(!bluetoothClientConnect.isAlive()){
            bluetoothClientConnect = new BluetoothClientConnect(mContext, mmSocket);
            bluetoothClientConnect.start();
        }
    }

    public void write(String command) {
        interfaceBluetoothClient.write(command);
    }

    public ObjectCommand sendCommand(Commands commands) {
        return interfaceBluetoothClient.sendCommand(commands);
    }

    public void stopProcess(){
        bluetoothClientConnect.terminate();
        //bluetoothClientConnect.cancel();
    }

    public void closeSocket(){
        bluetoothClientConnect.cancel();
    }

    public boolean sendByte(byte ch) {
        return interfaceBluetoothClient.writeByte(ch);
    }

    public int getByte() {
        return interfaceBluetoothClient.getByte();
    }
}
