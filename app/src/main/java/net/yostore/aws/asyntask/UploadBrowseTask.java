package net.yostore.aws.asyntask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

import com.ncku.mis.litchi.R;

import net.yostore.aws.api.entity.B_FileInfo;
import net.yostore.aws.api.entity.B_FolderInfo;
import net.yostore.aws.api.exception.APIException;
import net.yostore.aws.entity.FsInfo;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

//import net.yostore.aws.view.common.R;

public class UploadBrowseTask extends AsyncTask<String, Integer, Integer>
{
	private static final String TAG = "UploadBrowseTask";
	private AsyncTask<String, Integer, Integer> task;
	private Context ctx;
	private ProgressDialog _mdialog;
	
	private List<FsInfo> list;
	
	private int    errCode = APIException.GENERAL_SUCC;
	private String errMsg  = "";

	public UploadBrowseTask(Context ctx)
	{
		this.ctx = ctx;
	}

	@Override
	protected Integer doInBackground(String... mPath)
	{
		this.publishProgress(0);
		
		StringBuilder msg = new StringBuilder();

		if ( mPath == null || mPath.length == 0 || mPath[0].trim().length() == 0 )
		{
			errCode = APIException.GENERAL_ERR;
			errMsg  = "Path can not be empty!";
			
			Log.e(TAG, errMsg);
			return errCode;
		}
		
		File file = new File(mPath[0]);
		
		if ( !file.isDirectory() )
		{
			msg.append(mPath).append(" is not a directory!");
			errCode = APIException.GENERAL_ERR;
			errMsg  = msg.toString();
			
			Log.e(TAG, errMsg);
			return errCode;
		}
		File[] files = file.listFiles();
		
		List<FsInfo> tmpList = new LinkedList<FsInfo>();
		
		for ( File f: files )
		{
			FsInfo fi = null;
			
			if ( f.isDirectory() )
			{
				B_FolderInfo fo = new B_FolderInfo();
				fo.setId(f.getAbsolutePath());
				fo.setDisplay(f.getName());
				fo.setCreatedtime(String.valueOf(f.lastModified()));
				
				fi = new FsInfo(fo);
			}
			else
			{
				B_FileInfo fo = new B_FileInfo();
				fo.setId(f.getAbsolutePath());
				fo.setDisplay(f.getName());
				fo.setSize(f.length());
				fo.setCreatedtime(String.valueOf(f.lastModified()));				
				
				fi = new FsInfo(fo);
			}			
			fi.parent = f.getParent();
			tmpList.add(fi);
		}
		
		this.list = tmpList;
		
		errCode = APIException.GENERAL_SUCC;
		return errCode;
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		if(_mdialog!=null)
		{
			if ( _mdialog != null ) try { _mdialog.dismiss(); } catch ( Exception e ){}
		}
	}

	@Override
	protected void onPostExecute(Integer result)
	{
		this.publishProgress(100);
		
		switch ( result )
		{
			case APIException.GENERAL_SUCC:
				refreshList(list);
				break;
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		if ( values[0]==0 )
		{
			try
			{
				_mdialog = ProgressDialog.show(ctx, ctx.getString(R.string.app_name), "Loading...", true, true, new OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialog) 
					{
						task.cancel(true);
						( (Activity)ctx ).finish();
					}
				});
			}
			catch(Exception e){}
		}
		else
		{
			if ( _mdialog != null ) try { _mdialog.dismiss(); } catch ( Exception e ){}
		}
	}
	
	protected void refreshList(List<FsInfo> fsInfos)
	{		
	}
}
