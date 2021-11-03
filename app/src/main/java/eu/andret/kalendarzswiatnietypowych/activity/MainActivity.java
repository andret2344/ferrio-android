package eu.andret.kalendarzswiatnietypowych.activity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.MyWidgetProvider;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.DrawerAdapter;
import eu.andret.kalendarzswiatnietypowych.adapter.MonthFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.adapter.SearchHolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerImage;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerItem;
import eu.andret.kalendarzswiatnietypowych.drawer.ViewItem;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class MainActivity extends AppCompatActivity {
	public static final String ID = "id";
	public static final String WIDGET = "widget";
	public static final String MONTH = "month";
	public static final String DAY = "day";
	public static final String FROM = "from";

	private Util util;
	private DrawerLayout navigationDrawer;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private ViewPager2 pager;
	private ListView list;
	private PowerManager.WakeLock wakeLock;
	private LinearLayout preLoaderLayout;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		final String stringFrom = getIntent().getStringExtra(FROM);
		if (stringFrom != null && stringFrom.equals("widget")) {
			final Intent intent = new Intent(this, DayActivity.class);
			intent.putExtra(FROM, "calendar");
			intent.putExtra(DAY, getIntent().getIntExtra(DAY, 1));
			intent.putExtra(MONTH, getIntent().getIntExtra(MONTH, 1));
			startActivityForResult(intent, getResources().getInteger(R.integer.request_code_change_month));
		}
		super.onCreate(savedInstanceState);

		util = new Util(this);
		util.applyTheme();

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

		list = findViewById(R.id.main_list_results);

		setUpNavigationDrawer();
		pager = findViewById(R.id.main_pager_months);
		pager.setAdapter(new MonthFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
		pager.setCurrentItem(LocalDate.now().getMonthValue());
		getSupportActionBar().setTitle(util.getMonth(pager.getCurrentItem()));
		pager.setOffscreenPageLimit(12);
		pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
				getSupportActionBar().setTitle(util.getMonth(position));
			}
		});

		new Handler().postDelayed(MainActivity.this::dismissPreLoader, 4000);
		util.createAd(R.id.main_adview_bottom);
		update();
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
		final TypedArray ta = obtainStyledAttributes(R.styleable.images);
		final List<ViewItem> data = new ArrayList<>();
		data.add(new NavigationDrawerImage(ContextCompat.getDrawable(this, R.drawable.ic_launcher)));
		data.add(new NavigationDrawerItem(R.string.settings, ta.getDrawable(R.styleable.images_settings), v -> startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), getApplicationContext().getResources().getInteger(R.integer.request_code_settings))));

		data.add(new NavigationDrawerItem(R.string.languages, ta.getDrawable(R.styleable.images_translate), v -> startActivity(new Intent(getApplicationContext(), LanguageActivity.class))));

		data.add(new NavigationDrawerItem(R.string.about_calendar, ta.getDrawable(R.styleable.images_event), v -> util.createAlert(R.string.about_calendar, R.string.about_calendar_text)));

		data.add(new NavigationDrawerItem(R.string.about_holidays, ta.getDrawable(R.styleable.images_format_quote), v -> util.createAlertWithImage(R.drawable.holidays, R.string.about_holidays, R.string.about_holidays_text)));

		navigationDrawer = findViewById(R.id.main_drawer_main);
		drawerList = findViewById(R.id.main_list_drawer);
		drawerToggle = new ActionBarDrawerToggle(this, navigationDrawer, R.string.drawer_open, R.string.drawer_close);
		navigationDrawer.addDrawerListener(drawerToggle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		drawerList.setAdapter(new DrawerAdapter(this, data));
		ta.recycle();
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
		final ArrayList<HolidayDay> originalList = new ArrayList<>(HolidayCalendar.getInstance(this).getAllDays());
		final ArrayList<HolidayDay> list = new ArrayList<>(originalList);
		final SharedPreferences theme = Data.getPreferences(this, Data.Prefs.THEME);
		final SearchHolidayAdapter adapter = new SearchHolidayAdapter(this, list);
		this.list.setAdapter(adapter);
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
					list.addAll(originalList);
					MainActivity.this.list.setVisibility(View.INVISIBLE);
					pager.setVisibility(View.VISIBLE);
				} else {
					MainActivity.this.list.setVisibility(View.VISIBLE);
					pager.setVisibility(View.INVISIBLE);
					for (int i = 0; i < originalList.size(); i++) {
						final HolidayDay ho = originalList.get(i);
						final List<Holiday> holidaysTmpList = ho.getHolidaysList(theme.getBoolean(getResources().getString(R.string.settings_usual_holidays), false));
						for (final Holiday hd : holidaysTmpList) {
							if (hd.getText().toLowerCase(Locale.getDefault()).contains(newText.toLowerCase(Locale.getDefault()))) {
								final HolidayDay hday = ho.getMonth().new HolidayDay(ho);

								hday.getHolidays().clear();
								for (final Holiday h : holidaysTmpList) {
									if (h.getText().toLowerCase(Locale.getDefault()).contains(newText.toLowerCase(Locale.getDefault()))) {
										hday.getHolidays().add(h);
									}
								}
								list.add(hday);
								break;
							}
						}
					}
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
				pager.setCurrentItem(data.getIntExtra(MONTH, LocalDate.now().getMonthValue()) - 1);
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
			pager.setCurrentItem(LocalDate.now().getMonthValue());
		}
		return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	public void update() {
		final int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), MyWidgetProvider.class));
		new MyWidgetProvider().onUpdate(this, AppWidgetManager.getInstance(this), ids);
		pager.invalidate();
		pager.refreshDrawableState();
		final SharedPreferences theme = Data.getPreferences(this, Data.Prefs.THEME);
		final Data.AppColorSet color = Data.getColors(theme.getInt(getResources().getString(R.string.settings_theme_app), 1));
		navigationDrawer.setBackgroundColor(color.background);
		drawerList.setBackgroundColor(color.background);
		findViewById(R.id.main_relative_main).setBackgroundColor(color.background);
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
