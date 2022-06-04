/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.TokenException;

/**
 * Singleton manager to access the {@link Module}. This eases working with the same {@link Module} across operations.
 */
@SuppressWarnings("java:S2139")
//during the static initialization, an error can be thrown (not an exception) that is not handled and logged correctly by the Spring exceptionHandler
public final class ModuleManager {

	public static final ModuleManager INSTANCE;
	private static final Logger LOGGER = LoggerFactory.getLogger(ModuleManager.class);

	static {
		try {
			INSTANCE = new ModuleManager();
		} catch (final FileNotFoundException e) {
			LOGGER.error("Unable to initialize the ModuleManager", e);
			throw new ExceptionInInitializerError(e);
		}
	}

	private Module pkcs11Module;

	private ModuleManager() throws FileNotFoundException {
		init();
	}

	public Module getModule() {
		return pkcs11Module;
	}

	private void init() throws FileNotFoundException {
		final Module module;
		try {
			LOGGER.info("Loading the PKCS11 native library");
			final File libName = LibraryLoader.loadPkcs11Library();
			module = Module.getInstance(libName.getAbsolutePath(), false);
			module.initialize(null);
			this.pkcs11Module = module;
		} catch (final FileNotFoundException e) {
			throw e;
		} catch (final IOException ioe) {
			throw new IllegalStateException("Error finding the pkcs11wrapper library", ioe);
		} catch (final TokenException te) {
			throw new IllegalStateException("Error initializing the pkcs11 module", te);
		}
	}

	/**
	 * Clean references to the underlying {@link Module}.
	 */
	public void clean() {
		// always finalize
		try {
			pkcs11Module.finalize(null);
		} catch (final TokenException te) {
			LOGGER.info("Error while finalizing pkcs11 module", te);
		}
	}
}
