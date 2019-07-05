package eu.andret.kalendarzswiatnietypowych.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerImage;
import eu.andret.kalendarzswiatnietypowych.drawer.NavigationDrawerItem;
import eu.andret.kalendarzswiatnietypowych.drawer.ViewItem;
import eu.andret.kalendarzswiatnietypowych.utils.Data;

public class DrawerAdapter extends ArrayAdapter<ViewItem> {
	public DrawerAdapter(Context context, List<ViewItem> values) {
		super(context, R.layout.drawer_list_item, values);
	}

	private static class ViewHolder {
		private TextView name;
		private ImageView icon;
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			assert inflater != null;
			convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
			holder = new ViewHolder();
			holder.name = convertView.findViewById(R.id.draweritem_text_name);
			holder.icon = convertView.findViewById(R.id.draweritem_image_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		SharedPreferences theme = Data.getPreferences(getContext(), Data.Prefs.THEME);
		Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getContext().getResources().getString(R.string.settings_theme_app), "1")));
		holder.name.setTextColor(color.forground);
		convertView.setBackgroundColor(color.background);
		ViewItem v = getItem(position);
		if (v instanceof NavigationDrawerItem) {
			NavigationDrawerItem ndi = (NavigationDrawerItem) v;
			holder.icon.setImageDrawable(ndi.getIcon());
			holder.name.setText(ndi.getName());
			convertView.setOnClickListener(ndi.getListener());
		} else if (v instanceof NavigationDrawerImage) {
			NavigationDrawerImage ndi = (NavigationDrawerImage) v;
			holder.icon.setImageDrawable(ndi.getImage());
			holder.icon.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			convertView.setOnClickListener(ndi.getListener());
		}
		return convertView;
	}
}
