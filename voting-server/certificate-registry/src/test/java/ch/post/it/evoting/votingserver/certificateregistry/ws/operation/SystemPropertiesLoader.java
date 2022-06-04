/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.ws.operation;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import org.junit.ClassRule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class SystemPropertiesLoader {

	public static final String FILE_NAME = "arq_system.properties";
	public static final String ABSOLUTEPATH = Paths.get("target").toFile().getAbsolutePath();
	@ClassRule
	public static EnvironmentVariables environmentVariables = new EnvironmentVariables();

	public void setProperties() {
		Properties props = load();
		if (props != null) {
			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				String string = entry.getValue().toString();
				String replace = string.replace("{testFolder}", ABSOLUTEPATH);
				environmentVariables.set(entry.getKey().toString(), replace);
			}
		}
	}

	public void unsetProperties() {
		Properties props = load();
		if (props != null) {
			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				System.clearProperty(entry.getKey().toString());
			}
		}
	}

	public Properties load() {
		try (InputStream propsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(FILE_NAME)) {
			if (propsStream != null) {
				Properties props = new Properties();
				props.load(propsStream);
				return props;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not load properties", e);
		}
	}
}
