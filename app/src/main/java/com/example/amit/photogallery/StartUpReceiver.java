package com.example.amit.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


// Broadcast receiver that listens to the boot up

public class StartUpReceiver extends BroadcastReceiver {

    // The method is executed when an appropriate broadcast(As per registered properties) is received
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("Received Broadcast:",intent.getAction());

        // check the alarm state and fire the PollService to work in background on boot
        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);

    }


}
