/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.konst.scaleslibrary.module.scale;

import com.konst.scaleslibrary.module.Module;

/**
 * @author Kostya
 */
public abstract class ScaleVersion {
    protected Module scaleModule;
    /** Показание датчика веса с учетом offset.  */
    protected int sensorTenzoOffset;
    /** Текущий вес.  */
    protected int weight;
    /** Максимальный вес для весов. */
    protected int weightMax;
    /** максимальное значение фильтра ацп. */
    protected static final int MAX_ADC_FILTER = 15;
    /** Значение фильтра ацп по умоляанию. */
    protected static final int DEFAULT_ADC_FILTER = 8;
    /** Максимальное время бездействия весов в минутах. */
    protected static final int MAX_TIME_OFF = 60;
    /** Минимальное время бездействия весов в минутах.  */
    protected static final int MIN_TIME_OFF = 10;


    /**Получить сохраненные параметры из модуля.
     * @throws Exception Ошибка загрузки параметров.
     */
    public abstract void extract() throws Exception;
    /** Обновить значения веса.
     * Получаем показания сенсора и переводим в занчение веса.
     * @return Значение веса.
     */
    public abstract int updateWeight();
    public abstract boolean writeData();
    //abstract int getWeight();
    public abstract int getSensor();
    public abstract int getMarginTenzo();
    //abstract int getWeightMax();
    //abstract void setWeightMax(int weightMax);
    public abstract boolean isLimit();
    public abstract boolean isMargin();
    public abstract boolean setOffsetScale();

    public void setSensorTenzoOffset(int sensorTenzoOffset) {this.sensorTenzoOffset = sensorTenzoOffset;}
    public int getWeight() { return weight; }
    public int getWeightMax() { return weightMax; }
    public void setWeightMax(int weightMax) { this.weightMax = weightMax;}


}
