package eu.andret.kalendarzswiatnietypowych.util;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

public final class HolidayDiffCallback extends DiffUtil.Callback {
	private final List<Holiday> oldList;
	private final List<Holiday> newList;

	public HolidayDiffCallback(@NonNull final List<Holiday> oldList, @NonNull final List<Holiday> newList) {
		this.oldList = oldList;
		this.newList = newList;
	}

	@Override
	public int getOldListSize() {
		return oldList.size();
	}

	@Override
	public int getNewListSize() {
		return newList.size();
	}

	@Override
	public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
		return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
	}

	@Override
	public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
		return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
	}
}
