package com.konst.scaleslibrary.module;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.konst.scaleslibrary.module.bluetooth.BluetoothHandler;
import com.konst.scaleslibrary.module.bluetooth.BluetoothProcessManager;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.scale.ObjectScales;
import com.konst.scaleslibrary.module.scale.ScaleModule;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Весовой модуль
 *
 * @author Kostya
 */
public abstract class Module implements InterfaceModule{
    private final Context mContext;
    /** Bluetooth устройство модуля весов. */
    protected BluetoothDevice device;
    protected WifiManager wifiManager;
    private Thread moduleThreadProcess;
    protected BluetoothProcessManager bluetoothProcessManager;
    protected BluetoothConnectReceiver bluetoothConnectReceiver;
    /** Bluetooth адаптер терминала. */
    protected BluetoothAdapter bluetoothAdapter;
    private final Handler handler = new Handler();
    public static final String TAG = Module.class.getName();
    protected InterfaceCallbackScales resultCallback;
    /** Константа время задержки для получения байта. */
    private static final int TIMEOUT_GET_BYTE = 1000;
    private boolean flagTimeout;
    protected boolean isAttach;

    /** Константы результат соединения.  */
    public enum ResultConnect {
        /** Соединение и загрузка данных из весового модуля успешно. */
        STATUS_LOAD_OK,
        /** Неизвесная вервия весового модуля. */
        STATUS_VERSION_UNKNOWN,
        /** Конец стадии присоединения (можно использовать для закрытия прогресс диалога). */
        STATUS_ATTACH_FINISH,
        /** Начало стадии присоединения (можно использовать для открытия прогресс диалога). */
        STATUS_ATTACH_START,
        /** Ошибка настриек терминала. */
        TERMINAL_ERROR,
        /** Ошибка настроек весового модуля. */
        MODULE_ERROR,
        /** Ошибка соединения с модулем. */
        CONNECT_ERROR
    }

    protected abstract void dettach();
    protected abstract void attach() throws InterruptedException;
    protected abstract void attachWiFi() throws InterruptedException;
    protected abstract boolean isVersion() throws Exception;
    protected abstract void reconnect();
    protected abstract void load();
    /** Получаем соединение с bluetooth весовым модулем.
     * @throws IOException Ошибка сокета соединения.
     * @throws NullPointerException Нулевое значение.
     */
    //protected abstract void connect() throws IOException, NullPointerException;
    protected abstract void connectWiFi() throws IOException, NullPointerException;

    private Module(Context mContext, InterfaceCallbackScales event) throws Exception {
        this.mContext = mContext;
        /** Проверяем и включаем bluetooth. */
        isEnableBluetooth();
        resultCallback = event;
        Commands.setInterfaceCommand(this);
    }

    /** Конструктор модуля.
     * @param mContext Контекст.
     * @param device Устройство bluetooth.
     * @param event Интерфейс обратного вызова.
     * @throws Exception Ошибка при создании модуля.
     * @throws ErrorDeviceException Ошибка bluetooth утройства.
     */
    protected Module(Context mContext, BluetoothDevice device, InterfaceCallbackScales event)throws Exception, ErrorDeviceException{
        this(mContext, event);
        init(device);
    }

    protected Module(Context mContext, String device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException{
        this(mContext, event);
        try{
            BluetoothDevice tmp = bluetoothAdapter.getRemoteDevice(device);
            init(tmp);
        }catch (Exception e){
            throw new ErrorDeviceException(e.getMessage());
        }
    }

    protected Module(Context mContext, final WifiManager wifiManager, InterfaceCallbackScales event) throws Exception, ErrorDeviceException{
        this.mContext = mContext;
        this.wifiManager = wifiManager;
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"WeightScale\"";
        wc.preSharedKey = "\"12345678\"";
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
// connect to and enable the connection
        this.wifiManager.setWifiEnabled(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!wifiManager.isWifiEnabled())
                    flagTimeout = true;
            }
        }, 5000);
        while (!wifiManager.isWifiEnabled() && !flagTimeout) ;//ждем включения bluetooth
        if(flagTimeout)
            throw new Exception("Timeout enabled bluetooth");
        int netId = wifiManager.addNetwork(wc);
        this.wifiManager.disconnect();
        this.wifiManager.enableNetwork(netId, true);
        this.wifiManager.reconnect();
        resultCallback = event;
    }

    /** Проверяем адаптер bluetooth и включаем.
     * @return true все прошло без ошибок.
     * @throws Exception Ошибки при выполнении.
     */
    private boolean isEnableBluetooth() throws Exception {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
            throw new Exception("Bluetooth adapter missing");
        bluetoothAdapter.enable();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!bluetoothAdapter.isEnabled())
                    flagTimeout = true;
            }
        }, 5000);
        while (!bluetoothAdapter.isEnabled() && !flagTimeout) ;//ждем включения bluetooth
        if(flagTimeout)
            throw new Exception("Timeout enabled bluetooth");
        return true;
    }

    public boolean isAttach() { return isAttach; }

    /*public void setConnectResultCallback(InterfaceResultCallback connectResultCallback) {
        this.resultCallback = connectResultCallback;
    }*/

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
        bluetoothConnectReceiver = new BluetoothConnectReceiver(mContext);
        bluetoothConnectReceiver.register();
    }

    public Context getContext() {
        return mContext;
    }

    /** Получить bluetooth устройство модуля.
     * @return bluetooth устройство.
     */
    protected BluetoothDevice getDevice() {
        return device;
    }

    public String getAddressBluetoothDevice() {
        return device.getAddress(); }

    /** Получить bluetooth адаптер терминала.
     * @return bluetooth адаптер.
     */
    protected BluetoothAdapter getAdapter() {
        return bluetoothAdapter;
    }

    /** Получаем версию программы из весового модуля.
     * @return Версия весового модуля в текстовом виде.
     * @see Commands#VRS
     */
    public String getModuleVersion() {
        return Commands.VRS.getParam();
    }

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

    /** Получаем версию hardware весового модуля.
     * @return Hardware версия весового модуля.
     * @see Commands#HRW
     */
    public String getModuleHardware() {
        return Commands.HRW.getParam();
    }

    public class BluetoothConnectReceiver extends BroadcastReceiver {
        final Context mContext;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

        BluetoothConnectReceiver(Context context){
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
            mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_START).putExtra(InterfaceModule.EXTRA_DEVICE_NAME,getNameBluetoothDevice()));
            //try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            //try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
            try {
                mmSocket.connect();
                bluetoothProcessManager = new BluetoothProcessManager(mmSocket, bluetoothHandler);
            } catch (IOException connectException) {
                try {mmSocket.close();} catch (IOException closeException) { }
                try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR).putExtra(InterfaceModule.EXTRA_MESSAGE, connectException.getMessage()));
            }finally {
                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_FINISH));
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
            mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_START).putExtra(InterfaceModule.EXTRA_DEVICE_NAME,getNameBluetoothDevice()));
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            //try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
            try {
                mmSocket.connect();
                bluetoothProcessManager = new BluetoothProcessManager(mmSocket, bluetoothHandler);
            } catch (IOException connectException) {
                try {mmSocket.close();} catch (IOException closeException) { }
                try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) {}
                bluetoothHandler.obtainMessage(BluetoothHandler.MSG.DISCONNECT.ordinal()).sendToTarget();
            }finally {
                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_FINISH));
            }

            Log.i(TAG, "thread done");
        }

        public void cancel() {
            try {mmSocket.close();} catch (IOException e) { }
            Thread.currentThread().interrupt();
        }
    }

    private final BluetoothHandler bluetoothHandler = new BluetoothHandler(){

        @Override
        public void handleMessage(Message msg) {
            switch (MSG.values()[msg.what]){
                case RECEIVE:
                    ObjectCommand cmd = (ObjectCommand)msg.obj;
                    break;
                case CONNECT:
                    try {
                        if (isVersion()){
                            load();
                            if (!isAttach){
                                isAttach = true;
                                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_LOAD_OK).putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales()));
                            }else {
                                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_RECONNECT_OK).putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales()));
                            }
                        }else {
                            dettach();
                        }
                    } catch (Exception e) {
                        dettach();
                        mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR).putExtra(InterfaceModule.EXTRA_MESSAGE, e.getMessage()));
                    }
                    break;
                case DISCONNECT:
                    if (isAttach)
                        reconnect();
                    break;
                case ERROR:
                    //resultCallback.resultConnect(ResultConnect.CONNECT_ERROR,"Не включен модуль или большое растояние. Если не помогает просто перегрузите телефон.", null);
                    mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR)
                            .putExtra(InterfaceModule.EXTRA_MESSAGE, "Не включен модуль или большое растояние. Если не помогает просто перегрузите телефон."));
                    break;
                default:
            }
        }
    };
}
