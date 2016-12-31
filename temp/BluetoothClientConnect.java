package com.konst.scaleslibrary.module.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.InterfaceTransferClient;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.ObjectCommand;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Kostya on 26.07.2016.
 */
class BluetoothClientConnect extends Thread implements InterfaceTransferClient {
    private Context mContext;
    private final BluetoothSocket mmSocket;
    protected final BufferedReader bufferedReader;
    protected final PrintWriter printWriter;
    private ObjectCommand response;
    private boolean isTerminate;
    private static final String TAG = BluetoothClientConnect.class.getName();

    public BluetoothClientConnect(Context context, BluetoothSocket socket) {
        mContext = context;
        mmSocket = socket;
        BufferedReader tmpIn = null;
        PrintWriter tmpOut = null;

        try {
            tmpIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            tmpOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
        } catch (IOException e) {Log.e(TAG, e.getMessage());}

        bufferedReader = tmpIn;
        printWriter = tmpOut;
    }

    @Override
    public void run() {
        mContext.sendBroadcast(new Intent(Module.CONNECT));
        //bluetoothHandler.obtainMessage(BluetoothHandler.MSG.CONNECT.ordinal()).sendToTarget();
        try {
            while (!isInterrupted()) {
                String substring = bufferedReader.readLine();
                try {
                    Commands cmd = Commands.valueOf(substring.substring(0, 3));
                    if (cmd == response.getCommand()){
                        response.setValue(substring.replace(cmd.name(),""));
                        response.setResponse(true);
                    }else {
                        //objectCommand = new ObjectCommand(cmd, substring.replace(cmd.name(),""));
                    }
                    //bluetoothHandler.obtainMessage(BluetoothHandler.MSG.RECEIVE.ordinal(), new ObjectCommand(command, substring.replace(command.name(),""))).sendToTarget();
                } catch (Exception e) {
                    Log.i(TAG, e.getMessage());
                }
            }
        }catch (IOException e){
            if(!isTerminate)
                mContext.sendBroadcast(new Intent(Module.DISCONNECT));
                //bluetoothHandler.obtainMessage(BluetoothHandler.MSG.DISCONNECT.ordinal()).sendToTarget();
            /*else
                bluetoothHandler.obtainMessage(BluetoothHandler.MSG.ERROR.ordinal()).sendToTarget();*/
        }finally {
            cancel();
        }
        Log.i(TAG, "done thread");
    }

    @Override
    public void write(String data) {
        printWriter.write(data);
        printWriter.write('\r');
        printWriter.write('\n');
        printWriter.flush();
        //printWriter.println(data);
    }

    @Override
    public synchronized ObjectCommand sendCommand(Commands cmd) {
        write(cmd.toString());
        response = new ObjectCommand(cmd, "");
        for (int i = 0; i < cmd.getTimeOut(); i++) {
            try {TimeUnit.MILLISECONDS.sleep(1);} catch (InterruptedException e) {Log.e(TAG, e.getMessage());}
            try {
                if (response.isResponse()) {
                    return response;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean writeByte(byte ch) {
        printWriter.print(ch);
        printWriter.flush();
        return false;
    }

    @Override
    public int getByte() {
        return 0;
    }

    public void cancel() {
        try {mmSocket.close();} catch (IOException e) {Log.e(TAG, e.getMessage()); }
        try {bufferedReader.close();} catch (IOException e) {Log.e(TAG, e.getMessage());}
        //interrupt();
    }

    public void terminate(){
        isTerminate = true;
        interrupt();
        cancel();
    }

}
