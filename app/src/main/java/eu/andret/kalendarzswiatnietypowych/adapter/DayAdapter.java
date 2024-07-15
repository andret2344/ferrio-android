package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
	private final Context context;
	private final List<MonthFragment.HolidayDayViewModel> holidayDays;

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

	public DayAdapter(final Context context, final List<MonthFragment.HolidayDayViewModel> holidayDays) {
		this.context = context;
		this.holidayDays = holidayDays;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_day, viewGroup, false);
		view.getLayoutParams().height = viewGroup.getMeasuredHeight() / 6;
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final MonthFragment.HolidayDayViewModel holidayDay = holidayDays.get(position);

		viewHolder.cardView.setCardBackgroundColor(holidayDay.getCardBackgroundColor());
		viewHolder.cardView.setStrokeColor(holidayDay.getStrokeColor());
		viewHolder.cardView.setStrokeWidth(holidayDay.getStrokeWidth());
		viewHolder.sadImageView.setVisibility(holidayDay.getSadImageVisibility());
		viewHolder.smallDateTextView.setText(holidayDay.getSmallDate());
		viewHolder.bigDateTextView.setText(holidayDay.getBigDate());
		viewHolder.holidayTextView.setTypeface(null, holidayDay.getTypeFace());
		viewHolder.holidayTextView.setText(holidayDay.getHolidayText());
		viewHolder.moreTextView.setText(holidayDay.getMoreText());

		viewHolder.cardView.setOnClickListener(v -> {
			final Intent intent = new Intent(context, DayActivity.class);
			intent.putExtra(MainActivity.DAY, holidayDay.getDay());
			intent.putExtra(MainActivity.MONTH, holidayDay.getMonth());
			((MainActivity) context).activityResult.launch(intent);
		});
	}

	@Override
	public int getItemCount() {
		return holidayDays.size();
	}
}
