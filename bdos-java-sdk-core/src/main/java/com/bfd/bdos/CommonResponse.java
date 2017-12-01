package com.bfd.bdos;

import com.bfd.bdos.exceptions.ClientException;
import com.bfd.bdos.exceptions.ServerException;
import com.bfd.bdos.http.HttpResponse;

public class CommonResponse extends BaseHttpResponse {
    
    private String data;
    
    private int httpStatus;
    
    private HttpResponse httpResponse;

    @Override
    public BaseHttpResponse getInstance(UnmarshallerContext context) throws ClientException, ServerException {
        this.setData(context.getData());
        this.setHttpResponse(context.getHttpResponse());
        this.setHttpStatus(context.getHttpStatus());
        return this;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

}
