package com.bfd.bdos;

import com.bfd.bdos.endpoint.ApiEndPoint;
import com.bfd.bdos.http.FormatType;
import com.bfd.bdos.http.ProtocolType;

public class CommonRpcRequest extends BaseHttpRequest<CommonResponse> {

 
    public CommonRpcRequest(String product) {
        super(product);
//        this.setProtocol(ProtocolType.HTTP);
        setAcceptFormat(FormatType.JSON);
    }
    
    public CommonRpcRequest(String product, String action)  {
        super(product, action);
//        this.setProtocol(ProtocolType.HTTP);
        setAcceptFormat(FormatType.JSON);
    }


    @Override
    public Class<CommonResponse> getResponseClass() {
        return CommonResponse.class;
    }

    public String getEndPoint() {
    	System.out.println("enter into getEndPoint");
        return ApiEndPoint.getEndPoint(super.getProduct(), super.getActionName());
    }
    
}
