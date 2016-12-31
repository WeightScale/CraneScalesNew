//Активность настроек
package com.konst.grabewifi.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.common.io.ByteStreams;
import com.konst.grabewifi.Globals;
import com.konst.grabewifi.MainActivity;
import com.konst.grabewifi.R;
import com.konst.grabewifi.filedialog.FileChooserDialog;
import com.konst.scaleslibrary.Settings;
import com.konst.scaleslibrary.module.Module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ActivityPreferences extends PreferenceActivity {
    private static Settings settings;
    private static Globals globals;
    private static Module scaleModule;
    public enum KEY{
        DELTA_STAB(R.string.KEY_DELTA_STAB){
            @Override
            void setup(Preference name) {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                name.setTitle(title + " " + settings.read(name.getKey(), 10)+ "кг");
                name.setOnPreferenceChangeListener(new MyOnPreferenceChangeListener(context, title));
            }
            class MyOnPreferenceChangeListener implements Preference.OnPreferenceChangeListener {
                private final Context context;
                private final CharSequence title;

                public MyOnPreferenceChangeListener(Context context, CharSequence title) {
                    this.context = context;
                    this.title = title;
                }

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
            }
        },
        CAPTURE(R.string.KEY_AUTO_CAPTURE){
            @Override
            void setup(Preference name) {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                name.setTitle(name.getTitle() + " " + settings.read(name.getKey(), 100)+ "кг");
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
            void setup(Preference name) {
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
            void setup(Preference name) {
                final Context context = name.getContext();
                final CharSequence title = name.getTitle();
                boolean check = name.getSharedPreferences().getBoolean(name.getContext().getString(R.string.KEY_SWITCH_LOADING), false);
                name.setEnabled(check);
                name.setTitle(title + " " + name.getSharedPreferences().getInt(name.getKey(), 1000) + ' ' + context.getString(R.string.scales_kg));
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
            void setup(Preference name) {
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
            void setup(Preference name) {
                final Context context = name.getContext();
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (!com.konst.scaleslibrary.settings.ActivityProperties.isActive()) {
                            preference.getContext().startActivity(new Intent(preference.getContext(), com.konst.scaleslibrary.settings.ActivityProperties.class));
                            ((Activity) preference.getContext()).finish();
                        }
                        return false;
                    }
                });
            }
        },
        PATH_FILE_FORM(R.string.KEY_PATH_FORM){
            @Override
            void setup(Preference name) {
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showFileChooser(preference.getContext());
                        return false;
                    }
                });
            }
            public void showFileChooser(Context context) {
                FileChooserDialog dialog = new FileChooserDialog(context);
                dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
                    @Override
                    public void onFileSelected(Dialog source, File file) {
                        source.hide();
                        /* Получаем путь к файлу. */
                        Uri uri = Uri.fromFile(file);
                        /* Создаем фаил с именем . */
                        File storeFile = new File(Globals.getInstance().pathLocalForms, "form.xml");
                        try {
                            /* Создаем поток для записи фаила в папку хранения. */
                            FileOutputStream fileOutputStream = new FileOutputStream(storeFile);
                            InputStream inputStream = source.getContext().getContentResolver().openInputStream(uri);
                            /* Получаем байты данных. */
                            byte[] bytes = ByteStreams.toByteArray(inputStream);
                            inputStream.close();
                            /* Записываем фаил в папку. */
                            fileOutputStream.write(bytes);
                            /* Закрываем поток. */
                            fileOutputStream.close();
                            Toast.makeText(source.getContext(), "Фаил сохранен " + file.getPath(),  Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(source.getContext(), "Ошибка выбора файла " + e.getMessage(),  Toast.LENGTH_LONG).show();
                        }
                        //systemTable.updateEntry(SystemTable.Name.PATH_FORM, uri.toString());
                    }
                    @Override
                    public void onFileSelected(Dialog source, File folder, String name) {
                        source.hide();
                        Toast toast = Toast.makeText(source.getContext(), "File created: " + folder.getName() + '/' + name, Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                dialog.show();
                /*Intent intent = new Intent();
                //intent.setType("**//*//*");
                //intent.addCategory(Intent.CATEGORY_OPENABLE);
                if (Build.VERSION.SDK_INT < 19){
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    //((Activity)context).startActivityForResult(intent, FILE_SELECT_CODE);
                } else {
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    //((Activity)context).startActivityForResult(intent, FILE_SELECT_CODE);
                }
                intent.setType("**//*//*");

                try {
                    ((Activity)context).startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(context, "Пожалуйста инсталируйте File Manager.",  Toast.LENGTH_LONG).show();
                }*/
            }
        },
        ABOUT(R.string.KEY_ABOUT){
            @Override
            void setup(Preference name) {
                final Context context = name.getContext();
                name.setSummary(context.getString(R.string.version) + globals.getPackageInfo().versionName + " v." + Integer.toString(globals.getPackageInfo().versionCode));
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        context.startActivity(new Intent().setClass(context, MainActivity.class));
                        return false;
                    }
                });
            }
        };

        private final int resId;
        abstract void setup(Preference name);

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
        settings = new Settings(this, MainActivity.SETTINGS);
        scaleModule = globals.getScaleModule();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        process();
    }


}

