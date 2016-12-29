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
import com.konst.scaleslibrary.module.scale.*;
import com.konst.scaleslibrary.module.wifi.ClientWiFi;
import com.konst.scaleslibrary.module.wifi.WifiBaseManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Весовой модуль
 *
 * @author Kostya
 */
public abstract class Module implements InterfaceModule{
    private final Context mContext;

    protected WifiBaseManager wifiBaseManager;
    protected ClientWiFi clientWiFi;
    protected ScaleVersion version;
    protected ObjectScales objectScales = new ObjectScales();
    private ThreadScalesProcess threadScalesProcess;
    //private Thread moduleThreadProcess;
    private BaseModuleReceiver baseModuleReceiver;
    protected BluetoothProcessManager bluetoothProcessManager;

    /** Bluetooth адаптер терминала. */
    protected BluetoothAdapter bluetoothAdapter;
    private final Handler handler = new Handler();
    public static final String TAG = Module.class.getName();
    protected InterfaceCallbackScales resultCallback;
    protected String versionName;
    /** Номер версии программы. */
    protected int versionNum;
    /** Процент заряда батареи (0-100%). */
    private int battery;
    /** Температура в целсиях. */
    private int temperature;
    /** АЦП-фильтр (0-15). */
    private int filterADC;
    /** Время выключения весов. */
    private int timeOff;
    /** Предельный вес взвешивания. */
    private int weightMargin;
    /** Калибровочный коэффициент a. */
    private float coefficientA;
    /** Калибровочный коэффициент b. */
    private float coefficientB;
    private String spreadsheet;
    private String userName;
    /* Пароль акаунта google.*/
    private String password;
    /* Номер телефона. */
    private String phone;
    /** Текущее показание датчика веса. */
    private int sensorTenzo;
    /** Максимальное показание датчика. */
    private int limitTenzo;
    /** Номер пломбы*/
    private int seal;
    /** Счётчик автообнуления. */
    private int autoNull;
    /** Время срабатывания авто ноля. */
    private int timerZero;
    /** Погрешность веса автоноль. */
    private int weightError;
    /** Шаг дискреты. */
    private int stepScale = 1;
    /** Дельта стабилизации веса. */
    private int deltaStab = 10;
    /** Количество стабильных показаний веса для авто сохранения. */
    public static final int STABLE_NUM_MAX = 15;
    /** Константа время задержки для получения байта. */
    private static final int TIMEOUT_GET_BYTE = 1000;
    /** Флаг использования авто обнуленияю. */
    private boolean enableAutoNull = true;
    /** Флаг обнаружения стабильного веса. */
    private boolean enableProcessStable = true;
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
    //protected abstract void attach();
    //protected abstract void attachWiFi();
    protected abstract boolean isVersion() throws Exception;
    protected abstract void connect();
    protected abstract void reconnect();
    protected abstract void load();
    /** Получаем соединение с bluetooth весовым модулем.
     * @throws IOException Ошибка сокета соединения.
     * @throws NullPointerException Нулевое значение.
     */
    //protected abstract void connect() throws IOException, NullPointerException;
    //protected abstract void connectWiFi();
    protected Module(Context context) {
        mContext = context;
        baseModuleReceiver = new BaseModuleReceiver(mContext);
        baseModuleReceiver.register();
    }

    protected Module(Context context, String version) throws Exception {
        this(context);
        versionName = version;
    }

    /** Конструктор модуля.
     * @param context Контекст.
     * @param event Интерфейс обратного вызова.
     * @throws Exception Ошибка при создании модуля.
     * @throws ErrorDeviceException Ошибка bluetooth утройства.
     */
    protected Module(Context context, InterfaceCallbackScales event) throws Exception {
        this(context);
        /* Проверяем и включаем bluetooth. */
        isEnableBluetooth();
        resultCallback = event;
        Commands.setInterfaceCommand(this);
    }

    protected Module(Context context, final WifiManager wifiManager, InterfaceCallbackScales event) throws Exception {
        this(context, event);

        wifiBaseManager = new WifiBaseManager(context,"SCALES.ESP.36.6.4","12345678", new WifiBaseManager.OnWifiBaseManagerListener() {
            @Override
            public void onConnect(String ssid, InetSocketAddress ipAddress) {
                clientWiFi = new ClientWiFi(mContext, ipAddress);
                clientWiFi.restartWorkingThread();

                Log.i(TAG,"Соединение с сетью " + ssid);
            }

            @Override
            public void onDisconnect() {clientWiFi.killWorkingThread();}
        });
        /*WifiConfiguration wc = new WifiConfiguration();
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
            throw new Exception("Timeout enabled wifi");
        int netId = wifiManager.addNetwork(wc);
        this.wifiManager.disconnect();
        this.wifiManager.enableNetwork(netId, true);
        this.wifiManager.reconnect();
        resultCallback = event;*/
    }

    /** Установить значение фильтра АЦП.
     * @param filterADC Значение АЦП.*/
    public void setFilterADC(int filterADC) {this.filterADC = filterADC;}
    /** Получить сохраненое значение фильтраАЦП.
     * @return Значение фильтра от 1 до 15.   */
    public int getFilterADC() {return filterADC;}
    /** Установливаем новое значение АЦП в весовом модуле. Знчение от1 до 15.
     * @param filterADC Значение АЦП от 1 до 15.
     * @return true Значение установлено.
     * @see Commands#FAD
     */
    public boolean setModuleFilterADC(int filterADC) {
        if(Commands.FAD.setParam(filterADC)){
            this.filterADC = filterADC;
            return true;
        }
        return false;
    }

    /** Получить время работы при бездействии модуля.
     * @return Время в минутах.  */
    public int getTimeOff() {
        return timeOff;
    }
    /** Установить время бездействия модуля.
     * @param timeOff Время в минутах.
     */
    public void setTimeOff(int timeOff) {
        this.timeOff = timeOff;
    }
    /** Записываем в весовой модуль время бездействия устройства.
     * По истечению времени модуль выключается.
     * @param timeOff Время в минутах.
     * @return true Значение установлено.
     * @see Commands#TOF
     */
    public boolean setModuleTimeOff(int timeOff) {
        if(Commands.TOF.setParam(timeOff)){
            this.timeOff = timeOff;
            return true;
        }
        return false;
    }

    public void setWeightMargin(int weightMargin) {this.weightMargin = weightMargin;}
    public int getWeightMargin() {return weightMargin;}

    /** Получить коэффициент каллибровки.
     * @return Значение коэффициента. */
    public float getCoefficientA() { return coefficientA; }
    /** Усттановить коэффициент каллибровки (только локально не в модуле).
     * @param coefficientA Значение коэффициента.     */
    public void setCoefficientA(float coefficientA) {
        this.coefficientA = coefficientA;
    }

    /** Получить коэффициент смещения.
     * @return Значение коэффициента.  */
    public float getCoefficientB() {
        return coefficientB;
    }
    /** Усттановить коэффициент смещения (только локально не в модуле).
     * @param coefficientB Значение коэффициента.     */
    public void setCoefficientB(float coefficientB) {
        this.coefficientB = coefficientB;
    }

    public void setSensorTenzo(int sensorTenzo) {
        this.sensorTenzo = sensorTenzo;
    }
    public int getSensorTenzo() {
        return sensorTenzo;
    }
    /** Получить значение датчика веса.
     * @return Значение датчика.
     * @see Commands#DCH
     */
    public String feelWeightSensor() {return Commands.DCH.getParam();}

    /** Получить таблицу google disk.
     * @return Имя таблици.*/
    public String getSpreadsheet() { return spreadsheet; }
    public void setSpreadsheet(String spreadsheet) {
        this.spreadsheet = spreadsheet;
    }
    /** Устанавливаем имя spreadsheet google drive в модуле.
     * @param sheet Имя таблици.
     * @return true - Имя записано успешно.
     */
    public boolean setModuleSpreadsheet(String sheet) {
        if (Commands.SGD.setParam(sheet)){
            spreadsheet = sheet;
            return true;
        }
        return false;
    }

    public void setUserName(String userName){this.userName = userName;}
    /** Получить имя акаунта google.
     * @return Имя акаунта. */
    public String getUserName() {
        return userName;
    }
    /** Устанавливаем имя аккаунта google в модуле.
     * @param userName Имя аккаунта.
     * @return true - Имя записано успешно.
     */
    public boolean setModuleUserName(String userName) {
        if (Commands.UGD.setParam(userName)){
            this.userName = userName;
            return true;
        }
        return false;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    /** Получить пароль акаута google.
     * @return Пароль.   */
    public String getPassword() {
        return password;
    }
    /** Устанавливаем пароль в google.
     * @param password Пароль аккаунта.
     * @return true - Пароль записано успешно.
     */
    public boolean setModulePassword(String password) {
        if (Commands.PGD.setParam(password)){
            this.password = password;
            return true;
        }
        return false;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    /** Получить номер телефона.
     * @return Номер телефона.   */
    public String getPhone() {
        return phone;
    }
    /** Устанавливаем номер телефона. Формат "+38хххххххххх".
     * @param phone Номер телефона.
     * @return true - телефон записано успешно.
     */
    public boolean setModulePhone(String phone) {
        if(Commands.PHN.setParam(phone)){
            this.phone = phone;
            return true;
        }
        return false;
    }

    /** Получаем сервис код.
     * @return код
     * @see Commands#SRC
     */
    public String getModuleServiceCod() {
        return Commands.SRC.getParam();
        //return cmd(InterfaceVersions.CMD_SERVICE_COD);
    }
    /** Установливаем сервис код.
     * @param cod Код.
     * @return true Значение установлено.
     * @see Commands#SRC
     */
    public boolean setModuleServiceCod(String cod) {
        return Commands.SRC.setParam(cod);
    }

    public int getWeightMax(){return version.getWeightMax();}
    public void setWeightMax(int weightMax) {version.setWeightMax(weightMax);}

    /** Получить максиматьное значение датчика.
     * @return Значение датчика. */
    public int getLimitTenzo(){return limitTenzo;}
    /** Установить максимальное значение датчика.
     * @param limitTenzo Значение датчика.     */
    public void setLimitTenzo(int limitTenzo) {this.limitTenzo = limitTenzo;}

    public int getMarginTenzo() {return version.getMarginTenzo();}

    public int getSeal(){return seal;}
    public void setSeal(int seal){this.seal = seal;}

    /** Получаем значение веса погрешности для расчета атоноль.
     * @return возвращяет значение веса.
     */
    public int getWeightError() {
        return weightError;
    }
    /** Сохраняем значение веса погрешности для расчета автоноль.
     * @param weightError Значение погрешности в килограмах.
     */
    public void setWeightError(int weightError) {
        this.weightError = weightError;
    }

    /** Время для срабатывания автоноль.
     * @return возвращяем время после которого установливается автоноль.
     */
    public int getTimerZero() {
        return timerZero;
    }
    /** Устонавливаем значение времени после которого срабатывает автоноль.
     * @param timer Значение времени в секундах.
     */
    public void setTimerZero(int timer) {
        timerZero = timer;
    }

    /** Получить температуру модуля.
     * @return Значение температуры. */
    public int getTemperature() {return temperature; }
    /** Получаем значение температуры весового модуля.
     * @return Температура в градусах.
     * @see Commands#DTM
     */
    private int getModuleTemperature() {
        try {
            int temp = Integer.valueOf(Commands.DTM.getParam());
            return (int) ((double) (float) (( temp - 0x800000) / 7169) / 0.81) - 273;
        } catch (Exception e) {
            return -273;
        }
    }

    /** Получаем значение заряда батерии.
     * @return Заряд батареи в процентах.
     * @see Commands#GBT
     */
    public int getModuleBatteryCharge() {
        try {
            battery = Integer.valueOf(Commands.GBT.getParam());
        } catch (Exception e) {
            battery = -0;
        }
        return battery;
    }
    /** Устанавливаем заряд батареи.
     * Используется для калибровки заряда батареи.
     * @param charge Заряд батереи в процентах.
     * @return true - Заряд установлен.
     * @see Commands#CBT
     */
    public boolean setModuleBatteryCharge(int charge) {
        if(Commands.CBT.setParam(charge)){
            battery = charge;
            return true;
        }
        return false;
    }

    public int getDeltaStab() {
        return deltaStab;
    }
    public void setDeltaStab(int deltaStab) {
        this.deltaStab = deltaStab;
    }

    public int getStepScale() {return stepScale; }
    public void setStepScale(int stepScale) {
        if (stepScale == 0)
            return;
        this.stepScale = stepScale;
    }

    public boolean writeData() {
        return version.writeData();
    }

    /** Получаем класс загруженой версии весового модуля.
     * @return класс версии весового модуля.
     */
    public ScaleVersion getVersion() {return version;}
    /** Получить номер версии программы.
     * @return Номер версии.  */
    public int getVersionNum() { return versionNum; }

    /** Выключить питание модуля.
     * @return true - питание модкля выключено.
     */
    public boolean powerOff() {return Commands.POF.getParam().equals(Commands.POF.getName());}

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

    public void setEnableAutoNull(boolean enableAutoNull) {this.enableAutoNull = enableAutoNull;}

    /** Определяем версию весов.
     * @param version Имя версии.
     * @return Экземпляр версии.
     * @throws  Exception Ошибка неправильная версия весов.
     */
    protected ScaleVersion fetchVersion(int version) throws Exception {
        switch (version) {
            case 1:
                return new ScaleVersion1(this);
            case 4:
                return new ScaleVersion4(this);
            default:
                throw new Exception("illegal version");
        }
    }

    public Context getContext() {
        return mContext;
    }

    /** Получить bluetooth адаптер терминала.
     * @return bluetooth адаптер.
     */
    protected BluetoothAdapter getAdapter() {return bluetoothAdapter;}

    /** Получаем версию программы из весового модуля.
     * @return Версия весового модуля в текстовом виде.
     * @see Commands#VRS
     */
    public String getModuleVersion() {return Commands.VRS.getParam();}

    /** Получаем версию hardware весового модуля.
     * @return Hardware версия весового модуля.
     * @see Commands#HRW
     */
    public String getModuleHardware() {
        return Commands.HRW.getParam();
    }

    /** Установить обнуление.
     * @return true - Обнуление установлено.
     */
    public synchronized boolean setOffsetScale() {
        return version.setOffsetScale();
    }

    public void resetAutoNull(){ autoNull = 0; }



    /*private final BluetoothHandler bluetoothHandler = new BluetoothHandler(){

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
                                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_LOAD_OK)*//*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*//*);
                            }else {
                                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_RECONNECT_OK)*//*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*//*);
                            }
                        }else {
                            throw new Exception("Ошибка проверки версии.");
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
    };*/

    class BaseModuleReceiver extends BroadcastReceiver{
        private Context mContext;
        private IntentFilter intentFilter;
        private boolean isRegistered;

        BaseModuleReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(BluetoothHandler.MSG.CONNECT.name());
            intentFilter.addAction(BluetoothHandler.MSG.DISCONNECT.name());
            intentFilter.addAction(BluetoothHandler.MSG.ERROR.name());
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (BluetoothHandler.MSG.valueOf(action)){
                case CONNECT:
                    try {
                        if (isVersion()){
                            load();
                            if (!isAttach){
                                isAttach = true;
                                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_LOAD_OK)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
                            }else {
                                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_RECONNECT_OK)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
                            }
                        }else {
                            throw new Exception("Ошибка проверки версии.");
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
                    mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR)
                            .putExtra(InterfaceModule.EXTRA_MESSAGE, "Не включен модуль или большое растояние. Если не помогает просто перегрузите телефон."));
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

    private class ThreadScalesProcess extends Thread{
        //private final ObjectScales objectScales;
        private int numTimeTemp;
        /** Временная переменная для хранения веса. */
        private int tempWeight;
        private boolean cancel;
        /** Делитель для авто ноль. */
        private static final int DIVIDER_AUTO_NULL = 15;
        /** Время обновления значения веса в милисекундах. */
        private static final int PERIOD_UPDATE = 20;

        ThreadScalesProcess(){
            //objectScales = new ObjectScales();
        }

        @Override
        public void run() {
            while (!interrupted() && !cancel){
                try{
                    /* Секция вес. */
                    int temp = version.updateWeight();
                    ScaleModule.ResultWeight resultWeight;
                    if (temp == Integer.MIN_VALUE) {
                        resultWeight = ScaleModule.ResultWeight.WEIGHT_ERROR;
                    } else {
                        if (version.isLimit())
                            resultWeight = version.isMargin() ? ScaleModule.ResultWeight.WEIGHT_MARGIN : ScaleModule.ResultWeight.WEIGHT_LIMIT;
                        else {
                            resultWeight = ScaleModule.ResultWeight.WEIGHT_NORMAL;
                        }
                    }
                    objectScales.setWeight(getWeightToStepMeasuring(temp));
                    objectScales.setResultWeight(resultWeight);
                    objectScales.setTenzoSensor(version.getSensor());
                    /* Секция авто ноль. */
                    if (enableAutoNull){
                        if (version.getWeight() != Integer.MIN_VALUE && Math.abs(version.getWeight()) < weightError) { //автоноль
                            autoNull += 1;
                            if (autoNull > timerZero * (DIVIDER_AUTO_NULL / (filterADC==0?1:filterADC))) {
                                setOffsetScale();
                                autoNull = 0;
                            }
                        } else {
                            autoNull = 0;
                        }
                    }
                    /* Секция определения стабильного веса. */
                    if (enableProcessStable){
                        if (tempWeight - getDeltaStab() <= objectScales.getWeight() && tempWeight + getDeltaStab() >= objectScales.getWeight()) {
                            if (objectScales.getStableNum() <= STABLE_NUM_MAX){
                                if (objectScales.getStableNum() == STABLE_NUM_MAX) {
                                    getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_WEIGHT_STABLE).putExtra(InterfaceModule.EXTRA_SCALES, objectScales));
                                    //objectScales.setFlagStab(true);
                                }
                                objectScales.setStableNum(objectScales.getStableNum()+1);
                            }
                        } else {
                            objectScales.setStableNum(0);
                            //objectScales.setFlagStab(false);
                        }
                        tempWeight = objectScales.getWeight();
                    }
                    /* Секция батарея температура. */
                    if (numTimeTemp == 0){
                        numTimeTemp = 250;
                        objectScales.setBattery(getModuleBatteryCharge());
                        objectScales.setTemperature(getModuleTemperature());

                    }
                    getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_SCALES_RESULT).putExtra(InterfaceModule.EXTRA_SCALES, objectScales));
                }catch (Exception e){}
                numTimeTemp--;
                try { TimeUnit.MILLISECONDS.sleep(PERIOD_UPDATE); } catch (InterruptedException e) {}
            }
            Log.i(TAG, "interrupt");
        }

        /**
         * Преобразовать вес в шкалу шага веса.
         * Шаг измерения установливается в настройках.
         *
         * @param weight Вес для преобразования.
         * @return Преобразованый вес. */
        private int getWeightToStepMeasuring(int weight) {
            return (weight / stepScale) * stepScale;
            //return weight / globals.getStepMeasuring() * globals.getStepMeasuring();
        }

        @Override
        public void interrupt() {
            super.interrupt();
            cancel = true;
        }
    }
    public void scalesProcessEnable(boolean process){
        try {
            if (process){
                if (threadScalesProcess != null)
                    threadScalesProcess.interrupt();
                threadScalesProcess = new ThreadScalesProcess();
                threadScalesProcess.start();
            }else {
                if (threadScalesProcess != null)
                    threadScalesProcess.interrupt();
            }
        }catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    public void setEnableProcessStable(boolean stable) {
        objectScales.setStableNum(0);
        enableProcessStable = stable;
    }
}
