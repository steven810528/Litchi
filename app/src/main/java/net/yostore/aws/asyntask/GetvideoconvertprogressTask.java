package net.yostore.aws.asyntask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.entity.GetVideoconvertprogressResponse;
import net.yostore.aws.api.entity.VideoInfo;
import net.yostore.aws.api.exception.APIException;
import net.yostore.aws.api.helper.GetvideoconVertprogressHelp;

import java.util.ArrayList;
import java.util.List;

public class GetvideoconvertprogressTask extends
	AsyncTask<Void, Integer, Integer> {
	private String TAG = "GetvideoconvertprogressTask";
	private ProgressDialog _mdialog;
	private Context ctx;
	private ApiConfig apiCfg;
	private String fileid;
	private Intent startIntent;
	Bundle bundle = new Bundle();
	
	private List<VideoInfo> video;
	
	private int errCode = APIException.GENERAL_SUCC;
	private String errMsg = "";

	private GetVideoconvertprogressResponse GBRsp = null;

	public GetvideoconvertprogressTask(Context cxt, ApiConfig apiCfg,
			Intent startIntent, String fileID) {
		this.ctx = cxt;
		this.apiCfg = apiCfg;
		this.startIntent = startIntent;
		this.fileid = fileID;

	}

	@Override
	protected Integer doInBackground(Void... arg0) {

		this.publishProgress(0);
		StringBuilder msg = new StringBuilder();
		msg.delete(0, msg.length());

		GetvideoconVertprogressHelp GVHelp = new GetvideoconVertprogressHelp(
				fileid);
		Log.d(TAG, "currentFolderId:" + apiCfg.currentFolderId);

		try {
			GBRsp = (GetVideoconvertprogressResponse) GVHelp.process(apiCfg);
			bundle.putString("Status",  String.valueOf(GBRsp.getStatus()));
			if (GBRsp.getStatus() != APIException.GENERAL_SUCC) {
				errCode = GBRsp.getStatus();
				errMsg = "";
				msg.append("Doing folderBrowse fail, status:").append(errCode);
				Log.e(TAG, msg.toString());
				return errCode;
			} else {
				video= GBRsp.getVideoList();
				
				
				return APIException.GENERAL_SUCC;
			}

		} catch (APIException e) {

		}

		return null;

	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		if (_mdialog != null) {
			if (_mdialog != null)
				try {
					_mdialog.dismiss();
				} catch (Exception e) {
				}
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (values[0] == 0) {
			try {
				_mdialog = ProgressDialog
						.show(ctx,
								ctx.getString(com.ncku.mis.litchi.R.string.app_name),
								"Connecting to server...", true, true,
								new OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
									}
								});
			} catch (Exception e) {
			}
		} else {
			if (_mdialog != null) {
				try {
					_mdialog.dismiss();
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		this.publishProgress(100);
		goNextActivity();
	}

	protected void goNextActivity() {
		
		//bundle.putString("url", "test");
		ArrayList list = new ArrayList();
		list.add(video);
		bundle.putString("fileID", fileid);
		bundle.putParcelableArrayList("video", list);
		startIntent.putExtras(bundle);
		ctx.startActivity(this.startIntent);
	}

}