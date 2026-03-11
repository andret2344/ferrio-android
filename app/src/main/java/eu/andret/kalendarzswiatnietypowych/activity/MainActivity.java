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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
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
import eu.andret.kalendarzswiatnietypowych.util.LoadState;

public class MainActivity extends BaseActivity implements DayClickListener {
	public static final String WIDGET = "widget";
	public static final String MONTH = "month";
	public static final String DAY = "day";
	public static final String FROM = "from";
	public static final String HOLIDAY = "holiday";
	public static final String INTERNET = "INTERNET";

	private static final long SEARCH_DEBOUNCE_MS = 300;

	private ViewPager2 viewPager2;
	private RecyclerView searchListView;
	private MaterialToolbar materialToolbar;
	private final List<HolidayDay> holidayDays = new ArrayList<>();
	private SearchHolidayAdapter searchAdapter;
	private final Handler searchHandler = new Handler(Looper.getMainLooper());
	private FirebaseAuth firebaseAuth;
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
		firebaseAuth = FirebaseAuth.getInstance();
		materialToolbar = findViewById(R.id.activity_main_toolbar);
		setSupportActionBar(materialToolbar);

		final AdView adView = findViewById(R.id.main_adview_bottom);
		adView.loadAd(new AdRequest.Builder().build());

		setUpNavDrawer();

		viewPager2.setAdapter(new MonthFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
		viewPager2.setCurrentItem(currentMonthValue - 1, false);
		viewPager2.setOffscreenPageLimit(12);
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

		holidayViewModel.getLoadState().observe(this, state -> {
			if (state != LoadState.LOADING) {
				swipeRefreshLayout.setRefreshing(false);
			}
			if (state == LoadState.ERROR) {
				Snackbar.make(viewPager2, R.string.refresh_error, Snackbar.LENGTH_LONG)
						.setAction(R.string.retry, v -> getFerrioApplication().getAppRepository().refresh())
						.show();
			}
		});

		getFerrioApplication().getAppRepository().getAllHolidayDays().observe(this, days -> {
			holidayDays.clear();
			holidayDays.addAll(days);
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
						final List<HolidayDay> snapshot = new ArrayList<>(holidayDays);
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
		final int itemId = item.getItemId();
		if (itemId == R.id.menu_main_today) {
			viewPager2.setCurrentItem(LocalDate.now().getMonthValue() - 1);
		}
		return true;
	}

	private void setUpNavDrawer() {
		final DrawerLayout drawer = findViewById(R.id.activity_main_layout_drawer);
		final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, materialToolbar, R.string.content_description_drawer_open, R.string.content_description_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		final NavigationView navigationView = findViewById(R.id.activity_main_navigation);
		final View headerView = navigationView.getHeaderView(0);
		final MenuItem suggest = navigationView.getMenu().findItem(R.id.menu_drawer_suggest);
		final MenuItem suggestions = navigationView.getMenu().findItem(R.id.menu_drawer_suggestions);
		final MenuItem reports = navigationView.getMenu().findItem(R.id.menu_drawer_reports);

		final FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null && headerView != null) {
			final ImageView imageViewAvatar = headerView.findViewById(R.id.navigation_drawer_image);
			final TextView textViewHeading = headerView.findViewById(R.id.navigation_drawer_heading);
			final TextView textViewSubtitle = headerView.findViewById(R.id.navigation_drawer_subtitle);
			suggest.setEnabled(!user.isAnonymous());
			suggestions.setEnabled(!user.isAnonymous());
			reports.setEnabled(!user.isAnonymous());
			final Picasso picasso = Picasso.get();
			if (user.isAnonymous()) {
				picasso.load(String.format("https://gravatar.com/avatar/%s?d=identicon", sha256(user.getUid())))
						.into(imageViewAvatar);
				textViewHeading.setText(R.string.anonymous_user);
				textViewSubtitle.setVisibility(View.GONE);
			} else {
				picasso.load(user.getPhotoUrl())
						.into(imageViewAvatar);
				textViewHeading.setText(user.getDisplayName());
				textViewSubtitle.setText(user.getEmail());
			}
		}

		navigationView.setNavigationItemSelectedListener(menuItem -> {
			if (menuItem.getItemId() == R.id.menu_drawer_suggest) {
				startActivity(new Intent(this, SuggestionActivity.class));
				drawer.close();
			} else if (menuItem.getItemId() == R.id.menu_drawer_suggestions) {
				startActivity(new Intent(this, SuggestionsActivity.class));
				drawer.close();
			} else if (menuItem.getItemId() == R.id.menu_drawer_reports) {
				startActivity(new Intent(this, ReportsActivity.class));
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

	public AlertDialog createAboutCalendarAlert() {
		final View view = LayoutInflater.from(this)
				.inflate(R.layout.image_alert, null);

		return new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.about_calendar)
				.setView(view)
				.setPositiveButton(R.string.ok, null)
				.create();
	}

	@NonNull
	private String sha256(@NonNull final String input) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			final StringBuilder hexString = new StringBuilder();
			for (final byte b : hash) {
				final String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (final NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}
}
