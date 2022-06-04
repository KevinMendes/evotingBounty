/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

describe('confirm view', () => {

	let confirmCtrl;
	let $rootScope;
	let sessionService;
	let $scope;

	beforeEach(angular.mock.module('app.confirm'));

	beforeEach(inject(function (_$controller_, _sessionService_, _$rootScope_) {

		sessionService = _sessionService_;
		$rootScope = _$rootScope_;

		sessionService.voteCastCode = '12345678';

		spyOn(sessionService, 'getBallotName').and.returnValue('ballot');

		$scope = $rootScope.$new();

		confirmCtrl = _$controller_('confirm', {$scope: $scope});

	}));

	describe('controller initialization', () => {

		xit('obtains data from sessionService', () => {

			expect(confirmCtrl).toBeDefined();

			expect($scope.data.voteCastCode).toBe(sessionService.voteCastCode);
			expect($scope.errors).toEqual({});
			expect($scope.ballotName).toBe('ballot');

		});

		xit('formats casting code', () => {

			expect($scope.voteCastReturnCodeFormatted).toBe('1234 5678');

		});

	});

	describe('isVoteNew()', () => {

		let getStateSpy;

		beforeEach(() => {

			getStateSpy = spyOn(sessionService, 'getStatus');

		});

		xit('returns false if status is "voted"', () => {

			getStateSpy.and.returnValue('voted');

			const result = $scope.isVoteNew();

			expect(getStateSpy).toHaveBeenCalled();
			expect(result).toBe(false);

		});

		xit('returns true if status is not "voted"', () => {

			getStateSpy.and.returnValue('cast');

			const result = $scope.isVoteNew();

			expect(getStateSpy).toHaveBeenCalled();
			expect(result).toBe(true);

		});

	});

});
