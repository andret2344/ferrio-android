package eu.andret.kalendarzswiatnietypowych.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
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
import eu.andret.kalendarzswiatnietypowych.persistance.UpdateDataWorker;

public class MainActivity extends UHCActivity {
	public static final String WIDGET = "widget";
	public static final String MONTH = "month";
	public static final String DAY = "day";
	public static final String FROM = "from";
	public static final String HOLIDAY = "holiday";
	public static final String INTERNET = "INTERNET";

	private ViewPager2 viewPager2;
	private RecyclerView searchListView;
	private MaterialToolbar materialToolbar;
	private final List<HolidayDay> holidayDays = new ArrayList<>();
	private FirebaseAuth firebaseAuth;
	public final ActivityResultLauncher<Intent> activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
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
		viewPager2 = findViewById(R.id.main_pager_months);
		firebaseAuth = FirebaseAuth.getInstance();
		materialToolbar = findViewById(R.id.activity_main_toolbar);
		setSupportActionBar(materialToolbar);

		MobileAds.initialize(this);
		final AdView adView = findViewById(R.id.main_adview_bottom);
		adView.loadAd(new AdRequest.Builder().build());

		setUpNavDrawer();

		viewPager2.setAdapter(new MonthFragmentAdapter(getSupportFragmentManager(), getLifecycle()));
		viewPager2.setCurrentItem(currentMonthValue - 1, false);
		viewPager2.setOffscreenPageLimit(12);
		viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
				materialToolbar.setTitle(getMonthName(position + 1));
			}
		});

		getUHCApplication().getAppRepository().getAllHolidayDays().observe(this, days -> {
			holidayDays.clear();
			holidayDays.addAll(days);
		});

		if (getIntent().getBooleanExtra(INTERNET, false)) {
			final OneTimeWorkRequest updateDataRequest = new OneTimeWorkRequest.Builder(UpdateDataWorker.class).build();
			WorkManager.getInstance(this).enqueue(updateDataRequest);
		}
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
		final List<HolidayDay> list = new ArrayList<>(holidayDays);
		final SearchHolidayAdapter adapter = new SearchHolidayAdapter(this, list);
		searchListView.setAdapter(adapter);
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
			@SuppressLint("NotifyDataSetChanged")
			public boolean onQueryTextChange(final String newText) {
				list.clear();
				if (newText == null || newText.isEmpty()) {
					searchListView.setVisibility(View.INVISIBLE);
					viewPager2.setVisibility(View.VISIBLE);
				} else {
					searchListView.setVisibility(View.VISIBLE);
					viewPager2.setVisibility(View.INVISIBLE);
					holidayDays.stream()
							.map(holidayDay -> {
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
		final ImageView imageViewAvatar = headerView.findViewById(R.id.navigation_drawer_image);
		final TextView textViewHeading = headerView.findViewById(R.id.navigation_drawer_heading);
		final TextView textViewSubtitle = headerView.findViewById(R.id.navigation_drawer_subtitle);
		final MenuItem missing = navigationView.getMenu().findItem(R.id.menu_drawer_missing);
		final MenuItem suggestions = navigationView.getMenu().findItem(R.id.menu_drawer_suggestions);
		final MenuItem reports = navigationView.getMenu().findItem(R.id.menu_drawer_reports);

		final FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
			missing.setEnabled(!user.isAnonymous());
			suggestions.setEnabled(!user.isAnonymous());
			reports.setEnabled(!user.isAnonymous());
			final Picasso picasso = Picasso.get();
			if (user.isAnonymous()) {
				picasso.load(String.format("https://gravatar.com/avatar/%s?d=identicon", user.getUid()))
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
			if (menuItem.getItemId() == R.id.menu_drawer_missing) {
				startActivity(new Intent(this, MissingActivity.class));
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
