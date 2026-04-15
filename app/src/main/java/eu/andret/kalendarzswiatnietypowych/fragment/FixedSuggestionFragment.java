package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.FormResultHandler;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.SimpleTextWatcher;

public class FixedSuggestionFragment extends AuthenticatedFragment {
	private Month selectedMonth;

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_suggestion_fixed, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final AutoCompleteTextView textViewMonth = view.findViewById(R.id.fragment_suggestion_fixed_month_value);
		final AutoCompleteTextView textViewDay = view.findViewById(R.id.fragment_suggestion_fixed_day_value);
		final EditText editTextName = view.findViewById(R.id.fragment_suggestion_fixed_name_value);
		final EditText editTextDescription = view.findViewById(R.id.fragment_suggestion_fixed_description_value);
		final MaterialButton button = view.findViewById(R.id.fragment_suggestion_fixed_button_send);

		final BooleanSupplier condition = () -> !textViewMonth.getText().toString().isBlank()
				&& !editTextName.getText().toString().isBlank()
				&& !editTextDescription.getText().toString().isBlank();

		textViewMonth.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, Month.values()));
		textViewMonth.setOnItemClickListener((parent, v, position, id) -> {
			selectedMonth = Month.values()[position];
			final int length = YearMonth.of(Year.now().getValue(), selectedMonth).lengthOfMonth();
			final List<Integer> items = Stream.iterate(1, i -> i + 1)
					.limit(length)
					.collect(Collectors.toList());
			textViewDay.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, items));
			textViewDay.setEnabled(true);
			final String currentDayValue = textViewDay.getText().toString();
			final int currentDay = Optional.of(currentDayValue)
					.filter(s -> !s.isBlank())
					.map(Integer::parseInt)
					.orElse(1);
			textViewDay.setText(String.valueOf(Math.min(length, currentDay)), false);
			button.setEnabled(condition.getAsBoolean());
		});

		editTextName.addTextChangedListener((SimpleTextWatcher) () -> button.setEnabled(condition.getAsBoolean()));
		editTextDescription.addTextChangedListener((SimpleTextWatcher) () -> button.setEnabled(condition.getAsBoolean()));

		button.setOnClickListener(v -> {
			final FormResultHandler handler = (FormResultHandler) requireActivity();
			final Month month = selectedMonth;
			final int day = Integer.parseInt(textViewDay.getText().toString());
			final String name = editTextName.getText().toString();
			final String description = editTextDescription.getText().toString();
			final JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("month", month.getValue());
			jsonObject.addProperty("day", day);
			jsonObject.addProperty("name", name);
			jsonObject.addProperty("description", description);
			submitAuthenticated(
					(token, cancel) -> getApiClient().post(
							getApiClient().buildReportsUrl(ApiClient.REPORT_TYPE_SUGGESTION, ApiClient.HOLIDAY_TYPE_FIXED),
							token, jsonObject.toString(), cancel),
					handler::showSuccessDialog,
					ex -> {
						if (ex.isBanned()) {
							handler.showBanDialog(ex.getBanReason());
						} else {
							handler.showErrorDialog();
						}
					});
		});
	}

	@NonNull
	public static FixedSuggestionFragment newInstance() {
		return new FixedSuggestionFragment();
	}
}
