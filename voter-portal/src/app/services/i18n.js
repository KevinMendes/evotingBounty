/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($http, $q) {
	'ngInject';

	const _this = this;

	// default config
	this.configData = {
		languages: [
			{
				code: 'en_US',
				name: 'English',
			},
			{
				code: 'fr_FR',
				name: 'Français',
			},
		],
		default: 'en_US',
	};

	// load from external json:

	this.loadConfig = function () {
		const deferred = $q.defer();

		$http
			.get('i18n.json')
			.then(function (res) {
				const data = res.data;

				_this.configData = data;
				deferred.resolve(_this.configData);
			})
			.catch(function () {
				console.log('using default i18n');
				deferred.resolve(_this.configData);
			});

		return deferred.promise;
	};
};
