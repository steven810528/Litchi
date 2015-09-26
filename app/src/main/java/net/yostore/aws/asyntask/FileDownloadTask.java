package net.yostore.aws.asyntask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.ecareme.asuswebstorage.ASUSWebStorage;
import com.ncku.mis.litchi.R;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.BaseApi;
import net.yostore.aws.api.entity.ApiCookies;
import net.yostore.aws.api.exception.APIException;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class FileDownloadTask extends AsyncTask<Void, Integer, Integer>
{
	private static final String TAG = "FileDownloadTask";
	private Context ctx;
	private ProgressDialog _mdialog;
	private ApiConfig apiCfg;
	private long fileId;
	private String fileName;
	private String destFile;
	
	private int errCode = 0;
	private String errMessage = "";

	public FileDownloadTask(Context ctx, long id, String name, ApiConfig apiCfg)
	{
		this.ctx 	  = ctx;
		this.fileId   = id;
		this.fileName = name;
		this.apiCfg   = apiCfg;
		
		if ( id < 1 )
		{
			Toast.makeText(ctx, "Download fileId invalid!", Toast.LENGTH_LONG);
			return;
		}
	}

	@Override
	protected Integer doInBackground(Void... arg0)
	{
		StringBuilder msg = new StringBuilder();
		
		this.publishProgress(0);
		
		String urlStr = "https://"+apiCfg.webRelay+"/webrelay/directdownload/"+apiCfg.token+"/?fi="+String.valueOf(fileId);

		HttpURLConnection conn = null;
		try
		{
			URL url = new URL(urlStr);
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(BaseApi.TIMEOUT); // 30 sec
			conn.setReadTimeout(BaseApi.TIMEOUT);
			conn.setRequestMethod("GET");

			ApiCookies apicookies = new ApiCookies();
			String cookieStr = "sid="+apicookies.getSid()+";c="+apicookies.getC_ClientType()+";v="+apicookies.getV_ClientVersion()+";x-v="+BaseApi.clientversion+";EEE_MANU_Maunfactory="+apicookies.getEEE_MANU_Maunfactory()+";EEE_PROD_ProductModal="+apicookies.getEEE_PROD_ProductModal()+";OS_VER="+Build.VERSION.SDK+";";
			conn.setRequestProperty("extension-pragma", cookieStr);
			conn.setRequestProperty("Cookie", cookieStr);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.connect();
			
			int status = conn.getResponseCode();
			if ( status != 200 )
			{
				msg.append("Downloading file fail, status").append(status)
				   .append(", message:").append(conn.getResponseMessage());
				Log.e(TAG, msg.toString());
				
				errCode    = status;
				errMessage = conn.getResponseMessage();
				
				return status;
			}
			
			InputStream in = conn.getInputStream();
			FileOutputStream fo = new FileOutputStream(this.destFile);
			try
			{
				IOUtils.copy(in, fo);
			}
			finally
			{
				fo.close();
			}
			
			return APIException.GENERAL_SUCC;
		}
		catch ( Exception e )
		{
			msg.append("Downloading file error:").append(e.getMessage());
			Log.e(TAG, msg.toString(), e);
			
			errCode    = APIException.GENERAL_ERR;
			errMessage = e.getMessage();
			
			return APIException.GENERAL_ERR;
		}
		finally
		{
			if ( conn != null )
				conn.disconnect();
		}
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		
		this.destFile = ASUSWebStorage.downloadPath + this.fileName;
		
		//Checking destination folder existance
		File fi = new File(ASUSWebStorage.downloadPath);
		if ( !fi.exists() )
		{
			if ( !fi.mkdirs() )
			{
				Log.e(TAG, "Creating download temp folder fail");
				this.cancel(true);
				return;
			}
		}
		
		//Checking destionation file name existance
		fi = new File(this.destFile);
		if ( fi.exists() )
		{
			this.fileName += ("_" + Calendar.getInstance().getTimeInMillis());			
			this.destFile = ASUSWebStorage.downloadPath + this.fileName;
		}
	}

	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		
		if ( _mdialog != null )
		{
			try
			{
				_mdialog.dismiss();
			}
			catch ( Exception e )
			{}
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
				onFail(errCode, errMessage);
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		if(values[0]==0)
		{
			try
			{
				_mdialog = ProgressDialog.show(ctx, 
											   ctx.getString(R.string.app_name),
											   "Downloading file...", true, true, 
											   new OnCancelListener() 
											   {
												   @Override
												   public void onCancel(DialogInterface dialog) 
												   {
												   }
											   }
											   );
			}
			catch ( Exception e )
			{}
		}
		else
		{
			if ( _mdialog != null )
			{
				try
				{
					_mdialog.dismiss();
				}
				catch ( Exception e )
				{}
			}
		}
	}
	
	protected void onSuccess()
	{
		Toast.makeText(ctx, "Successful!", Toast.LENGTH_LONG);
	}
	
	protected void onFail(int errCode, String errMessage)
	{
		StringBuilder msg = new StringBuilder();
		msg.append("Status:").append(errCode)
		   .append(", Message:").append(errMessage);
		
		Toast.makeText(ctx, msg.toString(), Toast.LENGTH_LONG);
	}
}
