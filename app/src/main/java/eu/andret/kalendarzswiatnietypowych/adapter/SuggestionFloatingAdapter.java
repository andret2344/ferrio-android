package eu.andret.kalendarzswiatnietypowych.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.MissingFloatingHoliday;
import eu.andret.kalendarzswiatnietypowych.entity.ReportState;

public class SuggestionFloatingAdapter extends RecyclerView.Adapter<SuggestionFloatingAdapter.ViewHolder> {
	private final List<MissingFloatingHoliday> holidays;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewId;
		private final TextView textViewDate;
		private final Chip chipStatus;
		private final TextView textViewName;
		private final TextView textViewDescription;

		public ViewHolder(final View view) {
			super(view);
			textViewId = view.findViewById(R.id.adapter_suggestion_floating_text_id);
			textViewDate = view.findViewById(R.id.adapter_suggestion_floating_text_date);
			chipStatus = view.findViewById(R.id.adapter_suggestion_floating_chip);
			textViewName = view.findViewById(R.id.adapter_suggestion_floating_text_name);
			textViewDescription = view.findViewById(R.id.adapter_suggestion_floating_text_description);
		}
	}

	public SuggestionFloatingAdapter(final List<MissingFloatingHoliday> holidays) {
		this.holidays = holidays;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_suggestion_floating, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final MissingFloatingHoliday holiday = holidays.get(position);

		viewHolder.textViewId.setText(String.format(Locale.getDefault(), "#%d", holiday.getId()));
		viewHolder.textViewDate.setText(holiday.getDate());
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

		if (holiday.getReportState() == ReportState.APPLIED && holiday.getHolidayId() != 0) {
			viewHolder.itemView.setOnClickListener(view -> {
				// TODO Fix after database
//				final Intent intent = new Intent(view.getContext(), HolidayActivity.class);
//				intent.putExtra(MainActivity.HOLIDAY, new FloatingHoliday(holiday.getHolidayId()));
//				view.getContext().startActivity(intent);
			});
		}
	}

	@Override
	public int getItemCount() {
		return holidays.size();
	}
}
