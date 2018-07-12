package com.example.amit.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.widget.Toast;

// Generic fragment that hides the foreground notifications
public abstract class VisibleFragment extends Fragment {


    @Override
    public void onStart() {
        super.onStart();
        // Register a broadcast receiver dynamically with proper intent filter(i.e which kind of intent it should recieve)
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification,filter);
        }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the broadcast receiver
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    // create a broadcast receiver as an anymous class to register and unregister dynamically since it is vailid for this context only
    BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getActivity(),
                    "Got a broadcast:" + intent.getAction(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    };
}
