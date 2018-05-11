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

package com.liferay.poshi.runner.prose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yi-Chen Tsai
 */
public class PoshiProseStatement {

	public PoshiProseStatement(String proseStatement) throws Exception {
		_proseStatement = proseStatement;

		_poshiProseMatcher = PoshiProseMatcher.getPoshiProseMatcher(
			_proseStatement.replaceAll(_varValuePattern.pattern(), "\"\""));

		List<String> parameterNames = _poshiProseMatcher.getParameterNames();

		List<String> parameterValues = new ArrayList<>();

		Matcher matcher = _varValuePattern.matcher(_proseStatement);

		while (matcher.find()) {
			parameterValues.add(matcher.group(1));
		}

		for (int i = 0; i < parameterNames.size(); i++) {
			String parameterName = parameterNames.get(i);
			String parameterValue = parameterValues.get(i);

			if (!_varValueMap.containsKey(parameterName)) {
				_varValueMap.put(parameterName, parameterValue);

				continue;
			}

			String varValue = _varValueMap.get(parameterName);

			if (varValue.equals(parameterValue)) {
				StringBuilder sb = new StringBuilder();

				sb.append("Duplicate variable value assignment: ${");
				sb.append(parameterName);
				sb.append("} already has a value of ");
				sb.append(varValue);
				sb.append("\nProse statement: ");
				sb.append(_proseStatement);
				sb.append("\nMatching maro prose statement: ");
				sb.append(_poshiProseMatcher.getPoshiProse());

				throw new RuntimeException(sb.toString());
			}
		}
	}

	public String getProseStatement() {
		return _proseStatement;
	}

	public Map<String, String> getVarValueMap() {
		return _varValueMap;
	}

	private static final Pattern _varValuePattern = Pattern.compile(
		"\"(.*?)\"");

	private final PoshiProseMatcher _poshiProseMatcher;
	private final String _proseStatement;
	private final Map<String, String> _varValueMap = new HashMap<>();

}