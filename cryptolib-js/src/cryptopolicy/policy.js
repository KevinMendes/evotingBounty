/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const objs = require('./objs');

// The default policy is created with a null prototype and then frozen, so that
// it cannot be manipulated (accidentally or otherwise).
let _defaultPolicy = require('./default-policy.json');
_defaultPolicy = objs.freeze(objs.leanCopy(_defaultPolicy));

module.exports = CryptographicPolicy;

/**
 * @class CryptographicPolicy
 * @classdesc The cryptographic policy API. To instantiate this object, use the
 * method {@link newInstance}.
 * @hideconstructor
 * @param {Object}
 *            [policy=Default policy] The cryptographic policy to use, as a JSON
 *            object.
 */
function CryptographicPolicy(policy) {
    // Get a fresh copy of the default policy.
    objs.leanCopy(_defaultPolicy, this);

    // If there is a policy as a constructor argument, merge it with the default
    // policy. But first make sure the object is lean.
    if (typeof policy === 'object') {
        policy = objs.leanCopy(policy);
        objs.leanCopy(objs.merge(this, policy), this);
    }
}
