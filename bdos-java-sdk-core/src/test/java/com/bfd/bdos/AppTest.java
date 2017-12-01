package com.bfd.bdos;

import java.net.HttpURLConnection;

import com.bfd.bdos.auth.BdosAccount;
import com.bfd.bdos.auth.BdosCredentials;
import com.bfd.bdos.http.HttpRequest;
import com.bfd.bdos.http.HttpResponse;
import com.bfd.bdos.http.MethodType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() throws Exception {
		HttpRequest req = new HttpRequest("http://www.baidu.com");
		req.setMethod(MethodType.GET);
		HttpResponse response = HttpResponse.getResponse(req);
		String strResult = new String(response.getHttpContent(), response.getEncoding());
		System.out.println(strResult);

	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp2() throws Exception {

		BdosCredentials account = new BdosAccount("actID", "appsecrect");
		CommonRpcRequest request = new CommonRpcRequest("aa", "a2");
		request.setMethod(MethodType.GET);
		DefaultBdosClient client = new DefaultBdosClient("aa", "ss");
		CommonResponse response = client.getAcsResponse(request);
		// String strResult = new String(response.getData(),
		// response.getEncoding());
		System.out.println(response.getData());

	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp3() throws Exception {

		BdosCredentials account = new BdosAccount("actID", "appsecrect");
		CommonRpcRequest request = new CommonRpcRequest("aa");
		request.setMethod(MethodType.GET);
		request.setActionName("a2");
		DefaultBdosClient client = new DefaultBdosClient("aa", "ss");
		CommonResponse response = client.getAcsResponse(request);
		// String strResult = new String(response.getData(),
		// response.getEncoding());
		System.out.println(response.getData());

		request.setActionName("token");
		response = client.getAcsResponse(request);
		System.out.println(response.getData());

	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp4() throws Exception {

		BdosCredentials account = new BdosAccount("32b5e8b9fe5c4942abba1c272a5e97b3",
				"3d95d146b18a40f9a58df076b562b388");
		CommonRpcRequest request = new CommonRpcRequest("aa");
		request.setMethod(MethodType.POST);
		request.setActionName("token");
		request.putQueryParameter("grant_type", "client_credentials");
		request.putQueryParameter("client_id", account.getAccessKeyId());
		request.putQueryParameter("client_secret", account.getAccessKeySecret());
		DefaultBdosClient client = new DefaultBdosClient("aa", "ss");

		CommonResponse response = client.getAcsResponse(request);
		System.out.println(response.getData());

	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp5() throws Exception {

		BdosCredentials account = new BdosAccount("32b5e8b9fe5c4942abba1c272a5e97b3",
				"3d95d146b18a40f9a58df076b562b388");
		CommonRpcRequest request = new CommonRpcRequest("aa");
		request.setMethod(MethodType.POST);
		request.setActionName("token");
		request.putQueryParameter("grant_type", "refresh_token");
		request.putQueryParameter("client_id", account.getAccessKeyId());
		request.putQueryParameter("client_secret", account.getAccessKeySecret());
		// {"expires_in":3600,"refresh_token":"09e2eb1dd8a17cdcf57918469cdc8e4a","access_token":"5fd08a0b7ea477e399e3701a54c8d01b"}
		request.putQueryParameter("refresh_token", "dbb325ae4da09ed2e8232708e4df20d8");
		DefaultBdosClient client = new DefaultBdosClient("aa", "ss");

		CommonResponse response = client.getAcsResponse(request);

		System.out.println(response.getData());

	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp6() throws Exception {

		DefaultBdosClient client = new DefaultBdosClient("32b5e8b9fe5c4942abba1c272a5e97b3",
				"3d95d146b18a40f9a58df076b562b388");
		CommonRpcRequest request = new CommonRpcRequest("aa");
		request.setActionName("/api/info/p_info3005");
		request.setMethod(MethodType.POST);
		request.putQueryParameter("@limit", 100);
		CommonResponse response = client.getAcsResponse(request);
		System.out.println(response.getData());

	}

}
