package com.konst.scaleslibrary.module;

/**
 * @author Kostya
 */
public enum Commands {
    /** Старт программирования. */
    STR("STR", 300),
    /** Получить код микроконтроллера. */
    PRC("PRC", 300),
    /** получить версию весов. */
    VRS("VRS", 1000),
    /** получить/установить АЦП-фильтр. */
    FAD("FAD", 1000),
    /** получить/установить таймер выключения весов. */
    TOF("TOF", 500),
    /** получить/установить скорость передачи данных. */
    BST("BST", 7000),
    /** получить offset. */
    GCO("GCO", 1000),
    /** установить offset. */
    SCO("SCO", 2000),
    /** получить передать заряд батареи. */
    GBT("GBT", 1000),
    /** считать/записать данные температуры. */
    DTM("DTM", 1000),
    /** получить версию hardware. */
    HRW("HRW", 300),
    /** установить имя весов. */
    SNA("SNA", 7000),
    /** каллибровать процент батареи. */
    CBT("CBT", 400),
    /** Считать/записать данные весов. */
    DAT("DAT", 1000),
    /** получить показание датчика веса. */
    DCH("DCH", 1000),
    /** считать/записать имя таблици созданой в google disc. */
    SGD("SGD", 300),
    /** считать/записать account google disc. */
    UGD("UGD", 300),
    /** считать/записать password google disc. */
    PGD("PGD", 300),
    /** считать/записать phone for sms boss. */
    PHN("PHN", 300),
    /** получить показание датчика веса минус офсет. */
    DCO("DCO", 1000),
    /** получить показание датчика температуры аккумулятор. */
    SDO("SDO", 1000),
    /** включаем выключаем процесс измерения. */
    SPE("SPE", 1000),
    /** Выключить питание модуля. */
    POF("POF", 300),
    /** Установка мощности модуля. */
    MTP("MTP", 7000),
    /** Значение сервис кода. */
    SRC("SRC", 300);

    private final String name;
    private final int time;
    private String cmd;
    public String value;
    public boolean isReceived;
    private static InterfaceModule interfaceModule;

    Commands(String n, int t){
        name = n;
        time = t;
    }

    public String toString() { return cmd; }

    /** Получит время timeout комманды.
     * @return Время в милисекундах.  */
    public int getTimeOut(){ return time;}

    /** Получить имя комманды.
     * @return Имя комманды.  */
    public String getName(){return name;}

    /** Выполнить комманду получить данные с обратным вызовом.
     * @return Данные выполненой комманды. */
    public String getParam(){
        cmd = name;
        ObjectCommand obj = interfaceModule.sendCommand(this);
        return obj != null? obj.getValue():"";
    }

    /** Выполнить комманду установить данные.
     * @param param Данные для установки.
     * @return true - комманда выполнена.  */
    public boolean setParam(String param){
        cmd = name + param;
        return interfaceModule.sendCommand(this).getCommand() == this;
    }

    /** Выполнить комманду установить данные.
     * @param param Данные для установки.
     * @return true - комманда выполнена.  */
    public boolean setParam(int param){
        cmd = name + param;
        return interfaceModule.sendCommand(this).getCommand() == this;
    }

    public static void setInterfaceCommand(InterfaceModule i){
        interfaceModule = i;
    }
}
