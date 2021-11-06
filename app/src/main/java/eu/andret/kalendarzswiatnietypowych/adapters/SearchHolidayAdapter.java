package eu.andret.kalendarzswiatnietypowych.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activities.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activities.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class SearchHolidayAdapter extends ArrayAdapter<HolidayDay> {
	private final Context context;

	private static class ViewHolder {
		private TextView date;
		private LinearLayout holidays;
		private LinearLayout border;
	}

	public SearchHolidayAdapter(final Context context, final List<HolidayDay> values) {
		super(context, R.layout.adapter_search, values);
		this.context = context;
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		final SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		final Data.AppColorSet color = Data.getColors(getContext());

		holder.date.setTextColor(color.foreground);
		holder.border.setBackgroundColor(color.background);
		convertView.setBackgroundColor(color.foreground);

		final HolidayDay day = getItem(position);
		if (day == null) {
			return convertView;
		}
		holder.date.setText(String.format(Locale.ROOT, "%02d.%02d", day.getDay(), day.getMonth()));
		boolean colorized;
		try {
			colorized = theme.getBoolean(getContext().getResources().getString(R.string.settings_key_theme_colorized), false);
		} catch (final ClassCastException ex) {
			colorized = theme.getString(getContext().getResources().getString(R.string.settings_key_theme_colorized), "false").equals("true");
			theme.edit()
					.remove(getContext().getResources().getString(R.string.settings_key_theme_colorized))
					.putBoolean(getContext().getResources().getString(R.string.settings_key_theme_colorized), colorized)
					.apply();
		}
		if (colorized) {
			holder.border.setBackgroundColor(Util.randomize(color.dark));
		}
		holder.date.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.adapter_month_holiday_main_text));
		holder.holidays.removeAllViews();
		final boolean isUsual = theme.getBoolean(getContext().getResources().getString(R.string.settings_key_usual_holidays), false);
		for (final Holiday holiday : day.getHolidaysList(isUsual)) {
			final TextView textView = new TextView(getContext());
			final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 2, 0, 2);
			textView.setLayoutParams(layoutParams);
			textView.setText(getContext().getResources().getString(R.string.pointed_text, holiday.getText()));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.adapter_month_holiday_main_text));
			holder.holidays.addView(textView);
			textView.setTextColor(color.foreground);
			if (holiday.isUsual()) {
				textView.setTypeface(null, Typeface.BOLD);
			}
		}

		convertView.setOnClickListener(v -> {
			final Intent intent = new Intent(getContext(), DayActivity.class);
			intent.putExtra(MainActivity.DAY, day.getDay());
			intent.putExtra(MainActivity.MONTH, day.getMonth());
			getContext().startActivity(intent);
		});

		return convertView;
	}
}
