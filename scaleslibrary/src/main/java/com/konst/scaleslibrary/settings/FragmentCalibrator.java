package com.konst.scaleslibrary.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;
import com.konst.scaleslibrary.R;
import com.konst.scaleslibrary.ScalesView;
import com.konst.scaleslibrary.module.Module;

import java.util.Date;
import java.util.Random;

/**
 * @author Kostya  06.11.2016.
 */
public class FragmentCalibrator extends PreferenceFragment {
    public static Module scaleModule;
    private static Calibrator calibrator = null;

    enum EnumSettings{
        WEIGHT_MAX(R.string.KEY_WEIGHT_MAX){
            @Override
            void setup(final Preference name) throws Exception {
                final Context context = name.getContext();
                name.setTitle(context.getString(R.string.Max_weight) + scaleModule.getWeightMax() + context.getString(R.string.scales_kg));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || Integer.valueOf(o.toString()) < context.getResources().getInteger(R.integer.default_max_weight)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        calibrator = new Calibrator(context, Integer.valueOf(o.toString()));
                        //scaleModule.setWeightMax(Integer.valueOf(o.toString()));
                        //scaleModule.setWeightMargin((int) (scaleModule.getWeightMax() * 1.2));
                        name.getPreferenceManager().findPreference(name.getContext().getString(R.string.KEY_POINT1)).setEnabled(true);
                        name.setEnabled(false);
                        preference.setTitle(context.getString(R.string.Max_weight) + scaleModule.getWeightMax() + context.getString(R.string.scales_kg));
                        Toast.makeText(context, R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        //flag_restore = true;
                        return true;
                    }
                });
            }
        },
        POINT1(R.string.KEY_POINT1){
            @Override
            void setup(Preference name) throws Exception {
                if(!scaleModule.isAttach())
                    throw new Exception(" ");
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return startDialog(preference);
                    }
                });
            }

            boolean startDialog(final Preference name){
                Context context = name.getContext();
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Установка ноль");
                dialog.setCancelable(false);
                dialog.setPositiveButton(context.getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            calibrator.setPoint1(Integer.valueOf(scaleModule.feelWeightSensor()), 0);
                            /*String sensor = scaleModule.feelWeightSensor();
                            point1.x = Integer.valueOf(sensor);
                            point1.y = 0;*/
                            Toast.makeText(name.getContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                            Preference preference = name.getPreferenceManager().findPreference(name.getContext().getString(R.string.KEY_POINT2));
                            preference.setEnabled(true);
                            name.setEnabled(false);
                            //flag_restore = true;
                        } catch (Exception e) {
                            Toast.makeText(name.getContext(), R.string.error + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.setNegativeButton(context.getString(R.string.NO), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setMessage("Вы действительно хотите установить ноль калибровки.");
                dialog.show();
                return true;
            }
        },
        POINT2(R.string.KEY_POINT2){
            @Override
            void setup(final Preference name) throws Exception {
                if(!scaleModule.isAttach())
                    throw new Exception(" ");
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        try {
                            String sensor = scaleModule.feelWeightSensor();
                            if (sensor.isEmpty()) {
                                Toast.makeText(name.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            calibrator.setPoint2(Integer.valueOf(sensor), Integer.valueOf(o.toString()));
                            /*point2.x = Integer.valueOf(sensor);
                            point2.y = Integer.valueOf(o.toString());*/
                            name.getPreferenceManager().findPreference(name.getContext().getString(R.string.KEY_SEAL)).setEnabled(true);
                            name.setEnabled(false);
                            Toast.makeText(name.getContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                            //flag_restore = true;
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(name.getContext(), R.string.error + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                });
            }
        },
        COEFFICIENT_A(R.string.KEY_COEFFICIENT_A){
            @Override
            void setup(Preference name) throws Exception {
                final Context context = name.getContext();
                name.setTitle(context.getString(R.string.ConstantA) + Float.toString(scaleModule.getCoefficientA()));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        try {
                            scaleModule.setCoefficientA(Float.valueOf(o.toString()));
                            preference.setTitle(context.getString(R.string.ConstantA) + Float.toString(scaleModule.getCoefficientA()));
                            Toast.makeText(context, R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                });
            }
        },
        SEALING(R.string.KEY_SEAL){
            @Override
            void setup(Preference name) throws Exception {
                final Context context = name.getContext();
                ((SealingLayout)name).setOnClickButton(new SealingLayout.OnClickButton() {
                    @Override
                    public void onButtonYes() {
                        calibrator.doSealing();
                        //setSealing();
                        ((Activity)context).onBackPressed();
                    }

                    @Override
                    public void onButtonNo() {
                        ((Activity)context).onBackPressed();
                    }
                });
            }

            /*void setSealing(){
                if (point1.x != Integer.MIN_VALUE && point2.x != Integer.MIN_VALUE) {
                    scaleModule.setCoefficientA((float) (point1.y - point2.y) / (point1.x - point2.x));
                    scaleModule.setCoefficientB(point1.y - scaleModule.getCoefficientA() * point1.x);
                }
                scaleModule.setLimitTenzo((int) (scaleModule.getWeightMax() / scaleModule.getCoefficientA()));
                if (scaleModule.getLimitTenzo() > 0xffffff) {
                    scaleModule.setLimitTenzo(0xffffff);
                    scaleModule.setWeightMax((int) (0xffffff * scaleModule.getCoefficientA()));
                }
                if (scaleModule.writeData()) {
                    Toast.makeText(context, R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }*/
        };

        private final int resId;
        abstract void setup(Preference name)throws Exception;

        EnumSettings(int key){
            resId = key;
        }
        public int getResId() { return resId; }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_calibrator);
        scaleModule = ScalesView.getInstance().getScaleModule();
        initPreferences();
    }

    public void initPreferences(){
        for (EnumSettings enumPreference : EnumSettings.values()){
            Preference preference = findPreference(getString(enumPreference.getResId()));
            if(preference != null){
                try {
                    enumPreference.setup(preference);
                } catch (Exception e) {
                    preference.setEnabled(false);
                }
            }
        }
    }

    static class Calibrator {
        final Context context;
        private final Point point1 = new Point(Integer.MIN_VALUE, 0);
        private final Point point2 = new Point(Integer.MIN_VALUE, 0);
        private int weightMaX = 0;
        private int weightMargin = 0;

        Calibrator(Context context, int weightMaX){
            this.context = context;
            this.weightMaX = weightMaX;
            weightMargin = (int)(weightMaX * 1.2);
        }

        void setPoint1(int x, int y){
            point1.x = x;
            point1.y = y;
        }

        void setPoint2(int x, int y){
            point2.x = x;
            point2.y = y;
        }

        void doSealing(){
            scaleModule.setSeal(new Random(new Date().getTime()).nextInt(10000));
            if (point1.x != Integer.MIN_VALUE && point2.x != Integer.MIN_VALUE) {
                scaleModule.setCoefficientA((float) (point1.y - point2.y) / (point1.x - point2.x));
                scaleModule.setCoefficientB(point1.y - scaleModule.getCoefficientA() * point1.x);
            }
            scaleModule.setWeightMax(weightMaX);
            scaleModule.setWeightMargin(weightMargin);
            scaleModule.setLimitTenzo((int) (weightMaX / scaleModule.getCoefficientA()));
            if (scaleModule.getLimitTenzo() > 0xffffff) {
                scaleModule.setLimitTenzo(0xffffff);
                scaleModule.setWeightMax((int) (0xffffff * scaleModule.getCoefficientA()));
            }
            if (scaleModule.writeData()) {
                Toast.makeText(context, R.string.preferences_yes, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
