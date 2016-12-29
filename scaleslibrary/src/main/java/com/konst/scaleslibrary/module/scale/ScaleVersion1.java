/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.konst.scaleslibrary.module.scale;

import com.konst.scaleslibrary.module.Commands;
import com.konst.scaleslibrary.module.ErrorModuleException;
import com.konst.scaleslibrary.module.Module;

/**
 * @author Kostya
 */
public class ScaleVersion1 extends ScaleVersion {

    public ScaleVersion1(Module module){
        scaleModule = module;
    }

    @Override
    public void extract() throws Exception {
        scaleModule.setFilterADC(Integer.valueOf(Commands.FAD.getParam()));
        if (scaleModule.getFilterADC() < 0 || scaleModule.getFilterADC() > MAX_ADC_FILTER) {
            if (Commands.FAD.setParam(DEFAULT_ADC_FILTER)){
                scaleModule.setFilterADC(DEFAULT_ADC_FILTER);
            }else
                throw new ErrorModuleException("Фильтер АЦП не установлен в настройках");
        }
        //==============================================================================================================
        scaleModule.setTimeOff(Integer.valueOf(Commands.TOF.getParam()));
        if (scaleModule.getTimeOff() < MIN_TIME_OFF || scaleModule.getTimeOff() > MAX_TIME_OFF) {
            if (Commands.TOF.setParam(MIN_TIME_OFF)){
                scaleModule.setTimeOff(MIN_TIME_OFF);
            }else
                throw new ErrorModuleException("Таймер выключения не установлен в настройках");
        }
        //==============================================================================================================
        int speed = Integer.valueOf(Commands.BST.getParam());
        if (speed < 1 || speed > 5) {
            if (!Commands.BST.setParam(5))
                throw new ErrorModuleException("Скорость передачи не установлена в настройках");
        }
        //==============================================================================================================
        parserData(Commands.DAT.getParam());

        scaleModule.setWeightMargin((int) (weightMax * 1.2));
    }

    @Override
    public boolean isLimit() {
        return Math.abs(weight) > weightMax;
    }

    @Override
    public boolean isMargin() {
        return Math.abs(weight) < scaleModule.getWeightMargin();
    }

    @Override
    public boolean setOffsetScale() {
        try {
            scaleModule.setCoefficientB(-scaleModule.getCoefficientA() * Integer.valueOf(Commands.DCH.getParam()));
            //coefficientB = -scaleModule.getCoefficientA() * Integer.valueOf(Commands.CMD_SENSOR.getParam());
        } catch (Exception e) {
            return false;
        }
        return true;
        //return Commands.CMD_SET_OFFSET.getParam().equals(Commands.CMD_SET_OFFSET.getName());
    }

    @Override
    public synchronized int updateWeight() {
        try {
            scaleModule.setSensorTenzo(Integer.valueOf(Commands.DCH.getParam()));
            //return weight = (int) (coefficientA/sensorTenzo  + coefficientB);
            return weight = (int) (scaleModule.getSensorTenzo() * scaleModule.getCoefficientA()   + scaleModule.getCoefficientB());
        } catch (Exception e) {
            scaleModule.setSensorTenzo(Integer.MIN_VALUE);
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public boolean writeData() {
        return Commands.DAT.setParam("S" + scaleModule.getCoefficientA() + ' ' + scaleModule.getCoefficientB() + ' ' + weightMax);
    }

    @Override
    public int getSensor() {
        return scaleModule.getSensorTenzo();
    }

    @Override
    public int getMarginTenzo() { return 0; }

    protected void parserData(String d) throws Exception {
        StringBuilder dataBuffer = new StringBuilder(d);
        //synchronized (this) {
        dataBuffer.deleteCharAt(0);
        String str = dataBuffer.substring(0, dataBuffer.indexOf(" "));
        scaleModule.setCoefficientA(Float.valueOf(str));
        if (scaleModule.getCoefficientA() == 0.0f)
            throw new ErrorModuleException("Коэффициент А=" + scaleModule.getCoefficientA());
        dataBuffer.delete(0, dataBuffer.indexOf(" ") + 1);
        str = dataBuffer.substring(0, dataBuffer.indexOf(" "));
        scaleModule.setCoefficientB(Float.valueOf(str));
        dataBuffer.delete(0, dataBuffer.indexOf(" ") + 1);
        weightMax = Integer.valueOf(dataBuffer.toString());
        if (weightMax <= 0)
            throw new ErrorModuleException("Предельный вес =" + weightMax);
        if (weightMax <= 0) {
            weightMax = 1000;
        }
        //}
    }

}
