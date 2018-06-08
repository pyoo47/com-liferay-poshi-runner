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
public class BaseTable implements Table {

	public BaseTable(List<List<String>> rawData) {
		this.rawData = rawData;
	}

	@Override
	public Iterator getIterator() {
		return rawData.iterator();
	}

	public int getRawDataWidth() {
		if (rawData.isEmpty()) {
			return 0;
		}

		List<String> firstRow = rawData.get(0);

		return firstRow.size();
	}

	public void verifyRawDataWidth(int width) {
		if (getRawDataWidth() != width) {
			throw new RuntimeException(
				"The raw data must have exactly " + width + " columns");
		}
	}

	protected List<List<String>> rawData;

}