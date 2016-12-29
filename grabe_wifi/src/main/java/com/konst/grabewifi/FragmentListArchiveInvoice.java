package com.konst.grabewifi;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.konst.grabewifi.provider.InvoiceTable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Kostya on 10.12.2016.
 */
public class FragmentListArchiveInvoice extends Fragment {
    private SimpleCursorAdapter simpleCursorAdapter;
    private InvoiceTable invoiceTable;
    private TextView dateText, totalWeight;
    private RecyclerView recyclerView;
    //ListView listView;
    private String date, total;
    private static final String ARG_DATE = "arg_date";
    private static final String ARG_TOTAL = "arg_total";

    /**
     * @return A new instance of fragment InvoiceFragment.
     */
    public static FragmentListArchiveInvoice newInstance(String date, String total) {
        FragmentListArchiveInvoice fragment = new FragmentListArchiveInvoice();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        args.putString(ARG_TOTAL, total);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = getArguments().getString(ARG_DATE);
            total = getArguments().getString(ARG_TOTAL);
        }else {
            date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
            total = "";
        }
        invoiceTable = new InvoiceTable(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_archive_invoice, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.blank);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        dateText = (TextView)view.findViewById(R.id.date_archive_invoice);
        dateText.setText(date);

        totalWeight = (TextView)view.findViewById(R.id.totalWeight);
        SpannableStringBuilder w = new SpannableStringBuilder(total);
        w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
        textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgListInvoice), 0, textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        w.append(textKg);
        totalWeight.setText(w, TextView.BufferType.SPANNABLE);

        //listView = (ListView)view.findViewById(R.id.list);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateRecyclerView();

    }

    private void updateRecyclerView() {
        Cursor cursor = invoiceTable.getToday(date);
        if (cursor == null) {
            return;
        }

        recyclerView.setAdapter(new CursorRecyclerViewAdapter(getActivity(), cursor) {
            @Override
            public void onBindViewHolder(CursorRecyclerViewAdapter.InvoiceHolder viewHolder, Cursor cursor) {
                viewHolder._id.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_ID))));
                viewHolder.date.setText(date);
                viewHolder.number.setText(String.valueOf(cursor.getString(cursor.getColumnIndex(InvoiceTable.KEY_NAME_AUTO))));
                int ready = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                int send = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                if (ready == InvoiceTable.UNREADY){
                    viewHolder.ready.setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                }else if(send == InvoiceTable.READY){
                    viewHolder.ready.setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_check));
                }else  {
                    viewHolder.ready.setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice));
                }
                SpannableStringBuilder w = new SpannableStringBuilder(cursor.getString(cursor.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT)));
                w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
                textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgListInvoice), 0, textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                w.append(textKg);
                viewHolder.weight.setText(w, TextView.BufferType.SPANNABLE);
                //((TextView)view).setText(w, TextView.BufferType.SPANNABLE);
            }
        });

    }

    /*private class ListInvoiceViewBinder implements SimpleCursorAdapter.ViewBinder {
        private int ready, send;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (view.getId()){
                case R.id.imageReady:
                    ready = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_READY));
                    send = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_IS_CLOUD));
                    if (ready == InvoiceTable.UNREADY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_red));
                    }else if(send == InvoiceTable.READY){
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice_check));
                    }else  {
                        ((ImageView)view).setImageDrawable(getResources().getDrawable(R.drawable.ic_invoice));
                    }
                break;
                case R.id.weight_row:
                    SpannableStringBuilder w = new SpannableStringBuilder(cursor.getString(cursor.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT)));
                    w.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.background2)), 0, w.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    SpannableStringBuilder textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
                    textKg.setSpan(new TextAppearanceSpan(getActivity(), R.style.SpanTextKgListInvoice), 0, textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    w.append(textKg);
                    ((TextView)view).setText(w, TextView.BufferType.SPANNABLE);
                break;
                default:
                    return false;
            }
            return true;
        }
    }*/

    protected abstract class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.InvoiceHolder> {

        private Context mContext;
        private Cursor mCursor;
        private boolean mDataValid;
        private int mRowIdColumn;
        private DataSetObserver mDataSetObserver;

        protected CursorRecyclerViewAdapter(Context context, Cursor cursor) {
            mContext = context;
            mCursor = cursor;
            mDataValid = cursor != null;
            mRowIdColumn = mDataValid ? mCursor.getColumnIndex("_id") : -1;
            /*mDataSetObserver = new NotifyingDataSetObserver();
            if (mCursor != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }*/
        }

        public Cursor getCursor() {
            return mCursor;
        }

        @Override
        public int getItemCount() {
            if (mDataValid && mCursor != null) {
                return mCursor.getCount();
            }
            return 0;
        }

        @Override
        public long getItemId(int position) {
            if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIdColumn);
            }
            return 0;
        }

        public abstract void onBindViewHolder(CursorRecyclerViewAdapter.InvoiceHolder viewHolder, Cursor cursor);

        @Override
        public void onBindViewHolder(CursorRecyclerViewAdapter.InvoiceHolder viewHolder, int position) {
            if (!mDataValid) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
            onBindViewHolder(viewHolder, mCursor);
        }

        @Override
        public InvoiceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_archive_invoice, parent, false);
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_invoice, parent, false);
            InvoiceHolder vh = new InvoiceHolder(itemView);
            return vh;
        }

        public class InvoiceHolder extends RecyclerView.ViewHolder {
            private TextView _id;
            private TextView date;
            private TextView number;
            private TextView weight;
            private ImageView ready;


            public InvoiceHolder(View view) {
                super(view);
                _id = (TextView) view.findViewById(R.id.id_row);
                date = (TextView) view.findViewById(R.id.date_row);
                number = (TextView) view.findViewById(R.id.number_row);
                weight = (TextView)view.findViewById(R.id.weight_row);
                ready = (ImageView) view.findViewById(R.id.imageReady);
            }
        }

        private class NotifyingDataSetObserver extends DataSetObserver {
            @Override
            public void onChanged() {
                super.onChanged();
                mDataValid = true;
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                mDataValid = false;
                notifyDataSetChanged();
                //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
            }
        }
    }
}
