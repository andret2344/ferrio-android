package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.function.BooleanSupplier;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.util.SimpleTextWatcher;
import java9.util.concurrent.CompletableFuture;

public class MissingFloatingFragment extends Fragment {
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_missing_floating, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final Bundle arguments = getArguments();
		if (arguments == null) {
			return;
		}
		final String userId = arguments.getString("userId");

		final TextView textViewDate = view.findViewById(R.id.fragment_missing_floating_date_value);
		final TextView editTextName = view.findViewById(R.id.fragment_missing_floating_name_value);
		final TextView editTextDescription = view.findViewById(R.id.fragment_missing_floating_description_value);

		final BooleanSupplier condition = () -> !textViewDate.getText().toString().isBlank()
				&& !editTextName.getText().toString().isBlank()
				&& !editTextDescription.getText().toString().isBlank();

		final MaterialButton button = view.findViewById(R.id.fragment_missing_floating_button_send);
		button.setOnClickListener(v -> {
			final String date = textViewDate.getText().toString();
			final String name = editTextName.getText().toString();
			final String description = editTextDescription.getText().toString();
			CompletableFuture.runAsync(() -> {
				final boolean success = send(userId, date, name, description);
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

		editTextName.addTextChangedListener((SimpleTextWatcher) () -> button.setEnabled(condition.getAsBoolean()));
		editTextDescription.addTextChangedListener((SimpleTextWatcher) () -> button.setEnabled(condition.getAsBoolean()));

	}

	@NonNull
	public static MissingFloatingFragment newInstance(@Nullable final String userId) {
		final Bundle args = new Bundle();
		args.putString("userId", userId);
		final MissingFloatingFragment fragment = new MissingFloatingFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public boolean send(@Nullable final String userId, @NonNull final String date, @NonNull final String name, @NonNull final String description) {
		try {
			final URL url = new URL("https://api.unusualcalendar.net/v2/missing/floating");
			final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			final OutputStream outputStream = connection.getOutputStream();
			final String json = String.format(Locale.getDefault(), "{\"user_id\":\"%s\",\"date\":\"%s\",\"name\":\"%s\",\"description\":\"%s\"}", userId, date, name, description);
			outputStream.write(json.getBytes());
			final int responseCode = connection.getResponseCode();
			connection.disconnect();
			return responseCode < 400;
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
