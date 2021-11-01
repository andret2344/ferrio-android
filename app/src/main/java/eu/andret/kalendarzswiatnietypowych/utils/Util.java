package eu.andret.kalendarzswiatnietypowych.utils;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import eu.andret.kalendarzswiatnietypowych.R;

public class Util {
	private final Context context;
	private static String[] months;
	private static String[] monthsGenitive;
	private final NetworkInfo networkInfo;

	public Util(final Context context) {
		this.context = context;
		if (months == null) {
			months = context.getResources().getStringArray(R.array.months);
		}
		if (monthsGenitive == null) {
			monthsGenitive = context.getResources().getStringArray(R.array.months_genitive);
		}
		final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		networkInfo = connectivityManager.getActiveNetworkInfo();
	}

	public void createNotification(final String title, final String text, final int ico, final Intent intent, final boolean cancelOnClick) {
		final NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "UHC");
		notification.setSmallIcon(ico);
		notification.setContentTitle(title);
		notification.setContentText(text);

		final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addNextIntent(intent);
		final PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setContentIntent(resultPendingIntent);
		final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification n = notification.build();
		if (cancelOnClick) {
			n.flags |= Notification.FLAG_AUTO_CANCEL;
		} else {
			n.flags |= Notification.FLAG_NO_CLEAR;
		}
		mNotificationManager.notify(1, n);
	}

	public boolean isConnection() {
		return networkInfo != null && networkInfo.isConnected();
	}

	public void createAlert(final String title, final String text) {
		final Builder alert = new Builder(context);
		alert.setTitle(title);
		alert.setMessage(text);
		alert.setPositiveButton(R.string.ok, null);
		alert.show();
	}

	public void createAlert(final int title, final int text) {
		final Builder alert = new Builder(context);
		alert.setTitle(title);
		alert.setMessage(text);
		alert.setPositiveButton(R.string.ok, null);
		alert.show();
	}

	public void createAlertWithImage(final int img, final int title, final int text) {
		final Builder alert = new Builder(context);
		alert.setTitle(title);
		final LinearLayout layout = new LinearLayout(context);
		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 30, 0, 0);
		layout.setLayoutParams(params);
		layout.setOrientation(LinearLayout.VERTICAL);
		final ImageView image = new ImageView(context);
		image.setImageResource(img);
		layout.addView(image);
		final TextView tv = new TextView(context);
		tv.setText(text);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.drawer_list_name_text));
		layout.addView(tv);
		final LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(30, 20, 30, 20); // llp.setMargins(left, top, right, bottom);
		tv.setLayoutParams(llp);
		alert.setView(layout);
		alert.setPositiveButton(R.string.ok, null);
		alert.show();
	}

	public void createAd(final int viewId) {
//		MobileAds.initialize(context, context.getResources().getString(R.string.banner_ad_unit_id));
		final AdView adView = ((Activity) context).findViewById(viewId);
		adView.loadAd(new AdRequest.Builder().build());
	}

	public void applyTheme() {
		final SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		context.setTheme(theme.getString(context.getResources().getString(R.string.settings_theme_app), "1").equals("1") ? R.style.AppTheme_Dark : R.style.AppTheme);
	}

	public String getMonth(final int id) {
		return months[id];
	}

	public String getMonthGenitive(final int id) {
		return monthsGenitive[id];
	}
}
