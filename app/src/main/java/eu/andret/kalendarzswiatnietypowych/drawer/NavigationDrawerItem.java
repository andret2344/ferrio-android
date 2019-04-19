package eu.andret.kalendarzswiatnietypowych.drawer;

import android.graphics.drawable.Drawable;
import android.view.View;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class NavigationDrawerItem extends ViewItem {
	private int name;
	private Drawable icon;

	public NavigationDrawerItem(int name, Drawable icon) {
		this(name, icon, null);
	}

	public NavigationDrawerItem(int name, Drawable icon, View.OnClickListener listener) {
		super(listener);
		this.name = name;
		this.icon = icon;
	}
}
