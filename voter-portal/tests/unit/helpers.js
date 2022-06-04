/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

const getCompiledDirective = _.curry((template, scope) => {

	let $compile;
	let $rootScope;

	inject((_$compile_, _$rootScope_) => {

		$compile = _$compile_;
		$rootScope = _$rootScope_;

	});

	if (!scope) {

		scope = $rootScope.$new();

	}

	const element = angular.element(template);
	const compiledElement = $compile(element)(scope);

	scope.$digest();

	return compiledElement;

});

module.exports = {
	getCompiledDirective,
};
