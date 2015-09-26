package android.app;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class IconContextMenuItem
{
	public final CharSequence text;
	public final Drawable image;
	public final int actionTag;

	/**
	 * public constructor
	 * 
	 * @param res
	 *            resource handler
	 * @param textResourceId
	 *            id of title in resource
	 * @param imageResourceId
	 *            id of icon in resource
	 * @param actionTag
	 *            indicate action of menu item
	 */
	public IconContextMenuItem(Resources res, int textResourceId,
			int imageResourceId, int actionTag) {
		text = res.getString(textResourceId);
		if (imageResourceId != -1) {
			image = res.getDrawable(imageResourceId);
		} else {
			image = null;
		}
		this.actionTag = actionTag;
	}

	/**
	 * public constructor
	 * @param res
	 * 			resource handler
	 * @param title
	 * 			menu item title
	 * @param imageResourceId
	 * 			id of icon in resource
	 * @param actionTag
	 * 			indicate action of menu item
	 */
	public IconContextMenuItem(Resources res, CharSequence title,
			int imageResourceId, int actionTag) {
		text = title;
		if (imageResourceId != -1) {
			image = res.getDrawable(imageResourceId);
		} else {
			image = null;
		}
		this.actionTag = actionTag;
	}
}
