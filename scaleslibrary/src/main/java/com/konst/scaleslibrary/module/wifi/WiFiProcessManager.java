package com.konst.scaleslibrary.module.wifi;

import android.bluetooth.BluetoothSocket;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.ObjectCommand;
import com.konst.scaleslibrary.module.bluetooth.BluetoothHandler;
import com.konst.scaleslibrary.module.bluetooth.InterfaceBluetoothClient;

import java.net.Socket;

/**
 * @author Kostya on 21.07.2016.
 */
public class WiFiProcessManager {
    private final Socket mmSocket;
    private WiFiClientConnect wiFiClientConnect;
    final BluetoothHandler handler;
    final InterfaceBluetoothClient interfaceBluetoothClient;

    public WiFiProcessManager(Socket socket, BluetoothHandler handler){
        mmSocket = socket;
        this.handler = handler;
        wiFiClientConnect = new WiFiClientConnect(mmSocket, handler);
        interfaceBluetoothClient = wiFiClientConnect;
        wiFiClientConnect.start();
    }

   /* public BluetoothProcessManager(BluetoothSocket socket, InterfaceBluetoothClient interfaceClient){
        this(socket);
        interfaceBluetoothClient = interfaceClient;
    }*/

    public void connect(){
        if(!wiFiClientConnect.isAlive()){
            wiFiClientConnect = new WiFiClientConnect(mmSocket, handler);
            wiFiClientConnect.start();
        }
    }

    public void write(String command) {
        interfaceBluetoothClient.write(command);
    }

    public ObjectCommand sendCommand(Commands commands) {
        return interfaceBluetoothClient.sendCommand(commands);
    }

    public void stopProcess(){
        wiFiClientConnect.terminate();
        //bluetoothClientConnect.cancel();
    }

    public void closeSocket(){
        wiFiClientConnect.cancel();
    }

    public boolean sendByte(byte ch) {
        return interfaceBluetoothClient.writeByte(ch);
    }

    public int getByte() {
        return interfaceBluetoothClient.getByte();
    }
}
