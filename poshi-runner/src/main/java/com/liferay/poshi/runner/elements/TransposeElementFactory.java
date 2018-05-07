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

import org.dom4j.Element;

/**
 * @author Yi-Chen Tsai
 */
public class TransposeElementFactory {

	public static TransposeElement newTransposeElement(
		Element baseRootElement, Element overrideRootElement, String classType,
		String overrideNamespacedClassName) {

		if (classType.equals("function")) {
			return new FunctionTransposeElement(
				baseRootElement, overrideRootElement,
				overrideNamespacedClassName);
		}
		else if (classType.equals("macro")) {
			return new MacroTransposeElement(
				baseRootElement, overrideRootElement,
				overrideNamespacedClassName);
		}
		else if (classType.equals("test-case")) {
			return new TestCaseTransposeElement(
				baseRootElement, overrideRootElement,
				overrideNamespacedClassName);
		}

		return new TransposeElement(
			baseRootElement, overrideRootElement, overrideNamespacedClassName);
	}

}