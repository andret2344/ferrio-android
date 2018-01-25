package eu.andret.kalendarzswiatnietypowych;

import java.util.Calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import eu.andret.kalendarzswiatnietypowych.activities.MainActivity;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;

public class MyWidgetProvider extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		boolean dark = Data.getPreferences(context, Data.Prefs.THEME).getString(context.getResources().getString(R.string.settings_theme_widgets), "1").equals("1");
		Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH);
		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra("from", "widget");
		intent.putExtra("day", day);
		intent.putExtra("month", month);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), dark ? R.layout.widget_dark : R.layout.widget_light);
		SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		String output = "";
		HolidayDay ho = HolidayCalendar.getInstance(context).getTodayHolidays();
		if (ho == null || ho.countHolidays(theme.getBoolean(context.getResources().getString(R.string.settings_usual_holidays), false)) == 0) {
			output += context.getResources().getString(R.string.typical_day);
		} else {
			StringBuilder outputBuilder = new StringBuilder();
			for (Holiday s : ho.getHolidaysList(theme.getBoolean(context.getResources().getString(R.string.settings_usual_holidays), false))) {
				outputBuilder.append("\n\n").append(context.getResources().getString(R.string.pointer)).append(" ").append(s.getText());
			}
			output = outputBuilder.toString();
			output = output.substring(2);
		}
		remoteViews.setTextViewText(R.id.widget_text_holiday, output);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_relative_main, pendingIntent);
		for (int i : appWidgetIds) {
			intent.putExtra("widgetID", i);
			appWidgetManager.updateAppWidget(i, remoteViews);
		}
	}
}
