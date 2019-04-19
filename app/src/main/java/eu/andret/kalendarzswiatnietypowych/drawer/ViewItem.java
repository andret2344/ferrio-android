/**
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.drawer;

import android.view.View;

import lombok.Data;

@Data
public abstract class ViewItem {
	protected View.OnClickListener listener;

	public ViewItem(View.OnClickListener listener) {
		this.listener = listener;
	}
}
