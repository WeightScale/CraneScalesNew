package com.konst.scaleslibrary.module;

/**
 * Интерфей для комманд.
 * @author Kostya. */
public interface InterfaceModule {
    String ACTION_ATTACH_START = "com.konst.module.ACTION_ATTACH_START";
    String ACTION_ATTACH_FINISH = "com.konst.module.ACTION_ATTACH_FINISH";
    String ACTION_CONNECT_ERROR = "com.konst.module.ACTION_CONNECT_ERROR";
    String ACTION_LOAD_OK = "com.konst.module.ACTION_LOAD_OK";
    String ACTION_RECONNECT_OK = "com.konst.module.ACTION_RECONNECT_OK";
    String ACTION_MODULE_ERROR = "com.konst.module.ACTION_MODULE_ERROR";
    String ACTION_TERMINAL_ERROR = "com.konst.module.ACTION_TERMINAL_ERROR";
    String ACTION_SCALES_RESULT = "com.konst.module.ACTION_SCALES_RESULT";
    String ACTION_WEIGHT_STABLE = "com.konst.module.ACTION_WEIGHT_STABLE";
    String ACTION_BOOT_MODULE = "com.konst.module.ACTION_BOOT_MODULE";
    String EXTRA_DEVICE_NAME = "com.konst.module.EXTRA_DEVICE_NAME";
    String EXTRA_MESSAGE = "com.konst.module.EXTRA_MESSAGE";
    String EXTRA_MODULE = "com.konst.module.EXTRA_MODULE";
    String EXTRA_SCALES = "com.konst.module.EXTRA_SCALES";

    /** Коэфициэнт А.
     * Расчитывается при каллибровки весов. Используется для расчета веса.
     * Используется как параметр комманды {@link Commands#DAT}
     * @see Commands#DAT  */
    String CMD_DATA_CFA = "cfa";
    /** Коэфициэнт Б.
     * Используется как параметр комманды {@link Commands#DAT}
     *
     * @see Commands#DAT
     */
    String CMD_DATA_CFB = "cfb";
    /** Значение максимального веса для тензодатчика.
     * Используется как параметр комманды {@link Commands#DAT}
     *
     * @see Commands#DAT
     */
    String CMD_DATA_WGM = "wgm";
    /** Значение максимального значения для тензодатчика.
     * Используется как параметр комманды {@link Commands#DAT}
     *
     * @see Commands#DAT
     */
    String CMD_DATA_LMT = "lmt";
    /** Значение номера пломбы при калибровке.
     * Используется как параметр комманды {@link Commands#DAT}
     * @see Commands#DAT
     */
    String CMD_DATA_SEL = "sel";

    //String command(Commands commands);
    void write(String command);
    ObjectCommand sendCommand(Commands commands);
}
