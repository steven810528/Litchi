package net.yostore.aws.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.ncku.mis.litchi.R;
import net.yostore.aws.entity.FsInfo;

import android.app.ListActivity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BrowseAdapter extends ArrayAdapter<FsInfo>
{
	private static final String TAG = "BrowseAdapter";

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private ListActivity ctx;
	private List<FsInfo> list;

	private LayoutInflater mInflater;

	public BrowseAdapter(Context context, int textViewResourceId, List<FsInfo> list)
	{
		super(context, textViewResourceId, list);
		this.ctx 	= (ListActivity) context;
		this.list 	= list;
		
		mInflater 	= LayoutInflater.from(ctx);
	}

	@Override
	public void add(FsInfo object)
	{
		this.list.add(object);
		Log.d(TAG, "Add list, count:" + (list == null ? -1 : list.size()));
	}

	@Override
	public void insert(FsInfo object, int index)
	{
		this.list.add(index, object);
		Log.d(TAG, "Insert an entity into position:" + index);
	}

	@Override
	public void remove(FsInfo object)
	{
		this.list.remove(object);
		Log.d(TAG, "Remove list, count:" + (list == null ? -1 : list.size()));
	}

	@Override
	public int getCount()
	{
		return list == null ? 0 : list.size();
	}

	@Override
	public FsInfo getItem(int position)
	{
		return list == null ? null : (FsInfo)list.get(position);
	}

	@Override
	public long getItemId(int position)
	{		
//		return list == null ? -999999 : Long.valueOf(((FsInfo)list.get(position)).entryId);
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		StringBuilder msg = new StringBuilder();
		
		ViewHolder holder;
		final FsInfo fi = this.list.get(position);

		/* Using user-define layout */
		convertView = mInflater.inflate(R.layout.s_browse_item, null);

		/* Initial all objects in holder */
		holder = new ViewHolder();
		if(convertView.findViewById(android.R.id.progress)!=null)
			holder.progressBar = (ProgressBar) convertView.findViewById(android.R.id.progress);
		if(convertView.findViewById(R.id.toptext)!=null)
			holder.text = (TextView) convertView.findViewById(R.id.toptext);
		if(convertView.findViewById(R.id.iconid)!=null)
			holder.icon = (ImageView) convertView.findViewById(R.id.iconid);
		if(convertView.findViewById(R.id.bottomtext)!=null)
			holder.size = (TextView) convertView.findViewById(R.id.bottomtext);
		
		convertView.setTag(holder);

		String lastModifyDateDisp = " ";
		try
		{
			long dateLong = 0l;
			if (fi.attribute!=null && fi.attribute.getLastwritetime() != null && fi.attribute.getLastwritetime().trim().length()>0){
				dateLong = Long.valueOf(fi.attribute.getLastwritetime());
			}else if(fi.attribute!=null && fi.attribute.getCreationtime() != null && fi.attribute.getCreationtime().trim().length()>0){
				dateLong = Long.valueOf(fi.attribute.getCreationtime());
			}else if(fi.attribute!=null && fi.attribute.getLastaccesstime() != null && fi.attribute.getLastaccesstime().trim().length()>0){
				dateLong = Long.valueOf(fi.attribute.getLastaccesstime());
			}
			if(dateLong>0){
				Date mDate = new Date(dateLong*1000);
				lastModifyDateDisp += dateFormat.format(mDate);
			}
		}
		catch ( Exception e )
		{
			msg.delete(0, msg.length());
			msg.append("Converting file's last modified time error:").append(e.getMessage());
			Log.e(TAG, msg.toString(), e);
		}

		holder.kind = fi.entryType.getString();
		holder.fid = fi.entryId;
		holder.position = position;

		if ( fi.entryType == FsInfo.EntryType.File )
		{
			holder.text.setText(fi.display);
			if(fi.icon==0){
				fi.icon = R.drawable.icon_list_other;
			}
			holder.size.setText((fi.getSizeDisp()+","+lastModifyDateDisp).trim());
			holder.size.setVisibility(View.VISIBLE);
			
		}
		else if ( fi.entryType == FsInfo.EntryType.Folder)
		{
			holder.text.setText(fi.display);
			if ( fi.entryId != null )
			{
				fi.icon = R.drawable.icon_list_folder;
			}
			else
			{
				fi.icon = R.drawable.icon_list_rfolder;
			}
			holder.size.setText(lastModifyDateDisp.trim());
			holder.size.setVisibility(View.VISIBLE);
		}
		else if ( fi.entryType == FsInfo.EntryType.Process )
		{
			holder.progressBar.setVisibility(View.VISIBLE);
			holder.text.setText("Loading...");
			holder.icon.setVisibility(View.INVISIBLE);
			holder.size.setVisibility(View.INVISIBLE);
		}
		else if ( fi.entryType == FsInfo.EntryType.NUll )
		{
			holder.text.setText("Loading...");
			holder.icon.setVisibility(View.INVISIBLE);
			holder.size.setVisibility(View.INVISIBLE);
		}
		
		holder.icon.setImageResource(fi.icon);

		return convertView;
	}
	
	public List<FsInfo> getList()
	{
		return this.list;
	}
	
	/* class ViewHolder */
	protected class ViewHolder
	{
		ProgressBar progressBar; 
		TextView text;
		ImageView icon;
		TextView size;
		String kind;
		String fid;
		int position;
		boolean isStarred;
	}
}
