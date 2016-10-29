//Простой класс настроек
package com.konst.scaleslibrary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

public class Settings {
    Context mContext;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    /** Настройки общии для весов. */
    public static final String SETTINGS = ScalesView.class.getName() + ".SETTINGS"; //
    /** ключ адресс bluetooth. */
    //public static final String KEY_ADDRESS = ScalesView.class.getPackage() +".KEY_ADDRESS";
    /** Ключ значения дискретности веса. */
    /*public static final String KEY_DISCRETE = ScalesView.class.getPackage() +".KEY_DISCRETE";*/
    /** Ключь флага стабилизации веса. */
    public static final String KEY_STABLE = ScalesView.class.getPackage() +".KEY_STABLE";

    enum KEY{
        /** ключ адресс bluetooth. */
        KEY_ADDRESS(R.string.KEY_ADDRESS),
        /** Ключ значения дискретности веса. */
        KEY_DISCRETE(R.string.KEY_DISCRETE);
        private final int resId;
        KEY(int key){
            resId = key;
        }
        public int getResId() { return resId; }
    }

    public Settings(Context context, String name) {
        mContext = context;
        load(mContext.getSharedPreferences(name, Context.MODE_PRIVATE)); //загрузить настройки
    }

    public Settings(Context context) {
        mContext = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public void load(SharedPreferences sp) {
        sharedPreferences = sp;
        editor = sp.edit();
        editor.apply();
    }

    public void write(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void write(KEY key, String value) {
        editor.putString(mContext.getString(key.getResId()), value);
        editor.commit();
    }

    public void write(KEY key, int value) {
        editor.putInt(mContext.getString(key.getResId()), value);
        editor.commit();
    }

    public void write(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
        //sharedPreferences.edit().commit();
    }

    public void write(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public void write(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public String read(KEY key, String def) {

        return sharedPreferences.getString(mContext.getString(key.getResId()), def);
    }

    public String read(String key, String def) {
        return sharedPreferences.getString(key, def);
    }

    boolean read(String key, boolean def) {
        return sharedPreferences.getBoolean(key, def);
    }

    public int read(String key, int in) {
        return sharedPreferences.getInt(key, in);
    }

    public int read(KEY key, int in) {
        return sharedPreferences.getInt(mContext.getString(key.getResId()), in);
    }

    public float read(String key, float in) { return sharedPreferences.getFloat(key, in); }

    boolean contains(KEY key) {
        return sharedPreferences.contains(mContext.getString(key.getResId()));
    }

    boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /*public void preferenceChangeListener(Preference.OnPreferenceClickListener listener){
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }*/
}