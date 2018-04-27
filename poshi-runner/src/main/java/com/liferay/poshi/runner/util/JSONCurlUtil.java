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

/**
 * @author Michael Hashimoto
 */
public class JSONCurlUtil {

	public static String get(String requestString, String jsonPath)
		throws IOException {

		String response = _request(requestString, "GET");

		DocumentContext documentContext = JsonPath.parse(response);

		Object object = documentContext.read(jsonPath);

		return object.toString();
	}

	private static String _request(String requestString, String requestMethod)
		throws IOException {

		Runtime runtime = Runtime.getRuntime();

		StringBuilder sb = new StringBuilder();

		requestString = requestString.trim();

		requestString = requestString.replaceAll("\\s+\\\\?\\s+", " ");

		for (String curlOption : requestString.split(" ")) {
			if (curlOption.matches("https?:\\/\\/.+")) {
				sb.append(curlOption);
			}
			else if (curlOption.matches("[^\\=]*\\=[^\\=]*")) {
				int x = curlOption.indexOf("=");

				String name = curlOption.substring(0, x);
				String value = curlOption.substring(x + 1);

				if (value.startsWith("{") && value.endsWith("}") &&
					OSDetector.isWindows()) {

					value = value.replaceAll("\"", "\\\\\"");

					value = "\"" + value + "\"";
				}

				sb.append(" ");
				sb.append(name);
				sb.append("=");
				sb.append(value);
			}
			else {
				sb.append(" ");
				sb.append(curlOption);
			}
		}

		System.out.println("curl -X " + requestMethod + " " + sb.toString());

		Process process = runtime.exec(
			"curl -X " + requestMethod + " " + sb.toString());

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

}