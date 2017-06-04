package com.demo.small.update;

import android.app.Application;
import net.wequick.small.Small;

public class SmallApp extends Application {

    public SmallApp() {
        Small.preSetUp(this);
    }
}
