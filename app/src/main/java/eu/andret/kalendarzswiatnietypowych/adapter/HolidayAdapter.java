package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.HolidayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.ViewHolder> {
	private final Context context;
	private final List<Holiday> holidays;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView nameTextView;
		private final TextView descriptionTextView;
		private final TextView countryTextView;

		public ViewHolder(final View view) {
			super(view);
			nameTextView = view.findViewById(R.id.adapter_holiday_name);
			descriptionTextView = view.findViewById(R.id.adapter_holiday_description);
			countryTextView = view.findViewById(R.id.adapter_holiday_text_country);
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
		if (holiday.getDescription().isBlank()) {
			viewHolder.descriptionTextView.setVisibility(View.GONE);
		} else {
			viewHolder.itemView.setOnClickListener(view -> {
				final Intent intent = new Intent(context, HolidayActivity.class);
				intent.putExtra(MainActivity.HOLIDAY, holiday);
				context.startActivity(intent);
			});
		}

		viewHolder.nameTextView.setText(holiday.getName());
		viewHolder.descriptionTextView.setText(holiday.getDescription());
		if (holiday.getCountryCode() != null && !holiday.getCountryCode().isBlank()) {
			viewHolder.countryTextView.setText(holiday.getCountryCode());
			viewHolder.countryTextView.setTooltipText(holiday.getCountryName());
		}
		if (holiday.isUsual()) {
			viewHolder.nameTextView.setTypeface(null, Typeface.ITALIC);
		}
	}

	@Override
	public int getItemCount() {
		return holidays.size();
	}
}
