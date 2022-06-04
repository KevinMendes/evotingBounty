/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const codec = require('../codec');

module.exports = ElGamalPrivateKey;

/**
 * @class ElGamalPrivateKey
 * @classdesc Encapsulates an ElGamal private key. To instantiate this object,
 *            use the method {@link ElGamalCryptographyService.newPrivateKey}.
 * @property {ZpSubgroup} group The Zp subgroup to which the exponents of this
 *           private key are associated.
 * @property {Exponent[]} exponents The exponents that comprise this private
 *           key.
 */
function ElGamalPrivateKey(group, exponents) {
	this.group = group;
	this.exponents = exponents;
	Object.freeze(this.exponents);

	Object.freeze(this);
}

ElGamalPrivateKey.prototype = {
	/**
	 * Serializes this object into a JSON string representation.
	 * <p>
	 * <b>IMPORTANT:</b> This serialization must be exactly the same as the
	 * corresponding serialization in the library <code>cryptoLib</code>,
	 * implemented in Java, since the two libraries are expected to communicate
	 * with each other via these serializations.
	 *
	 * @function toJson
	 * @memberof ElGamalPrivateKey
	 * @returns {string} The JSON string representation of this object.
	 */
	toJson: function () {
		const gB64 = codec.base64Encode(this.group.generator.value);
		const pB64 = codec.base64Encode(this.group.p);
		const qB64 = codec.base64Encode(this.group.q);

		const exponentsB64 = [];
		for (let i = 0; i < this.exponents.length; i++) {
			exponentsB64.push(codec.base64Encode(this.exponents[i].value));
		}

		return JSON.stringify({
			privateKey:
				{zpSubgroup: {g: gB64, p: pB64, q: qB64}, exponents: exponentsB64}
		});
	}
};
