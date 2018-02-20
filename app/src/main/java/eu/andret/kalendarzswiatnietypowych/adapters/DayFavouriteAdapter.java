package eu.andret.kalendarzswiatnietypowych.adapters;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activities.DayActivity;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;

public class DayFavouriteAdapter extends ArrayAdapter<Holiday> {
	private final Context context;
	
	private class ViewHolder {
		private TextView date, holiday, click;
	}
	
	public DayFavouriteAdapter(Context context, Holiday[] values) {
		super(context, R.layout.adapter_favourite, values);
		this.context = context;
	}
	
	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.adapter_favourite, parent, false);
			holder.date = convertView.findViewById(R.id.adapter_favourite_text_number);
			holder.holiday = convertView.findViewById(R.id.adapter_favourite_text_holiday);
			holder.click = convertView.findViewById(R.id.adapter_favourite_text_more);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getContext().getResources().getString(R.string.settings_theme_app), "1")));
		final SharedPreferences favourites = Data.getPreferences(context, Data.Prefs.FAVOURITES);
		@SuppressWarnings("unchecked")
		final Set<String> favs = // favourites.getStringSet(Data.favourites, new HashSet<String>());
				(Set<String>) favourites.getAll().get("favourites");// TODO -> strings.xml
		holder.date.setTextColor(color.forground);
		holder.holiday.setTextColor(color.forground);
		holder.click.setTextColor(color.forground);
		convertView.setBackgroundColor(color.background);
		
		final String today = getItem(position).getText();
		holder.date.setText(today);
		String text = getItem(position).getDay().getHolidays().get(0).getText();
		String[] arr = text.split(" ");
		String result;
		final int words = 10;
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
		int number = getItem(position).getDay().getHolidays().size() - (full ? 1 : 0);
		if (number != 0) {
			holder.click.setText(number + " " + context.getResources().getString(R.string.see_more));
		}
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, DayActivity.class);
				intent.putExtra("date", today);
				Calendar c = Calendar.getInstance();
				String[] arr = today.split("\\.");
				c.set(Calendar.MONTH, Integer.parseInt(arr[1]) - 1);
				c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(arr[0]));
				intent.putExtra("id", c.get(Calendar.DAY_OF_YEAR));
				context.startActivity(intent);
			}
		});
		
		convertView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				final Context context = getContext();
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				String[] s = {"usun"};
				alert.setItems(s, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								AlertDialog.Builder deleting = new AlertDialog.Builder(context);
								deleting.setTitle(getContext().getResources().getString(R.string.deleting));
								deleting.setMessage(getContext().getResources().getString(R.string.are_you_sure));
								deleting.setPositiveButton(getContext().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Set<String> copy = new HashSet<>(favs);
										copy.remove(copy.toArray(new String[]{})[position]);
										SharedPreferences.Editor editor = favourites.edit();
										editor.remove("favourites");// TODO -> strings.xml
										editor.putStringSet("favourites", copy);// TODO -> strings.xml
										editor.apply();
										// context.recreate();
									}
								});
								deleting.setNegativeButton(getContext().getResources().getString(R.string.no), null);
								deleting.show();
								break;
						}
					}
				});
				alert.show();
				return false;
			}
		});
		return convertView;
	}
}
