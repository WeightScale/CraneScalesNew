package com.konst.scaleslibrary.module.wifi;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbRequest;
import android.util.Log;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.ObjectCommand;
import com.konst.scaleslibrary.module.bluetooth.BluetoothHandler;
import com.konst.scaleslibrary.module.bluetooth.InterfaceBluetoothClient;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Kostya 20.12.2016.
 */
public class ClientWiFi implements InterfaceBluetoothClient {
    private Context mContext;
    private Socket mSocket;
    private WorkerThread workerThread;
    protected BufferedReader bufferedReader;
    protected PrintWriter printWriter;
    private ObjectCommand response;
    private AtomicBoolean working;
    private InetSocketAddress inetSocketAddress;
    private String address;
    private int port;
    private static final int TIME_OUT_CONNECT = 2000; /** Время в милисекундах. */
    private static final String TAG = ClientWiFi.class.getName();

    public ClientWiFi(Context context, String address, int port){
        mContext = context;
        this.address = address;
        this.port = port;
    }

    public ClientWiFi(Context context, InetSocketAddress address){
        mContext = context;
        inetSocketAddress = address;
        port = inetSocketAddress.getPort();
    }

    public void killWorkingThread() {
        if(workerThread != null) {
            workerThread.stopWorkingThread();
            workerThread = null;
        }
    }

    public void restartWorkingThread() {
        if(workerThread == null) {
            workerThread = new WorkerThread();
            workerThread.start();

            /*while(true) {
                if(!workerThread.isAlive()) {
                    continue;
                }
            }*/
        }
    }

    @Override
    public void write(String data) {
        printWriter.write(data);
        printWriter.write('\r');
        printWriter.write('\n');
        printWriter.flush();
    }

    @Override
    public synchronized ObjectCommand sendCommand(Commands cmd) {
        write(cmd.toString());
        response = new ObjectCommand(cmd, "");
        for (int i = 0; i < cmd.getTimeOut(); i++) {
            try { TimeUnit.MILLISECONDS.sleep(1);} catch (InterruptedException e) {Log.e(TAG, e.getMessage());}
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
        return true;
    }

    @Override
    public int getByte() {
        return 0;
    }

    private class WorkerThread extends Thread {
        private AtomicBoolean working;

        WorkerThread(){
            working = new AtomicBoolean(true);
            mSocket = new Socket();

        }

        @Override
        public void run() {
            try {
                mSocket.connect(inetSocketAddress, TIME_OUT_CONNECT);

                bufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
                printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8")), true);
                printWriter.flush();
                mContext.sendBroadcast(new Intent(BluetoothHandler.MSG.CONNECT.name()));
                while(working.get()) {
                    String substring = bufferedReader.readLine();
                    try {
                        Commands cmd = Commands.valueOf(substring.substring(0, 3));
                        if (cmd == response.getCommand()){
                            response.setValue(substring.replace(cmd.name(),""));
                            response.setResponse(true);
                        }
                    } catch (Exception e) {
                        Log.i(TAG, e.getMessage());
                    }
                }
            }catch (IOException e){
                Log.e(TAG, e.getMessage());
            }finally {
                stopWorkingThread();
            }

        }

        public void stopWorkingThread() {
            working.set(false);
            try {mSocket.close();} catch (IOException e) { }
            try {bufferedReader.close();} catch (IOException e) { }
            printWriter.close();
            Thread.currentThread().interrupt();
        }
    }

}
