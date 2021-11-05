package eu.andret.kalendarzswiatnietypowych.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public class HolidayAdapter extends ArrayAdapter<Holiday> {
	private final Context context;
	private final int color;

	private static class ViewHolder {
		private TextView holiday;
		private RelativeLayout background;
	}

	public HolidayAdapter(final Context context, final List<Holiday> holidays, final int color) {
		super(context, R.layout.adapter_holiday, holidays);
		this.context = context;
		this.color = color;
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
		holder.background.setBackgroundColor(color);
		convertView.setBackgroundColor(color);
		return convertView;
	}
}
