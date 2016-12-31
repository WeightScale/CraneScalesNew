package com.konst.scaleslibrary;

import com.konst.scaleslibrary.module.Module;

/**
 * @author Kostya on 26.12.2016.
 */
public interface OnInteractionListener {

    void onUpdateSettings(Settings settings);
    void onScaleModuleCallback(Module obj);

}
