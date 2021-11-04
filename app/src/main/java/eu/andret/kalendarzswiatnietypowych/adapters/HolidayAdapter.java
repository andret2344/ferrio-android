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
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
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

	public HolidayAdapter(final Context context, final HolidayDay holiday, final int color, final boolean allowReports) {
		super(context, R.layout.adapter_holiday, holiday == null ? new ArrayList<>() : holiday.getHolidaysList(Data.getPreferences(context, Data.Prefs.THEME).getBoolean(context.getResources().getString(R.string.settings_usual_holidays), false)));
		this.context = context;
		this.color = color;
		this.allowReports = allowReports;
		util = new Util(getContext());
	}

	@NonNull
	@Override
	public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		final Holiday holiday = getItem(position);
		if (holiday == null) {
			return convertView;
		}

		holder.holiday.setText(getContext().getResources().getString(R.string.pointed_text, holiday.getText()));
		if (holiday.isUsual()) {
			holder.holiday.setTypeface(null, Typeface.BOLD);
		}
		final SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		final Data.AppColorSet appColorSet = Data.getColors(theme.getInt(getContext().getResources().getString(R.string.settings_theme_app), 1));
		holder.holiday.setTextColor(appColorSet.foreground);
		holder.background.setBackgroundColor(color);
		convertView.setBackgroundColor(color);
		if (!allowReports) {
			holder.report.setVisibility(View.INVISIBLE);
			return convertView;
		}
		holder.report.setVisibility(View.VISIBLE);
		holder.report.setOnClickListener(v -> {
			final SharedPreferences tutorial = Data.getPreferences(context, Data.Prefs.TUTORIAL);
			final boolean reportInfo = tutorial.getBoolean(context.getResources().getString(R.string.settings_tutorial_reports), false);
			if (!reportInfo) {
				final AlertDialog.Builder alert = new AlertDialog.Builder(context);
				final LayoutInflater adbInflater = LayoutInflater.from(context);
				final View eulaLayout = adbInflater.inflate(R.layout.checkbox, parent, false);
				final CheckBox dontShowAgain = eulaLayout.findViewById(R.id.skip);
				dontShowAgain.setOnClickListener(v1 -> {
					final SharedPreferences.Editor editor = tutorial.edit();
					editor.putBoolean(context.getResources().getString(R.string.settings_tutorial_reports), dontShowAgain.isChecked());
					editor.apply();
				});
				alert.setView(eulaLayout);
				alert.setTitle(R.string.caution);
				alert.setMessage(R.string.report_tutorial_info);
				alert.setPositiveButton(R.string.yes, (dialog, which) -> {
					if (util.isConnection()) {
						Toast.makeText(getContext(), R.string.report_sending, Toast.LENGTH_LONG).show();
//						h.report();
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
//					h.report();
					Toast.makeText(getContext(), R.string.report_sent, Toast.LENGTH_SHORT).show();
				} else {
					util.createAlert(getContext().getResources().getString(R.string.caution), getContext().getResources().getString(R.string.no_internet));
				}
			}
		});
		return convertView;
	}
}
