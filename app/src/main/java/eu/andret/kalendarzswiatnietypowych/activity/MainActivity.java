package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.Month;
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

	private ViewPager2 viewPager2;
	private RecyclerView searchListView;
	private MaterialToolbar materialToolbar;
	private volatile List<HolidayDay> holidayDays = Collections.emptyList();
	private SearchHolidayAdapter searchAdapter;
	private final Handler searchHandler = new Handler(Looper.getMainLooper());
	private final ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if (result.getResultCode() == RESULT_OK) {
			final Intent data = result.getData();
			if (data != null) {
				final int currentMonthValue = LocalDate.now().getMonthValue();
				viewPager2.setCurrentItem(data.getIntExtra(MONTH, currentMonthValue) - 1);
			}
		}
	});

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final String stringFrom = getIntent().getStringExtra(FROM);
		if (stringFrom != null && stringFrom.equals(WIDGET)) {
			final Intent intent = new Intent(this, DayActivity.class);
			intent.putExtra(DAY, getIntent().getIntExtra(DAY, 1));
			intent.putExtra(MONTH, getIntent().getIntExtra(MONTH, 1));
			activityResult.launch(intent);
		}

		final int currentMonthValue = LocalDate.now().getMonthValue();
		searchListView = findViewById(R.id.main_list_results);
		final boolean colorized = getSharedPreferences().getBoolean(getString(R.string.settings_key_theme_colorized), false);
		final boolean includeUsual = getSharedPreferences().getBoolean(getString(R.string.settings_key_usual_holidays), false);
		searchAdapter = new SearchHolidayAdapter(colorized, includeUsual);
		searchListView.setAdapter(searchAdapter);
		viewPager2 = findViewById(R.id.main_pager_months);
		materialToolbar = findViewById(R.id.activity_main_toolbar);
		setSupportActionBar(materialToolbar);

		registerAdView(findViewById(R.id.main_adview_bottom));

		setUpNavDrawer();

		viewPager2.setAdapter(new MonthFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
		// Keep all 12 months pre-inflated so swiping to a non-adjacent month doesn't
		// trigger a fresh layout + data bind on the UI thread.
		viewPager2.setOffscreenPageLimit(11);
		viewPager2.setCurrentItem(currentMonthValue - 1, false);
		final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh);
		viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset,
					final int positionOffsetPixels) {
				materialToolbar.setTitle(getMonthName(position + 1));
				swipeRefreshLayout.setEnabled(positionOffset == 0);
			}
		});
		swipeRefreshLayout.setOnRefreshListener(() -> getFerrioApplication().getAppRepository().refresh());

		final View progressIndicator = findViewById(R.id.activity_main_progress);

		holidayViewModel.getLoadState().observe(this, state -> {
			if (state != LoadState.LOADING) {
				swipeRefreshLayout.setRefreshing(false);
			}
			if (state == LoadState.ERROR) {
				progressIndicator.setVisibility(View.GONE);
				swipeRefreshLayout.setVisibility(View.VISIBLE);
				if (holidayDays.isEmpty()) {
					Snackbar.make(viewPager2, R.string.refresh_error, Snackbar.LENGTH_LONG)
							.setAction(R.string.retry, v -> getFerrioApplication().getAppRepository().refresh())
							.show();
				}
			}
		});

		getFerrioApplication().getAppRepository().getAllHolidayDays().observe(this, days -> {
			holidayDays = days;
			if (!days.isEmpty()) {
				progressIndicator.setVisibility(View.GONE);
				swipeRefreshLayout.setVisibility(View.VISIBLE);
			}
		});

		getFerrioApplication().getAppRepository().refresh();
	}

	@NonNull
	private String getMonthName(final int month) {
		final String displayName = Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
		return displayName.substring(0, 1).toUpperCase(Locale.getDefault()) + displayName.substring(1);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		final MenuItem searchItem = menu.findItem(R.id.menu_main_search);
		final SearchView searchView = (SearchView) searchItem.getActionView();
		if (searchView == null) {
			return true;
		}
		final boolean includeUsual = getSharedPreferences().getBoolean(getString(R.string.settings_key_usual_holidays), false);
		final long holidaysCount = holidayDays.stream()
				.map(holidayDay -> holidayDay.getHolidaysList(includeUsual))
				.mapToLong(Collection::size)
				.sum();
		searchView.setQueryHint(getString(R.string.search_placeholder, holidaysCount));
		searchView.setIconified(false);
		searchView.setIconifiedByDefault(false);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(final String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(final String newText) {
				searchHandler.removeCallbacksAndMessages(null);
				if (newText == null || newText.isEmpty()) {
					searchListView.setVisibility(View.INVISIBLE);
					viewPager2.setVisibility(View.VISIBLE);
					searchAdapter.submitList(Collections.emptyList());
				} else {
					searchHandler.postDelayed(() -> {
						searchListView.setVisibility(View.VISIBLE);
						viewPager2.setVisibility(View.INVISIBLE);
						final String query = newText.toLowerCase(Locale.ROOT);
						final List<HolidayDay> snapshot = holidayDays;
						CompletableFuture.supplyAsync(() -> snapshot.stream()
								.map(holidayDay -> {
									final List<Holiday> holidayList = holidayDay.getHolidaysList(includeUsual)
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
						).thenAccept(results -> runOnUiThread(() -> searchAdapter.submitList(results)));
					}, SEARCH_DEBOUNCE_MS);
				}
				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		if (item.getItemId() == R.id.menu_main_today) {
			viewPager2.setCurrentItem(LocalDate.now().getMonthValue() - 1);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setUpNavDrawer() {
		final DrawerLayout drawer = findViewById(R.id.activity_main_layout_drawer);
		final View content = findViewById(R.id.activity_main_content);
		ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
			final Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
			return WindowInsetsCompat.CONSUMED;
		});
		final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, materialToolbar, R.string.content_description_drawer_open, R.string.content_description_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		final NavigationView navigationView = findViewById(R.id.activity_main_navigation);
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
			final ImageView imageViewAvatar = headerView.findViewById(R.id.navigation_drawer_image);
			final TextView textViewHeading = headerView.findViewById(R.id.navigation_drawer_heading);
			final TextView textViewSubtitle = headerView.findViewById(R.id.navigation_drawer_subtitle);
			Glide.with(this).load(AuthSession.avatarUrl()).into(imageViewAvatar);
			if (AuthSession.isAnonymous()) {
				textViewHeading.setText(R.string.anonymous_user);
				textViewSubtitle.setVisibility(View.GONE);
			} else {
				textViewHeading.setText(AuthSession.displayName());
				textViewSubtitle.setText(AuthSession.email());
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
		searchHandler.removeCallbacksAndMessages(null);
	}

	public AlertDialog createAboutCalendarAlert() {
		final View view = LayoutInflater.from(this)
				.inflate(R.layout.image_alert, null);

		return new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.about_calendar)
				.setView(view)
				.setPositiveButton(R.string.ok, null)
				.create();
	}

}
