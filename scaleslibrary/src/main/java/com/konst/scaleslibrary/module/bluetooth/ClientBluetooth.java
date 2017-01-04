package com.konst.scaleslibrary.module.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.konst.scaleslibrary.module.*;
import com.konst.scaleslibrary.module.wifi.ClientWiFi;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Kostya on 30.12.2016.
 */
public class ClientBluetooth implements InterfaceTransferClient {
    private Context mContext;
    private WorkerThread workerThread;
    private BluetoothDevice device;
    private BluetoothSocket mmSocket;
    protected BufferedReader bufferedReader;
    protected PrintWriter printWriter;
    private ObjectCommand response;
    private AtomicBoolean working;
    private boolean isTerminate;
    private static final int TIME_OUT_CONNECT = 2000; /** Время в милисекундах. */
    private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = ClientWiFi.class.getName();

    public ClientBluetooth(Context context, BluetoothDevice device) throws IOException {
        mContext = context;
        this.device = device;
        BluetoothSocket tmp;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
            tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
        else
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        mmSocket = tmp;
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
            try { TimeUnit.MILLISECONDS.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}
            try {
                if (response.isResponse()) {
                    return response;
                }
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage());
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
        }
    }

    public void closeSocket() throws IOException {
        if (mmSocket != null)
            mmSocket.close();
    }

    private class WorkerThread extends Thread {
        private AtomicBoolean working;

        WorkerThread(){
            working = new AtomicBoolean(true);

        }

        @Override
        public void run() {
            mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_START).putExtra(InterfaceModule.EXTRA_DEVICE_NAME,device.getName()));
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                mmSocket.connect();
                bufferedReader = new BufferedReader(new InputStreamReader(mmSocket.getInputStream(), "UTF-8"));
                printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mmSocket.getOutputStream(), "UTF-8")), true);
                printWriter.flush();
                mContext.sendBroadcast(new Intent(Module.CONNECT));
            } catch (IOException e) {
                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_FINISH));
                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR).putExtra(InterfaceModule.EXTRA_MESSAGE, e.getMessage()));
                Log.e(TAG, e.getMessage());
                return;
            }

            try {
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
                //mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR).putExtra(InterfaceModule.EXTRA_MESSAGE, e.getMessage()));
                if(!isTerminate)
                    mContext.sendBroadcast(new Intent(Module.DISCONNECT));
                Log.e(TAG, e.getMessage());
            }finally {
                stopWorkingThread();
            }
        }

        public void stopWorkingThread() {
            isTerminate = true;
            working.set(false);
            try {mmSocket.close();} catch (Exception e){}
            try {bufferedReader.close();} catch (Exception e){}
            try{printWriter.close();}catch (Exception e){}
            Thread.currentThread().interrupt();
        }
    }
}
