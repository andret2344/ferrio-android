package eu.andret.kalendarzswiatnietypowych.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import eu.andret.kalendarzswiatnietypowych.R;

public class ReportFragment extends DialogFragment {
	private ReportViewModel reportViewModel;

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dialog_report, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		reportViewModel = new ViewModelProvider(requireActivity()).get(ReportViewModel.class);

		final TextInputEditText holidayNameEditText = view.findViewById(R.id.dialog_report_holiday_name);
		final TextInputEditText holidayDescEditText = view.findViewById(R.id.dialog_report_holiday_desc);
		reportViewModel.getHoliday().observe(requireActivity(), holiday -> {
			holidayNameEditText.setText(holiday.getName());
			holidayDescEditText.setText(holiday.getDescription());
		});
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		final Window window = dialog.getWindow();
		if (window != null) {
			window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		}

		return dialog;
	}
}
