package eu.andret.kalendarzswiatnietypowych.utils;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.time.LocalDate;
import java.time.Month;

import eu.andret.kalendarzswiatnietypowych.R;
import lombok.Value;

public class Util {
	private final Context context;
	private static String[] months;
	private static String[] monthsGenitive;
	private final NetworkInfo networkInfo;

	@Value
	public static class MonthDayPair {
		Month month;
		int day;
	}

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
		llp.setMargins(30, 20, 30, 20);
		tv.setLayoutParams(llp);
		alert.setView(layout);
		alert.setPositiveButton(R.string.ok, null);
		alert.show();
	}

	public void createAd(final int viewId) {
		final AdView adView = ((Activity) context).findViewById(viewId);
		adView.loadAd(new AdRequest.Builder().build());
	}

	public void applyTheme() {
		final SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		final String string = context.getResources().getString(R.string.settings_theme_app);
		int anInt;
		try {
			anInt = theme.getInt(string, 1);
		} catch (final ClassCastException ex) {
			anInt = Integer.parseInt(theme.getString(string, "1"));
			theme.edit().putInt(string, anInt).apply();
		}
		context.setTheme(anInt == 1 ? R.style.AppTheme_Dark : R.style.AppTheme);
	}

	public String getMonth(final int id) {
		return months[id];
	}

	public String getMonthGenitive(final Month month) {
		return getMonthGenitive(month.getValue());
	}

	public String getMonthGenitive(final int id) {
		return monthsGenitive[id - 1];
	}

	@NonNull
	public MonthDayPair calculateDates(final int id) {
		final LocalDate now = LocalDate.now();
		if (now.isLeapYear()) {
			if (id < 60) {
				final LocalDate date = LocalDate.ofYearDay(now.getYear(), id);
				return new MonthDayPair(date.getMonth(), date.getDayOfMonth());
			}
			if (id == 60) {
				return new MonthDayPair(Month.FEBRUARY, 30);
			}
			final LocalDate date = LocalDate.ofYearDay(now.getYear(), id - 1);
			return new MonthDayPair(date.getMonth(), date.getDayOfMonth());
		}
		if (id < 59) {
			final LocalDate date = LocalDate.ofYearDay(now.getYear(), id);
			return new MonthDayPair(date.getMonth(), date.getDayOfMonth());
		}
		if (id == 59) {
			return new MonthDayPair(Month.FEBRUARY, 29);
		}
		if (id == 60) {
			return new MonthDayPair(Month.FEBRUARY, 30);
		}
		final LocalDate date = LocalDate.ofYearDay(now.getYear(), id - 2);
		return new MonthDayPair(date.getMonth(), date.getDayOfMonth());
	}
}
