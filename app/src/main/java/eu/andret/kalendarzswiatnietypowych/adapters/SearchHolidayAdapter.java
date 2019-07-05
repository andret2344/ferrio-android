package eu.andret.kalendarzswiatnietypowych.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activities.DayActivity;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Data.Prefs;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;

public class SearchHolidayAdapter extends ArrayAdapter<HolidayDay> {
	private final Context context;

	private class ViewHolder {
		private TextView date;
		private LinearLayout holidays;
		private LinearLayout border;
	}

	public SearchHolidayAdapter(Context context, List<HolidayDay> values) {
		super(context, R.layout.adapter_search, values);
		this.context = context;
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert inflater != null;
			convertView = inflater.inflate(R.layout.adapter_search, parent, false);
			holder = new ViewHolder();
			holder.border = convertView.findViewById(R.id.adapter_search_linear_border);
			holder.holidays = convertView.findViewById(R.id.adapter_search_layout_holidays);
			holder.date = convertView.findViewById(R.id.adapter_search_text_date);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getContext().getResources().getString(R.string.settings_theme_app), "1")));

		holder.date.setTextColor(color.forground);
		holder.border.setBackgroundColor(color.background);
		convertView.setBackgroundColor(color.forground);

		HolidayDay day = getItem(position);
		if (day == null) {
			return convertView;
		}
		holder.date.setText(day.getDate());
		int c;
		boolean colorized;
		try {
			colorized = theme.getBoolean(getContext().getResources().getString(R.string.settings_theme_colorized), false);
		} catch (ClassCastException ex) {
			colorized = theme.getString(getContext().getResources().getString(R.string.settings_theme_colorized), "false").equals("true");
			SharedPreferences.Editor editor = theme.edit();
			editor.remove(getContext().getResources().getString(R.string.settings_theme_colorized));
			editor.putBoolean(getContext().getResources().getString(R.string.settings_theme_colorized), colorized);
			editor.apply();
		}
		if (colorized) {
			Random r = new Random();
			r.setSeed(day.getSeed());
			boolean dark = Data.getColors(Integer.parseInt(Data.getPreferences(context, Prefs.THEME).getString(getContext().getResources().getString(R.string.settings_theme_app), "1"))).dark;
			c = Color.rgb(r.nextInt(127) + (dark ? 0 : 127), r.nextInt(127) + (dark ? 0 : 127), r.nextInt(127) + (dark ? 0 : 127));
			holder.border.setBackgroundColor(c);
		}
		holder.date.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.adapter_month_holiday_main_text));
		holder.holidays.removeAllViews();
		for (Holiday h : day.getHolidays()) {
			if (!h.isUsual() || theme.getBoolean(getContext().getResources().getString(R.string.settings_usual_holidays), false)) {
				TextView tv = new TextView(getContext());
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				p.setMargins(0, 2, 0, 2);
				tv.setLayoutParams(p);
				tv.setText(getContext().getResources().getString(R.string.pointer) + " " + h.getText());
				tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.adapter_month_holiday_main_text));
				holder.holidays.addView(tv);
				tv.setTextColor(color.forground);
				if (h.isUsual()) {
					tv.setTypeface(null, Typeface.BOLD);
				}
			}
		}

		convertView.setOnClickListener(v -> {
			Intent i = new Intent(getContext(), DayActivity.class);
			i.putExtra("day", day.getDay());
			i.putExtra("month", day.getMonth().getMonth());
			getContext().startActivity(i);
		});

		return convertView;
	}
}
