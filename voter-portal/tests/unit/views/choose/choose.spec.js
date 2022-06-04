/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

describe('Choose controller', () => {

	let chooseCtrl;
	let $scope;
	let sessionService;
	let $state;

	const mockContests = [
		{
			validate: () => {
				// Empty function
			}
		},
		{
			validate: () => {
				// Empty function
			}
		},
	];

	const mockValidateCorrect = function (contest) {

		contest.error = false;

	};

	const mockValidateError = function (contest) {

		contest.error = true;

	};

	beforeEach(angular.mock.module('app.choose'));

	beforeEach(inject(function (_$controller_, _sessionService_, _$state_) {

		sessionService = _sessionService_;
		$state = _$state_;

		spyOn(sessionService, 'setState');
		spyOn(sessionService, 'getElections').and.returnValue(mockContests);

		$scope = {
			contestsForm: {
				[`electionForm_${mockContests[0].id}`]: {
					$invalid: false,
				},
				[`electionForm_${mockContests[1].id}`]: {
					$invalid: false,
				},
			},
		};
		chooseCtrl = _$controller_('choose', {$scope: $scope});

	}));

	it('should be defined and init correctly', function () {

		expect(chooseCtrl).toBeDefined();

		expect($scope.errors).toEqual({});
		expect(sessionService.setState).toHaveBeenCalledWith('voting');
		expect(sessionService.getElections).toHaveBeenCalled();

	});

	xit('should allow going to review if no contest has errors', function () {

		expect($scope.errors).toEqual({});

		spyOn($scope.elections[0], 'validate').and.callFake(mockValidateCorrect);
		spyOn($scope.elections[1], 'validate').and.callFake(mockValidateCorrect);
		spyOn($state, 'go');

		$scope.review();

		expect($scope.errors.hasError).toBe(false);
		expect($state.go).toHaveBeenCalledWith('review');

	});

	xit('should display the error and prevent going to review if any contest has errors', function () {

		expect($scope.errors).toEqual({});

		spyOn($scope.elections[0], 'validate').and.callFake(mockValidateCorrect);
		spyOn($scope.elections[1], 'validate').and.callFake(mockValidateError);
		spyOn($state, 'go');

		$scope.review();

		expect($scope.errors.hasError).toBe(true);
		expect($state.go).not.toHaveBeenCalled();

	});

});
