/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

/**
 * Interface to get messages from the token processing. The implementations must take care of message to use, presentation, screen updating, etc. This
 * interface is not forcing any pattern, so implementations can choose what fits best.
 */
public interface GuiOutput {

	/**
	 * Signals the GUI there is no readable token present.
	 */
	void noTokenPresent();

	/**
	 * Signals the GUI there is more than one token present.
	 *
	 * @param nTokens The number of tokens present.
	 */
	void tooManyTokens(final int nTokens);

}
