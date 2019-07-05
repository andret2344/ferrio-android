package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapters.HolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Data.Prefs;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;
import lombok.Getter;

public class DayFragment extends Fragment {
	private static final Random random = new Random();

	@Getter
	private int day;
	@Getter
	private int month;
	private int id;

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		day = args.getInt("day");
		month = args.getInt("month");
		id = args.getInt("id");
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View dayView = inflater.inflate(R.layout.fragment_day, parent, false);
		if (day == -1 || month == -1) {
			Calendar calendar = Calendar.getInstance();
			if (new GregorianCalendar().isLeapYear(calendar.get(Calendar.YEAR))) {
				if (id < 60) {
					calendar.set(Calendar.DAY_OF_YEAR, id + 1);
					day = calendar.get(Calendar.DAY_OF_MONTH);
					month = 1 + calendar.get(Calendar.MONTH);
				} else if (id == 60) {
					day = 30;
					month = 2;
				} else {
					calendar.set(Calendar.DAY_OF_YEAR, id);
					day = calendar.get(Calendar.DAY_OF_MONTH);
					month = 1 + calendar.get(Calendar.MONTH);
				}
			} else {
				if (id < 59) {
					calendar.set(Calendar.DAY_OF_YEAR, id + 1);
					day = calendar.get(Calendar.DAY_OF_MONTH);
					month = 1 + calendar.get(Calendar.MONTH);
				} else if (id == 59) {
					day = 29;
					month = 2;
				} else if (id == 60) {
					day = 30;
					month = 2;
				} else {
					calendar.set(Calendar.DAY_OF_YEAR, id - 1);
					day = calendar.get(Calendar.DAY_OF_MONTH);
					month = calendar.get(Calendar.MONTH) + 1;
				}
			}
		}
		SharedPreferences theme = Data.getPreferences(getActivity(), Data.Prefs.THEME);
		Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getContext().getResources().getString(R.string.settings_theme_app), "1")));
		HolidayDay holidays = HolidayCalendar.getInstance(getActivity()).getMonth(month).getDay(day);
		if (!holidays.hasHolidays(theme.getBoolean(getContext().getResources().getString(R.string.settings_usual_holidays), false))) {
			dayView.findViewById(R.id.fragment_day_image_sad).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setBackgroundColor(Color.GRAY);
		}
		int c = color.background;
		if (theme.getBoolean(getContext().getResources().getString(R.string.settings_theme_colorized), false)) {
			random.setSeed(holidays.getSeed());
			boolean dark = Data.getColors(Integer.parseInt(Data.getPreferences(getActivity(), Prefs.THEME).getString(getContext().getResources().getString(R.string.settings_theme_app), "1"))).dark;
			c = Color.rgb(random.nextInt(127) + (dark ? 0 : 127), random.nextInt(127) + (dark ? 0 : 127), random.nextInt(127) + (dark ? 0 : 127));
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(c);
		} else {
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(color.background);
		}
		((ListView) dayView.findViewById(R.id.fragment_day_list_holidays)).setAdapter(new HolidayAdapter(getActivity(), holidays, c, true));

		// final SharedPreferences favourites = Data.getPreferences(getActivity(), Data.Prefs.FAVOURITES);
		// final ImageView iv = (ImageView) dayView.findViewById(R.id.colectionday_image_favourite);
		// final Set<String> set = favourites.getStringSet(Data.favourites, new HashSet<String>());
		// favourites.getStringSet(Data.favourites, new HashSet<String>());
		// All().get(Data.favourites);
		// Log.d("AC", favourites.getAll().toString());
		// if (set.contains(current)) {
		// iv.setImageResource(R.drawable.star_yellow);
		// }
		// iv.setVisibility(View.INVISIBLE);
		// if (new SimpleDateFormat("dd.MM", Locale.US).format(new Date()).equals(current)) {
		// SharedPreferences.Editor editor = favourites.edit();
		// editor.putString(Data.lastShow, current);
		// editor.apply();
		// }
		// final TypedArray ta = getContext().obtainStyledAttributes(R.styleable.images);
		// final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		// alert.setTitle(getResources().getString(R.string.deleting));
		// alert.setMessage(getResources().getString(R.string.are_you_sure));
		// alert.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// SharedPreferences.Editor editor = favourites.edit();
		// Set<String> copy = new HashSet<String>(set);
		// copy.remove(current);
		// editor.putStringSet(Data.favourites, copy);
		// editor.apply();
		// iv.setImageDrawable(ta.getDrawable(R.styleable.images_star));
		// iv.invalidate();
		// Toast.makeText(DayFragment.this.getActivity(), "Removed current day (" + current + ") from favourites",
		// Toast.LENGTH_SHORT).show();
		// }
		// });
		// alert.setNegativeButton(getResources().getString(R.string.no), null);
		// iv.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Toast.makeText(getActivity(), current, Toast.LENGTH_LONG).show();
		// }
		// });
		// ta.recycle();
		return dayView;
	}
}
