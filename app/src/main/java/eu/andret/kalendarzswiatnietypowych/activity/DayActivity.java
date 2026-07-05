package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.DayFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.databinding.ActivityDayBinding;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.ShareCardRenderer;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayActivity extends BaseActivity {
	public static final String POSITION = "position";
	private static final Random RANDOM = new Random();

	private ActivityDayBinding binding;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityDayBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		final int day = getIntent().getIntExtra(MainActivity.DAY, -1);
		final int month = getIntent().getIntExtra(MainActivity.MONTH, -1);
		if (month < 1 || month > 12 || day < 1 || day > 31) {
			finish();
			return;
		}

		binding.dayPagerDays.setAdapter(new DayFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
		final Pair<Month, Integer> initialPair = new Pair<>(Month.of(month), day);
		binding.dayPagerDays.setCurrentItem(Util.calculateIndex(month, day), false);

		setSupportActionBar(binding.activityDayToolbar);
		retrieveSupportActionBar().ifPresent(actionBar -> {
			actionBar.setTitle(Util.getFormattedDateWithYear(initialPair));
			actionBar.setDisplayHomeAsUpEnabled(true);
		});

		binding.dayPagerDays.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(final int position) {
				final Pair<Month, Integer> pair = Util.calculateDates(position + 1);
				final String format = Util.getFormattedDateWithYear(pair);
				retrieveSupportActionBar().ifPresent(actionBar ->
						actionBar.setTitle(format));
			}
		});
		registerAdView(binding.dayAdviewBottom);

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				final Pair<Month, Integer> pair = Util.calculateDates(binding.dayPagerDays.getCurrentItem() + 1);
				final Intent returnIntent = new Intent();
				returnIntent.putExtra(MainActivity.MONTH, pair.first.getValue());
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.day, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getOnBackPressedDispatcher().onBackPressed();
			return true;
		}
		if (item.getItemId() == R.id.menu_day_today) {
			final LocalDate now = LocalDate.now(ZoneId.systemDefault());
			binding.dayPagerDays.setCurrentItem(Util.calculateIndex(now.getMonthValue(), now.getDayOfMonth()), true);
			return true;
		}
		if (item.getItemId() == R.id.menu_day_random) {
			binding.dayPagerDays.setCurrentItem(RANDOM.nextInt(367), true);
			return true;
		}
		if (item.getItemId() == R.id.menu_day_share) {
			final Pair<Month, Integer> pair = Util.calculateDates(binding.dayPagerDays.getCurrentItem() + 1);
			final boolean usualHolidays = getPreferences().includeUsualHolidays();
			final boolean showAdult = getPreferences().showAdultContent();
			observeOnce(holidayViewModel.getHolidayDay(pair.first.getValue(), pair.second), this,
					holidayDay -> ShareCardRenderer.shareDay(DayActivity.this, pair, holidayDay.getHolidaysList(usualHolidays, showAdult)));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private static <T> void observeOnce(@NonNull final LiveData<T> liveData,
			@NonNull final LifecycleOwner owner, @NonNull final Observer<T> observer) {
		liveData.observe(owner, new Observer<>() {
			@Override
			public void onChanged(final T value) {
				liveData.removeObserver(this);
				observer.onChanged(value);
			}
		});
	}
}
