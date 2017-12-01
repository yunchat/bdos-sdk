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

package com.bfd.bdos.auth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.bfd.bdos.endpoint.ApiEndPoint;
import com.bfd.bdos.exceptions.ClientException;
import com.bfd.bdos.http.HttpRequest;
import com.bfd.bdos.http.HttpResponse;
import com.bfd.bdos.http.MethodType;
import com.bfd.org.json.JSONException;
import com.bfd.org.json.JSONObject;


public class BdosSessionCredentialsFetcher {
	
    private static final int DEFAULT_TIMEOUT_IN_MILLISECONDS = 5000;
//    private String roleName;
//    private String metadataServiceHost = "100.100.100.200";
    private int connectionTimeoutInMilliseconds;
    private static final String FETCH_ERROR_MSG ="Failed to get credentials from auth service.";
    private static final int DEFAULT_SESSION_TOKEN_DURATION_SECONDS = 3600 * 6;
    
    
    private static final String CREDENTIAL_PRODUCT = "auth";
    
    private static final String CREDENTIAL_ACTION_TOKEN = "/api-cloud-platform/oauth2/token";
   
    private final String  accessKeyId;
    private final String  accessKeySecret;
    private String refreshToken;
    		

    public BdosSessionCredentialsFetcher(String accessKeyId, String accessKeySecret) {
    	this.accessKeyId = accessKeyId;
    	this.accessKeySecret = accessKeySecret;
        this.connectionTimeoutInMilliseconds = DEFAULT_TIMEOUT_IN_MILLISECONDS;
    }

//    public void setRoleName(String roleName) {
//        if (null == roleName) {
//            throw new NullPointerException("You must specifiy a valid role name.");
//        }
//        this.roleName = roleName;
//        setCredentialUrl();
//    }

//    private void setCredentialUrl() {
//        try {
//            this.credentialUrl = new URL("http://" + metadataServiceHost + URL_IN_ECS_METADATA + roleName);
//        } catch (MalformedURLException e) {
//            throw new IllegalArgumentException(e.toString());
//        }
//    }

//    public BdosCredentialsFetcher withECSMetadataServiceHost(String host) {
//        System.err.println("withECSMetadataServiceHost() method is only for testing, please don't use it");
//        this.metadataServiceHost = host;
//        setCredentialUrl();
//        return this;
//    }

    public BdosSessionCredentialsFetcher withConnectionTimeout(int milliseconds) {
        this.connectionTimeoutInMilliseconds = milliseconds;
        return this;
    }
    
    public String getMetadata() throws ClientException {
    	


        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("grant_type", "client_credentials");
        queryParameters.put("client_id", accessKeyId);
        queryParameters.put("client_secret", accessKeySecret);
        
    	String api = ApiEndPoint.getEndPoint(CREDENTIAL_PRODUCT, CREDENTIAL_ACTION_TOKEN);
        String url = this.composeUrl(api, queryParameters);
        url = "http://" + url;
        HttpRequest request = new HttpRequest(url);
        request.setMethod(MethodType.POST);
        request.setConnectTimeout(connectionTimeoutInMilliseconds);
        request.setReadTimeout(connectionTimeoutInMilliseconds);

        HttpResponse response;

        try {
            response = HttpResponse.getResponse(request);
        } catch (IOException e) {
            throw new ClientException("Failed to connect Auth Service: " + e.toString());
        }

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new ClientException(FETCH_ERROR_MSG + " HttpCode=" + response.getStatus());
        }

        return new String(response.getHttpContent());
    }

//    public String getMetadata() throws ClientException {
//
//    	CommonRpcRequest request = new CommonRpcRequest("auth");
//    	request.setMethod(MethodType.POST);
//    	request.setActionName("token");
//    	request.putQueryParameter("grant_type", "client_credentials");
//    	request.putQueryParameter("client_id", accessKeyId);
//    	request.putQueryParameter("client_secret", accessKeySecret);
//    	
//    	String url = ApiEndPoint.getEndPoint("auth", "oauth2/token");
//    	
//    	url = this.composeUrl(this.getMetadata(), queries);
//    	
//    	
//    	DefaultAcsClient client = new DefaultAcsClient(account);
//    	CommonResponse response  = null;
//
//        try {
//        	response = client.getAcsResponse(request);
////        } catch (IOException e) {
////            throw new ClientException("Failed to connect Auth Service: " + e.toString());
//        } catch (ServerException e) {
//        	throw e;
//        } catch (ClientException e) {
//        	throw e;
//        }
//
//        if (response.getHttpStatus() != HttpURLConnection.HTTP_OK) {
//            throw new ClientException(FETCH_ERROR_MSG + " HttpCode=" + response.getHttpStatus());
//        }
//
//        return response.getData();
//    }

    public BdosSessionCredentials fetch() throws ClientException {
        String jsonContent = getMetadata();
        JSONObject obj;
        try {
            obj = new JSONObject(jsonContent);
        } catch (JSONException e) {
            throw new ClientException(FETCH_ERROR_MSG + " Reason: " + e.toString());
        }

        if (obj.has("access_token") &&
            obj.has("refresh_token") &&
            obj.has("expires_in")) {

        } else {
            throw new ClientException("Invalid json got from auth service.");
        }
//        
//        if (!"Success".equals(obj.getString("Code"))) {
//            throw new ClientException(FETCH_ERROR_MSG);
//        }
        //{"expires_in":2801,"refresh_token":"4c99d3654e3275ca80222281d131eb04","access_token":"7d33c3501b57a53dd929ef5f78b3515a"}
        refreshToken = obj.getString("refresh_token");
        return new BdosSessionCredentials(
        		this.accessKeyId,
        		this.accessKeySecret,
                obj.getString("access_token"),
                obj.getString("refresh_token"),
                obj.getLong("expires_in"),
                DEFAULT_SESSION_TOKEN_DURATION_SECONDS
            );
    }
    
    public String refreshMetadata() throws ClientException {
    	


        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("grant_type", "refresh_token");
        queryParameters.put("client_id", accessKeyId);
        queryParameters.put("client_secret", accessKeySecret);
        queryParameters.put("refresh_token", refreshToken);
        
    	String api = ApiEndPoint.getEndPoint(CREDENTIAL_PRODUCT, CREDENTIAL_ACTION_TOKEN);
        String url = this.composeUrl(api, queryParameters);
        url = "http://" + url;
        HttpRequest request = new HttpRequest(url);
        request.setMethod(MethodType.POST);
        request.setConnectTimeout(connectionTimeoutInMilliseconds);
        request.setReadTimeout(connectionTimeoutInMilliseconds);

        HttpResponse response;

        try {
            response = HttpResponse.getResponse(request);
        } catch (IOException e) {
            throw new ClientException("Failed to connect Auth Service: " + e.toString());
        }

        if (response.getStatus() != HttpURLConnection.HTTP_OK) {
            throw new ClientException(FETCH_ERROR_MSG + " HttpCode=" + response.getStatus());
        }

        return new String(response.getHttpContent());
    }
    
    public BdosSessionCredentials refresh() throws ClientException {
    	//{"expires_in":3600,"refresh_token":"09e2eb1dd8a17cdcf57918469cdc8e4a","access_token":"5fd08a0b7ea477e399e3701a54c8d01b"}
        String jsonContent = getMetadata();
        JSONObject obj;
        try {
            obj = new JSONObject(jsonContent);
        } catch (JSONException e) {
            throw new ClientException(FETCH_ERROR_MSG + " Reason: " + e.toString());
        }

        if (obj.has("access_token") &&
            obj.has("refresh_token") &&
            obj.has("expires_in")) {

        } else {
            throw new ClientException("Invalid json got from auth service.");
        }
        
//        if (!"Success".equals(obj.getString("Code"))) {
//            throw new ClientException(FETCH_ERROR_MSG);
//        }
        //{"expires_in":2801,"refresh_token":"4c99d3654e3275ca80222281d131eb04","access_token":"7d33c3501b57a53dd929ef5f78b3515a"}
//        refresh_token = 
        return new BdosSessionCredentials(
        		this.accessKeyId,
        		this.accessKeySecret,
                obj.getString("access_token"),
                obj.getString("refresh_token"),
                obj.getLong("expires_in"),
                DEFAULT_SESSION_TOKEN_DURATION_SECONDS
            );
    }
    

    public String composeUrl(String endPoint, Map<String, String> queries)  {
        StringBuilder urlBuilder = new StringBuilder(endPoint);
        if (-1 == urlBuilder.indexOf("?")) {
            urlBuilder.append("?");
        } else if (!urlBuilder.toString().endsWith("?")) {
            urlBuilder.append("&");
        }
        String query = concatQueryString(queries);
        String url = urlBuilder.append(query).toString();
        if (url.endsWith("?") || url.endsWith("&")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
    
    
    public String concatQueryString(Map<String, String> parameters) {
            if (null == parameters) { return null; }

            StringBuilder urlBuilder = new StringBuilder("");
            for (Entry<String, String> entry : parameters.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                urlBuilder.append(key);
                if (val != null) {
                    urlBuilder.append("=").append(val);
                }
                urlBuilder.append("&");
            }

            int strIndex = urlBuilder.length();
            if (parameters.size() > 0) { urlBuilder.deleteCharAt(strIndex - 1); }

            return urlBuilder.toString();
    }

    public BdosSessionCredentials fetch(int retryTimes) throws ClientException {
        for (int i = 0; i <= retryTimes; i ++) {
            try {
                return fetch();
            } catch (ClientException e) {
                if (i == retryTimes) {
                    throw e;
                }
            }
        }
        throw new ClientException("Failed to connect Auth Service: Max retry times exceeded.");
    }
}