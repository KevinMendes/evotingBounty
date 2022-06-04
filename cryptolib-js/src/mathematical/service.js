/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const ZpSubgroup = require('./zp-subgroup');
const ZpGroupElement = require('./zp-group-element');
const Exponent = require('./exponent');
const MathematicalRandomGenerator = require('./random-generator');
const secureRandom = require('../securerandom');
const validator = require('../input-validator');
const codec = require('../codec');
const forge = require('node-forge');
const cryptoPolicy = require('../cryptopolicy');

const BigInteger = forge.jsbn.BigInteger;
const ONE = new BigInteger('1');
const TWO = new BigInteger('2');

const groupType = cryptoPolicy.options.mathematical.groups.type;

module.exports = MathematicalService;

/**
 * @class MathematicalService
 * @classdesc The mathematical service API. To instantiate this object, use the
 *            method {@link newService}.
 * @hideconstructor
 * @param {Object}
 *            [options] An object containing optional arguments.
 * @param {SecureRandomService}
 *            [options.secureRandomService=Created internally] The secure random
 *            service to use.
 */
function MathematicalService(options) {
    options = options || {};

	let policy;
	if (options.policy) {
        policy = options.policy;
    } else {
        policy = cryptoPolicy.newInstance();
    }

	let secureRandomService;
	if (options.secureRandomService) {
        secureRandomService = options.secureRandomService;
    } else {
        secureRandomService = secureRandom;
    }

    /**
     * Creates a new MathematicalRandomGenerator object for generating random
     * mathematical objects.
     *
     * @function newRandomGenerator
     * @memberof MathematicalService
     * @returns {MathematicalRandomGenerator} The new
     *          MathematicalRandomGenerator object.
     */
    this.newRandomGenerator = function () {
        return new MathematicalRandomGenerator(secureRandomService);
    };

    /**
     * Creates a new ZpSubgroup object, which encapsulates a Zp subgroup.
     *
     * @function newZpSubgroup
     * @memberof MathematicalService
     * @param {forge.jsbn.BigInteger|string}
     *            pOrJson The modulus of the Zp subgroup <b>OR</b> a JSON
     *            string representation of a ZpSubgroup object, compatible with
     *            its <code>toJson</code> method. For the latter case, any
     *            additional input arguments will be ignored.
     * @param {forge.jsbn.BigInteger}
     *            q The order of the Zp subgroup. It must be within the range
     *            <code>[1, p-1]</code>.
     * @param {forge.jsbn.BigInteger}
     *            g The generator of the Zp subgroup. It must be within the
     *            range <code>[2, p-1]</code>.
     * @returns {ZpSubgroup} The new ZpSubgroup object.
     * @throws {Error}
     *             If the input data validation fails.
     */
    this.newZpSubgroup = function (pOrJson, q, g) {
        if (typeof pOrJson !== 'string') {
            validator.checkZpSubgroupModulus(
                pOrJson, 'Modulus p to create new ZpSubgroup object');
            validator.checkZpSubgroupOrder(
                q, 'Order q to create new ZpSubgroup object', pOrJson);
            validator.checkZpSubgroupGenerator(
                g, 'Generator to create new ZpSubgroup object', pOrJson);

            return new ZpSubgroup(pOrJson, q, g);
        } else {
            return jsonToZpSubgroup(pOrJson);
        }
    };

    /**
     * Check whether a group or element matches the
     * relevant policy type.
     *
     * @param {ZpSubGroup|ZpGroupElement} groupOrElement
     *             the group or element to check.
     * @throws {TypeError}
     *             If the group does not match the type.
     */
    this.checkGroupMatchesPolicy = function (groupOrElement) {
		const type = policy.mathematical.groups.type;
		const p = groupOrElement.p;
		const q = groupOrElement.q;

		switch (type) {
            case groupType.ZP_2048_256:
                checkNumberLength(p, 2048, type, 'P');
                checkNumberLength(q, 256, type, 'Q');
                break;

            case groupType.ZP_2048_224:
                checkNumberLength(p, 2048, type, 'P');
                checkNumberLength(q, 224, type, 'Q');
                break;

            case groupType.QR_2048:
                checkNumberLength(q, 2047, type, 'Q');
                if (!p.equals(q.multiply(TWO).add(ONE))) {
                    throw new TypeError(
                        'Expected P to equal 2Q + 1 for group type ' + type);
                }
                break;

            case groupType.QR_3072:
                checkNumberLength(q, 3071, type, 'Q');
                if (!p.equals(q.multiply(TWO).add(ONE))) {
                    throw new TypeError(
                        'Expected P to equal 2Q + 1 for group type ' + type);
                }
                break;

            default:
                throw new TypeError('Invalid group type; Found ' + type);
        }
    };

    /**
     * Check whether each group of element in an array
     * matches the relevant policy type.
     *
     * @param {ZpSubGroup[]|ZpGroupElement[]} groupsOrElements
     *             the array of groups or elements to check.
     * @throws {TypeError}
     *             If the group does not match the type.
     */
    this.checkGroupArrayMatchesPolicy = function (groupsOrElements) {
		const that = this;
		groupsOrElements.forEach(function (groupOrElement) {
            that.checkGroupMatchesPolicy(groupOrElement);
        });
    };
}

MathematicalService.prototype = {
    /**
     * Creates a new ZpGroupElement object, which encapsulates a Zp group
     * element.
     *
     * @function newZpGroupElement
     * @memberof MathematicalService
     * @param {forge.jsbn.BigInteger|string}
     *            pOrJson The modulus of the Zp subgroup to which the Zp group
     *            element belongs <b>OR</b> a JSON string representation of a
     *            ZpGroupElement object, compatible with its <code>toJson</code>
     *            method. For the latter case, any additional input arguments
     *            will be ignored.
     * @param {forge.jsbn.BigInteger}
     *            q The order of the Zp subgroup to which the Zp group element
     *            belongs. It must be within the range <code>[1, p-1]</code>.
     * @param {forge.jsbn.BigInteger}
     *            value The value of the Zp group element. It must be within the
     *            range <code>[1, p-1]</code>.
     * @returns {ZpGroupElement} The new ZpGroupElement object.
     * @throws {Error}
     *             If the input data validation fails.
     */
    newZpGroupElement: function (pOrJson, q, value) {
        if (typeof pOrJson !== 'string') {
            validator.checkZpSubgroupModulus(
                pOrJson, 'Modulus p to create new ZpGroupElement object');
            validator.checkZpSubgroupOrder(
                q, 'Order q to create new ZpGroupElement object', pOrJson);
            validator.checkZpGroupElementValue(
                value, 'Value to create new ZpGroupElement object', pOrJson);

            return new ZpGroupElement(pOrJson, q, value);
        } else {
            return jsonToZpGroupElement(pOrJson);
        }
    },

    /**
     * Creates a new Exponent object, which encapsulates an exponent.
     *
     * @function newExponent
     * @memberof MathematicalService
     * @param {forge.jsbn.BigInteger|string}
     *            qOrJson The order of the Zp subgroup to which the exponent is
     *            associated <b>OR</b> a JSON string representation of an
     *            Exponent object, compatible with its <code>toJson</code>
     *            method. For the latter case, any additional input arguments
     *            will be ignored.
     * @param {forge.jsbn.BigInteger}
     *            value The value of the exponent. If this value is greater than
     *            or equal to <code>q</code> it will be adjusted to be less
     *            then <code>q</code>, via the <code>mod(q)</code>
     *            operation.
     * @returns {Exponent} The new Exponent object.
     * @throws {Error}
     *             If the input data validation fails.
     */
    newExponent: function (qOrJson, value) {
        if (typeof qOrJson !== 'string') {
            validator.checkZpSubgroupOrder(
                qOrJson, 'Order q to create new Exponent object');
            validator.checkExponentValue(
                value, 'Value to create new Exponent object');

            return new Exponent(qOrJson, value);
        } else {
            return jsonToExponent(qOrJson);
        }
    },

    /**
     * Creates a new quadratic residue Zp subgroup from the modulus
     * <code>p</code> and <code>generator</code> provided as input.
     * <p>
     * <b>NOTE</b>: This method creates a particular type of Zp subgroup, with
     * the following property:
     * <p>
     * <code>p = 2q + 1</code>
     *
     * @function newQuadraticResidueGroup
     * @memberof MathematicalService
     * @param {forge.jsbn.BigInteger}
     *            p The modulus of the quadratic residue group.
     * @param {forge.jsbn.BigInteger}
     *            g The generator of the quadratic residue group.
     * @returns {ZpSubgroup} The quadratic residue group.
     * @throws {Error}
     *             If the input validation fails.
     */
    newQuadraticResidueGroup: function (p, g) {
        validator.checkZpSubgroupModulus(
            p, 'Modulus p for quadratic residue group generation');
        validator.checkZpSubgroupGenerator(
            g, 'Generator for quadratic residue group generation', p);

		const q = p.subtract(BigInteger.ONE).divide(new BigInteger('2'));

		return new ZpSubgroup(p, q, g);
    },
};

function jsonToZpSubgroup(json) {
    validator.checkIsJsonString(
        json, 'JSON string representation of ZpSubgroup object');

	const parsed = JSON.parse(json);

	const p = codec.bytesToBigInteger(codec.base64Decode(parsed.zpSubgroup.p));
	const q = codec.bytesToBigInteger(codec.base64Decode(parsed.zpSubgroup.q));
	const g = codec.bytesToBigInteger(codec.base64Decode(parsed.zpSubgroup.g));

	return new ZpSubgroup(p, q, g);
}

function jsonToZpGroupElement(json) {
    validator.checkIsJsonString(
        json, 'JSON string representation of ZpGroupElement object');

	const parsed = JSON.parse(json);

	const p = codec.bytesToBigInteger(codec.base64Decode(parsed.zpGroupElement.p));
	const q = codec.bytesToBigInteger(codec.base64Decode(parsed.zpGroupElement.q));
	const value =
		codec.bytesToBigInteger(codec.base64Decode(parsed.zpGroupElement.value));

	return new ZpGroupElement(p, q, value);
}

function jsonToExponent(json) {
    validator.checkIsJsonString(
        json, 'JSON string representation of Exponent object');

	const parsed = JSON.parse(json);

	const q = codec.bytesToBigInteger(codec.base64Decode(parsed.exponent.q));
	const value =
		codec.bytesToBigInteger(codec.base64Decode(parsed.exponent.value));

	return new Exponent(q, value);
}

/**
 * Checks if a big integer has a specific length.
 *
 * @param {BigInteger} num
 *               the number to check.
 * @param {number} expected
 *               the length the number must have.
 * @param {string} type
 *               the group type.
 * @param {string} label
 *               the name of the number.
 * @throws {TypeError}
 *               If the number does not have the specified length.
 */
function checkNumberLength(num, expected, type, label) {
	const length = num.bitLength();

	if (length !== expected) {
        throw new TypeError('Expected ' + label + ' to have a length of ' +
            expected + ' for group type ' + type + '; Found ' + length);
    }
}
