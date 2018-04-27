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

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Michael Hashimoto
 */
public class JSONCurlUtil {

	public static String get(String requestString)
		throws IOException, TimeoutException {

		Request request = new Request(requestString, "GET");

		return request.send();
	}

	public static String get(String requestString, String jsonPath)
		throws IOException, TimeoutException {

		Request request = new Request(requestString, "GET");

		String response = request.send();

		DocumentContext documentContext = JsonPath.parse(response);

		Object object = documentContext.read(jsonPath);

		return object.toString();
	}

	public static void post(String requestString)
		throws IOException, TimeoutException {

		Request request = new Request(requestString, "POST");

		request.send();
	}

	protected Request getRequest(String requestString, String requestMethod) {
		return new Request(requestString, requestMethod);
	}

	private static class Request {

		public Request(String requestString, String requestMethod) {
			_requestMethod = requestMethod;

			requestString = requestString.replaceAll("\\s+\\\\?\\s+", "\n");

			requestString = _escapeRequestString(requestString);

			List<String> tokens = _tokenize(requestString.trim());

			_requestURL = _getRequestURL(tokens);

			tokens.remove(_requestURL);

			_setRequestOptions(tokens);
		}

		public String send()
			throws IOException, TimeoutException {

			StringBuilder sb = new StringBuilder();

			sb.append("curl -X ");
			sb.append(_requestMethod);
			sb.append(" ");
			sb.append(_getRequestOptionsString());
			sb.append(" ");
			sb.append(_requestURL);

			Process process = ExecUtil.executeCommands(sb.toString());

			InputStream inputStream = process.getInputStream();

			inputStream.mark(Integer.MAX_VALUE);

			String response = ExecUtil.readInputStream(inputStream);

			System.out.println("Response: " + response);

			inputStream.reset();

			if (process.exitValue() != 0) {
				inputStream = process.getErrorStream();

				inputStream.mark(Integer.MAX_VALUE);

				System.out.println(
					"Error stream: " + ExecUtil.readInputStream(inputStream));

				inputStream.reset();

				throw new RuntimeException(
					"Command finished with exit value: " + process.exitValue());
			}

			return response;
		}

		private String _getRequestOptionsString() {
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