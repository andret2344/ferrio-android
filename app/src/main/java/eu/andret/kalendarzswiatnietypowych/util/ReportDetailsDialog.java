package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import eu.andret.kalendarzswiatnietypowych.R;

public final class ReportDetailsDialog {

	private ReportDetailsDialog() {
	}

	public static void show(@NonNull final Context context, @NonNull final String meta,
			@NonNull final String reason, @Nullable final String description,
			@Nullable final String comment) {
		final View view = LayoutInflater.from(context)
				.inflate(R.layout.dialog_report_details, null, false);

		final TextView metaView = view.findViewById(R.id.dialog_report_details_meta);
		final TextView reasonView = view.findViewById(R.id.dialog_report_details_reason);
		final TextView descriptionView = view.findViewById(R.id.dialog_report_details_description);
		final TextView commentLabel = view.findViewById(R.id.dialog_report_details_comment_label);
		final TextView commentView = view.findViewById(R.id.dialog_report_details_comment);

		metaView.setText(meta);
		reasonView.setText(reason);
		descriptionView.setText(description != null && !description.isEmpty()
				? description
				: context.getString(R.string.report_details_no_description));

		if (comment != null && !comment.isEmpty()) {
			commentLabel.setVisibility(View.VISIBLE);
			commentView.setVisibility(View.VISIBLE);
			commentView.setText(comment);
		}

		new MaterialAlertDialogBuilder(context)
				.setTitle(R.string.report_details_title)
				.setView(view)
				.setPositiveButton(R.string.close, null)
				.show();
	}
}
