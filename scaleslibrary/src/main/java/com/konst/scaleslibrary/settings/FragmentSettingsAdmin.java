package com.konst.scaleslibrary.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;
import com.konst.scaleslibrary.R;
import com.konst.scaleslibrary.ScalesView;
import com.konst.scaleslibrary.module.scale.ScaleModule;

/**
 * @author Kostya 26.10.2016.
 */
public class FragmentSettingsAdmin extends PreferenceFragment {
    public static ScaleModule scaleModule;
    private static final Point point1 = new Point(Integer.MIN_VALUE, 0);
    private static final Point point2 = new Point(Integer.MIN_VALUE, 0);

    enum EnumSettings{
        POINT1(R.string.KEY_POINT1){
            Context context;
            @Override
            void setup(final Preference name) throws Exception {
                context = name.getContext();
                if(!scaleModule.isAttach())
                    throw new Exception(" ");
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return startDialog(name);
                        /*try {
                            String str = scaleModule.feelWeightSensor();
                            scaleModule.setSensorTenzo(Integer.valueOf(str));
                            point1.x = Integer.valueOf(str);
                            point1.y = 0;
                            Toast.makeText(name.getContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                            flag_restore = true;
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(name.getContext(), R.string.preferences_no + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return false;
                        }*/
                    }
                });
            }

            boolean startDialog(final Preference name){
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Установка ноль");
                dialog.setCancelable(false);
                dialog.setPositiveButton(context.getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            String sensor = scaleModule.feelWeightSensor();
                            //scaleModule.setSensorTenzo(Integer.valueOf(str));
                            point1.x = Integer.valueOf(sensor);
                            point1.y = 0;
                            Toast.makeText(name.getContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                            //flag_restore = true;
                        } catch (Exception e) {
                            Toast.makeText(name.getContext(), R.string.error + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.setNegativeButton(context.getString(R.string.Close), new DialogInterface.OnClickListener() {
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
                            //scaleModule.setSensorTenzo(Integer.valueOf(str));
                            point2.x = Integer.valueOf(sensor);
                            point2.y = Integer.valueOf(o.toString());
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
        WEIGHT_MAX(R.string.KEY_WEIGHT_MAX){
            @Override
            void setup(Preference name) throws Exception {
                final Context context = name.getContext();
                name.setTitle(context.getString(R.string.Max_weight) + scaleModule.getWeightMax() + context.getString(R.string.scales_kg));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || Integer.valueOf(o.toString()) < context.getResources().getInteger(R.integer.default_max_weight)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        scaleModule.setWeightMax(Integer.valueOf(o.toString()));
                        scaleModule.setWeightMargin((int) (scaleModule.getWeightMax() * 1.2));
                        preference.setTitle(context.getString(R.string.Max_weight) + scaleModule.getWeightMax() + context.getString(R.string.scales_kg));
                        Toast.makeText(context, R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        //flag_restore = true;
                        return true;
                    }
                });
            }
        },
        SERVICE_COD(R.string.KEY_SERVICE_COD){
            @Override
            void setup(final Preference name) throws Exception {
                if(!scaleModule.isAttach())
                    throw new Exception(" ");
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue.toString().length() > 32 || newValue.toString().length() < 4) {
                            Toast.makeText(name.getContext(), "Длина кода больше 32 или меньше 4 знаков", Toast.LENGTH_LONG).show();
                            return false;
                        }

                        try {
                            scaleModule.setModuleServiceCod(newValue.toString());
                            Toast.makeText(name.getContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(name.getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                });
            }
        },
        CLOSED(R.string.KEY_CLOSED){
            @Override
            void setup(Preference name) throws Exception {
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ((Activity)preference.getContext()).onBackPressed();
                        return false;
                    }
                });
            }
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
        addPreferencesFromResource(R.xml.fragment_settings_admin);
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
}
