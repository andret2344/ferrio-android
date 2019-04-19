package eu.andret.kalendarzswiatnietypowych.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapters.DayFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.fragment.DayFragment;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class DayActivity extends AppCompatActivity {
	private Util util;
	private ViewPager pager;
	private final Random random = new Random();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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

		int day = getIntent().getIntExtra("day", -1);
		int month = getIntent().getIntExtra("month", -1);
		Calendar c = Calendar.getInstance();
		pager.setAdapter(new DayFragmentAdapter(getSupportFragmentManager()));
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		boolean leap = new GregorianCalendar().isLeapYear(c.get(Calendar.YEAR));
		int id = c.get(Calendar.DAY_OF_YEAR);
		if (id > (leap ? 60 : 59)) {
			id += leap ? 1 : 2;
		}
		pager.setCurrentItem(id - 1);
		getSupportActionBar().setTitle(day + getAddition(day) + " " + util.getMonthGenitive(month));
		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				DayFragment fragment = (DayFragment) pager.getAdapter().instantiateItem(pager, pager.getCurrentItem());
				Objects.requireNonNull(getSupportActionBar()).setTitle(fragment.getDay() + getAddition(fragment.getDay()) + " " + util.getMonthGenitive(fragment.getMonth() - 1));
			}

			@Override
			public void onPageSelected(int position) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});
		util.createAd(R.id.day_adview_bottom);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.day, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		} else if (item.getItemId() == R.id.menu_day_today) {
			Calendar c = Calendar.getInstance();
			boolean leap = new GregorianCalendar().isLeapYear(c.get(Calendar.YEAR));
			int id = c.get(Calendar.DAY_OF_YEAR);
			if (id > (leap ? 60 : 59)) {
				id += leap ? 0 : 1;
			}
			pager.setCurrentItem(id, true);
			return true;
		} else if (item.getItemId() == R.id.menu_day_random) {
			pager.setCurrentItem(random.nextInt(367), true);
			return true;
		} else if (item.getItemId() == R.id.menu_day_share) {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.unusual_holiday));
			DayFragment fragment = (DayFragment) pager.getAdapter().instantiateItem(pager, pager.getCurrentItem());
			String date = fragment.getDay() + getAddition(fragment.getDay()) + " " + util.getMonthGenitive(fragment.getMonth() - 1);
			StringBuilder holidays = new StringBuilder();
			HolidayDay d = HolidayCalendar.getInstance(this).getMonth(fragment.getMonth()).getDay(fragment.getDay());
			for (Holiday h : d.getHolidays()) {
				holidays.append("\n").append(getResources().getString(R.string.pointer)).append(" ").append(h.getText());
			}
			i.putExtra(Intent.EXTRA_TEXT, date + ":\n" + holidays + "\n\n" + getResources().getString(R.string.check_it_yourself) + "\nhttps://play.google.com/store/apps/details?id=eu.andret.kalendarzswiatnietypowych");
			startActivity(Intent.createChooser(i, getResources().getString(R.string.share_via)));
			// Naprawic przycisk facebook
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBackPressed() {
		try {
			int id = pager.getCurrentItem();
			Calendar c = Calendar.getInstance();

			if (id > 58) {
				id -= new GregorianCalendar().isLeapYear(c.get(Calendar.YEAR)) ? 1 : 2;
			}
			c.set(Calendar.DAY_OF_YEAR, id + 1);

			Intent returnIntent = new Intent();
			returnIntent.putExtra("month", c.get(Calendar.MONTH));
			setResult(Activity.RESULT_OK, returnIntent);
		} catch (Exception ex) {
			util.createAlert(R.string.oops, R.string.something_went_wrong);
		}
		super.onBackPressed();
	}

	public String getAddition(int day) {
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
