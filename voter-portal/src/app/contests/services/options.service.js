/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function () {
	'ngInject';

	const validate = contest => {
		contest.error = false;
		contest.errors = [];

		contest.questions.forEach(q => {
			q.error = false;
		});
	};


	return {
		initialize: function () {
			// Empty default function
		},
		validate,
	};
};
