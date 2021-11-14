package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public class HolidayAdapter extends ArrayAdapter<Holiday> {
	private final Context context;

	private static class ViewHolder {
		private TextView holiday;
	}

	public HolidayAdapter(final Context context, final List<Holiday> holidays) {
		super(context, R.layout.adapter_holiday, holidays);
		this.context = context;
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
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Holiday holiday = getItem(position);
		if (holiday == null) {
			return convertView;
		}

		holder.holiday.setText(getContext().getString(R.string.pointed_text, holiday.getText()));
		if (holiday.isUsual()) {
			holder.holiday.setTypeface(null, Typeface.BOLD);
		}
		return convertView;
	}
}
