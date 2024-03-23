package eu.andret.kalendarzswiatnietypowych.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.ViewHolder> {
	private final List<Holiday> holidays;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView holidayTextView;
		private final TextView countryTextView;

		public ViewHolder(final View view) {
			super(view);
			holidayTextView = view.findViewById(R.id.adapter_holiday_text_holiday);
			countryTextView = view.findViewById(R.id.adapter_holiday_text_country);
		}
	}

	public HolidayAdapter(final List<Holiday> holidays) {
		this.holidays = holidays;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_holiday, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final Holiday holiday = holidays.get(position);
		viewHolder.holidayTextView.setText(holiday.getName());
		viewHolder.countryTextView.setText(holiday.getCountryCode());
		if (holiday.isUsual()) {
			viewHolder.holidayTextView.setTypeface(null, Typeface.BOLD);
		}
	}

	@Override
	public int getItemCount() {
		return holidays.size();
	}
}
