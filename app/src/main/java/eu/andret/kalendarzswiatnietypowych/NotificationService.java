package eu.andret.kalendarzswiatnietypowych;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import eu.andret.kalendarzswiatnietypowych.activities.DayActivity;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class NotificationService extends Service {
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class PollTask extends AsyncTask<Intent, Void, Void> {
		
		@Override
		protected Void doInBackground(Intent... params) {
			SharedPreferences favourites = Data.getPreferences(getBaseContext(), Data.Prefs.FAVOURITES);
			Set<String> days = favourites.getStringSet("favourites", new HashSet<String>()); // TODO -> strings.xml
			String last = favourites.getString("lastShow", null); // TODO -> strings.xml
			for (String s : days) {
				if (s.equals(new SimpleDateFormat("dd.MM", Locale.US).format(new Date())) && !s.equals(last)) {
					Intent i = new Intent(getBaseContext(), DayActivity.class);
					i.putExtra("from", "calendar");
					int id = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
					i.putExtra("id", id < 60 ? id : id - 1);
					new Util(getBaseContext()).createNotification(getResources().getString(R.string.app_name), getResources().getString(R.string.favourite_today), R.drawable.ic_star_white_24dp, i, true);
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			new PollTask().execute();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new PollTask().execute();
		return START_NOT_STICKY;
	}
}
