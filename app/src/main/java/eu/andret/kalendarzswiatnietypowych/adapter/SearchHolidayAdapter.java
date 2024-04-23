package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class SearchHolidayAdapter extends RecyclerView.Adapter<SearchHolidayAdapter.ViewHolder> {
	private final Context context;
	private final List<HolidayDay> holidayDays;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView dateTextView;
		private final TextView holidaysTextView;

		public ViewHolder(final View view) {
			super(view);
			dateTextView = view.findViewById(R.id.adapter_search_text_date);
			holidaysTextView = view.findViewById(R.id.adapter_search_text_content);
		}
	}

	public SearchHolidayAdapter(final Context context, final List<HolidayDay> holidayDays) {
		this.context = context;
		this.holidayDays = holidayDays;
		Log.d("UHC-adapter", holidayDays + "");
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_search, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final HolidayDay day = holidayDays.get(position);
		viewHolder.dateTextView.setText(String.format(Locale.ROOT, "%02d.%02d", day.getDay(), day.getMonth()));
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (preferences.getBoolean(context.getString(R.string.settings_key_theme_colorized), false)) {
			viewHolder.itemView.setBackgroundColor(Util.randomizeColor(context, day.getSeed()));
		}
		final boolean isUsual = preferences.getBoolean(context.getString(R.string.settings_key_usual_holidays), false);
		final String holidays = day.getHolidaysList(isUsual)
				.stream()
				.map(Holiday::getName)
				.map(name -> context.getString(R.string.pointed_text, name))
				.collect(Collectors.joining("\n"));
		viewHolder.holidaysTextView.setText(holidays);
		viewHolder.itemView.setOnClickListener(v -> {
			final Intent intent = new Intent(context, DayActivity.class);
			intent.putExtra(MainActivity.DAY, day.getDay());
			intent.putExtra(MainActivity.MONTH, day.getMonth());
			context.startActivity(intent);
		});
	}

	@Override
	public int getItemCount() {
		return holidayDays.size();
	}
}
