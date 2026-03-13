package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.HolidayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.FloatingHolidaySuggestion;
import eu.andret.kalendarzswiatnietypowych.entity.ReportState;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class SuggestionFloatingAdapter extends ListAdapter<FloatingHolidaySuggestion, SuggestionFloatingAdapter.ViewHolder> {

	private static final DiffUtil.ItemCallback<FloatingHolidaySuggestion> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
		@Override
		public boolean areItemsTheSame(@NonNull final FloatingHolidaySuggestion oldItem, @NonNull final FloatingHolidaySuggestion newItem) {
			return oldItem.getId() == newItem.getId();
		}

		@Override
		public boolean areContentsTheSame(@NonNull final FloatingHolidaySuggestion oldItem, @NonNull final FloatingHolidaySuggestion newItem) {
			return oldItem.equals(newItem);
		}
	};

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView textViewDatetime;
		private final TextView textViewCountry;
		private final TextView textViewDate;
		private final TextView textViewStatus;
		private final TextView textViewName;
		private final TextView textViewDescription;
		private final ImageView chevron;

		public ViewHolder(final View view) {
			super(view);
			textViewDatetime = view.findViewById(R.id.adapter_suggestion_floating_text_datetime);
			textViewCountry = view.findViewById(R.id.adapter_suggestion_floating_text_country);
			textViewDate = view.findViewById(R.id.adapter_suggestion_floating_text_date);
			textViewStatus = view.findViewById(R.id.adapter_suggestion_floating_chip);
			textViewName = view.findViewById(R.id.adapter_suggestion_floating_text_name);
			textViewDescription = view.findViewById(R.id.adapter_suggestion_floating_text_description);
			chevron = view.findViewById(R.id.adapter_suggestion_floating_chevron);
		}
	}

	public SuggestionFloatingAdapter() {
		super(DIFF_CALLBACK);
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_suggestion_floating, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final FloatingHolidaySuggestion holiday = getItem(position);

		viewHolder.textViewDatetime.setText(holiday.getDatetime().format(Util.getDateTimeFormatter()));
		viewHolder.textViewCountry.setText(holiday.getCountry() != null ? Util.countryCodeToFlag(holiday.getCountry()) : "");
		viewHolder.textViewDate.setText(holiday.getDate());
		viewHolder.textViewName.setText(holiday.getName());
		viewHolder.textViewDescription.setText(holiday.getDescription());
		Util.applyStatusBadge(viewHolder.textViewStatus, holiday.getReportState());

		if (holiday.getReportState() == ReportState.APPLIED && holiday.getHolidayId() != null) {
			viewHolder.chevron.setVisibility(View.VISIBLE);
			viewHolder.itemView.setOnClickListener(view -> {
				final Intent intent = new Intent(view.getContext(), HolidayActivity.class);
				intent.putExtra(MainActivity.HOLIDAY, holiday.getFullHolidayId());
				view.getContext().startActivity(intent);
			});
		} else {
			viewHolder.chevron.setVisibility(View.GONE);
			viewHolder.itemView.setOnClickListener(null);
		}
	}
}
