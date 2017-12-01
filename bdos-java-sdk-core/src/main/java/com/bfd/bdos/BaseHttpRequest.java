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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import com.bfd.bdos.auth.BdosCredentials;
import com.bfd.bdos.auth.Signer;
import com.bfd.bdos.http.FormatType;
import com.bfd.bdos.http.HttpRequest;
import com.bfd.bdos.http.ProtocolType;
import com.bfd.bdos.utils.ParameterHelper;

public abstract class BaseHttpRequest<T extends BaseHttpResponse> extends HttpRequest {

    protected String uriPattern = null;
    private Map<String, String> pathParameters = new HashMap<String, String>();
    private String version = null;
    private String product = null;

	private String actionName = null;
    private String securityToken = null;
    private FormatType acceptFormat = null;
    
    private String endPoint;

	public FormatType getAcceptFormat() {
		return acceptFormat;
	}

	public void setAcceptFormat(FormatType acceptFormat) {
		this.acceptFormat = acceptFormat;
	}

	private ProtocolType protocol = ProtocolType.HTTP;
    private final Map<String, String> queryParameters = new HashMap<String, String>();
//  private final Map<String, String> domainParameters = new HashMap<String, String>();
    private final Map<String, String> bodyParameters = new HashMap<String, String>();
    
    

    public BaseHttpRequest(String product) {
        super(null);
        this.product = product;
//      initialize();
    }

    public BaseHttpRequest(String product, String action) {
        super(null);
        this.product = product;
        this.actionName = action;
        this.setVersion(version);
        
//        initialize();
    }

    public BaseHttpRequest(String product, String action, String version) {
        super(null);
        this.product = product;
        this.setVersion(version);
        this.setActionName(action);
//        initialize();
    }
    

    private void initialize() {
        this.setHttpContent(new byte[0], "utf-8", FormatType.RAW);
    }
    

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}
    
    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
    
    public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}
    

    public void setVersion(String version) {
        this.putHeaderParameter("x-acs-version", version);
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
        this.putQueryParameter("SecurityToken", securityToken);
        this.putHeaderParameter("x-acs-security-token", securityToken);
    }

    public Map<String, String> getPathParameters() {
        return Collections.unmodifiableMap(pathParameters);
    }

    protected void putPathParameter(String name, Object value) {
        setParameter(this.pathParameters, name, value);
    }

    protected void putPathParameter(String name, String value) {
        setParameter(this.pathParameters, name, value);
    }

    public String composeUrl(String endpoint, Map<String, String> queries) throws UnsupportedEncodingException {

        Map<String, String> mapQueries = (queries == null) ? this.getQueryParameters() : queries;
        StringBuilder urlBuilder = new StringBuilder("");
        urlBuilder.append(this.getProtocol().toString());
        urlBuilder.append("://").append(endpoint);
        if (null != this.uriPattern) {
            urlBuilder.append(replaceOccupiedParameters(uriPattern, this.getPathParameters()));
        }
        if (-1 == urlBuilder.indexOf("?")) {
            urlBuilder.append("?");
        } else if (!urlBuilder.toString().endsWith("?")) {
            urlBuilder.append("&");
        }
        String query = concatQueryString(mapQueries);
        String url = urlBuilder.append(query).toString();
        if (url.endsWith("?") || url.endsWith("&")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public void setUriPattern(String uriPattern) {
        this.uriPattern = uriPattern;
    }


    public HttpRequest signRequest(Signer signer, BdosCredentials credentials,
                                   FormatType format)
        throws InvalidKeyException, IllegalStateException, UnsupportedEncodingException, NoSuchAlgorithmException {

        Map<String, String> formParams = this.getBodyParameters();
        if (formParams != null && !formParams.isEmpty()) {
            byte[] data = ParameterHelper.getFormData(formParams);
            this.setHttpContent(data, "UTF-8", FormatType.FORM);
        }

        Map<String, String> imutableMap = new HashMap<String, String>(this.getHeaders());
        if (null != signer && null != credentials) {
            String accessKeyId = credentials.getAccessKeyId();
//            imutableMap = this.composer.refreshSignParameters(this.getHeaders(), signer, accessKeyId, format);
//            if (credentials instanceof BasicSessionCredentials) {
//                String sessionToken = ((BasicSessionCredentials)credentials).getSessionToken();
//                if (null != sessionToken) {
//                    imutableMap.put("x-acs-security-token", sessionToken);
//                }
//            } 
//            String strToSign = this.composer.composeStringToSign(this.getMethod(), this.getUriPattern(), signer,
//                this.getQueryParameters(), imutableMap, this.getPathParameters());
            
            String signature = signer.signString("ssss", credentials);
            imutableMap.put("Authorization", "acs " + accessKeyId + ":" + signature);
        }
        this.setUrl(this.composeUrl(this.getEndPoint(), this.getQueryParameters()));
//        this.headers = imutableMap;
        return this;
    }
    
//
//    @Override
//    public HttpRequest signRequest(Signer signer, AlibabaCloudCredentials credentials,
//                                   FormatType format, ProductDomain domain)
//        throws InvalidKeyException, IllegalStateException, UnsupportedEncodingException, NoSuchAlgorithmException {
//
//        Map<String, String> formParams = this.getBodyParameters();
//        if (formParams != null && !formParams.isEmpty()) {
//            byte[] data = ParameterHelper.getFormData(formParams);
//            this.setHttpContent(data, "UTF-8", FormatType.FORM);
//        }
//
//        Map<String, String> imutableMap = new HashMap<String, String>(this.getHeaders());
//        if (null != signer && null != credentials) {
//            String accessKeyId = credentials.getAccessKeyId();
//            imutableMap = this.composer.refreshSignParameters(this.getHeaders(), signer, accessKeyId, format);
//            if (credentials instanceof BasicSessionCredentials) {
//                String sessionToken = ((BasicSessionCredentials)credentials).getSessionToken();
//                if (null != sessionToken) {
//                    imutableMap.put("x-acs-security-token", sessionToken);
//                }
//            } 
//            String strToSign = this.composer.composeStringToSign(this.getMethod(), this.getUriPattern(), signer,
//                this.getQueryParameters(), imutableMap, this.getPathParameters());
//            String signature = signer.signString(strToSign, credentials);
//            imutableMap.put("Authorization", "acs " + accessKeyId + ":" + signature);
//        }
//        this.setUrl(this.composeUrl(domain.getDomianName(), this.getQueryParameters()));
//        this.headers = imutableMap;
//        return this;
//    }
    
    	public abstract Class<T> getResponseClass();
    

	    public ProtocolType getProtocol() {
	        return protocol;
	    }
	
	    public void setProtocol(ProtocolType protocol) {
	        this.protocol = protocol;
	    }
	
	    public Map<String, String> getQueryParameters() {
	        return Collections.unmodifiableMap(queryParameters);
	    }
	
	    public <K> void putQueryParameter(String name, K value) {
	        setParameter(this.queryParameters, name, value);
	    }
	
	    protected void putQueryParameter(String name, String value) {
	        setParameter(this.queryParameters, name, value);
	    }
	
	
	    public Map<String, String> getBodyParameters() {
	        return Collections.unmodifiableMap(bodyParameters);
	    }
	
	    protected void putBodyParameter(String name, Object value) {
	        setParameter(this.bodyParameters, name, value);
	    }
	
	    protected void setParameter(Map<String, String> map, String name, Object value) {
	        if (null == map || null == name || null == value) {
	            return;
	        }
	        map.put(name, String.valueOf(value));
	    }
	    
//	    public static String concatQueryString(Map<String, String> parameters)
//	            throws UnsupportedEncodingException {
//	            if (null == parameters) { return null; }
//
//	            StringBuilder urlBuilder = new StringBuilder("");
//	            for (Entry<String, String> entry : parameters.entrySet()) {
//	                String key = entry.getKey();
//	                String val = entry.getValue();
//	                urlBuilder.append(AcsURLEncoder.encode(key));
//	                if (val != null) {
//	                    urlBuilder.append("=").append(AcsURLEncoder.encode(val));
//	                }
//	                urlBuilder.append("&");
//	            }
//
//	            int strIndex = urlBuilder.length();
//	            if (parameters.size() > 0) { urlBuilder.deleteCharAt(strIndex - 1); }
//
//	            return urlBuilder.toString();
//	    }

}
