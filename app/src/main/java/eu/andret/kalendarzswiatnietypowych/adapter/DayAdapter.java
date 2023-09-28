package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

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
import eu.andret.kalendarzswiatnietypowych.util.Data;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayAdapter extends ArrayAdapter<HolidayDay> {
	private static final int MAX_WORDS_COUNT = 4;

	private final int month;
	private final List<HolidayDay> holidayDays;

	private static class ViewHolder {
		private TextView dateSmall;
		private TextView dateBig;
		private TextView holiday;
		private TextView more;
		private ImageView sad;
	}

	public DayAdapter(final Context context, final List<HolidayDay> holidayDays, final List<HolidayDay> days, final int month) {
		super(context, R.layout.adapter_day, days);
		this.holidayDays = holidayDays;
		this.month = month;
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			assert getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) != null;
			convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adapter_day, parent, false);
			holder = new ViewHolder();
			holder.dateSmall = convertView.findViewById(R.id.adapter_day_text_number_small);
			holder.dateBig = convertView.findViewById(R.id.adapter_day_text_number_big);
			holder.holiday = convertView.findViewById(R.id.adapter_day_text_holiday);
			holder.more = convertView.findViewById(R.id.adapter_day_text_more);
			holder.sad = convertView.findViewById(R.id.adapter_day_image_sad);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		final Data.ColorSet color = Data.getColors(getContext());

		holder.dateSmall.setTextColor(color.getForegroundColor());
		holder.holiday.setTextColor(color.getForegroundColor());
		holder.more.setTextColor(color.getForegroundColor());
		convertView.setBackgroundColor(color.getBackgroundColor());

		final HolidayDay holidayDay = getItem(position);
		if (holidayDay == null) {
			return convertView;
		}

		final LocalDate now = LocalDate.now();
		if (holidayDay.getDay() == now.getDayOfMonth() && holidayDay.getMonth() == now.getMonthValue()) {
			convertView.setBackgroundColor(color.isDarkTheme() ? Color.rgb(55, 0, 0) : Color.rgb(200, 255, 255));
		}

		if (holidayDay.getMonth() != month) {
			convertView.setBackgroundColor(ContextCompat.getColor(getContext(), color.isDarkTheme() ? R.color.color_dark_gray : R.color.color_light_gray));
		} else if (preferences.getBoolean(getContext().getString(R.string.settings_key_theme_colorized), false)) {
			convertView.setBackgroundColor(Util.randomizeColor(getContext(), holidayDay.getSeed()));
		}
		convertView.setOnClickListener(v -> {
			final Intent intent = new Intent(getContext(), DayActivity.class);
			intent.putExtra(MainActivity.DAY, holidayDay.getDay());
			intent.putExtra(MainActivity.MONTH, holidayDay.getMonth());
			intent.putParcelableArrayListExtra(MainActivity.HOLIDAY_DAYS, new ArrayList<>(holidayDays));
			((MainActivity) getContext()).startActivityForResult(intent, getContext().getResources().getInteger(R.integer.request_code_change_month));
		});

		final boolean includeUsual = preferences.getBoolean(getContext().getString(R.string.settings_key_usual_holidays), false);
		final boolean displayShortcuts = preferences.getBoolean(getContext().getString(R.string.settings_key_display_shortcuts), true);
		final long holidaysCount = holidayDay.countHolidays(includeUsual);
		if (holidaysCount == 0) {
			holder.sad.setVisibility(View.VISIBLE);
			return convertView;
		}
		if (!displayShortcuts) {
			holder.dateBig.setText(String.valueOf(holidayDay.getDay()));
			return convertView;
		}
		holder.dateSmall.setText(String.valueOf(holidayDay.getDay()));

		final Holiday displayedHoliday = holidayDay.getHolidaysList(includeUsual).get(0);
		final String[] words = displayedHoliday.getName().split(" ");
		final String result = Arrays.stream(words)
				.limit(MAX_WORDS_COUNT)
				.collect(Collectors.joining(" "));
		final boolean full = words.length <= MAX_WORDS_COUNT;
		final boolean isDisplayedUsual = displayedHoliday.isUsual();
		if (isDisplayedUsual) {
			holder.holiday.setTypeface(null, Typeface.BOLD);
		}
		long holidaysCountIndicator = holidaysCount;
		if (full) {
			holidaysCountIndicator--;
			holder.holiday.setText(result);
		} else {
			holder.holiday.setText(getContext().getString(R.string.ellipsis_text, result));
		}

		if (holidaysCountIndicator > 0) {
			holder.more.setText(getContext().getString(R.string.see_more, holidaysCountIndicator));
		}
		return convertView;
	}
}
