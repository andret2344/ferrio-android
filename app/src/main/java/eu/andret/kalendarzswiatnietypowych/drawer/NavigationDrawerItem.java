package eu.andret.kalendarzswiatnietypowych.drawer;

import android.graphics.drawable.Drawable;
import android.view.View;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class NavigationDrawerItem extends ViewItem {
	int name;
	Drawable icon;

	public NavigationDrawerItem(final int name, final Drawable icon) {
		this(name, icon, null);
	}

	public NavigationDrawerItem(final int name, final Drawable icon, final View.OnClickListener listener) {
		super(listener);
		this.name = name;
		this.icon = icon;
	}
}
