package eu.andret.kalendarzswiatnietypowych.persistance;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import eu.andret.kalendarzswiatnietypowych.UHCApplication;
import eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar;
import eu.andret.kalendarzswiatnietypowych.util.Downloader;

public class UpdateDataWorker extends Worker {
	private final AppRepository repository;

	public UpdateDataWorker(@NonNull final Context context, @NonNull final WorkerParameters params) {
		super(context, params);
		repository = ((UHCApplication) context.getApplicationContext()).getAppRepository();
	}

	@NonNull
	@Override
	public Result doWork() {
		final UnusualCalendar calendar = new Downloader.UnusualCalendarDownloader().get();
		if (calendar != null) {
			repository.updateCalendarData(calendar);
			return Result.success();
		}
		return Result.failure();
	}
}
