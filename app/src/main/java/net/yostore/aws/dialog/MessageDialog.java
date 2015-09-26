package net.yostore.aws.dialog;

import net.yostore.aws.menu.NoneListener;
import android.app.AlertDialog;
import android.content.Context;

public class MessageDialog
{
	public static void show(Context act, String title, String message)
	{
		new AlertDialog.Builder(act)
		   .setTitle(title).setMessage(message)
		   .setPositiveButton("OK", NoneListener.dialogClickListener).show();
	}
}
