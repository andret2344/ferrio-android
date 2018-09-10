package eu.andret.kalendarzswiatnietypowych.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activities.DayActivity;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;

public class DayFavouriteAdapter extends ArrayAdapter<Holiday> {

	private class ViewHolder {
		private TextView date;
		private TextView holiday;
		private TextView click;
	}

	public DayFavouriteAdapter(Context context, Holiday[] values) {
		super(context, R.layout.adapter_favourite, values);
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.adapter_favourite, parent, false);
			holder.date = convertView.findViewById(R.id.adapter_favourite_text_number);
			holder.holiday = convertView.findViewById(R.id.adapter_favourite_text_holiday);
			holder.click = convertView.findViewById(R.id.adapter_favourite_text_more);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		SharedPreferences theme = Data.getPreferences(getContext(), Data.Prefs.THEME);
		Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getContext().getResources().getString(R.string.settings_theme_app), "1")));
		SharedPreferences favourites = Data.getPreferences(getContext(), Data.Prefs.FAVOURITES);
		Set<String> favs = favourites.getStringSet("favourites", new HashSet<>());
//				(Set<String>) favourites.getAll().get("favourites");// TODO -> strings.xml
		holder.date.setTextColor(color.forground);
		holder.holiday.setTextColor(color.forground);
		holder.click.setTextColor(color.forground);
		convertView.setBackgroundColor(color.background);

		Holiday holiday = getItem(position);
		if (holiday == null) {
			return convertView;
		}
		String today = holiday.getText();
		holder.date.setText(today);
		String text = holiday.getDay().getHolidays().get(0).getText();
		String[] arr = text.split(" ");
		String result;
		int words = 10;
		boolean full = false;
		if (arr.length <= words) {
			result = text;
			full = true;
		} else {
			StringBuilder resultBuilder = new StringBuilder();
			for (int i = 0; i < words; i++) {
				resultBuilder.append(" ").append(arr[i]);
			}
			result = resultBuilder.toString();
			result += "...";
		}
		holder.holiday.setText(result);
		int number = holiday.getDay().getHolidays().size() - (full ? 1 : 0);
		if (number != 0) {
			holder.click.setText(number + " " + getContext().getResources().getString(R.string.see_more));
		}
		convertView.setOnClickListener(v -> {
			Intent intent = new Intent(getContext(), DayActivity.class);
			intent.putExtra("date", today);
			Calendar c = Calendar.getInstance();
			String[] arr1 = today.split("\\.");
			c.set(Calendar.MONTH, Integer.parseInt(arr1[1]) - 1);
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(arr1[0]));
			intent.putExtra("id", c.get(Calendar.DAY_OF_YEAR));
			getContext().startActivity(intent);
		});

		convertView.setOnLongClickListener(v -> {
			Context context = getContext();
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
			String[] s = {"usun"};
			alert.setItems(s, (dialog, which) -> {
				if (which == 0) {
					AlertDialog.Builder deleting = new AlertDialog.Builder(context);
					deleting.setTitle(getContext().getResources().getString(R.string.deleting));
					deleting.setMessage(getContext().getResources().getString(R.string.are_you_sure));
					deleting.setPositiveButton(getContext().getResources().getString(R.string.yes), (dialog1, which1) -> {
						Set<String> copy = new HashSet<>(favs);
						copy.remove(copy.toArray(new String[]{})[position]);
						SharedPreferences.Editor editor = favourites.edit();
						editor.remove("favourites");// TODO -> strings.xml
						editor.putStringSet("favourites", copy);// TODO -> strings.xml
						editor.apply();
					});
					deleting.setNegativeButton(getContext().getResources().getString(R.string.no), null);
					deleting.show();
				}
			});
			alert.show();
			return false;
		});
		return convertView;
	}
}
