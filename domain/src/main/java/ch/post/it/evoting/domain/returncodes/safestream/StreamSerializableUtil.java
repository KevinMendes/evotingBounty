/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes.safestream;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;

import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPublicKey;
import ch.post.it.evoting.domain.election.model.messaging.SafeStreamDeserializationException;
import ch.post.it.evoting.domain.election.model.messaging.StreamSerializableClassType;

public class StreamSerializableUtil {

	private StreamSerializableUtil() {
		// Intentionally left blank.
	}

	public static Object resolveByName(String classId) throws SafeStreamDeserializationException {
		try {
			Class<?> resolvedClass = Class.forName(StreamSerializableClassType.valueOf(classId).getClassName());
			return resolvedClass.newInstance();
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			throw new SafeStreamDeserializationException(e);
		}
	}

	public static void storeStringValueWithNullCheck(MessagePacker packer, String value) throws IOException {
		if (value == null) {
			packer.packNil();
		} else {
			packer.packString(value);
		}
	}

	public static String retrieveStringValueWithNullCheck(MessageUnpacker unpacker) throws IOException {
		return unpacker.tryUnpackNil() ? null : unpacker.unpackString();
	}

	public static void storeIntValueWithNullCheck(MessagePacker packer, Integer value) throws IOException {
		if (value == null) {
			packer.packNil();
		} else {
			packer.packInt(value);
		}
	}

	public static Integer retrieveIntValueWithNullCheck(MessageUnpacker unpacker) throws IOException {
		return unpacker.tryUnpackNil() ? null : unpacker.unpackInt();
	}

	public static void storeBigIntegerValueWithNullCheck(MessagePacker packer, BigInteger value) throws IOException {
		if (value == null) {
			packer.packNil();
		} else {
			// MsgPack has a size limitation on the BigInteger (2^64-1)
			packer.packString(value.toString());
		}
	}

	public static BigInteger retrieveBigIntegerValueWithNullCheck(MessageUnpacker unpacker) throws IOException {
		// MsgPack has a size limitation on the BigInteger (2^64-1)
		return unpacker.tryUnpackNil() ? null : new BigInteger(unpacker.unpackString());
	}

	public static void storeDateValueWithNullCheck(MessagePacker packer, ZonedDateTime value) throws IOException {
		if (value == null) {
			packer.packNil();
		} else {
			packer.packString(value.toString());
		}
	}

	public static ZonedDateTime retrieveDateValueWithNullCheck(MessageUnpacker unpacker) throws IOException {
		return unpacker.tryUnpackNil() ? null : ZonedDateTime.parse(unpacker.unpackString());
	}

	public static void storeElGamalPublicKeyValueWithNullCheck(final MessagePacker packer, final ElGamalPublicKey value) throws IOException {
		if (value == null) {
			packer.packNil();
		} else {
			try {
				packer.packString(value.toJson());
			} catch (GeneralCryptoLibException e) {
				throw new IOException(e);
			}
		}
	}

	public static ElGamalPublicKey retrieveElGamalPublicKeyValueWithNullCheck(final MessageUnpacker unpacker) throws IOException {
		if (unpacker.tryUnpackNil()) {
			return null;
		} else {
			try {
				return ElGamalPublicKey.fromJson(unpacker.unpackString());
			} catch (GeneralCryptoLibException e) {
				throw new IOException(e);
			}
		}
	}

	public static void storeCertificateValueWithNullCheck(MessagePacker packer, X509Certificate value) throws IOException, GeneralCryptoLibException {
		if (value == null) {
			packer.packNil();
		} else {
			packer.packString(PemUtils.certificateToPem(value));
		}
	}

	public static X509Certificate retrieveCertificateValueWithNullCheck(MessageUnpacker unpacker) throws IOException, GeneralCryptoLibException {
		return unpacker.tryUnpackNil() ? null : (X509Certificate) PemUtils.certificateFromPem(unpacker.unpackString());
	}
}
