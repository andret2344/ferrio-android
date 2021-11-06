package eu.andret.kalendarzswiatnietypowych.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapters.DrawerAdapter;
import eu.andret.kalendarzswiatnietypowych.adapters.MonthFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.adapters.SearchHolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerImage;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerItem;
import eu.andret.kalendarzswiatnietypowych.drawer.ViewItem;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class MainActivity extends AppCompatActivity {
	public static final String CALENDAR = "calendar";
	public static final String WIDGET = "widget";
	public static final String MONTH = "month";
	public static final String DAY = "day";
	public static final String FROM = "from";
	public static final String HOLIDAY_DAYS = "holidayDays";
	public static final String SELECTED_LANGUAGE = "selectedLanguage";
	public static final String HOLIDAY_DAY = "holidayDay";

	private DrawerLayout navigationDrawer;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private ViewPager2 viewPager2;
	private ListView searchListView;
	private PowerManager.WakeLock wakeLock;
	private LinearLayout preLoaderLayout;
	private HolidayCalendar holidayCalendar;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		final String stringFrom = getIntent().getStringExtra(FROM);
		if (stringFrom != null && stringFrom.equals(MainActivity.WIDGET)) {
			final Intent intent = new Intent(this, DayActivity.class);
			intent.putExtra(FROM, MainActivity.CALENDAR);
			intent.putExtra(DAY, getIntent().getIntExtra(DAY, 1));
			intent.putExtra(MONTH, getIntent().getIntExtra(MONTH, 1));
			startActivityForResult(intent, getResources().getInteger(R.integer.request_code_change_month));
		}
		super.onCreate(savedInstanceState);

		final String themeDarkKey = getString(R.string.settings_key_theme_dark);
		final String themeLightKey = getString(R.string.settings_key_theme_light);
		final String themeSettingsKey = getString(R.string.settings_key_theme_app);
		final String themeValue = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(themeSettingsKey, themeDarkKey);
		if (themeDarkKey.equals(themeValue)) {
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
		} else if (themeLightKey.equals(themeValue)) {
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		}

		final ViewGroup v = (ViewGroup) getWindow().getDecorView().getRootView();
		preLoaderLayout = new LinearLayout(this);
		preLoaderLayout.setOrientation(LinearLayout.VERTICAL);
		preLoaderLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		preLoaderLayout.setBackgroundColor(Data.MyColor.BLACK);
		v.addView(preLoaderLayout);

		final ImageView image = new ImageView(this);
		final LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		imageParams.gravity = Gravity.CENTER;
		image.setLayoutParams(imageParams);
		image.setImageResource(R.drawable.ic_app_logo);
		preLoaderLayout.addView(image);

		final TextView text = new TextView(this);
		final LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		textParams.gravity = Gravity.CENTER;
		text.setGravity(Gravity.CENTER);
		text.setText(R.string.app_name);
		text.setLayoutParams(textParams);
		text.setTextSize(getResources().getDimension(R.dimen.drawer_list_name_text));

		final ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
		final LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		progressParams.gravity = Gravity.CENTER;
		progress.setLayoutParams(progressParams);
		preLoaderLayout.addView(progress);

		setContentView(R.layout.activity_main);
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (pm != null) {
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			wakeLock.acquire(2 * 60 * 1000L);
		}

		searchListView = findViewById(R.id.main_list_results);

		setUpNavigationDrawer();
		viewPager2 = findViewById(R.id.main_pager_months);
		final SharedPreferences prefs = Data.getPreferences(this, Data.Prefs.LANGUAGE);
		final String selectedLanguageCode = prefs.getString(MainActivity.SELECTED_LANGUAGE, "en");
		final HolidaysDBHelper holidaysDBHelper = new HolidaysDBHelper(this);
		if (holidaysDBHelper.getLanguages().isEmpty()) {
			startActivity(new Intent(this, LanguageActivity.class));
		}
		holidayCalendar = holidaysDBHelper.getAll(selectedLanguageCode);
		viewPager2.setAdapter(new MonthFragmentAdapter(getSupportFragmentManager(), getLifecycle(), holidayCalendar));
		holidaysDBHelper.close();
		viewPager2.setCurrentItem(LocalDate.now().getMonthValue() - 1);
		final String[] months = getResources().getStringArray(R.array.months);
		getSupportActionBar().setTitle(months[viewPager2.getCurrentItem()]);
		viewPager2.setOffscreenPageLimit(12);
		viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
				getSupportActionBar().setTitle(months[position]);
			}
		});

		new Handler(Looper.getMainLooper()).postDelayed(this::dismissPreLoader, 2500);
		update();

		MobileAds.initialize(this);
		final AdView adView = findViewById(R.id.main_adview_bottom);
		adView.loadAd(new AdRequest.Builder().build());
	}

	@Override
	public void onBackPressed() {
		if (navigationDrawer.isDrawerOpen(GravityCompat.START)) {
			navigationDrawer.closeDrawer(GravityCompat.START);
		} else {
			wakeLock.release();
			super.onBackPressed();
		}
	}

	public void setUpNavigationDrawer() {
		final TypedArray typedArray = obtainStyledAttributes(R.styleable.images);
		final List<ViewItem> data = new ArrayList<>();
		data.add(new NavigationDrawerImage(ContextCompat.getDrawable(this, R.drawable.ic_launcher)));
		data.add(new NavigationDrawerItem(R.string.settings, typedArray.getDrawable(R.styleable.images_settings), v -> startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), getApplicationContext().getResources().getInteger(R.integer.request_code_settings))));

		data.add(new NavigationDrawerItem(R.string.languages, typedArray.getDrawable(R.styleable.images_translate), v -> startActivity(new Intent(getApplicationContext(), LanguageActivity.class))));

		data.add(new NavigationDrawerItem(R.string.about_calendar, typedArray.getDrawable(R.styleable.images_event), v -> Util.createAlert(this, R.string.about_calendar, R.string.about_calendar_text)));

		data.add(new NavigationDrawerItem(R.string.about_holidays, typedArray.getDrawable(R.styleable.images_format_quote), v -> Util.createAlertWithImage(this, R.drawable.holidays, R.string.about_holidays, R.string.about_holidays_text)));

		navigationDrawer = findViewById(R.id.main_drawer_main);
		drawerList = findViewById(R.id.main_list_drawer);
		drawerToggle = new ActionBarDrawerToggle(this, navigationDrawer, R.string.drawer_open, R.string.drawer_close);
		navigationDrawer.addDrawerListener(drawerToggle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		drawerList.setAdapter(new DrawerAdapter(this, data));
		typedArray.recycle();
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (drawerToggle != null) {
			drawerToggle.syncState();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		wakeLock.acquire(2 * 60 * 1000L);
	}

	@Override
	protected void onPause() {
		super.onPause();
		wakeLock.release();
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		final MenuItem searchItem = menu.findItem(R.id.menu_main_search);
		final SearchView searchView = (SearchView) searchItem.getActionView();
		final Collection<HolidayDay> originalList = Collections.unmodifiableCollection(holidayCalendar.getHolidayDays());
		final List<HolidayDay> list = new ArrayList<>(originalList);
		final SharedPreferences theme = Data.getPreferences(this, Data.Prefs.THEME);
		final SearchHolidayAdapter adapter = new SearchHolidayAdapter(this, list);
		searchListView.setAdapter(adapter);
		if (searchView == null) {
			return true;
		}
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(final String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				list.clear();
				if (newText == null || newText.equals("")) {
					searchListView.setVisibility(View.INVISIBLE);
					viewPager2.setVisibility(View.VISIBLE);
					list.addAll(originalList);
				} else {
					searchListView.setVisibility(View.VISIBLE);
					viewPager2.setVisibility(View.INVISIBLE);
					originalList.stream()
							.map(holidayDay -> {
								final boolean includeUsual = theme.getBoolean(getResources().getString(R.string.settings_key_usual_holidays), false);
								final List<Holiday> holidayList = holidayDay.getHolidaysList(includeUsual)
										.stream()
										.filter(holiday -> holiday.getText().toLowerCase(Locale.ROOT).contains(newText.toLowerCase(Locale.ROOT)))
										.collect(Collectors.toList());
								if (holidayList.isEmpty()) {
									return null;
								}
								return new HolidayDay(holidayDay.getMonth(), holidayDay.getDay(), holidayList);
							})
							.filter(Objects::nonNull)
							.forEach(list::add);
				}
				Collections.sort(list);
				adapter.notifyDataSetChanged();
				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == getResources().getInteger(R.integer.request_code_change_month)) {
			if (resultCode == RESULT_OK) {
				viewPager2.setCurrentItem(data.getIntExtra(MONTH, LocalDate.now().getMonthValue()) - 1);
			} else {
				Toast.makeText(this, "unknown error", Toast.LENGTH_SHORT).show();
			}
		} else if (requestCode == getResources().getInteger(R.integer.request_code_settings)) {
			if (resultCode == RESULT_OK) {
				update();
			} else {
				Toast.makeText(this, "unknown error", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.menu_main_today) {
			viewPager2.setCurrentItem(LocalDate.now().getMonthValue() - 1);
		}
		return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	public void update() {
		final Data.AppColorSet color = Data.getColors(Util.isDarkTheme(this));
		drawerList.setBackgroundColor(color.background);
	}

	public void dismissPreLoader() {
		final AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
		fadeOut.setStartOffset(1500);
		fadeOut.setDuration(500);
		fadeOut.setFillAfter(false);
		fadeOut.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(final Animation animation) {
				// do nothing
			}

			@Override
			public void onAnimationRepeat(final Animation animation) {
				// do nothing
			}

			@Override
			public void onAnimationEnd(final Animation animation) {
				preLoaderLayout.setVisibility(View.INVISIBLE);
			}
		});
		preLoaderLayout.startAnimation(fadeOut);
	}
}
