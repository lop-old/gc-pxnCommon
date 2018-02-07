package com.poixson.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.poixson.app.Failure;
import com.poixson.logger.xLogRoot;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class AppProps {

	private static final String PROPS_FILE = "/app.properties";

	// property values
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
				final String msg =
					StringUtils.ReplaceTags(
						"Failed to load {} resource from jar",
						PROPS_FILE
					);
				final RuntimeException e = new RuntimeException(msg);
				Failure.fail(msg, e);
				throw(e);
			}
			props.load(in);
		} catch (IOException e) {
			xLogRoot.get().trace(e);
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
