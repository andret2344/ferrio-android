package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
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
		final UHCApplication ferrioApplication = (UHCApplication) creationExtras.get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY);
		assert ferrioApplication != null;
		return new HolidayViewModel(ferrioApplication.getAppRepository());
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

	@NonNull
	public LiveData<Optional<Holiday>> getHoliday(final int id) {
		return Transformations.map(repository.getHoliday(id), Optional::ofNullable);
	}
}
