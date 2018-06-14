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

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * @author Kenji Heigel
 */
public class ForPoshiElement extends PoshiElement {

	@Override
	public PoshiElement clone(Element element) {
		if (isElementType(_ELEMENT_NAME, element)) {
			return new ForPoshiElement(element);
		}

		return null;
	}

	@Override
	public PoshiElement clone(
		PoshiElement parentPoshiElement, String poshiScript) {

		if (_isElementType(poshiScript)) {
			return new ForPoshiElement(parentPoshiElement, poshiScript);
		}

		return null;
	}

	@Override
	public void parsePoshiScript(String poshiScript) {
		for (String poshiScriptSnippet : getPoshiScriptSnippets(poshiScript)) {
			if (poshiScriptSnippet.startsWith("for (") &&
				!poshiScriptSnippet.endsWith("}")) {

				String parentheticalContent = getParentheticalContent(
					poshiScriptSnippet);

				Matcher matcher = _blockParameterPattern.matcher(
					parentheticalContent);

				if (matcher.find()) {
					addAttribute("param", matcher.group(1));

					addAttribute(matcher.group(2), matcher.group(3));

					continue;
				}

				throw new RuntimeException(
					"Invalid parameter syntax:\n" + parentheticalContent);
			}

			if (isPoshiScriptComment(poshiScriptSnippet)) {
				add(PoshiNodeFactory.newPoshiNode(this, poshiScriptSnippet));

				continue;
			}

			add(PoshiNodeFactory.newPoshiNode(this, poshiScriptSnippet));
		}
	}

	@Override
	public String toPoshiScript() {
		String poshiScript = super.toPoshiScript();

		return "\n" + createPoshiScriptSnippet(poshiScript);
	}

	protected ForPoshiElement() {
	}

	protected ForPoshiElement(Element element) {
		super(_ELEMENT_NAME, element);

		initTypeAttributeName(element);
	}

	protected ForPoshiElement(List<Attribute> attributes, List<Node> nodes) {
		super(_ELEMENT_NAME, attributes, nodes);
	}

	protected ForPoshiElement(
		PoshiElement parentPoshiElement, String poshiScript) {

		super(_ELEMENT_NAME, parentPoshiElement, poshiScript);
	}

	@Override
	protected String getBlockName() {
		StringBuilder sb = new StringBuilder();

		sb.append("for (var ");
		sb.append(attributeValue("param"));
		sb.append(" : list \"");
		sb.append(attributeValue("list"));
		sb.append("\")");

		return sb.toString();
	}

	protected List<String> getPoshiScriptSnippets(String poshiScript) {
		StringBuilder sb = new StringBuilder();

		List<String> poshiScriptSnippets = new ArrayList<>();

		for (String line : poshiScript.split("\n")) {
			String trimmedLine = line.trim();

			if (poshiScript.startsWith(line) &&
				trimmedLine.startsWith("for (")) {

				poshiScriptSnippets.add(line);

				continue;
			}

			if (!trimmedLine.startsWith("else if (") &&
				!trimmedLine.startsWith("else {")) {

				String poshiScriptSnippet = sb.toString();

				poshiScriptSnippet = poshiScriptSnippet.trim();

				if (isValidPoshiScriptSnippet(poshiScriptSnippet)) {
					poshiScriptSnippets.add(poshiScriptSnippet);

					sb.setLength(0);
				}
			}

			sb.append(line);
			sb.append("\n");
		}

		return poshiScriptSnippets;
	}

	protected void initTypeAttributeName(Element element) {
		if ((element.attribute("list") != null)) {
			typeAttributeName = "list";

			return;
		}

		if (element.attribute("table") != null) {
			typeAttributeName = "table";

			return;
		}

		try {
			throw new IllegalArgumentException(
				"Invalid 'for' element " + Dom4JUtil.format(element));
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException("Invalid 'for' element", ioe);
		}
	}

	protected String typeAttributeName;

	private boolean _isElementType(String poshiScript) {
		return isValidPoshiScriptBlock(_blockNamePattern, poshiScript);
	}

	private static final String _ELEMENT_NAME = "for";

	private static final String _POSHI_SCRIPT_KEYWORD = _ELEMENT_NAME;

	private static final Pattern _blockNamePattern = Pattern.compile(
		"^" + _POSHI_SCRIPT_KEYWORD + BLOCK_NAME_PARAMETER_REGEX,
		Pattern.DOTALL);
	private static final Pattern _blockParameterPattern = Pattern.compile(
		"var[\\s]*([\\w]*)[\\s]*:[\\s]*([\\w]*)[\\s]*\"(.*)\"");

}