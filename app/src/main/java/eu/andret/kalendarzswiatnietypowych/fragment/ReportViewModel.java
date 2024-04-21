package eu.andret.kalendarzswiatnietypowych.fragment;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public class ReportViewModel extends ViewModel {
	private final MutableLiveData<Holiday> holiday = new MutableLiveData<>();

	public void setHoliday(final Holiday item) {
		holiday.setValue(item);
	}

	public LiveData<Holiday> getHoliday() {
		return holiday;
	}
}
