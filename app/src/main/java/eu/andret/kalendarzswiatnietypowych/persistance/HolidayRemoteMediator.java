package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.LoadState;

public class HolidayRemoteMediator {
	private final AppDao appDao;
	private final ApiClient apiClient;
	private final MutableLiveData<LoadState> loadState = new MutableLiveData<>();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public HolidayRemoteMediator(@NonNull final AppDao appDao, @NonNull final ApiClient apiClient) {
		this.appDao = appDao;
		this.apiClient = apiClient;
	}

	public void refresh() {
		loadState.postValue(LoadState.LOADING);
		CompletableFuture.runAsync(() -> {
			try {
				final List<Holiday> holidays = apiClient.getList(apiClient.buildHolidaysPath(), Holiday.class);
				if (!holidays.isEmpty()) {
					appDao.replaceAll(holidays);
				}
				loadState.postValue(LoadState.SUCCESS);
			} catch (final Exception ex) {
				loadState.postValue(LoadState.ERROR);
			}
		}, executor);
	}

	@NonNull
	public LiveData<LoadState> getLoadState() {
		return loadState;
	}
}
