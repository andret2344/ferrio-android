package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
	private static final int MAX_WORDS_COUNT = 4;

	private final Context context;
	private final int month;
	private final List<HolidayDay> holidayDays;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView smallDateTextView;
		private final TextView bigDateTextView;
		private final TextView holidayTextView;
		private final TextView moreTextView;
		private final ImageView sadImageView;
		private final CardView cardView;

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

	public DayAdapter(final Context context, final int month, final List<HolidayDay> holidayDays) {
		this.context = context;
		this.month = month;
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
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

		final HolidayDay holidayDay = holidayDays.get(position);
		if (holidayDay == null) {
			return;
		}

		if (holidayDay.getMonth() != month) {
			viewHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.background_accent));
		} else if (preferences.getBoolean(context.getString(R.string.settings_key_theme_colorized), false)) {
			viewHolder.cardView.setCardBackgroundColor(Util.randomizeColor(context, holidayDay.getSeed()));
		} else {
			viewHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.background_secondary));
		}

		final LocalDate now = LocalDate.now();
		if (holidayDay.getDay() == now.getDayOfMonth() && holidayDay.getMonth() == now.getMonthValue()) {
			viewHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.dynamic_selection));
		}

		viewHolder.cardView.setOnClickListener(v -> {
			final Intent intent = new Intent(context, DayActivity.class);
			intent.putExtra(MainActivity.DAY, holidayDay.getDay());
			intent.putExtra(MainActivity.MONTH, holidayDay.getMonth());
			intent.putParcelableArrayListExtra(MainActivity.HOLIDAY_DAYS, new ArrayList<>(holidayDays));
			((MainActivity) context).startActivityForResult(intent, context.getResources().getInteger(R.integer.request_code_change_month));
		});

		final boolean includeUsual = preferences.getBoolean(context.getString(R.string.settings_key_usual_holidays), false);
		final boolean displayShortcuts = preferences.getBoolean(context.getString(R.string.settings_key_display_shortcuts), true);
		final long holidaysCount = holidayDay.countHolidays(includeUsual);
		if (holidaysCount == 0) {
			viewHolder.sadImageView.setVisibility(View.VISIBLE);
			return;
		}
		if (!displayShortcuts) {
			viewHolder.bigDateTextView.setText(String.valueOf(holidayDay.getDay()));
			return;
		}
		viewHolder.smallDateTextView.setText(String.valueOf(holidayDay.getDay()));

		final Holiday displayedHoliday = holidayDay.getHolidaysList(includeUsual).get(0);
		final String[] words = displayedHoliday.getName().split(" ");
		final String result = Arrays.stream(words).limit(MAX_WORDS_COUNT).collect(Collectors.joining(" "));
		final boolean full = words.length <= MAX_WORDS_COUNT;
		final boolean isDisplayedUsual = displayedHoliday.isUsual();
		if (isDisplayedUsual) {
			viewHolder.holidayTextView.setTypeface(null, Typeface.BOLD);
		}
		long holidaysCountIndicator = holidaysCount;
		if (full) {
			holidaysCountIndicator--;
			viewHolder.holidayTextView.setText(result);
		} else {
			viewHolder.holidayTextView.setText(context.getString(R.string.ellipsis_text, result));
		}

		if (holidaysCountIndicator > 0) {
			viewHolder.moreTextView.setText(context.getString(R.string.see_more, holidaysCountIndicator));
		}
	}

	@Override
	public int getItemCount() {
		return holidayDays.size();
	}
}
