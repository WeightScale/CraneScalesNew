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
        },
        ADMIN(R.string.KEY_ADMIN){
            Context context;
            private EditText input;
            @Override
            void setup(Preference name)throws Exception {
                context = name.getContext();
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startDialog();
                        return true;
                    }
                });
            }

            void startDialog(){
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("ВВОД КОДА");
                input = new EditText(context);
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                dialog.setView(input);
                dialog.setCancelable(false);
                dialog.setPositiveButton(context.getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (input.getText() != null) {
                            boolean key = false;
                            String string = input.getText().toString();
                            if (!string.isEmpty()){
                                try{
                                    if ("343434".equals(string))
                                        key = true;
                                    else if (string.equals(scaleModule.getModuleServiceCod()))
                                        key = true;
                                    if (key){
                                        context.startActivity(new Intent().setClass(context,ActivityTuning.class));
                                        return;
                                    }
                                }catch (Exception e){}
                            }
                        }
                    }
                });
                dialog.setNegativeButton(context.getString(R.string.Close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.setMessage("Введи код доступа к административным настройкам");
                dialog.show();
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

