package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.appbar.MaterialToolbar;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.andret.kalendarzswiatnietypowych.R;

public class MissingActivity extends UHCActivity {
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_missing);

		final MaterialToolbar materialToolbar = findViewById(R.id.activity_missing_toolbar);
		setSupportActionBar(materialToolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));

		final AutoCompleteTextView textViewMonth = findViewById(R.id.activity_missing_month_value);
		final AutoCompleteTextView textViewDay = findViewById(R.id.activity_missing_day_value);
		textViewMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Month.values()));
		textViewMonth.setOnItemClickListener((parent, view, position, id) -> {
			final int length = YearMonth.of(Year.now().getValue(), Month.values()[position]).lengthOfMonth();
			final List<Integer> items = Stream.iterate(1, i -> i + 1).limit(length).collect(Collectors.toList());
			textViewDay.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
			textViewDay.setEnabled(true);
			final String currentDayValue = textViewDay.getText().toString();
			final int currentDay = Optional.of(currentDayValue)
					.filter(s -> !s.isBlank())
					.map(Integer::parseInt)
					.orElse(1);
			textViewDay.setText(String.valueOf(Math.min(length, currentDay)), false);
		});

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				finish();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getOnBackPressedDispatcher().onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
