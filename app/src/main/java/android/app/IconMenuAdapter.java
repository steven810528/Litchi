package android.app;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class IconMenuAdapter extends ArrayAdapter<IconContextMenuItem>
{
	private Context context = null;
	private static final int LIST_PREFERED_HEIGHT = 65;
	private int viewResourceId = 0;
    private List<IconContextMenuItem> mItems;
	
    public IconMenuAdapter(Context context, int viewResourceId, List<IconContextMenuItem> list) {
    	super(context, viewResourceId, list);
    	this.context = context;
    	this.viewResourceId = viewResourceId;
    	this.mItems = list;
    	
    }
    
    /**
     * add item to adapter
     * @param menuItem
     */
    public void addItem(IconContextMenuItem menuItem) {
    	mItems.add(menuItem);
    }
    
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public IconContextMenuItem getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		IconContextMenuItem item = (IconContextMenuItem) getItem(position);
		return item.actionTag;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		IconContextMenuItem item = (IconContextMenuItem) getItem(position);
		
		Resources res = context.getResources();
		
		if (convertView == null) {
        	TextView temp = new TextView(context);
        	AbsListView.LayoutParams param = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, 
        																  AbsListView.LayoutParams.WRAP_CONTENT);
        	temp.setLayoutParams(param);
        	temp.setPadding((int)toPixel(res, 15), 0, (int)toPixel(res, 15), 0);
        	temp.setGravity(android.view.Gravity.CENTER_VERTICAL);
        	
        	Theme th = context.getTheme();
			TypedValue tv = new TypedValue();
			
			if (th.resolveAttribute(android.R.attr.textAppearanceLarge, tv, true)) {
				temp.setTextAppearance(context, tv.resourceId);
			}
//			temp.setTextAppearance(context, android.R.style.TextAppearance_Large);
			
        	temp.setMinHeight(LIST_PREFERED_HEIGHT);
        	temp.setCompoundDrawablePadding((int)toPixel(res, 14));
        	convertView = temp;
		}
		
		TextView textView = (TextView) convertView;
		textView.setTag(item);
		textView.setText(item.text);
		textView.setTextColor(Color.BLACK);
		textView.setCompoundDrawablesWithIntrinsicBounds(item.image, null, null, null);
//		textView.setBackgroundColor(android.R.color.darker_gray);
//		textView.setTextColor(android.R.color.);
    	
        return textView;
	}
	
	private float toPixel(Resources res, int dip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
		return px;
	}
}
