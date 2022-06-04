/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('settler', [
        // 'conf.globals'
    ])

    .factory('settler', function () {
        'use strict';

        // parses an array of promises like
        // [
        // { state: 'fulfilled', value: 1 },
        // { state: 'rejected', reason: 2 },
        // { state: 'fulfilled', value: 3 },
        // ]
        // and returns an object with the following properties:
        //
        // {
        //     fulfilled: [], // array of fullfilled values
        //     rejected: [],  // array of rejection reasons
        //     error: true    // true if some promise is rejected
        //     ok: true       // true if some promise is fulfilled
        // }

		const settle = function (promises) {
			const fulfilled = [],
				rejected = [];
			let error = false;

			promises = promises || [];
			promises.forEach(function (p) {
				if (p.state === 'fulfilled') {
					fulfilled.push(p.value);
				} else if (p.state === 'rejected') {
					rejected.push(p.reason);
					error = true;
				}
			});
			return {
				fulfilled: fulfilled,
				rejected: rejected,
				error: error,
				ok: fulfilled.length > 0,
			};
		};

		return {
            settle: settle,
        };
    });
