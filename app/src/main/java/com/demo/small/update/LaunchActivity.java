package com.demo.small.update;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.wequick.small.Small;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Small.setUp(this, null);
    }

    public void startStubActivity(View view) {
        Small.openUri("upgrade", this);
    }

    public void loadUpgrade(View view) {
        SmallManager.getInstance().requestUpgrade();
    }
}
