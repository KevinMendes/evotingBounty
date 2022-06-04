/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function () {
	const _ = require('lodash');
	const search = function (searchText, propertyToSearch, items) {
		if (!searchText) {
			return items;
		}

		const result = [];

		if (items) {
			_.each(items, function (item) {
				try {
					const included =
						item.hasOwnProperty(propertyToSearch) &&
						item[propertyToSearch]
							.toLowerCase()
							.indexOf(searchText.toLowerCase()) !== -1;

					if (included) {
						result.push(item);
					}
				} catch (e) {
					// ignore
				}
			});
		}

		return _.uniq(result, true);
	};

	return {
		search: search,
	};
};
