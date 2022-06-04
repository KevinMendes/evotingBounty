/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const forge = require('node-forge');

const BigInteger = forge.jsbn.BigInteger;

const EXPECTED_ORDER = 'Expected order q of';
const EQUAL_MODULUS = 'to equal modulus of Zp subgroup provided as input:';
const EQUAL_ORDER = 'to equal order of Zp subgroup provided as input:';
const MUST_BE_LESS = 'must be less than modulus p:';

/**
 * Input data validation utility for this module. Only intended for internal
 * use.
 */
module.exports = {
    /**
     * Checks if a value is defined.
     *
     * @function checkIsDefined
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is undefined.
     */
    checkIsDefined: function (value, label) {
        if (typeof value === 'undefined') {
            throw new TypeError(`${label} is undefined.`);
        }
    },

    /**
     * Checks if a value is not null.
     *
     * @function checkIsNotNull
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is null.
     */
    checkIsNotNull: function (value, label) {
        if (value === null) {
            throw new TypeError(`${label} is null.`);
        }
    },

    /**
     * Checks if a value is defined and not null.
     *
     * @function checkIsDefinedAndNotNull
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not defined or it is null.
     */
    checkIsDefinedAndNotNull: function (value, label) {
        this.checkIsDefined(value, label);
        this.checkIsNotNull(value, label);
    },

    /**
     * Checks if a value is of an expected type.
     *
     * @function checkIsType
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            type The expected type of the value.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not of the expected type.
     */
    checkIsType: function (value, type, label) {
		const typeFound = typeof value;
		if (typeFound !== type) {
            throw new TypeError(`Expected ${label} to have type '${type}' ; Found: '${typeFound}'`);
        }
    },

    /**
     * Checks if a value is an instance of an Object.
     *
     * @function checkIsInstanceOf
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {Object}
     *            obj The Object to check against.
     * @param {string}
     *            objName The Object name, for error handling purposes.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the object is undefined, null, or it is not an instance of
     * the Object.
     */
    checkIsInstanceOf: function (value, obj, objName, label) {
        this.checkIsDefinedAndNotNull(value, label);

        if (!(value instanceof obj)) {
            throw new TypeError(`${label} is not an instance of Object ${objName}`);
        }
    },

    /**
     * Checks if a value is an object.
     *
     * @function checkIsObject
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not an object.
     */
    checkIsObject: function (value, label) {
        if (typeof value !== 'object') {
            throw new TypeError(`${label} is not an object.`);
        }
    },

    /**
     * Checks if a value is an object and has properties.
     *
     * @function checkIsObjectWithProperties
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not an object or it has no properties.
     */
    checkIsObjectWithProperties: function (value, label) {
        this.checkIsDefinedAndNotNull(value, label);

        this.checkIsObject(value, label);

        if (!Object.getOwnPropertyNames(value).length) {
            throw new TypeError(`${label} does not have any properties.`);
        }
    },

    /**
     * Checks if a value is a positive number.
     *
     * @function checkIsPositiveNumber
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not a positive number.
     */
    checkIsPositiveNumber: function (value, label) {
        this.checkIsType(value, 'number', label);

        if (Number(value) === 0 || Number(value) < 0) {
            throw new TypeError(`${label} must be at least 1. Found: ${value}`);
        }
    },

    /**
     * Checks if a value is a non-empty string.
     *
     * @function checkIsNonEmptyString
     * @private
     * @param {string}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not a non-empty string.
     */
    checkIsNonEmptyString: function (value, label) {
        this.checkIsDefinedAndNotNull(value, label);

        this.checkIsType(value, 'string', label);

        if (value.length === 0) {
            throw new TypeError(`${label} is empty.`);
        }
    },

    /**
     * Checks if a value is a valid JSON object.
     *
     * @function checkIsJsonObject
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is a valid JSON object.
     */
    checkIsJsonObject: function (value, label) {
        try {
            JSON.parse(value);
        } catch (error) {
            throw new TypeError(`${label} is not a valid JSON object.`);
        }
    },

    /**
     * Checks if a value is a valid JSON string.
     *
     * @function checkIsJsonString
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is a valid JSON string.
     */
    checkIsJsonString: function (value, label) {
        this.checkIsNonEmptyString(value, label);

        this.checkIsJsonObject(value, label);
    },

    /**
     * Checks the validity of a Zp subgroup modulus.
     *
     * @function checkZpSubgroupModulus
     * @private
     * @param {BigInteger}
     *            p The modulus to check.
     * @param {string}
     *            label The error message label for the modulus.
     * @throws {TypeError}
     *             If the modulus is not valid.
     */
    checkZpSubgroupModulus: function (p, label) {
        this.checkIsPositiveBigInteger(p, label);

        if (p.compareTo(new BigInteger('3')) < 0) {
            throw new TypeError(`${label} must not be less than 3; Found: ${p}`);
        }
    },

    /**
     * Checks the validity of a Zp subgroup order.
     *
     * @function checkZpSubgroupOrder
     * @private
     * @param {BigInteger}
     *            q The order to check.
     * @param {string}
     *            label The error message label for the order.
     * @param {BigInteger}
     *            [p] An optional parameter pertaining to the modulus of the Zp
     *            subgroup. If this option is provided, the order will be
     *            checked against this modulus and throw an error if the check
     *            fails.
     * @throws {TypeError}
     *             If the order is not valid.
     */
    checkZpSubgroupOrder: function (q, label, p) {
        this.checkIsPositiveBigInteger(q, label);

        if (typeof p !== 'undefined' && q.compareTo(p) >= 0) {
            throw new TypeError(`${label} ${MUST_BE_LESS} ${p} ; Found: ${q}`);
        }
    },

    /**
     * Checks the validity of a Zp subgroup generator.
     *
     * @function checkZpSubgroupGenerator
     * @private
     * @param {BigInteger}
     *            g The generator to check.
     * @param {string}
     *            label The error message label for the generator.
     * @param {BigInteger}
     *            [p] An optional parameter pertaining to the modulus of the Zp
     *            subgroup. If this option is provided, the generator will be
     *            checked against this modulus and throw an error if the check
     *            fails.
     * @throws {TypeError}
     *             If the generator is not valid.
     */
    checkZpSubgroupGenerator: function (g, label, p) {
        this.checkIsPositiveBigInteger(g, label);

        if (g.compareTo(new BigInteger('2')) < 0) {
            throw new TypeError(`${label} must not be less than 2; Found: ${g}`);
        }

        if (typeof p !== 'undefined' && g.compareTo(p) >= 0) {
            throw new TypeError(`${label} ${MUST_BE_LESS} ${p} ; Found: ${g}`);
        }
    },

    /**
     * Checks the validity of a Zp group element value.
     *
     * @function checkZpGroupElementValue
     * @private
     * @param {BigInteger}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @param {BigInteger}
     *            [p] An optional parameter pertaining to the modulus of the Zp
     *            subgroup. If this option is provided, the generator will be
     *            checked against this modulus and throw an error if the check
     *            fails.
     * @throws {TypeError}
     *             If the Zp group element value is not valid.
     */
    checkZpGroupElementValue: function (value, label, p) {
        this.checkIsPositiveBigInteger(value, label);

        if (typeof p !== 'undefined' && value.compareTo(p) >= 0) {
            throw new TypeError(`${label} ${MUST_BE_LESS} ${p} ; Found: ${value}`);
        }
    },

    /**
     * Checks the validity of an exponent value.
     *
     * @function checkExponentValue
     * @private
     * @param {BigInteger}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the exponent value is not valid.
     */
    checkExponentValue: function (value, label) {
        this.checkIsBigInteger(value, label);
    },

    /**
     * Checks if a value is an Array object.
     *
     * @function checkIsArray
     * @private
     * @param {Array}
     *            array The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not an Array object.
     */
    checkIsArray: function (value, label) {
        this.checkIsDefinedAndNotNull(value, `Array of ${label}`);

        if (value.constructor !== Array) {
            throw new TypeError(`Array of ${label} is not of type Array.`);
        }
    },

    /**
     * Checks if a value is a non-empty Array object.
     *
     * @function checkIsNonEmptyArray
     * @private
     * @param {Array}
     *            array The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not a non-empty Array object.
     */
    checkIsNonEmptyArray: function (value, label) {
        this.checkIsArray(value, label);

        if (value.length < 1) {
            throw new TypeError(`Array of ${label} is empty.`);
        }
    },

    /**
     * Checks if a value is an array of strings.
     *
     * @function checkIsStringArray
     * @private
     * @param {string[]}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If value is not an array of strings.
     */
    checkIsStringArray: function (value, label) {
        this.checkIsNonEmptyArray(value, label);

        for (let i = 0; i < value.length; i++) {
            this.checkIsType(value[i], 'string', `Element ${i} of ${label}`);
        }
    },

    /**
     * Checks the validity of a ZpGroupElement object.
     *
     * @function checkZpGroupElement
     * @private
     * @param {ZpGroupElement}
     *            element The object to check.
     * @param {string}
     *            label The error message label for the object.
     * @param {Zpsubgroup}
     *            [group] Optional parameter pertaining to a Zp subgroup. If
     *            this option is provided, the modulus and order of the element
     *            will be checked against those of the group.
     * @throws {TypeError}
     *             If the value is not a valid ZpGroupElement object.
     */
    checkZpGroupElement: function (element, label, group) {
        this.checkIsObjectWithProperties(element, label);

        if (typeof group !== 'undefined') {
            const groupP = group.p;
            const groupQ = group.q;

            const elementP = element.p;
            if (!elementP.equals(groupP)) {
                throw new TypeError(`Expected modulus p of ${label} ${EQUAL_MODULUS} ${groupP} ; Found: ${elementP}`);
            }

            const elementQ = element.q;
            if (!elementQ.equals(groupQ)) {
                throw new TypeError(`${EXPECTED_ORDER} ${label} ${EQUAL_ORDER} ${groupQ} ; Found: ${elementQ}`);
            }
        }
    },

    /**
     * Checks the validity of an Exponent object.
     *
     * @function checkExponent
     * @private
     * @param {Exponent}
     *            exponent The object to check.
     * @param {string}
     *            label The error message label for the object.
     * @param {Zpsubgroup}
     *            [q] Optional parameter pertaining to a Zp subgroup order. If
     *            this option is provided, the order of the exponent will be
     *            checked against that order.
     * @throws {TypeError}
     *             If the value is not a valid Exponent value.
     */
    checkExponent: function (exponent, label, q) {
        this.checkIsObjectWithProperties(exponent, label);

        if (typeof q !== 'undefined') {
            const exponentQ = exponent.q;
            if (!exponentQ.equals(q)) {
                throw new TypeError(`${EXPECTED_ORDER} ${label} ${EQUAL_ORDER} ${q} ; Found: ${exponentQ}`);
            }
        }
    },

    /**
     * Checks the validity of an array of ZpGroupElement objects.
     *
     * @function checkZpGroupElements
     * @private
     * @param {ZpGroupElement[]}
     *            elements The array to check.
     * @param {string}
     *            label The error message label for the array.
     * @param {Zpsubgroup}
     *            [group] Optional parameter pertaining to a Zp subgroup. If
     *            this option is provided, the modulus and order of each element
     *            will be checked against those of the group.
     * @throws {TypeError}
     *             If the array of ZpGroupElement objects is not valid.
     */
    checkZpGroupElements: function (elements, label, group) {
        this.checkIsNonEmptyArray(elements, label);

        for (let i = 0; i < elements.length; i++) {
            this.checkZpGroupElement(
                elements[i], `element ${i} of ${label}`, group);
        }
    },

    /**
     * Checks the validity of an array of Exponent objects.
     *
     * @function checkExponents
     * @private
     * @param {Exponent[]}
     *            exponents The array to check.
     * @param {string}
     *            label The error message label for the array.
     * @param {Zpsubgroup}
     *            [q] Optional parameter pertaining to a Zp subgroup order. If
     *            this option is provided, the order of the exponent will be
     *            checked against that order.
     * @throws {TypeError}
     *             If the array of Exponent objects is not valid.
     */
    checkExponents: function (exponents, label, q) {
        this.checkIsNonEmptyArray(exponents, label);

        for (let i = 0; i < exponents.length; i++) {
            this.checkExponent(exponents[i], `exponent ${i} of ${label}`, q);
        }
    },

    /**
     * Checks the validity of an ElGamalPrivateKey object.
     *
     * @function checkElGamalPrivateKey
     * @private
     * @param {ElGamalPrivateKey}
     *            privateKey The object to check.
     * @param {string}
     *            label The error message label for the object.
     * @param {Zpsubgroup}
     *            [group] Optional parameter pertaining to a Zp subgroup. If
     *            this option is provided, the order of the private key will be
     *            checked against that of the group.
     * @throws {TypeError}
     *             If the ElGamalPrivateKey object is not valid.
     */
    checkElGamalPrivateKey: function (privateKey, label, group) {
        this.checkIsObjectWithProperties(privateKey, label);

        if (typeof group !== 'undefined') {
            const groupQ = group.q;

            const privateKeyQ = privateKey.group.q;
            if (!privateKeyQ.equals(groupQ)) {
                throw new TypeError(`${EXPECTED_ORDER} ${label} ${EQUAL_ORDER} ${groupQ} ; Found: ${privateKeyQ}`);
            }
        }
    },

    /**
     * Checks if a value is a two-dimensional Array object.
     *
     * @function checkIsTwoDimensionalArray
     * @private
     * @param {Array}
     *            array The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not a two-dimensional Array object.
     */
    checkIsTwoDimensionalArray: function (array, label) {
        this.checkIsNonEmptyArray(array, label);

        for (const value of array) {
            this.checkIsArray(value, label);
        }
    },

    /**
     * Checks if a value is a function.
     *
     * @function checkIsFunction
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not a function.
     */
    checkIsFunction: function (value, label) {
        this.checkIsDefinedAndNotNull(value, label);

        if (typeof value !== 'function') {
            throw new TypeError(`${label} is not a function.`);
        }
    },

    /**
     * Checks if a value is a BigInteger object.
     *
     * @function checkIsBigInteger
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not a BigInteger object.
     */
    checkIsBigInteger: function (value, label) {
        this.checkIsDefinedAndNotNull(value, label);

        if (typeof value.abs === 'undefined') {
            throw new TypeError(`${label} is not a BigInteger object.`);
        }
    },

    /**
     * Checks if a value is a positive BigInteger object.
     *
     * @function checkIsPositiveBigInteger
     * @private
     * @param {Object}
     *            value The value to check.
     * @param {string}
     *            label The error message label for the value.
     * @throws {TypeError}
     *             If the value is not a positive BigInteger object.
     */
    checkIsPositiveBigInteger: function (value, label) {
        this.checkIsBigInteger(value, label);

        if (value.compareTo(BigInteger.ONE) < 0) {
            throw new TypeError(`${label} must not be less than 1; Found: ${value}`);
        }
    },

    /**
     * Checks the CryptographicPolicy object provided as input.
     *
     * @function checkCryptographicPolicy
     * @private
     * @param {CryptographicPolicy}
     *            policy The CryptographicPolicy object to check.
     * @param {string}
     *            label The error message label for the CryptographicPolicy
     * object.
     * @throws {TypeError}
     *             If the check of the CryptographicPolicy object fails.
     */
    checkCryptographicPolicy: function (policy, label) {
        this.checkIsNotNull(policy, label);

        this.checkIsDefinedAndNotNull(
            policy.asymmetric, `Property "asymmetric" of ${label}`);
    },

    /**
     * Checks the SecureRandomService object provided as input.
     *
     * @function checkSecureRandomService
     * @private
     * @param {SecureRandomService}
     *            secureRandomService The SecureRandomService object to check.
     * @param {string}
     *            label The error message label for the SecureRandomService
     * object.
     * @throws {TypeError}
     *             If the check of the SecureRandomService object fails.
     */
    checkSecureRandomService: function (secureRandomService, label) {
        this.checkIsNotNull(secureRandomService, label);

        this.checkIsFunction(
            secureRandomService.newRandomGenerator,
            `Function "newRandomGenerator" of ${label}`);
    },

    /**
     * Checks the public key parameters object provided as input.
     *
     * @function checkPublicKeyParameters
     * @private
     * @param {Object}
     *            params The public key parameters object.
     * @param {string}
     *            label The error message label for the public key parameters
     * object.
     * @throws {TypeError}
     *             If the check of the public key parameters object fails.
     */
    checkPublicKeyParameters: function (params, label) {
        this.checkIsDefinedAndNotNull(params, label);

        if (typeof params.pem !== 'undefined') {
            this.checkIsNonEmptyString(params.pem, 'RSA public key, PEM encoded');
        } else if (typeof params.n !== 'undefined') {
            this.checkIsPositiveBigInteger(params.n, 'RSA public key modulus (n)');
            this.checkIsPositiveBigInteger(params.e, 'RSA public key public exponent (e)');
        } else {
            throw new Error(`${label} contains neither field 'pem' nor field 'n'.`);
        }
    },

    /**
     * Checks the private key parameters object provided as input.
     *
     * @function checkPrivateKeyParameters
     * @private
     * @param {Object}
     *            params The private key parameters object.
     * @param {string}
     *            label The error message label for the private key parameters
     * object.
     * @throws {TypeError}
     *             If the check of the private key parameters object fails.
     */
    checkPrivateKeyParameters: function (params, label) {
        this.checkIsDefinedAndNotNull(params, label);

        if (typeof params.pem !== 'undefined') {
            this.checkIsNonEmptyString(params.pem, 'RSA private key, PEM encoded');
        } else if (typeof params.n !== 'undefined') {
            this.checkIsPositiveBigInteger(params.n, 'RSA private key modulus (n)');
            this.checkIsPositiveBigInteger(params.e, 'RSA private key public exponent (e)');
            this.checkIsPositiveBigInteger(params.d, 'RSA private key private exponent (d)');
            this.checkIsPositiveBigInteger(params.p, 'RSA private key first prime (p)');
            this.checkIsPositiveBigInteger(params.q, 'RSA private key second prime (q)');
            this.checkIsPositiveBigInteger(params.dP, 'RSA private key first exponent (dP)');
            this.checkIsPositiveBigInteger(params.dQ, 'RSA private key second exponent (dQ)');
            this.checkIsPositiveBigInteger(params.qInv, 'RSA private key coefficient (qInv)');
        } else {
            throw new Error(`${label} contains neither field 'pem' nor field 'n'.`);
        }
    }
};
