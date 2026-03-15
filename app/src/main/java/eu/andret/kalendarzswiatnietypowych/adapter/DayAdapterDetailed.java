package eu.andret.kalendarzswiatnietypowych.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class DayAdapterDetailed extends BaseDayAdapter<DayAdapterDetailed.ViewHolder> {
	private static final DiffUtil.ItemCallback<MonthFragment.HolidayDayViewModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
		@Override
		public boolean areItemsTheSame(@NonNull final MonthFragment.HolidayDayViewModel oldItem,
				@NonNull final MonthFragment.HolidayDayViewModel newItem) {
			return BaseDayAdapter.areItemsTheSame(oldItem, newItem);
		}

		@Override
		public boolean areContentsTheSame(@NonNull final MonthFragment.HolidayDayViewModel oldItem,
				@NonNull final MonthFragment.HolidayDayViewModel newItem) {
			return oldItem.getCardBackgroundColor() == newItem.getCardBackgroundColor()
					&& oldItem.getStrokeColor() == newItem.getStrokeColor()
					&& oldItem.getStrokeWidth() == newItem.getStrokeWidth()
					&& oldItem.getSadImageVisibility() == newItem.getSadImageVisibility()
					&& oldItem.getTypeFace() == newItem.getTypeFace()
					&& Objects.equals(oldItem.getDate(), newItem.getDate())
					&& Objects.equals(oldItem.getHolidayText(), newItem.getHolidayText())
					&& Objects.equals(oldItem.getMoreText(), newItem.getMoreText());
		}
	};

	public DayAdapterDetailed(@NonNull final DayClickListener dayClickListener) {
		super(DIFF_CALLBACK, dayClickListener, R.layout.adapter_day_detailed);
	}

	@NonNull
	@Override
	protected ViewHolder createViewHolder(@NonNull final View view) {
		return new ViewHolder(view);
	}

	@Override
	protected void bindExtra(@NonNull final ViewHolder holder, @NonNull final MonthFragment.HolidayDayViewModel item) {
		holder.dateTextView.setText(item.getDate());
		holder.holidayTextView.setTypeface(null, item.getTypeFace());
		holder.holidayTextView.setText(item.getHolidayText());
		holder.moreTextView.setText(item.getMoreText());
	}

	public static class ViewHolder extends BaseDayAdapter.BaseViewHolder {
		private final TextView holidayTextView;
		private final TextView moreTextView;

		public ViewHolder(@NonNull final View view) {
			super(view);
			holidayTextView = view.findViewById(R.id.adapter_day_text_holiday);
			moreTextView = view.findViewById(R.id.adapter_day_text_more);
		}
	}
}
