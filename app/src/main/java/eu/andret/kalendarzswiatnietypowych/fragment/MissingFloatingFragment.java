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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.BooleanSupplier;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.util.ConnectivityUtil;
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
				try {
					final JSONObject jsonObject = new JSONObject()
							.put("user_id", userId)
							.put("date", date)
							.put("name", name)
							.put("description", description);
					final boolean success = ConnectivityUtil.send("missing/floating", jsonObject);
					requireActivity().runOnUiThread(() -> {
						if (success) {
							Toast.makeText(requireActivity(), "Sent!", Toast.LENGTH_SHORT).show();
							requireActivity().getSupportFragmentManager().popBackStackImmediate();
						} else {
							Toast.makeText(requireActivity(), "Error!", Toast.LENGTH_SHORT).show();
						}
					});
				} catch (@NonNull final JSONException ex) {
					Toast.makeText(requireActivity(), "Error!", Toast.LENGTH_SHORT).show();
				}
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
}
