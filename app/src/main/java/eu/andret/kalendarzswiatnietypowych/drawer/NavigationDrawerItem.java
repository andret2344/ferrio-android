/**
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.drawer;

import android.graphics.drawable.Drawable;
import android.view.View;

public class NavigationDrawerItem implements Comparable<NavigationDrawerItem>, ViewItem {
	private final int name;
	private final Drawable icon;
	private final int id;
	private static int globalID = 0;
	private final View.OnClickListener listener;
	
	public NavigationDrawerItem(int name, Drawable icon, View.OnClickListener listener) {
		this.name = name;
		this.icon = icon;
		id = globalID++;
		this.listener = listener;
	}
	
	public int getName() {
		return name;
	}
	
	public Drawable getIcon() {
		return icon;
	}
	
	public View.OnClickListener getListener() {
		return listener;
	}
	
	@Override
	public int compareTo(NavigationDrawerItem another) {
		return id - another.id;
	}
}
