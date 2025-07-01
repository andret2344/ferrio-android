package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.time.Month;
import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.HolidayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.util.HolidayDiffCallback;

public class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.ViewHolder> {
	private final Context context;
	private final List<Holiday> holidays;
	private final Pair<Month, Integer> datePair;

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

	public HolidayAdapter(final Context context, final List<Holiday> holidays, final Pair<Month, Integer> datePair) {
		this.context = context;
		this.holidays = holidays;
		this.datePair = datePair;
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
		}
		viewHolder.itemView.setOnClickListener(view -> {
			final Intent intent = new Intent(context, HolidayActivity.class);
			intent.putExtra(MainActivity.HOLIDAY, holiday.getId());
			intent.putExtra(MainActivity.MONTH, datePair.first.getValue());
			intent.putExtra(MainActivity.DAY, datePair.second);
			context.startActivity(intent);
		});

		viewHolder.nameTextView.setText(holiday.getName());
		viewHolder.descriptionTextView.setText(holiday.getDescription());
		if (holiday.getCountryCode() != null && !holiday.getCountryCode().isBlank()) {
			final Emoji emoji = EmojiManager.getForAlias(holiday.getCountryCode().toLowerCase(Locale.ROOT));
			viewHolder.countryTextView.setText(emoji.getUnicode());
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

	public void updateData(final List<Holiday> newHolidaysList) {
		final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new HolidayDiffCallback(holidays, newHolidaysList));
		holidays.clear();
		holidays.addAll(newHolidaysList);
		diffResult.dispatchUpdatesTo(this);
	}
}
