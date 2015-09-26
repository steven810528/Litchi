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
import net.yostore.aws.api.entity.SetAdvancedSharecodeResponse;
import net.yostore.aws.api.exception.APIException;
import net.yostore.aws.api.helper.SetAdvancedSharecodeHelper;
import net.yostore.aws.dialog.MessageDialog;
import net.yostore.aws.entity.FsInfo;
import net.yostore.aws.entity.FsInfo.EntryType;

public class ClearSetAdvancedSharecodeTask extends
		AsyncTask<FsInfo, Integer, Integer> {
	private static final String TAG = "ClearSetAdvancedSharecodeTask";
	AsyncTask<FsInfo, Integer, Integer> task = this;
	private Context ctx;
	private ProgressDialog _mdialog;
	private ApiConfig apiCfg;

	String error = null;
	String shareCode;

	public ClearSetAdvancedSharecodeTask(Context ctx, ApiConfig apiCfg) {
		this.ctx = ctx;
		this.apiCfg = apiCfg;
	}

	@Override
	protected Integer doInBackground(FsInfo... fInfo) {
		int rtn = 0;

		String test = fInfo[0].entryType == EntryType.File ? "0" : "1";
		Log.d(TAG, "file :" + test);

		SetAdvancedSharecodeHelper helper = new SetAdvancedSharecodeHelper(
				fInfo[0].entryType == EntryType.File ? "0" : "1",
				fInfo[0].entryId, "1");

		try {
			SetAdvancedSharecodeResponse response = (SetAdvancedSharecodeResponse) helper
					.process(apiCfg);
			if (response == null) {
				error = "Can not connect to server!\rresponse is null";
				Log.e(TAG, error);
			} else if (response.getStatus() == 0) {
				shareCode = response.getSharecode();
				Log.d(TAG, "Isgroupaware: " + response.getIsgroupaware());
				rtn = 1;
			} else {
				error = "Can not connect to server!\rstatus:"
						+ response.getStatus();
				Log.e(TAG, error);
			}

		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rtn;

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
				_mdialog = ProgressDialog.show(ctx,
						ctx.getString(R.string.app_name),
						"Connecting to server...", true, true,
						new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								task.cancel(true);
							}
						});
			} catch (Exception e) {
			}
		} else {
			if (_mdialog != null)
				try {
					_mdialog.dismiss();
				} catch (Exception e) {
				}
		}
	}

	protected void onPostExecute(Integer result) {
		this.publishProgress(100);
		if (result == 1) {
			MessageDialog.show(ctx, ctx.getString(R.string.app_name),
					"ShareCode is Clear");
		} else {
			Toast.makeText(ctx, error, Toast.LENGTH_LONG).show();
		}
	}
}