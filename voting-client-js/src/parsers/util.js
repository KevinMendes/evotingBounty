/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* eslint no-var: 0 */
/* global OV */

const _ = require('lodash');

const getDefaultTranslation = function (
	attributeId,
	attributeProperty,
	propertyKey,
	context,
) {
	if (OV.config('debug')) {
		return (
			context + '_' + attributeId + '_' + attributeProperty + '_' + propertyKey
		);
	}

	return '';
};

/**
 *
 * @param {Object} texts
 * @param {String} attributeId
 * @param {String} attributeProperty - From which property the text should be taken for the given attribute
 * @param {String|Number} propertyKey - If the attribute's property is an array instead of a simple string, indicate which
 *                                element of the array should be treated as being the text of the given attribute
 * @param {String?} context - For debugging purposes: Used to specify some details about the expected translation
 */
const getText = function (
	texts,
	attributeId,
	attributeProperty,
	propertyKey,
	context,
) {
	if (!texts || !attributeProperty || !attributeId) {
		return getDefaultTranslation(
			attributeId,
			attributeProperty,
			propertyKey,
			context,
		);
	}

	try {
		if (!_.isUndefined(propertyKey) && !_.isNull(propertyKey)) {
			let translationObj;

			if (typeof propertyKey === 'number') {
				translationObj = texts[attributeId][attributeProperty][propertyKey];
			} else {
				translationObj = texts[attributeId][attributeProperty].find(function (
					o,
				) {
					return o.key === propertyKey;
				});
			}

			if (!translationObj) {
				return getDefaultTranslation(
					attributeId,
					attributeProperty,
					propertyKey,
					context,
				);
			}

			return translationObj.value;
		}

		if (typeof texts[attributeId][attributeProperty] !== 'string') {
			return getDefaultTranslation(
				attributeId,
				attributeProperty,
				propertyKey,
				context,
			);
		}

		return texts[attributeId][attributeProperty];
	} catch (e) {
		return getDefaultTranslation(
			attributeId,
			attributeProperty,
			propertyKey,
			context,
		);
	}
};

const parseAttributeTranslations = function (texts, attributeId) {
	const translations = {};

	if (
		!texts ||
		!attributeId ||
		!texts[attributeId] ||
		!texts[attributeId].length
	) {
		return translations;
	}

	const curriedMerge = _.curry(_.merge, 2);

	_.forEach(texts[attributeId], curriedMerge(translations));

	return translations;
};

module.exports = {
	getText: getText,
	parseAttributeTranslations: parseAttributeTranslations,
};
