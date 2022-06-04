/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes;

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPublicKey;
import ch.post.it.evoting.domain.election.model.messaging.SafeStreamDeserializationException;
import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;
import ch.post.it.evoting.domain.election.model.messaging.StreamSerializableClassType;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableUtil;

public class CCPublicKey implements StreamSerializable {

	private ElGamalPublicKey publicKey;

	private KeyType keytype;

	private byte[] keySignature;

	private X509Certificate signerCertificate;

	private X509Certificate nodeCACertificate;

	public ElGamalPublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(final ElGamalPublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public KeyType getKeytype() {
		return keytype;
	}

	public void setKeytype(final KeyType keytype) {
		this.keytype = keytype;
	}

	public byte[] getKeySignature() {
		return keySignature;
	}

	public void setKeySignature(final byte[] keySignature) {
		this.keySignature = keySignature;
	}

	public X509Certificate getSignerCertificate() {
		return signerCertificate;
	}

	public void setSignerCertificate(final X509Certificate signerCertificate) {
		this.signerCertificate = signerCertificate;
	}

	public X509Certificate getNodeCACertificate() {
		return nodeCACertificate;
	}

	public void setNodeCACertificate(final X509Certificate nodeCACertificate) {
		this.nodeCACertificate = nodeCACertificate;
	}

	@Override
	public void serialize(final MessagePacker packer) throws IOException {

		try {
			StreamSerializableUtil.storeElGamalPublicKeyValueWithNullCheck(packer, publicKey);
			packer.packString(keytype.toString());
			if (keySignature != null) {
				packer.packBinaryHeader(keySignature.length);
				packer.addPayload(keySignature);
			} else {
				packer.packNil();
			}
			StreamSerializableUtil.storeCertificateValueWithNullCheck(packer, signerCertificate);
			StreamSerializableUtil.storeCertificateValueWithNullCheck(packer, nodeCACertificate);
		} catch (final GeneralCryptoLibException e) {
			throw new IOException(e);
		}

	}

	@Override
	public void deserialize(final MessageUnpacker unpacker) throws SafeStreamDeserializationException {
		try {
			this.publicKey = StreamSerializableUtil.retrieveElGamalPublicKeyValueWithNullCheck(unpacker);
			this.keytype = KeyType.valueOf(unpacker.unpackString());
			if (!unpacker.tryUnpackNil()) {
				final int signatureLength = unpacker.unpackBinaryHeader();
				this.keySignature = unpacker.readPayload(signatureLength);
			} else {
				this.keySignature = null;
			}
			this.signerCertificate = StreamSerializableUtil.retrieveCertificateValueWithNullCheck(unpacker);
			this.nodeCACertificate = StreamSerializableUtil.retrieveCertificateValueWithNullCheck(unpacker);
		} catch (final IOException | GeneralCryptoLibException e) {
			throw new SafeStreamDeserializationException(e);
		}
	}

	@Override
	public StreamSerializableClassType type() {
		return StreamSerializableClassType.CC_PUBLIC_KEY;
	}

}
