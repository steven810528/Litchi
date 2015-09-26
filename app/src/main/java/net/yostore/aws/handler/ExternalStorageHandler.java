package net.yostore.aws.handler;

import java.io.File;

import com.ecareme.asuswebstorage.ASUSWebStorage;

import android.os.Environment;

public class ExternalStorageHandler {

	private static final String CAP_CAM = "/photo";
	private static final String CAP_VDO = "/video";
	private static final String CAP_ADO = "/audio";
	private static final String CAP_NOT = "/note";
	private static final String CAP_FIL = "/file";
//	private static final String CAP_FAM = "/family";
	private static final String CAP_MEM = "/family/memo";
	private static final String OPEN_CACHE = "/cache";
//	private static final String OPEN_DATA = "/data";
	private static final String OFFLINE_RECNTCHANGE = "/RecentChange";
//	private static final String SDPath =  Environment.getExternalStorageDirectory().getPath();
	
	public static String getSdRoot(){return getExternalApDataRoot("");}
//	public static String getSdRoot(){return SDPath;}
	public static String getCamRoot(){return getExternalApDataRoot(CAP_CAM);}
	public static String getVdoRoot(){return getExternalApDataRoot(CAP_VDO);}
	public static String getAdoRoot(){return getExternalApDataRoot(CAP_ADO);}
	public static String getNotRoot(){return getExternalApDataRoot(CAP_NOT);}
	public static String getFilRoot(){return getExternalApDataRoot(CAP_FIL);}
//	public static String getFamRoot(){return getRoot(CAP_FAM);}
	public static String getMamRoot(){return getExternalApDataRoot(CAP_MEM);}
	public static String getOpenCacheRoot(){return getExternalApDataRoot(OPEN_CACHE);}
	
	public static String getRecentChangeOfflineRoot(){return getOfflineRoot(OFFLINE_RECNTCHANGE);}
	
	private static String getExternalApDataRoot(String value)
	{
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			if ( ASUSWebStorage.cacheRoot != null )
			{
				if ( !ASUSWebStorage.cacheRoot.exists() )
				{
					ASUSWebStorage.cacheRoot.mkdirs();
				}
				File rtn = new File(ASUSWebStorage.cacheRoot.getParent(), value);
				if( !rtn.exists() )
					rtn.mkdirs();
				return rtn.getAbsolutePath();
			}
			else
			{
				File rtn = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/asuswebstorage" + value);
				if ( !rtn.exists() )
					rtn.mkdirs();
				return rtn.getAbsolutePath();
//				return Environment.getExternalStorageDirectory().getAbsolutePath()+"/asuswebstorage" + value;
			}
		}
		else
		{
			return null;
		}
	}
	
	private static String getOfflineRoot(String value)
	{
		if ( Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) )
		{
			File rtn = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/asuswebstorage/offline" + value);
			if( !rtn.exists() )
				rtn.mkdirs();
			return rtn.getAbsolutePath();
//			return Environment.getExternalStorageDirectory().getAbsolutePath()+"/asuswebstorage/offline" + value;
		}
		else
		{
			return null;
		}
	}
}
