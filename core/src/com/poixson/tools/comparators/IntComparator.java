package com.poixson.tools.comparators;

import java.util.Comparator;

import com.poixson.exceptions.RequiredArgumentException;


public class IntComparator implements Comparator<Integer> {

	protected final boolean reverse;



	public IntComparator() {
		this(false);
	}
	public IntComparator(final boolean reverse) {
		this.reverse = reverse;
	}



	@Override
	public int compare(final Integer valA, final Integer valB) {
		if (valA == null) throw new RequiredArgumentException("valA");
		if (valB == null) throw new RequiredArgumentException("valB");
		if (this.reverse)
			return valB.intValue() - valA.intValue();
		return valA.intValue() - valB.intValue();
	}



}
