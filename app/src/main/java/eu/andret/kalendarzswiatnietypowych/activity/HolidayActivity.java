package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.fragment.ReportFragment;
import eu.andret.kalendarzswiatnietypowych.fragment.ReportViewModel;

public class HolidayActivity extends UHCActivity {


	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_holiday);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_holiday_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		MobileAds.initialize(this);
		this.<AdView>findViewById(R.id.activity_holiday_adview_bottom)
				.loadAd(new AdRequest.Builder().build());

		final Holiday holiday = getIntent().getParcelableExtra(MainActivity.HOLIDAY);
		if (holiday == null) {
			return;
		}
		this.<TextView>findViewById(R.id.activity_holiday_name).setText(holiday.getName());
		this.<TextView>findViewById(R.id.activity_holiday_description).setText(holiday.getDescription());
		if (!holiday.getUrl().isBlank()) {
			final MaterialButton buttonReadMore = findViewById(R.id.activity_holiday_button_read_more);
			buttonReadMore.setVisibility(View.VISIBLE);
			final Uri targetUri = Uri.parse(holiday.getUrl());
			buttonReadMore.setOnClickListener(v ->
					startActivity(new Intent(Intent.ACTION_VIEW).setData(targetUri)));
		}
		final ReportViewModel reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
		findViewById(R.id.activity_holiday_button_report).setOnClickListener(v -> {
			reportViewModel.setHoliday(holiday);
			final FragmentManager fragmentManager = getSupportFragmentManager();
			final ReportFragment newFragment = new ReportFragment();
			final FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.add(android.R.id.content, newFragment)
					.addToBackStack(null)
					.commit();
		});
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
