package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SpanningGridLayoutManager extends GridLayoutManager {
	public SpanningGridLayoutManager(final Context context, final int spanCount, final int orientation, final boolean reverseLayout) {
		super(context, spanCount, orientation, reverseLayout);
	}

	@Override
	public RecyclerView.LayoutParams generateDefaultLayoutParams() {
		return spanLayoutSize(super.generateDefaultLayoutParams());
	}

	@Override
	public RecyclerView.LayoutParams generateLayoutParams(final Context c, final AttributeSet attrs) {
		return spanLayoutSize(super.generateLayoutParams(c, attrs));
	}

	@Override
	public RecyclerView.LayoutParams generateLayoutParams(final ViewGroup.LayoutParams lp) {
		return spanLayoutSize(super.generateLayoutParams(lp));
	}

	@Override
	public boolean canScrollVertically() {
		return false;
	}

	@Override
	public boolean canScrollHorizontally() {
		return false;
	}

	private RecyclerView.LayoutParams spanLayoutSize(final RecyclerView.LayoutParams layoutParams) {
		if (getOrientation() == HORIZONTAL) {
			layoutParams.width = getHorizontalSpace() / getItemCount();
		} else if (getOrientation() == VERTICAL) {
			layoutParams.height = getVerticalSpace() / getItemCount();
		}
		return layoutParams;
	}

	private int getHorizontalSpace() {
		return getWidth() - getPaddingRight() - getPaddingLeft();
	}

	private int getVerticalSpace() {
		return getHeight() - getPaddingBottom() - getPaddingTop();
	}
}
