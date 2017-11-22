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

import com.liferay.poshi.runner.util.Dom4JUtil;
import com.liferay.poshi.runner.util.FileUtil;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * @author Kenji Heigel
 */
public class PoshiElementFactory extends PoshiNodeFactory {

	public static PoshiElement newPoshiElement(Element element) {
		return (PoshiElement)newPoshiNode(element);
	}

	public static PoshiElement newPoshiElement(
		PoshiElement parentPoshiElement, String readableSyntax) {

		for (PoshiNode poshiNode : getPoshiNodes("PoshiElement")) {
			PoshiElement poshiElement = (PoshiElement)poshiNode;

			PoshiElement newPoshiElement = poshiElement.clone(
				parentPoshiElement, readableSyntax);

			if (newPoshiElement != null) {
				return newPoshiElement;
			}
		}

		throw new RuntimeException(
			"Unknown readable syntax\n" + readableSyntax);
	}

	public static PoshiElement newPoshiElementFromFile(String filePath) {
		File file = new File(filePath);

		try {
			String content = FileUtil.read(file);

			if (content.contains("<definition")) {
				Document document = Dom4JUtil.parse(content);

				Element rootElement = document.getRootElement();

				return newPoshiElement(rootElement);
			}

			return newPoshiElement(null, content);
		}
		catch (Exception e) {
			System.out.println("Unable to generate the Poshi element");

			e.printStackTrace();
		}

		return null;
	}

}