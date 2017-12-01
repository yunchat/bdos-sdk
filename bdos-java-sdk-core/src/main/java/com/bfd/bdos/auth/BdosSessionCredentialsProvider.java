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

import com.bfd.bdos.exceptions.ClientException;

/**
 * 
 * @author yunchat
 *
 */
public class BdosSessionCredentialsProvider implements ICredentialProvider {

    /**
     * Default duration for started sessions.
     */
    private BdosSessionCredentials credentials = null;
    public int ecsMetadataServiceFetchCount = 0;
    private BdosSessionCredentialsFetcher fetcher;
    private static final int MAX_ECS_METADATA_FETCH_RETRY_TIMES = 3;
    private int maxRetryTimes = MAX_ECS_METADATA_FETCH_RETRY_TIMES;

    public BdosSessionCredentialsProvider(String accessKeyId, String accessKeySecret) {
        if (null == accessKeyId) {
            throw new NullPointerException("You must specifiy a access client id.");
        }
        if (null == accessKeySecret) {
            throw new NullPointerException("You must specifiy a access key secret.");
        }
        this.fetcher = new BdosSessionCredentialsFetcher(accessKeyId, accessKeySecret);
    }

    public BdosSessionCredentialsProvider withFetcher(BdosSessionCredentialsFetcher fetcher) {
        this.fetcher = fetcher;
        return this;
    }

    @Override
    public BdosCredentials getCredentials() throws ClientException {
        if (credentials == null) {
            ecsMetadataServiceFetchCount += 1;
            credentials = fetcher.fetch(maxRetryTimes);
        } else if (credentials.isExpired()) {
            //throw new ClientException("SDK.SessionTokenExpired", "Current session token has expired.");
            ecsMetadataServiceFetchCount += 1;
            credentials = fetcher.fetch(maxRetryTimes);
        } else if (credentials.willSoonExpire() && credentials.shouldRefresh()) {
            try {
                ecsMetadataServiceFetchCount += 1;
                credentials = fetcher.refresh();
            } catch (ClientException e) {
                // Use the current expiring session token and wait for next round
                credentials.setLastFailedRefreshTime();
            }
        }
        System.out.println(credentials.toString());
        return credentials;
    }
}