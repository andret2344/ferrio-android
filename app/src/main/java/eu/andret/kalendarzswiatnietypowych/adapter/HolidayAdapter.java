package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
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
	private final Context context;
	private final List<Holiday> holidays;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView holidayTextView;

		public ViewHolder(final View view) {
			super(view);
			holidayTextView = view.findViewById(R.id.adapter_holiday_text_holiday);
		}
	}

	public HolidayAdapter(final Context context, final List<Holiday> holidays) {
		this.context = context;
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
		if (holiday == null) {
			return;
		}

		viewHolder.holidayTextView.setText(context.getString(R.string.pointed_text, holiday.getName()));
		if (holiday.isUsual()) {
			viewHolder.holidayTextView.setTypeface(null, Typeface.BOLD);
		}
	}

	@Override
	public int getItemCount() {
		return holidays.size();
	}
}
