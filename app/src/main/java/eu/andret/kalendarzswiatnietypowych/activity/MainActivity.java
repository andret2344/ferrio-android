package eu.andret.kalendarzswiatnietypowych.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.MonthFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.adapter.SearchHolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar;
import eu.andret.kalendarzswiatnietypowych.util.Downloader;
import eu.andret.kalendarzswiatnietypowych.util.Util;
import java9.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
	public static final String CALENDAR = "calendar";
	public static final String WIDGET = "widget";
	public static final String MONTH = "month";
	public static final String DAY = "day";
	public static final String FROM = "from";
	public static final String HOLIDAY_DAYS = "holidayDays";
	public static final String HOLIDAY_DAY = "holidayDay";
	private static final List<Integer> TRANSPORTS = List.of(
			NetworkCapabilities.TRANSPORT_CELLULAR,
			NetworkCapabilities.TRANSPORT_WIFI,
			NetworkCapabilities.TRANSPORT_ETHERNET);

	private ViewPager2 viewPager2;
	private ListView searchListView;
	private AlertDialog alertDialog;
	private final List<HolidayDay> holidayDays = new ArrayList<>();
	private MutableLiveData<Boolean> internet;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		setContentView(R.layout.activity_main);
		configureObservers();
		final String stringFrom = getIntent().getStringExtra(FROM);
		final int currentMonthValue = LocalDate.now().getMonthValue();
		if (stringFrom != null && stringFrom.equals(WIDGET)) {
			final Intent intent = new Intent(this, DayActivity.class);
			intent.putExtra(FROM, CALENDAR);
			intent.putExtra(DAY, getIntent().getIntExtra(DAY, 1));
			intent.putExtra(MONTH, getIntent().getIntExtra(MONTH, 1));
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {
							final Intent data = result.getData();
							if (data != null) {
								viewPager2.setCurrentItem(data.getIntExtra(MONTH, currentMonthValue) - 1);
							}
						}
					}).launch(intent);
		}

		searchListView = findViewById(R.id.main_list_results);
		viewPager2 = findViewById(R.id.main_pager_months);

		MobileAds.initialize(this);
		final AdView adView = findViewById(R.id.main_adview_bottom);
		adView.loadAd(new AdRequest.Builder().build());

		if (!Boolean.FALSE.equals(internet.getValue())) {
			call();
			return;
		}
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.no_internet_connection);
		alert.setCancelable(false);
		alert.setMessage(R.string.no_internet);
		alertDialog = alert.show();
	}

	private void configureObservers() {
		internet = new MutableLiveData<>(Util.isNetworkAvailable(this));
		internet.observe(this, isConnected -> {
			if (Boolean.TRUE.equals(isConnected) && alertDialog != null && holidayDays.isEmpty()) {
				call();
				alertDialog.dismiss();
			}
		});
		final ConnectivityManager connectivityManager =
				(ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
		TRANSPORTS.stream()
				.map(new NetworkRequest.Builder()::addTransportType)
				.map(NetworkRequest.Builder::build)
				.forEach(x -> connectivityManager.registerNetworkCallback(x, new ConnectivityManager.NetworkCallback() {
					@Override
					public void onAvailable(@NonNull final Network network) {
						super.onAvailable(network);
						internet.postValue(true);
					}

					@Override
					public void onLost(@NonNull final Network network) {
						super.onLost(network);
						internet.postValue(false);
					}
				}));
	}

	private void call() {
		CompletableFuture.supplyAsync(new Downloader.UnusualCalendarDownloader())
				.thenAccept(unusualCalendar -> {
					holidayDays.addAll(unusualCalendar.getFixed());
					unusualCalendar.getFloating()
							.forEach(floatingHoliday -> {
								try (final Context context = Context.enter()) {
									context.setOptimizationLevel(-1);
									final Scriptable scope = context.initStandardObjects();
									final Object result = context.evaluateString(scope, floatingHoliday.getScript(), "<cmd>", 1, null);
									if (result != null) {
										final String[] split = result.toString().split("\\.");
										UnusualCalendar.getOrCreateDay(holidayDays, Integer.parseInt(split[1]), Integer.parseInt(split[0]))
												.addHoliday(new Holiday(floatingHoliday));
									}
								} catch (final EcmaError ex) {
									// do nothing, ignore the holiday
								}
							});
				})
				.join();
		final int currentMonthValue = LocalDate.now().getMonthValue();
		viewPager2.setAdapter(new MonthFragmentAdapter(getSupportFragmentManager(), getLifecycle(), holidayDays));
		viewPager2.setCurrentItem(currentMonthValue - 1, false);
		Objects.requireNonNull(getSupportActionBar()).setTitle(getMonthName(currentMonthValue));
		viewPager2.setOffscreenPageLimit(12);
		viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
				Objects.requireNonNull(getSupportActionBar()).setTitle(getMonthName(position + 1));
			}
		});
	}

	@NotNull
	private String getMonthName(final int month) {
		final String displayName = Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
		return displayName.substring(0, 1).toUpperCase(Locale.getDefault()) + displayName.substring(1);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		final MenuItem searchItem = menu.findItem(R.id.menu_main_search);
		final SearchView searchView = (SearchView) searchItem.getActionView();
		final List<HolidayDay> list = new ArrayList<>(holidayDays);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final SearchHolidayAdapter adapter = new SearchHolidayAdapter(this, holidayDays, list);
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
				} else {
					searchListView.setVisibility(View.VISIBLE);
					viewPager2.setVisibility(View.INVISIBLE);
					holidayDays.stream()
							.map(holidayDay -> {
								final boolean includeUsual = preferences.getBoolean(getString(R.string.settings_key_usual_holidays), false);
								final List<Holiday> holidayList = holidayDay.getHolidaysList(includeUsual)
										.stream()
										.filter(holiday -> holiday.getName().toLowerCase(Locale.ROOT).contains(newText.toLowerCase(Locale.ROOT)))
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
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == R.id.menu_main_today) {
			viewPager2.setCurrentItem(LocalDate.now().getMonthValue() - 1);
		} else if (itemId == R.id.menu_main_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}
}
