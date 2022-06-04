/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
'use strict';
angular
    .module('filters', [])

    // formats an array as a 'delimiter' separated string

    .filter('join', function () {
        return function (input, delimiter) {
            if (input && input.join) {
                return input.join(delimiter || ', ');
            } else {
                return '';
            }
        };
    })

    .filter('truncate', function () {
        return function (text, length, end) {
            if (isNaN(length)) {
                length = 10;
            }
            if (end === undefined) {
                end = '...';
            }
            if (text.length <= length || text.length - end.length <= length) {
                return text;
            } else {
                return String(text).substring(0, length - end.length) + end;
            }
        };
    })

    .filter('sparse', function () {
        return function (text) {
            return text ? text.replace(/,/g, ', ') : text;
        };
    });
