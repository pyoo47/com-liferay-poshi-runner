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

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class JSONCurlUtil {

	public static String get(String requestString, String jsonPath)
		throws IOException {

		Request request = new Request(requestString);

		request.setRequestMethod("GET");

		String response = _request(request);

		DocumentContext documentContext = JsonPath.parse(response);

		Object object = documentContext.read(jsonPath);

		return object.toString();
	}

	protected Request getRequest(String requestString) {
		return new Request(requestString);
	}

	private static String _request(Request request) throws IOException {
		Runtime runtime = Runtime.getRuntime();

		StringBuilder sb = new StringBuilder();

		sb.append("curl -X ");
		sb.append(request.getRequestMethod());
		sb.append(" ");
		sb.append(request.getRequestOptionsString());
		sb.append(" ");
		sb.append(request.getRequestURL());

		System.out.println("Making request: " + sb.toString());

		Process process = runtime.exec(sb.toString());

		InputStreamReader inputStreamReader = new InputStreamReader(
			process.getInputStream());

		BufferedReader inputBufferedReader = new BufferedReader(
			inputStreamReader);

		String line = null;

		sb = new StringBuilder();

		while ((line = inputBufferedReader.readLine()) != null) {
			sb.append(line);
		}

		String response = sb.toString();

		System.out.println(response);

		return response;
	}

	private static class Request {

		public Request(String requestString) {
			requestString = requestString.replaceAll("\\s+\\\\?\\s+", "\n");

			requestString = _escapeRequestString(requestString);

			List<String> tokens = _tokenize(requestString.trim());

			_requestURL = _getRequestURL(tokens);

			tokens.remove(_requestURL);

			_setRequestOptions(tokens);
		}

		public String getRequestMethod() {
			return _requestMethod;
		}

		public Map<String, List<String>> getRequestOptions() {
			return _requestOptions;
		}

		public String getRequestOptionsString() {
			StringBuilder sb = new StringBuilder();

			for (Map.Entry<String, List<String>> requestOption :
					_requestOptions.entrySet()) {

				List<String> optionValues = requestOption.getValue();

				if (optionValues.isEmpty()) {
					sb.append(requestOption.getKey());
				}
				else {
					for (String optionValue : optionValues) {
						sb.append(requestOption.getKey());
						sb.append(" \"");
						sb.append(_escapeOptionValue(optionValue));
						sb.append("\" ");
					}
				}
			}

			return sb.toString();
		}

		public String getRequestURL() {
			return _requestURL;
		}

		public void setRequestMethod(String requestMethod) {
			_requestMethod = requestMethod;
		}

		private String _escapeOptionValue(String optionValue) {
			optionValue = optionValue.replaceAll("(?<!\\\\)\"", "\\\\\"");

			return optionValue;
		}

		private String _escapeRequestString(String requestString) {
			Matcher matcher = _escapePattern.matcher(requestString);

			String escapedRequestString = requestString;

			while (matcher.find()) {
				String key = "$CURLDATA:" + StringUtil.randomString("10");

				_escapedValues.put(key, matcher.group(1));

				escapedRequestString = escapedRequestString.replace(
					matcher.group(0), key);
			}

			return escapedRequestString;
		}

		private String _formatToken(String token) {
			if ((token.startsWith("'") && token.endsWith("'")) ||
				(token.startsWith("\"") && token.endsWith("\""))) {

				token = token.substring(1, token.length() - 1);
			}

			token = token.replaceAll("\\\\\"", "\"");

			return token;
		}

		private String _getRequestURL(List<String> tokens) {
			int requestURLIndex = -1;

			for (int i = 0; i < tokens.size(); i++) {
				String token = tokens.get(i);

				if (token.startsWith("http")) {
					if (requestURLIndex != -1) {
						StringBuilder sb = new StringBuilder();

						sb.append("Found 2 URLs when only 1 is allowed:\n");
						sb.append(tokens.get(requestURLIndex));
						sb.append("\n");
						sb.append(token);

						throw new IllegalArgumentException(sb.toString());
					}

					requestURLIndex = i;
				}
			}

			if (requestURLIndex == -1) {
				throw new IllegalArgumentException("No URL found in statement");
			}

			return tokens.get(requestURLIndex);
		}

		private void _setRequestOptions(List<String> tokens) {
			for (int i = 0; i < tokens.size(); i++) {
				String token = _formatToken(tokens.get(i));

				if (token.matches(_requestPatternString)) {
					if (i < (tokens.size() - 1)) {
						String nextToken = tokens.get(i + 1);

						if (!nextToken.matches(_requestPatternString)) {
							if (nextToken.matches("\\$CURLDATA:\\w{10}")) {
								nextToken = _escapedValues.get(nextToken);
							}

							nextToken = _formatToken(nextToken);

							_validateRequestOption(token, nextToken);

							List<String> optionValues = new ArrayList<>();

							if (_requestOptions.containsKey(token)) {
								optionValues = _requestOptions.get(token);
							}

							optionValues.add(nextToken);

							if (_customOptions.containsKey(token)) {
								token = _customOptions.get(token);
							}

							_requestOptions.put(token, optionValues);

							i++;

							continue;
						}
					}

					_requestOptions.put(token, new ArrayList<String>());
				}
			}
		}

		private List<String> _tokenize(String requestString) {
			List<String> tokens = new ArrayList<>();

			Matcher matcher = _requestPattern.matcher(requestString);

			int end = -1;
			int start = -1;

			while (matcher.find()) {
				if (!tokens.isEmpty()) {
					end = matcher.start();

					if ((end - start) > 1) {
						String miscellaneousToken = requestString.substring(
							start, end);

						tokens.add(miscellaneousToken.trim());
					}
				}

				String token = matcher.group(1);

				tokens.add(token.trim());

				start = matcher.end();
			}

			if (start != requestString.length()) {
				tokens.add(requestString.substring(start));
			}

			return tokens;
		}

		private void _validateRequestOption(
			String optionType, String optionValue) {

			if (optionType.equals("--json-data")) {
				try {
					new JSONObject(optionValue);
				}
				catch (JSONException jsone) {
					throw new RuntimeException(
						"Invalid JSON: '" + optionValue + "'");
				}
			}
		}

		private static Map<String, String> _customOptions = new HashMap<>();
		private static Pattern _escapePattern = Pattern.compile(
			"<CURL_DATA\\[(.*?)\\]CURL_DATA>");
		private static String _requestMethod;
		private static Pattern _requestPattern;
		private static String _requestPatternString =
			"(-[\\w#:\\.]|--[\\w#:\\.-]{2,}|https?:[^\\s]+)(\\s+|\\Z)";

		static {
			_customOptions.put("--json-data", "--data");

			_requestPattern = Pattern.compile(_requestPatternString);
		}

		private Map<String, String> _escapedValues = new HashMap<>();
		private Map<String, List<String>> _requestOptions = new HashMap<>();
		private final String _requestURL;

	}

}