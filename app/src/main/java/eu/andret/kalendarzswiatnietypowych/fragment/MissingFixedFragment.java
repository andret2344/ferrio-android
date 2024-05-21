package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.andret.kalendarzswiatnietypowych.R;

public class MissingFixedFragment extends Fragment {
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_missing_fixed, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		final AutoCompleteTextView textViewMonth = view.findViewById(R.id.fragment_missing_fixed_month_value);
		final AutoCompleteTextView textViewDay = view.findViewById(R.id.fragment_missing_fixed_day_value);
		textViewMonth.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, Month.values()));
		textViewMonth.setOnItemClickListener((parent, v, position, id) -> {
			final int length = YearMonth.of(Year.now().getValue(), Month.values()[position]).lengthOfMonth();
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
		});
	}

	@NonNull
	public static MissingFixedFragment newInstance() {
		return new MissingFixedFragment();
	}
}
