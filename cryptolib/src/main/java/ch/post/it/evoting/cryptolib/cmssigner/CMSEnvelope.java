/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.cryptolib.cmssigner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.crypto.spec.OAEPParameterSpec;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedDataParser;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSTypedStream;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.operator.jcajce.JceAsymmetricKeyWrapper;

/**
 * Helper to create and decipher CMS envelopes. Uses Bouncy Castle as underlying library.
 */
public class CMSEnvelope {

	private static final int BUFFER_SIZE = 4096;

	/**
	 * Make a CMS envelope: cipher the provided data using a generated secret key and then cipher the secret key with the public key in the provided
	 * certificate. The algorithm used to cipher is aes128-GCM.
	 *
	 * @param data          : The data to encrypt
	 * @param output        : Output stream to the envelope. It is closed always.
	 * @param recipientCert : Certificate of the intended recipient
	 * @throws IOException : If there is an error reading from the input stream or writing to the output stream
	 */
	public void generateEnvelope(final InputStream data, final OutputStream output, final Certificate recipientCert) throws IOException {
		final CMSEnvelopedDataStreamGenerator envelopedDataStreamGenerator = new CMSEnvelopedDataStreamGenerator();
		envelopedDataStreamGenerator.setBufferSize(BUFFER_SIZE);
		try {
			final AlgorithmIdentifier aId = new JceAsymmetricKeyWrapper(OAEPParameterSpec.DEFAULT, recipientCert.getPublicKey()).getAlgorithmIdentifier();
			envelopedDataStreamGenerator.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator((X509Certificate) recipientCert, aId));
		} catch (final CertificateEncodingException cee) {
			throw new IllegalArgumentException("Error parsing the provided certificate.", cee);
		}

		try (final OutputStream envelopedOutput = envelopedDataStreamGenerator
				.open(output, new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_GCM).build())) {
			IOUtils.copyLarge(data, envelopedOutput);
		} catch (final CMSException cmse) {
			throw new IllegalStateException("Error initializing envelope.", cmse);
		}
	}

	/**
	 * Read and decipher a CMS envelope using the recipient's private key. Returns a stream with the deciphered data.
	 *
	 * @param envelopeIs          : Input stream to the envelope for reading.
	 * @param recipientPrivateKey : the private key of the entity intending to decipher the envelope
	 * @return an {@link InputStream} with the deciphered content
	 * @throws IOException  if there are problems reading the envelope.
	 * @throws CMSException if the syntax of the envelop is incorrect.
	 */
	public InputStream openEnvelope(final InputStream envelopeIs, final PrivateKey recipientPrivateKey) throws IOException, CMSException {

		// parse CMS envelope data
		final CMSEnvelopedDataParser envelopedDataParser = new CMSEnvelopedDataParser(new BufferedInputStream(envelopeIs, BUFFER_SIZE));

		// expect exactly one recipient
		final Collection<?> recipients = envelopedDataParser.getRecipientInfos().getRecipients();
		if (recipients.size() != 1) {
			throw new IllegalArgumentException();
		}

		// retrieve recipient and decode it
		final RecipientInformation recipient = (RecipientInformation) recipients.iterator().next();
		final CMSTypedStream contentStream = recipient.getContentStream(new JceKeyTransEnvelopedRecipient(recipientPrivateKey));

		return contentStream.getContentStream();
	}
}
