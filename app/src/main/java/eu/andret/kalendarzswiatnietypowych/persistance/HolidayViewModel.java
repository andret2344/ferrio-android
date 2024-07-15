package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.UHCApplication;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;

public class HolidayViewModel extends ViewModel {
	public static final ViewModelInitializer<HolidayViewModel> INITIALIZER = new ViewModelInitializer<>(HolidayViewModel.class, creationExtras -> {
		final UHCApplication uhcApplication = (UHCApplication) creationExtras.get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY);
		assert uhcApplication != null;
		return new HolidayViewModel(uhcApplication.getAppRepository());
	});
	private final AppRepository repository;

	public HolidayViewModel(final AppRepository repository) {
		this.repository = repository;
	}

	public LiveData<Optional<HolidayDay>> getHolidayDay(final int month, final int day) {
		return repository.getHolidayDay(month, day);
	}

	public LiveData<List<HolidayDay>> getHolidayDays(@NonNull final LocalDate from, @NonNull final LocalDate to) {
		return repository.getHolidayDays(from.getMonthValue(), from.getDayOfMonth(), to.getMonthValue(), to.getDayOfMonth());
	}

	public LiveData<List<HolidayDay>> getAllHolidayDays() {
		return repository.getAllHolidayDays();
	}

	public LiveData<Holiday> getHoliday(final int id) {
		return repository.getHoliday(id);
	}
}
