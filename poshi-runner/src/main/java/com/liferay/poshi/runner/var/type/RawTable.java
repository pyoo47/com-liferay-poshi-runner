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

package com.liferay.poshi.runner.var.type;

import java.util.Iterator;
import java.util.List;

/**
 * @author Yi-Chen Tsai
 */
public class RawTable extends BaseTable {

	public RawTable(List<List<String>> rawData) {
		super(rawData);

		_rawTable = rawData;
	}

	@Override
	public Iterator getIterator() {
		return _rawTable.iterator();
	}

	public List<List<String>> getTable() {
		return _rawTable;
	}

	private final List<List<String>> _rawTable;

}