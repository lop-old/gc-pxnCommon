package com.poixson.commonjava.Utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.poixson.commonjava.xLogger.xLog;


public class DualKeyMap<K, J, V> {

	protected final Map<K, V> kMap;
	protected final Map<J, V> jMap;



	public DualKeyMap(final DualKeyMap<K, J, V> map) {
		this.kMap = new LinkedHashMap<K, V>();
		this.jMap = new LinkedHashMap<J, V>();
		this.putAll(map);
	}
	public DualKeyMap(final Map<K, V> kMap, final Map<J, V> jMap) {
		this.kMap = kMap;
		this.jMap = jMap;
	}



	public Map<K, V> getMapK() {
		return this.kMap;
	}
	public Map<J, V> getMapJ() {
		return this.jMap;
	}



	public int size() {
		return this.kMap.size();
	}



	public void clear() {
		this.kMap.clear();
		this.jMap.clear();
	}
	public V remove(final K kKey, final J jKey) {
		final V resultK = this.kMap.remove(kKey);
		final V resultJ = this.jMap.remove(jKey);
		if(resultK != null)
			return resultK;
		return resultJ;
	}



	public boolean containsKeyK(final K kKey) {
		return this.kMap.containsKey(kKey);
	}
	public boolean containsKeyJ(final J jKey) {
		return this.jMap.containsKey(jKey);
	}
	public boolean containsValue(final V value) {
		return this.kMap.containsValue(value);
	}



	public Set<Entry<K, V>> entrySetK() {
		return this.kMap.entrySet();
	}
	public Set<Entry<J, V>> entrySetJ() {
		return this.jMap.entrySet();
	}



	public V getK(final K kKey) {
		return this.kMap.get(kKey);
	}
	public V getJ(final J jKey) {
		return this.jMap.get(jKey);
	}



	public boolean isEmpty() {
		return this.kMap.isEmpty();
	}



	public Set<K> keySetK() {
		return this.kMap.keySet();
	}
	public Set<J> keySetJ() {
		return this.jMap.keySet();
	}



	public V put(final K kKey, final J jKey, final V value) {
		this.kMap.put(kKey, value);
		this.jMap.put(jKey, value);
		return value;
	}
	public void putAll(final DualKeyMap<K, J, V> map) {
		final Iterator<Entry<K, V>> itK = this.kMap.entrySet().iterator();
		final Iterator<Entry<J, V>> itJ = this.jMap.entrySet().iterator();
		while(itK.hasNext() && itJ.hasNext()) {
			final Entry<K, V> entryK = itK.next();
			final Entry<J, V> entryJ = itJ.next();
			if(!entryK.getValue().equals(entryJ.getValue())) {
				xLog.getRoot().severe("Missmatched values in DualKeyMap object! [ "+
						entryK.getKey().toString()+" : "+entryK.getValue().toString()+" ] != [ "+
						entryJ.getKey().toString()+" : "+entryJ.getValue().toString()+" ] ");
				continue;
			}
			this.put(
				entryK.getKey(),
				entryJ.getKey(),
				entryK.getValue()
			);
		}
	}



	public Collection<V> values() {
		return this.kMap.values();
	}



}
