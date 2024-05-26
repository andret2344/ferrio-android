package eu.andret.kalendarzswiatnietypowych.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.time.Month;
import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.MissingFixedHoliday;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
	private final List<MissingFixedHoliday> holidays;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewId;
		private final TextView textViewDate;
		private final Chip chipStatus;
		private final TextView textViewName;
		private final TextView textViewDescription;

		public ViewHolder(final View view) {
			super(view);
			textViewId = view.findViewById(R.id.adapter_report_text_id);
			textViewDate = view.findViewById(R.id.adapter_report_text_date);
			chipStatus = view.findViewById(R.id.adapter_report_chip);
			textViewName = view.findViewById(R.id.adapter_report_text_name);
			textViewDescription = view.findViewById(R.id.adapter_report_text_description);
		}
	}

	public ReportAdapter(final List<MissingFixedHoliday> holidays) {
		this.holidays = holidays;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_report, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final MissingFixedHoliday holiday = holidays.get(position);
		final Pair<Month, Integer> datePair = Pair.create(Month.of(holiday.getMonth()), holiday.getDay());

		viewHolder.textViewId.setText(String.format(Locale.getDefault(), "#%d", holiday.getId()));
		viewHolder.textViewDate.setText(Util.getFormattedDate(datePair));
		viewHolder.chipStatus.setText(holiday.getReportState().name());
		viewHolder.textViewName.setText(holiday.getName());
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
}
