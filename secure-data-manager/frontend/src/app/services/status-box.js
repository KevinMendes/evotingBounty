/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('statusBox', [
        // 'conf.globals'
    ])

    .factory('statusBox', function () {
        'use strict';

		const status = {
			// false == is NOT collapsed
			// true == is collapsed
			bb: false,
			vcs: false,
		};

		function toggleStatusBox(key) {
            status[key] = !status[key];
            return status[key];
        }

        function getStatusBox(key) {
            return status[key];
        }

        return {
            toggleStatusBox: toggleStatusBox,
            getStatusBox: getStatusBox,
        };
    });
