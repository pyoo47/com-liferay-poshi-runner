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

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * @author Kenji Heigel
 */
public class OnPoshiElement extends PoshiElement {

	@Override
	public PoshiElement clone(Element element) {
		if (isElementType(_ELEMENT_NAME, element)) {
			return new OnPoshiElement(_ELEMENT_NAME, element);
		}

		return null;
	}

	@Override
	public PoshiElement clone(
		PoshiElement parentPoshiElement, String poshiScript) {

		if (isElementType(poshiScript)) {
			return new OnPoshiElement(
				_ELEMENT_NAME, parentPoshiElement, poshiScript);
		}

		return null;
	}

	@Override
	public void parseReadableSyntax(String poshiScript) {
		for (String readableBlock : getReadableBlocks(poshiScript)) {
			if (readableBlock.startsWith(getBlockName() + " {")) {
				continue;
			}

			add(PoshiNodeFactory.newPoshiNode(this, readableBlock));
		}
	}

	@Override
	public String toReadableSyntax() {
		StringBuilder sb = new StringBuilder();

		for (PoshiElement poshiElement : toPoshiElements(elements())) {
			sb.append(poshiElement.toReadableSyntax());
		}

		return createReadableBlock(sb.toString());
	}

	protected OnPoshiElement() {
	}

	protected OnPoshiElement(List<Attribute> attributes, List<Node> nodes) {
		this(_ELEMENT_NAME, attributes, nodes);
	}

	protected OnPoshiElement(String name, Element element) {
		super(name, element);
	}

	protected OnPoshiElement(
		String elementName, List<Attribute> attributes, List<Node> nodes) {

		super(elementName, attributes, nodes);
	}

	protected OnPoshiElement(
		String name, PoshiElement parentPoshiElement, String poshiScript) {

		super(name, parentPoshiElement, poshiScript);
	}

	@Override
	protected String getBlockName() {
		return "on";
	}

	protected List<String> getReadableBlocks(String poshiScript) {
		StringBuilder sb = new StringBuilder();

		List<String> readableBlocks = new ArrayList<>();

		for (String line : poshiScript.split("\n")) {
			String trimmedLine = line.trim();

			String readableBlock = sb.toString();

			readableBlock = readableBlock.trim();

			if (trimmedLine.startsWith(getReadableName() + " (") &&
				trimmedLine.endsWith("{") && (readableBlock.length() == 0)) {

				readableBlocks.add(line);

				continue;
			}

			if (trimmedLine.endsWith("{") && readableBlocks.isEmpty()) {
				continue;
			}

			if (isValidReadableBlock(readableBlock)) {
				readableBlocks.add(readableBlock);

				sb.setLength(0);
			}

			sb.append(line);
			sb.append("\n");
		}

		return readableBlocks;
	}

	protected String getReadableName() {
		return getName();
	}

	protected boolean isElementType(String poshiScript) {
		poshiScript = poshiScript.trim();

		if (!isBalancedReadableSyntax(poshiScript)) {
			return false;
		}

		if (!poshiScript.startsWith(getBlockName() + " {")) {
			return false;
		}

		if (!poshiScript.endsWith("}")) {
			return false;
		}

		return true;
	}

	private static final String _ELEMENT_NAME = "on";

}