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

package com.liferay.poshi.runner.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Objects;

import org.openqa.selenium.StaleElementReferenceException;

/**
 * @author Kevin Yen
 */
public class ExternalMethod {

	public static Object execute(
			Method method, Object object, Object[] parameters)
		throws Exception {

		Object returnObject = null;

		try {
			returnObject = method.invoke(object, parameters);
		}
		catch (Exception e1) {
			Throwable throwable = e1.getCause();

			if (throwable instanceof StaleElementReferenceException) {
				StringBuilder sb = new StringBuilder();

				sb.append("\nElement turned stale while running ");
				sb.append(method.getName());
				sb.append(". Retrying in ");
				sb.append(PropsValues.TEST_RETRY_COMMAND_WAIT_TIME);
				sb.append("seconds.");

				System.out.println(sb.toString());

				try {
					returnObject = method.invoke(object, parameters);
				}
				catch (Exception e2) {
					throwable = e2.getCause();

					throw new Exception(throwable.getMessage(), e2);
				}
			}
			else {
				throw new Exception(throwable.getMessage(), e1);
			}
		}

		if (returnObject == null) {
			return "";
		}

		return returnObject;
	}

	public static Object execute(
			String methodName, Object object, Object[] parameters)
		throws Exception {

		Class<?> clazz = object.getClass();

		Method method = getMethod(clazz, methodName, parameters);

		return execute(method, object, parameters);
	}

	public static Object execute(String className, String methodName)
		throws Exception {

		Object[] parameters = {};

		return execute(className, methodName, parameters);
	}

	public static Object execute(
			String className, String methodName, Object[] parameters)
		throws Exception {

		Class<?> clazz = Class.forName(className);

		Method method = getMethod(clazz, methodName, parameters);

		int modifiers = method.getModifiers();

		Object object = null;

		if (!Modifier.isStatic(modifiers)) {
			object = clazz.newInstance();
		}

		return execute(method, object, parameters);
	}

	public static Method getMethod(
			Class clazz, String methodName, Object[] parameters)
		throws Exception {

		for (Method method : clazz.getMethods()) {
			if (!methodName.equals(method.getName())) {
				continue;
			}

			Class<?>[] parameterTypes = method.getParameterTypes();

			if (parameterTypes.length != parameters.length) {
				continue;
			}

			boolean parameterTypesMatch = true;

			for (int i = 0; i < parameterTypes.length; i++) {
				Object parameter = parameters[i];

				if (parameterTypes[i] != parameter.getClass()) {
					parameterTypesMatch = false;

					break;
				}
			}

			if (parameterTypesMatch) {
				return method;
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append("Unable to find method '");
		sb.append(methodName);
		sb.append("' of class '");
		sb.append(clazz.getCanonicalName());

		if ((parameters != null) && (parameters.length != 0)) {
			sb.append("' with parameters types: (");

			for (Object parameter : parameters) {
				Class<?> parameterType = parameter.getClass();

				sb.append(parameterType.toString());
				sb.append(", ");
			}

			sb.delete(sb.length() - 2, sb.length());

			sb.append(")");
		}

		throw new IllegalArgumentException(sb.toString());

	}

}