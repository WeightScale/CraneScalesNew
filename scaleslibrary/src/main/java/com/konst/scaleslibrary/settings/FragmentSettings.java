package com.konst.scaleslibrary.settings;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.konst.scaleslibrary.*;

public class FragmentSettings extends PreferenceFragment{
    private static Settings settings;
    public static ScalesView scalesView;

    public enum KEY{
        SWITCH_ZERO(R.string.KEY_SWITCH_ZERO){
            @Override
            void setup(Preference name) throws Exception {
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        boolean flag_switch = (boolean)o;

                        settings.write(preference.getKey(), flag_switch);
                        preference.getPreferenceManager().findPreference(preference.getContext().getString(R.string.KEY_TIMER_ZERO)).setEnabled(flag_switch);
                        preference.getPreferenceManager().findPreference(preference.getContext().getString(R.string.KEY_MAX_ZERO)).setEnabled(flag_switch);
                        return true;
                    }
                });
            }
        },
        TIMER_ZERO(R.string.KEY_TIMER_ZERO){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                boolean check = name.getSharedPreferences().getBoolean(name.getContext().getString(R.string.KEY_SWITCH_ZERO), false);
                name.setEnabled(check);
                name.setTitle(title + " " + name.getSharedPreferences().getInt(name.getKey(), 120) + ' ' + context.getString(R.string.second));
                name.setSummary(context.getString(R.string.sum_time_auto_zero) + ' ' + context.getResources().getInteger(R.integer.default_max_time_auto_null) + ' ' + context.getString(R.string.second));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || "0".equals(o.toString()) || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_max_time_auto_null)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        preference.setTitle(title + " " + o + ' ' + context.getString(R.string.second));
                        settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                        Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o + ' ' + context.getString(R.string.second), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        },
        MAX_ZERO(R.string.KEY_MAX_ZERO){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                boolean check = name.getSharedPreferences().getBoolean(name.getContext().getString(R.string.KEY_SWITCH_ZERO), false);
                name.setEnabled(check);
                name.setTitle(title + " " + name.getSharedPreferences().getInt(name.getKey(), 50) + ' ' + context.getString(R.string.scales_kg));
                name.setSummary(context.getString(R.string.sum_max_null) + ' ' + context.getResources().getInteger(R.integer.default_limit_auto_null) + ' ' + context.getString(R.string.scales_kg));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || "0".equals(o.toString()) || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_limit_auto_null)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        preference.setTitle(title + " " + o + ' ' + context.getString(R.string.scales_kg));
                        settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                        Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o + ' ' + context.getString(R.string.scales_kg), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        },
        SWITCH_STABLE(R.string.KEY_SWITCH_STABLE){
            @Override
            void setup(Preference name) throws Exception {
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        boolean flag_switch = (boolean)o;

                        settings.write(preference.getKey(), flag_switch);
                        preference.getPreferenceManager().findPreference(preference.getContext().getString(R.string.KEY_DELTA_STAB)).setEnabled(flag_switch);
                        //preference.getPreferenceManager().findPreference(preference.getContext().getString(R.string.KEY_MAX_ZERO)).setEnabled(flag_switch);
                        return true;
                    }
                });
            }
        },
        DELTA_STAB(R.string.KEY_DELTA_STAB){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                boolean check = name.getSharedPreferences().getBoolean(name.getContext().getString(R.string.KEY_SWITCH_STABLE), false);
                name.setEnabled(check);
                name.setTitle(title + " " + name.getSharedPreferences().getInt(name.getKey(), 10) + ' ' + context.getString(R.string.scales_kg));
                //name.setSummary(context.getString(R.string.sum_max_null) + ' ' + context.getResources().getInteger(R.integer.default_limit_auto_null) + ' ' + context.getString(R.string.scales_kg));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || "0".equals(o.toString()) /*|| Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_limit_auto_null)*/) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        preference.setTitle(title + " " + o + ' ' + context.getString(R.string.scales_kg));
                        settings.write(preference.getKey(), Integer.valueOf(o.toString()));
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
                final CharSequence title = name.getTitle();
                int t = name.getSharedPreferences().getInt(name.getKey(), 10);
                name.setDefaultValue(t);
                name.setTitle(title + " " + t + ' ' + context.getString(R.string.minute));
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
                        try {
                            if (scalesView.getScaleModule().setModuleTimeOff(Integer.valueOf(o.toString()))) {
                                settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                                preference.setTitle(title + " " + o + ' ' + context.getString(R.string.minute));
                                Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o + ' ' + context.getString(R.string.minute), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        return true;
                    }
                });
            }
        },
        STEP(R.string.KEY_DISCRETE){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                name.setTitle(title + " " + name.getSharedPreferences().getInt(name.getKey(), 5) + ' ' + context.getString(R.string.scales_kg));
                name.setSummary(context.getString(R.string.The_range_is_from_1_to) + context.getResources().getInteger(R.integer.default_max_step_scale) + ' ' + context.getString(R.string.scales_kg));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || "0".equals(o.toString()) || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_max_step_scale)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        preference.setTitle(title + " " + o + ' ' + context.getString(R.string.scales_kg));
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
                final CharSequence title = name.getTitle();
                int f = name.getSharedPreferences().getInt(name.getKey(), 15);
                name.setDefaultValue(f);
                name.setTitle(title + " " + String.valueOf(f));
                name.setSummary(context.getString(R.string.sum_filter_adc) + ' ' + context.getString(R.string.The_range_is_from_0_to) + context.getResources().getInteger(R.integer.default_adc_filter));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() || Integer.valueOf(o.toString()) > context.getResources().getInteger(R.integer.default_adc_filter)) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        try {
                            if (scalesView.getScaleModule().setModuleFilterADC(Integer.valueOf(o.toString()))) {
                                settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                                preference.setTitle(title + " " + o);
                                Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o.toString(), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        return true;
                    }
                });
            }
        },
        /*STABLE(R.string.KEY_STABLE){
            @Override
            void setup(Preference name) throws Exception {
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return false;
                    }
                });
            }
        },*/
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
        },
        TEST(R.string.KEY_ABOUT){
            @Override
            void setup(Preference name) throws Exception {
                //View view = name.getView(null, null);
                //TextView textView = (TextView)view.findViewById(R.id.textView1);
                //textView.setText("test");
            }
        };

        private final int resId;
        abstract void setup(Preference name)throws Exception;

        KEY(int key){
            resId = key;
        }
        public int getResId() { return resId; }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.fragment_settings, false);
        settings = new Settings(getActivity(), ScalesView.SETTINGS);
        scalesView = ScalesView.getInstance();
        initPreferences();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scalesView.updateSettings(settings);
    }

    public void initPreferences(){
        for (KEY enumPreference : KEY.values()){
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

