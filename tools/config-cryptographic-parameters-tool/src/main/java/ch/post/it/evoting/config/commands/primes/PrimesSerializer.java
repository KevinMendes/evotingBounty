/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.primes;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.cms.CMSException;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.cmssigner.CMSSigner;

@Service
public class PrimesSerializer {

	private static final String PRIMES_FILENAME = "primes";

	private static final String CSV_EXTENSION = ".csv";

	private static final String OUTPUT_FOLDER = "output";

	/**
	 * Method that serializes the primes file and signs it.
	 * <p>
	 * Creates the file with: -the list of primes -a timestamp -optional output folder.
	 * <p>
	 * For the signature it needs: -the chain -signer certificate -the private key related to the signer.
	 */
	public void serializePrimes(final List<BigInteger> primes, final String timeStamp, final String optionalOutput, final PrivateKey key,
			final List<Certificate> chain, final Certificate signerCertificate) throws IOException, CMSException {
		Path primesPath = getPrimesPath(timeStamp, optionalOutput);
		FileUtils.writeLines(primesPath.toFile(), primes);
		CMSSigner.sign(primesPath.toFile(), primesPath.resolveSibling(primesPath.getFileName() + CMSSigner.SIGNATURE_FILE_EXTENSION).toFile(),
				signerCertificate, chain, key);
	}

	private Path getPrimesPath(final String timeStamp, final String optionalOutput) {

		String outputPath;
		if (optionalOutput == null) {
			outputPath = OUTPUT_FOLDER;
		} else {
			outputPath = optionalOutput;
		}

		return Paths.get(outputPath, PRIMES_FILENAME + timeStamp + CSV_EXTENSION);
	}
}
