/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const objs = require('./objs');

/**
 * @name options
 * @description The cryptographic policy options.
 * @global
 *
 * @property {object} mathematical.groups - Policy for the mathematical
 *           groups.
 * @property {object} mathematical.groups.type - Possible types for the
 *           groups.
 * @property {string} mathematical.groups.type.ZP_2048_256 - 'ZP_2048_256' (only for testing)
 * @property {string} mathematical.groups.type.ZP_2048_224 - 'ZP_2048_224' (only for testing)
 * @property {string} mathematical.groups.type.QR_2048 - 'QR_2048'
 * @property {string} mathematical.groups.type.QR_3072 - 'QR_3072'
 * @property {object} primitives.securerandom - Policy for generating
 *           message digests.
 * @property {object} primitives.messageDigest.algorithm - Hash algorithm.
 * @property {string} primitives.messageDigest.algorithm.SHA256 - 'SHA256'
 * @property {string} primitives.messageDigest.algorithm.SHA512_224
 *           -'SHA512/224'
 *
 * @property {object} primitives.keyDerivation - Policy for deriving keys.
 * @property {object} primitives.keyDerivation.pbkdf - Password-based key
 *           derivation function (PBKDF)
 * @property {object} primitives.keyDerivation.pbkdf.keyLengthBytes - Key
 *           length, in bytes.
 * @property {number} primitives.keyDerivation.pbkdf.keyLengthBytes.KL_16 -
 *           16
 * @property {number} primitives.keyDerivation.pbkdf.keyLengthBytes.KL_32 -
 *           32
 * @property {object} primitives.keyDerivation.pbkdf.minSaltLengthBytes -
 *           Minimum salt length, in bytes.
 * @property {number}
 *           primitives.keyDerivation.pbkdf.minSaltLengthBytes.SL_20 - 20.
 * @property {number}
 *           primitives.keyDerivation.pbkdf.minSaltLengthBytes.SL_32 - 32
 * @property {object} primitives.keyDerivation.pbkdf.hashAlgorithm - Hash
 *           algorithm.
 * @property {string} primitives.keyDerivation.pbkdf.hashAlgorithm.SHA256 -
 *           'SHA256'
 * @property {string}
 *           primitives.keyDerivation.pbkdf.hashAlgorithm.SHA512_224 -
 *           'SHA512/224'
 * @property {object} primitives.keyDerivation.pbkdf.minPasswordLength - The
 *           minimum length of the password.
 * @property {string} primitives.keyDerivation.pbkdf.maxPasswordLength - The
 *           maximum length of the password.
 * @property {object} primitives.keyDerivation.pbkdf.numIterations - Number
 *           of iterations.
 * @property {number} primitives.keyDerivation.pbkdf.I_1 - 1
 * @property {number} primitives.keyDerivation.pbkdf.I_8000 - 8000
 * @property {number} primitives.keyDerivation.pbkdf.I_16000 - 16000
 * @property {number} primitives.keyDerivation.pbkdf.I_32000 - 32000
 * @property {number} primitives.keyDerivation.pbkdf.I_64000 - 64000
 *
 * @property {object} symmetric.secretkey - Policy for generating secret
 *           keys.
 * @property {object} symmetric.secretkey.encryption - Secret key for
 *           encryption.
 * @property {object} symmetric.secretKey.encryption.lengthBytes - Key
 *           length, in bytes.
 * @property {number} symmetric.secretKey.encryption.lengthBytes.KL_16 - 16
 * @property {number} symmetric.secretKey.encryption.lengthBytes.KL_32 - 32
 * @property {object} symmetric.secretkey.mac - Secret key for MAC
 *           generation.
 * @property {object} symmetric.secretKey.mac.lengthBytes - Key length, in
 *           bytes.
 * @property {number} symmetric.secretKey.mac.lengthBytes.KL_16 - 16
 * @property {number} symmetric.secretKey.mac.lengthBytes.KL_32 - 32
 *
 * @property {object} symmetric.cipher - Policy for symmetrically encrypting
 *           and decrypting data.
 * @property {object} symmetric.cipher.algorithm - Cipher algorithm.
 * @property {object} symmetric.cipher.algorithm.AES_GCM - AES-GCM
 *           algorithm.
 * @property {string} symmetric.cipher.algorithm.AES_GCM.name - 'AES-GCM'
 * @property {object} symmetric.cipher.algorithm.AES_GCM.keyLengthBytes -
 *           Key length, in bytes.
 * @property {number}
 *           symmetric.cipher.algorithm.AES_GCM.keyLengthBytes.KL_16 - 16
 * @property {number}
 *           symmetric.cipher.algorithm.AES_GCM.keyLengthBytes.KL_32 - 32
 * @property {object} symmetric.cipher.algorithm.AES_GCM.tagLengthBytes -
 *           Tag length, in bytes.
 * @property {number}
 *           symmetric.cipher.algorithm.AES_GCM.tagLengthBytes.TL_16 - 16
 * @property {number}
 *           symmetric.cipher.algorithm.AES_GCM.tagLengthBytes.TL_32 - 32
 * @property {object} symmetric.cipher.ivLengthBytes - Initialization vector
 *           length, in bytes.
 * @property {number} symmetric.cipher.ivLengthBytes.IVL_12 - 12
 * @property {number} symmetric.cipher.ivLengthBytes.IVL_16 - 16
 * @property {number} symmetric.cipher.ivLengthBytes.IVL_32 - 32
 * @property {number} symmetric.cipher.ivLengthBytes.IVL_64 - 64
 *
 * @property {object} symmetric.mac - Policy for generating MAC's.
 * @property {object} symmetric.mac.hashAlgorithm - Hash algorithm.
 * @property {string} symmetric.mac.hashAlgorithm.SHA256 = 'SHA256'
 * @property {string} symmetric.mac.hashAlgorithm.SHA512_224 = 'SHA512/224'
 *
 * @property {object} asymmetric.keyPair - Policy for generating key pairs.
 * @property {object} asymmetric.keyPair.encryption - Encryption key pair.
 * @property {object} asymmetric.keyPair.encryption.algorithm - Key pair
 *           generation algorithm.
 * @property {string} asymmetric.keyPair.encryption.algorithm.RSA - 'RSA'.
 * @property {object} asymmetric.keyPair.encryption.keyLengthBits - Key
 *           length, in bits.
 * @property {number} asymmetric.keyPair.encryption.keyLengthBits.KL_2048 -
 *           2048
 * @property {number} asymmetric.keyPair.encryption.keyLengthBits.KL_3072 -
 *           3072
 * @property {number} asymmetric.keyPair.encryption.keyLengthBits.KL_4096 -
 *           4096
 * @property {object} asymmetric.keyPair.encryption.publicExponent - Public
 *           exponent.
 * @property {number} asymmetric.keyPair.encryption.publicExponent.F4 -
 *           65537
 *
 * @property {object} asymmetric.signer - Policy for signing data and
 *           verifying signatures.
 * @property {object} asymmetric.signer.algorithm - Signer algorithm.
 * @property {string} asymmetric.signer.algorithm.RSA - 'RSA'
 * @property {object} asymmetric.signer.hashAlgorithm - Hash algorithm.
 * @property {string} asymmetric.signer.hashAlgorithm.SHA256 - SHA256'.
 * @property {string} asymmetric.signer.hashAlgorithm.SHA256_224 -
 *           'SHA512/224'
 * @property {object} asymmetric.signer.padding - Padding.
 * @property {object} asymmetric.signer.padding.PSS - PSS padding.
 * @property {string} asymmetric.signer.padding.PSS.name - 'PSS'
 * @property {object} asymmetric.signer.padding.PSS.hashAlgorithm - Hash
 *           algorithm.
 * @property {string} asymmetric.signer.padding.hashAlgorithm.SHA256 -
 *           'SHA256'
 * @property {string} asymmetric.signer.padding.hashAlgorithm.SHA256_224 -
 *           'SHA512/224'
 * @property {object} asymmetric.signer.padding.PSS.saltLengthBytes - Salt
 *           length, in bytes.
 * @property {number} asymmetric.signer.padding.PSS.saltLengthBytes.SL_32 -
 *           32
 * @property {number} asymmetric.signer.padding.PSS.saltLengthBytes.SL_64 -
 *           64
 * @property {object} asymmetric.signer.padding.PSS.maskGenerator - Mask
 *           generation function.
 * @property {object} asymmetric.signer.padding.PSS.maskGenerator.MGF1 -
 *           MGF1 mask generation algorithm.
 * @property {string} asymmetric.signer.padding.PSS.maskGenerator.MGF1.name -
 *           'MGF1'
 * @property {object}
 *           asymmetric.signer.padding.PSS.maskGenerator.MGF1.hashAlgorithm -
 *           Hash algorithm.
 * @property {string}
 *           asymmetric.signer.padding.PSS.maskGenerator.MGF1.hashAlgorithm.SHA256
 *           -'SHA256'
 * @property {string}
 *           asymmetric.signer.padding.PSS.maskGenerator.MGF1.hashAlgorithm.SHA256_224
 * - 'SHA512/224'
 * @property {object} asymmetric.signer.publicExponent - Public exponent.
 * @property {number} asymmetric.signer.publicExponent.F4 - 65537
 *
 * @property {object} asymmetric.cipher - Policy for asymmetrically
 *           encrypting and decrypting data.
 * @property {object} asymmetric.cipher.algorithm - Cipher algorithm.
 * @property {object} asymmetric.cipher.algorithm.RSA_OAEP - RSA-OAEP
 *           algorithm.
 * @property {string} asymmetric.cipher.algorithm.RSA_OAEP.name. -
 *           'RSA-OAEP'.
 * @property {object} asymmetric.cipher.algorithm.RSA_OAEP.hashAlgorithm -
 *           Hash algorithm.
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_OAEP.hashAlgorithm.SHA256 -
 *           'SHA-256'
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_OAEP.hashAlgorithm.SHA512_224 -
 *           'SHA512/224'
 * @property {object} asymmetric.cipher.algorithm.RSA_OAEP.maskGenerator -
 *           Mask generation function.
 * @property {object}
 *           asymmetric.cipher.algorithm.RSA_OAEP.maskGenerator.MGF1 - MGF1
 *           mask generation function.
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_OAEP.maskGenerator.MGF1.name -
 *           'MGF1'
 * @property {object}
 *           asymmetric.cipher.algorithm.RSA_OAEP.maskGenerator.MGF1.hashAlgorithm
 * - Hash algorithm.
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_OAEP.maskGenerator.MGF1.hashAlgorithm.SHA256
 * - 'SHA256'
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_OAEP.maskGenerator.MGF1.hashAlgorithm.SHA512_224
 * - 'SHA512/224'
 * @property {object} asymmetric.cipher.algorithm.RSA_KEM - RSA-KEM
 *           algorithm.
 * @property {string} asymmetric.cipher.algorithm.RSA_KEM.name - 'RSA-KEM'
 * @property {object} asymmetric.cipher.algorithm.RSA_KEM.hashAlgorithm -
 *           Hash algorithm.
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_KEM.hashAlgorithm.SHA256 -
 *           'SHA256'
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_KEM.hashAlgorithm.SHA512_224 -
 *           'SHA512/224'
 * @property {object}
 *           asymmetric.cipher.algorithm.RSA_KEM.secretKeyLengthBytes -
 *           Secret key length, in bytes.
 * @property {number}
 *           asymmetric.cipher.algorithm.RSA_KEM.secretKeyLengthBytes.KL_16 -
 *           16
 * @property {number}
 *           asymmetric.cipher.algorithm.RSA_KEM.secretKeyLengthBytes.KL_24 -
 *           24
 * @property {number}
 *           asymmetric.cipher.algorithm.RSA_KEM.secretKeyLengthBytes.KL_32 -
 *           32
 * @property {object} asymmetric.cipher.algorithm.RSA_KEM.ivLengthBytes -
 *           Initialization vector length, in bytes.
 * @property {number}
 *           asymmetric.cipher.algorithm.RSA_KEM.ivLengthBytes.IVL_12 - 12
 * @property {number}
 *           asymmetric.cipher.algorithm.RSA_KEM.ivLengthBytes.IVL_16 - 16
 * @property {number}
 *           asymmetric.cipher.algorithm.RSA_KEM.ivLengthBytes.IVL_32 - 32
 * @property {number}
 *           asymmetric.cipher.algorithm.RSA_KEM.ivLengthBytes.IVL_64 - 64
 * @property {object} asymmetric.cipher.algorithm.RSA_KEM.tagLengthBytes
 *           -Tag length, in bytes.
 * @property {object}
 *           asymmetric.cipher.algorithm.RSA_KEM.tagLengthBytes.TL_12 - 12
 * @property {object}
 *           asymmetric.cipher.algorithm.RSA_KEM.tagLengthBytes.TL_16 - 16
 * @property {object}
 *           asymmetric.cipher.algorithm.RSA_KEM.tagLengthBytes.TL_32 - 32
 * @property {object}
 *           asymmetric.cipher.algorithm.RSA_KEM.tagLengthBytes.TL_64 - 64
 * @property {object} asymmetric.cipher.algorithm.RSA_KEM.keyDeriver - Key
 *           deriver
 * @property {object} asymmetric.cipher.algorithm.RSA_KEM.keyDeriver.name -
 *           Key deriver name.
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_KEM.keyDeriver.name.KDF1 -
 *           'KDF1'
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_KEM.keyDeriver.name.KDF2 -
 *           'KDF2'
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_KEM.keyDeriver.name.MGF1 -
 *           'MGF1'
 * @property {object}
 *           asymmetric.cipher.algorithm.RSA_KEM.keyDeriver.hashAlgorithm -
 *           Hash function.
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_KEM.keyDeriver.hashAlgorithm.SHA256
 * - 'SHA256'
 * @property {string}
 *           asymmetric.cipher.algorithm.RSA_KEM.keyDeriver.hashAlgorithm.SHA512_224
 * - 'SHA512/224'
 *
 * @property {object} asymmetric.xmlSigner - Policy for signing and verifying
 * XML signatures.
 * @property {object} asymmetric.xmlSigner.signer - Policy for signing data and
 * verifying signatures.
 * @property {object} asymmetric.xmlSigner.signer.algorithm - Signer algorithm.
 * @property {string} asymmetric.xmlSigner.signer.algorithm.RSA - 'RSA'
 * @property {object} asymmetric.xmlSigner.signer.hashAlgorithm - Hash
 * algorithm.
 * @property {string} asymmetric.xmlSigner.signer.hashAlgorithm.SHA256 -
 * SHA256'.
 * @property {string} asymmetric.xmlSigner.signer.hashAlgorithm.SHA256_224 -
 * 'SHA512/224'
 * @property {object} asymmetric.xmlSigner.signer.publicExponent - Public
 * exponent.
 * @property {number} asymmetric.xmlSigner.signer.publicExponent.F4 - 65537
 * @property {object} asymmetric.xmlSigner.canonicalizationAlgorithm -
 * Canonicalization algorithm.
 * @property {string}
 * asymmetric.xmlSigner.canonicalizationAlgorithm.XML_EXC_C14N -
 * 'http://www.w3.org/2001/10/xml-exc-c14n#'
 * @property {object} asymmetric.xmlSigner.transformAlgorithm - Transform
 * algorithm.
 * @property {string}
 * asymmetric.xmlSigner.transformAlgorithm.ENVELOPED_SIGNATURE -
 * 'http://www.w3.org/2000/09/xmldsig#enveloped-signature'
 *
 * @property {object} proofs - Policy for generating zero-knowledge proofs.
 * @property {object} proofs.messageDigest - Policy for generating message
 *           digests.
 */
module.exports = objs.freeze(objs.leanCopy({
    mathematical: {
        groups: {
            type: {
                ZP_2048_256: 'ZP_2048_256', // only used for testing (faster execution than the QR groups)
                ZP_2048_224: 'ZP_2048_224', // only used for testing (faster execution than the QR groups)
                QR_2048: 'QR_2048',
                QR_3072: 'QR_3072'
            }
        }
    },
    primitives: {
        messageDigest: {
            algorithm: {
                SHA256: 'SHA256',
                SHA512_224: 'SHA512/224' // NOSONAR Rule javascript:S1192 - False positive
            },
        },
        keyDerivation: {
            pbkdf: {
                keyLengthBytes: {KL_16: 16, KL_32: 32},
                minSaltLengthBytes: {SL_20: 20, SL_32: 32},
                hashAlgorithm: {SHA256: 'SHA256'},
                minPasswordLength: 16,
                maxPasswordLength: 1000,
                numIterations: {
                    I_1: 1,
                    I_8000: 8000,
                    I_16000: 16000,
                    I_32000: 32000,
                    I_64000: 64000
                }
            }
        }
    },
    symmetric: {
        secretKey: {
            encryption: {lengthBytes: {KL_16: 16, KL_32: 32}},
            mac: {lengthBytes: {KL_32: 32, KL_64: 64}},
        },
        cipher: {
            algorithm: {
                AES_GCM: {
                    name: 'AES-GCM',
                    keyLengthBytes: {KL_16: 16, KL_32: 32},
                    tagLengthBytes: {TL_16: 16, TL_32: 32}
                }
            },
            ivLengthBytes: {IVL_12: 12, IVL_16: 16, IVL_32: 32, IVL_64: 64},
        },
        mac: {hashAlgorithm: {SHA256: 'SHA256', SHA512_224: 'SHA512/224'}} // NOSONAR Rule javascript:S1192 - False positive
    },
    asymmetric: {
        keyPair: {
            encryption: {
                algorithm: {RSA: 'RSA'},
                keyLengthBits: {KL_2048: 2048, KL_3072: 3072, KL_4096: 4096},
                publicExponent: {F4: 65537}
            },
        },
        signer: {
            algorithm: {RSA: 'RSA'},
            hashAlgorithm: {SHA256: 'SHA256', SHA512_224: 'SHA512/224'}, // NOSONAR Rule javascript:S1192 - False positive
            padding: {
                PSS: {
                    name: 'PSS',
                    hashAlgorithm: {SHA256: 'SHA256', SHA512_224: 'SHA512/224'}, // NOSONAR Rule javascript:S1192 - False positive
                    saltLengthBytes: {SL_32: 32, SL_64: 64},
                    maskGenerator: {
                        MGF1: {
                            name: 'MGF1',
                            hashAlgorithm: {SHA256: 'SHA256', SHA512_224: 'SHA512/224'} // NOSONAR Rule javascript:S1192 - False positive
                        }
                    }
                }
            },
            publicExponent: {F4: 65537},
        },
        cipher: {
            algorithm: {
                RSA_OAEP: {
                    name: 'RSA-OAEP',
                    hashAlgorithm: {SHA256: 'SHA256'},
                    maskGenerator: {MGF1: {name: 'MGF1', hashAlgorithm: {SHA1: 'SHA1'}}}
                },
                RSA_KEM: {
                    name: 'RSA-KEM',
                    secretKeyLengthBytes: {KL_16: 16, KL_24: 24, KL_32: 32},
                    ivLengthBytes: {IVL_12: 12, IVL_16: 16, IVL_32: 32, IVL_64: 64},
                    tagLengthBytes: {TL_12: 12, TL_16: 16, TL_32: 32, TL_64: 64},
                    keyDeriver: {
                        name: {KDF1: 'KDF1', KDF2: 'KDF2', MGF1: 'MGF1'},
                        hashAlgorithm: {SHA256: 'SHA256', SHA512_224: 'SHA512/224'} // NOSONAR Rule javascript:S1192 - False positive
                    },
                    symmetricCipher: {AES_GCM: 'AES-GCM'}
                }
            },
        },
        xmlSigner: {
            signer: {
                algorithm: {RSA: 'RSA'},
                hashAlgorithm: {SHA256: 'SHA256', SHA512_224: 'SHA512/224'}, // NOSONAR Rule javascript:S1192 - False positive
                publicExponent: {F4: 65537}
            },
            canonicalizationAlgorithm:
                {XML_EXC_C14N: 'http://www.w3.org/2001/10/xml-exc-c14n#'},
            transformAlgorithm: {
                ENVELOPED_SIGNATURE:
                    'http://www.w3.org/2000/09/xmldsig#enveloped-signature'
            }
        }
    },
    proofs: {
        messageDigest: {
            algorithm: {SHA256: 'SHA256', SHA512_224: 'SHA512/224'}, // NOSONAR Rule javascript:S1192 - False positive
        }
    },
}));
