/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.signature;

import static java.util.Objects.requireNonNull;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Signature;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.ArrayList;
import java.util.Collection;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.ConfigDigitalSignerAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicyFromProperties;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.PaddingInfo;

/**
 * Implementation of {@link SignatureFactory} which uses the same provider and algorithm which uses implementation of {@link AsymmetricServiceAPI}.
 */
public final class SignatureFactoryImpl implements SignatureFactory {
	private final String algorithm;

	private final Provider provider;

	private final AlgorithmParameterSpec[] parameters;

	private SignatureFactoryImpl(final String algorithm, final Provider provider, final AlgorithmParameterSpec[] parameters) {
		this.algorithm = algorithm;
		this.provider = provider;
		this.parameters = parameters;
	}

	/**
	 * Returns a new instance which uses the algorithm and provider defined in the current {@code cryptolibPolicy.properties}.
	 *
	 * @return the instance.
	 */
	public static SignatureFactoryImpl newInstance() {
		return newInstance(getDigitalSignerPolicy());
	}

	/**
	 * Returns a new instance which uses the specified algorithm and provider.
	 *
	 * @param spec the specification.
	 * @return the instance.
	 */
	public static SignatureFactoryImpl newInstance(final ConfigDigitalSignerAlgorithmAndSpec spec) {
		requireNonNull(spec, "Specification is null.");
		Signature signature = newSignature(spec);
		AlgorithmParameterSpec[] parameters = getParameters(spec, signature);
		return new SignatureFactoryImpl(signature.getAlgorithm(), signature.getProvider(), parameters);
	}

	/**
	 * Returns a new instance which uses the algorithm and provider defined by a given policy.
	 *
	 * @param policy the policy
	 * @return return the instance.
	 */
	public static SignatureFactoryImpl newInstance(final DigitalSignerPolicy policy) {
		requireNonNull(policy, "Policy is null.");
		return newInstance(policy.getDigitalSignerAlgorithmAndSpec());
	}

	private static void checkParameters(final AlgorithmParameterSpec[] parameters, final Signature signature) {
		try {
			for (AlgorithmParameterSpec parameter : parameters) {
				signature.setParameter(parameter);
			}
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalStateException("Invalid signature parameters.", e);
		}
	}

	private static DigitalSignerPolicyFromProperties getDigitalSignerPolicy() {
		return new DigitalSignerPolicyFromProperties();
	}

	private static AlgorithmParameterSpec[] getParameters(final ConfigDigitalSignerAlgorithmAndSpec spec, final Signature signature) {
		AlgorithmParameterSpec[] parameters = newParameters(spec);
		checkParameters(parameters, signature);
		return parameters;
	}

	private static AlgorithmParameterSpec[] newParameters(final ConfigDigitalSignerAlgorithmAndSpec spec) {
		Collection<AlgorithmParameterSpec> parameters = new ArrayList<>();
		PaddingInfo padding = spec.getPaddingInfo();
		if (padding == PaddingInfo.PSS_PADDING_INFO) {
			PSSParameterSpec parameter = new PSSParameterSpec(spec.getPaddingMessageDigestAlgorithm(),
					padding.getPaddingMaskingGenerationFunctionAlgorithm(),
					new MGF1ParameterSpec(padding.getPaddingMaskingGenerationFunctionMessageDigestAlgorithm()), padding.getPaddingSaltBitLength(),
					padding.getPaddingTrailerField());
			parameters.add(parameter);
		}
		return parameters.toArray(new AlgorithmParameterSpec[parameters.size()]);
	}

	private static Signature newSignature(final ConfigDigitalSignerAlgorithmAndSpec spec) {
		String algorithm = spec.getAlgorithmAndPadding();
		ch.post.it.evoting.cryptolib.commons.configuration.Provider provider = spec.getProvider();
		Signature signature;
		try {
			if (provider == ch.post.it.evoting.cryptolib.commons.configuration.Provider.DEFAULT) {
				signature = Signature.getInstance(algorithm);
			} else {
				signature = Signature.getInstance(algorithm, provider.getProviderName());
			}
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// cryptolib policy is expected to be correct
			throw new IllegalStateException("Failed to create signature.", e);
		}
		return signature;
	}

	@Override
	public Signature newSignature() {
		Signature signature;
		try {
			signature = Signature.getInstance(algorithm, provider);
			for (AlgorithmParameterSpec parameter : parameters) {
				signature.setParameter(parameter);
			}
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
			// correctness of the algorithm and provider and parameters is
			// already tested
			throw new IllegalStateException("Failed to create signature.", e);
		}
		return signature;
	}
}
