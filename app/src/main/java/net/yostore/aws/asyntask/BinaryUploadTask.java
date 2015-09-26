package net.yostore.aws.asyntask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.ncku.mis.litchi.R;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.entity.FinishBinaryUploadResponse;
import net.yostore.aws.api.entity.InitBinaryUploadResponse;
import net.yostore.aws.api.entity.ResumeBinaryUploadResponse;
import net.yostore.aws.api.exception.APIException;
import net.yostore.aws.api.helper.FinishBinaryUploadHelper;
import net.yostore.aws.api.helper.InitBinaryUploadHelper;
import net.yostore.aws.api.helper.ResumeBinaryUploadHelper;
import net.yostore.aws.dialog.MessageDialog;
import net.yostore.aws.entity.FsInfo;
import net.yostore.utility.ByteUtils;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//import net.yostore.aws.view.common.R;

public class BinaryUploadTask extends AsyncTask<Long, Integer, Integer> {
	private static final int BUFFER_SIZE = 1024 * 1024;

	private static final String TAG = "FileUploadTask";
	private AsyncTask<String, Integer, Integer> task;
	private Context ctx;
	private ApiConfig apiCfg;
	private ProgressDialog _mdialog;

	private int errCode = APIException.GENERAL_SUCC;
	private String errMsg = "";

	private FsInfo fin;
	private Long fileId = null;

	public BinaryUploadTask(Context ctx, ApiConfig apiCfg, FsInfo fi) {
		this.ctx = ctx;
		this.apiCfg = apiCfg;
		this.fin = fi;
	}

	@Override
	protected Integer doInBackground(Long... folderId) {
		StringBuilder msg = new StringBuilder();
		msg.append("Uploading file from ").append(fin.entryId)
				.append(" to folder (").append(folderId[0]).append(")...");

		Log.d(TAG, msg.toString());

		// Calculating file checksum
		File file = new File(fin.entryId);
		if (!file.exists()) {
			msg.delete(0, msg.length());
			msg.append("Uploading file (").append(fin.entryId)
					.append(") is not existed!");
			Log.e(TAG, msg.toString());

			errCode = APIException.GENERAL_ERR;
			errMsg = msg.toString();

			return errCode;
		}
		String atr = toXml(file);

		Log.d(TAG, "atr:" + atr);

		this.publishProgress(0);

		Log.d(TAG, "Calculating file checksum...");
		String fileChecksum = null;
		FileInputStream fi = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

		DigestInputStream dis = null;
		try {
			fi = new FileInputStream(file);
			dis = new DigestInputStream(fi, md);
			byte[] buff = new byte[BUFFER_SIZE];
			long count = 0;
			int read = 0;
			while ((read = dis.read(buff, 0, BUFFER_SIZE)) != -1) {
				count += read;
			}
			Log.d(TAG, "Read Length:" + count);

			byte[] checksum = md.digest();
			fileChecksum = ByteUtils.getHexString(checksum);
		} catch (IOException e) {
			msg.delete(0, msg.length());
			msg.append("Calculating file checksum error:").append(
					e.getMessage());
			Log.e(TAG, msg.toString(), e);

			errCode = APIException.GENERAL_ERR;
			errMsg = msg.toString();

			return errCode;
		} finally {
			try {
				fi.close();
				dis.close();
			} catch (IOException ie) {
			}
		}

		String transId = null;

		// initial binaryupload
		InitBinaryUploadResponse ibuResponse = null;
		msg.delete(0, msg.length());
		try {
			Log.d(TAG, "Initial Binary Uploading...");

			InitBinaryUploadHelper ibuHelper = new InitBinaryUploadHelper(
					apiCfg.token, folderId[0], fin.display, atr, fin.size,
					fileChecksum);
			ibuResponse = (InitBinaryUploadResponse) ibuHelper.process(apiCfg);
			errCode = ibuResponse.getStatus();

			if (errCode != APIException.GENERAL_SUCC) {
				msg.append("InitBinaryUpload fail, status:").append(errCode);
				errMsg = "";

				return errCode;
			}
			transId = ibuResponse.getTransactionId();
		} catch (APIException e) {
			errCode = e.status;
			errMsg = e.getMessage();

			msg.append("InitBinaryUpload error:(").append(errCode).append(")")
					.append(errMsg);
			Log.e(TAG, msg.toString(), e);

			return errCode;
		}

		Log.d(TAG, "TransactionId:" + (transId == null ? "null" : transId));

		// if fileId is not empty, means file has been duduped
		if (ibuResponse.getFileId() != null && ibuResponse.getFileId() > 0) {
			fileId = ibuResponse.getFileId();
			Log.d(TAG, "Get FileId:" + fileId);

			errCode = APIException.GENERAL_SUCC;
			return errCode;
		}

		if (transId == null || transId.trim().length() == 0) {
			errCode = APIException.GENERAL_ERR;
			Log.d(TAG, "Fetching transactionId fail!");

			return errCode;
		}

		// Doing file uploading by streaming

		msg.delete(0, msg.length());
		try {
			Log.d(TAG, "Resume Binary Uploading...");

			ResumeBinaryUploadHelper rbuHelper = new ResumeBinaryUploadHelper(
					apiCfg.token, transId, fin.entryId);
			ResumeBinaryUploadResponse rbuResponse = (ResumeBinaryUploadResponse) rbuHelper
					.process(apiCfg);
			errCode = rbuResponse.getStatus();

			if (errCode != APIException.GENERAL_SUCC) {
				msg.append("ResumeBinaryUpload fail, status:").append(errCode);
				errMsg = "";

				Log.e(TAG, msg.toString());

				return errCode;
			}
		} catch (APIException e) {
			errCode = e.status;
			errMsg = e.getMessage();

			msg.append("ResumeBinaryUpload error:(").append(errCode)
					.append(")").append(errMsg);
			Log.e(TAG, msg.toString(), e);

			return errCode;
		}

		// Finish binaryupload

		msg.delete(0, msg.length());
		try {
			Log.d(TAG, "Finish Binary Uploading...");

			FinishBinaryUploadHelper fbuHelper = new FinishBinaryUploadHelper(
					apiCfg.token, transId);
			FinishBinaryUploadResponse fbuResponse = (FinishBinaryUploadResponse) fbuHelper
					.process(apiCfg);
			errCode = fbuResponse.getStatus();

			if (errCode != APIException.GENERAL_SUCC) {
				msg.append("FinishBinaryUpload fail, status:").append(errCode);
				errMsg = "";

				Log.e(TAG, msg.toString());

				return errCode;
			}
			fileId = fbuResponse.getFileId();

			msg.append("FileId:").append(fileId);
			Log.d(TAG, msg.toString());
		} catch (APIException e) {
			errCode = e.status;
			errMsg = e.getMessage();

			msg.append("FinishBinaryUpload error:(").append(errCode)
					.append(")").append(errMsg);
			Log.e(TAG, msg.toString(), e);

			return errCode;
		}

		return errCode;
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
	protected void onPostExecute(Integer result) {
		this.publishProgress(100);

		switch (result) {
		case APIException.GENERAL_SUCC:
			Log.d(TAG, "GENERAL_SUCC:" + result);
			 doSuccess(fileId);
			break;
		default:

			Log.d(TAG, "default:" + result);
			 doFail(errCode, errMsg);
			break;
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

	protected void doSuccess(Long fileId) {
		StringBuilder msg = new StringBuilder();
		msg.append("File uploaded successful! FileI:").append(fileId);

		MessageDialog.show(ctx, "File Uploading", msg.toString());
	}

	protected void doFail(int errCode, String errMsg) {
		StringBuilder msg = new StringBuilder();
		msg.append("File uploading fail, status:").append(errCode)
				.append(", message:").append(errMsg);
		MessageDialog.show(ctx, "File Uploading", msg.toString());
	}


	protected String toXml(File file) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			// serializer.startDocument("UTF-8", true);
			// serializer.startTag("", "attribute");
			serializer.startTag("", "creationtime");
			serializer.text(""+file.lastModified());
			serializer.endTag("", "creationtime");
			serializer.startTag("", "lastaccesstime");
			serializer.text(""+file.lastModified());
			serializer.endTag("", "lastaccesstime");
			serializer.startTag("", "lastwritetime");
			serializer.text(""+file.lastModified());
			serializer.endTag("", "lastwritetime");

			// serializer.endTag("", "attribute");
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
