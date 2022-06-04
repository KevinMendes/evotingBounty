/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
(function () {
    'use strict';

    angular
        .module('ballotprinter', [])

        .factory('ballotprinter', function (_) {

			const b64decodeUnicode = function (b64) {
				return decodeURIComponent(
					Array.prototype.map
						.call(atob(b64), function (c) {
							return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
						})
						.join(''),
				);
			};

			const printBallot = function (ballot) {
				_.each(ballot.contests, function (contest) {
					_.each(contest.options, function (option) {
						// Contains an expandable b64encoded JSON

						try {
							const expanded = JSON.parse(b64decodeUnicode(option.defaultText));

							// substitute the expanded text:

							option.defaultText = expanded;
						} catch (ignore) {
							// ignored
						}
					});
				});
			};

			const print = function (ballots) {
				_.each(ballots, function (ballot) {
					printBallot(ballot);
				});
			};

            return {
                print: print,
            };
        });
})();
