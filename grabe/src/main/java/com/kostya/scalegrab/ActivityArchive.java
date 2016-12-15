package com.kostya.scalegrab;


import android.content.Context;
import android.database.Cursor;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import com.kostya.scalegrab.provider.InvoiceTable;

import java.util.HashMap;

public class ActivityArchive extends AppCompatActivity {
    //private SectionsPagerAdapter mSectionsPagerAdapter;
    private CursorFragmentPagerAdapter cursorFragmentPagerAdapter;
    private InvoiceTable invoiceTable;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        invoiceTable = new InvoiceTable(this);
        Cursor cursor = invoiceTable.getAllGroupDate();
        if (cursor == null) {
            return;
        }

        /*mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(),cursor) {
            @Override
            public Fragment getItem(Context context, Cursor cursor) {
                int i = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_ID));
                return PlaceholderFragment.newInstance(i);
            }
        };*/
        cursorFragmentPagerAdapter = new CursorFragmentPagerAdapter(this, getSupportFragmentManager(), cursor) {
            @Override
            public Fragment getItem(Context context, Cursor cursor) {
                String d = cursor.getString(cursor.getColumnIndex(InvoiceTable.KEY_DATE_CREATE));
                int w = cursor.getInt(cursor.getColumnIndex(InvoiceTable.KEY_TOTAL_WEIGHT));
                return FragmentListArchiveInvoice.newInstance(d, String.valueOf(w));
            }
        };

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(cursorFragmentPagerAdapter);
        //mViewPager.setAdapter(mSectionsPagerAdapter);

        //TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        //tabLayout.setupWithViewPager(mViewPager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_archive, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    /*public static class PlaceholderFragment extends Fragment {
        *//**
         * The fragment argument representing the section number for this
         * fragment.
         *//*
        private static final String ARG_DATE = "arg_date";

        public PlaceholderFragment() {
        }

        *//**
         * Returns a new instance of this fragment for the given section
         * number.
         *//*
        public static PlaceholderFragment newInstance(String date) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_DATE, date);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_archive, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getArguments().getString(ARG_DATE));
            return rootView;
        }
    }*/

    public abstract class CursorFragmentPagerAdapter extends FragmentPagerAdapter {

        protected boolean mDataValid;
        protected Cursor mCursor;
        protected Context mContext;
        protected SparseIntArray mItemPositions;
        protected HashMap<Object, Integer> mObjectMap;
        protected int mRowIDColumn;

        public CursorFragmentPagerAdapter(Context context, FragmentManager fm, Cursor cursor) {
            super(fm);

            init(context, cursor);
        }

        void init(Context context, Cursor c) {
            mObjectMap = new HashMap<Object, Integer>();
            boolean cursorPresent = c != null;
            mCursor = c;
            mDataValid = cursorPresent;
            mContext = context;
            mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
        }

        public Cursor getCursor() {
            return mCursor;
        }

        @Override
        public int getItemPosition(Object object) {
            Integer rowId = mObjectMap.get(object);
            if (rowId != null && mItemPositions != null) {
                return mItemPositions.get(rowId, POSITION_NONE);
            }
            return POSITION_NONE;
        }

        public void setItemPositions() {
            mItemPositions = null;

            if (mDataValid) {
                int count = mCursor.getCount();
                mItemPositions = new SparseIntArray(count);
                mCursor.moveToPosition(-1);
                while (mCursor.moveToNext()) {
                    int rowId = mCursor.getInt(mRowIDColumn);
                    int cursorPos = mCursor.getPosition();
                    mItemPositions.append(rowId, cursorPos);
                }
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (mDataValid) {
                mCursor.moveToPosition(position);
                return getItem(mContext, mCursor);
            } else {
                return null;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mObjectMap.remove(object);

            super.destroyItem(container, position, object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (!mDataValid) {
                throw new IllegalStateException("this should only be called when the cursor is valid");
            }
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }

            int rowId = mCursor.getInt(mRowIDColumn);
            Object obj = super.instantiateItem(container, position);
            mObjectMap.put(obj, rowId);

            return obj;
        }

        public abstract Fragment getItem(Context context, Cursor cursor);

        @Override
        public int getCount() {
            if (mDataValid) {
                return mCursor.getCount();
            } else {
                return 0;
            }
        }

        public void changeCursor(Cursor cursor) {
            Cursor old = swapCursor(cursor);
            if (old != null) {
                old.close();
            }
        }

        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor == mCursor) {
                return null;
            }
            Cursor oldCursor = mCursor;
            mCursor = newCursor;
            if (newCursor != null) {
                mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
                mDataValid = true;

            } else {
                mRowIDColumn = -1;
                mDataValid = false;
            }

            setItemPositions();
            if (mDataValid){
                notifyDataSetChanged();
            }


            return oldCursor;
        }

    }
}