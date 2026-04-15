package eu.andret.kalendarzswiatnietypowych.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayError;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class ReportDialogFragment extends AuthenticatedDialogFragment {
	private int selectedReason = -1;

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_report, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final ReportViewModel reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);

		final MaterialToolbar materialToolbar = view.findViewById(R.id.fragment_report_toolbar);
		materialToolbar.setNavigationIcon(R.drawable.baseline_close_24);
		materialToolbar.setNavigationOnClickListener(v -> dismiss());
		final TextView holidayNameTextView = view.findViewById(R.id.dialog_report_holiday_name);
		final TextView holidayDescTextView = view.findViewById(R.id.dialog_report_holiday_desc);
		final View divider = view.findViewById(R.id.dialog_report_divider);
		final AutoCompleteTextView reasonTextView = view.findViewById(R.id.dialog_report_text_reason_value);
		final TextInputLayout descriptionEditText = view.findViewById(R.id.dialog_report_text_description);
		final MaterialButton send = view.findViewById(R.id.dialog_report_button_send);

		reasonTextView.setOnItemClickListener((parent, view1, position, id) -> {
			send.setEnabled(true);
			selectedReason = position;
		});

		reportViewModel.getHoliday().observe(getViewLifecycleOwner(), holiday -> {
			holidayNameTextView.setText(holiday.getName());
			if (!holiday.getDescription().isEmpty()) {
				divider.setVisibility(View.VISIBLE);
				holidayDescTextView.setText(holiday.getDescription());
				holidayDescTextView.setVisibility(View.VISIBLE);
			}

			send.setOnClickListener(v -> {
				final Activity activity = requireActivity();
				final String language = Util.getLanguageCode();
				final String holidayType = holiday.isFloating() ? ApiClient.HOLIDAY_TYPE_FLOATING : ApiClient.HOLIDAY_TYPE_FIXED;
				final int metadata = holiday.getNumericId();
				final String reportType = activity.getResources().getStringArray(R.array.report_keys)[selectedReason];
				final String description = Objects.requireNonNull(descriptionEditText.getEditText()).getText().toString();
				final HolidayError holidayReport = new HolidayError(metadata, language, reportType, description);
				final String body = Util.GSON.toJson(holidayReport);
				submitAuthenticated(
						(token, cancel) -> getApiClient().post(
								getApiClient().buildReportsUrl(ApiClient.REPORT_TYPE_ERROR, holidayType),
								token, body, cancel),
						() -> new MaterialAlertDialogBuilder(activity)
								.setTitle(R.string.report_title)
								.setMessage(R.string.report_message)
								.setPositiveButton(R.string.ok, (dialog, which) -> activity.finish())
								.create()
								.show(),
						this::handleApiError);
			});
		});
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (dialog.getWindow() != null) {
			dialog.getWindow().setWindowAnimations(R.style.DialogSlideAnimation);
		}
		return dialog;
	}
}
