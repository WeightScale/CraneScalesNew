package com.konst.scaleslibrary.module.boot;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.konst.scaleslibrary.module.*;
import com.konst.scaleslibrary.module.bluetooth.ModuleBluetooth;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;

import java.io.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Класс для самопрограммирования весового модуля.
 * @author Kostya
 */
public class BootModule extends ModuleBluetooth {
    private static BootModule instance;
    private ThreadBootAttach threadAttach;
    //private String versionName = "";
    private int versionNumber;
    private static final String TAG = BootModule.class.getSimpleName();

    /** Конструктор модуля бутлодера.
     * @param version Верситя бутлодера.
     */
    private BootModule(Context context, String version, String address, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context, version, address, event);
        //runnableBootConnect = new RunnableBootConnect();
        attach();
    }

    /** Конструктор модуля бутлодера.
     * @param version Верситя бутлодера.
     */
    /*private BootModule(Context context, String version, BluetoothDevice device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context, version, device, event);
        //runnableBootConnect = new RunnableBootConnect();
        attach();
    }*/

    public static void create(Context context, String version, String address, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        instance = new BootModule(context, version, address, event);
    }

    /*public static void create(Context context, String version, BluetoothDevice device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        instance = new BootModule(context, version, device, event);
    }*/

    public static BootModule getInstance() { return instance; }

    @Override
    public void attach(){
        /*getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_START).putExtra(InterfaceModule.EXTRA_DEVICE_NAME, getNameBluetoothDevice()));
        //resultCallback.resultConnect(ResultConnect.STATUS_ATTACH_START, getNameBluetoothDevice(), null);
        if (threadBootAttach !=null){
            threadBootAttach.interrupt();
        }
        threadBootAttach = new ThreadBootAttach();
        threadBootAttach.start();*/

        if (threadAttach !=null){
            threadAttach.interrupt();
        }
        try {
            threadAttach = new ThreadBootAttach();
            threadAttach.start();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Определяем имя после соединения это бутлоадер модуль.
     * Указывается имя при инициализации класса com.kostya.module.BootModule.
     *
     * @return true Имя совпадает.
     */
    @Override
    public boolean isVersion() {
        String vrs = getModuleVersion(); //Получаем версию модуля.
        if(vrs.startsWith(versionName)) {
            try {
                versionNumber = Integer.valueOf(vrs.replace(versionName, ""));
            } catch (Exception var3) {
                versionNumber = 0;
            }
            return true;
        }
        return false;
    }

    @Override
    protected void connect() {

    }

    @Override
    public void reconnect() {

    }

    @Override
    public void load() {
        resultCallback.onCreate(instance);
    }

    /**
     * Разьеденится с загрузчиком.
     * Вызывать этот метод при закрытии программы.
     */
    @Override
    public void dettach(){
        //removeCallbacksAndMessages(null);todo проверка без handel
        //disconnect();
        //threadBootAttach.cancel();
        isAttach = false;
        //stopMeasuringWeight();
        //stopMeasuringBatteryTemperature();
        //disconnect();
        bluetoothConnectReceiver.unregister();
        if (threadAttach != null){
            threadAttach.cancel();
        }
    }

    @Override
    public void write(String command) {

    }

    @Override
    public ObjectCommand sendCommand(Commands commands) {
        return threadAttach.sendCommand(commands);
    }

    /** Обработчик для процесса соединения*/
    private class ThreadBootAttach extends Thread {
        private final BluetoothSocket mmSocket;
        protected OutputStream outputStream;
        protected InputStream inputStream;
        protected BufferedReader bufferedReader;
        //protected PrintWriter printWriter;
        private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        private ThreadBootAttach() throws IOException {
            BluetoothSocket tmp;
            //mmDevice = device;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
            else
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            mmSocket = tmp;
        }

        @Override
        public void run() {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_START).putExtra(InterfaceModule.EXTRA_DEVICE_NAME,getNameBluetoothDevice()));
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            try {
                mmSocket.connect();
                outputStream = mmSocket.getOutputStream();
                inputStream = mmSocket.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                if(isVersion()){
                    isAttach = true;
                    resultCallback.onCreate(instance);
                    getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_LOAD_OK));
                }else {
                    throw new Exception("Ошибка проверки версии.");
                }

            } catch (Exception e) {
                dettach();
                getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR).putExtra(InterfaceModule.EXTRA_MESSAGE, e.getMessage()));
                cancel();
            }finally {
                getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_FINISH));
            }
            Log.i(TAG, "thread done");
        }

        public void cancel() {
            try {mmSocket.close();} catch (IOException e) { }
        }

        public void writeStr(String data) throws IOException {
            outputStream.write(data.getBytes());
            outputStream.write('\r');
            outputStream.write('\n');
            outputStream.flush();
            //printWriter.println(data);
        }

        public String readStr() throws IOException {
            return bufferedReader.readLine();
        }

        public synchronized void sendByte(byte ch) {
            try {
                outputStream.write(ch);
                //outputStream.flush();
            } catch (Exception ioe) {
            }
        }

        public synchronized int getByte() {

            try {
                return inputStream.read();
            } catch (Exception ioe) {}
            return 0;
        }

        public ObjectCommand sendCommand(Commands commands) {
            ObjectCommand response = new ObjectCommand(commands, "");
            try {
                writeStr(commands.toString());
                try {TimeUnit.MILLISECONDS.sleep(100);} catch (InterruptedException e) {}
                String substring = readStr();
                Commands cmd = Commands.valueOf(substring.substring(0, 3));
                if (cmd == response.getCommand()){
                    response.setValue(substring.replace(cmd.name(),""));
                    return response;
                }
            } catch (IOException e) {
                Log.i(TAG, e.getMessage());
            }
            return null;
        }
    }

    /**
     * Комманда старт программирования.
     * Версия 2 и выше.
     * @return true - Запущено программирование.
     */
    public boolean startProgramming() {
        return Commands.STR.setParam("");
    }

    /**
     * Получить код микроконтролера.
     * Версия 2 и выше.
     * @return Код в текстовом виде.
     */
    public String getPartCode() {
        return Commands.PRC.getParam();
    }

    /**
     * Получить версию загрузчика.
     * @return Номер версии.
     */
    public int getBootVersion() {
        String vrs = getModuleVersion();
        if (vrs.startsWith(versionName)) {
            try {
                return Integer.valueOf(vrs.replace(versionName, ""));
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void sendByte(byte ch) {
        threadAttach.sendByte(ch);
    }

    public int getByte() {
        return threadAttach.getByte();
    }

}
