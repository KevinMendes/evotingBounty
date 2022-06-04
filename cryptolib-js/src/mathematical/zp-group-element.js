/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const validator = require('../input-validator');
const codec = require('../codec');

module.exports = ZpGroupElement;

/**
 * @class ZpGroupElement
 * @classdesc Encapsulates a Zp group element. To instantiate this object, use
 *            the method {@link MathematicalService.newZpGroupElement}.
 * @property {forge.jsbn.BigInteger} p The modulus of the Zp subgroup to which
 *           this Zp group element belongs.
 * @property {forge.jsbn.BigInteger} q The order of the Zp subgroup to which
 *           this Zp group element belongs.
 * @property {forge.jsbn.BigInteger} value The value of this Zp group element.
 */
function ZpGroupElement(p, q, value) {
    this.p = p;
    this.q = q;
    this.value = value;

    Object.freeze(this);
}

ZpGroupElement.prototype = {
    /**
     * Multiplies this Zp group element by the Zp group element provided as
     * input, using the formula:
     * <p>
     * <code>(this * element) mod p</code>
     *
     * @function multiply
     * @memberof ZpGroupElement
     * @param {ZpGroupElement}
     *            element The element to be multiplied with this element.
     * @returns {ZpGroupElement} The result of the multiplication operation.
     * @throws {Error}
     *             If the input data validation fails.
     */
    multiply: function (element) {
        checkZpGroupElement(
            element, 'Zp group element to multiply with this Zp group element',
            this.p, this.q);

		const newValue = this.value.multiply(element.value).mod(this.p);

		return new ZpGroupElement(this.p, this.q, newValue);
    },

    /**
     * Exponentiates this Zp group element with the exponent provided as input,
     * using the formula:
     * <p>
     * <code>(this<sup>exponent</sup>) mod p</code>
     *
     * @function exponentiate
     * @memberof ZpGroupElement
     * @param {Exponent}
     *            exponent The exponent to use in the exponentiation.
     * @returns {ZpGroupElement} The result of the exponentiation operation.
     * @throws {Error}
     *             If the input data validation fails.
     */
    exponentiate: function (exponent) {
        validator.checkExponent(
            exponent, 'exponent with which to exponentiate this Zp group element',
            this.q);

		const newValue = this.value.modPow(exponent.value, this.p);

		return new ZpGroupElement(this.p, this.q, newValue);
    },

    /**
     * Calculates the inverse of this Zp group element, using the formula:
     * <p>
     * <code>(this<sup>-1</sup>) mod p</code>
     *
     * @function invert
     * @memberof ZpGroupElement
     * @returns {ZpGroupElement} The result of the inverse operation.
     */
    invert: function () {
		const newValue = this.value.modInverse(this.p);

		return new ZpGroupElement(this.p, this.q, newValue);
    },

    /**
     * Checks if this Zp group element is equal to the Zp group element provided
     * as input.
     * <p>
     * Elements are considered equal if:
     * <ul>
     * <li>They belong to the same Zp subgroup.</li>
     * <li>They have the same value.</li>
     * </ul>
     *
     * @function equals
     * @memberof ZpGroupElement
     * @param {ZpGroupElement}
     *            element The Zp group element to compare with this Zp group
     *            element.
     * @returns {boolean} True if the equality holds, false otherwise.
     * @throws {Error}
     *             If the input data validation fails.
     */
    equals: function (element) {
        validator.checkZpGroupElement(
            element, 'Zp group element to compare with this Zp group element');

        return element.p.equals(this.p) && element.q.equals(this.q) &&
            element.value.equals(this.value);
    },

    /**
     * Serializes this object into a JSON string representation.
     * <p>
     * <b>IMPORTANT:</b> This serialization must be exactly the same as the
     * corresponding serialization in the library <code>cryptoLib</code>,
     * implemented in Java, since the two libraries are expected to communicate
     * with each other via these serializations.
     *
     * @function toJson
     * @memberof ZpGroupElement
     * @returns {string} The JSON string representation of this object.
     */
    toJson: function () {
        return JSON.stringify({
            zpGroupElement: {
                p: codec.base64Encode(this.p),
                q: codec.base64Encode(this.q),
                value: codec.base64Encode(this.value)
            }
        });
    }
};

function checkZpGroupElement(element, label, p, q) {
    validator.checkZpGroupElement(element, label);

	const pFound = element.p;
	if (!pFound.equals(p)) {
        throw new Error(
            'Expected ' + label + ' to belong to Zp subgroup with modulus p: ' + p +
            ' ; Found p: ' + pFound);
    }

	const qFound = element.q;
	if (!qFound.equals(q)) {
        throw new Error(
            'Expected ' + label + ' to belong to Zp subgroup of order q: ' + q +
            ' ; Found q: ' + qFound);
    }
}
