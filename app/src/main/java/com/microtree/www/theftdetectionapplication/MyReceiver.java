package com.microtree.www.theftdetectionapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
            Toast.makeText(context, "Boot Completed", Toast.LENGTH_LONG).show();
            Intent pushIntent = new Intent(context, MyService.class);
            context.startService(pushIntent);
            Log.d("MZK_APP", "SERVICE Started");

//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            }, 30000);
//
        }else {
            Log.d("MZK_APP", "SERVICE not Started");
        }
    }
}
