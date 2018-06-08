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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yi-Chen Tsai
 */
public class RowsHashTable extends BaseTable {

	public RowsHashTable(List<List<String>> rawData) {
		super(rawData);

		verifyRawDataWidth(2);

		LinkedHashMap row = new LinkedHashMap<>();

		for (List<String> rawDataRow : rawData) {
			row.put(rawDataRow.get(0), rawDataRow.get(1));
		}

		_rowsHashTable.add(row);
	}

	@Override
	public Iterator getIterator() {
		return _rowsHashTable.iterator();
	}

	public List<LinkedHashMap<String, String>> getTable() {
		return _rowsHashTable;
	}

	private final List<LinkedHashMap<String, String>> _rowsHashTable =
		new ArrayList<>();

}