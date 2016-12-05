package com.kostya.cranescale;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;

import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.konst.scaleslibrary.*;
import com.konst.scaleslibrary.module.InterfaceModule;
import com.konst.scaleslibrary.module.scale.ObjectScales;
import com.kostya.cranescale.provider.InvoiceTable;
import com.kostya.cranescale.provider.WeighingTable;
import com.kostya.cranescale.settings.ActivityPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class FragmentInvoice extends Fragment implements View.OnClickListener {
    private SimpleCursorAdapter adapterWeightingList;
    private InvoiceTable invoiceTable;
    private WeighingTable weighingTable;
    protected ContentValues values = new ContentValues();
    private EditText dateInvoice, nameInvoice, loadingInvoice, totalInvoice;
    private ListView listInvoice;
    private BaseReceiver baseReceiver; //приёмник намерений
    private Settings settings;
    private String entryID;
    private int deltaStab, capture;
    private static final String ARG_DATE = "date";
    private static final String ARG_PARAM2 = "param2";

    private String mParam2;

    private OnFragmentInvoiceListener mListener;

    enum STAGE{
        START,
        LOADING,
        STABLE,
        UNLOADING
    }

    public FragmentInvoice() {
        // Required empty public constructor
    }

    /**
     * @param date Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InvoiceFragment.
     */
    public static FragmentInvoice newInstance(String date, String param2) {
        FragmentInvoice fragment = new FragmentInvoice();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            values.put(InvoiceTable.KEY_DATE_TIME_CREATE, getArguments().getString(ARG_DATE));
            //mParam1 = getArguments().getString(ARG_DATE);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }else {
            values.put(InvoiceTable.KEY_DATE_TIME_CREATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        }

        settings = new Settings(getActivity(), ActivityTest.SETTINGS);

        invoiceTable = new InvoiceTable(getActivity());
        entryID = invoiceTable.insertNewEntry(values).getLastPathSegment();
        weighingTable = new WeighingTable(getActivity());

        baseReceiver = new BaseReceiver(getActivity());
        baseReceiver.register();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSettings(settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice, null);

        dateInvoice = (EditText)view.findViewById(R.id.invoiceDate);
        dateInvoice.setText(values.getAsString(InvoiceTable.KEY_DATE_TIME_CREATE));

        nameInvoice = (EditText)view.findViewById(R.id.invoiceName);
        loadingInvoice = (EditText)view.findViewById(R.id.invoiceLoading);
        listInvoice = (ListView) view.findViewById(R.id.invoiceList);
        updateListWeight();
        totalInvoice = (EditText)view.findViewById(R.id.invoiceTotal);
        view.findViewById(R.id.buttonCloseInvoice).setOnClickListener(this);

        return view;
    }

    /** Кнопка закрыть накладную. */
    public void onClosePressedButton() {
        if (mListener != null) {
            mListener.onInvoiceClosePressedButton();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInvoiceListener) {
            mListener = (OnFragmentInvoiceListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInvoiceListener) {
            mListener = (OnFragmentInvoiceListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        baseReceiver.unregister();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonCloseInvoice){
            onClosePressedButton();
        }
    }

    public void updateSettings(Settings settings){

        for(ActivityPreferences.Key key : ActivityPreferences.Key.values()){
            switch (key){
                case DELTA_STAB:
                    deltaStab = settings.read(key.getResId(), 20);
                    break;
                case CAPTURE:
                    capture = settings.read(key.getResId(), 100);
                    break;
                default:
            }
        }
    }

    public void removeWeightOnClick(View view) {

        //ListView list = getListView();
        int position = listInvoice.getPositionForView(view);
        //arrayList.remove(position);
        //vibrator.vibrate(50);
        adapterWeightingList.notifyDataSetChanged();
    }

    public void addRowWeight(int weight){
        weighingTable.insertNewEntry(Integer.valueOf(entryID), weight);
        //adapterWeightingList.notifyDataSetChanged();
    }

    /** Обновляем данные листа загрузок. */
    private void updateListWeight() {
        Cursor cursor = weighingTable.getEntryInvoice(Integer.valueOf(entryID));
        if (cursor == null) {
            return;
        }

        int[] to = {R.id.bottomText, R.id.topText};

        adapterWeightingList = new SimpleCursorAdapter(getActivity(), R.layout.list_item_weight, cursor, WeighingTable.COLUMN_FOR_INVOICE, to, CursorAdapter.FLAG_AUTO_REQUERY);
        //namesAdapter = new MyCursorAdapter(this, R.layout.item_check, cursor, columns, to);
        //adapterWeightingList.setViewBinder(new ListCheckViewBinder());
        listInvoice.setAdapter(adapterWeightingList);
        listInvoice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                weighingTable.removeEntry((int) l);
            }
        });
    }

    private void doProcess(ObjectScales objectScales){

    }

    /** Интерфейс обратного вызова. */
    public interface OnFragmentInvoiceListener {
        void onInvoiceClosePressedButton();
    }

    class BaseReceiver extends BroadcastReceiver {
        private final Context mContext;
        private SpannableStringBuilder w;
        private Rect bounds;
        private ProgressDialog dialogSearch;
        private final IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(InterfaceModule.ACTION_WEIGHT_STABLE);
            intentFilter.addAction(InterfaceModule.ACTION_SCALES_RESULT);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case InterfaceModule.ACTION_SCALES_RESULT:
                        ObjectScales obj = (ObjectScales) intent.getSerializableExtra(InterfaceModule.EXTRA_SCALES);
                        if (obj == null)
                            return;
                        doProcess(obj);
                        break;
                    case InterfaceModule.ACTION_WEIGHT_STABLE:

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
