/**
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.drawer;

import android.view.View;

import java.util.Objects;

public abstract class ViewItem {
	protected final View.OnClickListener listener;

	protected ViewItem(final View.OnClickListener listener) {
		this.listener = listener;
	}

	public View.OnClickListener getListener() {
		return listener;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ViewItem viewItem = (ViewItem) o;
		return Objects.equals(listener, viewItem.listener);
	}

	@Override
	public int hashCode() {
		return Objects.hash(listener);
	}

	@Override
	public String toString() {
		return "ViewItem{" +
				"listener=" + listener +
				'}';
	}
}
