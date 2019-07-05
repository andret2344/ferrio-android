package eu.andret.kalendarzswiatnietypowych.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class HolidayAdapter extends ArrayAdapter<Holiday> {
	private final Context context;
	private final int color;
	private final boolean allowReports;
	private final Util util;

	private static class ViewHolder {
		private TextView holiday;
		private RelativeLayout background;
		private ImageView report;
	}

	public HolidayAdapter(Context context, HolidayDay holiday, int color, boolean allowReports) {
		super(context, R.layout.adapter_holiday, holiday == null ? new ArrayList<>() : holiday.getHolidaysList(Data.getPreferences(context, Data.Prefs.THEME).getBoolean(context.getResources().getString(R.string.settings_usual_holidays), false)));
		this.context = context;
		this.color = color;
		this.allowReports = allowReports;
		util = new Util(getContext());
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert inflater != null;
			convertView = inflater.inflate(R.layout.adapter_holiday, parent, false);
			holder = new ViewHolder();
			holder.holiday = convertView.findViewById(R.id.adapter_holiday_text_holiday);
			holder.background = convertView.findViewById(R.id.adapter_holiday_relative_main);
			holder.report = convertView.findViewById(R.id.adapter_holiday_image_report);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Holiday h = getItem(position);
		if (h == null) {
			return convertView;
		}

		holder.holiday.setText(getContext().getResources().getString(R.string.pointer) + " " + h.getText());
		if (h.isUsual()) {
			holder.holiday.setTypeface(null, Typeface.BOLD);
		}
		SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		Data.AppColorSet appColorSet = Data.getColors(Integer.parseInt(theme.getString(getContext().getResources().getString(R.string.settings_theme_app), "1")));
		holder.holiday.setTextColor(appColorSet.forground);
		holder.background.setBackgroundColor(color);
		convertView.setBackgroundColor(color);
		if (!allowReports) {
			holder.report.setVisibility(View.INVISIBLE);
			return convertView;
		}
		holder.report.setVisibility(View.VISIBLE);
		holder.report.setOnClickListener(v -> {
			SharedPreferences tutorial = Data.getPreferences(context, Data.Prefs.TUTORIAL);
			boolean reportInfo = tutorial.getBoolean(context.getResources().getString(R.string.settings_tutorial_reports), false);
			if (!reportInfo) {
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				LayoutInflater adbInflater = LayoutInflater.from(context);
				View eulaLayout = adbInflater.inflate(R.layout.checkbox, parent, false);
				CheckBox dontShowAgain = eulaLayout.findViewById(R.id.skip);
				dontShowAgain.setOnClickListener(v1 -> {
					SharedPreferences.Editor editor = tutorial.edit();
					editor.putBoolean(context.getResources().getString(R.string.settings_tutorial_reports), dontShowAgain.isChecked());
					editor.apply();
				});
				alert.setView(eulaLayout);
				alert.setTitle(R.string.caution);
				alert.setMessage(R.string.report_tutorial_info);
				alert.setPositiveButton(R.string.yes, (dialog, which) -> {
					if (util.isConnection()) {
						Toast.makeText(getContext(), R.string.report_sending, Toast.LENGTH_LONG).show();
						h.report();
						Toast.makeText(getContext(), R.string.report_sent, Toast.LENGTH_SHORT).show();
					} else {
						util.createAlert(getContext().getResources().getString(R.string.caution), getContext().getResources().getString(R.string.no_internet));
					}
				});

				alert.setNegativeButton(R.string.no, null);
				alert.show();
			} else {
				if (util.isConnection()) {
					Toast.makeText(getContext(), R.string.report_sending, Toast.LENGTH_LONG).show();
					h.report();
					Toast.makeText(getContext(), R.string.report_sent, Toast.LENGTH_SHORT).show();
				} else {
					util.createAlert(getContext().getResources().getString(R.string.caution), getContext().getResources().getString(R.string.no_internet));
				}
			}
		});
		return convertView;
	}
}
