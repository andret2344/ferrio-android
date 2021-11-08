package eu.andret.kalendarzswiatnietypowych.adapters;

import android.app.Activity;
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

import java.time.LocalDate;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activities.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activities.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class DayAdapter extends ArrayAdapter<HolidayDay> {
	private final int month;

	private static class ViewHolder {
		private TextView dateSmall;
		private TextView dateBig;
		private TextView holiday;
		private TextView more;
		private ImageView sad;
	}

	public DayAdapter(final Context context, final List<HolidayDay> holidays, final int month) {
		super(context, R.layout.adapter_day, holidays);
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

		final SharedPreferences theme = Data.getPreferences(getContext(), Data.PreferenceType.THEME);
		final Data.AppColorSet color = Data.getColors(getContext());

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
			convertView.setBackgroundColor(getContext().getResources().getColor(color.isDarkTheme() ? R.color.color_dark_gray : R.color.color_light_gray));
		} else if (theme.getBoolean(getContext().getResources().getString(R.string.settings_key_theme_colorized), false)) {
			convertView.setBackgroundColor(Util.randomizeColor(color.isDarkTheme(), holidayDay.getSeed()));
		}
		convertView.setOnClickListener(v -> {
			final Intent intent = new Intent(getContext(), DayActivity.class);
			intent.putExtra(MainActivity.DAY, holidayDay.getDay());
			intent.putExtra(MainActivity.MONTH, holidayDay.getMonth());
			((Activity) getContext()).startActivityForResult(intent, getContext().getResources().getInteger(R.integer.request_code_change_month));
		});

		boolean full = true;
		final boolean includeUsual = theme.getBoolean(getContext().getResources().getString(R.string.settings_key_usual_holidays), false);
		if (holidayDay.countHolidays(includeUsual) == 0) {
			holder.sad.setVisibility(View.VISIBLE);
			holder.holiday.setText("");
		} else {
			final String text = holidayDay.getHolidaysList(includeUsual).get(0).getText();
			holder.sad.setVisibility(View.INVISIBLE);
			final boolean display = theme.getBoolean(getContext().getResources().getString(R.string.settings_key_display_shortcuts), true);
			if (display) {
				final String[] arr = text.split(" ");
				StringBuilder result = new StringBuilder();
				final int words = 4;
				if (arr.length <= words) {
					result = new StringBuilder(text);
				} else {
					for (int i = 0; i < words; i++) {
						result.append(" ").append(arr[i]);
					}
					result.append("...");
					full = false;
				}
				final boolean isAnyUsual = holidayDay.getHolidaysList(includeUsual).stream()
						.filter(holiday -> holiday.getText().equals(text))
						.findAny()
						.map(Holiday::isUsual)
						.orElse(false);
				if (isAnyUsual) {
					holder.holiday.setTypeface(null, Typeface.BOLD);
				}
				holder.holiday.setText(result.toString());
				holder.dateSmall.setText(String.valueOf(holidayDay.getDay()));

				final long number = holidayDay.countHolidays(includeUsual) - (full ? 1 : 0);
				if (number > 0) {
					holder.more.setText(getContext().getResources().getString(R.string.see_more, number));
				}
			} else {
				holder.dateBig.setText(String.valueOf(holidayDay.getDay()));
				final long number = holidayDay.countHolidays(includeUsual);
				if (number > 0) {
					holder.more.setText(getContext().getResources().getString(R.string.holidays, number));
				}
			}
		}
		return convertView;
	}
}
