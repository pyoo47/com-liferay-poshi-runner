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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Shuyang Zhou
 */
public class CentralizedThreadLocal<T> extends ThreadLocal<T> {

	public static void clearLongLivedThreadLocals() {
		_longLivedThreadLocals.remove();
	}

	public static void clearShortLivedThreadLocals() {
		_shortLivedThreadLocals.remove();
	}

	public static Map<CentralizedThreadLocal<?>, Object>
		getLongLivedThreadLocals() {

		return _toMap(_longLivedThreadLocals.get());
	}

	public static Map<CentralizedThreadLocal<?>, Object>
		getShortLivedThreadLocals() {

		return _toMap(_shortLivedThreadLocals.get());
	}

	public static void setThreadLocals(
		Map<CentralizedThreadLocal<?>, Object> longLivedCentralizedThreadLocals,
		Map<CentralizedThreadLocal<?>, Object>
			shortLivedCentralizedThreadLocals) {

		ThreadLocalMap threadLocalMap = _longLivedThreadLocals.get();

		for (Map.Entry<CentralizedThreadLocal<?>, Object> entry :
				longLivedCentralizedThreadLocals.entrySet()) {

			threadLocalMap.putEntry(entry.getKey(), entry.getValue());
		}

		threadLocalMap = _shortLivedThreadLocals.get();

		for (Map.Entry<CentralizedThreadLocal<?>, Object> entry :
				shortLivedCentralizedThreadLocals.entrySet()) {

			threadLocalMap.putEntry(entry.getKey(), entry.getValue());
		}
	}

	public CentralizedThreadLocal(boolean shortLived) {
		this(null, null, shortLived);
	}

	public CentralizedThreadLocal(String name) {
		this(name, null, true);
	}

	public CentralizedThreadLocal(String name, Object supplier) {
		this(name, supplier, true);
	}

	public CentralizedThreadLocal(
		String name, Object supplier, boolean shortLived) {

		this(name, supplier, null, shortLived);
	}

	public CentralizedThreadLocal(
		String name, Object supplier, Object copyFunction, boolean shortLived) {

		if (shortLived) {
			_hashCode = _shortLivedNextHasCode.getAndAdd(_HASH_INCREMENT);
		}
		else {
			_hashCode = _longLivedNextHasCode.getAndAdd(_HASH_INCREMENT);
		}

		if (name == null) {
			_name = super.toString();
		}
		else {
			_name = name;
		}

		if (supplier == null) {
			_supplier = null;
		}
		else {
			_supplier = supplier;
		}

		if (copyFunction == null) {
			Class<?> clazz = getClass();

			Method function = null;

			try {
				function = clazz.getDeclaredMethod("_copy", Object.class);
			}
			catch (NoSuchMethodException nsme) {
				nsme.printStackTrace();
			}

			_copyFunction = function;
		}
		else {
			_copyFunction = (Method)copyFunction;
		}

		_shortLived = shortLived;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		return false;
	}

	@Override
	public T get() {
		ThreadLocalMap threadLocalMap = _getThreadLocalMap();

		Entry entry = threadLocalMap.getEntry(this);

		if (entry == null) {
			T value = initialValue();

			threadLocalMap.putEntry(this, value);

			return value;
		}

		return (T)entry._value;
	}

	@Override
	public int hashCode() {
		return _hashCode;
	}

	@Override
	public void remove() {
		ThreadLocalMap threadLocalMap = _getThreadLocalMap();

		threadLocalMap.removeEntry(this);
	}

	@Override
	public void set(T value) {
		ThreadLocalMap threadLocalMap = _getThreadLocalMap();

		threadLocalMap.putEntry(this, value);
	}

	@Override
	public String toString() {
		return _name;
	}

	@Override
	protected T initialValue() {
		return (T)_supplier;
	}

	private static Map<CentralizedThreadLocal<?>, Object> _toMap(
		ThreadLocalMap threadLocalMap) {

		Map<CentralizedThreadLocal<?>, Object> map = new HashMap<>(
			threadLocalMap._table.length);

		for (Entry entry : threadLocalMap._table) {
			while (entry != null) {
				try {
					CentralizedThreadLocal<Object> centralizedThreadLocal =
						(CentralizedThreadLocal<Object>)entry._key;

					Object value = centralizedThreadLocal._copyFunction.invoke(
						entry._value);

					if (value != null) {
						map.put(centralizedThreadLocal, value);
					}

					entry = entry._next;
				}
				catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}

		return map;
	}

	private T _copy(T value) {
		if (value != null) {
			Class<?> clazz = value.getClass();

			if (_immutableTypes.contains(clazz)) {
				return value;
			}
		}

		return null;
	}

	private ThreadLocalMap _getThreadLocalMap() {
		if (_shortLived) {
			return _shortLivedThreadLocals.get();
		}
		else {
			return _longLivedThreadLocals.get();
		}
	}

	private static final int _HASH_INCREMENT = 0x61c88647;

	private static final Set<Class<?>> _immutableTypes = new HashSet<>();
	private static final AtomicInteger _longLivedNextHasCode =
		new AtomicInteger();

	private static final ThreadLocal<ThreadLocalMap> _longLivedThreadLocals =
		new ThreadLocal<ThreadLocalMap>() {

			@Override
			protected ThreadLocalMap initialValue() {
				return new ThreadLocalMap();
			}

		};

	private static final AtomicInteger _shortLivedNextHasCode =
		new AtomicInteger();

	private static final ThreadLocal<ThreadLocalMap> _shortLivedThreadLocals =
		new ThreadLocal<ThreadLocalMap>() {

			@Override
			protected ThreadLocalMap initialValue() {
				return new ThreadLocalMap();
			}

		};

	static {
		_immutableTypes.add(Boolean.class);
		_immutableTypes.add(Byte.class);
		_immutableTypes.add(Character.class);
		_immutableTypes.add(Short.class);
		_immutableTypes.add(Integer.class);
		_immutableTypes.add(Long.class);
		_immutableTypes.add(Float.class);
		_immutableTypes.add(Double.class);
		_immutableTypes.add(String.class);
	}

	private final Method _copyFunction;
	private final int _hashCode;
	private final String _name;
	private final boolean _shortLived;
	private final Object _supplier;

	private static class Entry {

		public Entry(CentralizedThreadLocal<?> key, Object value, Entry next) {
			_key = key;
			_value = value;
			_next = next;
		}

		private CentralizedThreadLocal<?> _key;
		private Entry _next;
		private Object _value;

	}

	private static class ThreadLocalMap {

		public void expand(int newCapacity) {
			if (newCapacity == (_MAXIMUM_CAPACITY * 2)) {
				_threshold = Integer.MAX_VALUE;

				return;
			}

			Entry[] newTable = new Entry[newCapacity];

			for (int i = 0; i < _table.length; i++) {
				Entry entry = _table[i];

				if (entry == null) {
					continue;
				}

				_table[i] = null;

				do {
					Entry nextEntry = entry._next;

					int index = entry._key._hashCode & (newCapacity - 1);

					entry._next = newTable[index];

					newTable[index] = entry;

					entry = nextEntry;
				}
				while (entry != null);
			}

			_table = newTable;

			_threshold = newCapacity * 2 / 3;
		}

		public Entry getEntry(CentralizedThreadLocal<?> key) {
			int index = key._hashCode & (_table.length - 1);

			Entry entry = _table[index];

			if (entry == null) {
				return null;
			}

			if (entry._key == key) {
				return entry;
			}

			while ((entry = entry._next) != null) {
				if (entry._key == key) {
					return entry;
				}
			}

			return null;
		}

		public void putEntry(CentralizedThreadLocal<?> key, Object value) {
			int index = key._hashCode & (_table.length - 1);

			for (Entry entry = _table[index]; entry != null;
				entry = entry._next) {

				if (entry._key == key) {
					entry._value = value;

					return;
				}
			}

			_table[index] = new Entry(key, value, _table[index]);

			if (_size++ >= _threshold) {
				expand(2 * _table.length);
			}
		}

		public void removeEntry(CentralizedThreadLocal<?> key) {
			int index = key._hashCode & (_table.length - 1);

			Entry previousEntry = null;

			Entry entry = _table[index];

			while (entry != null) {
				Entry nextEntry = entry._next;

				if (entry._key == key) {
					_size--;

					if (previousEntry == null) {
						_table[index] = nextEntry;
					}
					else {
						previousEntry._next = nextEntry;
					}

					return;
				}

				previousEntry = entry;
				entry = nextEntry;
			}
		}

		private static final int _INITIAL_CAPACITY = 16;

		private static final int _MAXIMUM_CAPACITY = 1 << 30;

		private int _size;
		private Entry[] _table = new Entry[_INITIAL_CAPACITY];
		private int _threshold = _INITIAL_CAPACITY * 2 / 3;

	}

}