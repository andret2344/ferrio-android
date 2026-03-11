package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.time.Month;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.DayFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.ShareCardRenderer;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayActivity extends BaseActivity {
	public static final String POSITION = "position";
	private static final Random RANDOM = new Random();

	private ViewPager2 pager;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day);

		pager = findViewById(R.id.day_pager_days);

		final int day = getIntent().getIntExtra(MainActivity.DAY, -1);
		final int month = getIntent().getIntExtra(MainActivity.MONTH, -1);
		if (month < 1 || month > 12 || day < 1 || day > 31) {
			finish();
			return;
		}

		pager.setAdapter(new DayFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
		pager.setCurrentItem(Util.calculateIndex(month, day), false);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_day_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar -> {
			actionBar.setTitle(date.format(Util.getDateTimeFormatter()));
			actionBar.setDisplayHomeAsUpEnabled(true);
		});

		pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageSelected(final int position) {
				final Pair<Month, Integer> pair = Util.calculateDates(position + 1);
				final String format = Util.getFormattedDateWithYear(pair);
				retrieveSupportActionBar().ifPresent(actionBar ->
						actionBar.setTitle(format));
			}
		});
		final AdView adView = findViewById(R.id.day_adview_bottom);
		adView.loadAd(new AdRequest.Builder().build());

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				final Pair<Month, Integer> pair = Util.calculateDates(pager.getCurrentItem() + 1);
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
			final LocalDate now = LocalDate.now();
			pager.setCurrentItem(Util.calculateIndex(now.getMonthValue(), now.getDayOfMonth()), true);
			return true;
		}
		if (item.getItemId() == R.id.menu_day_random) {
			pager.setCurrentItem(RANDOM.nextInt(367), true);
			return true;
		}
		if (item.getItemId() == R.id.menu_day_share) {
			final Pair<Month, Integer> pair = Util.calculateDates(pager.getCurrentItem() + 1);
			final LocalDate localDate = LocalDate.of(LocalDate.now().getYear(), pair.first, pair.second);
			final boolean usualHolidays = getSharedPreferences().getBoolean(getString(R.string.settings_key_usual_holidays), false);
			final LiveData<HolidayDay> liveData = holidayViewModel.getHolidayDay(pair.first.getValue(), pair.second);
			liveData.observe(this, new Observer<>() {
				@Override
				public void onChanged(final HolidayDay holidayDay) {
					liveData.removeObserver(this);
					ShareCardRenderer.shareDay(DayActivity.this, localDate, holidayDay.getHolidaysList(usualHolidays));
				}
			});
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
