package com.konst.scaleslibrary.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.Toast;
import com.konst.scaleslibrary.R;
import com.konst.scaleslibrary.ScalesView;
import com.konst.scaleslibrary.Settings;
import com.konst.scaleslibrary.module.scale.ScaleModule;

import java.util.List;

public class FragmentSettings extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private static Settings settings;
    public static ScalesView scalesView;

    @Override
    public boolean onPreferenceClick(Preference preference) {

        return false;
    }

    enum EnumSettings{
        TIMER_NULL(R.string.KEY_TIMER_ZERO){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                name.setTitle(context.getString(R.string.Time) + ' ' + name.getSharedPreferences().getInt(name.getKey(), 120) + ' ' + context.getString(R.string.second));
                name.setSummary(context.getString(R.string.sum_time_auto_zero) + ' ' + context.getResources().getInteger(R.integer.default_max_time_auto_null) + ' ' + context.getString(R.string.second));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || "0".equals(o.toString()) || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_max_time_auto_null)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        //scalesView.getScaleModule().setTimerNull(Integer.valueOf(o.toString()));
                        preference.setTitle(context.getString(R.string.Time) + ' ' + o + ' ' + context.getString(R.string.second));
                        settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                        //preference.getEditor().putInt(preference.getKey(), scaleModule.getTimerNull());
                        Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o + ' ' + context.getString(R.string.second), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        },
        MAX_NULL(R.string.KEY_MAX_ZERO){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                name.setTitle(context.getString(R.string.sum_weight) + ' ' + name.getSharedPreferences().getInt(name.getKey(), 50) + ' ' + context.getString(R.string.scales_kg));
                name.setSummary(context.getString(R.string.sum_max_null) + ' ' + context.getResources().getInteger(R.integer.default_limit_auto_null) + ' ' + context.getString(R.string.scales_kg));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || "0".equals(o.toString()) || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_limit_auto_null)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        //scalesView.getScaleModule().setWeightError(Integer.valueOf(o.toString()));
                        preference.setTitle(context.getString(R.string.sum_weight) + ' ' + o + ' ' + context.getString(R.string.scales_kg));
                        settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                        //preference.getEditor().putInt(preference.getKey(), scaleModule.getWeightError());
                        Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o + ' ' + context.getString(R.string.scales_kg), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        },
        TIMER(R.string.KEY_TIMER){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                int t = name.getSharedPreferences().getInt(name.getKey(), 10);
                name.setDefaultValue(t);
                name.setTitle(context.getString(R.string.Timer_off) + ' ' + t + ' ' + context.getString(R.string.minute));
                name.setSummary(context.getString(R.string.sum_timer) + ' ' + context.getString(R.string.range) + context.getResources().getInteger(R.integer.default_min_time_off) + context.getString(R.string.to) + context.getResources().getInteger(R.integer.default_max_time_off));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || "0".equals(o.toString())
                                || Integer.valueOf(o.toString()) < context.getResources().getInteger(R.integer.default_min_time_off)
                                || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_max_time_off)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        /*try {
                            if (scalesView.getScaleModule().setModuleTimeOff(Integer.valueOf(o.toString()))) {
                                scalesView.getScaleModule().setTimeOff(Integer.valueOf(o.toString()));
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }*/
                        preference.setTitle(context.getString(R.string.Timer_off) + ' ' + o + ' ' + context.getString(R.string.minute));
                        settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                        Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o + ' ' + context.getString(R.string.minute), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        },
        STEP(R.string.KEY_DISCRETE){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                name.setTitle(context.getString(R.string.measuring_step) + ' ' + name.getSharedPreferences().getInt(name.getKey(), 5) + ' ' + context.getString(R.string.scales_kg));
                name.setSummary(context.getString(R.string.The_range_is_from_1_to) + context.getResources().getInteger(R.integer.default_max_step_scale) + ' ' + context.getString(R.string.scales_kg));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || "0".equals(o.toString()) || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_max_step_scale)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        //scalesView.setDiscrete(Integer.valueOf(o.toString()));
                        preference.setTitle(context.getString(R.string.measuring_step) + ' ' + o + ' ' + context.getString(R.string.scales_kg));
                        settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                        Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o + ' ' + context.getString(R.string.scales_kg), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        },
        FILTER(R.string.KEY_FILTER){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                int f = name.getSharedPreferences().getInt(name.getKey(), 15);
                name.setDefaultValue(Integer.valueOf(f));
                name.setTitle(context.getString(R.string.filter_adc) + ' ' + String.valueOf(f));
                name.setSummary(context.getString(R.string.sum_filter_adc) + ' ' + context.getString(R.string.The_range_is_from_0_to) + context.getResources().getInteger(R.integer.default_adc_filter));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_adc_filter)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        /*try {
                            if (scaleModule.setModuleFilterADC(Integer.valueOf(o.toString()))) {
                                scaleModule.setFilterADC(Integer.valueOf(o.toString()));

                            }
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        }*/
                        settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                        preference.setTitle(context.getString(R.string.filter_adc) + ' ' + o);
                        Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o.toString(), Toast.LENGTH_SHORT).show();
                        return true;
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.fragment_settings, false);
        settings = new Settings(getActivity(), Settings.SETTINGS);
        scalesView = ScalesView.getInstance();
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

