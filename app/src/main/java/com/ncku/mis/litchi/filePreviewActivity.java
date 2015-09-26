package com.ncku.mis.litchi;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ecareme.asuswebstorage.ASUSWebStorage;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.helper.GetFullTextCompanionHelper;
import net.yostore.aws.api.helper.GetResizedPhotoHelper;
import net.yostore.aws.api.helper.GetVideoSnapshotHelper;

public class filePreviewActivity extends Activity
{
//	private static final String TAG = "FilePreviewActivity";
//	private Activity  actPreview;
	private ApiConfig apiCfg;
	
	private long 	  fileId   = -999;
	private String	  fileName = "";
	private int 	  fileType = 0;
	
	private TextView  mPath;
	private ImageView previewImg;
	private TextView  previewText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.s_preview);
		
//		this.actPreview = this;
		this.apiCfg		= ASUSWebStorage.apiCfg;

		Intent intent = getIntent();
		if ( intent != null )
		{
			Bundle bundle = intent.getExtras();
			if ( bundle != null )
			{
				fileId   = bundle.getLong("fileId", -999);
				fileName = bundle.getString("fileName");
				fileType = bundle.getInt("type", 1);
			}
		}
		
		mPath		= (TextView)  findViewById(R.id.mPath);
		previewImg  = (ImageView) findViewById(R.id.previewImg);
		previewText = (TextView)  findViewById(R.id.previewText);

		// Change title to show file name
		if ( fileName.trim().length() != 0 )
			mPath.setText(fileName);
		
		if ( fileType == 2 )
		{
			if ( previewText.getVisibility() == View.GONE )
			{
				previewText.setVisibility(View.VISIBLE);
				previewImg.setVisibility(View.GONE);
			}
			
			GetFullTextCompanionHelper helper = new GetFullTextCompanionHelper(fileId, 0, false);
			try
			{
				byte[] txt = helper.process(apiCfg);
				previewText.setText(new String(txt, "UTF-8"));
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}
			
		}
		else if ( fileType == 1 || fileType == 3 )
		{
			Bitmap bm = null;
			
			// Switching display
			if ( previewImg.getVisibility() != View.VISIBLE )
			{
				previewImg.setVisibility(View.VISIBLE);
				previewText.setVisibility(View.GONE);
			}
			
			if ( fileType == 1 )
			{
				GetResizedPhotoHelper helper = new GetResizedPhotoHelper(fileId, 3, true);
				try
				{
					bm = helper.process(apiCfg);
				}
				catch ( Exception e )
				{
					e.printStackTrace();
				}
			}
			else
			{
				GetVideoSnapshotHelper helper = new GetVideoSnapshotHelper(fileId, true);
				try
				{
					bm = helper.process(apiCfg);
				}
				catch ( Exception e )
				{
					e.printStackTrace();
				}
			}
			if ( bm != null )
				previewImg.setImageBitmap(bm);
		}

	}
}
