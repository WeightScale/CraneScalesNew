/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.konst.scaleslibrary.module.boot;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.ErrorDeviceException;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.ObjectCommand;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;

import java.io.IOException;

/**
 * Класс для самопрограммирования весового модуля.
 * @author Kostya
 */
public class BootModule extends Module {
    private static BootModule instance;
    private Thread threadAttach;
    private String versionName = "";

    /** Конструктор модуля бутлодера.
     * @param version Верситя бутлодера.
     */
    private BootModule(Context context, String version, String address, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context, address, event);
        //runnableBootConnect = new RunnableBootConnect();
        versionName = version;
        attach();
    }

    /** Конструктор модуля бутлодера.
     * @param version Верситя бутлодера.
     */
    private BootModule(Context context, String version, BluetoothDevice device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context, device, event);
        //runnableBootConnect = new RunnableBootConnect();
        versionName = version;
        attach();
    }

    public static void create(Context context, String version, String address, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        instance = new BootModule(context, version, address, event);
    }

    public static void create(Context context, String version, BluetoothDevice device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        instance = new BootModule(context, version, device, event);
    }

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
            threadAttach = new Thread(new RunnableAttach());
            threadAttach.start();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void attachWiFi() throws InterruptedException {

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
        return vrs.startsWith(versionName);
    }

    @Override
    protected void reconnect() {

    }

    @Override
    protected void load() {

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
        if (bluetoothProcessManager != null){
            bluetoothProcessManager.stopProcess();
        }
    }

    /**
     * Получаем соединение с bluetooth весовым модулем.
     * @throws IOException Ошибка соединения.
     */
    //@Override
    /*public synchronized void connect() throws IOException, NullPointerException {
        disconnect();
        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
            socket = device.createInsecureRfcommSocketToServiceRecord(getUuid());
        else
            socket = device.createRfcommSocketToServiceRecord(getUuid());
        bluetoothAdapter.cancelDiscovery();
        socket.connect();
        inputStream = socket.getInputStream();
        //inputStreamReader = new InputStreamReader(inputStream);
        //bufferedReader = new BufferedReader(inputStreamReader);
        outputStream = socket.getOutputStream();
        //bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
    }*/
    /*public synchronized void connect() throws IOException, NullPointerException {
        disconnect();
        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
            socket = device.createInsecureRfcommSocketToServiceRecord(getUuid());
        else
            socket = device.createRfcommSocketToServiceRecord(getUuid());
        bluetoothAdapter.cancelDiscovery();
        socket.connect();
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        //bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
    }*/

    @Override
    protected void connectWiFi() throws IOException, NullPointerException {

    }

    @Override
    public void write(String command) {

    }

    @Override
    public ObjectCommand sendCommand(Commands commands) {
        return bluetoothProcessManager.sendCommand(commands);
    }

    /**
     * Получаем разьединение с bluetooth весовым модулем
     */
    /*@Override
    public void disconnect() {
        try {
            *//*if(inputStreamReader != null)
                inputStreamReader.close();*//*
            if(inputStream != null)
                inputStream.close();
            *//*if (bufferedWriter != null)
                bufferedWriter.close();*//*
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException ioe) {
            socket = null;
        }
        inputStream = null;
        //inputStreamReader =  null;
        outputStream = null;
        //bufferedWriter = null;
        socket = null;
    }*/

    /** Обработчик для процесса соединения
     */
    /*private class ThreadBootAttach extends Thread {
        private final BluetoothSocket mmSocket;
        protected BufferedReader bufferedReader;
        protected PrintWriter printWriter;
        private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        private ThreadBootAttach() {
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
                //connect();
                mmSocket.connect();
                bufferedReader = new BufferedReader(new InputStreamReader(mmSocket.getInputStream(), "UTF-8"));
                printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mmSocket.getOutputStream(), "UTF-8")), true);

                bluetoothHandler.obtainMessage(BluetoothHandler.MSG.CONNECT.ordinal()).sendToTarget();

                *//*if(isVersion()){
                    connectResultCallback.resultConnect(ResultConnect.STATUS_LOAD_OK, "", instance);

                }else {
                    disconnect();
                    connectResultCallback.resultConnect(ResultConnect.STATUS_VERSION_UNKNOWN, "", null);
                }*//*

            } catch (IOException e) {
                //connectResultCallback.connectError(ResultError.CONNECT_ERROR, e.getMessage());

                resultCallback.resultConnect(ResultConnect.CONNECT_ERROR, e.getMessage(), null);
                //disconnect();
                cancel();
            }
            //connectResultCallback.resultConnect(ResultConnect.STATUS_ATTACH_FINISH, "");
            resultCallback.resultConnect(ResultConnect.STATUS_ATTACH_FINISH, "", null);
        }

        public void cancel() {
            try {mmSocket.close();} catch (IOException e) { }
        }

        public synchronized boolean sendByte(byte ch) {
            try {
                printWriter.write(ch);
                printWriter.flush();
                return true;
            } catch (Exception ioe) {}
            return false;
        }

        public synchronized int getByte() {

            try {
                int b = bufferedReader.read();
                return b;
            } catch (Exception ioe) {}
            return 0;
        }
    }*/

    /**
     * Комманда старт программирования.
     * Версия 2 и выше.
     * @return true - Запущено программирование.
     */
    public boolean startProgramming() {
        return Commands.STR.getParam().equals(Commands.STR.getName());

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
     *
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

    /*private BluetoothHandler bluetoothHandler = new BluetoothHandler(){

        @Override
        public void handleMessage(Message msg) {
            switch (MSG.values()[msg.what]){
                case RECEIVE:
                    ObjectCommand cmd = (ObjectCommand)msg.obj;
                    String value = cmd.getValue();
                    break;
                case CONNECT:
                    if(isVersion()){
                        resultCallback.resultConnect(ResultConnect.STATUS_LOAD_OK, "", instance);

                    }else {
                        threadBootAttach.cancel();
                        resultCallback.resultConnect(ResultConnect.STATUS_VERSION_UNKNOWN, "", null);
                    }
                    break;
                default:
            }
        }
    };*/

    public synchronized boolean sendByte(byte ch) {
        return bluetoothProcessManager.sendByte(ch);
    }

    public synchronized int getByte() {
        return bluetoothProcessManager.getByte();
    }

}
