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

import java.io.File;
import java.io.IOException;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Comment;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * @author Kenji Heigel
 */
public abstract class PoshiNodeFactory {

	public static PoshiNode newPoshiNode(Node node) {
		for (PoshiNode poshiNode : getPoshiNodes(node)) {
			PoshiNode newPoshiNode = (PoshiNode)poshiNode.clone(node);

			if (newPoshiNode != null) {
				return newPoshiNode;
			}
		}

		String nodeContent;

		try {
			nodeContent = Dom4JUtil.format(node);
		}
		catch (IOException ioe) {
			nodeContent = node.toString();
		}

		throw new RuntimeException("Unknown node\n" + nodeContent);
	}

	protected static List<PoshiNode> getPoshiNodes(Node node) {
		if (node instanceof Comment) {
			return getPoshiNodes("PoshiComment");
		}

		if (node instanceof Element) {
			return getPoshiNodes("PoshiElement");
		}

		return null;
	}

	protected static List<PoshiNode> getPoshiNodes(String key) {
		return _poshiNodes.get(key);
	}

	private static final Map<String, List<PoshiNode>> _poshiNodes =
		new HashMap<>();

	static {
		try {
			URL url = PoshiNode.class.getResource(
				PoshiNode.class.getSimpleName() + ".class");

			File poshiNodeClassFile = new File(url.toURI());

			File dir = poshiNodeClassFile.getParentFile();

			List<File> dirFiles = Arrays.asList(dir.listFiles());

			Collections.sort(dirFiles);

			List<PoshiNode> poshiComments = new ArrayList<>();
			List<PoshiNode> poshiElements = new ArrayList<>();

			for (File file : dirFiles) {
				String fileName = file.getName();

				if (fileName.startsWith("Base") ||
					fileName.startsWith("Poshi")) {

					continue;
				}

				Package pkg = PoshiNode.class.getPackage();

				int index = fileName.indexOf(".");

				String className = fileName.substring(0, index);

				Class<?> clazz = Class.forName(pkg.getName() + "." + className);

				PoshiNode poshiNode = (PoshiNode)clazz.newInstance();

				if (poshiNode instanceof PoshiComment) {
					poshiComments.add(poshiNode);

					continue;
				}

				if (poshiNode instanceof PoshiElement) {
					poshiElements.add(poshiNode);
				}
			}

			_poshiNodes.put("PoshiComment", poshiComments);
			_poshiNodes.put("PoshiElement", poshiElements);
		}
		catch (ClassNotFoundException | IllegalAccessException |
			   InstantiationException | URISyntaxException e) {

			throw new RuntimeException(e);
		}
	}

}