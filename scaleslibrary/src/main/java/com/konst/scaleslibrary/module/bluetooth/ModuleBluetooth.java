package com.konst.scaleslibrary.module.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import com.konst.scaleslibrary.module.*;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.scale.ObjectScales;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Главный класс для работы с весовым модулем. Инициализируем в теле программы. В абстрактных методах используем
 * возвращеные результаты после запуска метода {@link ModuleBluetooth#create(Context, String, String, InterfaceCallbackScales)}}.
 * Пример:
 * com.kostya.module.ScaleModule scaleModule = new com.kostya.module.ScaleModule("version module");
 * scaleModule.init("bluetooth device");
 * @author Kostya
 */
public class ModuleBluetooth extends Module {
    private static ModuleBluetooth instance;
    /** Bluetooth устройство модуля весов. */
    protected BluetoothDevice device;
    protected BluetoothConnectReceiver bluetoothConnectReceiver;
    //private ThreadStableProcess threadStableProcess;
    private static final String TAG = ModuleBluetooth.class.getName();
    private ClientBluetooth clientBluetooth;
    //private Thread threadAttach;

    /** Конструктор класса весового модуля.
     * @param moduleVersion Имя и номер версии в формате [[Имя][Номер]].
     * @throws Exception Ошибка при создании модуля.
     */
    protected ModuleBluetooth(Context context, String moduleVersion, String device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context,moduleVersion,event);
        /* Проверяем и включаем bluetooth. */
        try {onEnableBluetooth(BluetoothAdapter.getDefaultAdapter());} catch (Exception e) {throw new ErrorDeviceException(e.getMessage());}
        try{
            init(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device));
        }catch (Exception e){
            throw new ErrorDeviceException(e.getMessage());
        }
    }

    public static void create(Context context, String moduleVersion, String bluetoothDevice, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        instance = new ModuleBluetooth(context, moduleVersion, bluetoothDevice, event);
        instance.attach();
    }

    @Override
    public void write(String command) {
        clientBluetooth.write(command);
        //bluetoothProcessManager.write(command);
        //threadScaleAttach.write(command);
    }

    @Override
    public ObjectCommand sendCommand(Commands commands) {
        return clientBluetooth.sendCommand(commands);
        //return bluetoothProcessManager.sendCommand(commands);
        //return threadScaleAttach.sendCommand(commands);
    }

    /** Соединится с модулем. */
    @Override
    public void reconnect() /*throws InterruptedException*/ {
        if (clientBluetooth !=null){
            clientBluetooth.killWorkingThread();
        }
        try {
            clientBluetooth = new ClientBluetooth(getContext(), device);
            clientBluetooth.restartWorkingThread();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        /*if (threadAttach !=null){
            threadAttach.interrupt();
        }
        try {
            threadAttach = new Thread(new RunnableConnect());
            threadAttach.start();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }*/
    }

    @Override
    protected void connect() {

    }

    /** Отсоединение от весового модуля.
     * Необходимо использовать перед закрытием программы чтобы остановить работающие процессы
     */
    @Override
    public void dettach() {
        super.dettach();
        if (bluetoothConnectReceiver != null)
            bluetoothConnectReceiver.unregister();
        if (clientBluetooth != null){
            clientBluetooth.killWorkingThread();
        }
    }

    /** Инициализация bluetooth адаптера и модуля.
     * Перед инициализациеи надо создать класс com.kostya.module.ScaleModule
     * Для соединения {@link ModuleBluetooth#attach()}
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

    /** Проверяем адаптер bluetooth и включаем.
     * @throws Exception Ошибки при выполнении.
     */
    private void onEnableBluetooth(final BluetoothAdapter adapter) throws Exception {
        final boolean[] flagTimeout = new boolean[1];
        final Handler handler = new Handler();
        if(adapter == null)
            throw new Exception("Bluetooth adapter missing");
        adapter.enable();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!adapter.isEnabled())
                    flagTimeout[0] = true;
            }
        }, 5000);
        while (!adapter.isEnabled() && !flagTimeout[0]) ;//ждем включения bluetooth
        if(flagTimeout[0])
            throw new Exception("Timeout enabled bluetooth");
    }

    public static ModuleBluetooth getInstance() { return instance; }

    /** Соединится с модулем. */
    @Override
    public void attach() /*throws InterruptedException*/ {
        super.attach();
        if (clientBluetooth !=null){
            clientBluetooth.killWorkingThread();
        }
        try {
            clientBluetooth = new ClientBluetooth(getContext(), device);
            clientBluetooth.restartWorkingThread();
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
        }
        return name;
    }

    //==================================================================================================================

    /** Получить офсет датчика веса.
     * @return Значение офсет.
     * @see Commands#GCO
     */
    public String getModuleOffsetSensor() {
        return Commands.GCO.getParam();
    }

    /** Устанавливаем калибровку батареи.
     * @param percent Значение калибровки в процентах.
     * @return true - Калибровка прошла успешно.
     * @see Commands#CBT
     */
    public boolean setModuleCalibrateBattery(int percent) {
        return Commands.CBT.setParam(percent);
    }

    /*protected class RunnableAttach implements Runnable {
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
    }*/

    /*public class RunnableConnect implements Runnable {
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
                getContext().sendBroadcast(new Intent(MSG.DISCONNECT.name()));
            }finally {
                getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_FINISH));
            }

            Log.i(TAG, "thread done");
        }

        public void cancel() {
            try {mmSocket.close();} catch (IOException e) { }
            Thread.currentThread().interrupt();
        }
    }*/

    public class BluetoothConnectReceiver extends BroadcastReceiver {
        final Context mContext;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

        public BluetoothConnectReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action){
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        try{clientBluetooth.closeSocket();}catch (Exception e){}
                        break;
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        //mContext.sendBroadcast(new Intent(Module.CONNECT));
                    break;
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

}
