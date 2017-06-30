package com.demo.small.update;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ServiceStubManager {

    public static ServiceStubManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final ServiceStubManager INSTANCE = new ServiceStubManager();
    }

    public void setup(Context context) {
        try {
            //第一步：通过反射获取到ActivityManagerNative类。
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Method getDefault = activityManagerNativeClass.getMethod("getDefault");
            Object gDefault = getDefault.invoke(null);

            //获取mInstance变量。
            Class<?> singleton = Class.forName("android.util.Singleton");
            Field instanceField = singleton.getDeclaredField("mInstance");
            instanceField.setAccessible(true);

            //获取IActivityManager类。
            Class<?> iActivityManager = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(context.getClassLoader(), new Class[]{iActivityManager}, new InvocationHandler() {

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Log.d("ServiceStubManager", "call invoke, methodName=" + method.getName());
                    return method.invoke(proxy, args);
                }

            });
            instanceField.set(gDefault, proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
