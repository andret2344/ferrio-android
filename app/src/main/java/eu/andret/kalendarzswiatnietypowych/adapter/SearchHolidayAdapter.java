package eu.andret.kalendarzswiatnietypowych.adapter;

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
import androidx.preference.PreferenceManager;

import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.Data;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class SearchHolidayAdapter extends ArrayAdapter<HolidayDay> {
	private final Context context;

	private static class ViewHolder {
		private TextView date;
		private LinearLayout holidays;
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
			holder.holidays = convertView.findViewById(R.id.adapter_search_layout_holidays);
			holder.date = convertView.findViewById(R.id.adapter_search_text_date);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final HolidayDay day = getItem(position);
		if (day == null) {
			return convertView;
		}

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		final Data.ColorSet color = Data.getColors(getContext());
		final float dimension = getContext().getResources().getDimension(R.dimen.adapter_month_holiday_main_text);

		holder.date.setTextColor(color.getForegroundColor());
		holder.date.setText(String.format(Locale.ROOT, "%02d.%02d", day.getDay(), day.getMonth()));
		holder.date.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimension);
		holder.holidays.removeAllViews();

		if (preferences.getBoolean(getContext().getString(R.string.settings_key_theme_colorized), false)) {
			convertView.setBackgroundColor(Util.randomizeColor(color.isDarkTheme(), day.getSeed()));
		} else {
			convertView.setBackgroundColor(color.getBackgroundColor());
		}
		final boolean isUsual = preferences.getBoolean(getContext().getString(R.string.settings_key_usual_holidays), false);
		for (final Holiday holiday : day.getHolidaysList(isUsual)) {
			final TextView textView = new TextView(getContext());
			final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 2, 0, 2);
			textView.setLayoutParams(layoutParams);
			textView.setText(getContext().getString(R.string.pointed_text, holiday.getText()));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimension);
			holder.holidays.addView(textView);
			textView.setTextColor(color.getForegroundColor());
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
