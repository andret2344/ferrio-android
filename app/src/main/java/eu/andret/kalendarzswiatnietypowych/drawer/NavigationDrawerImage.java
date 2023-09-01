/*
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.drawer;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Objects;

public class NavigationDrawerImage extends ViewItem {
	private final Drawable image;

	public NavigationDrawerImage(final Drawable image) {
		this(image, null);
	}

	public NavigationDrawerImage(final Drawable image, final View.OnClickListener listener) {
		super(listener);
		this.image = image;
	}

	public Drawable getImage() {
		return image;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		final NavigationDrawerImage that = (NavigationDrawerImage) o;
		return Objects.equals(image, that.image);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), image);
	}

	@NonNull
	@Override
	public String toString() {
		return "NavigationDrawerImage{" +
				"image=" + image +
				"} " + super.toString();
	}
}
