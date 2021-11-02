package eu.andret.kalendarzswiatnietypowych.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.DayFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.fragment.DayFragment;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class DayActivity extends AppCompatActivity {
	private Util util;
	private ViewPager pager;
	private final Random random = new Random();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(Color.rgb(0xff, 0x8a, 0x00)));
		util = new Util(this);
		util.applyTheme();
		setContentView(R.layout.activity_day);

		pager = findViewById(R.id.day_pager_days);
		pager.setOffscreenPageLimit(10);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowCustomEnabled(false);

		final int day = getIntent().getIntExtra("day", -1);
		final int month = getIntent().getIntExtra("month", -1);
		pager.setAdapter(new DayFragmentAdapter(getSupportFragmentManager()));
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
		final boolean leap = date.isLeapYear();
		int id = date.getDayOfYear();
		if (id > (leap ? 60 : 59)) {
			id += leap ? 1 : 2;
		}
		pager.setCurrentItem(id - 1);
		getSupportActionBar().setTitle(day + getAddition(day) + " " + util.getMonthGenitive(month));
		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
				final DayFragment fragment = (DayFragment) Objects.requireNonNull(pager.getAdapter()).instantiateItem(pager, pager.getCurrentItem());
				Objects.requireNonNull(getSupportActionBar()).setTitle(fragment.getDay() + getAddition(fragment.getDay()) + " " + util.getMonthGenitive(fragment.getMonth() - 1));
			}

			@Override
			public void onPageSelected(final int position) {
				// do nothing
			}

			@Override
			public void onPageScrollStateChanged(final int state) {
				// do nothing
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
			pager.setCurrentItem(random.nextInt(367), true);
			return true;
		} else if (item.getItemId() == R.id.menu_day_share) {
			final Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.unusual_holiday));
			final DayFragment fragment = (DayFragment) Objects.requireNonNull(pager.getAdapter()).instantiateItem(pager, pager.getCurrentItem());
			final String date = fragment.getDay() + getAddition(fragment.getDay()) + " " + util.getMonthGenitive(fragment.getMonth() - 1);
			final StringBuilder holidays = new StringBuilder();
			final HolidayDay d = HolidayCalendar.getInstance(this).getMonth(fragment.getMonth()).getDay(fragment.getDay());
			for (final Holiday h : d.getHolidays()) {
				holidays.append("\n").append(getResources().getString(R.string.pointer)).append(" ").append(h.getText());
			}
			i.putExtra(Intent.EXTRA_TEXT, date + ":\n" + holidays + "\n\n" + getResources().getString(R.string.check_it_yourself) + "\nhttps://play.google.com/store/apps/details?id=eu.andret.kalendarzswiatnietypowych");
			startActivity(Intent.createChooser(i, getResources().getString(R.string.share_via)));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBackPressed() {
		try {
			int id = pager.getCurrentItem();

			if (id > 58) {
				id -= LocalDate.now().isLeapYear() ? 1 : 2;
			}

			final LocalDate date = LocalDate.ofYearDay(LocalDate.now().getYear(), id + 1);
			final Intent returnIntent = new Intent();
			returnIntent.putExtra("month", date.getMonthValue());
			setResult(Activity.RESULT_OK, returnIntent);
		} catch (final Exception ex) {
			util.createAlert(R.string.oops, R.string.something_went_wrong);
		}
		super.onBackPressed();
	}

	public String getAddition(final int day) {
		if (Locale.getDefault().getLanguage().equalsIgnoreCase("en")) {
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
