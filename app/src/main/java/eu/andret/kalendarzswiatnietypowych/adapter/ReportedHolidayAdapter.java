package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.ReportedHoliday;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class ReportedHolidayAdapter extends RecyclerView.Adapter<ReportedHolidayAdapter.ViewHolder> {
	private final List<ReportedHoliday> holidays;
	private final Map<String, String> reasonMap;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewDate;
		private final TextView textViewStatus;
		private final TextView textViewName;
		private final TextView textViewDescription;

		public ViewHolder(final View view) {
			super(view);
			textViewDate = view.findViewById(R.id.adapter_reported_holiday_text_date);
			textViewStatus = view.findViewById(R.id.adapter_reported_holiday_chip);
			textViewName = view.findViewById(R.id.adapter_reported_holiday_text_name);
			textViewDescription = view.findViewById(R.id.adapter_reported_holiday_text_description);
		}
	}

	public ReportedHolidayAdapter(final Context context, final List<ReportedHoliday> holidays) {
		this.holidays = holidays;
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
		final ReportedHoliday holiday = holidays.get(position);

		viewHolder.textViewDate.setText(holiday.getDatetime().format(Util.getDateTimeFormatter()));
		viewHolder.textViewName.setText(mapReason(holiday.getReportType()));
		viewHolder.textViewDescription.setText(holiday.getDescription());
		Util.applyStatusBadge(viewHolder.textViewStatus, holiday.getReportState());
	}

	@Override
	public int getItemCount() {
		return holidays.size();
	}

	private String mapReason(final String reportType) {
		return reasonMap.getOrDefault(reportType, reportType);
	}
}
