/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
(function () {
	'use strict';

	angular
		.module('app.errors.dict', [])
		.factory('ErrorsDict', function (gettextCatalog) {
			// this use of gettextCatalog is so that the module is able to find the keys for translation
			const errors = {
				99: gettextCatalog.getString('Unexpected error, please check the Integration tools logs'),
				100: gettextCatalog.getString('General error, please check the Integration tools logs'),
				101: gettextCatalog.getString('Invalid action, please check the Integration tools logs'),
				102: gettextCatalog.getString('Missing parameter, please check the Integration tools logs'),
				103: gettextCatalog.getString('Parameter not valid, please check the Integration tools logs'),
				104: gettextCatalog.getString('File not found, please check the Integration tools logs'),
				105: gettextCatalog.getString('Error parsing file, please check the Integration tools logs'),
				106: gettextCatalog.getString('Content not valid, please check the Integration tools logs'),
				107: gettextCatalog.getString('Usb not found'),
				108: gettextCatalog.getString('Error copying files, please check the Integration tools logs'),
				109: gettextCatalog.getString('Election Event already in the usb, please check the Integration tools logs'),
				4000: gettextCatalog.getString('Secure Data Manager error, please check the logs'),
				4001: gettextCatalog.getString('Integration issue between components'),
				4002: gettextCatalog.getString('Import/Export problems reading files or some files are missing'),
				4003: gettextCatalog.getString('Error parsing plugin.xml'),
				4004: gettextCatalog.getString('Integration issue between components'),
				4005: gettextCatalog.getString('No action has been performed. Please review the plugin.xml configuration.'),
				4012: gettextCatalog.getString('Control Components keys do not exist. Please request them first')
			};

			return function (number) {
				return errors[number];
			};
		});
})();
