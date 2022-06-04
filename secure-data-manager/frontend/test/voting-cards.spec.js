/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
describe('voting-cards.js', function () {
    'use strict';

    beforeEach(module('endpoints'));
    beforeEach(module('gettext'));
    beforeEach(module('voting-cards'));

    describe('filters', function () {

        function seconds(time) {
            return time * 1000;
        }

        function minutes(time) {
            return seconds(time) * 60;
        }

        function hours(time) {
            return minutes(time) * 60;
        }

        it('Should show elapsed time formatted', inject(function ($filter) {
			const elapsed = $filter('elapsed');

			let time = seconds(0);
			expect(elapsed(time)).toBe('0 seconds');

            time = seconds(1);
            expect(elapsed(time)).toBe('1 seconds');

            time = seconds(59);
            expect(elapsed(time)).toBe('59 seconds');

            time = seconds(60);
            expect(elapsed(time)).toBe('1 minute');

            time = seconds(61);
            expect(elapsed(time)).toBe('1 minute 1 seconds');

            time = minutes(59);
            expect(elapsed(time)).toBe('59 minutes 0 seconds');

            time = minutes(60);
            expect(elapsed(time)).toBe('1 hour 0 minutes');

            time = minutes(61);
            expect(elapsed(time)).toBe('1 hour 1 minutes');

            time = hours(2);
            expect(elapsed(time)).toBe('2 hours 0 minutes');
        }));

    });

});
