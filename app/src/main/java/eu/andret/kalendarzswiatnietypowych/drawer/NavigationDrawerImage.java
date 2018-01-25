/**
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.drawer;

import android.graphics.drawable.Drawable;
import android.view.View;

public class NavigationDrawerImage implements ViewItem {
	private final Drawable image;
	private final View.OnClickListener listener;
	
	public NavigationDrawerImage(Drawable image, View.OnClickListener listener) {
		this.image = image;
		this.listener = listener;
	}
	
	public Drawable getIcon() {
		return image;
	}
	
	public View.OnClickListener getListener() {
		return listener;
	}
}
