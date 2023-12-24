package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.Data;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class SearchHolidayAdapter extends RecyclerView.Adapter<SearchHolidayAdapter.ViewHolder> {
	private final Context context;
	private final List<HolidayDay> holidayDays;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView dateTextView;
		private final LinearLayout holidaysLinearLayout;

		public ViewHolder(final View view) {
			super(view);
			dateTextView = view.findViewById(R.id.adapter_search_text_date);
			holidaysLinearLayout = view.findViewById(R.id.adapter_search_layout_holidays);
		}
	}

	public SearchHolidayAdapter(final Context context, final List<HolidayDay> holidayDays) {
		this.context = context;
		this.holidayDays = holidayDays;
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
		final HolidayDay day = holidayDays.get(position);
		if (day == null) {
			return;
		}

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final Data.ColorSet color = Data.getColors(context);
		final float dimension = context.getResources().getDimension(R.dimen.adapter_month_holiday_main_text);

		viewHolder.dateTextView.setTextColor(color.getForegroundColor());
		viewHolder.dateTextView.setText(String.format(Locale.ROOT, "%02d.%02d", day.getDay(), day.getMonth()));
		viewHolder.dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimension);
		viewHolder.holidaysLinearLayout.removeAllViews();

		if (preferences.getBoolean(context.getString(R.string.settings_key_theme_colorized), false)) {
			viewHolder.itemView.setBackgroundColor(Util.randomizeColor(context, day.getSeed()));
		} else {
			viewHolder.itemView.setBackgroundColor(color.getBackgroundColor());
		}
		final boolean isUsual = preferences.getBoolean(context.getString(R.string.settings_key_usual_holidays), false);
		for (final Holiday holiday : day.getHolidaysList(isUsual)) {
			final TextView textView = new TextView(context);
			final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 2, 0, 2);
			textView.setLayoutParams(layoutParams);
			textView.setText(context.getString(R.string.pointed_text, holiday.getName()));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimension);
			viewHolder.holidaysLinearLayout.addView(textView);
			textView.setTextColor(color.getForegroundColor());
			if (holiday.isUsual()) {
				textView.setTypeface(null, Typeface.BOLD);
			}
		}

		viewHolder.itemView.setOnClickListener(v -> {
			final Intent intent = new Intent(context, DayActivity.class);
			intent.putExtra(MainActivity.DAY, day.getDay());
			intent.putExtra(MainActivity.MONTH, day.getMonth());
			context.startActivity(intent);
		});
	}

	@Override
	public int getItemCount() {
		return holidayDays.size();
	}
}
