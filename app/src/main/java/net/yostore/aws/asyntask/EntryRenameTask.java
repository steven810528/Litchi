package net.yostore.aws.asyntask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ncku.mis.litchi.R;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.entity.ApiResponse;
import net.yostore.aws.api.exception.APIException;
import net.yostore.aws.api.helper.BaseHelper;
import net.yostore.aws.api.helper.FileRenameHelper;
import net.yostore.aws.api.helper.FolderRenameHelper;
import net.yostore.aws.dialog.MessageDialog;
import net.yostore.aws.entity.FsInfo;
import net.yostore.aws.entity.FsInfo.EntryType;

//import net.yostore.aws.view.common.R;

public class EntryRenameTask extends AsyncTask<FsInfo, Integer, Integer>
{
	private static final String TAG = "EntryRenameTask";
	private AsyncTask<FsInfo, Integer, Integer> task;
	private Context ctx;
	private ApiConfig apiCfg;
	
	private ProgressDialog _mdialog;
	private String typeName = "";
	private int errCode = 0;
	private String errMsg = "";
	
	public EntryRenameTask(Context ctx, ApiConfig apiCfg)
	{
		this.ctx 	= ctx;
		this.apiCfg = apiCfg;		
	}

	@Override
	protected Integer doInBackground(FsInfo... fInfo)
	{
		StringBuilder msg = new StringBuilder();
		BaseHelper helper = null;
		ApiResponse rsp   = null;
		
		if ( fInfo[0].entryType == EntryType.File )
		{
			typeName = "File";
			helper	 = new FileRenameHelper(fInfo[0].entryId, false, false, fInfo[0].display);
		}
		else if ( fInfo[0].entryType == EntryType.Folder )
		{
			typeName = "Folder";
			helper	 = new FolderRenameHelper(fInfo[0].entryId, false, false, fInfo[0].display);
		}
		else
		{
			errCode = APIException.GENERAL_ERR;
			errMsg  = "Entry type is invalid!";
			Log.e(TAG, errMsg);
			
			return errCode;
		}
		
		msg.append(typeName).append(" renaming...");
		
		Log.d(TAG, msg.toString());
		
		this.publishProgress(0);
		try
		{
			rsp = helper.process(apiCfg);
			errCode = rsp.getStatus();
			
			return errCode;
		}
		catch ( APIException e )
		{
			msg.delete(0, msg.length());
			msg.append("Removing ").append(typeName)
			   .append(" Error:").append(e.getMessage());
			errCode = APIException.GENERAL_ERR;
			errMsg  = e.getMessage();
			
			Log.e(TAG, msg.toString(), e);
			return errCode;
		}
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		if ( _mdialog != null )
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
				onSuccess();
				break;
			default:
				switch ( result )
				{
					case APIException.EXC_AAA:
						errMsg = "Authenticatioin Fail";
						break;
					case 200:
						errMsg = "Access Deny";
						break;
					case APIException.EXC_FNX:	// Folder not found
						errMsg = "Folder Not Found";
						break;
					case APIException.EXC_FSX:	// File not found
						errMsg = "File Not Found";
				}
				onFail(errCode, errMsg);
				break;
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		if ( values[0] == 0 )
		{
			try
			{
				_mdialog = ProgressDialog.show(ctx, ctx.getString(R.string.app_name), typeName + " Renaming...", true, true, new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						task.cancel(true);
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
	
	protected void onSuccess()
	{
		String msg = typeName + " name has been changed!";
		Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
	}
	
	protected void onFail(int errCode, String errMsg)
	{		
		StringBuilder msg = new StringBuilder(typeName);
		msg.append(" remove fail, Error:").append(errCode)
		   .append(", Message:").append(errMsg);
		MessageDialog.show(ctx, typeName + " renameing...", msg.toString());
	}

}
