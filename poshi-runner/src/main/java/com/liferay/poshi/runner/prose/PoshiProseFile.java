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

import org.dom4j.Element;

/**
 * @author Yi-Chen Tsai
 */
public class PoshiProseFile {

	public PoshiProseFile(String fileName, String fileContent)
		throws Exception {

		_fileName = fileName;
		_fileContent = fileContent;

		List<String> poshiProseScenarioStrings = StringUtil.splitByKeys(
			_fileContent, PoshiProseScenario.PROSE_SCENARIO_KEYWORDS);

		for (String poshiProseScenarioString : poshiProseScenarioStrings) {
			_poshiProseScenarios.add(
				new PoshiProseScenario(poshiProseScenarioString));
		}
	}

	public Element getDefinitionElement() {
		Element definitionElement = Dom4JUtil.getNewElement("definition");

		for (PoshiProseScenario poshiProseScenario : _poshiProseScenarios) {
			definitionElement.add(poshiProseScenario.getCommandElement());
		}

		return definitionElement;
	}

	public String getFileName() {
		return _fileName;
	}

	private final String _fileContent;
	private final String _fileName;
	private final List<PoshiProseScenario> _poshiProseScenarios =
		new ArrayList<>();

}