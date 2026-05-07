package eu.andret.kalendarzswiatnietypowych.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import eu.andret.kalendarzswiatnietypowych.BuildConfig;

public class WidgetUpdateWorker extends Worker {
	private static final String TAG = "Ferrio-WidgetUpdateWorker";

	static final String KEY_WIDGET_IDS = "widget_ids";
	static final String KEY_LAYOUT_RES_ID = "layout_res_id";
	private static final String UNIQUE_NAME_PREFIX = "widget_update_";

	public WidgetUpdateWorker(@NonNull final Context context,
			@NonNull final WorkerParameters params) {
		super(context, params);
	}

	@NonNull
	@Override
	public Result doWork() {
		final Context context = getApplicationContext();
		final AppWidgetManager manager = AppWidgetManager.getInstance(context);
		final int[] ids = getInputData().getIntArray(KEY_WIDGET_IDS);
		final int layoutResId = getInputData().getInt(KEY_LAYOUT_RES_ID, 0);
		if (ids == null || layoutResId == 0) {
			return Result.failure();
		}

		for (final int id : ids) {
			try {
				if (BuildConfig.DEBUG && "W3".equals(BaseWidgetProvider.DEBUG_FORCE_ERROR)) {
					throw new RuntimeException("Debug: forced W3 error");
				}
				BaseWidgetProvider.renderDataState(context, manager, id, layoutResId);
			} catch (final Exception ex) {
				Log.e(TAG, "Failed to render widget data state", ex);
				BaseWidgetProvider.renderErrorState(context, manager, id, layoutResId, "W3");
			}
		}
		return Result.success();
	}

	static void enqueue(@NonNull final Context context, @NonNull final int[] widgetIds,
			final int layoutResId) {
		if (widgetIds.length == 0) {
			return;
		}
		final Data inputData = new Data.Builder()
				.putIntArray(KEY_WIDGET_IDS, widgetIds)
				.putInt(KEY_LAYOUT_RES_ID, layoutResId)
				.build();
		final OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WidgetUpdateWorker.class)
				.setInputData(inputData)
				.build();
		WorkManager.getInstance(context).enqueueUniqueWork(
				UNIQUE_NAME_PREFIX + layoutResId,
				ExistingWorkPolicy.REPLACE,
				request);
	}
}
