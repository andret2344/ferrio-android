package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.DayClickListener;
import eu.andret.kalendarzswiatnietypowych.adapter.MonthFragmentAdapter;
import eu.andret.kalendarzswiatnietypowych.adapter.SearchHolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.databinding.ActivityMainBinding;
import eu.andret.kalendarzswiatnietypowych.databinding.ImageAlertBinding;
import eu.andret.kalendarzswiatnietypowych.databinding.NavigationDrawerHeaderBinding;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.LoadState;
import eu.andret.kalendarzswiatnietypowych.util.auth.AuthSession;

public class MainActivity extends BaseActivity implements DayClickListener {
	public static final String WIDGET = "widget";
	public static final String MONTH = "month";
	public static final String DAY = "day";
	public static final String FROM = "from";
	public static final String HOLIDAY = "holiday";

	private static final long SEARCH_DEBOUNCE_MS = 300;

	private ActivityMainBinding binding;
	private volatile List<HolidayDay> holidayDays = Collections.emptyList();
	private SearchHolidayAdapter searchAdapter;
	@Nullable
	private SearchView searchView;
	@Nullable
	private Runnable pendingSearch;
	private final ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (result.getResultCode() == RESULT_OK) {
			final Intent data = result.getData();
			if (data != null) {
				final int currentMonthValue = LocalDate.now(ZoneId.systemDefault()).getMonthValue();
				binding.mainPagerMonths.setCurrentItem(data.getIntExtra(MONTH, currentMonthValue) - 1);
			}
		}
	});

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		handleWidgetLaunchIntent();

		setSupportActionBar(binding.activityMainToolbar);

		setUpSearchAdapter();
		registerAdView(binding.mainAdviewBottom);
		setUpNavDrawer();
		setUpMonthPager();
		setUpDataObservers();

		getFerrioApplication().getAppRepository().refresh();
	}

	private void handleWidgetLaunchIntent() {
		final String stringFrom = getIntent().getStringExtra(FROM);
		if (stringFrom != null && stringFrom.equals(WIDGET)) {
			final Intent intent = new Intent(this, DayActivity.class);
			intent.putExtra(DAY, getIntent().getIntExtra(DAY, 1));
			intent.putExtra(MONTH, getIntent().getIntExtra(MONTH, 1));
			activityResult.launch(intent);
		}
	}

	private void setUpSearchAdapter() {
		searchAdapter = new SearchHolidayAdapter(
				getPreferences().isThemeColorized(),
				getPreferences().includeUsualHolidays(),
				getPreferences().showAdultContent());
		binding.mainListResults.setAdapter(searchAdapter);
	}

	private void setUpMonthPager() {
		binding.mainPagerMonths.setAdapter(new MonthFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
		// Keep all 12 months pre-inflated so swiping to a non-adjacent month doesn't
		// trigger a fresh layout + data bind on the UI thread.
		binding.mainPagerMonths.setOffscreenPageLimit(11);
		binding.mainPagerMonths.setCurrentItem(LocalDate.now(ZoneId.systemDefault()).getMonthValue() - 1, false);

		binding.mainPagerMonths.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset,
					final int positionOffsetPixels) {
				binding.activityMainToolbar.setTitle(getMonthName(position + 1));
				binding.activityMainSwipeRefresh.setEnabled(positionOffset == 0);
			}
		});
		binding.activityMainSwipeRefresh.setOnRefreshListener(() -> getFerrioApplication().getAppRepository().refresh());
	}

	private void setUpDataObservers() {
		holidayViewModel.getLoadState().observe(this, state -> {
			if (state != LoadState.LOADING) {
				binding.activityMainSwipeRefresh.setRefreshing(false);
			}
			if (state == LoadState.ERROR) {
				binding.activityMainProgress.setVisibility(View.GONE);
				binding.activityMainSwipeRefresh.setVisibility(View.VISIBLE);
				if (holidayDays.isEmpty()) {
					Snackbar.make(binding.mainPagerMonths, R.string.refresh_error, Snackbar.LENGTH_LONG)
							.setAction(R.string.retry, v -> getFerrioApplication().getAppRepository().refresh())
							.show();
				}
			}
		});

		getFerrioApplication().getAppRepository().getAllHolidayDays().observe(this, days -> {
			holidayDays = days;
			if (!days.isEmpty()) {
				binding.activityMainProgress.setVisibility(View.GONE);
				binding.activityMainSwipeRefresh.setVisibility(View.VISIBLE);
			}
			updateSearchHint();
		});
	}

	@NonNull
	private String getMonthName(final int month) {
		final String displayName = Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
		return displayName.substring(0, 1).toUpperCase(Locale.getDefault()) + displayName.substring(1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateSearchHint();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		final MenuItem searchItem = menu.findItem(R.id.menu_main_search);
		searchView = (SearchView) searchItem.getActionView();
		if (searchView == null) {
			return true;
		}
		updateSearchHint();
		searchView.setIconified(false);
		searchView.setIconifiedByDefault(false);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(final String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				if (pendingSearch != null) {
					binding.mainListResults.removeCallbacks(pendingSearch);
					pendingSearch = null;
				}
				if (newText == null || newText.isEmpty()) {
					binding.mainListResults.setVisibility(View.INVISIBLE);
					binding.mainPagerMonths.setVisibility(View.VISIBLE);
					searchAdapter.submitList(Collections.emptyList());
				} else {
					pendingSearch = () -> {
						binding.mainListResults.setVisibility(View.VISIBLE);
						binding.mainPagerMonths.setVisibility(View.INVISIBLE);
						final String query = newText.toLowerCase(Locale.ROOT);
						final List<HolidayDay> snapshot = holidayDays;
						final boolean includeUsual = getPreferences().includeUsualHolidays();
						final boolean showAdult = getPreferences().showAdultContent();
						CompletableFuture.supplyAsync(() -> snapshot.stream()
								.map(holidayDay -> {
									final List<Holiday> holidayList = holidayDay.getHolidaysList(includeUsual, showAdult)
											.stream()
											.filter(holiday -> holiday.getName().toLowerCase(Locale.ROOT).contains(query))
											.collect(Collectors.toList());
									if (holidayList.isEmpty()) {
										return null;
									}
									return new HolidayDay(holidayDay.getMonth(), holidayDay.getDay(), holidayList);
								})
								.filter(Objects::nonNull)
								.sorted()
								.collect(Collectors.toList())
						).thenAccept(results -> binding.mainListResults.post(() -> searchAdapter.submitList(results)));
					};
					binding.mainListResults.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
				}
				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		if (item.getItemId() == R.id.menu_main_today) {
			binding.mainPagerMonths.setCurrentItem(LocalDate.now(ZoneId.systemDefault()).getMonthValue() - 1);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateSearchHint() {
		if (searchView == null) {
			return;
		}
		final boolean includeUsual = getPreferences().includeUsualHolidays();
		final boolean showAdult = getPreferences().showAdultContent();
		final long holidaysCount = holidayDays.stream()
				.map(holidayDay -> holidayDay.getHolidaysList(includeUsual, showAdult))
				.mapToLong(Collection::size)
				.sum();
		searchView.setQueryHint(getString(R.string.search_placeholder, holidaysCount));
	}

	private void setUpNavDrawer() {
		final DrawerLayout drawer = binding.activityMainLayoutDrawer;
		ViewCompat.setOnApplyWindowInsetsListener(binding.activityMainContent, (v, windowInsets) -> {
			final Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
			return WindowInsetsCompat.CONSUMED;
		});
		final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, binding.activityMainToolbar, R.string.content_description_drawer_open, R.string.content_description_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		final NavigationView navigationView = binding.activityMainNavigation;
		final View headerView = navigationView.getHeaderView(0);
		ViewCompat.setOnApplyWindowInsetsListener(navigationView, (v, windowInsets) -> {
			final Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			headerView.setPadding(headerView.getPaddingLeft(), insets.top,
					headerView.getPaddingRight(), headerView.getPaddingBottom());
			return WindowInsetsCompat.CONSUMED;
		});
		final MenuItem suggest = navigationView.getMenu().findItem(R.id.menu_drawer_suggest);
		final MenuItem suggestions = navigationView.getMenu().findItem(R.id.menu_drawer_suggestions);
		final MenuItem reports = navigationView.getMenu().findItem(R.id.menu_drawer_reports);

		final boolean canSubmit = AuthSession.canSubmitUserContent();
		suggest.setEnabled(canSubmit);
		suggestions.setEnabled(canSubmit);
		reports.setEnabled(canSubmit);
		if (AuthSession.isSignedIn() && headerView != null) {
			final NavigationDrawerHeaderBinding headerBinding = NavigationDrawerHeaderBinding.bind(headerView);
			Glide.with(this).load(AuthSession.avatarUrl()).into(headerBinding.navigationDrawerImage);
			if (AuthSession.isAnonymous()) {
				headerBinding.navigationDrawerHeading.setText(R.string.anonymous_user);
				headerBinding.navigationDrawerSubtitle.setVisibility(View.GONE);
			} else {
				headerBinding.navigationDrawerHeading.setText(AuthSession.displayName());
				headerBinding.navigationDrawerSubtitle.setText(AuthSession.email());
			}
		}

		navigationView.setNavigationItemSelectedListener(menuItem -> {
			if (menuItem.getItemId() == R.id.menu_drawer_suggest) {
				startActivity(new Intent(this, SuggestionActivity.class));
				drawer.close();
			} else if (menuItem.getItemId() == R.id.menu_drawer_suggestions) {
				startActivity(TabbedListActivity.createIntent(this, ApiClient.REPORT_TYPE_SUGGESTION));
				drawer.close();
			} else if (menuItem.getItemId() == R.id.menu_drawer_reports) {
				startActivity(TabbedListActivity.createIntent(this, ApiClient.REPORT_TYPE_ERROR));
				drawer.close();
			} else if (menuItem.getItemId() == R.id.menu_drawer_settings) {
				startActivity(new Intent(this, SettingsActivity.class));
				drawer.close();
			} else if (menuItem.getItemId() == R.id.menu_drawer_about) {
				createAboutCalendarAlert().show();
			}
			return true;
		});

		getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				if (drawer.isOpen()) {
					drawer.close();
				} else {
					finish();
				}
			}
		});
	}

	@Override
	public void onDayClicked(final int day, final int month) {
		final Intent intent = new Intent(this, DayActivity.class);
		intent.putExtra(DAY, day);
		intent.putExtra(MONTH, month);
		activityResult.launch(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (pendingSearch != null) {
			binding.mainListResults.removeCallbacks(pendingSearch);
			pendingSearch = null;
		}
	}

	public AlertDialog createAboutCalendarAlert() {
		final ImageAlertBinding alertBinding = ImageAlertBinding.inflate(getLayoutInflater());
		return new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.about_calendar)
				.setView(alertBinding.getRoot())
				.setPositiveButton(R.string.ok, null)
				.create();
	}

}
