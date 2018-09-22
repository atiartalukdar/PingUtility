package mobitsolutions.com.pingutility;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import bp.MyLocationService;

public class SeparateProcessJob extends Job {
    @Override
    @NonNull
    protected Result onRunJob(final Params params) {
        Intent intent = new Intent(getContext(), MyLocationService.class);
        getContext().startService(intent);
        return Result.SUCCESS;
    }
}

