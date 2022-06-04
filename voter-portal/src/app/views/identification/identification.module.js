/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('app.identification', ['ui.router', 'app.ui.modal', 'app.services'])

	.controller('identificationBase', require('./identification-base.ctrl'))
	.controller('identificationDOB', require('./identification-dob.ctrl'))
	.controller('identificationSVK', require('./identification-svk.ctrl'))

	.directive('authChallenge', require('./auth-challenge')).name;
