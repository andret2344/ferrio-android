package eu.andret.kalendarzswiatnietypowych.persistence;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.ViewModelInitializer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.LoadState;

public class HolidayViewModel extends ViewModel {
	public static final ViewModelInitializer<HolidayViewModel> INITIALIZER = new ViewModelInitializer<>(HolidayViewModel.class, creationExtras -> {
		final FerrioApplication ferrioApplication = (FerrioApplication) creationExtras.get(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY);
		Objects.requireNonNull(ferrioApplication, "Application must not be null");
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
	public LiveData<Map<Integer, HolidayDay>> getHolidayDayMap() {
		return repository.getHolidayDayMap();
	}

	@NonNull
	public LiveData<List<HolidayDay>> getHolidayDays(@NonNull final LocalDate from,
			@NonNull final LocalDate to) {
		return repository.getHolidayDays(from.getMonthValue(), from.getDayOfMonth(), to.getMonthValue(), to.getDayOfMonth());
	}

	@NonNull
	public LiveData<Optional<Holiday>> getHoliday(@NonNull final String id) {
		return Transformations.map(repository.getHoliday(id), holiday ->
				Optional.ofNullable(holiday));
	}

	@NonNull
	public LiveData<LoadState> getLoadState() {
		return repository.getLoadState();
	}
}
