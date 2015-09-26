package com.ncku.mis.litchi;


import android.app.AlertDialog;
import android.app.IconContextMenuItem;
import android.app.IconMenuAdapter;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecareme.asuswebstorage.ASUSWebStorage;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import net.yostore.aws.adapter.BrowseAdapter;
import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.exception.APIException;
import net.yostore.aws.asyntask.BrowseTask;
import net.yostore.aws.asyntask.ClearSetAdvancedSharecodeTask;
import net.yostore.aws.asyntask.EntryRemoveTask;
import net.yostore.aws.asyntask.EntryRenameTask;
import net.yostore.aws.asyntask.FileDownloadTask;
import net.yostore.aws.asyntask.FolderCreateTask;
import net.yostore.aws.asyntask.SetAdvancedSharecodeTask;
import net.yostore.aws.asyntask.ShareEntryTask;
import net.yostore.aws.dialog.MessageDialog;
import net.yostore.aws.entity.FsInfo;
import net.yostore.aws.entity.FsInfo.EntryType;
import net.yostore.aws.menu.ActivityMenu;

import java.util.ArrayList;
import java.util.List;

public class myBrowseActivity extends ListActivity {
    private ListActivity actBrow;

    private static final String TAG = "MyBrowseActivity";
    private ApiConfig apiCfg;
    //產生的列表
    private BrowseTask browse;
    private BrowseAdapter browAdapter;

    // Declare component variables
    private TextView mPath;
    private ImageButton btnHome;
    private ImageButton btnBack;
    protected String fileUrl;

    private String android_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.s_browse);

        actBrow = this;
        apiCfg = ASUSWebStorage.apiCfg;

        Parse.enableLocalDatastore(actBrow);
        Parse.initialize(actBrow, "9SEi5AMIUibTgWPBZAAFKxvw2DIOSYv0LQFrCFHu", "fGQhe7GTXIoO3CYAIhfesUdr87YWZ1Ezdu5wSsOQ");

        android_id = Settings.Secure.getString(this.getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        loginParse(android_id);

        // finding component's instance
        mPath = (TextView) findViewById(R.id.mPath);
        btnHome = (ImageButton) findViewById(R.id.homeBt);
        btnBack = (ImageButton) findViewById(R.id.backBt);

        // Initial List Event Action
        initList();

        this.setListAdapter(new BrowseAdapter(actBrow, R.layout.s_browse_item,
                ASUSWebStorage.processList));

        buildBrowsTask();
    }

    private void initList() {
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (browAdapter == null || browAdapter.getList() == null
                        || position >= browAdapter.getList().size())
                    return;
                // if (position >= fbRtn.getFbList().size()) return;

                FsInfo fi = browAdapter.getList().get(position);
                //handle the fold type
                if (fi.entryType == EntryType.Folder) {
                    apiCfg.parentFolderId = apiCfg.currentFolderId;
                    apiCfg.parentName = apiCfg.folderName;
                    apiCfg.currentFolderId = fi.entryId;
                    apiCfg.folderName = fi.display;

                    buildBrowsTask();
                }

                //handle the file type
                else if (fi.entryType == EntryType.File) {
                    // Recognize file type
                    int type = recognizeFileType(fi.display);

                    if (type > 0) {

                        if (type == 3) {
                            //不提供影片播放功能
                            // setViedoPlay(fi);

                            //setViedoPlay_only(fi);

                        }
                        //key point of our idea!!
                        else if(type == 5)
                        {
                            Intent intent = new Intent();
                            intent.setClass(actBrow, printActivity.class);

                            //get the fileUrl
                            ShareEntryTask sh = new ShareEntryTask(actBrow, apiCfg,1);
                            sh.execute(fi);
                            fileUrl=ASUSWebStorage.buffer;
                            ASUSWebStorage.buffer=null;

                            //MessageDialog.show(actBrow,"getUrl",ASUSWebStorage.buffer);

                            if(fileUrl!=null) {

                                Bundle bundle = new Bundle();
                                bundle.putLong("fileId", Long.parseLong(fi.entryId));
                                bundle.putString("fileName", fi.display);
                                bundle.putInt("type", type);
                                bundle.putString("url", fileUrl);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        }
                        else {

                            MessageDialog.show(actBrow,"warning!","only support the PDF file type!");
                        }

                    }
                }

            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                if (browAdapter == null || browAdapter.getList() == null)
                    return false;
                // if (position >= fbRtn.getFbList().size()) return;

                FsInfo fi = browAdapter.getList().get(position);
                if (fi.entryType == FsInfo.EntryType.Folder) {
                    showFolderContextMenu(position);
                    return true;
                } else if (fi.entryType == FsInfo.EntryType.File) {
                    showFileContextMenu(position);
                    return true;
                } else
                    return false;
            }
        });
    }

    public void backFunction(View v) {
        apiCfg.currentFolderId = apiCfg.parentFolderId;
        apiCfg.folderName = apiCfg.parentName;

        buildBrowsTask();
    }
    //using while keep pressing the folder
    protected void showFolderContextMenu(final int position) {
        final FsInfo fi = browAdapter.getList().get(position);

        String[] itemArray = getResources().getStringArray(
                R.array.folder_long_click);
        TypedArray itemIconArray = getResources().obtainTypedArray(
                R.array.folder_long_click_icon);

        // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        List<IconContextMenuItem> itemList = new ArrayList<IconContextMenuItem>();
        for (int i = 0; i < itemArray.length; i++) {
            IconContextMenuItem _item;
            _item = new IconContextMenuItem(getResources(), itemArray[i],
                    itemIconArray.getResourceId(i, -1), i);
            itemList.add(_item);
        }

        IconMenuAdapter tmpAdapter = new IconMenuAdapter(actBrow,
                R.layout.s_context_menu_item, itemList);
        AlertDialog dialog = new AlertDialog.Builder(actBrow)
                .setTitle(fi.display)
                .setAdapter(tmpAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:// share
                                setShareType(fi);
                                // new ShareEntryTask(actBrow, apiCfg).execute(fi);
                                break;
                            case 1:// Rename
                                showRenameDialog(actBrow, apiCfg, fi, position);
                                break;
                            case 2:// delete
                                showRemoveDialog(actBrow, apiCfg, fi);
                                break;
                        }
                    }
                }).create();
        dialog.show();
    }
    //using while keep pressing the item
    protected void showFileContextMenu(final int position) {
        final FsInfo fi = browAdapter.getList().get(position);
        Log.d(TAG, "Now Position -->" + position);

        int menuid = R.array.file_marked_long_click;
        int menuIconid = R.array.file_marked_long_click_icon;

        String[] itemArray = getResources().getStringArray(menuid);
        TypedArray itemIconArray = getResources().obtainTypedArray(menuIconid);

        // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        List<IconContextMenuItem> itemList = new ArrayList<IconContextMenuItem>();
        for (int i = 0; i < itemArray.length; i++) {
            IconContextMenuItem _item;
            _item = new IconContextMenuItem(getResources(), itemArray[i],
                    itemIconArray.getResourceId(i, -1), i);
            itemList.add(_item);
        }
        IconMenuAdapter tmpAdapter = new IconMenuAdapter(actBrow,
                R.layout.s_context_menu_item, itemList);
        AlertDialog dialog = new AlertDialog.Builder(actBrow)
                .setTitle(fi.display)
                .setAdapter(tmpAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:// download
                                FileDownloadTask fdTask = new FileDownloadTask(
                                        actBrow, Long.valueOf(fi.entryId),
                                        fi.display, apiCfg) {

                                    @Override
                                    protected void onSuccess() {
                                        super.onSuccess();
                                    }

                                    @Override
                                    protected void onFail(int errCode,
                                                          String errMessage) {
                                        super.onFail(errCode, errMessage);
                                    }

                                };
                                fdTask.execute((Void) null);

                                break;
                            case 1:// share
                                // new ShareEntryTask(actBrow,
                                // apiCfg).execute(fi);

                                setShareTypeFIle(fi);
                                break;
                            case 2:// rename
                                showRenameDialog(actBrow, apiCfg, fi, position);
                                break;
                            case 3:// delete
                                showRemoveDialog(actBrow, apiCfg, fi);
                                break;
                            case 4://set print
                                break;
                        }
                    }
                }).create();
        dialog.show();
    }

    private void showRenameDialog(final Context ctx, final ApiConfig apicfg,
                                  final FsInfo fInfo, final int position) {
        final int dotPos = fInfo.display.lastIndexOf(".");
        String oldName;
        // String fileExt;
        if (dotPos > 0) {
            // fileExt = fInfo.display.substring(dotPos).trim();
            oldName = fInfo.display.substring(0, dotPos);
        } else {
            // fileExt = "";
            oldName = fInfo.display;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Rename");

        final EditText editName = new EditText(ctx);

        editName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editName.setText(oldName);
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // task.cancel(true);
                    }

                });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileExt;
                if (dotPos > -1) {
                    fileExt = fInfo.display.substring(dotPos).trim();
                } else {
                    fileExt = "";
                }
                final String newName = editName.getEditableText().toString()
                        .trim()
                        + fileExt;

                if (!chkName(newName)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Rename");
                    builder.setNeutralButton("OK", null);
                    builder.setMessage("Please enter correct name. The\\ /:*?\"&lt;&gt;| symbols are not accepted.");
                    builder.create().show();
                    return;
                }
                final FsInfo fs = new FsInfo();
                fs.display = newName;
                fs.entryType = fInfo.entryType;
                fs.entryId = fInfo.entryId;
                fs.parent = fInfo.parent;
                fs.attribute = fInfo.attribute;
                fs.size = fInfo.size;
                fs.icon = fInfo.icon;

                new EntryRenameTask(ctx, apicfg) {

                    @Override
                    protected void onSuccess() {

                        // Removed original entity
                        browAdapter.remove(fInfo);

                        // Insert the entity with new name at original position
                        browAdapter.insert(fs, position);
                        actBrow.setListAdapter(browAdapter);

                        super.onSuccess();
                    }

                    @Override
                    protected void onFail(int errCode, String errMsg) {
                        super.onFail(errCode, errMsg);
                    }

                }.execute(fs);
            }

        });
        builder.setView(editName);
        builder.create().show();
    }

    private void showRemoveDialog(final Context ctx, final ApiConfig apicfg,
                                  final FsInfo fInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Delete");
        builder.setMessage(String.format("Are you sure to delete %s?",
                fInfo.display));

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // task.cancel(true);
                    }

                });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                new EntryRemoveTask(ctx, apicfg) {

                    @Override
                    protected void onSuccess() {
                        super.onSuccess();

                        // Removing entity from List
                        browAdapter.remove(fInfo);
                        actBrow.setListAdapter(browAdapter);
                    }

                    @Override
                    protected void onFail(int errCode, String errMsg) {
                        super.onFail(errCode, errMsg);
                    }

                }.execute(fInfo);
            }

        });
        builder.create().show();
    }

    private boolean chkName(String name) {
        return (!(name.indexOf("\\") > -1 || name.indexOf("/") > -1
                || name.indexOf(":") > -1 || name.indexOf("*") > -1
                || name.indexOf("?") > -1 || name.indexOf(">") > -1
                || name.indexOf("|") > -1 || name.indexOf("<") > -1 || name
                .indexOf("\"") > -1) && name.length() > 0 && name.length() < 250);
    }
    //Upload the file we choose
    private void fileUploadFunction(String folderId) {
        Intent intent = new Intent();
        intent.setClass(actBrow, uploadActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("uploadFolder", folderId);
        intent.putExtras(bundle);

        startActivityForResult(intent, 0);
        // startActivity(intent);
    }
    //Create a new folder
    private void folderCreateFunction(String folderId) {
        showCreateFolderDialog(actBrow, this.apiCfg,
                this.apiCfg.currentFolderId);
    }

    private void showCreateFolderDialog(final Context ctx,
                                        final ApiConfig apicfg, String parent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Creating new folder");
        final EditText editName = new EditText(ctx);
        editName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editName.setHint("Please input new folder name");
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // task.cancel(true);
                    }

                });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newName = editName.getEditableText().toString().trim();
                // Pattern p = Pattern.compile("[%#/:\\x2a\\x3f\\<>|\'\\s\"]+");
                // Matcher m = p.matcher(newName);
                // if (m.find()) {
                // }
                if (!chkName(newName)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Creating new folder");
                    builder.setNeutralButton("OK", null);
                    builder.setMessage("Please enter correct name. The\\ /:*?\"&lt;&gt;| symbols are not accepted.");
                    builder.create().show();
                    return;
                }

                FolderCreateTask fcTask = new FolderCreateTask(actBrow, apiCfg,
                        apiCfg.currentFolderId) {

                    @Override
                    protected void onSuccess(long folderId) {
                        buildBrowsTask();
                    }

                    @Override
                    protected void onFail(int errCode, String errMsg) {
                        StringBuilder msg = new StringBuilder();

                        msg.append("Create folder error:(").append(errCode)
                                .append(")").append(errMsg);

                        MessageDialog.show(actBrow, "Creating new folder",
                                msg.toString());
                    }

                };
                fcTask.execute(newName);
            }

        });
        builder.setView(editName);
        builder.create().show();
    }
    //show the item in the folder. It's a sub function.
    private void buildBrowsTask() {
        if (apiCfg.mySyncFolderId.equals(apiCfg.currentFolderId)
                && btnHome.getVisibility() == View.GONE) {
            btnBack.setVisibility(View.GONE);
            btnHome.setVisibility(View.VISIBLE);
        } else if (!apiCfg.mySyncFolderId.equals(apiCfg.currentFolderId)
                && btnBack.getVisibility() == View.GONE) {
            btnHome.setVisibility(View.GONE);
            btnBack.setVisibility(View.VISIBLE);
        }

        if (browse != null && !browse.isCancelled())
            browse.cancel(true);

        browse = new BrowseTask(actBrow, null, ASUSWebStorage.processList,
                apiCfg) {
            @Override
            protected void onProgressUpdate(Integer... values) {
                if (values[0] == 0) {
                    mPath.setText(apiCfg.folderName);
                    actBrow.setListAdapter(new BrowseAdapter(actBrow,
                            R.layout.s_browse_item, ASUSWebStorage.processList));
                }
            }

            @Override
            protected void refreshList(List<FsInfo> fsInfos) {
                browAdapter = new BrowseAdapter(actBrow,
                        R.layout.s_browse_item, fsInfos);
                actBrow.setListAdapter(browAdapter);
            }
        };
        browse.execute((Void) null);
    }
    //辨識檔案類別,
    protected int recognizeFileType(String name) {
        String fileType = FsInfo.parseFileType(name);
        int type = 0;

        if (fileType.equals("image/*"))
            type = 1;
        else if (fileType.equals("video/*"))
            type = 3;
        else if (fileType.equals("application/zip"))
            type = 0;
        else if (fileType.equals("application/pdf"))
            type = 5;
        else {
            type = 2;
        }
        return type;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Long fileId = bundle.getLong("fileId");
            Toast.makeText(actBrow, "File Uploaded Successful!",
                    Toast.LENGTH_LONG);

            // Refresh list
            buildBrowsTask();
        } else {
            int errCode = APIException.GENERAL_ERR;
            String errMsg = "";

            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    errCode = bundle
                            .getInt("errCode", APIException.GENERAL_ERR);
                    errMsg = bundle.getString("errMsg");
                }
            }

            StringBuilder msg = new StringBuilder();
            msg.append("File uploading fail, status:").append(errCode)
                    .append(", message:").append(errMsg);

            MessageDialog.show(actBrow, "File Uploading", msg.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.folder_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //while keep pressing on setting button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean bRtn = super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.mmuAbout:
                ActivityMenu.openOptionsDialog(actBrow);
                break;
            case R.id.mmuFCreate:
                folderCreateFunction(apiCfg.currentFolderId);
                break;
            case R.id.mmuUpload:
                fileUploadFunction(apiCfg.currentFolderId);
                break;
            case R.id.mmuExit:
                finish();
        }
        return bRtn;
    }

    private void setShareType(final FsInfo fi) {
        //
        final CharSequence[] items = getResources().getStringArray(
                R.array.share_func);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fi.display);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:// 設定分享
                        new ShareEntryTask(actBrow, apiCfg).execute(fi);
                        break;
                    case 1:// 設為群組分享
                        new SetAdvancedSharecodeTask(actBrow, apiCfg).execute(fi);
                        break;
                    case 2:// 取消分享
                        new ClearSetAdvancedSharecodeTask(actBrow, apiCfg)
                                .execute(fi);

                        break;
                }
            }
        }).create();

        builder.show();
    }
    //the function we need
    private void setShareTypeFIle(final FsInfo fi) {
        //
        final CharSequence[] items = getResources().getStringArray(
                R.array.share_func_file);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fi.display);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:// 設定分享

                        ShareEntryTask sh = new ShareEntryTask(actBrow, apiCfg);
                        sh.execute(fi);

                        break;
                    case 1:// 取消分享
                        new ClearSetAdvancedSharecodeTask(actBrow, apiCfg)
                                .execute(fi);

                        break;
                }
            }
        }).create();

        builder.show();
    }


    //connect to parse
    private void loginParse(String id)
    {
        queryParse(id);
    }
    private void queryParse(String id)
    {
        ParseQuery query = ParseQuery.getQuery("User");
        query.whereEqualTo("device_ID", id);
        query.whereEqualTo("ASUSAccount",apiCfg.userid);

        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (object == null) {
                    NewUser(android_id);
                    MessageDialog.show(actBrow,"Welcome to Litchi","It's your first time to use the system!");
                    MessageDialog.show(actBrow,"Back",android_id);


                } else {
                    setUser(android_id);
                    //MessageDialog.show(actBrow,"success",android_id);
                }
            }
        });

    }
    private void setUser(String id)
    {
        ASUSWebStorage.user.UserID=id;
        ASUSWebStorage.user.ASUSID=apiCfg.userid;
        ASUSWebStorage.user.ASUSpwd=apiCfg.password;


    }
    private void NewUser(String id)
    {
        ParseObject user = new ParseObject("User");
        user.put("device_ID",id);
        user.put("ASUSAccount",apiCfg.userid);
        user.put("password",apiCfg.password);
        user.saveInBackground();
        setUser(id);

    }

}
