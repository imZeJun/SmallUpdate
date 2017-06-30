package com.demo.small.update;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

public class ServiceSampleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_sample);
        ServiceStubManager.getInstance().setup(this);
    }

    public void startService1(View view) {
        Intent intent = new Intent(this, StubService.class);
        startService(intent);
    }

}
