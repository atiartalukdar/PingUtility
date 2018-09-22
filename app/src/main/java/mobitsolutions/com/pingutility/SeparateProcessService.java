package mobitsolutions.com.pingutility;

import android.app.IntentService;
import android.content.Intent;

public class SeparateProcessService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SeparateProcessService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // do work
    }
}
