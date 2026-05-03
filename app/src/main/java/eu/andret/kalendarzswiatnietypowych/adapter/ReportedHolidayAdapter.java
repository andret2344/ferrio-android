package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.ReportedHoliday;
import eu.andret.kalendarzswiatnietypowych.util.ReportDetailsDialog;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class ReportedHolidayAdapter extends ListAdapter<ReportedHoliday, ReportedHolidayAdapter.ViewHolder> {

	private static final DiffUtil.ItemCallback<ReportedHoliday> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
		@Override
		public boolean areItemsTheSame(@NonNull final ReportedHoliday oldItem, @NonNull final ReportedHoliday newItem) {
			return oldItem.getId() == newItem.getId();
		}

		@Override
		public boolean areContentsTheSame(@NonNull final ReportedHoliday oldItem, @NonNull final ReportedHoliday newItem) {
			return oldItem.equals(newItem);
		}
	};

	private final Map<String, String> reasonMap;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewDate;
		private final TextView textViewStatus;
		private final TextView textViewName;
		private final TextView textViewDescription;
		private final ImageView commentIndicator;

		public ViewHolder(final View view) {
			super(view);
			textViewDate = view.findViewById(R.id.adapter_reported_holiday_text_date);
			textViewStatus = view.findViewById(R.id.adapter_reported_holiday_chip);
			textViewName = view.findViewById(R.id.adapter_reported_holiday_text_name);
			textViewDescription = view.findViewById(R.id.adapter_reported_holiday_text_description);
			commentIndicator = view.findViewById(R.id.adapter_reported_holiday_comment_indicator);
		}
	}

	public ReportedHolidayAdapter(@NonNull final Context context) {
		super(DIFF_CALLBACK);
		final Resources resources = context.getResources();
		final String[] keys = resources.getStringArray(R.array.report_keys);
		final String[] values = resources.getStringArray(R.array.report_values);
		reasonMap = new HashMap<>(keys.length);
		for (int i = 0; i < keys.length; i++) {
			reasonMap.put(keys[i], values[i]);
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_reported_holiday, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final ReportedHoliday holiday = getItem(position);
		final String reason = mapReason(holiday.getReportType());
		final String formattedDate = holiday.getDatetime().format(Util.getDateTimeFormatter());

		viewHolder.textViewDate.setText(formattedDate);
		viewHolder.textViewName.setText(reason);
		viewHolder.textViewDescription.setText(holiday.getDescription());
		Util.applyStatusBadge(viewHolder.textViewStatus, holiday.getReportState());

		final boolean hasComment = holiday.getComment() != null && !holiday.getComment().isEmpty();
		viewHolder.commentIndicator.setVisibility(hasComment ? View.VISIBLE : View.GONE);

		viewHolder.itemView.setOnClickListener(view -> ReportDetailsDialog.show(
				view.getContext(),
				formattedDate,
				reason,
				holiday.getDescription(),
				holiday.getComment()));
	}

	private String mapReason(final String reportType) {
		return reasonMap.getOrDefault(reportType, reportType);
	}
}
