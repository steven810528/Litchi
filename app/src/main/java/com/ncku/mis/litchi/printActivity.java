package com.ncku.mis.litchi;

/**
 * Created by steven on 2015/2/19.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ecareme.asuswebstorage.ASUSWebStorage;
import com.parse.Parse;
import com.parse.ParseObject;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.dialog.MessageDialog;


public class printActivity extends Activity
{
    //	private static final String TAG = "FilePreviewActivity";
//	private Activity  actPreview;
    private ApiConfig apiCfg;

    private long 	  fileId   = -999;
    private String	  fileName = "";
    private int 	  fileType = 0;

    private TextView  mPath;
    //private ImageView previewImg;
    //private TextView  previewText;
    private EditText numET;
    private int num;
    private String Url;

    private String parseUserID;
    private ParseObject  taskObject;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.s_print);
        taskObject = new ParseObject("Task");


        //Parse.enableLocalDatastore(this);
        Parse.initialize(this, "9SEi5AMIUibTgWPBZAAFKxvw2DIOSYv0LQFrCFHu", "fGQhe7GTXIoO3CYAIhfesUdr87YWZ1Ezdu5wSsOQ");

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
                Url  = bundle.getString("url");
                //MessageDialog.show(this, "URL", Url+fileName);
            }
        }

        mPath		= (TextView)  findViewById(R.id.mPath);
        //previewImg  = (ImageView) findViewById(R.id.previewImg);
        //previewText = (TextView)  findViewById(R.id.previewText);
        numET         = (EditText)  findViewById(R.id.num_a);

        // Change title to show file name
        if ( fileName.trim().length() != 0 )
            mPath.setText(fileName);

    }
    public void buttonClick(View v)
    {
        try {
            //this.num = Integer.parseInt("2");
            String st = this.numET.getText().toString();
            this.num = Integer.parseInt(st);

        }
        catch (Exception ex)
        {
            MessageDialog.show(this, "WARNING", ex.toString());
        }

        if(checkValue()==true) {
            saveParse();
            String st = "The file name is "+ this.fileName + "\nThe file is saved in the "+Url;
            st+= "\nThe number you want to print is "+num;

            MessageDialog.show(this,"Success","The task has been save int the Cloud!/\n"+st);

        }
    }
    private void saveParse()
    {
        if(checkValue()==true) {
            taskObject.put("UserID", apiCfg.userid);
            //taskObject.put("ASUSAccount",ASUSUser);
            taskObject.put("num", this.num);
            taskObject.put("fileName", this.fileName);
            taskObject.put("fileURL", this.Url);
            taskObject.saveInBackground();
        }

            //MessageDialog.show(this,"Error","The task was ");
    }
    private boolean checkValue()
    {
        if (this.fileName==null || this.Url == null ||this.num<=0)
        {
            MessageDialog.show(this,"Error","Please double check the value");
            return false;
        }
        return true;
    }

}