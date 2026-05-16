package eu.andret.kalendarzswiatnietypowych.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.databinding.DialogReportBinding;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayError;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.ReviewHelper;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class ReportDialogFragment extends AuthenticatedDialogFragment {
	private int selectedReason = -1;
	@Nullable
	private DialogReportBinding binding;

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
			@Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		binding = DialogReportBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final DialogReportBinding b = binding;
		if (b == null) {
			return;
		}

		final ReportViewModel reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);

		b.fragmentReportToolbar.setNavigationIcon(R.drawable.baseline_close_24);
		b.fragmentReportToolbar.setNavigationOnClickListener(v -> dismiss());

		b.dialogReportTextReasonValue.setOnItemClickListener((parent, view1, position, id) -> {
			b.dialogReportButtonSend.setEnabled(true);
			selectedReason = position;
		});

		reportViewModel.getHoliday().observe(getViewLifecycleOwner(), holiday -> {
			b.dialogReportHolidayName.setText(holiday.getName());
			if (!holiday.getDescription().isEmpty()) {
				b.dialogReportDivider.setVisibility(View.VISIBLE);
				b.dialogReportHolidayDesc.setText(holiday.getDescription());
				b.dialogReportHolidayDesc.setVisibility(View.VISIBLE);
			}

			b.dialogReportButtonSend.setOnClickListener(v -> {
				final Activity activity = requireActivity();
				final String language = Util.getLanguageCode();
				final String holidayType = holiday.isFloating() ? ApiClient.HOLIDAY_TYPE_FLOATING : ApiClient.HOLIDAY_TYPE_FIXED;
				final int metadata = holiday.getNumericId();
				final String reportType = activity.getResources().getStringArray(R.array.report_keys)[selectedReason];
				final String description = Objects.requireNonNull(b.dialogReportTextDescription.getEditText()).getText().toString();
				final HolidayError holidayReport = new HolidayError(metadata, language, reportType, description);
				final String body = Util.GSON.toJson(holidayReport);
				submitAuthenticated(
						(token, cancel) -> getApiClient().post(
								getApiClient().buildReportsUrl(ApiClient.REPORT_TYPE_ERROR, holidayType),
								token, body, cancel),
						() -> new MaterialAlertDialogBuilder(activity)
								.setTitle(R.string.report_title)
								.setMessage(R.string.report_message)
								.setPositiveButton(R.string.ok, (dialog, which) ->
										ReviewHelper.requestReview(activity, activity::finish))
								.create()
								.show(),
						this::handleApiError);
			});
		});
	}

	@Override
	public void onDestroyView() {
		binding = null;
		super.onDestroyView();
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
