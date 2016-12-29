package com.konst.grabewifi.settings;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.konst.grabewifi.R;

/**
 *  @author Kostya 16.12.2016.
 */
public class AdMobPreference extends Preference {
    AdView mAdView;

    public AdMobPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        // this will create the linear layout defined in ads_layout.xml
        View view = super.onCreateView(parent);

        /*// the context is a PreferenceActivity
        Activity activity = (Activity)getContext();

        // Create the adView
        AdView adView = new AdView(activity, AdSize.BANNER, "ca-app-pub-5128519816521867/4047819830");

        ((LinearLayout)view).addView(adView);

        // Initiate a generic request to load it with an ad
        AdRequest request = new AdRequest();
        adView.loadAd(request);*/
        setupBanner(view);

        return view;
    }

    private void setupBanner(View view){
        AdView mAdView = (AdView)view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //.addTestDevice(Globals.getInstance().getDeviceId())
                .build();
        mAdView.loadAd(adRequest);
    }
}
