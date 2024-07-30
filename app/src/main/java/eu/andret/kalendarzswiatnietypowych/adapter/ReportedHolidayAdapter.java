package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.ReportedHoliday;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class ReportedHolidayAdapter extends RecyclerView.Adapter<ReportedHolidayAdapter.ViewHolder> {
	private final Context context;
	private final List<ReportedHoliday> holidays;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewId;
		private final TextView textViewDate;
		private final Chip chipStatus;
		private final TextView textViewName;
		private final TextView textViewDescription;

		public ViewHolder(final View view) {
			super(view);
			textViewId = view.findViewById(R.id.adapter_reported_holiday_text_id);
			textViewDate = view.findViewById(R.id.adapter_reported_holiday_text_date);
			chipStatus = view.findViewById(R.id.adapter_reported_holiday_chip);
			textViewName = view.findViewById(R.id.adapter_reported_holiday_text_name);
			textViewDescription = view.findViewById(R.id.adapter_reported_holiday_text_description);
		}
	}

	public ReportedHolidayAdapter(final Context context, final List<ReportedHoliday> holidays) {
		this.context = context;
		this.holidays = holidays;
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

		viewHolder.textViewId.setText(String.format(Locale.getDefault(), "#%d", holiday.getId()));
		viewHolder.textViewDate.setText(holiday.getDatetime().format(Util.getDateTimeFormatter()));
		viewHolder.chipStatus.setText(holiday.getReportState().name().replace("_", " "));
		viewHolder.textViewName.setText(mapReason(holiday.getReportType()));
		viewHolder.textViewDescription.setText(holiday.getDescription());
		switch (holiday.getReportState()) {
			case REPORTED:
				viewHolder.chipStatus.setChipBackgroundColorResource(R.color.status_reported);
				break;
			case APPLIED:
				viewHolder.chipStatus.setChipBackgroundColorResource(R.color.status_applied);
				break;
			case DECLINED:
				viewHolder.chipStatus.setChipBackgroundColorResource(R.color.status_declined);
				break;
			case ON_HOLD:
				viewHolder.chipStatus.setChipBackgroundColorResource(R.color.status_on_hold);
				break;
			default:
				break;
		}
	}

	@Override
	public int getItemCount() {
		return holidays.size();
	}

	private String mapReason(final String reportType) {
		final Resources resources = context.getResources();
		final int index = Arrays.asList(resources.getStringArray(R.array.report_keys)).indexOf(reportType);
		return resources.getStringArray(R.array.report_values)[index];
	}
}
