package com.kostya.cranescale;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import com.kostya.cranescale.provider.InvoiceTable;

import java.util.Date;

/**
 *
 */
public class FragmentInvoice extends Fragment implements View.OnClickListener {
    private InvoiceTable invoiceTable;
    protected ContentValues values = new ContentValues();
    private EditText invoiceDate, invoiceName, invoiceLoading, invoiceTotal;
    private ListView invoiceList;
    private static final String ARG_DATE = "date";
    private static final String ARG_PARAM2 = "param2";

    private String mParam2;

    private OnFragmentInvoiceListener mListener;

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice, null);

        invoiceDate = (EditText)view.findViewById(R.id.invoiceDate);
        invoiceDate.setText(values.getAsString(InvoiceTable.KEY_DATE_TIME_CREATE));

        invoiceName = (EditText)view.findViewById(R.id.invoiceName);
        invoiceLoading = (EditText)view.findViewById(R.id.invoiceLoading);
        invoiceList = (ListView) view.findViewById(R.id.invoiceList);
        invoiceTotal = (EditText)view.findViewById(R.id.invoiceTotal);
        view.findViewById(R.id.buttonCloseInvoice).setOnClickListener(this);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
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
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonCloseInvoice){
            onClosePressedButton();
        }
    }

    /**
     */
    public interface OnFragmentInvoiceListener {

        void onInvoiceClosePressedButton();
    }
}
