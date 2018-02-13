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

package com.liferay.poshi.runner;

import com.liferay.poshi.runner.logger.CommandLoggerHandler;
import com.liferay.poshi.runner.logger.LoggerUtil;
import com.liferay.poshi.runner.logger.SummaryLoggerHandler;
import com.liferay.poshi.runner.logger.XMLLoggerHandler;
import com.liferay.poshi.runner.selenium.LiferaySeleniumHelper;
import com.liferay.poshi.runner.selenium.SeleniumUtil;
import com.liferay.poshi.runner.util.PropsValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Element;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.UnreachableBrowserException;

/**
 * @author Brian Wing Shun Chan
 * @author Michael Hashimoto
 * @author Karen Dang
 * @author Leslie Wong
 */
@RunWith(Parameterized.class)
public class PoshiRunner {

	@Parameters(name = "{0}")
	public static List<String> getList() throws Exception {
		PoshiRunnerContext.readFiles();

		List<String> namespaceClassCommandNames = new ArrayList<>();

		List<String> testNames = Arrays.asList(
			PropsValues.TEST_NAME.split("\\s*,\\s*"));

		for (String testName : testNames) {
			PoshiRunnerValidation.validate(testName);

			String namespace =
				PoshiRunnerGetterUtil.getNamespaceFromNamespaceClassCommandName(
					testName);

			if (testName.contains("#")) {
				String classCommandName =
					PoshiRunnerGetterUtil.
						getClassCommandNameFromNamespaceClassCommandName(
							testName);

				namespaceClassCommandNames.add(
					namespace + "." + classCommandName);
			}
			else {
				String className =
					PoshiRunnerGetterUtil.
						getClassNameFromNamespaceClassCommandName(testName);

				Element rootElement = PoshiRunnerContext.getTestCaseRootElement(
					className, namespace);

				List<Element> commandElements = rootElement.elements("command");

				for (Element commandElement : commandElements) {
					namespaceClassCommandNames.add(
						namespace + "." + className + "#" +
							commandElement.attributeValue("name"));
				}
			}
		}

		return namespaceClassCommandNames;
	}

	public PoshiRunner(String namespaceClassCommandName) throws Exception {
		_testNamespaceClassCommandName = namespaceClassCommandName;

		_testNamespaceClassName =
			PoshiRunnerGetterUtil.
				getNamespaceClassNameFromNamespaceClassCommandName(
					_testNamespaceClassCommandName);
	}

	@Before
	public void setUp() throws Exception {
		System.out.println();
		System.out.println("###");
		System.out.println("### " + _testNamespaceClassCommandName);
		System.out.println("###");
		System.out.println();

		PoshiRunnerContext.setTestCaseNamespaceClassCommandName(
			_testNamespaceClassCommandName);

		PoshiRunnerVariablesUtil.clear();

		try {
			XMLLoggerHandler.generateXMLLog(_testNamespaceClassCommandName);

			LoggerUtil.startLogger();

			SeleniumUtil.startSelenium();

			_runSetUp();
		}
		catch (WebDriverException wde) {
			wde.printStackTrace();

			throw wde;
		}
		catch (Exception e) {
			LiferaySeleniumHelper.printJavaProcessStacktrace();

			PoshiRunnerStackTraceUtil.printStackTrace(e.getMessage());

			PoshiRunnerStackTraceUtil.emptyStackTrace();

			e.printStackTrace();

			if (PropsValues.TEST_PAUSE_ON_FAILURE) {
				LoggerUtil.pauseFailedTest();
			}

			throw e;
		}
	}

	@After
	public void tearDown() throws Exception {
		LiferaySeleniumHelper.writePoshiWarnings();

		LoggerUtil.createSummary();

		try {
			if (!PropsValues.TEST_SKIP_TEAR_DOWN) {
				_runTearDown();
			}
		}
		catch (Exception e) {
			PoshiRunnerStackTraceUtil.printStackTrace(e.getMessage());

			PoshiRunnerStackTraceUtil.emptyStackTrace();

			if (PropsValues.TEST_PAUSE_ON_FAILURE) {
				LoggerUtil.pauseFailedTest();
			}
		}
		finally {
			LoggerUtil.stopLogger();

			SeleniumUtil.stopSelenium();
		}
	}

	@Test
	public void test() throws Exception {
		try {
			_runCommand();

			LiferaySeleniumHelper.assertNoPoshiWarnings();
		}
		catch (Exception e) {
			LiferaySeleniumHelper.printJavaProcessStacktrace();

			PoshiRunnerStackTraceUtil.printStackTrace(e.getMessage());

			PoshiRunnerStackTraceUtil.emptyStackTrace();

			e.printStackTrace();

			if (PropsValues.TEST_PAUSE_ON_FAILURE) {
				LoggerUtil.pauseFailedTest();
			}

			throw e;
		}
	}

	@Rule
	public Retry retry = new Retry(
		3, TimeoutException.class, UnreachableBrowserException.class);

	private void _runCommand() throws Exception {
		CommandLoggerHandler.logNamespaceClassCommandName(
			_testNamespaceClassCommandName);

		_runNamespaceClassCommandName(_testNamespaceClassCommandName);
	}

	private void _runNamespaceClassCommandName(String namespaceClassCommandName)
		throws Exception {

		String className =
			PoshiRunnerGetterUtil.getClassNameFromNamespaceClassCommandName(
				namespaceClassCommandName);
		String namespace =
			PoshiRunnerGetterUtil.getNamespaceFromNamespaceClassCommandName(
				namespaceClassCommandName);

		Element rootElement = PoshiRunnerContext.getTestCaseRootElement(
			className, namespace);

		List<Element> varElements = rootElement.elements("var");

		for (Element varElement : varElements) {
			PoshiRunnerExecutor.runVarElement(varElement, false, false);
		}

		PoshiRunnerVariablesUtil.pushCommandMap(true);

		String classCommandName =
			PoshiRunnerGetterUtil.
				getClassCommandNameFromNamespaceClassCommandName(
					namespaceClassCommandName);

		Element commandElement = PoshiRunnerContext.getTestCaseCommandElement(
			classCommandName, namespace);

		if (commandElement != null) {
			PoshiRunnerStackTraceUtil.startStackTrace(
				namespaceClassCommandName, "test-case");

			XMLLoggerHandler.updateStatus(commandElement, "pending");

			PoshiRunnerExecutor.parseElement(commandElement);

			XMLLoggerHandler.updateStatus(commandElement, "pass");

			PoshiRunnerStackTraceUtil.emptyStackTrace();
		}
	}

	private void _runSetUp() throws Exception {
		CommandLoggerHandler.logNamespaceClassCommandName(
			_testNamespaceClassName + "#set-up");

		SummaryLoggerHandler.startMajorSteps();

		_runNamespaceClassCommandName(_testNamespaceClassName + "#set-up");
	}

	private void _runTearDown() throws Exception {
		CommandLoggerHandler.logNamespaceClassCommandName(
			_testNamespaceClassName + "#tear-down");

		SummaryLoggerHandler.startMajorSteps();

		_runNamespaceClassCommandName(_testNamespaceClassName + "#tear-down");
	}

	private final String _testNamespaceClassCommandName;
	private final String _testNamespaceClassName;

	private class Retry implements TestRule {

		public Retry(int retryCount, Class... retryClasses) {
			_retryCount = retryCount;
			_retryClasses = retryClasses;
		}

		public Statement apply(
			final Statement statement, final Description description) {

			return new Statement() {

				@Override
				public void evaluate() throws Throwable {
					for (int i = 0; i < _retryCount; i++) {
						try {
							statement.evaluate();

							return;
						}
						catch (Throwable t) {
							if (i == (_retryCount - 1)) {
								throw t;
							}

							boolean retry = false;

							List<Throwable> throwables = null;

							if (t instanceof MultipleFailureException) {
								MultipleFailureException mfe =
									(MultipleFailureException)t;

								throwables = mfe.getFailures();
							}
							else {
								throwables = new ArrayList<>(1);

								throwables.add(t);
							}

							for (Class retryClass : _retryClasses) {
								for (Throwable throwable : throwables) {
									if (retryClass.isInstance(throwable)) {
										retry = true;
									}
								}
							}

							if (retry == false) {
								throw t;
							}
						}
					}
				}

			};
		}

		private final Class[] _retryClasses;
		private final int _retryCount;

	}

}