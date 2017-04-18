package com.poixson.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class AppProps {

	private static final String PROPS_FILE = "/app.properties";


	// properties
	public final String name;
	public final String title;
	public final String full_title;
	public final String version;
	public final String commitHash;
	public final String url;
	public final String org_name;
	public final String org_url;
	public final String issue_name;
	public final String issue_url;






	// load app.properties file
	public AppProps(final Class<?> clss) {
		Properties props = null;
		InputStream in = null;
		try {
			props = new Properties();
			in = clss.getResourceAsStream(PROPS_FILE);
			if (in == null) {
//TODO:
//				Failure.fail("Failed to load "+PROPS_FILE+" resource from jar");
				throw new RuntimeException();
			}
			props.load(in);
		} catch (IOException e) {
//TODO:
//			xLog.getRoot().trace(e);
		} finally {
			Utils.safeClose(in);
		}
		this.name       = props.getProperty("name");
		this.title      = props.getProperty("title");
		this.version    = props.getProperty("version");
		this.commitHash = props.getProperty("commit");
		this.full_title = this.title+" "+this.version;
		this.url        = props.getProperty("url");
		this.org_name   = props.getProperty("org_name");
		this.org_url    = props.getProperty("org_url");
		this.issue_name = props.getProperty("issue_name");
		this.issue_url  = props.getProperty("issue_url");
	}



}
