package com.konst.scaleslibrary;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * @author Kostya on 12.11.2016.
 */
public class Main extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
