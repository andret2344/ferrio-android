package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import java.util.List;
import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.UHCApplication;
import eu.andret.kalendarzswiatnietypowych.entity.FloatingHoliday;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;

public class SharedViewModel extends ViewModel {
	public static final ViewModelInitializer<SharedViewModel> INITIALIZER = new ViewModelInitializer<>(SharedViewModel.class, creationExtras -> {
		final UHCApplication uhcApplication = (UHCApplication) creationExtras.get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY);
		assert uhcApplication != null;
		return new SharedViewModel(uhcApplication.getAppRepository());
	});
	private final AppRepository repository;

	public SharedViewModel(final AppRepository repository) {
		this.repository = repository;
	}

	public LiveData<Optional<HolidayDay>> getHolidayDay(final int month, final int day) {
		return repository.getHolidayDay(month, day);
	}

	public LiveData<List<HolidayDay>> getHolidayDays(final int monthFrom, final int dayFrom, final int monthTo, final int dayTo) {
		return repository.getHolidayDays(monthFrom, dayFrom, monthTo, dayTo);
	}

	public LiveData<List<HolidayDay>> getAllHolidayDays() {
		return repository.getAllHolidayDays();
	}

	public void insertHolidays(final List<Holiday> holidays) {
		repository.insertHolidays(holidays);
	}

	public void insertFloatingHoliday(final List<FloatingHoliday> floatingHolidays) {
		repository.insertFloatingHoliday(floatingHolidays);
	}

	public void insertHolidayDays(final List<HolidayDay> holidayDays) {
		repository.insertHolidayDays(holidayDays);
	}
}
