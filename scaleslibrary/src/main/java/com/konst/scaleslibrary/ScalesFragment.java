package com.konst.scaleslibrary;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.konst.scaleslibrary.module.InterfaceModule;
import com.konst.scaleslibrary.module.scale.ObjectScales;
import com.konst.scaleslibrary.module.scale.ScaleModule;
import com.konst.scaleslibrary.settings.ActivityProperties;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScalesFragment.OnInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScalesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScalesFragment extends Fragment implements View.OnClickListener {
    //private Context mContext;
    private ScaleModule scaleModule;
    private SpannableStringBuilder textKg;
    private ProgressBar progressBarStable;
    private ProgressBar progressBarWeight;
    private TextView weightTextView, textViewBattery, textViewTemperature;
    private ImageView imageViewWait, imageViewBluetooth;
    private Drawable dProgressWeight, dWeightDanger;
    private SimpleGestureFilter detectorWeightView;
    private Vibrator vibrator; //вибратор
    private BaseReceiver baseReceiver; //приёмник намерений
    private int moduleWeight;
    private boolean touchWeightView;
    private boolean weightViewIsSwipe;
    protected boolean isStable;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static OnInteractionListener onInteractionListener;

    public ScalesFragment(){
        // Required empty public constructor
    }

    protected void loadModule(ScaleModule scaleModule) {
        this.scaleModule = scaleModule;
    }

    public void setOnInteractionListener(OnInteractionListener listener){
        onInteractionListener = listener;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.     *
     * @return A new instance of fragment ScalesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScalesFragment newInstance(OnInteractionListener listener) {
        onInteractionListener = listener;
        ScalesFragment fragment = new ScalesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scales, null);
        progressBarWeight = (ProgressBar)view.findViewById(R.id.progressBarWeight);
        progressBarStable = (ProgressBar)view.findViewById(R.id.progressBarStable);
        weightTextView = (TextView)view.findViewById(R.id.weightTextView);
        //weightTextView.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stroke, 0, 0, 0);
        //weightTextView.setShadowLayer(10, 0, 0,   getResources().getColor(R.color.text));

        imageViewWait = (ImageView)view.findViewById(R.id.imageViewWait);

        view.findViewById(R.id.buttonSettings).setOnClickListener(this);

        textViewBattery = (TextView)view.findViewById(R.id.textBattery);
        textViewTemperature = (TextView)view.findViewById(R.id.textTemperature);

        setupWeightView();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement OnFragmentInteractionListener");
        }*/
        //mContext = activity;
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
        textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKg),0,textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        baseReceiver = new BaseReceiver(getActivity());
        baseReceiver.register();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {scaleModule.scalesProcessEnable(true);}catch (Exception e){}

    }

    @Override
    public void onPause() {
        super.onPause();
        try {scaleModule.scalesProcessEnable(false);}catch (Exception e){}
    }

    @Override
    public void onDetach() {
        super.onDetach();
        baseReceiver.unregister();
        onInteractionListener.detachScales();
        //onInteractionListener = null;
    }

    private void setupWeightView() {

        if (scaleModule != null){
            progressBarWeight.setMax(scaleModule.getMarginTenzo());
            progressBarWeight.setSecondaryProgress(scaleModule.getLimitTenzo());
            progressBarStable.setMax(ScaleModule.STABLE_NUM_MAX);
        }

        progressBarStable.setProgress(0);

        dProgressWeight = getResources().getDrawable(R.drawable.progress_weight);
        dWeightDanger = getResources().getDrawable(R.drawable.progress_weight_danger);

        SimpleGestureFilter.SimpleGestureListener weightViewGestureListener = new SimpleGestureFilter.SimpleGestureListener() {
            @Override
            public void onSwipe(int direction) {

                switch (direction) {
                    case SimpleGestureFilter.SWIPE_RIGHT:
                    //case SimpleGestureFilter.SWIPE_LEFT:
                        weightViewIsSwipe = true;
                        getActivity().sendBroadcast(new Intent(InterfaceModule.ACTION_WEIGHT_STABLE));
                        break;
                    case SimpleGestureFilter.SWIPE_DOWN:
                        openSearch();
                        break;
                    default:
                }
            }

            @Override
            public void onDoubleTap() {
                progressBarStable.setProgress(0);
                vibrator.vibrate(100);
                new ZeroThread(getActivity()).start();
            }
        };

        detectorWeightView = new SimpleGestureFilter(getActivity(), weightViewGestureListener);
        detectorWeightView.setSwipeMinVelocity(50);
        weightTextView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detectorWeightView.setSwipeMaxDistance(v.getMeasuredWidth());
                detectorWeightView.setSwipeMinDistance(detectorWeightView.getSwipeMaxDistance() / 3);
                detectorWeightView.onTouchEvent(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        touchWeightView = true;
                        //vibrator.vibrate(5);
                        //int progress = (int) (event.getX() / (detectorWeightView.getSwipeMaxDistance() / progressBarStable.getMax()));
                        //progressBarStable.setProgress(progress);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        //progressBarStable.setProgress(0);
                        touchWeightView = false;
                        break;
                    default:
                }
                return false;
            }
        });
        /*weightTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                openSearch();
                return false;
            }
        });*/
    }

    private void openSearch(){
        onInteractionListener.openSearchDialog("Выбор устройства для соединения");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.buttonSettings) {
            startActivity(new Intent(getActivity(), ActivityProperties.class));
        }
    }

    /**
     * Обработка обнуления весов.
     */
    private class ZeroThread extends Thread {
        private final ProgressDialog dialog;

        ZeroThread(Context context) {
            // Создаём новый поток
            super(getString(R.string.Zeroing));
            dialog = new ProgressDialog(getActivity());
            dialog.setCancelable(false);
            dialog.setIndeterminate(false);
            dialog.show();
            dialog.setContentView(R.layout.zeroing_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            TextView tv1 = (TextView) dialog.findViewById(R.id.textView1);
            tv1.setText(R.string.Zeroing);
        }

        @Override
        public void run() {
            scaleModule.setOffsetScale();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
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
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(InterfaceModule.ACTION_SCALES_RESULT);
            intentFilter.addAction(InterfaceModule.ACTION_WEIGHT_STABLE);
        }

        @Override
        public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        switch (BluetoothAdapter.getDefaultAdapter().getState()) {
                            case BluetoothAdapter.STATE_OFF:
                                Toast.makeText(mContext, R.string.bluetooth_off, Toast.LENGTH_SHORT).show();
                                new Internet(mContext).turnOnWiFiConnection(false);
                                BluetoothAdapter.getDefaultAdapter().enable();
                                break;
                            case BluetoothAdapter.STATE_TURNING_ON:
                                Toast.makeText(mContext, R.string.bluetooth_turning_on, Toast.LENGTH_SHORT).show();
                                break;
                            case BluetoothAdapter.STATE_ON:
                                Toast.makeText(mContext, R.string.bluetooth_on, Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;
                        }
                        break;
                    case InterfaceModule.ACTION_SCALES_RESULT:
                        ObjectScales obj = (ObjectScales) intent.getSerializableExtra(InterfaceModule.EXTRA_SCALES);
                        if (obj == null)
                            return;
                        moduleWeight = obj.getWeight();
                        final String textWeight = String.valueOf(moduleWeight);
                        /** Обновляем прогресс стабилизации веса. */
                        progressBarStable.setProgress(obj.getStableNum());
                        //handler.obtainMessage(Action.UPDATE_PROGRESS.ordinal(), obj.getStableNum(), 0).sendToTarget();
                        switch (obj.getResultWeight()) {
                            case WEIGHT_NORMAL:
                                w = new SpannableStringBuilder(textWeight);
                                w.setSpan(new ForegroundColorSpan(Color.WHITE), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                w.append(textKg);
                                progressBarWeight.setProgress(obj.getTenzoSensor());
                                bounds = progressBarWeight.getProgressDrawable().getBounds();
                                progressBarWeight.setProgressDrawable(dProgressWeight);
                                progressBarWeight.getProgressDrawable().setBounds(bounds);
                                break;
                            case WEIGHT_LIMIT:
                                w = new SpannableStringBuilder(textWeight);
                                w.setSpan(new ForegroundColorSpan(Color.RED), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                w.append(textKg);
                                progressBarWeight.setProgress(obj.getTenzoSensor());
                                bounds = progressBarWeight.getProgressDrawable().getBounds();
                                progressBarWeight.setProgressDrawable(dWeightDanger);
                                progressBarWeight.getProgressDrawable().setBounds(bounds);
                                break;
                            case WEIGHT_MARGIN:
                                w = new SpannableStringBuilder(String.valueOf(moduleWeight));
                                w.setSpan(new ForegroundColorSpan(Color.RED), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                progressBarWeight.setProgress(obj.getTenzoSensor());
                                vibrator.vibrate(100);
                                break;
                            case WEIGHT_ERROR:
                                w = new SpannableStringBuilder("- - -");
                                w.setSpan(new ForegroundColorSpan(Color.RED), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                                moduleWeight = 0;
                                progressBarWeight.setProgress(0);
                                break;
                            default:
                        }
                        weightTextView.setText(w, TextView.BufferType.SPANNABLE);
                        textViewTemperature.setText(obj.getTemperature() + "°C");
                        textViewBattery.setText(obj.getBattery() + "%");
                        textViewBattery.setTextColor(Color.WHITE);
                        if (obj.getBattery() > 90) {
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_full, 0, 0, 0);
                        } else if (obj.getBattery() > 80){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_90, 0, 0, 0);
                        } else if (obj.getBattery() > 60){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_80, 0, 0, 0);
                        } else if (obj.getBattery() > 50){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_60, 0, 0, 0);
                        } else if (obj.getBattery() > 30){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_50, 0, 0, 0);
                        } else if (obj.getBattery() > 20){
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_30, 0, 0, 0);
                        } else if (obj.getBattery() >= 0) {
                            //textViewBattery.setText(obj.getBattery() + "%");
                            textViewBattery.setTextColor(Color.RED);
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_20, 0, 0, 0);
                        }else {
                            textViewBattery.setText("нет данных!!!");
                            textViewBattery.setTextColor(Color.BLUE);
                            textViewBattery.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_battery_20, 0, 0, 0);
                        }
                        //    }
                        break;
                    /*case InterfaceModule.ACTION_WEIGHT_STABLE:
                        isStable = true;
                        handler.obtainMessage(Action.STORE_WEIGHTING.ordinal(), moduleWeight, 0).sendToTarget();                 //сохраняем стабильный вес
                        break;*/
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

    /**
     * Интерфейс обратного вызова.
     */
    protected interface OnInteractionListener {
        void openSearchDialog(String msg);
        void detachScales();
    }
}
