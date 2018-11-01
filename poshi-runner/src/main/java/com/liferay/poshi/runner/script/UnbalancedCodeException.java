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

/**
 * @author Kenji Heigel
 */
public class UnbalancedCodeException extends RuntimeException {

	public UnbalancedCodeException() {
	}

	public UnbalancedCodeException(String msg) {
		super(msg);
	}

	public UnbalancedCodeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public UnbalancedCodeException(Throwable cause) {
		super(cause);
	}

}