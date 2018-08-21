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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

/**
 * @author Leslie Wong
 */
public class PoshiExecutionNode {

	public PoshiExecutionNode(Element element) {
		this(element, null);
	}

	public PoshiExecutionNode(Element element, PoshiExecutionNode parentNode) {
		_element = element;
		_parentNode = parentNode;
	}

	public void add(Element element) {
		PoshiExecutionNode childNode = new PoshiExecutionNode(element, this);

		_childNodes.add(childNode);

		_lastChildNode = childNode;
	}

	public void addVariable(String key, Object value) {
		_variables.put(key, value);
	}

	public List<PoshiExecutionNode> getChildNodes() {
		return _childNodes;
	}

	public Element getElement() {
		return _element;
	}

	public PoshiExecutionNode getLastChildNode() {
		return _lastChildNode;
	}

	public PoshiExecutionNode getRootNode() {
		PoshiExecutionNode parentNode = this;

		while (!parentNode.isRootNode()) {
			parentNode = parentNode.getParentNode();
		}

		return parentNode;
	}

	public List<PoshiExecutionNode> getStackTrace() {
		List<PoshiExecutionNode> stacktrace = new ArrayList<>();

		PoshiExecutionNode parentNode = this;

		while (!parentNode.isRootNode()) {
			stacktrace.add(parentNode);

			parentNode = parentNode.getParentNode();
		}

		return stacktrace;
	}

	public Object getVariable(String key) {
		return _variables.get(key);
	}

	public boolean isRootNode() {
		if (_parentNode == null) {
			return true;
		}

		return false;
	}

	public boolean isVariableSet(String key) {
		return _variables.containsKey(key);
	}

	protected PoshiExecutionNode getParentNode() {
		return _parentNode;
	}

	private List<PoshiExecutionNode> _childNodes = new ArrayList<>();
	private final Element _element;
	private PoshiExecutionNode _lastChildNode;
	private PoshiExecutionNode _parentNode;
	private Map<String, Object> _variables = new HashMap<>();

}