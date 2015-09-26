package net.yostore.aws.asyntask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ecareme.asuswebstorage.ASUSWebStorage;
import com.ncku.mis.litchi.R;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.entity.AcquireTokenResponse;
import net.yostore.aws.api.entity.GetMySyncFolderResponse;
import net.yostore.aws.api.entity.RequestServiceGatewayResponse;
import net.yostore.aws.api.exception.APIException;
import net.yostore.aws.api.helper.AcquireTokenHelper;
import net.yostore.aws.api.helper.GetMySyncFolderHelper;
import net.yostore.aws.api.helper.RequestServiceGatewayHelper;
import net.yostore.aws.dialog.MessageDialog;
//import android.widget.Toast;

public class LoginTask extends AsyncTask<Void, Integer, Integer>
{
	String TAG = "LoginTask";
	ProgressDialog _mdialog;
	Context ctx;
	ApiConfig apiCfg;
	String secure;
	Intent startIntent;
	
	public LoginTask(Context cxt, ApiConfig apiCfg, Intent startIntent, String... secure)
	{
		this.ctx 		 = cxt;
		this.apiCfg		 = apiCfg;
		this.startIntent = startIntent;
		
		if ( secure != null && secure.length > 0 )
			this.secure = secure[0];
	}

    @Override
    protected Integer doInBackground(Void... params)
	{

		this.publishProgress(0);
		
		StringBuilder msg = new StringBuilder();
		
		//Fetching ServiceGateway Domain from ServicePortal By UserId & hashed Password
    	if ( apiCfg.ServiceGateway == null || 
    		 apiCfg.ServiceGateway.trim().length() == 0 )
    	{
			msg.delete(0, msg.length());
    		try
			{
    			RequestServiceGatewayHelper rsgh = new RequestServiceGatewayHelper();
				RequestServiceGatewayResponse rsp = (RequestServiceGatewayResponse)rsgh.process(apiCfg);
				if ( rsp.getStatus() == 0 )
				{
					apiCfg.ServiceGateway = rsp.getServicegateway();
					
					//Only for lab debugging
					if ( apiCfg.ServiceGateway.lastIndexOf(":") > 0 )
					{
						int pos = apiCfg.ServiceGateway.lastIndexOf(":");
						apiCfg.ServiceGateway = apiCfg.ServiceGateway.substring(0, pos);
					}
					
					msg.append("Got ServiceGateway : [").append(apiCfg.ServiceGateway).append("]");					
					Log.d(TAG, msg.toString());
					
//					Toast.makeText(ctx, msg.toString(), Toast.LENGTH_LONG).show();
				}
				else
				{
					msg.append("Request ServiceGateway Fail, Status:").append(rsp.getStatus());
					Log.e(TAG, msg.toString());
					
//					MessageDialog.show(ctx, ctx.getString(R.string.app_name), msg.toString());
					return rsp.getStatus();
				}
			}
			catch ( APIException e )
			{
				msg.append("Request ServiceGateway Error:").append(e.getMessage());
				Log.e(TAG, msg.toString(), e);
				
//				MessageDialog.show(ctx, ctx.getString(R.string.app_name), msg.toString());
				return e.status;
			}
    	}

    	// Acquire Token from ServiceGateway
    	try
    	{
    		msg.delete(0, msg.length());
    		AcquireTokenHelper ath = new AcquireTokenHelper();
    		ath.setAuxpassword(this.secure);
    		AcquireTokenResponse rsp = (AcquireTokenResponse)ath.process(apiCfg);
    		
    		if ( rsp.getStatus() != 0 )
    		{
    			msg.append("Acquire Token Fail, Status:[").append(rsp.getStatus()).append("]");
    			Log.e(TAG, msg.toString());
    			
//    			Toast.makeText(ctx, msg.toString(), Toast.LENGTH_LONG).show();
    			return rsp.getStatus();
    			
    		}

    		//*
    		//Fetcing Token & User Package Info
    		apiCfg.token		  = rsp.getToken();
    		apiCfg.infoRelay 	  = rsp.getInforelay();
    		apiCfg.webRelay  	  = rsp.getWebrelay();
    		apiCfg.packageDisplay = rsp.getPackageinfo().getDisplay();
    		apiCfg.capacity		  = rsp.getPackageinfo().getCapacity();
    		apiCfg.expireDate	  = rsp.getPackageinfo().getExpire();
    		
    		if ( ASUSWebStorage.isDebug )
    		{
    			int pos = apiCfg.infoRelay.lastIndexOf(":");
    			if ( pos > 0 )
    				apiCfg.infoRelay = apiCfg.infoRelay.substring(0, pos) + ":444";
    		}
    		
    		msg.append("Got the token:[").append(apiCfg.token).append("]\n")
    		   .append("InfoRelay:[").append(apiCfg.infoRelay).append("]\n")
    		   .append("WebRelay:[").append(apiCfg.webRelay).append("]\n")
    		   .append("Package:[").append(apiCfg.packageDisplay).append("]\n")
    		   .append("Capacity:[").append(apiCfg.capacity).append("]\n")
    		   .append("ExpireDate:[").append(apiCfg.expireDate).append("]")
    		   ;
    		
    		Log.i(TAG, msg.toString());
    		
//    		Toast.makeText(ctx, msg.toString(), Toast.LENGTH_LONG).show();
    		//*/
    	}
    	catch ( APIException e )
    	{
    		msg.append("Acquire Token Error:").append(e.getMessage());
    		Log.e(TAG, msg.toString(), e);
    		
//    		MessageDialog.show(ctx, ctx.getString(R.string.app_name), msg.toString());
    		return e.status;
    	}
    	
    	//Finding MySyncFolder real folderId
		if ( apiCfg.mySyncFolderId == null || apiCfg.mySyncFolderId.trim().length() == 0 )
		{
			msg.delete(0, msg.length());
			
			// if SyncFolderId is empty, do propFind to get sync folderId
			GetMySyncFolderHelper mySyncFolder = new GetMySyncFolderHelper();
			try
			{
				GetMySyncFolderResponse rsp = (GetMySyncFolderResponse)mySyncFolder.process(apiCfg);
				if ( rsp.getStatus() != APIException.GENERAL_SUCC )
				{
					msg.append("Get sync folder fail, status:").append(rsp.getStatus());
					Log.e(TAG, msg.toString());
					
					return rsp.getStatus();
				}
				apiCfg.mySyncFolderId = rsp.getId();
				apiCfg.parentFolderId = apiCfg.SYNCROOTID;
				apiCfg.currentFolderId= rsp.getId();
				apiCfg.folderName 	  = apiCfg.SYNCFOLDERNAME;
			}
			catch ( APIException e )
			{
				msg.append("Get sync folder error:").append(e.getMessage());
				Log.e(TAG, msg.toString(), e);
				
				return APIException.GENERAL_ERR;
			}

		}

		return 0;
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
				goNextActivity();
				break;
			case APIException.EXC_AAA:			//UserId & password authorize fail
				loginFail(result);
				break;
			case APIException.EXC_OAUTH:		//SID & ProgKey authorize fail
				authFail(result);
				break;
			case APIException.EXC_OTP_AUTH:		//Need OTP Authorization
				goOTP(result);
				break;
			case APIException.EXC_OTP_LOCK:		//OTP in LOCKED state
				goOTP(result);
				break;
			case APIException.EXC_CAPTCHA:		//CAPTCHA fail
				goCAPTCHA(result);
				break;
			default:
				loginFail(result);
				break;
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
											   "Connecting to server...", true, true, 
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

	protected void goOTP(Integer result)
	{
		MessageDialog.show(ctx, ctx.getString(R.string.app_name), "You have OTP. Please input OTP secure code.");
	}

	protected void goCAPTCHA(Integer result)
	{
		MessageDialog.show(ctx, ctx.getString(R.string.app_name), "Please input the righe code in the graphic.");
	}
	
	protected void loginFail(Integer result)
	{
		((Activity)ctx).finish();
	}
	
	protected void authFail(Integer result)
	{
		MessageDialog.show(ctx, ctx.getString(R.string.app_name), "Application authorizing fail!");
		((Activity)ctx).finish();
	}
	
	protected void goNextActivity()
	{
		ctx.startActivity(this.startIntent);
		((Activity)ctx).finish();
	}
}
