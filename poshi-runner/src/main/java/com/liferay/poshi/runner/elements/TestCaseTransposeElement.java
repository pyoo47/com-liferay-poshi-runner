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

import com.liferay.poshi.runner.util.Validator;

import java.util.List;

import org.dom4j.Element;

/**
 * @author Yi-Chen Tsai
 */
public class TestCaseTransposeElement extends TransposeElement {

	public TestCaseTransposeElement(
		Element baseElement, Element overrideElement,
		String overrideNamespacedClassName) {

		super(baseElement, overrideElement, overrideNamespacedClassName);
	}

	@Override
	protected void overrideCommandElements() {
		Element overrideElement = getOverrideElementCopy();

		Element overrideSetUpElement = overrideElement.element("set-up");

		if (Validator.isNotNull(overrideSetUpElement)) {
			Element baseSetUpElement = element("set-up");

			baseSetUpElement.detach();

			add(
				new CommandTransposeElement(
					baseSetUpElement, overrideSetUpElement,
					getOverrideNamespacedClassName()));
		}

		Element overrideTearDownElement = overrideElement.element("tear-down");

		if (Validator.isNotNull(overrideTearDownElement)) {
			Element baseTearDownElement = element("tear-down");

			baseTearDownElement.detach();

			add(
				new CommandTransposeElement(
					baseTearDownElement, overrideTearDownElement,
					getOverrideNamespacedClassName()));
		}

		super.overrideCommandElements();
	}

	@Override
	protected void transpose() {
		super.transpose();

		overrideCommandElements();
		overrideVarElements();
		_overridePropertyElements();
		_overrideRootSummaryAttribute();
	}

	private void _overridePropertyElements() {
		Element overrideElement = getOverrideElementCopy();

		List<Element> basePropertyElements = elements("property");
		List<Element> overridePropertyElements = overrideElement.elements(
			"property");

		for (Element overridePropertyElement : overridePropertyElements) {
			String overridePropertyName =
				overridePropertyElement.attributeValue("name");

			for (Element basePropertyElement : basePropertyElements) {
				if (overridePropertyName.equals(
						basePropertyElement.attributeValue("name"))) {

					basePropertyElement.detach();

					break;
				}
			}

			add(overridePropertyElement.createCopy());
		}
	}

	private void _overrideRootSummaryAttribute() {
		Element overrideElement = getOverrideElementCopy();

		String overrideRootSummary = overrideElement.attributeValue("summary");

		if (Validator.isNotNull(overrideRootSummary)) {
			addAttribute("summary", overrideRootSummary);
		}
	}

}