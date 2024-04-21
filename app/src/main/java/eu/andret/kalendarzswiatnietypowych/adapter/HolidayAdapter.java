package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.HolidayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.fragment.ReportFragment;
import eu.andret.kalendarzswiatnietypowych.fragment.ReportViewModel;

public class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.ViewHolder> {
	private final Context context;
	private final List<Holiday> holidays;

	private final ReportViewModel reportViewModel;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView holidayTextView;
		private final TextView countryTextView;
		private final ImageView moreImageView;

		public ViewHolder(final View view) {
			super(view);
			holidayTextView = view.findViewById(R.id.adapter_holiday_text_holiday);
			countryTextView = view.findViewById(R.id.adapter_holiday_text_country);
			moreImageView = view.findViewById(R.id.adapter_holiday_image_more);
		}
	}

	public HolidayAdapter(final Context context, final List<Holiday> holidays) {
		this.context = context;
		this.holidays = holidays;
		reportViewModel = new ViewModelProvider((AppCompatActivity) context).get(ReportViewModel.class);
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int viewType) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.adapter_holiday, viewGroup, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
		final Holiday holiday = holidays.get(position);
		if (holiday.getDescription().isBlank()) {
			viewHolder.moreImageView.setVisibility(View.INVISIBLE);
		} else {
			viewHolder.moreImageView.setOnClickListener(view -> {
				final Intent intent = new Intent(context, HolidayActivity.class);
				intent.putExtra(MainActivity.HOLIDAY, holiday.getId());
				context.startActivity(intent);
			});
		}

		viewHolder.holidayTextView.setText(holiday.getName());
		viewHolder.countryTextView.setText(holiday.getCountryCode());
		if (holiday.isUsual()) {
			viewHolder.holidayTextView.setTypeface(null, Typeface.BOLD);
		}

		viewHolder.itemView.setOnLongClickListener(v -> {
			reportViewModel.setHoliday(holiday);
			final FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
			final ReportFragment newFragment = new ReportFragment();
			final FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
					.add(android.R.id.content, newFragment)
					.addToBackStack(null)
					.commit();
			return true;
		});
	}

	@Override
	public int getItemCount() {
		return holidays.size();
	}
}
