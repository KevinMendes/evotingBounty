/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Module.SlotRequirement;
import iaik.pkcs.pkcs11.Module.WaitingBehavior;
import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;

/**
 * This {@link Callable} is tasked with acquiring a reference to the Token. It manages the necessary retries, errors and communication with a GUI.
 */
public class AcquireTokenTask implements Callable<Token> {
	private static final Logger LOGGER = LoggerFactory.getLogger(AcquireTokenTask.class);

	private final Module pkcs11Module;

	private final GuiOutput guiOutput;

	private final boolean retry;

	private boolean abort = false;

	/**
	 * Create the task.
	 *
	 * @param pkcs11Module The module to load the token from.
	 * @param guiOutput    User interface.
	 * @param retry        true if this task has to retry, false otherwise.
	 */
	public AcquireTokenTask(final Module pkcs11Module, final GuiOutput guiOutput, final boolean retry) {
		super();
		this.pkcs11Module = pkcs11Module;
		this.guiOutput = guiOutput;
		this.retry = retry;
	}

	/**
	 * Create the task, with retry.
	 *
	 * @param pkcs11Module The module to load the token from.
	 * @param guiOutput    User interface.
	 */
	public AcquireTokenTask(final Module pkcs11Module, final GuiOutput guiOutput) {
		this(pkcs11Module, guiOutput, true);
	}

	@Override
	public Token call() {
		return acquireToken();
	}

	private Token acquireToken() {
		Token detectedToken = null;
		do {
			final Slot[] slots = acquireSlots();
			switch (slots.length) {
			case 0:
				guiOutput.noTokenPresent();
				break;
			case 1:
				detectedToken = getOneToken(slots[0]);
				if (detectedToken != null) {
					return detectedToken;
				}
				break;
			default:
				guiOutput.tooManyTokens(slots.length);
			}
			if (retry) {
				waitForSlotEvent();
			} else {
				abort();
			}
		} while (!abort);
		// We are here only if the caller asked to abort. Returning the token,
		// but it will be null.
		return detectedToken;
	}

	private Token getOneToken(final Slot slot) {
		Token detectedToken = null;
		try {
			detectedToken = slot.getToken();
			LOGGER.debug("Token acquired : {}", detectedToken);
			return detectedToken;
		} catch (final TokenException te) {
			LOGGER.warn("Error getting token that was already detected.", te);
			guiOutput.noTokenPresent();
		}
		return detectedToken;
	}

	/**
	 * Stop retries. The next attempt to complete will be the last.
	 */
	public void abort() {
		abort = true;
	}

	private Slot[] acquireSlots() {
		final RetriableSharesTask<Slot[]> slotTask = new RetriableSharesTask<>(() -> pkcs11Module.getSlotList(SlotRequirement.TOKEN_PRESENT));
		return slotTask.call();
	}

	private void waitForSlotEvent() {
		final RetriableSharesTask<Slot> waitSlotTask = new RetriableSharesTask<>(() -> pkcs11Module.waitForSlotEvent(WaitingBehavior.BLOCK, null));
		waitSlotTask.call();
	}

}
