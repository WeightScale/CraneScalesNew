
package com.konst.scaleslibrary.module.scale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import com.konst.scaleslibrary.module.*;
import com.konst.scaleslibrary.module.bluetooth.BluetoothHandler;
import com.konst.scaleslibrary.module.bluetooth.BluetoothProcessManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Главный класс для работы с весовым модулем. Инициализируем в теле программы. В абстрактных методах используем
 * возвращеные результаты после запуска метода {@link ScaleModule#create(Context, String, BluetoothDevice, InterfaceCallbackScales)}}.
 * Пример:
 * com.kostya.module.ScaleModule scaleModule = new com.kostya.module.ScaleModule("version module");
 * scaleModule.init("bluetooth device");
 * @author Kostya
 */
public class ScaleModule extends Module /*implements Serializable*/{
    private static ScaleModule instance;
    /** Bluetooth устройство модуля весов. */
    protected BluetoothDevice device;
    protected BluetoothConnectReceiver bluetoothConnectReceiver;
    //private ThreadStableProcess threadStableProcess;
    private static final String TAG = ScaleModule.class.getName();
    private Thread threadAttach;
    /** Имя версии программы */
    private final String versionName;
    /** Скорость порта. */
    private int speedPort;
    /** Константы результата взвешивания. */
    public enum ResultWeight {
        /** Значение веса неправильное. */
        WEIGHT_ERROR,
        /** Значение веса в диапазоне весового модуля. */
        WEIGHT_NORMAL,
        /** Значение веса в диапазоне лилита взвешивания. */
        WEIGHT_LIMIT,
        /** Значение веса в диапазоне перегрузки. */
        WEIGHT_MARGIN
    }

    /** Конструктор класса весового модуля.
     * @param moduleVersion Имя и номер версии в формате [[Имя][Номер]].
     * @throws Exception Ошибка при создании модуля.
     */
    protected ScaleModule(Context context, String moduleVersion, BluetoothDevice device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context, event);
        versionName = moduleVersion;
        this.device = device;
        init(device);
        attach();
    }

    protected ScaleModule(Context context, String moduleVersion, String device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context, event);
        versionName = moduleVersion;
        try{
            BluetoothDevice tmp = bluetoothAdapter.getRemoteDevice(device);
            init(tmp);
        }catch (Exception e){
            throw new ErrorDeviceException(e.getMessage());
        }
        attach();
    }

    private ScaleModule(Context context, String moduleVersion, WifiManager wifiManager, InterfaceCallbackScales event)throws Exception, ErrorDeviceException {
        super(context, wifiManager, event);
        versionName = moduleVersion;
    }

    @Override
    public void write(String command) {
        bluetoothProcessManager.write(command);
        //threadScaleAttach.write(command);
    }

    @Override
    public ObjectCommand sendCommand(Commands commands) {
        return bluetoothProcessManager.sendCommand(commands);
        //return threadScaleAttach.sendCommand(commands);
    }

    /** Соединится с модулем. */
    @Override
    public void reconnect() /*throws InterruptedException*/ {
        if (threadAttach !=null){
            threadAttach.interrupt();
        }
        try {
            threadAttach = new Thread(new RunnableConnect());
            threadAttach.setPriority(Thread.MAX_PRIORITY);
            threadAttach.start();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void load() {
        try {
            version.extract();
            resultCallback.onCreate(instance);
        }  catch (ErrorTerminalException e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_TERMINAL_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        } catch (Exception e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_MODULE_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        }
    }

    /** Определяем после соединения это весовой модуль и какой версии.
     * Проверяем версию указаной при инициализации класса com.kostya.module.ScaleModule.
     * @return true - Версия правильная.
     */
    @Override
    public boolean isVersion() throws Exception {
        String vrs = getModuleVersion(); //Получаем версию весов
        if (vrs.startsWith(versionName)) {
            try {
                String s = vrs.replace(versionName, "");
                versionNum = Integer.valueOf(s);
                //setVersion(fetchVersion(numVersion));
                version = fetchVersion(versionNum);
            } catch (Exception e) {
                throw new Exception(e);
            }
            /* Если версия правильная создаем обьек и посылаем сообщения. */
            objectScales = new ObjectScales();
            return true;
        }
        throw new Exception("Это не весы или неправильная версия!!!");
    }

    @Override
    protected void connect() {

    }

    /** Отсоединение от весового модуля.
     * Необходимо использовать перед закрытием программы чтобы остановить работающие процессы
     */
    @Override
    public void dettach() {
        isAttach = false;
        scalesProcessEnable(false);
        //stableActionEnable(false);
        bluetoothConnectReceiver.unregister();
        if (bluetoothProcessManager != null){
            bluetoothProcessManager.stopProcess();
        }
    }

    public static void create(Context context, String moduleVersion, BluetoothDevice device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        instance = new ScaleModule(context, moduleVersion, device, event);
    }

    public static void create(Context context, String moduleVersion, String bluetoothDevice, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        instance = new ScaleModule(context, moduleVersion, bluetoothDevice, event);
    }

    public static void createWiFi(Context context, String moduleVersion, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        instance = new ScaleModule(context, moduleVersion, (WifiManager)context.getSystemService(Context.WIFI_SERVICE), event);
    }

    /** Инициализация bluetooth адаптера и модуля.
     * Перед инициализациеи надо создать класс com.kostya.module.ScaleModule
     * Для соединения {@link ScaleModule#attach()}
     * @param device bluetooth устройство.
     * @throws ErrorDeviceException Ошибка удаленного устройства.
     */
    private void init( BluetoothDevice device) throws ErrorDeviceException{
        if(device == null)
            throw new ErrorDeviceException("Bluetooth device is null ");
        this.device = device;
        bluetoothConnectReceiver = new BluetoothConnectReceiver(getContext());
        bluetoothConnectReceiver.register();
    }

    public static ScaleModule getInstance() { return instance; }

    /** Соединится с модулем. */
    public void attach() /*throws InterruptedException*/ {
        if (threadAttach !=null){
            threadAttach.interrupt();
        }
        try {
            threadAttach = new Thread(new RunnableAttach());
            threadAttach.setPriority(Thread.MAX_PRIORITY);
            threadAttach.start();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /** Получить bluetooth устройство модуля.
     * @return bluetooth устройство.
     */
    protected BluetoothDevice getDevice() {
        return device;
    }

    public String getAddressBluetoothDevice() {return device.getAddress();}

    /** Возвращяем имя bluetooth утройства.
     * @return Имя bluetooth.
     */
    public String getNameBluetoothDevice() {
        String name = null;
        try{
            name = device.getName();
        }catch (NullPointerException e){
            name = device.getAddress();
        }finally {
            if (name == null)
                name = device.getAddress();
            return name;
        }
    }

    public int getSpeedPort() {
        return speedPort;
    }

    public void setSpeedPort(int speedPort) {
        this.speedPort = speedPort;
    }

    //==================================================================================================================

    /** Получаем значение скорости порта bluetooth модуля обмена данными.
     * Значение от 1 до 5.
     * 1 - 9600bps.
     * 2 - 19200bps.
     * 3 - 38400bps.
     * 4 - 57600bps.
     * 5 - 115200bps.
     *
     * @return Значение от 1 до 5.
     * @see Commands#BST
     */
    public String getModuleSpeedPort() {
        return Commands.BST.getParam();
    }

    /** Устанавливаем скорость порта обмена данными bluetooth модуля.
     * Значение от 1 до 5.
     * 1 - 9600bps.
     * 2 - 19200bps.
     * 3 - 38400bps.
     * 4 - 57600bps.
     * 5 - 115200bps.
     *
     * @param speed Значение скорости.
     * @return true - Значение записано.
     * @see Commands#BST
     */
    public boolean setModuleSpeedPort(int speed) {
        return Commands.BST.setParam(speed);
    }

    /** Получить офсет датчика веса.
     * @return Значение офсет.
     * @see Commands#GCO
     */
    public String getModuleOffsetSensor() {
        return Commands.GCO.getParam();
    }

    /** Устанавливаем имя весового модуля.
     * @param name Имя весового модуля.
     * @return true - Имя записано в модуль.
     * @see Commands#SNA
     */
    public boolean setModuleName(String name) { return Commands.SNA.setParam(name);}

    /** Устанавливаем калибровку батареи.
     * @param percent Значение калибровки в процентах.
     * @return true - Калибровка прошла успешно.
     * @see Commands#CBT
     */
    public boolean setModuleCalibrateBattery(int percent) {
        return Commands.CBT.setParam(percent);
    }

    public boolean writeData() {
        return version.writeData();
    }

    /* Включаем выключаем процесс определения стабильного веса.
      @param stable true - процесс запускается, false - процесс останавливается.
     */
    /*public void stableActionEnable(boolean stable){
        try {
            if (stable){
                if (threadStableProcess != null)
                    threadStableProcess.interrupt();
                threadStableProcess = new ThreadStableProcess();
                threadStableProcess.start();
            }else {
                if (threadStableProcess != null)
                    threadStableProcess.interrupt();
            }
        }catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }*/

    /*
      Процесс обработки стабильного показания веса.
      Если вес стабильный определенное время посылаем сообщение.
     */
    /*private class ThreadStableProcess extends Thread{
        *//** Временная переменная для хранения веса. *//*
        private int tempWeight;
        private boolean cancel;

        @Override
        public void run() {
            while (!interrupted() && !cancel){
                if (tempWeight - getStepScale() <= objectScales.getWeight() && tempWeight + getStepScale() >= objectScales.getWeight()) {
                    if (objectScales.getStableNum() <= STABLE_NUM_MAX){
                        if (objectScales.getStableNum() == STABLE_NUM_MAX) {
                            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_WEIGHT_STABLE).putExtra(InterfaceModule.EXTRA_SCALES, objectScales));
                        }
                        objectScales.setStableNum(objectScales.getStableNum()+1);
                    }
                } else {
                    objectScales.setStableNum(0);
                }
                tempWeight = objectScales.getWeight();
                getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_SCALES_RESULT).putExtra(InterfaceModule.EXTRA_SCALES, objectScales));
                try { TimeUnit.MILLISECONDS.sleep(20); } catch (InterruptedException e) {}
            }
        }

        @Override
        public void interrupt() {
            super.interrupt();
            cancel = true;
        }
    }*/

    protected class RunnableAttach implements Runnable {
        private final BluetoothSocket mmSocket;
        private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        //ObjectCommand response;

        public RunnableAttach() throws IOException {
            BluetoothSocket tmp;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
            else
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            mmSocket = tmp;
        }

        @Override
        public void run() {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_START).putExtra(InterfaceModule.EXTRA_DEVICE_NAME,getNameBluetoothDevice()));
            //try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            //try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
            try {
                mmSocket.connect();
                bluetoothProcessManager = new BluetoothProcessManager(getContext(), mmSocket);
            } catch (IOException connectException) {
                try {mmSocket.close();} catch (IOException closeException) { }
                try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
                getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR).putExtra(InterfaceModule.EXTRA_MESSAGE, connectException.getMessage()));
            }finally {
                getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_FINISH));
            }
            Log.i(TAG, "thread done");
        }

        public void cancel() {
            try {mmSocket.close();} catch (IOException e) { }
            Thread.currentThread().interrupt();
        }
    }

    public class RunnableConnect implements Runnable {
        private final BluetoothSocket mmSocket;
        private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        //ObjectCommand response;

        public RunnableConnect() throws IOException {
            BluetoothSocket tmp;
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
            //try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
            try {
                mmSocket.connect();
                bluetoothProcessManager = new BluetoothProcessManager(getContext(), mmSocket);
            } catch (IOException connectException) {
                try {mmSocket.close();} catch (IOException closeException) { }
                try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
                getContext().sendBroadcast(new Intent(BluetoothHandler.MSG.DISCONNECT.name()));
            }finally {
                getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_FINISH));
            }

            Log.i(TAG, "thread done");
        }

        public void cancel() {
            try {mmSocket.close();} catch (IOException e) { }
            Thread.currentThread().interrupt();
        }
    }

    public class BluetoothConnectReceiver extends BroadcastReceiver {
        final Context mContext;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

        public BluetoothConnectReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            //intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action){
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        try{bluetoothProcessManager.closeSocket();}catch (Exception e){}
                        break;
                    /*case BluetoothDevice.ACTION_ACL_CONNECTED:

                    break;*/
                    default:
                }
            }
        }

        public void register() {
            if (!isRegistered){
                isRegistered = true;
                mContext.registerReceiver(this, intentFilter);
            }
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    void managerConnect(Socket socket){
        if (bluetoothProcessManager != null)
            bluetoothProcessManager.connect();
    }

}
