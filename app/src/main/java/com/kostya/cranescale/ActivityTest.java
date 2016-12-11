package com.kostya.cranescale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.konst.module.InterfaceModule;
import com.konst.module.scale.ObjectScales;
import com.konst.scaleslibrary.ScalesFragment;
import com.konst.scaleslibrary.ScalesView;
import com.konst.scaleslibrary.Settings;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.scale.ScaleModule;
import com.kostya.cranescale.settings.ActivityPreferences;
import com.kostya.cranescale.task.IntentServiceGoogleForm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * @author Kostya on 02.10.2016.
 */
public class ActivityTest extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        FragmentInvoice.OnFragmentInvoiceListener, ScalesFragment.OnInteractionListener {
    private Vibrator vibrator; //вибратор
    private DrawerLayout drawer;
    private FloatingActionButton fab;
    private ScalesView scalesView;
    private ScaleModule scaleModule;
    private FragmentManager fragmentManager;
    private FragmentInvoice fragmentInvoice;
    private FragmentListInvoice fragmentListInvoice;
    private Globals globals;
    private ListView listView;
    private final ArrayList<WeightObject> arrayList = new ArrayList<>();
    private ArrayAdapter<WeightObject> customListAdapter;
    /** Настройки общии для модуля. */
    public static final String SETTINGS = ActivityTest.class.getName() + ".SETTINGS"; //
    private static final int ALERT_DIALOG1 = 1;
    private static final int ALERT_DIALOG2 = 2;
    private boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                openFragmentInvoice(null);
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        globals = Globals.getInstance();
        globals.initialize(this);

        fragmentManager = getFragmentManager();

        fragmentListInvoice = FragmentListInvoice.newInstance();
        fragmentManager.beginTransaction().add(R.id.fragmentInvoice, fragmentListInvoice, FragmentListInvoice.class.getSimpleName()).commit();
        //listView = (ListView)findViewById(R.id.listView);
        //listView.setCacheColorHint(getResources().getColor(R.color.transparent));
        //listView.setVerticalFadingEdgeEnabled(false);
        customListAdapter = new CustomListAdapter(this, R.layout.list_item_weight, arrayList);
        //listView.setAdapter(customListAdapter);
        customListAdapter.notifyDataSetChanged();

        scalesView = (ScalesView)findViewById(R.id.scalesView);
        scalesView.create(globals.getPackageInfo().versionName, new InterfaceCallbackScales() {
            @Override
            public void onCallback(Module obj) {
                globals.setScaleModule((ScaleModule)obj);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scales, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.preferences:
                startActivity(new Intent(getApplicationContext(), ActivityPreferences.class));
            break;
            case R.id.search:
                scalesView.openSearchScales();
            break;
            case R.id.power_off:
                finish();
            break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.search_scales:
                scalesView.openSearchScales();
            break;
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), ActivityPreferences.class));
            break;
            case R.id.new_invoice:
                openFragmentInvoice(null);
            break;
            case R.id.power:
                finish();
            break;
            default:
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scalesView.exit();
        startService(new Intent(this, IntentServiceGoogleForm.class).setAction(IntentServiceGoogleForm.ACTION_EVENT_TABLE));
    }

    @Override
    public void onBackPressed() {
        /*if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            //exit();
            return;
        }
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);*/
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.CustomAlertDialogInvoice));
        builder.setCancelable(false)
                .setTitle("Сообщение");
        switch(id) {
            case ALERT_DIALOG1:
                builder.setMessage("Накладная уже открыта. Закройте накладную после создайте новую.")
                        .setIcon(R.drawable.ic_notification)
                        .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Do something here
                                dialog.dismiss();
                            }
                        });
                break;
            case ALERT_DIALOG2:
                builder.setMessage("ВЫ ХОТИТЕ ЗАКРЫТЬ НАКЛАДНУЮ?")
                        .setIcon(R.drawable.ic_notification)
                        .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                fragmentManager.beginTransaction().remove(fragmentInvoice).commit();
                                fab.setVisibility(View.VISIBLE);
                            }
                        }).setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                break;
            default:
                return null;
        }
        return builder.create();

    }

    public void closedInvoice(){
        fragmentManager.beginTransaction().remove(fragmentInvoice).commit();
        fab.setVisibility(View.VISIBLE);
        startService(new Intent(this, IntentServiceGoogleForm.class).setAction(IntentServiceGoogleForm.ACTION_EVENT_TABLE));
    }

    @Override
    public void onEnableStable(boolean enable) {
        if (scaleModule != null)
            scaleModule.setEnableProcessStable(enable);
    }

    public void openFragmentInvoice(String id){
        vibrator.vibrate(50);
        Fragment fragment = getFragmentManager().findFragmentByTag(FragmentInvoice.class.getSimpleName());
        if (fragment instanceof FragmentInvoice){
            showDialog(ALERT_DIALOG1);
        }else {
            fragmentInvoice = FragmentInvoice.newInstance(new SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(new Date()),
                    new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()), id);
            fragmentManager.beginTransaction().add(R.id.fragmentInvoice, fragmentInvoice, FragmentInvoice.class.getSimpleName()).commit();
            fab.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUpdateSettings(Settings settings) {

    }

    @Override
    public void onScaleModuleCallback(ScaleModule obj) {
        scaleModule = obj;
    }

    @Override
    public void onSaveWeight(int weight) {
        if (fragmentInvoice != null)
            fragmentInvoice.addRowWeight(weight);
    }

    class BaseReceiver extends BroadcastReceiver {
        private final Context mContext;
        private final IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(InterfaceModule.ACTION_WEIGHT_STABLE);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case InterfaceModule.ACTION_WEIGHT_STABLE:
                        ObjectScales obj = (ObjectScales) intent.getSerializableExtra(InterfaceModule.EXTRA_SCALES);
                        if (obj == null)
                            return;
                        arrayList.add(new WeightObject(obj.getWeight()));
                        customListAdapter.notifyDataSetChanged();                 //сохраняем стабильный вес
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

    static class WeightObject {
        final String date;
        final String time;
        final int weight;

        WeightObject(int weight){
            this.weight = weight;
            Date d = new Date();
            date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(d);
            time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(d);
        }

        public int getWeight() { return weight; }

        public String getDate() { return date;  }

        public String getTime() { return time;  }
    }

    static class CustomListAdapter extends ArrayAdapter<WeightObject> {
        final ArrayList<WeightObject> item;

        public CustomListAdapter(Context context, int textViewResourceId, ArrayList<WeightObject> objects) {
            super(context, textViewResourceId, objects);
            item = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.list_item_weight, parent, false);
            }

            WeightObject o = getItem(position);
            if(o != null){
                TextView tt = (TextView) view.findViewById(R.id.topText);
                TextView bt = (TextView) view.findViewById(R.id.bottomText);

                tt.setText(o.getWeight() +" кг");
                bt.setText(o.getTime() + "   " + o.getDate());
            }


            return view;
        }
    }
}
