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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

/**
 * @author Leslie Wong
 */
public class PoshiLoggerElement {

	public void addToExecutionStackTrace(
		PoshiLoggerElement poshiLoggerElement) {

		_childPoshiLoggerElements.add(poshiLoggerElement);
	}

	public Element getElement() {
		return _element;
	}

	public String getEvent() {
		return _event;
	}

	public PoshiLoggerElement getLastChildLoggerElement() {
		if (!_childPoshiLoggerElements.isEmpty()) {
			return _childPoshiLoggerElements.get(
				_childPoshiLoggerElements.size() - 1);
		}

		return null;
	}

	public String getStatus() {
		return _status;
	}

	public void setEvent(String event) {
		_event = event;
	}

	public void setExecutionException(Exception e) {
		_executionException = e;
	}

	public void setStatus(String status) {
		_status = status;
	}

	protected PoshiLoggerElement(
		Element element, String event, String status,
		Map<String, Object> variables) {

		_element = element;
		_event = event;
		_status = status;
		_variables = variables;
	}

	private List<PoshiLoggerElement> _childPoshiLoggerElements =
		new ArrayList<>();
	private final Element _element;
	private String _event;
	private Exception _executionException;
	private String _status;
	private final Map<String, Object> _variables;

}