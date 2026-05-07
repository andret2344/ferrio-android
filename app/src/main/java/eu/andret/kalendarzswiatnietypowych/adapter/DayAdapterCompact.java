package eu.andret.kalendarzswiatnietypowych.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewbinding.ViewBinding;

import java.util.Objects;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.databinding.AdapterDayCompactBinding;
import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class DayAdapterCompact extends BaseDayAdapter<DayAdapterCompact.ViewHolder> {
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
					&& Objects.equals(oldItem.getDate(), newItem.getDate())
					&& oldItem.getHolidayCount() == newItem.getHolidayCount();
		}
	};

	public DayAdapterCompact(@NonNull final DayClickListener dayClickListener) {
		super(DIFF_CALLBACK, dayClickListener, AdapterDayCompactBinding::inflate);
	}

	@NonNull
	@Override
	protected ViewHolder createViewHolder(@NonNull final ViewBinding binding) {
		return new ViewHolder((AdapterDayCompactBinding) binding);
	}

	@Override
	protected void bindExtra(@NonNull final ViewHolder holder, @NonNull final MonthFragment.HolidayDayViewModel item) {
		final int count = item.getHolidayCount();
		if (count > 0) {
			holder.dateTextView.setVisibility(View.VISIBLE);
			holder.dateTextView.setText(item.getDate());
			holder.countTextView.setVisibility(View.VISIBLE);
			holder.countTextView.setText(holder.itemView.getContext().getString(R.string.holidays_count, count));
		} else {
			holder.dateTextView.setVisibility(View.INVISIBLE);
			holder.countTextView.setVisibility(View.GONE);
		}
	}

	public static class ViewHolder extends BaseDayAdapter.BaseViewHolder {
		private final TextView countTextView;

		public ViewHolder(@NonNull final AdapterDayCompactBinding binding) {
			super(binding.adapterDayCardView, binding.adapterDayImageSad, binding.adapterDayTextNumber);
			countTextView = binding.adapterDayTextCount;
		}
	}
}
