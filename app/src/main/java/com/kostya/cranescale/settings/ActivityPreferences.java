//Активность настроек
package com.kostya.cranescale.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import com.konst.scaleslibrary.ScalesView;
import com.konst.scaleslibrary.Settings;
import com.konst.scaleslibrary.module.scale.ScaleModule;
import com.konst.scaleslibrary.settings.ActivityProperties;
import com.kostya.cranescale.ActivityAbout;
import com.kostya.cranescale.ActivityTest;
import com.kostya.cranescale.Globals;
import com.kostya.cranescale.R;

public class ActivityPreferences extends PreferenceActivity {
    private static Settings settings;
    private static Globals globals;
    private static ScaleModule scaleModule;
    public enum KEY{
        DELTA_STAB(R.string.KEY_DELTA_STAB){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                name.setTitle(title + " " + settings.read(name.getKey(), 20)+ "кг");
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() ) {
                            Toast.makeText(context, R.string.preferences_no, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        try {
                            settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                            preference.setTitle(title + " " + Integer.valueOf(o.toString())+ "кг");
                            Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o.toString(), Toast.LENGTH_SHORT).show();
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
            }
        },
        CAPTURE(R.string.KEY_AUTO_CAPTURE){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                name.setTitle(name.getTitle() + " " + settings.read(name.getKey(), 200)+ "кг");
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty() ) {
                            Toast.makeText(context, R.string.preferences_no, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        try {
                            settings.write(preference.getKey(), Integer.valueOf(o.toString()));
                            preference.setTitle(title + " " + Integer.valueOf(o.toString())+ "кг");
                            Toast.makeText(context, context.getString(R.string.preferences_yes) + ' ' + o.toString(), Toast.LENGTH_SHORT).show();
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(context, R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                });
            }
        },
        SWITCH_LOADING(R.string.KEY_SWITCH_LOADING){
            @Override
            void setup(Preference name) throws Exception {
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        boolean flag_switch = (boolean)o;

                        settings.write(preference.getKey(), flag_switch);
                        preference.getPreferenceManager().findPreference(preference.getContext().getString(R.string.KEY_WEIGHT_LOADING)).setEnabled(flag_switch);
                        //preference.getPreferenceManager().findPreference(preference.getContext().getString(R.string.KEY_MAX_ZERO)).setEnabled(flag_switch);
                        return true;
                    }
                });
            }
        },
        WEIGHT_LOADING(R.string.KEY_WEIGHT_LOADING){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                boolean check = name.getSharedPreferences().getBoolean(name.getContext().getString(R.string.KEY_SWITCH_LOADING), false);
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
        CLOSING_INVOICE(R.string.KEY_CLOSING_INVOICE){
            @Override
            void setup(Preference name) throws Exception {
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        boolean flag_switch = (boolean)o;

                        settings.write(preference.getKey(), flag_switch);
                        //preference.getPreferenceManager().findPreference(preference.getContext().getString(R.string.KEY_WEIGHT_LOADING)).setEnabled(flag_switch);
                        //preference.getPreferenceManager().findPreference(preference.getContext().getString(R.string.KEY_MAX_ZERO)).setEnabled(flag_switch);
                        return true;
                    }
                });
            }
        },
        SCALES(R.string.KEY_SCALES){
            @Override
            void setup(Preference name) throws Exception {
                final Context context = name.getContext();
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (!com.konst.scaleslibrary.settings.ActivityProperties.isActive()) {
                            preference.getContext().startActivity(new Intent(preference.getContext(), com.konst.scaleslibrary.settings.ActivityProperties.class));
                            ((ActivityPreferences)preference.getContext()).finish();
                        }
                        return false;
                    }
                });
            }
        },
        ABOUT(R.string.KEY_ABOUT){
            @Override
            void setup(Preference name)throws Exception {
                final Context context = name.getContext();
                name.setSummary(context.getString(R.string.version) + globals.getPackageInfo().versionName + ' ' + Integer.toString(globals.getPackageInfo().versionCode));
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        context.startActivity(new Intent().setClass(context, ActivityAbout.class));
                        return false;
                    }
                });
            }
        };

        private final int resId;
        abstract void setup(Preference name)throws Exception;

        KEY(int key){
            resId = key;
        }

        public int getResId() { return resId; }
    }

    public void process(){
        for (KEY enumPreference : KEY.values()){
            Preference preference = findPreference(getString(enumPreference.getResId()));
            try {
                enumPreference.setup(preference);
            } catch (Exception e) {
                preference.setEnabled(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = 1.0f;
        getWindow().setAttributes(layoutParams);

        globals = Globals.getInstance();
        settings = new Settings(this, ActivityTest.SETTINGS);
        scaleModule = globals.getScaleModule();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        process();
    }
}

