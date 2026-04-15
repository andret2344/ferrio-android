package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.time.Month;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.fragment.ReportDialogFragment;
import eu.andret.kalendarzswiatnietypowych.fragment.ReportViewModel;
import eu.andret.kalendarzswiatnietypowych.util.ShareCardRenderer;
import eu.andret.kalendarzswiatnietypowych.util.Util;
import eu.andret.kalendarzswiatnietypowych.util.auth.AuthSession;

public class HolidayActivity extends BaseActivity {
	private Holiday currentHoliday;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_holiday);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_holiday_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		registerAdView(findViewById(R.id.activity_holiday_adview_bottom));

		final String holidayId = getIntent().getStringExtra(MainActivity.HOLIDAY);
		if (holidayId == null) {
			finish();
			return;
		}

		holidayViewModel.getHoliday(holidayId).observe(this, optionalHoliday -> optionalHoliday.ifPresent(holiday -> {
			findViewById(R.id.activity_holiday_progress).setVisibility(View.GONE);
			findViewById(R.id.activity_holiday_content).setVisibility(View.VISIBLE);

			currentHoliday = holiday;

			final TextView holidayNameTextView = findViewById(R.id.activity_holiday_name);
			final TextView holidayDateTextView = findViewById(R.id.activity_holiday_date);
			final LinearLayout descContainer = findViewById(R.id.activity_holiday_description_container);

			holidayNameTextView.setText(holiday.getName());

			final String dateText = Util.getFormattedDate(new Pair<>(Month.of(holiday.getMonth()), holiday.getDay()));
			final String countryLabel = getCountryLabel(holiday);
			holidayDateTextView.setText(getString(R.string.date_country, dateText, countryLabel));
			final String countryName = holiday.getCountry() != null && !holiday.getCountry().isBlank()
					? holiday.getCountryName()
					: getString(R.string.international);
			TooltipCompat.setTooltipText(holidayDateTextView, countryName);

			if (holiday.getDescription().isBlank()) {
				final TextView placeholder = new TextView(this);
				placeholder.setText(R.string.no_description);
				placeholder.setTypeface(null, Typeface.ITALIC);
				placeholder.setGravity(Gravity.CENTER);
				placeholder.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
				placeholder.setTextColor(Util.getThemeColor(this, android.R.attr.textColorSecondary));
				descContainer.addView(placeholder);
			} else {
				final String[] paragraphs = holiday.getDescription().split("\n\n");
				for (final String paragraph : paragraphs) {
					final TextView tv = new TextView(this);
					tv.setText(paragraph.trim());
					tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);
					tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.activity_holiday_description_body));
					tv.setTextColor(holidayDateTextView.getCurrentTextColor());
					tv.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.activity_holiday_paragraph_spacing));
					descContainer.addView(tv);
				}
			}
			final String url = holiday.getUrl();
			if (url != null && !url.isBlank()) {
				final Uri targetUri = Uri.parse(url);
				final String scheme = targetUri.getScheme();
				if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
					final MaterialButton buttonReadMore = findViewById(R.id.activity_holiday_button_read_more);
					buttonReadMore.setVisibility(View.VISIBLE);
					buttonReadMore.setOnClickListener(v ->
							startActivity(new Intent(Intent.ACTION_VIEW).setData(targetUri)));
				}
			}
			final ReportViewModel reportViewModel = new ViewModelProvider(this).get(ReportViewModel.class);
			final View reportButton = findViewById(R.id.activity_holiday_button_report);
			reportButton.setEnabled(AuthSession.canSubmitUserContent());
			reportButton.setOnClickListener(v -> {
				reportViewModel.setHoliday(holiday);
				final ReportDialogFragment newFragment = new ReportDialogFragment();
				newFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
				newFragment.show(getSupportFragmentManager(), "report");
			});
		}));
	}

	@NonNull
	private String getCountryLabel(@NonNull final Holiday holiday) {
		if (holiday.getCountry() == null || holiday.getCountry().isBlank()) {
			return String.format("\uD83C\uDF10 %s", getString(R.string.international));
		}
		final String flag = Util.getCountryFlag(holiday.getCountry());
		if (flag == null) {
			return "";
		}
		return String.format("%s %s", flag, holiday.getCountryName());
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
