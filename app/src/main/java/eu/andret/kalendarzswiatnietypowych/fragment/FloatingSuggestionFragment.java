package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

import java.util.function.BooleanSupplier;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.FormResultHandler;
import eu.andret.kalendarzswiatnietypowych.databinding.FragmentSuggestionFloatingBinding;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.DeviceMetadata;
import eu.andret.kalendarzswiatnietypowych.util.SimpleTextWatcher;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class FloatingSuggestionFragment extends AuthenticatedFragment {
	@Nullable
	private FragmentSuggestionFloatingBinding binding;

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		binding = FragmentSuggestionFloatingBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final FragmentSuggestionFloatingBinding b = binding;
		if (b == null) {
			return;
		}

		final BooleanSupplier condition = () -> !b.fragmentSuggestionFloatingDateValue.getText().toString().isBlank()
				&& !b.fragmentSuggestionFloatingNameValue.getText().toString().isBlank()
				&& !b.fragmentSuggestionFloatingDescriptionValue.getText().toString().isBlank();

		b.fragmentSuggestionFloatingButtonSend.setOnClickListener(v -> {
			final FormResultHandler handler = (FormResultHandler) requireActivity();
			final String date = b.fragmentSuggestionFloatingDateValue.getText().toString();
			final String name = b.fragmentSuggestionFloatingNameValue.getText().toString();
			final String description = b.fragmentSuggestionFloatingDescriptionValue.getText().toString();
			final JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("date", date);
			jsonObject.addProperty("name", name);
			jsonObject.addProperty("description", description);
			DeviceMetadata.addTo(jsonObject, requireActivity());
			submitAuthenticated(
					b.fragmentSuggestionFloatingButtonSend,
					(token, cancel) -> getApiClient().post(
							getApiClient().buildReportsUrl(ApiClient.REPORT_TYPE_SUGGESTION, ApiClient.HOLIDAY_TYPE_FLOATING),
							token, jsonObject.toString(), cancel),
					handler::showSuccessDialog,
					handler::showSubmitError);
		});

		b.fragmentSuggestionFloatingNameValue.addTextChangedListener((SimpleTextWatcher) () -> b.fragmentSuggestionFloatingButtonSend.setEnabled(condition.getAsBoolean()));
		b.fragmentSuggestionFloatingDescriptionValue.addTextChangedListener((SimpleTextWatcher) () -> b.fragmentSuggestionFloatingButtonSend.setEnabled(condition.getAsBoolean()));

		b.fragmentSuggestionFloatingName.setHint(Util.requiredHint(requireContext(), R.string.holiday_name));
		b.fragmentSuggestionFloatingDescription.setHint(Util.requiredHint(requireContext(), R.string.holiday_description));
		b.fragmentSuggestionFloatingDate.setHint(Util.requiredHint(requireContext(), R.string.date));

		Util.bindExpandable(b.fragmentSuggestionFloatingInfo, b.fragmentSuggestionFloatingNote, b.fragmentSuggestionFloatingChevron);
	}

	@Override
	public void onDestroyView() {
		binding = null;
		super.onDestroyView();
	}

	@NonNull
	public static FloatingSuggestionFragment newInstance() {
		return new FloatingSuggestionFragment();
	}
}
