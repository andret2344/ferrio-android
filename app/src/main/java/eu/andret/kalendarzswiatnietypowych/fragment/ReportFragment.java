package eu.andret.kalendarzswiatnietypowych.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayReport;
import eu.andret.kalendarzswiatnietypowych.util.Util;
import java9.util.concurrent.CompletableFuture;

public class ReportFragment extends DialogFragment {
	private int selectedReason = -1;
	private FirebaseAuth firebaseAuth;

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		firebaseAuth = FirebaseAuth.getInstance();
		return inflater.inflate(R.layout.dialog_report, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final ReportViewModel reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);

		final MaterialToolbar materialToolbar = view.findViewById(R.id.fragment_report_toolbar);
		materialToolbar.setNavigationIcon(R.drawable.baseline_close_24);
		materialToolbar.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
		final TextInputEditText holidayNameEditText = view.findViewById(R.id.dialog_report_holiday_name);
		final TextInputEditText holidayDescEditText = view.findViewById(R.id.dialog_report_holiday_desc);
		final AutoCompleteTextView reasonTextView = view.findViewById(R.id.dialog_report_text_reason_value);
		final TextInputLayout descriptionEditText = view.findViewById(R.id.dialog_report_text_description);
		final MaterialButton send = view.findViewById(R.id.dialog_report_button_send);

		reasonTextView.setOnItemClickListener((parent, view1, position, id) -> {
			send.setEnabled(true);
			selectedReason = position;
		});

		reportViewModel.getHoliday().observe(requireActivity(), holiday -> {
			holidayNameEditText.setText(holiday.getName());
			holidayDescEditText.setText(holiday.getDescription());

			send.setOnClickListener(v -> {
				final String language = Util.getLanguageCode();
				final boolean floating = holiday.getId() < 0;
				final int metadata = Math.abs(holiday.getId());
				final String reportType = requireActivity().getResources().getStringArray(R.array.report_keys)[selectedReason];
				final String description = descriptionEditText.getEditText().getText().toString();
				final HolidayReport holidayReport = new HolidayReport(firebaseAuth.getUid(), metadata, language, reportType, description);
				CompletableFuture.runAsync(() -> {
					final boolean success = sendReport(holidayReport, floating);
					requireActivity().runOnUiThread(() -> {
						if (success) {
							new MaterialAlertDialogBuilder(requireActivity())
									.setTitle(R.string.report_title)
									.setMessage(R.string.report_message)
									.setPositiveButton(R.string.ok, (dialog, which) -> requireActivity().finish())
									.create()
									.show();
						} else {
							new MaterialAlertDialogBuilder(requireActivity())
									.setTitle(R.string.error_title)
									.setMessage(R.string.error_message)
									.setPositiveButton(R.string.ok, null)
									.create()
									.show();
						}
					});
				});
			});
		});
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

	public boolean sendReport(@NonNull final HolidayReport holidayReport, final boolean floating) {
		final String path = floating ? "/floating" : "/fixed";
		try {
			final URL url = new URL("https://api.unusualcalendar.net/v2/report" + path);
			final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			final OutputStream outputStream = connection.getOutputStream();
			outputStream.write(Util.GSON.toJson(holidayReport).getBytes(StandardCharsets.UTF_8));
			final int responseCode = connection.getResponseCode();
			connection.disconnect();
			return responseCode < 400;
		} catch (final IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
