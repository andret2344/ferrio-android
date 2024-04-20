package eu.andret.kalendarzswiatnietypowych.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class ReportFragment extends DialogFragment {
	private int selectedReason = -1;

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_report, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final ReportViewModel reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);

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
				final int metadata = holiday.getId();
				final String reportType = requireActivity().getResources().getStringArray(R.array.report_keys)[selectedReason];
				final String description = descriptionEditText.getEditText().getText().toString();
				Log.d("UHC-fragment", "language=" + language + ", metadata=" + metadata + ", reportType=" + reportType + ", description=" + description);
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

	public boolean sendReport(@NonNull final String text) throws IOException {
		final URL url = new URL("https://dailyquote.andret.eu");
		final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setDoOutput(true);
		final OutputStream outputStream = connection.getOutputStream();
//		final Quote quote = new Quote(text);
//		outputStream.write(Util.GSON.toJson(quote).getBytes(StandardCharsets.UTF_8));
		final int responseCode = connection.getResponseCode();
		connection.disconnect();
		return responseCode < 400;
	}
}
