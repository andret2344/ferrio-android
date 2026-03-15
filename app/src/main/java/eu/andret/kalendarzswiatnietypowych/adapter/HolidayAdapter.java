package eu.andret.kalendarzswiatnietypowych.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.HolidayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public class HolidayAdapter extends ListAdapter<Holiday, HolidayAdapter.ViewHolder> {
	private static final DiffUtil.ItemCallback<Holiday> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
		@Override
		public boolean areItemsTheSame(@NonNull final Holiday oldItem,
				@NonNull final Holiday newItem) {
			return oldItem.getId().equals(newItem.getId());
		}

		@Override
		public boolean areContentsTheSame(@NonNull final Holiday oldItem,
				@NonNull final Holiday newItem) {
			return oldItem.equals(newItem);
		}
	};

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView nameTextView;
		private final TextView descriptionTextView;
		private final TextView countryTextView;

		public ViewHolder(final View view) {
			super(view);
			nameTextView = view.findViewById(R.id.adapter_holiday_name);
			descriptionTextView = view.findViewById(R.id.adapter_holiday_description);
			countryTextView = view.findViewById(R.id.adapter_holiday_text_country);
		}
	}

	public HolidayAdapter() {
		super(DIFF_CALLBACK);
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
		final Holiday holiday = getItem(position);
		final Context context = viewHolder.itemView.getContext();

		viewHolder.nameTextView.setText(holiday.getName());
		viewHolder.nameTextView.setTypeface(null, holiday.isUsual() ? Typeface.ITALIC : Typeface.NORMAL);

		if (holiday.getDescription().isBlank()) {
			viewHolder.descriptionTextView.setVisibility(View.GONE);
		} else {
			viewHolder.descriptionTextView.setText(holiday.getDescription());
			viewHolder.descriptionTextView.setVisibility(View.VISIBLE);
		}

		if (holiday.getCountry() != null && !holiday.getCountry().isBlank()) {
			final Emoji emoji = EmojiManager.getForAlias(holiday.getCountry().toLowerCase(Locale.ROOT));
			if (emoji != null) {
				viewHolder.countryTextView.setText(emoji.getUnicode());
				viewHolder.countryTextView.setTooltipText(holiday.getCountryName());
				viewHolder.countryTextView.setVisibility(View.VISIBLE);
			} else {
				viewHolder.countryTextView.setVisibility(View.GONE);
			}
		} else {
			viewHolder.countryTextView.setVisibility(View.GONE);
		}

		viewHolder.itemView.setOnClickListener(view -> {
			final Intent intent = new Intent(context, HolidayActivity.class);
			intent.putExtra(MainActivity.HOLIDAY, holiday.getId());
			context.startActivity(intent);
		});
	}
}
