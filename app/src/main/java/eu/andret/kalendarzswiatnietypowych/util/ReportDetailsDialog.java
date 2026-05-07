package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.databinding.DialogReportDetailsBinding;

public final class ReportDetailsDialog {

	private ReportDetailsDialog() {
	}

	public static void show(@NonNull final Context context, @NonNull final String meta,
			@NonNull final String reason, @Nullable final String description,
			@Nullable final String comment) {
		final DialogReportDetailsBinding binding = DialogReportDetailsBinding.inflate(LayoutInflater.from(context));

		binding.dialogReportDetailsMeta.setText(meta);
		binding.dialogReportDetailsReason.setText(reason);
		binding.dialogReportDetailsDescription.setText(description != null && !description.isEmpty()
				? description
				: context.getString(R.string.report_details_no_description));

		if (comment != null && !comment.isEmpty()) {
			binding.dialogReportDetailsCommentLabel.setVisibility(View.VISIBLE);
			binding.dialogReportDetailsComment.setVisibility(View.VISIBLE);
			binding.dialogReportDetailsComment.setText(comment);
		}

		new MaterialAlertDialogBuilder(context)
				.setTitle(R.string.report_details_title)
				.setView(binding.getRoot())
				.setPositiveButton(R.string.close, null)
				.show();
	}
}
