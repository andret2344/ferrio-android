package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.core.util.Pair;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.DayFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayActivity extends UHCActivity {
	private static final Random RANDOM = new Random();

	private ViewPager2 pager;
	private List<HolidayDay> holidayDays;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day);

		pager = findViewById(R.id.day_pager_days);

		final int day = getIntent().getIntExtra(MainActivity.DAY, -1);
		final int month = getIntent().getIntExtra(MainActivity.MONTH, -1);

		holidayDays = getIntent().getParcelableArrayListExtra(MainActivity.HOLIDAY_DAYS);
		pager.setAdapter(new DayFragmentAdapter(getSupportFragmentManager(), getLifecycle(), holidayDays));
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
		final boolean leap = date.isLeapYear();
		int id = date.getDayOfYear();
		if (id > (leap ? 60 : 59)) {
			id += leap ? 1 : 2;
		}
		pager.setCurrentItem(id - 1, false);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_day_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar -> {
			actionBar.setTitle(date.format(Util.getDateTimeFormatter()));
			actionBar.setDisplayHomeAsUpEnabled(true);
		});

		pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
				final Pair<Month, Integer> pair = Util.calculateDates(position + 1);
				final String format = Util.getFormattedDate(pair);
				retrieveSupportActionBar().ifPresent(actionBar ->
						actionBar.setTitle(format));
			}
		});
		MobileAds.initialize(this);
		final AdView adView = findViewById(R.id.day_adview_bottom);
		adView.loadAd(new AdRequest.Builder().build());

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				int id = pager.getCurrentItem();
				if (id > 58) {
					id -= LocalDate.now().isLeapYear() ? 0 : 1;
				}
				final LocalDate date = LocalDate.ofYearDay(LocalDate.now().getYear(), Math.max(id, 1));
				final Intent returnIntent = new Intent();
				returnIntent.putExtra(MainActivity.MONTH, date.getMonthValue());
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
			final LocalDate date = LocalDate.now();
			final boolean leap = date.isLeapYear();
			int id = date.getDayOfYear();
			if (id > (leap ? 60 : 59)) {
				id += leap ? 0 : 1;
			}
			pager.setCurrentItem(id, true);
			return true;
		}
		if (item.getItemId() == R.id.menu_day_random) {
			pager.setCurrentItem(RANDOM.nextInt(367), true);
			return true;
		}
		if (item.getItemId() == R.id.menu_day_share) {
			final Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.unusual_holiday));
			final Pair<Month, Integer> pair = Util.calculateDates(pager.getCurrentItem() + 1);
			final LocalDate localDate = LocalDate.of(LocalDate.now().getYear(), pair.first, pair.second);
			final boolean usualHolidays = getSharedPreferences().getBoolean(getString(R.string.settings_key_usual_holidays), false);
			holidayDays.stream().filter(h -> h.getMonth() == pair.first.getValue() && h.getDay() == pair.second)
					.findAny()
					.ifPresent(day -> {
						final String holidays = day.getHolidaysList(usualHolidays)
								.stream()
								.map(Holiday::getName)
								.map(text -> getString(R.string.pointed_text, text))
								.collect(Collectors.joining("\n"));
						intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, localDate, holidays));
						startActivity(Intent.createChooser(intent, getString(R.string.share_via)));
					});
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
