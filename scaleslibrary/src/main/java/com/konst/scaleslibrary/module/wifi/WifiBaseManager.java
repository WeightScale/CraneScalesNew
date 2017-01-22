package com.konst.scaleslibrary.module.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.*;
import android.util.Log;
import com.konst.scaleslibrary.internet.Internet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;


/**
 * @author Kostya
 */
public class WifiBaseManager {
    private final Context context;
    private ConnectionReceiver connectionReceiver;
    private BaseReceiver baseReceiver;
    private SupplicantDisconnectReceiver supplicantDisconnectReceiver;
    private ScanWifiReceiver scanWifiReceiver;
    private final Internet internet;
    /** Обратный вызов события соединения с сетью. */
    private final OnWifiBaseManagerListener onWifiBaseManagerListener;
    private final WifiManager wifiManager;
    private InetSocketAddress inetSocketAddress;
    /** Порт соединения с сервером. Зарезервирован для весов. */
    private static final int PORT = 1011;
    private String ssid = "", pass = "";
    private static final String TAG = WifiBaseManager.class.getName();
    private static final String PSK = "PSK";
    private static final String WEP = "WEP";
    private static final String OPEN = "Open";

    public interface OnWifiBaseManagerListener{
        /** Событие соединение с конкретной сетью.
         * @param ssid Имя Сети
         * @param ipAddress Адресс сервера.
         */
        void onConnect(String ssid, InetSocketAddress ipAddress);

        /** Событие рассоединение с конкретной сетью. */
        void onDisconnect();
    }

    /** Конструктор.
     * @param context Контекст программы.
     * @param listener Слушатель событий соединения.
     */
    public WifiBaseManager(Context context, String ssid, String key, OnWifiBaseManagerListener listener){
        this.context = context;
        this.ssid = ssid;
        pass = key;
        onWifiBaseManagerListener = listener;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        internet = new Internet(context);
        scanWifiReceiver = new ScanWifiReceiver(context);
        baseReceiver = new BaseReceiver(context);
        baseReceiver.register();
        //handleWIFI();
    }

    /** Вызывается когда присоединились к конкретной сети. */
    private void onAttachNetwork(){
        /* Регистрируем приемник на disconnect. */
        supplicantDisconnectReceiver = new SupplicantDisconnectReceiver(context);
        supplicantDisconnectReceiver.register();
        try {
            inetSocketAddress = getInetAddressServer(PORT);
            onWifiBaseManagerListener.onConnect(ssid, inetSocketAddress);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void setSsid(String ssid) {this.ssid = ssid;}

    public void setPass(String pass) {this.pass = pass;}

    /** Получить TCP адресс сервера в формате InetSocketAddress.
     * @return InetSocketAddress.
     */
    public InetSocketAddress getInetSocketAddress() { return inetSocketAddress; }

    /** Начало подключения к определенной сети wifi. */
    protected void handleWIFI() {
        if (wifiManager.isWifiEnabled()) {
            connectToSpecificNetwork();
        }
    }

    /** Подключение к определенной сети wifi. */
    private void connectToSpecificNetwork() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        try {
            /* Проверяем сеть на соединение и имя конкретной сети.
              Если верно то вызываем обратный вызов  и запускаем приемник на событие disconnect.  */
            if (networkInfo.isConnected() && wifiInfo.getSSID().replace("\"", "").equals(ssid)) {
                onAttachNetwork();
                return;
            }else {
                wifiManager.disconnect();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        /* Регистритуем приемник на прием события результат сканирования.*/
        scanWifiReceiver.register();
        /* Запускаем сканирование сети. */
        wifiManager.startScan();
    }

    private void connectNet(int netId){
        try {connectionReceiver.unregister();} catch (Exception e) {} // do nothing
        connectionReceiver = new ConnectionReceiver(context);
        connectionReceiver.register(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    /** Получаем тип безопасности сети.
     * @param scanResult Результат сканирования wifi.
     * @return Тип WEP, PSK, OPEN
     */
    private String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {WEP, PSK};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
        return OPEN;
    }

    public InetSocketAddress getInetAddressServer(int port) throws IOException {
        int ip = wifiManager.getDhcpInfo().serverAddress;
        byte[] byteAddress = {
                (byte) (ip & 0xff),
                (byte) (ip >> 8 & 0xff),
                (byte) (ip >> 16 & 0xff),
                (byte) (ip >> 24 & 0xff)
        };
        return new InetSocketAddress(InetAddress.getByAddress(byteAddress), port);
    }

    /** Приемник событий связаных со сканированием wifi. */
    private class ScanWifiReceiver extends BroadcastReceiver {
        private Context mContext;
        private IntentFilter intentFilter;
        protected boolean isRegistered;

        ScanWifiReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            /* Получаем результат сканирования сети.*/
            List<ScanResult> scanResultList = wifiManager.getScanResults();
            try {
                String security = null;
                boolean found = false;
                /* Сравниваем результат с конктетной сетью. */
                for (ScanResult scanResult : scanResultList) {
                    /* Если верно то конкретная сеть есть в сети.*/
                    if (scanResult.SSID.equals(ssid)) {
                        /* Получаем тип безопасности сети. */
                        security = getScanResultSecurity(scanResult);
                        /* Флаг конкретная сеть в сети. */
                        found = true;
                        break; // found don't need continue
                    }
                }
                /* Провероверяем конкретную сеть в сохраненных конфигурациях.*/
                if (found) {
                    boolean isConfigNet = false;
                    WifiConfiguration conf = new WifiConfiguration();
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for (WifiConfiguration wifiConfiguration : list){
                        try {
                            /* Если конкретная сеть есть в конфигурациях. */
                            if (wifiConfiguration.SSID.replace("\"", "").equals(ssid)){
                                /* сохраняем конфигурацию во временный переменную.*/
                                conf = wifiConfiguration;
                                /* Флаг конкретная сеть есть в конфигурациях.*/
                                isConfigNet = true;
                                break;
                            }
                        }catch (Exception e){
                            /* Значит конфигурация сети дает исключение. Удаляем конфигурацию. */
                            wifiManager.removeNetwork(wifiConfiguration.networkId);
                            wifiManager.saveConfiguration();
                        }
                    }
                    conf.SSID = '"' + ssid + '"';
                    switch (security) {
                        case WEP:
                            conf.wepKeys[0] = '"' + pass + '"';
                            conf.wepTxKeyIndex = 0;
                            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                            break;
                        case PSK:
                            conf.preSharedKey = '"' + pass + '"';
                            break;
                        case OPEN:
                            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                            break;
                        default:
                    }
                    /* Удаляем регистрацию приемника. */
                    try {connectionReceiver.unregister();} catch (Exception e) {} // do nothing
                    /* Регестрируем приемник заново. */
                    connectionReceiver = new ConnectionReceiver(context);
                    IntentFilter intentFilter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
                    intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    connectionReceiver.register(intentFilter);
                    /* Если нет то добавляем конкретную сеть в список конфигураций. */
                    if(!isConfigNet){
                        conf.networkId = wifiManager.addNetwork(conf);
                    }
                    int netId = wifiManager.updateNetwork(conf);
                    /* Ошибка добавления конфигурации сети. */
                    if(netId == -1)
                        return;
                    /* Сохраняем конфиругацию и перезапускаем сеть. */
                    wifiManager.saveConfiguration();
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(netId, true);
                    wifiManager.reconnect();
                    /* Удаляем регистрацию приемника. */
                    unregister();
                    return; /* Выходим после реконекта. */
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            //Todo сделать сообщение что нет соединения с сетью
        }

        public void register() {
            isRegistered = true;
            context.registerReceiver(this, intentFilter);
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    /** Приемник событий связаных с соединением wifi.  */
    private class ConnectionReceiver extends BroadcastReceiver {
        private Context mContext;
        protected boolean isRegistered;

        ConnectionReceiver(Context context){
            mContext = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            /* Проверяем событие соединение с конкретной сетью. */
            if (networkInfo.isConnected() && wifiInfo.getSSID().replace("\"", "").equals(ssid)) {
                /* Если верно удаляем приемник сообщений. */
                unregister();
                /* Посылаем событие соединение. */
                onAttachNetwork();
                return;
            }
            /* Проверяем событие ОШИБКА АВТОРИЗАЦИИ при подключении к сети. */
            int error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if(error== WifiManager.ERROR_AUTHENTICATING){
                /* Удаляем приемник сообщений. */
                unregister();
                /* Запускаем приемник на прием события результат сканирования.*/
                scanWifiReceiver.register();
                /* Запускаем сканирование сети. */
                wifiManager.startScan();
            }
        }

        void register(IntentFilter intentFilter){
            isRegistered = true;
            mContext.registerReceiver(this, intentFilter);
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }

    }

    /** Приемник событий связяных с вкл/выкл wifi. */
    private class BaseReceiver extends BroadcastReceiver {
        private Context mContext;
        private IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            /* Проверяем событие на выключение и включение WiFi приемника. */
            String action = intent.getAction();
            switch (action){
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    switch(extraWifiState){
                        case WifiManager.WIFI_STATE_DISABLED:
                            /* Если приемник был выключен заново включаем. */
                            internet.turnOnWiFiConnection(true);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            /* Приемник включен. Соеденяемся с конкретной сетью заново. */
                            connectToSpecificNetwork();
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                            break;
                        default:
                    }
                    break;
                default:
            }
        }

        public void register() {
            isRegistered = true;
            mContext.registerReceiver(this, intentFilter);
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    /** Приемник событий связяных с disconnect. */
    private class SupplicantDisconnectReceiver extends BroadcastReceiver {
        private Context mContext;
        protected boolean isRegistered;
        private IntentFilter intentFilter;

        SupplicantDisconnectReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    /* Если событие disconnect. */
                    if (state == SupplicantState.DISCONNECTED){
                        onWifiBaseManagerListener.onDisconnect();
                        /* Удаляем приемник сообщений. */
                        unregister();
                        /* Запускаем приемник на прием события результат сканирования.*/
                        scanWifiReceiver.register();
                        /* Запускаем сканирование сети. */
                        wifiManager.startScan();
                        Log.i(TAG, "Разьединение с сетью " + ssid);
                    }
                    break;
                default:
            }
        }

        public void register() {
            isRegistered = true;
            mContext.registerReceiver(this, intentFilter);
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    public void terminate(){
        try {connectionReceiver.unregister();}catch (NullPointerException e){}
        try {supplicantDisconnectReceiver.unregister();}catch (NullPointerException e){}
        baseReceiver.unregister();
        scanWifiReceiver.unregister();
    }

}
