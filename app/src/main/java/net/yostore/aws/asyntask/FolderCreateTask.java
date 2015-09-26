package net.yostore.aws.asyntask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

import com.ncku.mis.litchi.R;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.entity.FolderCreateResponse;
import net.yostore.aws.api.exception.APIException;
import net.yostore.aws.api.helper.FolderCreateHelper;

import java.util.Calendar;
import java.util.TimeZone;

//import net.yostore.aws.view.common.R;

public class FolderCreateTask extends AsyncTask<String, Integer, Integer>
{
	private static final String TAG = "FolderCreateTask";
	private AsyncTask<String, Integer, Integer> task;
	private Context   ctx;
	private ApiConfig apiCfg;
	private String	  parent;
	
	private Long	  folderId;
	private String	  folderName;
	
	private ProgressDialog _mdialog;
	private int 		   errCode = APIException.GENERAL_SUCC;
	private String 		   errMsg = "";

	public FolderCreateTask(Context ctx, ApiConfig apiCfg, String parent)
	{
		this.task	= this;
		this.ctx 	= ctx;
		this.apiCfg = apiCfg;
		this.parent = parent;
	}

	@Override
	protected Integer doInBackground(String... name)
	{
		StringBuilder msg = new StringBuilder();
		
		if ( name != null && name.length > 0 )
			folderName = name[0];
		
		msg.append("Creating folder (").append(folderName).append(")");
		
		Log.d(TAG, msg.toString());
		
		String attribute = makeAttribute();
		
		this.publishProgress(0);
		
		FolderCreateHelper helper = new FolderCreateHelper(parent, folderName, attribute);
		try
		{
			FolderCreateResponse response = (FolderCreateResponse)helper.process(apiCfg);
			errCode = response.getStatus();
			
			if ( errCode == APIException.GENERAL_SUCC )
			{
				folderId = response.getId();
			}			
			
			return errCode;
		}
		catch ( APIException e )
		{
			errCode = e.status;
			errMsg  = e.getMessage();
			
			e.printStackTrace();
			
			msg.delete(0, msg.length());
			msg.append("Folder create error:(").append(errCode)
			   .append(")").append(errMsg);
			
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
				onSuccess(folderId);
				break;
			default:
				switch ( result )
				{
					case APIException.EXC_AAA:
						errMsg = "Authenticatioin Fail";
						break;
					case 3:
						errMsg = "Payload is not valid";
					case 200:
						errMsg = "Access Deny";
						break;
					case 211:
						errMsg = "Name can not be empty";
						break;
					case 213:
						errMsg = "Name is too long";
						break;
					case APIException.EXC_FEX:	// Folder not found
						errMsg = "Folder Not Found";
						break;
					case APIException.EXC_FNX:
						errMsg = "Parent is not existed";
						break;
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
				_mdialog = ProgressDialog.show(ctx, ctx.getString(R.string.app_name), "Creating " + folderName + "...", true, true, new OnCancelListener() {
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
	
	protected void onSuccess(long folderId)
	{
		
	}
	
	protected void onFail(int errCode, String errMsg)
	{
		
	}
	
	private String makeAttribute()
	{
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		long now = (long)(cal.getTimeInMillis()/1000);
		StringBuilder sb = new StringBuilder();
		sb.append("<creationtime>").append(now).append("</creationtime>");
		sb.append("<lastaccesstime>").append(now).append("</lastaccesstime>");
		sb.append("<lastwritetime>").append(now).append("</lastwritetime>");
		
		return sb.toString();
	}
}
