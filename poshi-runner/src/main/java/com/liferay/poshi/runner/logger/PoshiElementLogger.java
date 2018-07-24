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

import com.liferay.poshi.runner.PoshiRunnerVariablesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.dom4j.Element;

/**
 * @author Leslie Wong
 */
public class PoshiElementLogger {

	public static void emptyExecutionStack() {
		_executionStack = new Stack<>();
	}

	public static void fail(Element element, Exception e) {
		PoshiLoggerElement poshiLoggerElement = new PoshiLoggerElement(
			element, null, "fail",
			PoshiRunnerVariablesUtil.getCommandMapVariables());

		poshiLoggerElement.setExecutionException(e);

		_addPoshiLoggerElement(poshiLoggerElement);
	}

	public static void pass(Element element) {
		pass(element, null);
	}

	public static void pass(Element element, String event) {
		PoshiLoggerElement poshiLoggerElement = new PoshiLoggerElement(
			element, event, "pass",
			PoshiRunnerVariablesUtil.getCommandMapVariables());

		_addPoshiLoggerElement(poshiLoggerElement);
	}

	public static void popExecutionStack() {
		if (_executionStack.empty()) {
			return;
		}

		PoshiLoggerElement poshiLoggerElement = _executionStack.pop();

		PoshiLoggerElement lastChildPoshiLoggerElement =
			poshiLoggerElement.getLastChildLoggerElement();

		if (lastChildPoshiLoggerElement != null) {
			String status = lastChildPoshiLoggerElement.getStatus();

			if (!status.equals("fail")) {
				poshiLoggerElement.setStatus("pass");
			}
		}
	}

	public static void pushExecutionStack(Element element) {
		PoshiLoggerElement poshiLoggerElement = new PoshiLoggerElement(
			element, null, "pending",
			PoshiRunnerVariablesUtil.getCommandMapVariables());

		_addPoshiLoggerElement(poshiLoggerElement);

		_executionStack.push(poshiLoggerElement);
	}

	public static void startLog() {
		_executionStack = new Stack<>();
		_poshiLoggerElements = new ArrayList<>();
	}

	public static void updateExecutingPoshiLoggerElementEvent(String event) {
		if (_executionStack.empty()) {
			throw new RuntimeException(
				"Failed to update execution stack with event, the execution " +
					"stack is empty");
		}

		PoshiLoggerElement executingPoshiLoggerElement = _executionStack.peek();

		PoshiLoggerElement lastChildLoggerElement =
			executingPoshiLoggerElement.getLastChildLoggerElement();

		lastChildLoggerElement.setEvent(event);
	}

	public static void warn(Element element, Exception e) {
		PoshiLoggerElement poshiLoggerElement = new PoshiLoggerElement(
			element, e.getMessage(), "warn",
			PoshiRunnerVariablesUtil.getCommandMapVariables());

		poshiLoggerElement.setExecutionException(e);

		_addPoshiLoggerElement(poshiLoggerElement);
	}

	private static void _addPoshiLoggerElement(
		PoshiLoggerElement poshiLoggerElement) {

		if (!_executionStack.empty()) {
			PoshiLoggerElement executingPoshiLoggerElement =
				_executionStack.peek();

			executingPoshiLoggerElement.addToExecutionStackTrace(
				poshiLoggerElement);
		}
		else {
			_poshiLoggerElements.add(poshiLoggerElement);
		}
	}

	private static Stack<PoshiLoggerElement> _executionStack;
	private static List<PoshiLoggerElement> _poshiLoggerElements;

}