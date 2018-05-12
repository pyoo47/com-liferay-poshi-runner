/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.poshi.runner.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Michael Hashimoto
 */
public class RequestUtil {

	public static Map<String, String> addRequestHeaders(
		Map<String, String> requestHeaders, String header) {

		if (requestHeaders == null) {
			requestHeaders = new HashMap<>();
		}

		int i = header.indexOf("=");

		if (i == -1) {
			throw new IllegalArgumentException("Invalid header: " + header);
		}

		String key = header.substring(0, i);
		String value = header.substring(i + 1);

		requestHeaders.put(key, value);

		return requestHeaders;
	}

	public static HttpAuthorization getHttpAuthorization(
		String type, String value) {

		if (type.equals("basic")) {
			String[] tokens = value.split(":");

			return new BasicHttpAuthorization(tokens[0], tokens[1]);
		}
		else if (type.equals("token")) {
			return new TokenHttpAuthorization(value);
		}

		throw new IllegalArgumentException(
			"Unsupported authorization type: " + type);
	}

	public static String getResponseBody(HttpURLConnection httpURLConnection)
		throws IOException {

		StringBuilder sb = new StringBuilder();

		int bytes = 0;
		String line = null;

		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(httpURLConnection.getInputStream()))) {

			while ((line = bufferedReader.readLine()) != null) {
				byte[] lineBytes = line.getBytes();

				bytes += lineBytes.length;

				if (bytes > (30 * 1024 * 1024)) {
					sb.append("Response for was truncated due to its size.");

					break;
				}

				sb.append(line);
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String getStatusCode(HttpURLConnection httpURLConnection)
		throws IOException {

		return String.valueOf(httpURLConnection.getResponseCode());
	}

	public static HttpURLConnection request(
			HttpAuthorization httpAuthorizationHeader, Integer maxRetries,
			String requestBody, Map<String, String> requestHeaders,
			String requestMethod, Integer retryPeriod, Integer timeout,
			String url)
		throws IOException {

		url = _fixURL(url);

		int retryCount = 0;

		while (true) {
			try {
				URL urlObject = new URL(url);

				URLConnection urlConnection = urlObject.openConnection();

				if (urlConnection instanceof HttpURLConnection) {
					HttpURLConnection httpURLConnection =
						(HttpURLConnection)urlConnection;

					httpURLConnection.setRequestMethod(requestMethod);

					if (httpAuthorizationHeader != null) {
						httpURLConnection.setRequestProperty(
							"Authorization",
							httpAuthorizationHeader.toString());
					}

					if (requestHeaders != null) {
						for (Map.Entry<String, String> requestHeader :
								requestHeaders.entrySet()) {

							httpURLConnection.setRequestProperty(
								requestHeader.getKey(),
								requestHeader.getValue());
						}
					}

					if (requestBody != null) {
						if (requestMethod.equals("GET")) {
							throw new IllegalArgumentException(
								"Request method 'GET' cannot have a request " +
									"body");
						}

						httpURLConnection.setDoOutput(true);

						try (OutputStream outputStream =
								httpURLConnection.getOutputStream()) {

							outputStream.write(requestBody.getBytes("UTF-8"));

							outputStream.flush();
						}
					}
				}

				return (HttpURLConnection)urlConnection;
			}
			catch (IOException ioe) {
				retryCount++;

				if ((maxRetries >= 0) && (retryCount >= maxRetries)) {
					throw ioe;
				}

				System.out.println(
					"Retrying " + url + " in " + retryPeriod + " seconds");

				ExecUtil.sleep(1000 * retryPeriod);
			}
		}
	}

	public static HttpURLConnection request(
			HttpAuthorization httpAuthorizationHeader, String requestBody,
			Map<String, String> requestHeaders, String requestMethod,
			String url)
		throws IOException {

		return request(
			httpAuthorizationHeader, _MAX_RETRIES_DEFAULT, requestBody,
			requestHeaders, requestMethod, _RETRY_PERIOD_DEFAULT,
			_TIMEOUT_DEFAULT, url);
	}

	public static class BasicHttpAuthorization extends HttpAuthorization {

		public BasicHttpAuthorization(String password, String username) {
			super(Type.BASIC);

			this.password = password;
			this.username = username;
		}

		@Override
		public String toString() {
			String authorization = StringUtil.combine(username, ":", password);

			return StringUtil.combine(
				"Basic ", Base64.encodeBase64String(authorization.getBytes()));
		}

		protected String password;
		protected String username;

	}

	public abstract static class HttpAuthorization {

		public Type getType() {
			return type;
		}

		public static enum Type {

			BASIC, TOKEN

		}

		protected HttpAuthorization(Type type) {
			this.type = type;
		}

		protected Type type;

	}

	public static class TokenHttpAuthorization extends HttpAuthorization {

		public TokenHttpAuthorization(String token) {
			super(Type.TOKEN);

			this.token = token;
		}

		@Override
		public String toString() {
			return StringUtil.combine("token ", token);
		}

		protected String token;

	}

	private static String _fixURL(String url) {
		url = url.replace(" ", "%20");
		url = url.replace("#", "%23");
		url = url.replace("(", "%28");
		url = url.replace(")", "%29");
		url = url.replace("[", "%5B");
		url = url.replace("]", "%5D");

		return url;
	}

	private static final Integer _MAX_RETRIES_DEFAULT = 3;

	private static final Integer _RETRY_PERIOD_DEFAULT = 5;

	private static final Integer _TIMEOUT_DEFAULT = 0;

}