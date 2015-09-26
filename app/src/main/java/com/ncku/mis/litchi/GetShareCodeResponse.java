package com.ncku.mis.litchi;

import net.yostore.aws.api.entity.ApiResponse;

public class GetShareCodeResponse extends ApiResponse {

	private String _scrip;
	public String getScrip(){ return this._scrip; }
	public void setScrip(String value){ this._scrip = value; }

	public void setUri(String _uri) {
		this._uri = _uri;
	}
	public String getUri() {
		return _uri;
	}

	public void setIspasswordneeded(String _ispasswordneeded) {
		this._ispasswordneeded = _ispasswordneeded;
	}
	public String getIspasswordneeded() {
		return _ispasswordneeded;
	}

	private String _uri;
	private String _ispasswordneeded;
	
	
}// end class 
