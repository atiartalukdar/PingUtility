package mobitsolutions.com.pingutility;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.RestrictTo;

import com.evernote.android.job.JobApi;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobManagerCreateException;

import bp.MyLocationService;

/**
 * A {@code BroadcastReceiver} rescheduling jobs after a reboot, if the underlying {@link JobApi} can't
 * handle it.
 *
 * @author rwondratschek
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class JobBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /*
         * Create the job manager. We may need to reschedule jobs and some applications aren't initializing the
         * manager in Application.onCreate(). It may happen that some jobs can't be created if the JobCreator
         * wasn't registered, yet. Apps / Libraries need to figure out how to solve this themselves.
         */

        System.out.println("BroadcastReceiverBroadcast--------------------ReceiverBroadcastReceiverBroadcastReceiver----------------BroadcastReceiver");

        try {
            System.out.println("Called on REBOOT");

            JobManager.create(context);

            Intent serviceIntent = new Intent(context, MyLocationService.class);
            context.startService(serviceIntent);

            NoteSyncJob.scheduleJob();
        } catch (JobManagerCreateException ignored) {
        }
    }
}
