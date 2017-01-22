package com.konst.scaleslibrary.module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.google.common.base.Splitter;
import com.konst.scaleslibrary.module.scale.*;
import com.konst.scaleslibrary.module.wifi.ClientWiFi;
import com.konst.scaleslibrary.module.wifi.WifiBaseManager;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Весовой модуль
 *
 * @author Kostya
 */
public abstract class Module implements InterfaceModule{
    private final Context mContext;
    //protected WifiBaseManager wifiBaseManager;
    protected ScaleVersion version;
    protected ObjectScales objectScales = new ObjectScales();
    private ThreadScalesProcess threadScalesProcess;
    //private Thread moduleThreadProcess;
    public final BaseModuleReceiver baseModuleReceiver;
    //protected BluetoothProcessManager bluetoothProcessManager;

    /** Bluetooth адаптер терминала. */
    //protected BluetoothAdapter bluetoothAdapter;
    public static final String TAG = Module.class.getName();
    public static final String RECEIVE = Module.class.getSimpleName() + "RECEIVE";
    public static final String CONNECT = Module.class.getSimpleName() + "CONNECT";
    public static final String DISCONNECT = Module.class.getSimpleName() + "DISCONNECT";
    public static final String ERROR = Module.class.getSimpleName() + "ERROR";
    public static final String COMMAND = Module.class.getSimpleName() + "COMMAND";
    protected InterfaceCallbackScales resultCallback;
    /** Имя версии программы */
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
    /** Флаг использования авто обнуленияю. */
    private boolean enableAutoNull = true;
    /** Флаг обнаружения стабильного веса. */
    private boolean enableProcessStable = true;
    protected boolean isAttach;

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

    /*public enum MSG {
        RECEIVE,
        CONNECT,
        DISCONNECT,
        ERROR
    }*/
    protected abstract void connect();
    protected abstract void reconnect();

    protected Module(Context context) {
        mContext = context;
        baseModuleReceiver = new BaseModuleReceiver(mContext);

    }

    /*protected Module(Context context, String version) throws Exception {
        this(context);
        versionName = version;
    }*/

    /** Конструктор модуля.
     * @param context Контекст.
     * @param event Интерфейс обратного вызова.
     * @throws Exception Ошибка при создании модуля.
     */
    protected Module(Context context, String version, InterfaceCallbackScales event) throws Exception {
        this(context);
        resultCallback = event;
        versionName = version;
        Commands.setInterfaceCommand(this);
    }

    /*protected Module(Context context, final WifiManager wifiManager, InterfaceCallbackScales event) throws Exception {
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
    }*/

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
    private int getModuleTemperature(int data) {
        try {
            return (int) ((double) (float) (( data - 0x800000) / 7169) / 0.81) - 273;
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

    /** Определяем после соединения это весовой модуль и какой версии.
     * Проверяем версию указаной при инициализации класса com.kostya.module.ScaleModule.
     * @return true - Версия правильная.
     */
    protected boolean isVersion() throws Exception {
        Commands.setInterfaceCommand(this);
        String vrs = getModuleVersion(); //Получаем версию весов
        if (vrs.startsWith(versionName)) {
            try {
                String s = vrs.replace(versionName, "");
                versionNum = Integer.valueOf(s);
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

    /** Выключить питание модуля.
     * @return true - питание модкля выключено.
     */
    public boolean powerOff() {return Commands.POF.getParam().equals(Commands.POF.getName());}

    public boolean isAttach() { return isAttach; }

    public void attach(){
        baseModuleReceiver.register();
    }

    public void dettach() {
        isAttach = false;
        scalesProcessEnable(false);
        if (baseModuleReceiver != null){
            baseModuleReceiver.unregister();
        }
    }

    public void load() {
        try {
            version.extract();
        }  catch (ErrorTerminalException e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_TERMINAL_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        } catch (Exception e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_MODULE_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        }
        resultCallback.onCreate(this);
    }

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

    public class BaseModuleReceiver extends BroadcastReceiver{
        private final Context mContext;
        private final IntentFilter intentFilter;
        private boolean isRegistered;
        private int numTimeTemp;
        /** Временная переменная для хранения веса. */
        private int tempWeight;

        BaseModuleReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(CONNECT);
            intentFilter.addAction(DISCONNECT);
            intentFilter.addAction(ERROR);
            intentFilter.addAction(COMMAND);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CONNECT.equals(action)){
                try {
                    if (isVersion()){
                        load();
                        if (isAttach) {
                            mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_RECONNECT_OK)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
                        } else {
                            isAttach = true;
                            mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_LOAD_OK)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
                        }
                    }else {
                        throw new Exception("Ошибка проверки версии.");
                    }
                } catch (Exception e) {
                    dettach();
                    mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR).putExtra(InterfaceModule.EXTRA_MESSAGE, e.getMessage()));
                }finally {
                    getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_ATTACH_FINISH));
                }
            }else if (DISCONNECT.equals(action)){
                if (isAttach)
                    reconnect();
            }else if (ERROR.equals(action)){
                mContext.sendBroadcast(new Intent(InterfaceModule.ACTION_CONNECT_ERROR)
                        .putExtra(InterfaceModule.EXTRA_MESSAGE, "Не включен модуль или большое растояние. Если не помогает просто перегрузите телефон."));
            }else if (COMMAND.equals(action)){
                ObjectCommand obj = (ObjectCommand) intent.getSerializableExtra(InterfaceModule.EXTRA_SCALES);
                if (obj == null)
                    return;
                try{
                    Map<String, String> map = Splitter.on( " " ).withKeyValueSeparator( '=' ).split( obj.getValue() );
                    /* Секция вес. */
                    int temp = (int) (Integer.valueOf(map.get("d")) * getCoefficientA());
                    version.setSensorTenzoOffset(Integer.valueOf(map.get("d")));
                    ResultWeight resultWeight;
                    if (temp == Integer.MIN_VALUE) {
                        resultWeight = ResultWeight.WEIGHT_ERROR;
                    } else {
                        if (version.isLimit())
                            resultWeight = version.isMargin() ? ResultWeight.WEIGHT_MARGIN : ResultWeight.WEIGHT_LIMIT;
                        else {
                            resultWeight = ResultWeight.WEIGHT_NORMAL;
                        }
                    }
                    objectScales.setWeight(getWeightToStepMeasuring(temp));
                    objectScales.setResultWeight(resultWeight);
                    objectScales.setTenzoSensor(version.getSensor());
                    /* Секция авто ноль. */
                    if (enableAutoNull){
                        if (version.getWeight() != Integer.MIN_VALUE && Math.abs(version.getWeight()) < weightError) { //автоноль
                            autoNull += 1;
                            if (autoNull > timerZero * (ThreadScalesProcess.DIVIDER_AUTO_NULL / (filterADC==0?1:filterADC))) {
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
                        objectScales.setBattery(Integer.valueOf(map.get("b")));
                        objectScales.setTemperature(getModuleTemperature(Integer.valueOf(map.get("t"))));
                    }
                    getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_SCALES_RESULT).putExtra(InterfaceModule.EXTRA_SCALES, objectScales));
                }catch (Exception e){}
                numTimeTemp--;
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
                    ResultWeight resultWeight;
                    if (temp == Integer.MIN_VALUE) {
                        resultWeight = ResultWeight.WEIGHT_ERROR;
                    } else {
                        if (version.isLimit())
                            resultWeight = version.isMargin() ? ResultWeight.WEIGHT_MARGIN : ResultWeight.WEIGHT_LIMIT;
                        else {
                            resultWeight = ResultWeight.WEIGHT_NORMAL;
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
                        objectScales.setTemperature(getModuleTemperature(Integer.valueOf(Commands.DTM.getParam())));

                    }
                    getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_SCALES_RESULT).putExtra(InterfaceModule.EXTRA_SCALES, objectScales));
                }catch (Exception e){}
                numTimeTemp--;
                try { TimeUnit.MILLISECONDS.sleep(PERIOD_UPDATE); } catch (InterruptedException e) {}
            }
            Log.i(TAG, "interrupt");
        }

        @Override
        public void interrupt() {
            super.interrupt();
            cancel = true;
        }
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
    /* Включаем выключаем процесс определения стабильного веса.
      @param stable true - процесс запускается, false - процесс останавливается.
     */
    public void setEnableProcessStable(boolean stable) {
        objectScales.setStableNum(0);
        enableProcessStable = stable;
    }
}
