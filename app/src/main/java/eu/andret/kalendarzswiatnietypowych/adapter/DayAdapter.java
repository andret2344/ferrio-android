package eu.andret.kalendarzswiatnietypowych.adapter;

import android.annotation.SuppressLint;
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
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Data.MyColor;
import eu.andret.kalendarzswiatnietypowych.utils.Data.Prefs;

public class DayAdapter extends ArrayAdapter<HolidayDay> {
	private final Random random = new Random();
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

	@SuppressLint("SetTextI18n")
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

		final SharedPreferences theme = Data.getPreferences(getContext(), Data.Prefs.THEME);
		final Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getContext().getResources().getString(R.string.settings_theme_app), "1")));

		holder.dateSmall.setTextColor(color.foreground);
		holder.holiday.setTextColor(color.foreground);
		holder.more.setTextColor(color.foreground);
		convertView.setBackgroundColor(color.background);

		final HolidayDay ho = getItem(position);
		if (ho == null) {
			return convertView;
		}

		final LocalDate now = LocalDate.now();
		if (ho.getDay() == now.getDayOfMonth() && ho.getMonth().getMonth() == now.getMonthValue()) {
			convertView.setBackgroundColor(color.dark ? Color.rgb(55, 0, 0) : Color.rgb(200, 255, 255));
		}

		if (ho.getMonth().getMonth() != month + 1) {
			convertView.setBackgroundColor(color.dark ? MyColor.GRAY_DARK : MyColor.GRAY_LIGHT);
		} else if (theme.getBoolean(getContext().getResources().getString(R.string.settings_theme_colorized), false)) {
			random.setSeed(ho.getSeed());
			final boolean dark = Data.getColors(Integer.parseInt(Data.getPreferences(getContext(), Prefs.THEME).getString(getContext().getResources().getString(R.string.settings_theme_app), "1"))).dark;
			final int c = Color.rgb(random.nextInt(127) + (dark ? 0 : 127), random.nextInt(127) + (dark ? 0 : 127), random.nextInt(127) + (dark ? 0 : 127));
			convertView.setBackgroundColor(c);
		}
		convertView.setOnClickListener(v -> {
			final Intent intent = new Intent(getContext(), DayActivity.class);
			intent.putExtra("day", ho.getDay());
			intent.putExtra("month", ho.getMonth().getMonth() - 1);
			intent.putExtra("from", "calendar");
			((Activity) getContext()).startActivityForResult(intent, getContext().getResources().getInteger(R.integer.request_code_change_month));
		});

		boolean full = true;
		if (ho.countHolidays(theme.getBoolean(getContext().getResources().getString(R.string.settings_usual_holidays), false)) == 0) {
			holder.sad.setVisibility(View.VISIBLE);
			holder.holiday.setText("");
		} else {
			final String text = ho.getHolidaysList(theme.getBoolean(getContext().getResources().getString(R.string.settings_usual_holidays), false)).get(0).getText();
			holder.sad.setVisibility(View.INVISIBLE);
			final boolean display = theme.getBoolean(getContext().getResources().getString(R.string.settings_display_shortcuts), true);
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
				if (ho.find(text).isUsual()) {
					holder.holiday.setTypeface(null, Typeface.BOLD);
				}
				holder.holiday.setText(result.toString());
				holder.dateSmall.setText(String.valueOf(ho.getDay()));

				final int number = ho.countHolidays(theme.getBoolean(getContext().getResources().getString(R.string.settings_usual_holidays), false)) - (full ? 1 : 0);
				if (number > 0) {
					holder.more.setText(number + " " + getContext().getResources().getString(R.string.see_more));
				}
			} else {
				holder.dateBig.setText(String.valueOf(ho.getDay()));
				final int number = ho.countHolidays(theme.getBoolean(getContext().getResources().getString(R.string.settings_usual_holidays), false));
				if (number > 0) {
					holder.more.setText(getContext().getResources().getString(R.string.holidays) + ": " + number);
				}
			}
		}
		return convertView;
	}
}
