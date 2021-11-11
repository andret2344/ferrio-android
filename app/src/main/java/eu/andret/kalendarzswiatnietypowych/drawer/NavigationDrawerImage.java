/*
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.drawer;

import android.graphics.drawable.Drawable;
import android.view.View;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class NavigationDrawerImage extends ViewItem {
	Drawable image;

	public NavigationDrawerImage(final Drawable image) {
		this(image, null);
	}

	public NavigationDrawerImage(final Drawable image, final View.OnClickListener listener) {
		super(listener);
		this.image = image;
	}
}
