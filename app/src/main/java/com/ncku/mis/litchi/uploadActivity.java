package com.ncku.mis.litchi;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ecareme.asuswebstorage.ASUSWebStorage;

import net.yostore.aws.adapter.UploadBrowseAdapter;
import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.asyntask.BinaryUploadTask;
import net.yostore.aws.asyntask.UploadBrowseTask;
import net.yostore.aws.entity.FsInfo;
import net.yostore.aws.view.common.MyBrowseActivity;

import java.io.File;
import java.util.List;

/**
 * Created by steven on 2015/2/17.
 */
public class uploadActivity extends ListActivity{
    private static final String TAG = "UploadActivity";
    private ListActivity actUpload;
    private ApiConfig apiCfg;
    private UploadBrowseTask browse;
    private UploadBrowseAdapter browAdapter;

    //Declare component variables
    private TextView mPath;
    private ImageButton btnHome;
    private ImageButton btnBack;

    private String parentPath;

    private Long uploadFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.s_browse);

        actUpload = this;
        apiCfg	  = ASUSWebStorage.apiCfg;

        Bundle bundle = this.getIntent().getExtras();
        if ( bundle.getString("uploadFolder") != null && bundle.getString("uploadFolder").trim().length() > 0 )
            uploadFolder = Long.parseLong(bundle.getString("uploadFolder").trim());
        else
            uploadFolder = Long.parseLong(apiCfg.mySyncFolderId);

        //finding component's instance
        mPath   = (TextView) findViewById(R.id.mPath);
        btnHome = (ImageButton) findViewById(R.id.homeBt);
        btnBack = (ImageButton) findViewById(R.id.backBt);

        //Initial List Event Action
        initList();

        this.setListAdapter(new UploadBrowseAdapter(actUpload, R.layout.s_browse_item, ASUSWebStorage.processList));

        buildBrowsTask(ASUSWebStorage.uploadBasePath);
    }

    private void initList()
    {
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if ( browAdapter == null || browAdapter.getList() == null || position >= browAdapter.getList().size() )
                    return;
                // if (position >= fbRtn.getFbList().size()) return;

                FsInfo fi = browAdapter.getList().get(position);
                if ( fi.entryType == FsInfo.EntryType.Folder )
                {
                    buildBrowsTask(fi.entryId);
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                if ( browAdapter == null || browAdapter.getList() == null )
                    return false;
                // if (position >= fbRtn.getFbList().size()) return;

                final FsInfo fi = browAdapter.getList().get(position);
                if ( fi.entryType == FsInfo.EntryType.File )
                {
                    new AlertDialog.Builder(actUpload)
                            .setTitle("Uploading")
                            .setMessage(String.format("Uploading %s to server ?", fi.display))
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1)
                                {
                                    fileUpload(fi);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                            {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1)
                                {
                                }
                            })
                            .show();
                    return true;
                }
                else
                    return false;
            }
        });
    }

    public void backFunction(View v)
    {
        buildBrowsTask(parentPath);
    }

    private void buildBrowsTask(String path)
    {
        if ( "/".equals(path) && btnHome.getVisibility() == View.GONE )
        {
            btnBack.setVisibility(View.GONE);
            btnHome.setVisibility(View.VISIBLE);
        }
        else if ( !"/".equals(path) && btnBack.getVisibility() == View.GONE )
        {
            btnHome.setVisibility(View.GONE);
            btnBack.setVisibility(View.VISIBLE);
        }

        if ( browse != null && !browse.isCancelled() )
            browse.cancel(true);

        File f = new File(path);
        if ( "/".equals(path) )
            mPath.setText("/");
        else
            mPath.setText(f.getName());

        parentPath  = f.getParent();

        browse =  new UploadBrowseTask(actUpload)
        {
            @Override
            protected void onProgressUpdate(Integer... values)
            {
                if(values[0]==0)
                {
                    actUpload.setListAdapter(new UploadBrowseAdapter(actUpload, R.layout.s_browse_item, ASUSWebStorage.processList));
                }
            }

            @Override
            protected void refreshList(List<FsInfo> fsInfos)
            {
                browAdapter = new UploadBrowseAdapter(actUpload, R.layout.s_browse_item, fsInfos);
                actUpload.setListAdapter(browAdapter);
            }
        };
        browse.execute(path);
    }

    private void fileUpload(FsInfo fi)
    {
        BinaryUploadTask upload = new BinaryUploadTask(actUpload, apiCfg, fi)
        {
            @Override
            protected void doSuccess(Long fileId)
            {
                Intent intent = new Intent();
                intent.setClass(actUpload, MyBrowseActivity.class);

                Bundle bundle = new Bundle();
                bundle.putLong("fileId", fileId);

                intent.putExtras(bundle);

                actUpload.setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            protected void doFail(int errCode, String errMsg)
            {
                Intent intent = new Intent();
                intent.setClass(actUpload, MyBrowseActivity.class);

                Bundle bundle = new Bundle();
                bundle.putInt("errCode", errCode);
                bundle.putString("errMsg", errMsg);

                intent.putExtras(bundle);

                actUpload.setResult(RESULT_CANCELED, intent);
                finish();
            }

        };
        upload.execute(uploadFolder);
    }
}
