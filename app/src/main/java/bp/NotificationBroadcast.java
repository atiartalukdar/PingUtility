package bp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mobitsolutions.com.pingutility.NoteSyncJob;

public class NotificationBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("BroadcastReceiverBroadcast--------------------ReceiverBroadcastReceiverBroadcastReceiver----------------BroadcastReceiver");
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                    System.out.println("Called on REBOOT");
                    // start a new service
                    Intent serviceIntent = new Intent(context, MyLocationService.class);
                    context.startService(serviceIntent);
                    NoteSyncJob.scheduleJob();

                    break;
                default:
                    break;
            }
        }
    }
}
