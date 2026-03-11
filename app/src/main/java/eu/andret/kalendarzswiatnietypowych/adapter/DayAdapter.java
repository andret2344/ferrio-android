package eu.andret.kalendarzswiatnietypowych.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class DayAdapter extends ListAdapter<MonthFragment.HolidayDayViewModel, DayAdapter.ViewHolder> {
	private static final DiffUtil.ItemCallback<MonthFragment.HolidayDayViewModel> DIFF_CALLBACK =
			new DiffUtil.ItemCallback<>() {
				@Override
				public boolean areItemsTheSame(@NonNull final MonthFragment.HolidayDayViewModel oldItem,
						@NonNull final MonthFragment.HolidayDayViewModel newItem) {
					return oldItem.getMonth() == newItem.getMonth() && oldItem.getDay() == newItem.getDay();
				}

				@Override
				public boolean areContentsTheSame(@NonNull final MonthFragment.HolidayDayViewModel oldItem,
						@NonNull final MonthFragment.HolidayDayViewModel newItem) {
					return oldItem.getCardBackgroundColor() == newItem.getCardBackgroundColor()
							&& oldItem.getStrokeColor() == newItem.getStrokeColor()
							&& oldItem.getStrokeWidth() == newItem.getStrokeWidth()
							&& oldItem.getSadImageVisibility() == newItem.getSadImageVisibility()
							&& oldItem.getTypeFace() == newItem.getTypeFace()
							&& java.util.Objects.equals(oldItem.getSmallDate(), newItem.getSmallDate())
							&& java.util.Objects.equals(oldItem.getBigDate(), newItem.getBigDate())
							&& java.util.Objects.equals(oldItem.getHolidayText(), newItem.getHolidayText())
							&& java.util.Objects.equals(oldItem.getMoreText(), newItem.getMoreText());
				}
			};

	private final DayClickListener dayClickListener;

	public DayAdapter(@NonNull final DayClickListener dayClickListener) {
		super(DIFF_CALLBACK);
		this.dayClickListener = dayClickListener;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView smallDateTextView;
		private final TextView bigDateTextView;
		private final TextView holidayTextView;
		private final TextView moreTextView;
		private final ImageView sadImageView;
		private final MaterialCardView cardView;

		public ViewHolder(final View view) {
			super(view);
			smallDateTextView = view.findViewById(R.id.adapter_day_text_number_small);
			bigDateTextView = view.findViewById(R.id.adapter_day_text_number_big);
			holidayTextView = view.findViewById(R.id.adapter_day_text_holiday);
			moreTextView = view.findViewById(R.id.adapter_day_text_more);
			sadImageView = view.findViewById(R.id.adapter_day_image_sad);
			cardView = view.findViewById(R.id.adapter_day_card_view);
		}
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_day, viewGroup, false);
		final int adHeight = (int) (50 * viewGroup.getResources().getDisplayMetrics().density);
		view.getLayoutParams().height = (viewGroup.getMeasuredHeight() - adHeight) / 6;
		return new ViewHolder(view);
	}

	@Override
	@SuppressLint("WrongConstant")
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final MonthFragment.HolidayDayViewModel holidayDay = getItem(position);
		final String contentDescription = viewHolder.itemView.getContext().getString(R.string.content_description_icon_sad_appendix, holidayDay.getId());

		viewHolder.cardView.setCardBackgroundColor(holidayDay.getCardBackgroundColor());
		viewHolder.cardView.setStrokeColor(holidayDay.getStrokeColor());
		viewHolder.cardView.setStrokeWidth(holidayDay.getStrokeWidth());
		viewHolder.sadImageView.setVisibility(holidayDay.getSadImageVisibility());
		viewHolder.sadImageView.setContentDescription(contentDescription);
		viewHolder.smallDateTextView.setText(holidayDay.getSmallDate());
		viewHolder.bigDateTextView.setText(holidayDay.getBigDate());
		viewHolder.holidayTextView.setTypeface(null, holidayDay.getTypeFace());
		viewHolder.holidayTextView.setText(holidayDay.getHolidayText());
		viewHolder.moreTextView.setText(holidayDay.getMoreText());

		viewHolder.cardView.setOnClickListener(v ->
				dayClickListener.onDayClicked(holidayDay.getDay(), holidayDay.getMonth()));
	}
}
