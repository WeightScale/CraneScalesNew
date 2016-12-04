package com.konst.scaleslibrary;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.*;

import java.util.ArrayList;

/**
 * @author Kostya on 04.10.2016.
 */
public class SearchDialogFragment extends DialogFragment implements View.OnClickListener{
    private BaseReceiver broadcastReceiver;                   //приёмник намерений
    private ArrayList<BluetoothDevice> foundDevice;           //чужие устройства
    private ArrayAdapter<BluetoothDevice> bluetoothAdapter;   //адаптер имён
    private ListView listView;                                //список весов
    private TextView textViewLog;                             //лог событий
    private Settings settings;
    private String message;
    private static final String ARG_MESSAGE = SearchDialogFragment.class.getSimpleName()+"MESSAGE";
    public static final String ARG_DEVICE = SearchDialogFragment.class.getSimpleName()+"DEVICE";
    private static final int REQUEST_DEVICE = 1;


    public static SearchDialogFragment newInstance(String text) {
        SearchDialogFragment fragment = new SearchDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            message = getArguments().getString(ARG_MESSAGE);
        }
        settings = new Settings(getActivity(), ScalesView.SETTINGS);
        foundDevice = new ArrayList<>();
        broadcastReceiver = new BaseReceiver(getActivity());
        broadcastReceiver.register();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.search_device, null);
        view.findViewById(R.id.buttonSearchBluetooth).setOnClickListener(this);
        view.findViewById(R.id.buttonBack).setOnClickListener(this);
        textViewLog = (TextView)view.findViewById(R.id.textLog);
        log(message);
        listView = (ListView)view.findViewById(R.id.listViewDevices);  //список весов
        listView.setOnItemClickListener(onItemClickListener);

        for (int i = 0; settings.contains(getActivity().getString(R.string.KEY_ADDRESS) + i); i++) { //заполнение списка
            foundDevice.add(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(settings.read(getActivity().getString(R.string.KEY_ADDRESS) + i, "")));
        }

        bluetoothAdapter = new BluetoothListAdapter(getActivity(), foundDevice);
        listView.setAdapter(bluetoothAdapter);

        if (foundDevice.isEmpty()) {
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        AlertDialog dialog = builder.create();
        setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonSearchBluetooth) {
            searchDevice();
        } else {
            dismiss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        }
        broadcastReceiver.unregister();

        for (int i = 0; settings.contains(getActivity().getString(R.string.KEY_ADDRESS) + i); i++) { //стереть прошлый список
            settings.remove(getActivity().getString(R.string.KEY_ADDRESS) + i);
        }
        for (int i = 0; i < foundDevice.size(); i++) { //сохранить новый список
            settings.write(getActivity().getString(R.string.KEY_ADDRESS) + i, ((BluetoothDevice) foundDevice.toArray()[i]).getAddress());
        }
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
            Intent intent = new Intent().putExtra(ARG_DEVICE, ((BluetoothDevice) foundDevice.toArray()[i]).getAddress());
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            dismiss();
        }
    };

    //==================================================================================================================
    void log(int resource) { //для ресурсов
        textViewLog.setText(getActivity().getString(resource) + '\n' + textViewLog.getText());
    }

    //==================================================================================================================
    public void log(String string) { //для текста
        textViewLog.setText(string + '\n' + textViewLog.getText());
    }

    //==================================================================================================================
    void log(int resource, boolean toast) { //для текста
        textViewLog.setText(getActivity().getString(resource) + '\n' + textViewLog.getText());
        if (toast) {
            Toast.makeText(getActivity(), resource, Toast.LENGTH_SHORT).show();
        }
    }

    //==================================================================================================================
    void log(int resource, String str) { //для ресурсов с текстовым дополнением
        textViewLog.setText(getActivity().getString(resource) + ' ' + str + '\n' + textViewLog.getText());
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
