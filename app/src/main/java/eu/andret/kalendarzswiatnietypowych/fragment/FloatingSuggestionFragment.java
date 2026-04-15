package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonObject;

import java.util.function.BooleanSupplier;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.FormResultHandler;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.SimpleTextWatcher;

public class FloatingSuggestionFragment extends AuthenticatedFragment {
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_suggestion_floating, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final TextView textViewDate = view.findViewById(R.id.fragment_suggestion_floating_date_value);
		final TextView editTextName = view.findViewById(R.id.fragment_suggestion_floating_name_value);
		final TextView editTextDescription = view.findViewById(R.id.fragment_suggestion_floating_description_value);

		final BooleanSupplier condition = () -> !textViewDate.getText().toString().isBlank()
				&& !editTextName.getText().toString().isBlank()
				&& !editTextDescription.getText().toString().isBlank();

		final MaterialButton button = view.findViewById(R.id.fragment_suggestion_floating_button_send);
		button.setOnClickListener(v -> {
			final FormResultHandler handler = (FormResultHandler) requireActivity();
			final String date = textViewDate.getText().toString();
			final String name = editTextName.getText().toString();
			final String description = editTextDescription.getText().toString();
			final JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("date", date);
			jsonObject.addProperty("name", name);
			jsonObject.addProperty("description", description);
			submitAuthenticated(
					(token, cancel) -> getApiClient().post(
							getApiClient().buildReportsUrl(ApiClient.REPORT_TYPE_SUGGESTION, ApiClient.HOLIDAY_TYPE_FLOATING),
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

		editTextName.addTextChangedListener((SimpleTextWatcher) () -> button.setEnabled(condition.getAsBoolean()));
		editTextDescription.addTextChangedListener((SimpleTextWatcher) () -> button.setEnabled(condition.getAsBoolean()));
	}

	@NonNull
	public static FloatingSuggestionFragment newInstance() {
		return new FloatingSuggestionFragment();
	}
}
