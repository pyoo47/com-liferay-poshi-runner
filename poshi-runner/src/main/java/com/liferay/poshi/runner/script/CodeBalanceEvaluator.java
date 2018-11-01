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

	private static int _getBracketTokenIndex(char c) {
		for (int i = 0; i < _bracketTokens.length; i++) {
			if (c == _bracketTokens[i]) {
				return i;
			}
		}

		return -1;
	}

	private static boolean _isClosingBracket(int tokenIndex) {
		if ((tokenIndex % 2) == 0) {
			return false;
		}

		return true;
	}

	private static boolean _isOpeningBracket(int tokenIndex) {
		return !_isClosingBracket(tokenIndex);
	}

	private Result _evaluateBrackets(Opener opener, int index) {
		int tokenIndex = -1;

		while (index < _chars.length) {
			char c = _chars[index];

			tokenIndex = _getBracketTokenIndex(c);

			if (tokenIndex == -1) {
				index++;

				continue;
			}

			if (_isClosingBracket(tokenIndex) && (opener != null)) {
				if (tokenIndex == (opener.getOpenerTokenIndex() + 1)) {
					return new Result(true, index);
				}

				return new Result(
					false, index,
					"Unexpected closing bracket found at " +
						_getLocationString(index));
			}

			if (_isOpeningBracket(tokenIndex)) {
				Result result = _evaluateBrackets(
					new Opener(_bracketTokens[tokenIndex], index, tokenIndex),
					index + 1);

				if (!result.isBalanced()) {
					return result;
				}

				return _evaluateBrackets(opener, result.getEndIndex() + 1);
			}

			break;
		}

		if (tokenIndex == -1) {
			if (opener == null) {
				return new Result(true, index);
			}

			return new Result(
				false, index,
				"Unclosed opening bracket at " +
					_getLocationString(opener.getIndex()));
		}

		return new Result(
			false, index,
			"Unexpected closing bracket found at " + _getLocationString(index));
	}

	private String _getLine(int lineNumber) {
		String[] lines = (new String(_chars)).split("\n");

		return lines[lineNumber - 1];
	}

	private String _getLocationString(int index) {
		int lineNumber = 1;

		int newLineIndex = -1;

		for (int i = 0; i < index; i++) {
			if (_chars[i] == '\n') {
				lineNumber++;

				newLineIndex = i;
			}
		}

		int column = 1;

		for (int i = newLineIndex + 1; i < index; i++) {
			if (_chars[i] == '\t') {
				column += 4;

				continue;
			}

			column++;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("line ");

		sb.append(lineNumber);

		sb.append(":\n");
		sb.append(_getLine(lineNumber));
		sb.append("\n");

		for (int i = 1; i < column; i++) {
			sb.append(" ");
		}

		sb.append("^");

		return sb.toString();
	}

	private static char[] _bracketTokens = {'{', '}', '(', ')', '[', ']'};
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

	private final char[] _chars;

	private class Opener {

		public Opener(char character, int index, int openerTokenIndex) {
			_character = character;
			_index = index;
			_openerTokenIndex = openerTokenIndex;
		}

		public char getCharacter() {
			return _character;
		}

		public int getIndex() {
			return _index;
		}

		public int getOpenerTokenIndex() {
			return _openerTokenIndex;
		}

		private final char _character;
		private final int _index;
		private final int _openerTokenIndex;

	}

	private class Result {

		public Result(boolean balanced, int endIndex) {
			this(balanced, endIndex, null);
		}

		public Result(boolean balanced, int endIndex, String message) {
			_balanced = balanced;
			_endIndex = endIndex;
			_message = message;
		}

		public int getEndIndex() {
			return _endIndex;
		}

		public String getMessage() {
			return _message;
		}

		public boolean isBalanced() {
			return _balanced;
		}

		private final boolean _balanced;
		private final int _endIndex;
		private final String _message;

	}

}