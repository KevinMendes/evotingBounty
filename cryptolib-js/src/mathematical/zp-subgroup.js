/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const ZpGroupElement = require('./zp-group-element');
const validator = require('../input-validator');
const codec = require('../codec');
const forge = require('node-forge');

module.exports = ZpSubgroup;

const BigInteger = forge.jsbn.BigInteger;

/**
 * Encapsulates a Zp subgroup. To instantiate this object, use the method {@link
    * MathematicalService.newZpSubgroup}.
 * <p>
 * Given a prime <code>p</code>, the Zp group is defined as the set of all
 * <code>Integers (mod p)</code>. A Zp subgroup is a subset of the Zp group,
 * of order <code>q</code>, defined as a set of <code>Primes (mod p)</code>.
 * <p>
 * Both the Zp group and the Zp subgroup are finite cyclic groups, which means
 * that all of their elements can be generated by exponentiating a special Zp
 * group element called the <code>generator</code>.
 * <p>
 * When the modulus <code>p</code> and the order <code>q</code> are related
 * by the restriction, <code>p = 2q + 1</code>, then the Zp subgroup can also
 * be referred to as a quadratic residue group.
 *
 * @class ZpSubgroup
 * @property {forge.jsbn.BigInteger} p The modulus of the Zp subgroup.
 * @property {forge.jsbn.BigInteger} q The order of the Zp subgroup.
 * @property {ZpGroupElement} generator The generator element of the Zp
 *           subgroup.
 * @property {ZpGroupElement} identity The identity element of the Zp subgroup.
 */
function ZpSubgroup(p, q, g) {
    this.p = p;
    this.q = q;
    this.generator = new ZpGroupElement(p, q, g);
    this.identity = new ZpGroupElement(p, q, BigInteger.ONE);

    Object.freeze(this);
}

ZpSubgroup.prototype = {
    /**
     * Checks whether a Zp group element provided as input is a member of this
     * Zp subgroup.
     *
     * An element is a member of the Zp subgroup if its value satisfies the
     * following conditions:
     * <ul>
     * <li><code>0 < value < p</code></li>
     * <li><code>value<sup>q</sup> mod p = 1</code></li>
     * </ul>
     *
     * @function isGroupMember
     * @memberof ZpSubgroup
     * @param {ZpGroupElement}
     *            element The Zp group element whose group membership is to be
     *            checked.
     * @returns {boolean} <code>true</code> if the element provided as input
     *          is a member of the subgroup, <code>false</false> otherwise.
     * @throws {Error}
     *             If the input data validation fails.
     */
    isGroupMember: function (element) {
        validator.checkZpGroupElement(
            element, 'Zp group element to check for group membership');

        if (element.p.equals(this.p)) {
            return element.value.modPow(this.q, this.p).equals(BigInteger.ONE);
        } else {
            return false;
        }
    },

    /**
     * Checks if this Zp subgroup is equal to the Zp subgroup provided as input.
     *
     * @function equals
     * @memberof ZpSubgroup
     * @param {ZpSubgroup}
     *            group The Zp subgroup to compare with this Zp subgroup.
     * @returns {boolean} True if the equality holds, false otherwise.
     * @throws {Error}
     *             If the input data validation fails.
     */
    equals: function (group) {
        validator.checkIsObjectWithProperties(
            group, 'Zp subgroup to compare with this Zp subgroup');

        return group.p.equals(this.p) && group.q.equals(this.q) &&
            group.generator.equals(this.generator);
    },

    /**
     * Check whether this Zp subgroup is a quadratic residue group, which is
     * defined such that <code>p = 2q + 1</code>.
     *
     * @function isQuadraticResidueGroup
     * @memberof ZpSubgroup
     * @returns {boolean} <code>true</code> if the given group is a quadratic
     *          residue group, <code>false</code> otherwise.
     */
    isQuadraticResidueGroup: function () {
        return this.p.equals(
            new BigInteger('2').multiply(this.q).add(BigInteger.ONE));
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
     * @memberof ZpSubgroup
     * @returns {string} The JSON string representation of this object.
     */
    toJson: function () {
        return JSON.stringify({
            zpSubgroup: {
                p: codec.base64Encode(this.p),
                q: codec.base64Encode(this.q),
                g: codec.base64Encode(this.generator.value)
            }
        });
    }
};
