package com.ncku.mis.litchi;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;

public class GetShareCodeRequest {
	public GetShareCodeRequest(){}
	public GetShareCodeRequest(String token, String userid, String entrytype, String entryid, String password, String actiontype){
		this._token = token;
		this._userid = userid;
		this._entrytype = entrytype;
		this._entryid = entryid;
		this._password = password;
		this._actiontype = actiontype;
	}
	
	
	private String _token;
	public String getToken(){ return this._token; }
	public void setToken(String value){ this._token = value; }

	private String _scrip=String.valueOf(System.currentTimeMillis());
	public String getScrip(){ return this._scrip; }
	public void setScrip(String value){ this._scrip = value; }

	private String _entryid;
	public String getEntryid(){ return this._entryid; }
	public void setEntryid(String value){ this._entryid = value; }

	private String _userid;
	public String getUserId(){ return this._userid; }
	public void setUserId(String value){ this._userid = value; }

	private String _password;
	public String getPassword(){ return this._password; }
	public void setPassword(String value){ this._password = value; }

	private String _entrytype;
	public String getEntryType(){ return this._entrytype; }
	public void setEntryType(String value){ this._entrytype = value; }

	private String _actiontype;
	public String getActionType(){ return this._actiontype; }
	public void setActionType(String value){ this._actiontype = value; }
	
	public String toXml()
	{
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try 
		{
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);

			serializer.startTag("", "setadvancedsharecode");

			serializer.startTag("", "token");
			serializer.text(this._token);
			serializer.endTag("", "token");

			serializer.startTag("", "userid");
			serializer.text(this._userid);
			serializer.endTag("", "userid");

			serializer.startTag("", "isfolder");
			serializer.text("1");
			serializer.endTag("", "isfolder");
			
			serializer.startTag("", "entryid");
			serializer.text(this._entryid);
			serializer.endTag("", "entryid");
			
			serializer.endTag("", "setadvancedsharecode");	

			

			serializer.endDocument();
			return writer.toString();

			

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	

/*
	public String toXml(){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "getsharecode");

			serializer.startTag("", "token");
			serializer.text(this._token);
			serializer.endTag("", "token");

			serializer.startTag("", "scrip");
			serializer.text(this._scrip);
			serializer.endTag("", "scrip");

			serializer.startTag("", "userid");
			serializer.text(this._userid);
			serializer.endTag("", "userid");
			
			if (this._password!=null && this._password.trim().length()>0){
				serializer.startTag("", "password");
				serializer.text(this._password);
				serializer.endTag("", "password");				
			}
			
			serializer.startTag("", "entrytype");
			serializer.text(this._entrytype);
			serializer.endTag("", "entrytype");
			serializer.startTag("", "entryid");
			serializer.text(this._entryid);
			serializer.endTag("", "entryid");
			serializer.startTag("", "actiontype");
			serializer.text(this._actiontype);
			serializer.endTag("", "actiontype");
			serializer.endTag("", "getsharecode");
			serializer.endDocument();
			return writer.toString();
//			return "?xml=" + URLEncoder.encode(writer.toString());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}



	}*/
}// end class 
