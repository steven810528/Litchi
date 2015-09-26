package net.yostore.aws.entity;

import net.yostore.aws.api.entity.Attribute;
import net.yostore.aws.api.entity.B_FileInfo;
import net.yostore.aws.api.entity.B_FolderInfo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FsInfo
{
	public static enum EntryType
	{
		File(0),
		Folder(1),
    	NUll(5),
    	Process(7);

		EntryType(int keyId)
		{
			this.key_id = keyId;
		}
        private final int key_id;

        public int getInt() {
            return key_id;
        }
        
        public String getString() {
            return String.valueOf(key_id);
        }
	};
	
	private final DateFormat createTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	private final DateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private final DateFormat attTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public EntryType entryType = EntryType.File;
	public String 	 entryId;
	public String 	 display; 
	public String 	 parent;
	public Attribute attribute;	
	public Long 	 size = null;
	public int 		 icon = 0;
	
	public FsInfo(){}
	
	public FsInfo(EntryType entryType, String display)
	{
		this.entryType = entryType;
		this.display = display;
	}
	
	public FsInfo(B_FolderInfo fi)
	{
		this.entryId   = fi.getId();
		this.display   = fi.getDisplay();
		
		this.entryType = EntryType.Folder;

		//this.icon = getBrowseRawIcon(this.display, this.entryType);
		parseAttribute();
	}
	
	public FsInfo(B_FileInfo fi)
	{
		this.entryId   = fi.getId();
		this.display   = fi.getDisplay();
		

		this.entryType = EntryType.File;
		
		this.size = fi.getSize();
		//this.icon = getBrowseRawIcon(this.display, this.entryType);
		parseAttribute();
	}

	private final long KB = 1024L;
	private final long MB = 1024 * 1024L;
	private final long GB = 1024 * 1024 * 1024L;
	private final long TB = 1024 * 1024 * 1024 * 1024L;
	public String getSizeDisp()
	{
		if (size > TB) return String.valueOf(Math.round(size/TB)) + " TB";
		else if(size > GB) return String.valueOf(Math.round(size/GB)) + " GB";
		else if(size > MB) return String.valueOf(Math.round(size/MB)) + " MB";
		else if(size > KB) return String.valueOf(Math.round(size/KB)) + " KB";
		else if(size >= 0) return String.valueOf(size) + " B";
		else return "";
	}
	/*
	public int getBrowseRawIcon(String disp, EntryType entryType)
	{
		int rtn = 0;
		if ( entryType==EntryType.Folder )
		{
			rtn = R.drawable.icon_list_folder;
		}
		else if ( entryType==EntryType.File )
		{
			String tp = FsInfo.parseFileType(disp);
			if ( tp.startsWith("audio/") )
				rtn = R.drawable.icon_list_music;
			else if ( tp.startsWith("video/") )
				rtn = R.drawable.icon_list_video;
			else if ( tp.startsWith("image/") )
				rtn = R.drawable.icon_list_photo;
			else if ( "application/pdf".equals(tp) )
				rtn = R.drawable.icon_list_pdf;
			else if ( "application/msword".equals(tp) )
				rtn = R.drawable.icon_list_doc;
			else if ( "application/vnd.ms-excel".equals(tp) )
				rtn = R.drawable.icon_list_excel;
			else if ( "application/vnd.ms-powerpoint".equals(tp) )
				rtn = R.drawable.icon_list_ppt;
			else if ( "text/*".equals(tp) )
				rtn = R.drawable.icon_list_txt;
			else if ( "application/zip".equals(tp) )
				rtn = R.drawable.icon_list_zip;
			else if ( "application/epub+zip".equals(tp) )
				rtn = R.drawable.icon_list_epub;
			else if ( "code".equals(tp) )
				rtn = R.drawable.icon_list_code;
			else
				rtn = R.drawable.icon_list_other;
		}
		return rtn;
	}
    */
	public static String parseFileType(String end)
	{
		end = end.substring(end.lastIndexOf(".") + 1, end.length()).toLowerCase();

		if ( end.equals("mp3") )
		{
			return "audio/mp3";
		}
		else if ( end.equals("m4a") || end.equals("mid") || end.equals("xmf") || end.equals("ogg") || end.equals("wav") || end.equals("amr") )
		{
			return "audio/*";
		}
		else if ( end.equals("avi") || end.equals("mp4") || end.equals("mpeg") || end.equals("mpg") || end.equals("m4v") || end.equals("mov")
				|| end.equals("mkv") || end.equals("vob") || end.equals("vcd") || end.equals("svcd") || end.equals("rm") || end.equals("rmvb")
				|| end.equals("divx") || end.equals("wmv") || end.equals("3gp") || end.equals("3gpp") || end.equals("flv") )
		{
			return "video/*";
		}
		else if ( end.equals("jpg") || end.equals("gif") || end.equals("png") || end.equals("jpeg") || end.equals("bmp") )
		{
			return "image/*";
		}
		else if ( end.equals("pdf") )
		{
			return "application/pdf";
		}
		else if ( end.equals("doc") || end.equals("docx") || end.equals("rtf") )
		{
			return "application/msword";
		}
		else if ( end.equals("xls") || end.equals("xlsx") )
		{
			return "application/vnd.ms-excel";
		}
		else if ( end.equals("ppt") || end.equals("pptx") )
		{
			return "application/vnd.ms-powerpoint";
		}
		else if ( end.equals("txt") || end.equals("odt") || end.equals("ods") || end.equals("odp") )
		{
			return "text/*";
		}
		else if ( end.equals("zip") || end.equals("rar") )
		{
			return "application/zip";
		}
		else if ( end.equals("epub") )
		{
			return "application/epub+zip";
		}
		else if ( end.equals("htm") || end.equals("html") || end.equals("xml") || end.equals("js") || end.equals("css") || end.equals("java")
				|| end.equals("aidl") || end.equals("vb") || end.equals("c") || end.equals("h") )
		{
			return "code";
		}
		else
		{
			return "*/*";
		}
	}
	
	
	private void parseAttribute()
	{
		long _nowDateTime = new Date().getTime();
		if ( attribute != null )
		{
			if ( attribute.getCreationtime() != null && attribute.getCreationtime().trim().length() > 0 )
			{
				try
				{
					Date d = attTimeFormat.parse(attribute.getCreationtime().trim());
					attribute.setCreationtime(String.valueOf((d.getTime()/1000)));
				}
				catch ( ParseException e1 )
				{
					long _d = Long.parseLong(attribute.getCreationtime().trim())*1000;
					if( _d > _nowDateTime*10 )
					{
						attribute.setCreationtime(String.valueOf((new Date(_d/1000000).getTime())));
					}
				}
			}
			if ( attribute.getLastaccesstime() != null && attribute.getLastaccesstime().trim().length() > 0 )
			{
				try
				{
					Date d = attTimeFormat.parse(attribute.getLastaccesstime().trim());
					attribute.setLastaccesstime(String.valueOf((d.getTime()/1000000)));
				}
				catch ( ParseException e1 )
				{
					long _d = Long.parseLong(attribute.getLastaccesstime().trim())*1000;
					if ( _d > _nowDateTime*10 )
					{
						attribute.setLastaccesstime(String.valueOf((new Date(_d/1000000).getTime())));
					}
				}
			}
			if ( attribute.getLastwritetime() != null && attribute.getLastwritetime().trim().length() > 0 )
			{
				try
				{
					Date d = attTimeFormat.parse(attribute.getLastwritetime().trim());
					attribute.setLastwritetime(String.valueOf((d.getTime()/1000000)));
				}
				catch ( ParseException e1 )
				{
					long _d = Long.parseLong(attribute.getLastwritetime().trim())*1000;
					if ( _d > _nowDateTime*10 ) 
					{
						attribute.setLastwritetime(String.valueOf((new Date(_d/1000000).getTime())));
					}
				}
			}
		}
	}
}
