/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global require */
/* global OV */
/* jshint maxlen: 666  */

const _ = require('lodash');

module.exports = (function () {
	const parseCorrectnessIds = function (correctnessIds, option, attributes) {
		const attrMap = _.reduce(
			attributes,
			function (acc, attr) {
				acc[attr.id] = attr;
				return acc;
			},
			{},
		);

		const prime = option.representation;
		const attr = attrMap[option.attribute];

		if (attr) {
			if (!correctnessIds[prime]) {
				correctnessIds[prime] = [];
			}

			const related = [attr.id].concat(attr.related);

			_.each(related, function (rel) {
				if (
					attrMap[rel] &&
					attrMap[rel].correctness &&
					attrMap[rel].correctness === 'true'
				) {
					correctnessIds[prime].push(rel);
				}
			});
		}
	};

	return parseCorrectnessIds;
})();
