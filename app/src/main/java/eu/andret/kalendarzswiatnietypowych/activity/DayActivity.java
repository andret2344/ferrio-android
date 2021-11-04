package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.DayFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class DayActivity extends AppCompatActivity {
	private static final Random RANDOM = new Random();

	private Util util;
	private ViewPager2 pager;
	private HolidayCalendar calendar;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(Color.rgb(0xff, 0x8a, 0x00)));
		util = new Util(this);
		util.applyTheme();
		setContentView(R.layout.activity_day);

		pager = findViewById(R.id.day_pager_days);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowCustomEnabled(false);

		final int day = getIntent().getIntExtra(MainActivity.DAY, -1);
		final int month = getIntent().getIntExtra(MainActivity.MONTH, -1);
		final SharedPreferences prefs = Data.getPreferences(this, Data.Prefs.LANGUAGE);
		final String selectedLanguageCode = prefs.getString(MainActivity.SELECTED_LANGUAGE, "en");
		final HolidaysDBHelper holidaysDBHelper = new HolidaysDBHelper(this);
		calendar = holidaysDBHelper.getAll(selectedLanguageCode);
		holidaysDBHelper.close();
		pager.setAdapter(new DayFragmentAdapter(getSupportFragmentManager(), getLifecycle(), calendar));
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
		final boolean leap = date.isLeapYear();
		int id = date.getDayOfYear();
		if (id > (leap ? 60 : 59)) {
			id += leap ? 1 : 2;
		}
		pager.setCurrentItem(id - 1, false);
		getSupportActionBar().setTitle(day + getAddition(day) + " " + util.getMonthGenitive(month));
		pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
				final Util.MonthDayPair pair = Util.calculateDates(position + 1);
				Objects.requireNonNull(getSupportActionBar()).setTitle(pair.getDay() + getAddition(pair.getDay()) + " " + util.getMonthGenitive(pair.getMonth()));
			}
		});
		util.createAd(R.id.day_adview_bottom);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.day, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		} else if (item.getItemId() == R.id.menu_day_today) {
			final LocalDate date = LocalDate.now();
			final boolean leap = date.isLeapYear();
			int id = date.getDayOfYear();
			if (id > (leap ? 60 : 59)) {
				id += leap ? 0 : 1;
			}
			pager.setCurrentItem(id, true);
			return true;
		} else if (item.getItemId() == R.id.menu_day_random) {
			pager.setCurrentItem(RANDOM.nextInt(367), true);
			return true;
		} else if (item.getItemId() == R.id.menu_day_share) {
			final Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.unusual_holiday));
			final Util.MonthDayPair pair = Util.calculateDates(pager.getCurrentItem());
			final String date = pair.getDay() + getAddition(pair.getDay()) + " " + util.getMonthGenitive(pair.getMonth().getValue() - 1);
			final StringBuilder holidays = new StringBuilder();
			final HolidayDay holidayDay = calendar.getDay(pair.getMonth().getValue(), pair.getDay());
			for (final Holiday h : holidayDay.getHolidays()) {
				holidays.append("\n").append(getResources().getString(R.string.pointed_text, h.getText()));
			}
			intent.putExtra(Intent.EXTRA_TEXT, date + ":\n" + holidays + "\n\n" + getResources().getString(R.string.check_it_yourself) + "\nhttps://play.google.com/store/apps/details?id=eu.andret.kalendarzswiatnietypowych");
			startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_via)));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		int id = pager.getCurrentItem();
		if (id > 58) {
			id -= LocalDate.now().isLeapYear() ? 0 : 1;
		}
		final LocalDate date = LocalDate.ofYearDay(LocalDate.now().getYear(), Math.max(id, 1));
		final Intent returnIntent = new Intent();
		returnIntent.putExtra(MainActivity.MONTH, date.getMonthValue());
		setResult(RESULT_OK, returnIntent);
		super.onBackPressed();
	}

	public String getAddition(final int day) {
		if (Locale.ROOT.getLanguage().equalsIgnoreCase("en")) {
			if (day % 100 >= 10 && day % 100 <= 20) {
				return "th";
			}
			switch (day % 10) {
				case 1:
					return "st";
				case 2:
					return "nd";
				case 3:
					return "rd";
				default:
					return "th";
			}
		}
		return "";
	}
}
