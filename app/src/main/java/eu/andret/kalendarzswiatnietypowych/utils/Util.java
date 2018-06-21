package eu.andret.kalendarzswiatnietypowych.utils;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import eu.andret.kalendarzswiatnietypowych.R;

public class Util {
	private final Context context;
	private static String[] months;
	private static String[] monthsGenitive;
	private final NetworkInfo networkInfo;
	
	public Util(Context context) {
		this.context = context;
		if (months == null) {
			months = context.getResources().getStringArray(R.array.months);
		}
		if (monthsGenitive == null) {
			monthsGenitive = context.getResources().getStringArray(R.array.months_genitive);
		}
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		networkInfo = connectivityManager.getActiveNetworkInfo();
	}
	
	public void createNotification(String title, String text, int ico, Intent intent, boolean cancelOnClick) {
		NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "UHC");
		notification.setSmallIcon(ico);
		notification.setContentTitle(title);
		notification.setContentText(text);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addNextIntent(intent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = notification.build();
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
	
	public void createAlert(String title, String text) {
		Builder alert = new Builder(context);
		alert.setTitle(title);
		alert.setMessage(text);
		alert.setPositiveButton(R.string.ok, null);
		alert.show();
	}
	
	public void createAlert(int title, int text) {
		Builder alert = new Builder(context);
		alert.setTitle(title);
		alert.setMessage(text);
		alert.setPositiveButton(R.string.ok, null);
		alert.show();
	}
	
	public void createAlertWithImage(int img, int title, int text) {
		Builder alert = new Builder(context);
		alert.setTitle(title);
		LinearLayout layout = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 30, 0, 0);
		layout.setLayoutParams(params);
		layout.setOrientation(LinearLayout.VERTICAL);
		ImageView image = new ImageView(context);
		image.setImageResource(img);
		layout.addView(image);
		TextView tv = new TextView(context);
		tv.setText(text);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.drawer_list_name_text));
		layout.addView(tv);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(30, 20, 30, 20); // llp.setMargins(left, top, right, bottom);
		tv.setLayoutParams(llp);
		alert.setView(layout);
		alert.setPositiveButton(R.string.ok, null);
		alert.show();
	}
	
	public void createAd(int viewId) {
		MobileAds.initialize(context, context.getResources().getString(R.string.banner_ad_unit_id));
		AdView adView = ((Activity) context).findViewById(viewId);
		adView.loadAd(new AdRequest.Builder().build());
	}
	
	public void applyTheme() {
		SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		context.setTheme(theme.getString(context.getResources().getString(R.string.settings_theme_app), "1").equals("1") ? R.style.AppTheme_Dark : R.style.AppTheme);
	}
	
	public String getMonth(int id) {
		return months[id];
	}
	
	public String getMonthGenitive(int id) {
		return monthsGenitive[id];
	}
}
