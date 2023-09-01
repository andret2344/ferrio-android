package eu.andret.kalendarzswiatnietypowych.drawer;

import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.Objects;

public class NavigationDrawerItem extends ViewItem {
	private final int name;
	private final Drawable icon;

	public NavigationDrawerItem(final int name, final Drawable icon, final View.OnClickListener listener) {
		super(listener);
		this.name = name;
		this.icon = icon;
	}

	public int getName() {
		return name;
	}

	public Drawable getIcon() {
		return icon;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final NavigationDrawerItem that = (NavigationDrawerItem) o;
		return name == that.name && Objects.equals(icon, that.icon);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, icon);
	}

	@NonNull
	@Override
	public String toString() {
		return "NavigationDrawerItem{" +
				"name=" + name +
				", icon=" + icon +
				'}';
	}
}
