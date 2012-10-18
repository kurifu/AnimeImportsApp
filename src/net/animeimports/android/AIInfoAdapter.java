package net.animeimports.android;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AIInfoAdapter extends ArrayAdapter<String> {
	private ArrayList<String> items;
	Context mContext = null;
	
	public ArrayList<String> getItems() {
		return items;
	}

	public AIInfoAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
		super(context, textViewResourceId, items);
		mContext = context;
		if(items.size() != 0) {
    		this.items = items;
		}
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if(v == null) {
			LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row_info, null);
		}
		String info = items.get(position);
		TextView tt = (TextView) v.findViewById(R.id.textInfo);
		ImageView icon = (ImageView) v.findViewById(R.id.iconInfo);
		
		//LinearLayout llRowEvent = (LinearLayout) (mContext.findViewById(R.id.llRowEvent));
		//parent.setLayoutParams(new LayoutParams(30, 30));
		//android.view.ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(30, 30);
		//icon.setLayoutParams(layoutParams);
		
		//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
		//icon.setLayoutParams(params);
		
		
		//image.setScaleType(ImageView.ScaleType.CENTER_CROP);
		//layout.addView(image);
		
		//icon.setLayoutParams(layoutParams);
		//LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(30, 30);
		//yourImageView.setLayoutParams(layoutParams);
		
		//icon.setLayoutParams(new LayoutParams(30, 30));
		
		if(info != null) {
			icon.setImageResource(getIcon(position));
			if(tt != null) {
				tt.setText(info);
			}
		}
		return v;
	}


	/**
	 * Returns the appropriate icon for the provided event
	 * Draft: return the latest (at the time) set's Common symbol
	 * FNM: return the latest (at the time) set's Uncommon symbol
	 * Release/PreRelease/Gameday: return the latest (at the time) set's Rare symbol
	 * GPT: return the latest (at the time) set's Mythic symbol
	 * @param input
	 * @return
	 */
	private int getIcon(int input) {
		int retVal = R.drawable.icon_map;
		switch(input) {
		case 0:
			retVal = R.drawable.icon_map;
			break;
		case 1:
			retVal = R.drawable.icon_phone;
			break;
		case 2:
			retVal = R.drawable.icon_email;
			break;
		}
		return retVal;
	}
}
