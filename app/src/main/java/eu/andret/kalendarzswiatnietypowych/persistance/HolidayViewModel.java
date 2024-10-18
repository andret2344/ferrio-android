package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import java.time.LocalDate;
import java.util.List;

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

	@NonNull
	public LiveData<HolidayDay> getHolidayDay(final int month, final int day) {
		return Transformations.map(repository.getHolidayDay(month, day), holidayDay -> holidayDay.orElse(new HolidayDay(month, day)));
	}

	@NonNull
	public LiveData<List<HolidayDay>> getHolidayDays(@NonNull final LocalDate from, @NonNull final LocalDate to) {
		return repository.getHolidayDays(from.getMonthValue(), from.getDayOfMonth(), to.getMonthValue(), to.getDayOfMonth());
	}

	@Nullable
	public LiveData<Holiday> getHoliday(final int id) {
		return repository.getHoliday(id);
	}
}
