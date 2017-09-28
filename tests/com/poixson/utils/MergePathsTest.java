package com.poixson.utils;

import org.junit.Test;

import junit.framework.TestCase;


public class MergePathsTest extends TestCase {



	@Test
	public void testMergePaths() {
		final String cwd = FileUtils.cwd();
		System.err.println("CWD: "+cwd);
		assertEquals(
			"no args",
			null,
			FileUtils.MergePaths()
		);
		assertEquals(
			"null args",
			null,
			FileUtils.MergePaths(null, null, null)
		);
		assertEquals(
			"blank args",
			null,
			FileUtils.MergePaths("", "", "")
		);
		assertEquals(
			"relative dot file",
			"aaa/bbb/.ccc",
			FileUtils.MergePaths("aaa", "bbb", ".ccc")
		);
		assertEquals(
			"absolute dot file",
			"/aaa/bbb/.ccc",
			FileUtils.MergePaths("/", "aaa", "bbb", ".ccc")
		);
		assertEquals(
			"relative to full path",
			cwd+"/aaa",
			FileUtils.MergePaths(".", "aaa")
		);
		assertEquals(
			"relative to full path overloaded",
			cwd+"/aaa",
			FileUtils.MergePaths(".", ".", "aaa")
		);
		assertEquals(
			"parent path",
			"aaa/c.cc",
			FileUtils.MergePaths("aaa", "bbb", "..", "c.cc")
		);
		assertEquals(
			"parent cwd path",
			cwd+"/aaa",
			FileUtils.MergePaths("./aaa/bbb/ccc/../..")
		);
		assertEquals(
			"parent to nothing",
			"/",
			FileUtils.MergePaths("/..")
		);
		assertEquals(
			"maintain relative",
			"aaa/bbb",
			FileUtils.MergePaths("aaa/", "/bbb/")
		);
	}



}
