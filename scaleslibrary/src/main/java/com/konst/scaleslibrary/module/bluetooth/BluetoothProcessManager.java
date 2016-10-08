package com.konst.scaleslibrary.module.bluetooth;

import android.bluetooth.BluetoothSocket;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.ObjectCommand;

/**
 * @author Kostya on 21.07.2016.
 */
public class BluetoothProcessManager {
    private final BluetoothSocket mmSocket;
    private BluetoothClientConnect bluetoothClientConnect;
    final BluetoothHandler handler;
    final InterfaceBluetoothClient interfaceBluetoothClient;

    public BluetoothProcessManager(BluetoothSocket socket, BluetoothHandler handler){
        mmSocket = socket;
        this.handler = handler;
        bluetoothClientConnect = new BluetoothClientConnect(mmSocket, handler);
        interfaceBluetoothClient = bluetoothClientConnect;
        bluetoothClientConnect.start();
    }

   /* public BluetoothProcessManager(BluetoothSocket socket, InterfaceBluetoothClient interfaceClient){
        this(socket);
        interfaceBluetoothClient = interfaceClient;
    }*/

    public void connect(){
        if(!bluetoothClientConnect.isAlive()){
            bluetoothClientConnect = new BluetoothClientConnect(mmSocket, handler);
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
