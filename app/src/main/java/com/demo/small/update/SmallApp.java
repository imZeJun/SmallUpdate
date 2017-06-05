package com.demo.small.update;

import android.app.Application;
import android.content.Context;

import net.wequick.small.Small;

public class SmallApp extends Application {

    private static Context sContext;

    public SmallApp() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        Small.preSetUp(this);
        SmallManager.getInstance().requestInitPlug();
    }

    public static Context getAppContext() {
        return sContext;
    }
}
