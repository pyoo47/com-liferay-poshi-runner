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

package com.liferay.poshi.runner.script;

import com.liferay.poshi.runner.elements.PoshiNode;
import com.liferay.poshi.runner.util.StringUtil;

import java.io.File;

/**
 * @author Kenji Heigel
 */
public class PoshiScriptParserException extends Exception {

	public static int getExceptionCount() {
		return exceptionCount;
	}

	public PoshiScriptParserException(String msg) {
		super(msg);

		exceptionCount++;
	}

	public PoshiScriptParserException(String msg, PoshiNode poshiNode) {
		super(_formatMessage(msg, poshiNode));

		exceptionCount++;
	}

	public PoshiScriptParserException(
		String msg, String poshiScript, PoshiNode parentPoshiNode) {

		super(_formatMessage(msg, poshiScript, parentPoshiNode));

		exceptionCount++;
	}

	public PoshiScriptParserException(UnbalancedCodeException uce, File file) {
		super(
			_formatMessage(
				uce.getMessage(), file.getAbsolutePath(), uce.getLineNumber(),
				uce.getErrorPositionString()));

		exceptionCount++;
	}

	protected static int exceptionCount;

	private static String _formatMessage(String msg, PoshiNode poshiNode) {
		String poshiScript = poshiNode.getPoshiScript();

		return _formatMessage(
			msg, poshiNode.getFilePath(), poshiNode.getPoshiScriptLineNumber(),
			poshiScript);
	}

	private static String _formatMessage(
		String msg, String filePath, int lineNumber, String errorDetails) {

		StringBuilder sb = new StringBuilder();

		sb.append(msg);
		sb.append(" at:\n");
		sb.append(filePath);
		sb.append(":");
		sb.append(lineNumber);
		sb.append("\n");
		sb.append(errorDetails);

		return sb.toString();
	}

	private static String _formatMessage(
		String msg, String poshiScript, PoshiNode parentPoshiNode) {

		String parentPoshiScript = parentPoshiNode.getPoshiScript();

		parentPoshiScript = parentPoshiScript.trim();

		int index = parentPoshiScript.indexOf(poshiScript.trim());

		int lineNumber =
			parentPoshiNode.getPoshiScriptLineNumber() +
				StringUtil.count(parentPoshiScript, "\n", index);

		return _formatMessage(
			msg, parentPoshiNode.getFilePath(), lineNumber, poshiScript);
	}

}