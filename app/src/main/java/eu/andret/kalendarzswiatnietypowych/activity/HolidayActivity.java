package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.fragment.ReportDialogFragment;
import eu.andret.kalendarzswiatnietypowych.fragment.ReportViewModel;
import eu.andret.kalendarzswiatnietypowych.util.ShareCardRenderer;

public class HolidayActivity extends BaseActivity {
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault());
	private Holiday currentHoliday;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_holiday);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_holiday_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		this.<AdView>findViewById(R.id.activity_holiday_adview_bottom)
				.loadAd(new AdRequest.Builder().build());

		final String holidayId = getIntent().getStringExtra(MainActivity.HOLIDAY);
		if (holidayId == null) {
			finish();
			return;
		}

		holidayViewModel.getHoliday(holidayId).observe(this, optionalHoliday -> optionalHoliday.ifPresent(holiday -> {
			findViewById(R.id.activity_holiday_progress).setVisibility(View.GONE);
			findViewById(R.id.activity_holiday_content).setVisibility(View.VISIBLE);

			currentHoliday = holiday;
			final LocalDate date = LocalDate.of(Year.now().getValue(), Month.of(holiday.getMonth()), holiday.getDay());

			final TextView holidayNameTextView = findViewById(R.id.activity_holiday_name);
			final TextView holidayDateTextView = findViewById(R.id.activity_holiday_date);
			final TextView holidayDescTextView = findViewById(R.id.activity_holiday_description);

			holidayNameTextView.setText(holiday.getName());

			final String dateText = formatter.format(date);
			final String countryLabel = getCountryLabel(holiday);
			holidayDateTextView.setText(getString(R.string.date_country, dateText, countryLabel));

			if (holiday.getDescription().isBlank()) {
				holidayDescTextView.setText(R.string.no_description);
				holidayDescTextView.setTypeface(null, Typeface.ITALIC);
				holidayDescTextView.setGravity(Gravity.CENTER);
			} else {
				holidayDescTextView.setText(holiday.getDescription());
			}
			if (!holiday.getUrl().isBlank()) {
				final Uri targetUri = Uri.parse(holiday.getUrl());
				final String scheme = targetUri.getScheme();
				if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
					final MaterialButton buttonReadMore = findViewById(R.id.activity_holiday_button_read_more);
					buttonReadMore.setVisibility(View.VISIBLE);
					buttonReadMore.setOnClickListener(v ->
							startActivity(new Intent(Intent.ACTION_VIEW).setData(targetUri)));
				}
			}
			final ReportViewModel reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
			findViewById(R.id.activity_holiday_button_report).setOnClickListener(v -> {
				reportViewModel.setHoliday(holiday);
				final ReportDialogFragment newFragment = new ReportDialogFragment();
				newFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_NoActionBar);
				newFragment.show(getSupportFragmentManager(), "report");
			});
		}));
	}

	@NonNull
	private String getCountryLabel(@NonNull final Holiday holiday) {
		if (holiday.getCountry() == null || holiday.getCountry().isBlank()) {
			return String.format("\uD83C\uDF10 %s", getString(R.string.international));
		}
		final Emoji emoji = EmojiManager.getForAlias(holiday.getCountry().toLowerCase(Locale.ROOT));
		if (emoji == null) {
			return "";
		}
		return String.format("%s %s", emoji.getUnicode(), holiday.getCountryName());
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.holiday, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		if (item.getItemId() == R.id.menu_holiday_share && currentHoliday != null) {
			ShareCardRenderer.shareHoliday(this, currentHoliday);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
