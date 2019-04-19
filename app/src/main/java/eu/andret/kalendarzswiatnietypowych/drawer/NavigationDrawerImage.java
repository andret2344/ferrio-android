/**
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
	private Drawable image;

	public NavigationDrawerImage(Drawable image) {
		this(image, null);
	}

	public NavigationDrawerImage(Drawable image, View.OnClickListener listener) {
		super(listener);
		this.image = image;
	}
}
