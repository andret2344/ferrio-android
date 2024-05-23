package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.util.SimpleTextWatcher;
import java9.util.concurrent.CompletableFuture;

public class MissingFixedFragment extends Fragment {
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_missing_fixed, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments == null) {
			return;
		}
		final String userId = arguments.getString("userId");

		final AutoCompleteTextView textViewMonth = view.findViewById(R.id.fragment_missing_fixed_month_value);
		final AutoCompleteTextView textViewDay = view.findViewById(R.id.fragment_missing_fixed_day_value);
		final EditText editTextName = view.findViewById(R.id.fragment_missing_fixed_name_value);
		final EditText editTextDescription = view.findViewById(R.id.fragment_missing_fixed_description_value);
		final MaterialButton button = view.findViewById(R.id.fragment_missing_fixed_button_send);

		final BooleanSupplier condition = () -> !textViewMonth.getText().toString().isBlank()
				&& !editTextName.getText().toString().isBlank()
				&& !editTextDescription.getText().toString().isBlank();

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
			button.setEnabled(condition.getAsBoolean());
		});

		editTextName.addTextChangedListener((SimpleTextWatcher) () -> button.setEnabled(condition.getAsBoolean()));
		editTextDescription.addTextChangedListener((SimpleTextWatcher) () -> button.setEnabled(condition.getAsBoolean()));

		button.setOnClickListener(v -> {
			final Month month = Month.valueOf(textViewMonth.getText().toString());
			final int day = Integer.parseInt(textViewDay.getText().toString());
			final String name = editTextName.getText().toString();
			final String description = editTextDescription.getText().toString();
			CompletableFuture.runAsync(() -> {
				final boolean success = send(userId, month.getValue(), day, name, description);
				requireActivity().runOnUiThread(() -> {
					if (success) {
						Toast.makeText(requireActivity(), "Sent!", Toast.LENGTH_SHORT).show();
						requireActivity().getSupportFragmentManager().popBackStackImmediate();
					} else {
						Toast.makeText(requireActivity(), "Error!", Toast.LENGTH_SHORT).show();
					}
				});
			});
		});
	}

	@NonNull
	public static MissingFixedFragment newInstance(@Nullable final String userId) {
		final Bundle args = new Bundle();
		args.putString("userId", userId);
		final MissingFixedFragment fragment = new MissingFixedFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public boolean send(@Nullable final String userId, final int month, final int day, @NonNull final String name, @NonNull final String description) {
		try {
			final URL url = new URL("https://api.unusualcalendar.net/v2/missing/fixed");
			final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			final OutputStream outputStream = connection.getOutputStream();
			final String json = String.format(Locale.getDefault(), "{\"user_id\":\"%s\",\"month\":%d,\"day\":%d,\"name\":\"%s\",\"description\":\"%s\"}", userId, month, day, name, description);
			outputStream.write(json.getBytes());
			final int responseCode = connection.getResponseCode();
			connection.disconnect();
			return responseCode < 400;
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
