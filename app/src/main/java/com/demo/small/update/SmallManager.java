package com.demo.small.update;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import net.wequick.small.Bundle;
import net.wequick.small.Small;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SmallManager {

    private static final String TAG = "SmallManager";
    private static final String PATCH_PATH = Environment.getExternalStorageDirectory().toString() + "/Small/bundle.json";

    private Handler mWorkerHandler;
    private Handler mUiHandler;

    private SmallManager() {
        HandlerThread workThread = new HandlerThread("workThread");
        workThread.start();
        mWorkerHandler = new MHandler(workThread.getLooper());
        mUiHandler = new MHandler(Looper.getMainLooper());
    }

    public void requestUpgrade() {
        mWorkerHandler.sendEmptyMessage(MHandler.MSG_REQ_UPGRADE);
    }

    private void onUpgraded(boolean success) {
        Log.d(TAG, "onUpgraded, success=" + success);
    }

    private void upgrade() {
        Log.d(TAG, "upgrade");
        try {
            //1.获取插件更新信息。
            UpgradeInfo upgradeInfo = getUpgradeInfo();
            if (upgradeInfo != null) {
                if (upgradeInfo.manifest != null) {
                    if (!Small.updateManifest(upgradeInfo.manifest, false)) {
                        mUiHandler.sendMessage(mUiHandler.obtainMessage(MHandler.MSG_UPGRADE_FINISHED, false));
                        return;
                    }
                }
                List<UpdateInfo> updates = upgradeInfo.updates;
                for (UpdateInfo update : updates) {
                    //2.获得对应包名的Bundle
                    Bundle bundle = Small.getBundle(update.packageName);
                    //3.利用更新信息来更新对应包名的Bundle。
                    boolean result = upgradeBundle(bundle, update);
                    Log.d(TAG, "pkg=" + update.packageName + ",result=" + result);
                }
                mUiHandler.sendMessage(mUiHandler.obtainMessage(MHandler.MSG_UPGRADE_FINISHED, true));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUiHandler.sendMessage(mUiHandler.obtainMessage(MHandler.MSG_UPGRADE_FINISHED, false));
    }

    private UpgradeInfo getUpgradeInfo() {
        try {
            File patchFile = new File(PATCH_PATH);
            //获得新的bundle.json，这实际情况下可以通过访问网络进行下载的方式实现，这里我们用读取外部存储的方式代替，原理是一样的。
            FileInputStream fileInputStream = new FileInputStream(patchFile);
            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, length));
            }
            JSONObject jo = new JSONObject(sb.toString());
            //manifest部分的更新。
            JSONObject mf = jo.has("manifest") ? jo.getJSONObject("manifest") : null;
            //每个插件部分的更新，包含了包名以及新的插件so路径。
            JSONArray updateJson = jo.getJSONArray("updates");
            int N = updateJson.length();
            List<UpdateInfo> updates = new ArrayList<>(N);
            for (int i = 0; i < N; i++) {
                JSONObject o = updateJson.getJSONObject(i);
                UpdateInfo info = new UpdateInfo();
                info.packageName = o.getString("pkg");
                info.soPath = o.getString("url");
                updates.add(info);
            }
            //将更新的信息封装起来，用于下一步的更新操作。
            UpgradeInfo ui = new UpgradeInfo();
            ui.manifest = mf;
            ui.updates = updates;
            return ui;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean upgradeBundle(Bundle origin, UpdateInfo updateInfo) {
        try {
            //获得Bundle的PatchFile。
            File file = origin.getPatchFile();
            //获取新的so，也就是新的插件，实际情况下可以通过访问网络进行下载的方式实现，这里我们用读取外部存储的方式代替，原理是一样的。
            File updateFile = new File(Environment.getExternalStorageDirectory().toString() + "/Small/" + updateInfo.soPath);
            FileInputStream updateFileInput = new FileInputStream(updateFile);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            //将更新的插件so，写入到Bundle的PatchFile。
            while ((length = updateFileInput.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            updateFileInput.close();
            //调用Bundle的upgrade方法。
            origin.upgrade();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static class UpdateInfo {
        private String packageName;
        private String soPath;
    }

    private static class UpgradeInfo {
        private JSONObject manifest;
        private List<UpdateInfo> updates;
    }

    private class MHandler extends Handler {

        private static final int MSG_REQ_UPGRADE = 0;
        private static final int MSG_UPGRADE_FINISHED = 1;

        public MHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REQ_UPGRADE:
                    upgrade();
                    break;
                case MSG_UPGRADE_FINISHED:
                    onUpgraded((boolean) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    public static SmallManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static SmallManager INSTANCE = new SmallManager();
    }
}
