package com.konst.scaleslibrary;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.Window;
import android.widget.*;

import java.util.ArrayList;

/**
 * @author Kostya on 04.10.2016.
 */
public class SearchDialog extends Dialog implements View.OnClickListener{
    private final BaseReceiver broadcastReceiver;                 //приёмник намерений
    private final ArrayList<BluetoothDevice> foundDevice;         //чужие устройства
    private ArrayAdapter<BluetoothDevice> bluetoothAdapter; //адаптер имён
    private ListView listView;                              //список весов
    private TextView textViewLog;                           //лог событий
    private final Settings settings;
    private final String message;
    private final ScalesView.OnCreateScalesListener callbackScales;

    public SearchDialog(Context context, String text, ScalesView.OnCreateScalesListener callbackScales) {
        super(context);
        message = text;
        this.callbackScales = callbackScales;
        settings = new Settings(context, Settings.SETTINGS);
        foundDevice = new ArrayList<>();
        broadcastReceiver = new BaseReceiver(getContext());
        broadcastReceiver.register();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.search_device);
        setCancelable(false);

        findViewById(R.id.buttonSearchBluetooth).setOnClickListener(this);
        findViewById(R.id.buttonBack).setOnClickListener(this);

        textViewLog = (TextView)findViewById(R.id.textLog);
        log(message);
        listView = (ListView)findViewById(R.id.listViewDevices);  //список весов
        listView.setOnItemClickListener(onItemClickListener);

        for (int i = 0; settings.contains(Settings.KEY_ADDRESS + i); i++) { //заполнение списка
            foundDevice.add(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(settings.read(Settings.KEY_ADDRESS + i, "")));
        }

        bluetoothAdapter = new BluetoothListAdapter(getContext(), foundDevice);
        listView.setAdapter(bluetoothAdapter);

        if (foundDevice.isEmpty()) {
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
        }
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.buttonSearchBluetooth) {
            searchDevice();
        } else if (i == R.id.buttonBack) {
            dismiss();
        } else {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        }
        broadcastReceiver.unregister();

        for (int i = 0; settings.contains(Settings.KEY_ADDRESS + i); i++) { //стереть прошлый список
            settings.remove(Settings.KEY_ADDRESS + i);
        }
        for (int i = 0; i < foundDevice.size(); i++) { //сохранить новый список
            settings.write(Settings.KEY_ADDRESS + i, ((BluetoothDevice) foundDevice.toArray()[i]).getAddress());
        }
        super.dismiss();
    }

    void searchDevice(){
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        broadcastReceiver.register();
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            }
            callbackScales.onCreate(((BluetoothDevice) foundDevice.toArray()[i]).getAddress());
            dismiss();
        }
    };

    //==================================================================================================================
    void log(int resource) { //для ресурсов
        textViewLog.setText(getContext().getString(resource) + '\n' + textViewLog.getText());
    }

    //==================================================================================================================
    public void log(String string) { //для текста
        textViewLog.setText(string + '\n' + textViewLog.getText());
    }

    //==================================================================================================================
    void log(int resource, boolean toast) { //для текста
        textViewLog.setText(getContext().getString(resource) + '\n' + textViewLog.getText());
        if (toast) {
            Toast.makeText(getContext(), resource, Toast.LENGTH_SHORT).show();
        }
    }

    //==================================================================================================================
    void log(int resource, String str) { //для ресурсов с текстовым дополнением
        textViewLog.setText(getContext().getString(resource) + ' ' + str + '\n' + textViewLog.getText());
    }

    class BaseReceiver extends BroadcastReceiver {
        final Context mContext;
        SpannableStringBuilder w;
        Rect bounds;
        ProgressDialog dialogSearch;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED: //поиск начался
                        log(R.string.discovery_started);
                        foundDevice.clear();
                        bluetoothAdapter.notifyDataSetChanged();
                        break;
                    case BluetoothDevice.ACTION_FOUND:  //найдено устройство
                        BluetoothDevice bd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        foundDevice.add(bd);
                        bluetoothAdapter.notifyDataSetChanged();
                        //BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String name = null;
                        if (bd != null) {
                            name = bd.getName();
                        }
                        if (name != null) {
                            log(R.string.device_found, name);
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:  //поиск завершён
                        log("Поиск завершён");
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
}
