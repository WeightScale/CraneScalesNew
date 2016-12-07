package com.kostya.cranescale;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;

import android.provider.BaseColumns;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
    private CustomSimpleCursorAdapter adapterWeightingList;
    private InvoiceTable invoiceTable;
    private WeighingTable weighingTable;
    protected ContentValues values = new ContentValues();
    private EditText dateInvoice, nameInvoice, loadingInvoice, totalInvoice;
    //private ImageView buttonDelete;
    private ListView listInvoice;
    private BaseReceiver baseReceiver; //приёмник намерений
    private Settings settings;
    private String entryID;
    private int deltaStab, capture;
    private int grab, grab_virtual, auto, loading;
    private static final String ARG_DATE = "date";
    private static final String ARG_PARAM2 = "param2";
    private STAGE stage = STAGE.START;
    private String mParam2;
    private boolean stable;

    private OnFragmentInvoiceListener mListener;

    enum STAGE{
        START,
        LOADING,
        STABLE,
        UNLOADING,
        BATCHING
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
        nameInvoice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
                    values.put(InvoiceTable.KEY_NAME_AUTO, editable.toString());
                    invoiceTable.updateEntry(Integer.valueOf(entryID), values);
                }
            }
        });

        loadingInvoice = (EditText)view.findViewById(R.id.invoiceLoading);
        loadingInvoice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
                    loading = Integer.valueOf(editable.toString());
                }
            }
        });

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

        for(ActivityPreferences.KEY key : ActivityPreferences.KEY.values()){
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

    public void removeWeightOnClick(int weight, int id) {

        //ListView list = getListView();
        //int position = listInvoice.getPositionForView(view);
        //arrayList.remove(position);
        //vibrator.vibrate(50);
        //adapterWeightingList.notifyDataSetChanged();
        //TextView textView = (TextView)view.findViewById(R.id.topText);
        //int weight = Integer.valueOf(textView.getText().toString());
        auto-= weight;
        values.put(InvoiceTable.KEY_TOTAL_WEIGHT, auto);
        invoiceTable.updateEntry(Integer.valueOf(entryID), values);
        totalInvoice.setText(String.valueOf(auto));
        weighingTable.removeEntry((int) id);
    }

    public void addRowWeight(int weight){
        auto += weight;
        totalInvoice.setText(String.valueOf(auto));
        values.put(InvoiceTable.KEY_TOTAL_WEIGHT, auto);
        invoiceTable.updateEntry(Integer.valueOf(entryID), values);
        weighingTable.insertNewEntry(Integer.valueOf(entryID), weight);
        //adapterWeightingList.notifyDataSetChanged();
    }

    /** Обновляем данные листа загрузок. */
    private void updateListWeight() {
        Cursor cursor = weighingTable.getEntryInvoice(Integer.valueOf(entryID));
        if (cursor == null) {
            return;
        }

        int[] to = {R.id.bottomText, R.id.topText/*, R.id.buttonDelete*/};

        adapterWeightingList = new CustomSimpleCursorAdapter(getActivity(), R.layout.list_item_weight, cursor, WeighingTable.COLUMN_FOR_INVOICE, to, CursorAdapter.FLAG_AUTO_REQUERY);
        //namesAdapter = new MyCursorAdapter(this, R.layout.item_check, cursor, columns, to);
        //adapterWeightingList.setViewBinder(new ListWeightViewBinder());
        listInvoice.setAdapter(adapterWeightingList);
        /*listInvoice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = (TextView)view.findViewById(R.id.topText);
                int weight = Integer.valueOf(textView.getText().toString());
                auto-= weight;
                values.put(InvoiceTable.KEY_TOTAL_WEIGHT, auto);
                invoiceTable.updateEntry(Integer.valueOf(entryID), values);
                totalInvoice.setText(String.valueOf(auto));
                weighingTable.removeEntry((int) l);
            }
        });*/
    }

    class CustomSimpleCursorAdapter extends SimpleCursorAdapter{
        int layout;

        public CustomSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int... to) {
            super(context, layout, c, from, to);
            this.layout = layout;
        }

        public CustomSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            this.layout = layout;
        }

        /*@Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(layout, parent, false);
            }
            ImageView buttonDelete = (ImageView)view.findViewById(R.id.buttonDelete);
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                private int id = c.getInt(c.getColumnIndex(BaseColumns._ID));
                @Override
                public void onClick(View view) {
                    removeWeightOnClick(view);
                }
            });

            return view;
        }*/

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            //final Context t = context;
            final Cursor c = cursor;

            ImageView buttonDelete = (ImageView)view.findViewById(R.id.buttonDelete);
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                final int id = c.getInt(c.getColumnIndex(BaseColumns._ID));
                final int weight = c.getInt(c.getColumnIndex(WeighingTable.KEY_WEIGHT));
                @Override
                public void onClick(View view) {
                    removeWeightOnClick(weight, id);
                }
            });

        }
    }

    /*private class ListWeightViewBinder implements SimpleCursorAdapter.ViewBinder {
        private int direct;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            ImageView buttonDelete = (ImageView)view.findViewById(R.id.buttonDelete);
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeWeightOnClick(view);
                }
            });
            return true;
        }

        public void setViewText(TextView v, CharSequence text) {
            v.setText(text);
        }
    }*/

    private void doProcess(ObjectScales objectScales){
        switch (stage){
            /** Стадия загрузки ковша. */
            case LOADING:
                if (objectScales.getWeight() >= capture){
                    stage = STAGE.STABLE;
                    mListener.onEnableStable(true);
                }
            break;
            /** Стадия разгруски ковша. */
            case UNLOADING:
                if (objectScales.getWeight()< grab){
                    /** Разница то что высыпалось. */
                    int temp = grab - objectScales.getWeight();
                    /** Добавляем разницу в виртуальный кузов. */
                    grab_virtual += temp;
                    /** Очитаем из ковша разницу. */
                    grab -= temp;
                }
                if (stable && objectScales.getWeight() > 0){
                    stable = false;
                    addRowWeight(grab_virtual);
                    grab_virtual = 0;
                }else if(objectScales.getWeight() <= 0){
                    addRowWeight(grab_virtual);
                    stage = STAGE.LOADING;
                    mListener.onEnableStable(false);
                }
            break;
            /** Стадия дозирования. */
            case BATCHING:

            break;
            /** Стадия стабилизации ковша после загрузки. */
            case STABLE:
                if (stable){
                    /** Если вес больше или равно автозахвата. */
                    if (objectScales.getWeight() >= capture){
                        /** Сохранчем вес коша. */
                        grab = objectScales.getWeight();
                        grab_virtual = 0;
                        /** Если норма загрузки указана и вес авто и текущего ковша превышает норму загрузки.*/
                        if (loading != 0 && (grab + auto) > loading){
                            /** Стадия дозирования. */
                            stage = STAGE.BATCHING;
                        }else {
                            /** Стадия разгруски. */
                            stage = STAGE.UNLOADING;
                        }
                        /** Ложное срабатывание обратно в загрузку. */
                    }else {
                        /** Стадия загрузки. */
                        stage = STAGE.LOADING;
                    }
                    stable = false;
                }
            break;
            /** Определяем текущую стадию. */
            default:{
                if (objectScales.getWeight() <= 0){
                    stage = STAGE.LOADING;
                    mListener.onEnableStable(false);
                }else if (objectScales.getWeight() >= capture){
                    stage = STAGE.STABLE;
                    mListener.onEnableStable(true);
                }
            }
        }
    }

    /** Интерфейс обратного вызова. */
    public interface OnFragmentInvoiceListener {
        void onInvoiceClosePressedButton();
        void onEnableStable(boolean enable);
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
                ObjectScales obj = (ObjectScales) intent.getSerializableExtra(InterfaceModule.EXTRA_SCALES);
                if (obj == null)
                    return;
                switch (action) {
                    case InterfaceModule.ACTION_SCALES_RESULT:
                        doProcess(obj);
                        break;
                    case InterfaceModule.ACTION_WEIGHT_STABLE:
                        stable = true;
                        doProcess(obj);
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
