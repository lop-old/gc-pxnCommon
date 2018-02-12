package com.poixson.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.poixson.app.Failure;
import com.poixson.utils.StringUtils;
import com.poixson.utils.Utils;


public class AppProps {

	private static final String PROPS_FILE = "/app.properties";

	// property values
	public final String name;
	public final String title;
	public final String titleFull;
	public final String version;
	public final String commitHashFull;
	public final String commitHashShort;
	public final String url;
	public final String orgName;
	public final String orgUrl;
	public final String issueName;
	public final String issueUrl;



	// load app.properties file
	public AppProps(final Class<?> clss) {
		Properties props = null;
		InputStream in = null;
		try {
			props = new Properties();
			in = clss.getResourceAsStream(PROPS_FILE);
			if (in == null) {
				throw new RuntimeException(
					StringUtils.ReplaceTags(
						"Failed to load {} resource from jar",
						PROPS_FILE
					)
				);
			}
			props.load(in);
		} catch (IOException e) {
			Failure.fail(e);
			throw new RuntimeException(e);
		} finally {
			Utils.safeClose(in);
		}
		this.name      = props.getProperty("name");
		this.title     = props.getProperty("title");
		this.version   = props.getProperty("version");
		this.url       = props.getProperty("url");
		this.orgName   = props.getProperty("org_name");
		this.orgUrl    = props.getProperty("org_url");
		this.issueName = props.getProperty("issue_name");
		this.issueUrl  = props.getProperty("issue_url");
		// title version
		this.titleFull =
			(new StringBuilder())
				.append(this.title)
				.append(' ')
				.append(this.version)
				.toString();
		// commit hash
		{
			final String hash = props.getProperty("commit");
			if (Utils.isEmpty(hash)) {
				this.commitHashFull  = null;
				this.commitHashShort = null;
			} else
			if (hash.startsWith("${")) {
				this.commitHashFull  = null;
				this.commitHashShort = null;
			} else {
				this.commitHashFull  = hash;
				this.commitHashShort = hash.substring(0, 7);
			}
		}
	}



}
