/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const validator = require('../input-validator');
const codec = require('../codec');

module.exports = Exponent;

/**
 * @class Exponent
 * @classdesc Encapsulates an exponent. To instantiate this object, use the
 *            method {@link MathematicalService.newExponent}.
 * @property {forge.jsbn.BigInteger} q The order of the exponent's associated
 * @property {forge.jsbn.BigInteger} value The value of the exponent.
 */
function Exponent(q, value) {
    this.q = q;
    this.value = value.mod(q);

    Object.freeze(this);
}

Exponent.prototype = {
    /**
     * Adds this exponent to the exponent provided as input, using the formula:
     * <p>
     * <code>(this + exponent) mod q</code>
     *
     * @function add
     * @memberof Exponent
     * @param {Exponent}
     *            exponent The exponent to be added to this exponent.
     * @returns {Exponent} The result of the addition operation.
     * @throws {Error}
     *             If the input data validation fails.
     */
    add: function (exponent) {
        validator.checkExponent(
            exponent, 'exponent to add to this exponent', this.q);

		const newValue = this.value.add(exponent.value).mod(this.q);

		return new Exponent(this.q, newValue);
    },

    /**
     * Subtracts the exponent provided as input from this exponent, using the
     * formula:
     * <p>
     * <code>(this - exponent) mod q</code>
     *
     * @function subtract
     * @memberof Exponent
     * @param {Exponent}
     *            exponent The exponent to be subtracted from this exponent.
     * @returns {Exponent} The result of the subtraction operation.
     * @throws {Error}
     *             If the input data validation fails.
     */
    subtract: function (exponent) {
        validator.checkExponent(
            exponent, 'exponent to subtract from this exponent', this.q);

		const newValue = this.value.subtract(exponent.value).mod(this.q);

		return new Exponent(this.q, newValue);
    },

    /**
     * Multiplies this exponent with the exponent provided as input, using the
     * formula:
     * <p>
     * <code>(this * exponent) mod q</code>
     *
     * @function multiply
     * @memberof Exponent
     * @param {Exponent}
     *            exponent The exponent to be multiplied with this exponent.
     * @returns {Exponent} The result of the multiplication operation.
     * @throws {Error}
     *             If the input data validation fails.
     */
    multiply: function (exponent) {
        validator.checkExponent(
            exponent, 'exponent to multiply with this exponent', this.q);

		const newValue = this.value.multiply(exponent.value).mod(this.q);

		return new Exponent(this.q, newValue);
    },

    /**
     * Negates this exponent, using the formula:
     * <p>
     * <code>(-this) mod q</code>
     *
     * @function negate
     * @memberof Exponent
     * @returns {Exponent} The result of the negation operation.
     */
    negate: function () {
        return new Exponent(this.q, this.value.negate().mod(this.q));
    },

    /**
     * Checks if this exponent is equal to the exponent provided as input.
     * <p>
     * The exponents are considered equal if:
     * <ul>
     * <li>They are associated with the same Zp subgroup.</li>
     * <li>They have the same value.</li>
     * </ul>
     *
     * @function equals
     * @memberof Exponent
     * @param {Exponent}
     *            exponent The exponent to compare with this exponent.
     * @returns {boolean} True if the equality holds, false otherwise.
     * @throws {Error}
     *             If the input data validation fails.
     */
    equals: function (exponent) {
        validator.checkExponent(
            exponent, 'exponent to compare with this exponent', this.q);

        return exponent.q.equals(this.q) && exponent.value.equals(this.value);
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
     * @memberof Exponent
     * @returns {string} The JSON string representation of this object.
     */
    toJson: function () {
        return JSON.stringify({
            exponent:
                {q: codec.base64Encode(this.q), value: codec.base64Encode(this.value)}
        });
    }
};
