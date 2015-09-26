package com.ncku.mis.litchi;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.api.InfoRelayApi;
import net.yostore.aws.api.entity.ApiResponse;
import net.yostore.aws.api.helper.BaseHelper;
import net.yostore.aws.api.entity.GetShareCodeRequest;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

//import net.yostore.aws.api.entity.GetShareCodeRequest;

public class GetShareCodeHelper extends BaseHelper {

	private String entrytype;
	private String entryid;

	public GetShareCodeHelper(String entrytype, String entryid){
		this.entrytype = entrytype;
		this.entryid = entryid;
	}
	
	@Override
	protected ApiResponse doApi(ApiConfig apicfg) throws MalformedURLException, ProtocolException,IOException, SAXException {
		
		GetShareCodeRequest request = new GetShareCodeRequest(
				apicfg.token,
				apicfg.userid,
				this.entrytype,
				this.entryid,
				null,
				"0"
		);
		
		InfoRelayApi ir = new InfoRelayApi(apicfg.infoRelay);
		return ir.getShareCode(request);
		
	}
}
