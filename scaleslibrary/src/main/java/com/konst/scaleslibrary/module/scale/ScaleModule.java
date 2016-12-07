
package com.konst.scaleslibrary.module.scale;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.konst.scaleslibrary.module.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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
    private ObjectScales objectScales = new ObjectScales();
    private ScaleVersion version;
    private ThreadScalesProcess threadScalesProcess;
    //private ThreadStableProcess threadStableProcess;
    private static final String TAG = ScaleModule.class.getName();
    private Thread threadAttach;
    /** Процент заряда батареи (0-100%). */
    private int battery;
    /** Температура в целсиях. */
    private int temperature;
    /** Погрешность веса автоноль. */
    private int weightError;
    /** Счётчик автообнуления. */
    private int autoNull;
    /** Время срабатывания авто ноля. */
    private int timerZero;
    /** Номер версии программы. */
    private int numVersion;
    /** Имя версии программы */
    private final String versionName;
    /** АЦП-фильтр (0-15). */
    private int filterADC;
    /** Время выключения весов. */
    private int timeOff;
    /** Скорость порта. */
    private int speedPort;
    private String spreadsheet;
    private String username;
    private String password;
    private String phone;
    /** Калибровочный коэффициент a. */
    private float coefficientA;
    /** Калибровочный коэффициент b. */
    private float coefficientB;
    /** Текущее показание датчика веса. */
    private int sensorTenzo;
    /** Максимальное показание датчика. */
    private int limitTenzo;
    /** Предельный вес взвешивания. */
    private int weightMargin;
    /** Номер пломбы*/
    private int seal;
    /** Количество стабильных показаний веса для авто сохранения. */
    public static final int STABLE_NUM_MAX = 15;
    /** Флаг использования авто обнуленияю. */
    private boolean enableAutoNull = true;
    /** Флаг обнаружения стабильного веса. */
    private boolean enableProcessStable = true;

    public int getStepScale() {return stepScale; }

    public void setStepScale(int stepScale) {
        if (stepScale == 0)
            return;
        this.stepScale = stepScale;
    }

    /** Шаг дискреты. */
    private int stepScale = 1;

    /** Получить таблицу google disk.
     * @return Имя таблици.*/
    public String getSpreadsheet() {
        return spreadsheet;
    }

    public void setSpreadsheet(String spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /** Получить акаунт google.
     * @return Имя аккаунта.*/
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
    private ScaleModule(Context context, String moduleVersion, BluetoothDevice device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context, device, event);
        versionName = moduleVersion;
        attach();
    }

    private ScaleModule(Context context, String moduleVersion, String device, InterfaceCallbackScales event) throws Exception, ErrorDeviceException {
        super(context, device, event);
        versionName = moduleVersion;
        attach();
    }

    private ScaleModule(Context context, String moduleVersion, WifiManager wifiManager, InterfaceCallbackScales event)throws Exception, ErrorDeviceException {
        super(context, wifiManager, event);
        versionName = moduleVersion;
        attachWiFi();
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

    public static ScaleModule getInstance() { return instance; }

    /** Соединится с модулем. */
    @Override
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
            version.load();
            resultCallback.onCallback(instance);
        }  catch (ErrorTerminalException e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_TERMINAL_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        } catch (Exception e) {
            getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_MODULE_ERROR)/*.putExtra(InterfaceModule.EXTRA_MODULE, new ObjectScales())*/);
        }
    }

    @Override
    protected void attachWiFi() throws InterruptedException {
        //new RunnableScaleAttachWiFi();
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
                numVersion = Integer.valueOf(s);
                //setVersion(fetchVersion(numVersion));
                version = fetchVersion(numVersion);
            } catch (Exception e) {
                throw new Exception(e);
            }
            /** Если версия правильная создаем обьек и посылаем сообщения. */
            objectScales = new ObjectScales();
            return true;
        }
        throw new Exception("Это не весы или неправильная версия!!!");
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

    @Override
    protected void connectWiFi() throws IOException {
        WifiInfo info = wifiManager.getConnectionInfo();
        int ip = info.getIpAddress();

        try {
            //Socket socket = new Socket("192.168.4.1", 1001);
            Socket socket = new Socket();
            InetSocketAddress socketAddress = new InetSocketAddress("192.168.4.1", 1001);
            socket.connect(socketAddress, 10000);
            DataOutputStream DataOut = new DataOutputStream(socket.getOutputStream());
            DataOut.writeBytes("Test");
            DataOut.flush();

            socket.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /** Определяем версию весов.
     * @param version Имя версии.
     * @return Экземпляр версии.
     * @throws  Exception Ошибка неправильная версия весов.
     */
    private ScaleVersion fetchVersion(int version) throws Exception {
        switch (version) {
            case 1:
                return new ScaleVersion1(this);
            case 4:
                return new ScaleVersion4(this);
            default:
                throw new Exception("illegal version");
        }
    }

    /** Получаем класс загруженой версии весового модуля.
     * @return класс версии весового модуля.
     */
    public ScaleVersion getVersion() {
        return version;
    }

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

    /** Получить номер версии программы.
     * @return Номер версии.  */
    public int getNumVersion() { return numVersion; }

    /** Получить имя акаунта google.
     * @return Имя акаунта. */
    public String getUserName() {
        return username;
    }

    /** Получить максиматьное значение датчика.
     * @return Значение датчика. */
    public int getLimitTenzo(){ return limitTenzo; }

    /** Установить максимальное значение датчика.
     * @param limitTenzo Значение датчика.     */
    public void setLimitTenzo(int limitTenzo) {
        this.limitTenzo = limitTenzo;
    }

    /** Получить температуру модуля.
     * @return Значение температуры. */
    public int getTemperature() {return temperature; }

    /** Получить сохраненое значение фильтраАЦП.
     * @return Значение фильтра от 1 до 15.   */
    public int getFilterADC() {
        return filterADC;
    }

    /** Установить значение фильтра АЦП.
     * @param filterADC Значение АЦП.*/
    public void setFilterADC(int filterADC) {
        this.filterADC = filterADC;
    }

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

    /** Пароль акаунта google.*/ /** Получить пароль акаута google.
     * @return Пароль.   */
    public String getPassword() {
        return password;
    }

    /** Номер телефона. */ /** Получить номер телефона.
     * @return Номер телефона.   */
    public String getPhone() {
        return phone;
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

    public String getSpreadSheet() {
        return spreadsheet;
    }

    public void setSensorTenzo(int sensorTenzo) {
        this.sensorTenzo = sensorTenzo;
    }

    public int getSensorTenzo() {
        return sensorTenzo;
    }

    public int getWeightMargin() {
        return weightMargin;
    }

    public int getSeal(){return seal;}

    public void setSeal(int seal){this.seal = seal;}

    public void setWeightMargin(int weightMargin) {
        this.weightMargin = weightMargin;
    }

    public int getSpeedPort() {
        return speedPort;
    }

    public void setSpeedPort(int speedPort) {
        this.speedPort = speedPort;
    }

    //==================================================================================================================

    /** Установливаем сервис код.
     *
     * @param cod Код.
     * @return true Значение установлено.
     * @see Commands#SRC
     */
    public boolean setModuleServiceCod(String cod) {
        return Commands.SRC.setParam(cod);
    }

    /** Получаем сервис код.
     * @return код
     * @see Commands#SRC
     */
    public String getModuleServiceCod() {
        return Commands.SRC.getParam();
        //return cmd(InterfaceVersions.CMD_SERVICE_COD);
    }

    public int getMarginTenzo() {
        return version.getMarginTenzo(); }

    public int getWeightMax(){
        return version.getWeightMax(); }

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

    /** Получить значение датчика веса.
     * @return Значение датчика.
     * @see Commands#DCH
     */
    public String feelWeightSensor() {
        return Commands.DCH.getParam();
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

    /** Установить обнуление.
     * @return true - Обнуление установлено.
     */
    public synchronized boolean setOffsetScale() {
        return version.setOffsetScale();
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

    /** Устанавливаем имя аккаунта google в модуле.
     * @param username Имя аккаунта.
     * @return true - Имя записано успешно.
     */
    public boolean setModuleUserName(String username) {
        if (Commands.UGD.setParam(username)){
            this.username = username;
            return true;
        }
        return false;
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

    /** Выключить питание модуля.
     * @return true - питание модкля выключено.
     */
    public boolean powerOff() {
        return Commands.POF.getParam().equals(Commands.POF.getName());
    }

    public void setWeightMax(int weightMax) {
        version.setWeightMax(weightMax);
    }

    public void setEnableAutoNull(boolean enableAutoNull) {this.enableAutoNull = enableAutoNull;}

    public void setEnableProcessStable(boolean stable) {
        enableProcessStable = stable;
    }

    public boolean writeData() {
        return version.writeData();
    }

    public void resetAutoNull(){ autoNull = 0; }

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
                    /** Секция вес. */
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
                    /** Секция авто ноль. */
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
                    /** Секция определения стабильного веса. */
                    if (enableProcessStable){
                        if (tempWeight - getStepScale() <= objectScales.getWeight() && tempWeight + getStepScale() >= objectScales.getWeight()) {
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
                    /** Секция батарея температура. */
                    if (numTimeTemp == 0){
                        numTimeTemp = 250;
                        objectScales.setBattery(getModuleBatteryCharge());
                        objectScales.setTemperature(getModuleTemperature());

                    }
                    getContext().sendBroadcast(new Intent(InterfaceModule.ACTION_SCALES_RESULT).putExtra(InterfaceModule.EXTRA_SCALES, objectScales));
                }catch (Exception e){}
                numTimeTemp--;
                //try { TimeUnit.MILLISECONDS.sleep(PERIOD_UPDATE); } catch (InterruptedException e) {}
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

    /** Включаем выключаем процесс определения стабильного веса.
     * @param stable true - процесс запускается, false - процесс останавливается.
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

    /**
     * Процесс обработки стабильного показания веса.
     * Если вес стабильный определенное время посылаем сообщение.
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

    void managerConnect(Socket socket){
        if (bluetoothProcessManager != null)
            bluetoothProcessManager.connect();
    }

}
