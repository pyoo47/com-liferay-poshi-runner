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

package com.liferay.poshi.runner.script;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Kenji Heigel
 * @author Peter Yoo
 */
public class CodeBalanceEvaluator {

	public static boolean evaluate(char[] chars) {
		Stack<Character> stack = new Stack<>();

		for (char c : chars) {
			if (!stack.isEmpty()) {
				Character topCodeBoundary = stack.peek();

				if (c == _codeBoundariesMap.get(topCodeBoundary)) {
					stack.pop();

					continue;
				}

				if ((topCodeBoundary == '\"') || (topCodeBoundary == '\'')) {
					continue;
				}
			}

			if (_codeBoundariesMap.containsKey(c)) {
				stack.push(c);

				continue;
			}

			if (_codeBoundariesMap.containsValue(c)) {
				return false;
			}
		}

		return stack.isEmpty();
	}

	public static boolean evaluate(String string) {
		char[] chars = string.toCharArray();

		return evaluate(chars);
	}

	private static final Map<Character, Character> _codeBoundariesMap =
		new HashMap<Character, Character>() {
			{
				put('\'', '\'');
				put('\"', '\"');
				put('(', ')');
				put('{', '}');
				put('[', ']');
			}
		};

}