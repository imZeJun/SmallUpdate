package com.demo.small.update;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StubService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("StubService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("StubService", "onDestroy");
    }
}
