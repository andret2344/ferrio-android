package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.persistance.SharedViewModel;

public class HolidayActivity extends AppCompatActivity {
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		setContentView(R.layout.activity_holiday);

		Optional.ofNullable(getSupportActionBar()).ifPresent(actionBar -> {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
			actionBar.setDisplayShowCustomEnabled(false);
		});

		final Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(ContextCompat.getColor(this, R.color.dynamic_action_bar));
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				finish();
			}
		});

		final TextView nameTextView = findViewById(R.id.activity_holiday_name);
		final TextView descTextView = findViewById(R.id.activity_holiday_description);
		final int holidayId = getIntent().getIntExtra("holiday", 0);

		final SharedViewModel sharedViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(SharedViewModel.INITIALIZER))
				.get(SharedViewModel.class);

		sharedViewModel.getHoliday(holidayId).observeForever(holiday -> {
			nameTextView.setText(holiday.getName());
			descTextView.setText(holiday.getDescription());
		});

		MobileAds.initialize(this);
		final AdView adView = findViewById(R.id.activity_holiday_adview_bottom);
		adView.loadAd(new AdRequest.Builder().build());
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getOnBackPressedDispatcher().onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
