package eu.andret.kalendarzswiatnietypowych.widget;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;

public class MidnightWidgetRefreshWorker extends Worker {
	private static final String UNIQUE_WORK_NAME = "ferrio_midnight_widget_refresh";

	public MidnightWidgetRefreshWorker(@NonNull final Context context,
			@NonNull final WorkerParameters params) {
		super(context, params);
	}

	@NonNull
	@Override
	public Result doWork() {
		FerrioApplication.refreshWidgets(getApplicationContext());
		return Result.success();
	}

	public static void schedule(@NonNull final Context context) {
		final ZonedDateTime nextMidnight = LocalDate.now()
				.plusDays(1)
				.atTime(LocalTime.MIDNIGHT)
				.atZone(ZoneId.systemDefault());
		final long initialDelayMinutes = Math.max(1,
				Duration.between(ZonedDateTime.now(), nextMidnight).toMinutes());

		final PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
				MidnightWidgetRefreshWorker.class, 1, TimeUnit.DAYS)
				.setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
				.build();
		WorkManager.getInstance(context).enqueueUniquePeriodicWork(
				UNIQUE_WORK_NAME,
				ExistingPeriodicWorkPolicy.KEEP,
				request);
	}
}
