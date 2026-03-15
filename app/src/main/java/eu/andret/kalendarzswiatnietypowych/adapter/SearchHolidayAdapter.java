package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.time.Month;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class SearchHolidayAdapter extends ListAdapter<HolidayDay, SearchHolidayAdapter.ViewHolder> {
	private static final DiffUtil.ItemCallback<HolidayDay> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
		@Override
		public boolean areItemsTheSame(@NonNull final HolidayDay oldItem,
				@NonNull final HolidayDay newItem) {
			return oldItem.getMonth() == newItem.getMonth() && oldItem.getDay() == newItem.getDay();
		}

		@Override
		public boolean areContentsTheSame(@NonNull final HolidayDay oldItem,
				@NonNull final HolidayDay newItem) {
			return oldItem.equals(newItem);
		}
	};

	private final boolean colorized;
	private final boolean includeUsual;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView dateTextView;
		private final TextView holidaysTextView;

		public ViewHolder(final View view) {
			super(view);
			dateTextView = view.findViewById(R.id.adapter_search_text_date);
			holidaysTextView = view.findViewById(R.id.adapter_search_text_content);
		}
	}

	public SearchHolidayAdapter(final boolean colorized, final boolean includeUsual) {
		super(DIFF_CALLBACK);
		this.colorized = colorized;
		this.includeUsual = includeUsual;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_search, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final HolidayDay day = getItem(position);
		final Context context = viewHolder.itemView.getContext();
		final Pair<Month, Integer> datePair = new Pair<>(Month.of(day.getMonth()), day.getDay());
		viewHolder.dateTextView.setText(Util.getFormattedDate(datePair));
		if (colorized) {
			((MaterialCardView) viewHolder.itemView).setCardBackgroundColor(Util.randomizeColor(context, day.getSeed()));
		}
		final List<Holiday> holidaysList = day.getHolidaysList(includeUsual);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < holidaysList.size(); i++) {
			if (i > 0) {
				sb.append('\n');
			}
			sb.append(context.getString(R.string.bullet_point)).append(' ').append(holidaysList.get(i).getName());
		}
		viewHolder.holidaysTextView.setText(sb.toString());
		viewHolder.itemView.setOnClickListener(v -> {
			final Intent intent = new Intent(context, DayActivity.class);
			intent.putExtra(MainActivity.DAY, day.getDay());
			intent.putExtra(MainActivity.MONTH, day.getMonth());
			context.startActivity(intent);
		});
	}
}
