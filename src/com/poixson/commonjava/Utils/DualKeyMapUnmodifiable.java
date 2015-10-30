package com.poixson.commonjava.Utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


public class DualKeyMapUnmodifiable<K, J, V> extends DualKeyMap<K, J, V> {



	public DualKeyMapUnmodifiable(final DualKeyMap<K, J, V> map) {
		super(map);
	}
	public DualKeyMapUnmodifiable(final Map<K, V> kMap, final Map<J, V> jMap) {
		super(kMap, jMap);
	}



	@Override
	public Map<K, V> getMapK() {
		return Collections.unmodifiableMap(this.kMap);
	}
	@Override
	public Map<J, V> getMapJ() {
		return Collections.unmodifiableMap(this.jMap);
	}



	@Override
	public void clear() {
		throw new UnsupportedOperationException("Unmodifiable DualKeyMap");
	}
	@Override
	public V remove(final K kKey, final J jKey) {
		throw new UnsupportedOperationException("Unmodifiable DualKeyMap");
	}



	@Override
	public Set<Entry<K, V>> entrySetK() {
		return this.kMap.entrySet();
	}
	@Override
	public Set<Entry<J, V>> entrySetJ() {
		return this.jMap.entrySet();
	}



	@Override
	public Set<K> keySetK() {
		return this.kMap.keySet();
	}
	@Override
	public Set<J> keySetJ() {
		return this.jMap.keySet();
	}



	@Override
	public V put(final K kKey, final J jKey, final V value) {
		throw new UnsupportedOperationException("Unmodifiable DualKeyMap");
	}
	@Override
	public void putAll(final DualKeyMap<K, J, V> map) {
		throw new UnsupportedOperationException("Unmodifiable DualKeyMap");
	}



	@Override
	public Collection<V> values() {
		return this.kMap.values();
	}



}
