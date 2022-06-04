/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('pathConverter', [])

    .factory('pathConverter', function () {
        'use strict';

        // Remove path endings containing filename with extension
		const toFolderPath = function (path) {
			return path.replace(/(\\|\/)([\-\s\w]+)\.([a-zA-Z]+)$/, '');
		};

		return {
            toFolderPath: toFolderPath,
        };
    });
