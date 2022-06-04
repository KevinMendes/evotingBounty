/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
(function () {
    'use strict';

    angular
        .module('activeFilters', [])

        .factory('activeFilters', function (_) {
            // Init filters
			const activeFilters = {};

			const setActiveFilter = function (target, value) {
				activeFilters[target] = value;
			};

			const getActiveFilter = function (target) {
				return activeFilters[target];
			};

			return {
                getActiveFilter: getActiveFilter,
                setActiveFilter: setActiveFilter,
            };
        });
})();
