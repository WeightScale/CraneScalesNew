package com.kostya.cranescale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.os.Bundle;
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
import com.konst.module.InterfaceModule;
import com.konst.module.scale.ObjectScales;
import com.konst.scaleslibrary.ScalesFragment;
import com.konst.scaleslibrary.ScalesView;
import com.konst.scaleslibrary.module.Module;
import com.konst.scaleslibrary.module.scale.InterfaceCallbackScales;
import com.konst.scaleslibrary.module.scale.ScaleModule;
import com.kostya.cranescale.settings.ActivityPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * @author Kostya on 02.10.2016.
 */
public class ActivityTest extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        FragmentInvoice.OnFragmentInvoiceListener {
    private DrawerLayout drawer;
    FloatingActionButton fab;
    private ScalesView scalesView;
    private FragmentManager fragmentManager;
    private FragmentInvoice fragmentInvoice;
    private Globals globals;
    private ListView listView;
    private final ArrayList<WeightObject> arrayList = new ArrayList<>();
    private ArrayAdapter<WeightObject> customListAdapter;
    private static final int ALERT_DIALOG1 = 1;
    private static final int ALERT_DIALOG2 = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                openFragmentInvoice();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.preferences) {
            startActivity(new Intent(getApplicationContext(), ActivityPreferences.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.search_scales) {
            //scalesView.openSearchDialog("Выбор устройства для соединения");
        } else if (id == R.id.settings) {
            startActivity(new Intent(getApplicationContext(), ActivityPreferences.class));
        } else if (id == R.id.new_invoice){
            openFragmentInvoice();
        }

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scalesView.exit();
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
                builder.setMessage("Вы хотите закрыть накладную?")
                        .setIcon(R.drawable.ic_notification)
                        .setPositiveButton("ДА", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                fragmentManager.beginTransaction().remove(fragmentInvoice).commit();
                                fab.setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("НЕТ", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                break;
            default:
                return null;
        }
        return builder.create();

    }

    @Override
    public void onInvoiceClosePressedButton() {
        showDialog(ALERT_DIALOG2);
    }

    private void openFragmentInvoice(){
        Fragment fragment = getFragmentManager().findFragmentByTag(FragmentInvoice.class.getSimpleName());
        if (fragment instanceof FragmentInvoice){
            showDialog(ALERT_DIALOG1);
        }else {

            fragmentInvoice = FragmentInvoice.newInstance(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()), "");
            fragmentManager.beginTransaction().replace(R.id.fragmentInvoice, fragmentInvoice, FragmentInvoice.class.getSimpleName()).commit();
            fab.setVisibility(View.INVISIBLE);
        }
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
