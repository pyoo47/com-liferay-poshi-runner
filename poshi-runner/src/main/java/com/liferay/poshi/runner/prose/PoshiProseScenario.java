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

package com.liferay.poshi.runner.prose;

import com.liferay.poshi.runner.util.Dom4JUtil;
import com.liferay.poshi.runner.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;

/**
 * @author Yi-Chen Tsai
 */
public class PoshiProseScenario {

	public PoshiProseScenario(String proseScenario) throws Exception {
		Matcher matcher = _scenarioPattern.matcher(proseScenario);

		if (!matcher.find()) {
			throw new RuntimeException(
				"Prose scenario does not match pattern " +
					_scenarioPattern.pattern() + "\n" + proseScenario);
		}

		_scenarioName = matcher.group("name");
		_scenarioContent = matcher.group("content");

		List<String> poshiProseStatementStrings = StringUtil.splitByKeys(
			_scenarioContent, PoshiProseStatement.PROSE_STATEMENT_KEYWORDS);

		for (String poshiProseStatementString : poshiProseStatementStrings) {
			_poshiProseStatements.add(
				new PoshiProseStatement(poshiProseStatementString));
		}
	}

	public Element getCommandElement() {
		Element commandElement = Dom4JUtil.getNewElement("command");

		Dom4JUtil.addToElement(
			commandElement, new DefaultAttribute("name", _scenarioName));

		for (PoshiProseStatement poshiProseStatement : _poshiProseStatements) {
			Dom4JUtil.addToElement(
				commandElement, poshiProseStatement.getExecuteElement());
		}

		return commandElement;
	}

	public String getScenarioContent() {
		return _scenarioContent;
	}

	public String getScenarioName() {
		return _scenarioName;
	}

	protected static final String[] PROSE_SCENARIO_KEYWORDS = {"Scenario"};

	private final List<PoshiProseStatement> _poshiProseStatements =
		new ArrayList<>();
	private final String _scenarioContent;
	private final String _scenarioName;
	private final Pattern _scenarioPattern = Pattern.compile(
		"Scenario:[\\s]*(?<name>\\w+)[^\\w]*(?<content>[\\s\\S]*)");

}