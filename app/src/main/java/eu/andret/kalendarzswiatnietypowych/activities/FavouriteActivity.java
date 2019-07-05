package eu.andret.kalendarzswiatnietypowych.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.TreeSet;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapters.DayFavouriteAdapter;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class FavouriteActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences theme = Data.getPreferences(this, Data.Prefs.THEME);
		Util util = new Util(this);
		util.applyTheme();
		setContentView(R.layout.activity_favourite);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		ListView listView = findViewById(R.id.favourites_listview_calendar);
//		SharedPreferences favourites = Data.getPreferences(this, Data.Prefs.FAVOURITES);
//		Set<String> favs = favourites.getStringSet("favourites", new HashSet<String>());// TODO -> strings.xml
//		favs.hashCode();
		listView.setAdapter(new DayFavouriteAdapter(this, new TreeSet<HolidayCalendar.HolidayMonth.HolidayDay.Holiday>().toArray(new HolidayCalendar.HolidayMonth.HolidayDay.Holiday[]{})));

		util.createAd(R.id.favourites_adview_bottom);

		Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getResources().getString(R.string.settings_theme_app), "1")));
		listView.setBackgroundColor(color.forground);
		findViewById(R.id.favourites_adview_bottom).setVisibility(color.dark ? View.VISIBLE : View.INVISIBLE);
		findViewById(R.id.favourites_relative_main).setBackgroundColor(color.background);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
