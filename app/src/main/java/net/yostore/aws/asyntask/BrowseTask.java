package net.yostore.aws.asyntask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

import com.ncku.mis.litchi.R;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.entity.B_FileInfo;
import net.yostore.aws.api.entity.B_FolderInfo;
import net.yostore.aws.api.entity.BrowseFolderResponse;
import net.yostore.aws.api.exception.APIException;
import net.yostore.aws.api.helper.BrowseFolderHelper;
import net.yostore.aws.dialog.MessageDialog;
import net.yostore.aws.entity.FsInfo;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BrowseTask extends AsyncTask<Void, Integer, Integer> {
	private static final String TAG = "BrowseTask";
	private AsyncTask<Void, Integer, Integer> task;
	private ApiConfig apiCfg;
	private Context ctx;

	ProgressDialog _mdialog;
	private List<FsInfo> fsInfos;
	private BrowseFolderResponse fbRsp = null;

	private int errCode = APIException.GENERAL_SUCC;
	private String errMsg = "";

	public BrowseTask(Context ctx, String folderId, List<FsInfo> fsInfos,
			ApiConfig apiCfg) {
		this.apiCfg = apiCfg;
		this.ctx = ctx;
		this.fsInfos = fsInfos;

		if (folderId != null && folderId.trim().length() > 0)
			apiCfg.currentFolderId = folderId;
	}

	@Override
	protected Integer doInBackground(Void... arg0) {
		StringBuilder msg = new StringBuilder();

		// Setting progressBar initial value
		this.publishProgress(0);

		if (apiCfg.currentFolderId == null
				|| apiCfg.currentFolderId.trim().length() == 0) {
			apiCfg.currentFolderId = apiCfg.mySyncFolderId;
			apiCfg.folderName = apiCfg.SYNCFOLDERNAME;
		}

		msg.delete(0, msg.length());

		// Fetching folders & files information, that under specify folder
		BrowseFolderHelper fbHelp = new BrowseFolderHelper(
				apiCfg.currentFolderId, 1, 0, 0, 0);

		Log.d(TAG, "currentFolderId:" + apiCfg.currentFolderId);
		try {
			fbRsp = (BrowseFolderResponse) fbHelp.process(apiCfg);

			if (fbRsp.getStatus() != APIException.GENERAL_SUCC) {
				errCode = fbRsp.getStatus();
				errMsg = "";
				msg.append("Doing folderBrowse fail, status:").append(errCode);
				Log.e(TAG, msg.toString());

				return errCode;
			} else {
				// Get parent folderId;
				apiCfg.parentFolderId = String.valueOf(fbRsp.getParent());
				apiCfg.parentName = fbRsp.getRawfoldername();

				return APIException.GENERAL_SUCC;

			}
		} catch (APIException e) {
			errCode = e.status;
			errMsg = e.getMessage();
			msg.append("Doing folderBrowse error:").append(errMsg);
			Log.e(TAG, msg.toString(), e);

			return errCode;
		}
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
						ctx.getString(R.string.app_name), "Loading...", true,
						true, new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								task.cancel(true);
								((Activity) ctx).finish();
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

	@Override
	protected void onPostExecute(Integer result) {
		this.publishProgress(100);
		List<FsInfo> tmpList = new LinkedList<FsInfo>();
		if (result == APIException.GENERAL_SUCC) {
			if (fbRsp.getTotalcount() > 0) {
				// Adding folder info into List
				Iterator<B_FolderInfo> foIter = fbRsp.getFolderList()
						.iterator();
				while (foIter.hasNext()) {
					B_FolderInfo fo = foIter.next();
					tmpList.add(new FsInfo(fo));
				}

				// Adding file info into List
				Iterator<B_FileInfo> fiIter = fbRsp.getFileList().iterator();
				while (fiIter.hasNext()) {
					B_FileInfo fi = fiIter.next();
					tmpList.add(new FsInfo(fi));
				}
			}
		} else {
			StringBuilder msg = new StringBuilder();
			msg.append("Connecting server fail (Code:").append(errCode)
					.append(", Message:").append(errMsg)
					.append("), please retry later!");
			MessageDialog.show(ctx, ctx.getString(R.string.app_name),
					msg.toString());
		}
		this.fsInfos = tmpList;
		refreshList(this.fsInfos);
	}

	protected void refreshList(List<FsInfo> fsInfos) {
	}
}
