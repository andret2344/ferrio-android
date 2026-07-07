package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.FormResultHandler;
import eu.andret.kalendarzswiatnietypowych.databinding.FragmentSuggestionFixedBinding;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.DeviceMetadata;
import eu.andret.kalendarzswiatnietypowych.util.SimpleTextWatcher;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class FixedSuggestionFragment extends AuthenticatedFragment {
	private Month selectedMonth;
	@Nullable
	private FragmentSuggestionFixedBinding binding;

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		binding = FragmentSuggestionFixedBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final FragmentSuggestionFixedBinding b = binding;
		if (b == null) {
			return;
		}

		final BooleanSupplier condition = () -> !b.fragmentSuggestionFixedMonthValue.getText().toString().isBlank()
				&& !b.fragmentSuggestionFixedNameValue.getText().toString().isBlank()
				&& !b.fragmentSuggestionFixedDescriptionValue.getText().toString().isBlank();

		b.fragmentSuggestionFixedMonthValue.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, Month.values()));
		b.fragmentSuggestionFixedMonthValue.setOnItemClickListener((parent, v, position, id) -> {
			selectedMonth = Month.values()[position];
			final int length = YearMonth.of(Year.now(ZoneId.systemDefault()).getValue(), selectedMonth).lengthOfMonth();
			final List<Integer> items = Stream.iterate(1, i -> i + 1)
					.limit(length)
					.collect(Collectors.toList());
			b.fragmentSuggestionFixedDayValue.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, items));
			b.fragmentSuggestionFixedDayValue.setEnabled(true);
			final String currentDayValue = b.fragmentSuggestionFixedDayValue.getText().toString();
			final int currentDay = Optional.of(currentDayValue)
					.filter(s -> !s.isBlank())
					.map(Integer::parseInt)
					.orElse(1);
			b.fragmentSuggestionFixedDayValue.setText(String.valueOf(Math.min(length, currentDay)), false);
			b.fragmentSuggestionFixedButtonSend.setEnabled(condition.getAsBoolean());
		});

		b.fragmentSuggestionFixedNameValue.addTextChangedListener((SimpleTextWatcher) () -> b.fragmentSuggestionFixedButtonSend.setEnabled(condition.getAsBoolean()));
		b.fragmentSuggestionFixedDescriptionValue.addTextChangedListener((SimpleTextWatcher) () -> b.fragmentSuggestionFixedButtonSend.setEnabled(condition.getAsBoolean()));

		b.fragmentSuggestionFixedName.setHint(Util.requiredHint(requireContext(), R.string.holiday_name));
		b.fragmentSuggestionFixedDescription.setHint(Util.requiredHint(requireContext(), R.string.holiday_description));
		b.fragmentSuggestionFixedMonth.setHint(Util.requiredHint(requireContext(), R.string.month));
		b.fragmentSuggestionFixedDay.setHint(Util.requiredHint(requireContext(), R.string.day));

		Util.bindExpandable(b.fragmentSuggestionFixedInfo, b.fragmentSuggestionFixedNote, b.fragmentSuggestionFixedChevron);

		b.fragmentSuggestionFixedButtonSend.setOnClickListener(v -> {
			final FormResultHandler handler = (FormResultHandler) requireActivity();
			final Month month = selectedMonth;
			final int day = Integer.parseInt(b.fragmentSuggestionFixedDayValue.getText().toString());
			final String name = b.fragmentSuggestionFixedNameValue.getText().toString();
			final String description = b.fragmentSuggestionFixedDescriptionValue.getText().toString();
			final JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("month", month.getValue());
			jsonObject.addProperty("day", day);
			jsonObject.addProperty("name", name);
			jsonObject.addProperty("description", description);
			DeviceMetadata.addTo(jsonObject, requireActivity());
			submitAuthenticated(
					b.fragmentSuggestionFixedButtonSend,
					(token, cancel) -> getApiClient().post(
							getApiClient().buildReportsUrl(ApiClient.REPORT_TYPE_SUGGESTION, ApiClient.HOLIDAY_TYPE_FIXED),
							token, jsonObject.toString(), cancel),
					handler::showSuccessDialog,
					handler::showSubmitError);
		});
	}

	@Override
	public void onDestroyView() {
		binding = null;
		super.onDestroyView();
	}

	@NonNull
	public static FixedSuggestionFragment newInstance() {
		return new FixedSuggestionFragment();
	}
}
