package eu.andret.kalendarzswiatnietypowych.persistence;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.LoadState;
import eu.andret.kalendarzswiatnietypowych.util.PreferenceHelper;

public class HolidayRemoteMediator {
	private static final String TAG = "Ferrio-HolidayRemoteMediator";
	private final Context applicationContext;
	private final AppDao appDao;
	private final ApiClient apiClient;
	private final MutableLiveData<LoadState> loadState = new MutableLiveData<>();
	private final AtomicBoolean inFlight = new AtomicBoolean(false);
	private final AtomicBoolean pendingRefresh = new AtomicBoolean(false);

	public HolidayRemoteMediator(@NonNull final Context context, @NonNull final AppDao appDao,
			@NonNull final ApiClient apiClient) {
		applicationContext = context.getApplicationContext();
		this.appDao = appDao;
		this.apiClient = apiClient;
	}

	public void refresh() {
		// Always record the request. If a refresh is already running, it will pick up the new
		// state (e.g. a freshly toggled adult-content pref) and re-run when it finishes instead
		// of silently dropping the request.
		pendingRefresh.set(true);
		if (!inFlight.compareAndSet(false, true)) {
			return;
		}
		runRefreshLoop();
	}

	private void runRefreshLoop() {
		pendingRefresh.set(false);
		loadState.postValue(LoadState.LOADING);
		final boolean includeAdult = new PreferenceHelper(applicationContext).showAdultContent();
		Log.d(TAG, "Refreshing holidays with includeMatureContent=" + includeAdult);
		CompletableFuture.runAsync(() -> {
			try {
				final List<Holiday> holidays = apiClient.getList(apiClient.buildHolidaysUrl(includeAdult), Holiday.class);
				Log.d(TAG, "Fetched " + holidays.size() + " holidays; "
						+ holidays.stream().filter(Holiday::isMatureContent).count() + " mature");
				if (!holidays.isEmpty()) {
					appDao.replaceAll(holidays);
					FerrioApplication.refreshWidgets(applicationContext);
				}
				loadState.postValue(LoadState.SUCCESS);
			} catch (final Exception ex) {
				Log.e(TAG, "Failed to refresh holidays", ex);
				loadState.postValue(LoadState.ERROR);
			} finally {
				if (pendingRefresh.get()) {
					runRefreshLoop();
				} else {
					inFlight.set(false);
				}
			}
		}, FerrioApplication.IO_EXECUTOR);
	}

	@NonNull
	public LiveData<LoadState> getLoadState() {
		return loadState;
	}
}
