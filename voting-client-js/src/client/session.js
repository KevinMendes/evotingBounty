/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
// session values container

module.exports = (function () {
	'use strict';

	let sess = {};

	// get/set values in session

	const session = function (name, value) {
		if (value) {
			// if called with name and value args, assign value to name property
			return (sess[name] = value);
		} else {
			switch (typeof name) {
				case 'object':
					// if called with and object, assign it as new full state
					return (sess = name);

				case 'undefined':
					// if called with no args, return full state
					return sess;

				default:
					// if called with a single string, return that property
					return sess[name];
			}
		}
	};

	return session;
})();
