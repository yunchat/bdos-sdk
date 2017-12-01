/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.bfd.bdos;

import com.bfd.bdos.auth.BdosCredentials;
import com.bfd.bdos.auth.BdosSessionCredentials;
import com.bfd.bdos.auth.BdosSessionCredentialsProvider;
import com.bfd.bdos.auth.ICredentialProvider;
import com.bfd.bdos.auth.Signer;
import com.bfd.bdos.exceptions.ClientException;
import com.bfd.bdos.exceptions.ServerException;
import com.bfd.bdos.http.FormatType;
import com.bfd.bdos.http.HttpRequest;
import com.bfd.bdos.http.HttpResponse;
import com.bfd.bdos.reader.Reader;
import com.bfd.bdos.reader.ReaderFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public class DefaultBdosClient  {
    private int maxRetryNumber = 3;
    private boolean autoRetry = true;
    private BdosCredentials account = null;
    
    private ICredentialProvider credentialProvider = null;

    public DefaultBdosClient() {

    }
    
    public DefaultBdosClient(String accesskeyId, String secret) {
    	credentialProvider = new BdosSessionCredentialsProvider(accesskeyId, secret);
    }
     

//    public DefaultAcsClient(BdosCredentials account) {
//    	this.account = account;
//    	credentialProvider = new BdosSessionCredentialsProvider(account.getAccessKeyId(), account.getAccessKeySecret());
//    }
    
    
    public DefaultBdosClient(ICredentialProvider provider) {
    	this.credentialProvider = provider;
    }
    
    


    public <T extends BaseHttpResponse> HttpResponse doAction(BaseHttpRequest<T> request)
        throws ClientException, ServerException {
    	this.account = credentialProvider.getCredentials();
        return this.doAction(request, autoRetry, maxRetryNumber, this.account);
    }

    public <T extends BaseHttpResponse> HttpResponse doAction(BaseHttpRequest<T> request,
                                                         boolean autoRetry, int maxRetryCounts)
        throws ClientException, ServerException {
    	this.account = credentialProvider.getCredentials();
        return this.doAction(request, autoRetry, maxRetryCounts, this.account);
    }


    public <T extends BaseHttpResponse> HttpResponse doAction(BaseHttpRequest<T> request,  BdosCredentials credential)
        throws ClientException, ServerException {
        boolean retry = this.autoRetry;
        int retryNumber = this.maxRetryNumber;
        return this.doAction(request, retry, retryNumber, credential);
    }

    public <T extends BaseHttpResponse> T getAcsResponse(BaseHttpRequest<T> request)
        throws ServerException, ClientException {
        HttpResponse baseResponse = this.doAction(request);
        return parseAcsResponse(request.getResponseClass(), baseResponse);
    }

    public <T extends BaseHttpResponse> T getAcsResponse(BaseHttpRequest<T> request,
                                                    boolean autoRetry, int maxRetryCounts)
        throws ServerException, ClientException {
        HttpResponse baseResponse = this.doAction(request, autoRetry, maxRetryCounts);
        return parseAcsResponse(request.getResponseClass(), baseResponse);
    }

    public <T extends BaseHttpResponse> T getAcsResponse(BaseHttpRequest<T> request, BdosCredentials credential)
        throws ServerException, ClientException {
        HttpResponse baseResponse = this.doAction(request, credential);
        return parseAcsResponse(request.getResponseClass(), baseResponse);
    }

    
//    public CommonResponse getCommonResponse(CommonRequest request) 
//            throws ServerException, ClientException{
//        HttpResponse baseResponse = this.doAction(request.buildRequest());
//        String stringContent = getResponseContent(baseResponse);
//        CommonResponse response = new CommonResponse();
//        response.setData(stringContent);
//        response.setHttpStatus(baseResponse.getStatus());
//        response.setHttpResponse(baseResponse);
//        
//        return response;
//    }

    public <T extends BaseHttpResponse> HttpResponse doAction(BaseHttpRequest<T> request, boolean autoRetry,
                                                         int maxRetryCounts, BdosCredentials credentials)
        throws ClientException, ServerException {
        if (null == credentials) {
            throw new ClientException("SDK.InvalidProfile", "No active profile found.");
        }
        boolean retry = autoRetry;
        int retryNumber = maxRetryCounts;

        Signer signer = Signer.getSigner(credentials);
        FormatType format = request.getAcceptFormat();

        return this.doAction(request, retry, retryNumber, credentials, signer, format);
    }

    private <T extends BaseHttpResponse> T parseAcsResponse(Class<T> clasz, HttpResponse baseResponse)
        throws ServerException, ClientException {

        FormatType format = baseResponse.getHttpContentType();

        if (baseResponse.isSuccess()) {
            return readResponse(clasz, baseResponse, format);
        } else {
            BaseError error = apiError(baseResponse, format);
            if (500 <= baseResponse.getStatus()) {
                throw new ServerException(error.getErrorCode(), error.getErrorMessage(), error.getRequestId());
            } else {
                throw new ClientException(error.getErrorCode(), error.getErrorMessage(), error.getRequestId());
            }
        }
    }



    private  <T extends BaseHttpResponse> HttpResponse doAction(BaseHttpRequest<T> request,
                                                           boolean autoRetry, int maxRetryNumber,
                                                           BdosCredentials credentials,
                                                           Signer signer, FormatType format)
        throws ClientException, ServerException {

        try {
            FormatType requestFormatType = request.getAcceptFormat();
            if (null != requestFormatType) {
                format = requestFormatType;
            }
            
            
            if(credentials instanceof BdosSessionCredentials) {
            	request.putQueryParameter("access_token", ((BdosSessionCredentials) credentials).getSessionToken());
            }
            
//            ProductDomain domain = null;
//            if (request.getProductDomain() != null) {
//                domain = request.getProductDomain();
//            } else {
//                domain = Endpoint.findProductDomain(regionId, request.getProduct(), endpoints);
//            }
//            if (null == domain) {
//                throw new ClientException("SDK.InvalidRegionId", "Can not find endpoint to access.");
//            }

            boolean shouldRetry = true;
            for (int retryTimes = 0; shouldRetry; retryTimes ++) {

                shouldRetry = autoRetry && retryTimes < maxRetryNumber;

                HttpRequest httpRequest = request.signRequest(signer, credentials, format);

                HttpResponse response;
                response = HttpResponse.getResponse(httpRequest);
                if (response.getHttpContent() == null) {
                    if (shouldRetry) {
                        continue;
                    } else {
                        throw new ClientException("SDK.ConnectionReset", "Connection reset.");
                    }
                }

                if (500 <= response.getStatus() && shouldRetry) {
                    continue;
                }

                return response;
            }

        } catch (InvalidKeyException exp) {
            throw new ClientException("SDK.InvalidAccessSecret", "Speicified access secret is not valid.");
        } catch (SocketTimeoutException exp) {
            throw new ClientException("SDK.ServerUnreachable",
                "SocketTimeoutException has occurred on a socket read or accept.");
        } catch (IOException exp) {
            throw new ClientException("SDK.ServerUnreachable", "Server unreachable: " + exp.toString());
        } catch (NoSuchAlgorithmException exp) {
            throw new ClientException("SDK.InvalidMD5Algorithm", "MD5 hash is not supported by client side.");
        }

        return null;
    }

    private <T extends BaseHttpResponse> T readResponse(Class<T> clasz, HttpResponse httpResponse, FormatType format)
        throws ClientException {
//        Reader reader = ReaderFactory.createInstance(format);
        UnmarshallerContext context = new UnmarshallerContext();
        T response = null;
        String stringContent = getResponseContent(httpResponse);
        try {
            response = clasz.newInstance();
        } catch (Exception e) {
            throw new ClientException("SDK.InvalidResponseClass", "Unable to allocate " + clasz.getName() + " class");
        }
        
//        String responseEndpoint = clasz.getName().substring(clasz.getName().lastIndexOf(".") + 1);

        
//        if (response.checkShowJsonItemName()) {
//            context.setResponseMap(reader.read(stringContent, responseEndpoint));
//        } else {
//            context.setResponseMap(reader.readForHideArrayItem(stringContent, responseEndpoint)); 
//        }
        
        context.setData(stringContent);
        context.setHttpStatus(httpResponse.getStatus());
        context.setHttpResponse(httpResponse);
        response.getInstance(context);
        return response;
    }

    private String getResponseContent(HttpResponse httpResponse) throws ClientException {
        String stringContent = null;
        try {
            if (null == httpResponse.getEncoding()) {
                stringContent = new String(httpResponse.getHttpContent());
            } else {
                stringContent = new String(httpResponse.getHttpContent(), httpResponse.getEncoding());
            }
        } catch (UnsupportedEncodingException exp) {
            throw new ClientException("SDK.UnsupportedEncoding",
                "Can not parse response due to un supported encoding.");
        }
        return stringContent;
    }
    
    private BaseError apiError(HttpResponse httpResponse, FormatType format) throws ClientException {
        BaseError error = new BaseError();
        String stringContent = getResponseContent(httpResponse);
        
        error.setErrorMessage(stringContent);
        error.setStatusCode(httpResponse.getStatus());
        return error;
    }

    private BaseError readError(HttpResponse httpResponse, FormatType format) throws ClientException {
        BaseError error = new BaseError();
        String responseEndpoint = "Error";
        Reader reader = ReaderFactory.createInstance(format);
        UnmarshallerContext context = new UnmarshallerContext();
        String stringContent = getResponseContent(httpResponse);
        context.setResponseMap(reader.read(stringContent, responseEndpoint));
        return error.getInstance(context);
    }

    public boolean isAutoRetry() {
        return autoRetry;
    }

    public void setAutoRetry(boolean autoRetry) {
        this.autoRetry = autoRetry;
    }

    public int getMaxRetryNumber() {
        return maxRetryNumber;
    }

    public void setMaxRetryNumber(int maxRetryNumber) {
        this.maxRetryNumber = maxRetryNumber;
    }

}
