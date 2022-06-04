/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

module.exports = {
    // This function exposes a private one so that the private one can refer to
    // itself without having to worry about the `this` moving target.
    leanCopy: leanCopy,

    // This function will export the working function or an innocuous one
    // depending on
    // whether the platform supports object freezing or not.
    freeze: Object.freeze ? freeze : doNotFreeze,

    // This function will merge multiple objects into a single object.
    merge: merge
};

/**
 * Copies an object's properties into another, leaving out functions and
 * prototypes.
 *
 * The function can be used to return objects, or to modify an existing object,
 * which would then be passed as the second parameter.
 *
 * @function leanCopy
 * @private
 * @param {Object}
 *            src The object to copy from
 * @param {Object}
 *            dest The object to copy to
 * @returns {Object} An object with the same scalar properties as the source,
 *          but stripped of everything else.
 */
function leanCopy(src, dest) {
    if ('undefined' === typeof dest) {
        // Make sure there's a destination object.
        dest = Object.create(null);
    }

    for (const propertyName in src) {
        // Check whether the property is not inherited. Alternatively, check that
        // the `hasOwnProperty` method exists, otherwise the property is not
        // likely to be inherited.
        if (!src.hasOwnProperty || Object.prototype.hasOwnProperty.call(src, propertyName)) {
			const property = src[propertyName];
			if (typeof property === 'object') {
                // Copy onto a new prototype-less object, lean as they come.
                dest[propertyName] = leanCopy(property, Object.create(null));
            } else if (typeof property !== 'function') {
                dest[propertyName] = property;
            }
        }
    }

    // Return a lean copy of the source object.
    return dest;
}

/**
 * Prohibits an object from being frozen.
 *
 * @function doNotFreeze
 * @private
 * @param {Object}
 *            target The object not to freeze.
 * @returns {Object} The object, unfrozen.
 */
function doNotFreeze(target) {
    return target;
}

/**
 * Protects an object and all its contents against modification.
 *
 * @function freeze
 * @private
 * @param {Object}
 *            target The object to freeze.
 * @returns {Object} The object, after being frozen.
 */
function freeze(target) {
    // Get all property names
	const propNames = Object.getOwnPropertyNames(target);

	// Freeze the object's properties.
    propNames.forEach(function (name) {
		const property = target[name];

		// Freeze the properties' properties.
        if (typeof property === 'object' && property !== null) {
            freeze(property);
        }
    });

    // Freeze the object itself.
    return Object.freeze(target);
}

/**
 * Merges all of the objects provided as input into a single object.
 *
 * <p>
 * <b>NOTE</b> This function is a system independent version of the function
 * <code>Object.assign</code>.
 *
 * @function merge
 * @private
 * @returns {Object} The merged objects.
 */
function merge() {
	const resObj = {};
	for (let i = 0; i < arguments.length; i += 1) {
		const obj = arguments[i], keys = Object.keys(obj);
		for (let j = 0; j < keys.length; j += 1) {
            resObj[keys[j]] = obj[keys[j]];
        }
    }

    return resObj;
}
