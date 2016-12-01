package com.kostya.cranescale;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.konst.module.InterfaceModule;
import com.kostya.cranescale.services.ServiceScales;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FragmentScales fragmentScales;
    private FragmentSearch fragmentSearch;
    private FragmentTransaction fragmentTransaction;
    private BaseReceiver baseReceiver;
    private Globals globals;
    private boolean doubleBackToExitPressedOnce;
    private static final String TAG_FRAGMENT = ActivityMain.class.getName() + "TAG_FRAGMENT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        globals = Globals.getInstance();
        globals.initialize(this);

        fragmentScales = new FragmentScales();
        fragmentSearch = new FragmentSearch();

        baseReceiver = new BaseReceiver(this);
        baseReceiver.register();

        if (savedInstanceState == null){
            lockOrientation();
            Bundle bundle = new Bundle();
            bundle.putString(ServiceScales.EXTRA_VERSION, globals.getPackageInfo().versionName);
            bundle.putString(ServiceScales.EXTRA_DEVICE, globals.getPreferencesScale().read(getString(R.string.KEY_LAST_SCALES), ""));
            Intent intent = new Intent(getApplicationContext(), ServiceScales.class);
            intent.setAction(ServiceScales.ACTION_CONNECT_SCALES);
            intent.putExtra(ServiceScales.EXTRA_BUNDLE, bundle);
            startService(intent);
        }else {
            String tag = savedInstanceState.getString(TAG_FRAGMENT);
            Fragment fragment = getFragmentManager().findFragmentByTag(tag);
            if (fragment != null){
                if (fragment instanceof FragmentScales){
                    fragmentScales = (FragmentScales) fragment;
                }else if(fragment instanceof  FragmentSearch){
                    fragmentSearch = (FragmentSearch) fragment;
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentCont);
        if (fragment != null){
            outState.putString(TAG_FRAGMENT, fragment.getTag());
        }
    }

    @Override
    public void onDestroy() { //при разрушении активности
        super.onDestroy();
        baseReceiver.unregister();
        //exit();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            exit();
            return;
        }
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_again_to_exit , Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    public void unlockOrientation() {
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public void lockOrientation() {

        if (Build.VERSION.SDK_INT < 18)
            setRequestedOrientation(getOrientation());
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    private int getOrientation() throws AssertionError {

        int port = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        int revP = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        int land = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        int revL = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        if (Build.VERSION.SDK_INT < 9) {
            revL = land;
            revP = port;
        } else if (isLandscape270()) {
            land = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            revL = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        Display display = getWindowManager().getDefaultDisplay();
        boolean wide = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                return wide ? land : port;
            case Surface.ROTATION_90:
                return wide ? land : revP;
            case Surface.ROTATION_180:
                return wide ? revL : revP;
            case Surface.ROTATION_270:
                return wide ? revL : port;
            default:
                throw new AssertionError();
        }
    }

    private static boolean isLandscape270() {

        return "Amazon".equals(Build.MANUFACTURER) && !("KFOT".equals(Build.MODEL) || "Kindle Fire".equals(Build.MODEL));
    }

    /**
     * Открыть активность поиска весов.
     */
    public void openSearch() {
        try{ globals.getScaleModule().dettach(); }catch (Exception e){}
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentCont, fragmentSearch, fragmentSearch.getClass().getName());
        fragmentTransaction.commit();
    }

    protected void exit() {
        stopService(new Intent(this, ServiceScales.class));
        //todo System.exit(0);
    }

    class BaseReceiver extends BroadcastReceiver {
        final Context mContext;
        ProgressDialog dialogSearch;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(InterfaceModule.ACTION_LOAD_OK);
            intentFilter.addAction(InterfaceModule.ACTION_ATTACH_START);
            intentFilter.addAction(InterfaceModule.ACTION_ATTACH_FINISH);
            intentFilter.addAction(InterfaceModule.ACTION_CONNECT_ERROR);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                fragmentTransaction = getFragmentManager().beginTransaction();
                switch (action) {
                    case InterfaceModule.ACTION_LOAD_OK:
                        unlockOrientation();
                        fragmentScales = new FragmentScales();
                        fragmentTransaction.replace(R.id.fragmentCont, fragmentScales, fragmentScales.getClass().getName());
                        fragmentTransaction.commit();
                        break;
                    case InterfaceModule.ACTION_ATTACH_START:
                        if(dialogSearch != null){
                            if(dialogSearch.isShowing())
                                break;
                        }
                        dialogSearch = new ProgressDialog(ActivityMain.this);
                        dialogSearch.setCancelable(true);
                        dialogSearch.setIndeterminate(false);
                        dialogSearch.show();
                        //View view = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null);
                        dialogSearch.setContentView(R.layout.connect_dialog);
                        dialogSearch.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        String msg = intent.getStringExtra(InterfaceModule.EXTRA_DEVICE_NAME);
                        TextView tv1 = (TextView) dialogSearch.findViewById(R.id.textView1);
                        tv1.setText(getString(R.string.Connecting) + '\n' + msg);
                        break;
                    case InterfaceModule.ACTION_ATTACH_FINISH:
                        if (dialogSearch.isShowing()) {
                            dialogSearch.dismiss();
                        }
                        break;
                    case InterfaceModule.ACTION_CONNECT_ERROR:
                        String value = intent.getStringExtra(InterfaceModule.EXTRA_MESSAGE);
                        fragmentSearch = new FragmentSearch();
                        if (value!=null){
                            Bundle bundle = new Bundle();
                            bundle.putString(InterfaceModule.EXTRA_MESSAGE, value);
                            fragmentSearch.setArguments(bundle);
                        }
                        fragmentTransaction.replace(R.id.fragmentCont, fragmentSearch, fragmentSearch.getClass().getName());

                        fragmentTransaction.commit();
                        break;
                    default:
                }
            }
        }

        public void register() {
            isRegistered = true;
            mContext.registerReceiver(this, intentFilter);
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

    public void removeWeightOnClick(View view) {
        fragmentScales.removeWeightOnClick(view);
    }
}
