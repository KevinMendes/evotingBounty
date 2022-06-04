/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../../helpers');

describe('candidate-pre-selector component', () => {

	let ContestService;
	let CandidateService;
	let $rootScope;
	let scope;

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	const directive = getCompiledDirective(
		`<div candidate-pre-selector
      id          = "1"
      contest     = "contest"
      ng-model    = "selectedCandidateModel"
      position    = "position"
      on-selected = "onCandidateSelected"
      on-cleared  = "onCandidateCleared"
    ></div>`
	);

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_, _CandidateService_, _ContestService_) => {

		$rootScope = _$rootScope_;
		CandidateService = _CandidateService_;
		ContestService = _ContestService_;

		scope = $rootScope.$new();

		scope.contest = {};
		scope.position = 1;
		scope.selectedCandidateModel = null;
		scope.onCandidateSelected = jasmine.createSpy('onCandidateSelected');
		scope.onCandidateCleared = jasmine.createSpy('onCandidateCleared');

	}));

	xit('reports and error if the directive has no id specified', () => {

		const faultyDirective = getCompiledDirective(
			'<div candidate-pre-selector ng-model="selectedCandidateModel"></div>'
		);

		spyOn(console, 'error');

		faultyDirective(scope);

		expect(console.error).toHaveBeenCalled();

	});

	it('initializes the writeIn view model with the existing candidate\'s name if model is a writeIn candidate', () => {

		scope.selectedCandidateModel = {
			isWriteIn: true,
			name: 'test',
		};

		const elementUT = directive(scope);
		const isolatedScope = elementUT.isolateScope();

		expect(isolatedScope.viewModel.writeInCandidate)
			.toEqual(scope.selectedCandidateModel.name);

	});

	it('initializes the writeIn view model with empty string if model is not a writeIn candidate', () => {

		scope.selectedCandidateModel = {
			isWriteIn: false,
			name: 'test',
		};

		const elementUT = directive(scope);
		const isolatedScope = elementUT.isolateScope();

		expect(isolatedScope.viewModel.writeInCandidate)
			.toEqual('');

	});

	/**
	 * Controller
	 */
	describe('openCandidateSelectionModal()', () => {

		let $modal;
		let modalPromise;
		let modalOpenSpy;
		let getAllListsSpy;
		let getAllCandidatesSpy;

		beforeEach(inject((_ListService_, _CandidateService_, _$modal_, _$q_) => {

			$modal = _$modal_;
			modalPromise = _$q_.defer();

			getAllListsSpy = spyOn(_ListService_, 'getAllLists');
			getAllCandidatesSpy = spyOn(_CandidateService_, 'getAllCandidates');
			modalOpenSpy = spyOn($modal, 'open').and.returnValue({
				result: modalPromise.promise,
			});

		}));

		xit('obtains the data needed for the modal', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.openCandidateSelectionModal();

			expect(isolatedScope.data.searchedText).toBe('');
			expect(isolatedScope.data.searchCandidateText).toBe('');
			expect(isolatedScope.data.selectedList).toEqual({});

			expect(getAllListsSpy)
				.toHaveBeenCalled();
			expect(getAllCandidatesSpy)
				.toHaveBeenCalled();

		});

		xit('calls $modal.open', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.openCandidateSelectionModal();

			expect(modalOpenSpy)
				.toHaveBeenCalledWith({
					templateUrl: 'views/modals/choose-candidate.tpl.html',
					controller: 'defaultModal',
					size: 'lg',
					scope: isolatedScope,
				});

		});

		xit('calls onSelected method when $modal.open promise resolves', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.openCandidateSelectionModal();
			spyOn(isolatedScope, 'refocusBtn');

			modalPromise.resolve({});

			$rootScope.$apply();

			expect(scope.onCandidateSelected)
				.toHaveBeenCalledWith({});

			expect(isolatedScope.refocusBtn)
				.toHaveBeenCalled();

		});

		xit('calls angular.noop if no onSelected method was provided for when $modal.open promise resolves', () => {

			scope.onCandidateSelected = null;
			spyOn(angular, 'noop');

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.openCandidateSelectionModal();

			modalPromise.resolve({});

			$rootScope.$apply();

			expect(angular.noop)
				.toHaveBeenCalled();

		});

	});

	describe('hasDetails()', () => {

		xit('return false if candidate has no description, age or profession', () => {

			const noDetailsCandidates = [null, 1, {},
				{description: '  '}, {age: '  '}, {profession: '  '},
				{description: '  ', age: '  ', profession: '  '},
			];

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			noDetailsCandidates.forEach(candidate => {

				const result = isolatedScope.hasDetails(candidate);

				expect(result).toBe(false);

			});

		});

		xit('return true if candidate has description, age, profession or any combination of them', () => {

			const withDetailsCandidates = [
				{description: ' d '},
				{age: ' a '},
				{profession: ' p '},
				{description: ' d ', age: ' a '},
				{description: ' d ', profession: ' p '},
				{age: ' a ', profession: ' p '},
				{description: ' d ', age: ' a ', profession: ' p '},
			];

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			withDetailsCandidates.forEach(candidate => {

				const result = isolatedScope.hasDetails(candidate);

				expect(result).toBe(true);

			});

		});

	});

	describe('clearCandidate()', () => {

		it('calls outer scope\'s onCleared function', () => {

			scope.selectedCandidateModel = {};
			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.clearCandidate();

			expect(scope.onCandidateCleared)
				.toHaveBeenCalledWith(
					scope.selectedCandidateModel,
					scope.position
				);

		});

		xit('clears the writeInCandate view model', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.viewModel.writeInCandidate = 'writein';

			isolatedScope.clearCandidate();

			expect(isolatedScope.viewModel.writeInCandidate)
				.toBe('');

		});

	});

	describe('$watch(viewModel.writeInCandidate)', () => {

		[null, {isBlank: true}].forEach(candidate => {

			xit('adds a writein candidate representation if selected candidate is/has ' + JSON.stringify(candidate) + ' and writein view model is added', () => {

				spyOn(ContestService, 'addWriteInCandidate');

				scope.selectedCandidateModel = null;
				const elementUT = directive(scope);
				const isolatedScope = elementUT.isolateScope();

				isolatedScope.viewModel.writeInCandidate = 'writein';

				scope.$digest();

				expect(ContestService.addWriteInCandidate)
					.toHaveBeenCalledWith(scope.contest, scope.position, 'writein');

			});

		});

		xit('clears the writein representation if writein view model is erased', () => {

			scope.selectedCandidateModel = {isWriteIn: true, name: 'writein'};

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			spyOn(isolatedScope, 'clearCandidate');

			expect(isolatedScope.viewModel.writeInCandidate)
				.toBe('writein');

			isolatedScope.viewModel.writeInCandidate = '';

			scope.$digest();

			expect(isolatedScope.clearCandidate)
				.toHaveBeenCalled();

		});


		it('updates the name property of the writein model if view model was modified', () => {

			scope.selectedCandidateModel = {isWriteIn: true, name: 'writein'};

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.viewModel.writeInCandidate += 'test';

			scope.$digest();

			expect(scope.selectedCandidateModel.name)
				.toEqual('writeintest');

		});

	});

	describe('$watch(candidate.name)', () => {

		xit('clears the writeInCandate view model if the model is removed or is replaced with one that is not a writein', () => {

			scope.selectedCandidateModel = {
				isWriteIn: true,
			};
			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.viewModel.writeInCandidate = 'valid writein';

			scope.$digest();

			expect(isolatedScope.viewModel.writeInCandidate).toBe('valid writein');

			scope.selectedCandidateModel = null;

			scope.$digest();

			expect(isolatedScope.viewModel.writeInCandidate).toBe('');

		});

		it('validates the writein', () => {

			const formDirective = getCompiledDirective(
				`<form name="form">
          <div candidate-pre-selector
            id          = "1"
            name        = "candidate1"
            contest     = "contest"
            ng-model    = "selectedCandidateModel"
            position    = "position"
            on-selected = "onCandidateSelected"
            on-cleared  = "onCandidateCleared"
          ></div>
        </form>`
			);

			spyOn(CandidateService, 'isWriteInValid').and.returnValue(false);

			scope.selectedCandidateModel = {
				isWriteIn: true,
			};

			formDirective(scope);

			expect(CandidateService.isWriteInValid).toHaveBeenCalledWith(scope.selectedCandidateModel);
			expect(scope.form.candidate1.$valid).toBe(false);

		});

	});

});
