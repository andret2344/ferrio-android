package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public class HolidayActivity extends UHCActivity {
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_holiday);

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
