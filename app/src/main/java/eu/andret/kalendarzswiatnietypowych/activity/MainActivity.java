package eu.andret.kalendarzswiatnietypowych.activity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Calendar;
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
	public static final String MONTH = "month";
	public static final String DAY = "day";
	public static final String FROM = "from";
	private final Calendar calendar = Calendar.getInstance();

	private Util util;
	private DrawerLayout navigationDrawer;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private ViewPager pager;
	private ListView list;
	private PowerManager.WakeLock wakeLock;
	private LinearLayout preloaderLayout;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		final String f = getIntent().getStringExtra(FROM);
		if (f != null && (f.equals("widget") || f.equals("notification"))) {
			final Intent i = new Intent(this, DayActivity.class);
			i.putExtra(FROM, "calendar");
			i.putExtra(DAY, getIntent().getIntExtra(DAY, 1));
			i.putExtra(MONTH, getIntent().getIntExtra(MONTH, 1));
			startActivityForResult(i, getResources().getInteger(R.integer.request_code_change_month));
		}
		super.onCreate(savedInstanceState);

		util = new Util(this);
		util.applyTheme();

		final ViewGroup v = (ViewGroup) getWindow().getDecorView().getRootView();
		preloaderLayout = new LinearLayout(this);
		preloaderLayout.setOrientation(LinearLayout.VERTICAL);
		preloaderLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		preloaderLayout.setBackgroundColor(Data.MyColor.BLACK);
		v.addView(preloaderLayout);

		final ImageView image = new ImageView(this);
		final LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		imageParams.gravity = Gravity.CENTER;
		image.setLayoutParams(imageParams);
		image.setImageResource(R.drawable.ic_app_logo);
		preloaderLayout.addView(image);

		final TextView text = new TextView(this);
		final LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		textParams.gravity = Gravity.CENTER;
		text.setGravity(Gravity.CENTER);
		text.setText(R.string.app_name);
		text.setLayoutParams(textParams);
		text.setTextSize(getResources().getDimension(R.dimen.drawer_list_name_text));
		// preloaderLayout.addView(text);

		final ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
		final LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		progressParams.gravity = Gravity.CENTER;
		progress.setLayoutParams(progressParams);
		preloaderLayout.addView(progress);

		setContentView(R.layout.activity_main);
//		MobileAds.initialize(this, "ca-app-pub-3410450408196791~3872850665");
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (pm != null) {
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			wakeLock.acquire(2 * 60 * 1000L);
		}

		list = findViewById(R.id.main_list_results);

		setUpNavigationDrawer();
		pager = findViewById(R.id.main_pager_months);
		pager.setAdapter(new MonthFragmentAdapter(getSupportFragmentManager()));
		pager.setCurrentItem(calendar.get(Calendar.MONTH));
		getSupportActionBar().setTitle(util.getMonth(pager.getCurrentItem()));
		pager.setOffscreenPageLimit(12);
		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
				getSupportActionBar().setTitle(util.getMonth(position));
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

		new Handler().postDelayed(MainActivity.this::dismissPreLoader, 4000);

		util.createAd(R.id.main_adview_bottom);
		// util.createNotification("UHC", "Today is", R.drawable.ic_launcher, getIntent(), false);

		// startService(new Intent(this, NotificationService.class));
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

		// data.add(
		new NavigationDrawerItem(R.string.menu_favourites, ta.getDrawable(R.styleable.images_star), v -> {

		});
		// );

		data.add(new NavigationDrawerItem(R.string.languages, ta.getDrawable(R.styleable.images_translate), v -> startActivity(new Intent(getApplicationContext(), LanguageActivity.class))));

		data.add(new NavigationDrawerItem(R.string.rate, ta.getDrawable(R.styleable.images_thumb_up), v -> {
			final String packet = "eu.andret.kalendarzswiatnietypowych";
			final Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packet));
			boolean marketFound = false;

			final List<ResolveInfo> otherApps = getPackageManager().queryIntentActivities(rateIntent, 0);
			for (final ResolveInfo otherApp : otherApps) {
				if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {
					final ActivityInfo otherAppActivity = otherApp.activityInfo;
					final ComponentName componentName = new ComponentName(otherAppActivity.applicationInfo.packageName, otherAppActivity.name);
					rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					rateIntent.setComponent(componentName);
					startActivity(rateIntent);
					marketFound = true;
					break;
				}
			}

			if (!marketFound) {
				final Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packet));
				startActivity(webIntent);
			}
		}));

		data.add(new NavigationDrawerItem(R.string.about_calendar, ta.getDrawable(R.styleable.images_event), v -> util.createAlert(R.string.about_calendar, R.string.about_calendar_text)));

		data.add(new NavigationDrawerItem(R.string.about_holidays, ta.getDrawable(R.styleable.images_format_quote), v -> util.createAlertWithImage(R.drawable.holidays, R.string.about_holidays, R.string.about_holidays_text)));

		// data.add(
		new NavigationDrawerItem(R.string.recommend_also, ta.getDrawable(R.styleable.images_myfeasts), v -> {
			final String pck = "eu.deyanix.myfeasts";
			final Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pck));
			boolean marketFound = false;

			final List<ResolveInfo> otherApps = getPackageManager().queryIntentActivities(rateIntent, 0);
			for (final ResolveInfo otherApp : otherApps) {
				if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {
					final ActivityInfo otherAppActivity = otherApp.activityInfo;
					final ComponentName componentName = new ComponentName(otherAppActivity.applicationInfo.packageName, otherAppActivity.name);
					rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					rateIntent.setComponent(componentName);
					startActivity(rateIntent);
					marketFound = true;
					break;
				}
			}

			if (!marketFound) {
				final Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pck));
				startActivity(webIntent);
			}
		});
		// );

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
		MainActivity.this.list.setAdapter(adapter);
		if (searchView == null) {
			return true;
		} // TODO
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(final String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				list.clear();
				if (newText == null || newText.equals("")) {
					list.clear();
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
				set(data.getIntExtra(MONTH, calendar.get(Calendar.MONTH)), false);
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
			final Calendar cal = Calendar.getInstance();
			pager.setCurrentItem(cal.get(Calendar.MONTH));
		}
		return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	public void update() {
		final int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), MyWidgetProvider.class));
		new MyWidgetProvider().onUpdate(this, AppWidgetManager.getInstance(this), ids);
		pager.invalidate();
		pager.refreshDrawableState();
		final SharedPreferences theme = Data.getPreferences(this, Data.Prefs.THEME);
		final Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getResources().getString(R.string.settings_theme_app), "1")));
		navigationDrawer.setBackgroundColor(color.background);
		drawerList.setBackgroundColor(color.background);
		findViewById(R.id.main_relative_main).setBackgroundColor(color.background);
	}

	private void set(final int id, final boolean smooth) {
		pager.setCurrentItem(id, smooth);
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
				preloaderLayout.setVisibility(View.INVISIBLE);
			}
		});
		preloaderLayout.startAnimation(fadeOut);
	}
}
