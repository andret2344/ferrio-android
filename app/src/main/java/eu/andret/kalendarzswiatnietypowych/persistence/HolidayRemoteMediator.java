package eu.andret.kalendarzswiatnietypowych.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.LoadState;

public class HolidayRemoteMediator {
	private static final String TAG = "Ferrio-HolidayRemoteMediator";
	private final Context applicationContext;
	private final AppDao appDao;
	private final ApiClient apiClient;
	private final MutableLiveData<LoadState> loadState = new MutableLiveData<>();

	public HolidayRemoteMediator(@NonNull final Context context, @NonNull final AppDao appDao,
			@NonNull final ApiClient apiClient) {
		applicationContext = context.getApplicationContext();
		this.appDao = appDao;
		this.apiClient = apiClient;
	}

	public void refresh() {
		loadState.postValue(LoadState.LOADING);
		CompletableFuture.runAsync(() -> {
			try {
				final List<Holiday> holidays = apiClient.getList(apiClient.buildHolidaysUrl(), Holiday.class);
				if (!holidays.isEmpty()) {
					appDao.replaceAll(holidays);
					FerrioApplication.refreshWidgets(applicationContext);
				}
				loadState.postValue(LoadState.SUCCESS);
			} catch (final Exception ex) {
				Log.e(TAG, "Failed to refresh holidays", ex);
				loadState.postValue(LoadState.ERROR);
			}
		}, FerrioApplication.IO_EXECUTOR);
	}

	@NonNull
	public LiveData<LoadState> getLoadState() {
		return loadState;
	}
}
