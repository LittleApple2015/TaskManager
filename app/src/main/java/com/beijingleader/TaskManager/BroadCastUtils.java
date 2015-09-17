package com.beijingleader.TaskManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.sip.SipSession;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by apple on 2015/9/7.
 */
public class BroadCastUtils extends BroadcastReceiver {
    NetworkInfo.State wifiState = null;
    NetworkInfo.State mobileState = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        //获取手机的连接服务管理器，这里是连接管理器类
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);//获取系统服务
        wifiState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();//得到wifi网络状态
        mobileState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();//得到GPRS网络状态

        // Intent connectivityManagerIntent = new Intent(context, LoginActivity.class);
        if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED == mobileState) {
            //wifiState mobileState 都不为空 网络状态等于mobileState
            Toast.makeText(context, "GPRS网络连接成功！", Toast.LENGTH_SHORT).show();
        } else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED == wifiState && NetworkInfo.State.CONNECTED != mobileState) {
            //wifiState mobileState 都不为空 网络状态等于wifiState
            Toast.makeText(context, "wifi网络连接成功！", Toast.LENGTH_SHORT).show();
        } else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED != mobileState) {
            //wifiState mobileState 都不为空 网络状态不等于wifiState 也不等于mobileState
            Toast.makeText(context, "世界上最遥远的距离是没有网络，请打开2G/3G/4G网络....", Toast.LENGTH_SHORT).show();
        }
    }
}
