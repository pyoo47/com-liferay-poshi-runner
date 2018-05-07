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

package com.liferay.poshi.runner.elements;

import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 * @author Yi-Chen Tsai
 */
public class TransposeElement extends DefaultElement {

	public TransposeElement(
		Element baseElement, Element overrideElement,
		String overrideNamespacedClassName) {

		super("");

		_baseElement = baseElement;
		_baseNamespacedClassName = overrideElement.attributeValue("override");
		_overrideElement = overrideElement;
		_overrideNamespacedClassName = overrideNamespacedClassName;

		transpose();
	}

	public Element getBaseElementCopy() {
		return _baseElement.createCopy();
	}

	public String getBaseNamespacedClassName() {
		return _baseNamespacedClassName;
	}

	public Element getOverrideElementCopy() {
		return _overrideElement.createCopy();
	}

	public String getOverrideNamespacedClassName() {
		return _overrideNamespacedClassName;
	}

	protected void overrideCommandElements() {
		Element overrideElement = getOverrideElementCopy();

		List<Element> baseCommandElements = elements("command");
		List<Element> overrideCommandElements = overrideElement.elements(
			"command");

		for (Element overrideCommandElement : overrideCommandElements) {
			String overrideCommandName = overrideCommandElement.attributeValue(
				"name");

			TransposeElement commandTransposeElement =
				new CommandTransposeElement(
					overrideCommandElement, overrideCommandElement,
					getOverrideNamespacedClassName());

			for (Element baseCommandElement : baseCommandElements) {
				if (overrideCommandName.equals(
						baseCommandElement.attributeValue("name"))) {

					baseCommandElement.detach();

					commandTransposeElement = new CommandTransposeElement(
						baseCommandElement, overrideCommandElement,
						getOverrideNamespacedClassName());

					break;
				}
			}

			add(commandTransposeElement);
		}
	}

	protected void overrideVarElements() {
		Element overrideElement = getOverrideElementCopy();

		List<Element> baseVarElements = elements("var");
		List<Element> overrideVarElements = overrideElement.elements("var");

		for (Element overrideVarElement : overrideVarElements) {
			String overrideVarName = overrideVarElement.attributeValue("name");

			for (Element baseVarElement : baseVarElements) {
				if (overrideVarName.equals(
						baseVarElement.attributeValue("name"))) {

					baseVarElement.detach();

					break;
				}
			}

			add(overrideVarElement.createCopy());
		}
	}

	protected void transpose() {
		Element baseElement = getBaseElementCopy();

		setQName(baseElement.getQName());

		setAttributes(baseElement.attributes());

		setContent(baseElement.content());
	}

	private final Element _baseElement;
	private final String _baseNamespacedClassName;
	private final Element _overrideElement;
	private final String _overrideNamespacedClassName;

}