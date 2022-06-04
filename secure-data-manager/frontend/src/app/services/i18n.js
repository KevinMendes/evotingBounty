/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('i18n', ['gettext'])
    .constant('i18n', {})
    .factory('statuses', function (gettext) {
        'use strict';

		const statuses = [
			gettext('New'),
			gettext('Locked'),
			gettext('Constituted'),
			gettext('Ready'),
			gettext('Approved'),
			gettext('Signed'),
			gettext('Generated'),
			gettext('Closed'),
			gettext('Downloaded'),
			gettext('Ready To Mix'),
			gettext('Mixed'),
			gettext('Decrypted'),
			gettext('Synchronized'),
			gettext('Error synchronizing'),
			gettext('Pending to synchronize'),
		];

		return {
            statuses: statuses,
        };
    });
