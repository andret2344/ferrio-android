package eu.andret.kalendarzswiatnietypowych.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.viewbinding.ViewBinding;

import java.util.Objects;

import eu.andret.kalendarzswiatnietypowych.databinding.AdapterDaySimpleBinding;
import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class DayAdapterSimple extends BaseDayAdapter<BaseDayAdapter.BaseViewHolder> {
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
					&& Objects.equals(oldItem.getDate(), newItem.getDate());
		}
	};

	public DayAdapterSimple(@NonNull final DayClickListener dayClickListener) {
		super(DIFF_CALLBACK, dayClickListener, AdapterDaySimpleBinding::inflate);
	}

	@NonNull
	@Override
	protected BaseViewHolder createViewHolder(@NonNull final ViewBinding binding) {
		final AdapterDaySimpleBinding b = (AdapterDaySimpleBinding) binding;
		return new BaseViewHolder(b.adapterDayCardView, b.adapterDayImageSad, b.adapterDayTextNumber);
	}

	@Override
	protected void bindExtra(@NonNull final BaseViewHolder holder, @NonNull final MonthFragment.HolidayDayViewModel item) {
		if (item.getHolidayCount() > 0) {
			holder.dateTextView.setVisibility(View.VISIBLE);
			holder.dateTextView.setText(item.getDate());
		} else {
			holder.dateTextView.setVisibility(View.INVISIBLE);
		}
	}
}
