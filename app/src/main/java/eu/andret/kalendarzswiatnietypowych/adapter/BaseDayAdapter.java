package eu.andret.kalendarzswiatnietypowych.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public abstract class BaseDayAdapter<H extends BaseDayAdapter.BaseViewHolder>
		extends ListAdapter<MonthFragment.HolidayDayViewModel, H> {

	protected static boolean areItemsTheSame(
			@NonNull final MonthFragment.HolidayDayViewModel oldItem,
			@NonNull final MonthFragment.HolidayDayViewModel newItem) {
		return oldItem.getMonth() == newItem.getMonth() && oldItem.getDay() == newItem.getDay();
	}

	private final DayClickListener dayClickListener;
	@LayoutRes
	private final int layoutResId;

	protected BaseDayAdapter(
			@NonNull final DiffUtil.ItemCallback<MonthFragment.HolidayDayViewModel> diffCallback,
			@NonNull final DayClickListener dayClickListener, @LayoutRes final int layoutResId) {
		super(diffCallback);
		this.dayClickListener = dayClickListener;
		this.layoutResId = layoutResId;
	}

	@NonNull
	protected abstract H createViewHolder(@NonNull View view);

	protected abstract void bindExtra(@NonNull H holder,
			@NonNull MonthFragment.HolidayDayViewModel item);

	@NonNull
	@Override
	public H onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(layoutResId, viewGroup, false);
		final int parentHeight = viewGroup.getMeasuredHeight();
		if (parentHeight > 0) {
			final int adHeight = (int) (50 * viewGroup.getResources().getDisplayMetrics().density);
			view.getLayoutParams().height = (parentHeight - adHeight) / 6;
		}
		return createViewHolder(view);
	}

	@Override
	@SuppressLint("WrongConstant")
	public void onBindViewHolder(@NonNull final H viewHolder, final int position) {
		final MonthFragment.HolidayDayViewModel holidayDay = getItem(position);
		final String contentDescription = viewHolder.itemView.getContext()
				.getString(R.string.content_description_icon_sad_appendix, holidayDay.getId());

		viewHolder.cardView.setCardBackgroundColor(holidayDay.getCardBackgroundColor());
		viewHolder.cardView.setStrokeColor(holidayDay.getStrokeColor());
		viewHolder.cardView.setStrokeWidth(holidayDay.getStrokeWidth());
		viewHolder.sadImageView.setVisibility(holidayDay.getSadImageVisibility());
		viewHolder.sadImageView.setContentDescription(contentDescription);

		bindExtra(viewHolder, holidayDay);

		viewHolder.cardView.setOnClickListener(v ->
				dayClickListener.onDayClicked(holidayDay.getDay(), holidayDay.getMonth()));
	}

	public static class BaseViewHolder extends RecyclerView.ViewHolder {
		final MaterialCardView cardView;
		final ImageView sadImageView;
		final TextView dateTextView;

		public BaseViewHolder(@NonNull final View view) {
			super(view);
			cardView = view.findViewById(R.id.adapter_day_card_view);
			sadImageView = view.findViewById(R.id.adapter_day_image_sad);
			dateTextView = view.findViewById(R.id.adapter_day_text_number);
		}
	}
}
