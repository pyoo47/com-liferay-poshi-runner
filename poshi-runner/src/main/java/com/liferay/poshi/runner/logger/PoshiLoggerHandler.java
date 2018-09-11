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

package com.liferay.poshi.runner.logger;

import com.liferay.poshi.runner.PoshiRunnerContext;
import com.liferay.poshi.runner.PoshiRunnerGetterUtil;
import com.liferay.poshi.runner.PoshiRunnerStackTraceUtil;
import com.liferay.poshi.runner.elements.PoshiElement;
import com.liferay.poshi.runner.exception.PoshiRunnerLoggerException;
import com.liferay.poshi.runner.util.FileUtil;
import com.liferay.poshi.runner.util.GetterUtil;
import com.liferay.poshi.runner.util.PropsValues;
import com.liferay.poshi.runner.util.StringUtil;

import java.io.IOException;

import java.net.URL;

import java.util.List;

import org.dom4j.Element;

/**
 * @author Leslie Wong
 */
public class PoshiLoggerHandler {

	public PoshiLoggerHandler(String testNamespacedClassCommandName)
		throws Exception {

		_commandLoggerHandler = new CommandLoggerHandler();

		_syntaxLoggerHandler = _getSyntaxLoggerHandler(
			testNamespacedClassCommandName);
	}

	public void createPoshiReport() throws IOException {
		ClassLoader classLoader = PoshiLoggerHandler.class.getClassLoader();

		URL url = classLoader.getResource("META-INF/resources/html/index.html");

		String indexHTMLContent = FileUtil.read(url);

		indexHTMLContent = indexHTMLContent.replace(
			"<ul class=\"command-log\" data-logid=\"01\" " +
				"id=\"commandLog\"></ul>",
			_commandLoggerHandler.getCommandLogText());
		indexHTMLContent = indexHTMLContent.replace(
			"<ul class=\"syntax-log-container\" id=\"syntaxLogContainer\"" +
				"></ul>",
			_syntaxLoggerHandler.getSyntaxLogText());

		String currentDirName = PoshiRunnerGetterUtil.getCanonicalPath(".");

		if (PropsValues.TEST_RUN_LOCALLY) {
			FileUtil.copyFileFromResource(
				"META-INF/resources/css/main.css",
				currentDirName + "/test-results/css/main.css");
			FileUtil.copyFileFromResource(
				"META-INF/resources/js/component.js",
				currentDirName + "/test-results/js/component.js");
			FileUtil.copyFileFromResource(
				"META-INF/resources/js/main.js",
				currentDirName + "/test-results/js/main.js");
		}
		else {
			indexHTMLContent = StringUtil.replace(
				indexHTMLContent, "<link href=\"../css/main.css\"",
				"<link href=\"" + PropsValues.LOGGER_RESOURCES_URL +
					"/css/main.css\"");
			indexHTMLContent = StringUtil.replace(
				indexHTMLContent, "<script defer src=\"../js/component.js\"",
				"<script defer src=\"" + PropsValues.LOGGER_RESOURCES_URL +
					"/js/component.js\"");
			indexHTMLContent = StringUtil.replace(
				indexHTMLContent, "<script defer src=\"../js/main.js\"",
				"<script defer src=\"" + PropsValues.LOGGER_RESOURCES_URL +
					"/js/main.js\"");
		}

		StringBuilder sb = new StringBuilder();

		sb.append(currentDirName);
		sb.append("/test-results/");
		sb.append(
			StringUtil.replace(
				PoshiRunnerContext.getTestCaseNamespacedClassCommandName(), "#",
				"_"));
		sb.append("/index.html");

		FileUtil.write(sb.toString(), indexHTMLContent);
	}

	public void failCommand(Element element) throws PoshiRunnerLoggerException {
		_commandLoggerHandler.failCommand(element, _syntaxLoggerHandler);

		LoggerElement syntaxLoggerElement =
			_syntaxLoggerHandler.getSyntaxLoggerElement(
				PoshiRunnerStackTraceUtil.getSimpleStackTrace());

		syntaxLoggerElement.setAttribute("data-status01", "fail");
	}

	public int getErrorLinkId() {
		return _commandLoggerHandler.getErrorLinkId();
	}

	public void logExternalMethodCommand(
			Element element, List<String> arguments, Object returnValue)
		throws Exception {

		_commandLoggerHandler.logExternalMethodCommand(
			element, arguments, returnValue, _syntaxLoggerHandler);
	}

	public void logMessage(Element element) throws PoshiRunnerLoggerException {
		_commandLoggerHandler.logMessage(element, _syntaxLoggerHandler);

		LoggerElement syntaxLoggerElement =
			_syntaxLoggerHandler.getSyntaxLoggerElement(
				PoshiRunnerStackTraceUtil.getSimpleStackTrace());

		syntaxLoggerElement.setAttribute("data-status01", "pass");

		_linkLoggerElements(
			syntaxLoggerElement, _commandLoggerHandler.lineGroupLoggerElement);
	}

	public void logNamespacedClassCommandName(
		String namespacedClassCommandName) {

		_commandLoggerHandler.logNamespacedClassCommandName(
			namespacedClassCommandName);
	}

	public void logSeleniumCommand(Element element, List<String> arguments)
		throws PoshiRunnerLoggerException {

		_commandLoggerHandler.logSeleniumCommand(element, arguments);
	}

	public void passCommand(Element element) throws PoshiRunnerLoggerException {
		_commandLoggerHandler.passCommand(element, _syntaxLoggerHandler);

		LoggerElement syntaxLoggerElement =
			_syntaxLoggerHandler.getSyntaxLoggerElement(
				PoshiRunnerStackTraceUtil.getSimpleStackTrace());

		syntaxLoggerElement.setAttribute("data-status01", "pass");
	}

	public void startCommand(Element element)
		throws PoshiRunnerLoggerException {

		_commandLoggerHandler.startCommand(element, _syntaxLoggerHandler);

		LoggerElement syntaxLoggerElement =
			_syntaxLoggerHandler.getSyntaxLoggerElement(
				PoshiRunnerStackTraceUtil.getSimpleStackTrace());

		syntaxLoggerElement.setAttribute("data-status01", "pending");

		_linkLoggerElements(
			syntaxLoggerElement, _commandLoggerHandler.lineGroupLoggerElement);
	}

	public void updateStatus(Element element, String status) {
		_syntaxLoggerHandler.updateStatus(element, status);
	}

	public void warnCommand(Element element) throws PoshiRunnerLoggerException {
		_commandLoggerHandler.warnCommand(element, _syntaxLoggerHandler);

		LoggerElement syntaxLoggerElement =
			_syntaxLoggerHandler.getSyntaxLoggerElement(
				PoshiRunnerStackTraceUtil.getSimpleStackTrace());

		syntaxLoggerElement.setAttribute("data-status01", "warning");
	}

	private SyntaxLoggerHandler _getSyntaxLoggerHandler(
			String namespacedClassCommandName)
		throws Exception {

		String namespace =
			PoshiRunnerGetterUtil.getNamespaceFromNamespacedClassCommandName(
				namespacedClassCommandName);

		String classCommandName =
			PoshiRunnerGetterUtil.
				getClassCommandNameFromNamespacedClassCommandName(
					namespacedClassCommandName);

		Element commandElement = PoshiRunnerContext.getTestCaseCommandElement(
			classCommandName, namespace);

		if (commandElement instanceof PoshiElement) {
			return new ScriptLoggerHandler(namespacedClassCommandName);
		}

		return new XMLLoggerHandler(namespacedClassCommandName);
	}

	private void _linkLoggerElements(
		LoggerElement lineGroupLoggerElement,
		LoggerElement scriptLoggerElement) {

		String functionLinkID = scriptLoggerElement.getAttributeValue(
			"data-functionlinkid");

		if (functionLinkID != null) {
			_functionLinkId = GetterUtil.getInteger(
				functionLinkID.substring(15));
		}

		scriptLoggerElement.setAttribute(
			"data-functionlinkid", "functionLinkId-" + _functionLinkId);

		lineGroupLoggerElement.setAttribute(
			"data-functionlinkid", "functionLinkId-" + _functionLinkId);

		_functionLinkId++;
	}

	private final CommandLoggerHandler _commandLoggerHandler;
	private int _functionLinkId;
	private final SyntaxLoggerHandler _syntaxLoggerHandler;

}