package eu.andret.kalendarzswiatnietypowych.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

/**
 * Holds a {@link ListFragment} fetch result across configuration changes so rotation does not
 * trigger a refetch. The fragment marks {@link #isFetched()} after either a successful response
 * or a terminal error; on transient failures the user can retry by re-entering the screen, which
 * recreates the fragment and the ViewModel together.
 */
public class ListFragmentViewModel extends ViewModel {
	private final MutableLiveData<List<?>> data = new MutableLiveData<>();
	private boolean fetched;

	@NonNull
	public LiveData<List<?>> getData() {
		return data;
	}

	public boolean isFetched() {
		return fetched;
	}

	public void setData(@Nullable final List<?> list) {
		fetched = true;
		data.setValue(list);
	}

	public void markFetched() {
		fetched = true;
	}
}
