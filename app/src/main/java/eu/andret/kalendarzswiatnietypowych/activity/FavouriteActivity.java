package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.TreeSet;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.DayFavouriteAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class FavouriteActivity extends AppCompatActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final SharedPreferences theme = Data.getPreferences(this, Data.Prefs.THEME);
		final Util util = new Util(this);
		util.applyTheme();
		setContentView(R.layout.activity_favourite);
		if (getActionBar() != null) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		final ListView listView = findViewById(R.id.favourites_listview_calendar);
		listView.setAdapter(new DayFavouriteAdapter(this, new TreeSet<Holiday>().toArray(new Holiday[]{})));

		util.createAd(R.id.favourites_adview_bottom);

		final Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getResources().getString(R.string.settings_theme_app), "1")));
		listView.setBackgroundColor(color.foreground);
		findViewById(R.id.favourites_adview_bottom).setVisibility(color.dark ? View.VISIBLE : View.INVISIBLE);
		findViewById(R.id.favourites_relative_main).setBackgroundColor(color.background);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
