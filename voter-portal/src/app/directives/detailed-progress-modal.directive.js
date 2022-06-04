/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($modal, detailedProgressService) {
	'ngInject';

	return {
		restrict: 'EA',
		replace: true,
		scope: {
			title: '@detailedProgressModal',
			slowProgressTitle: '@',
		},
		template: `<div>
                <div data-ng-if="model.currentStepInProgress" class="login-modal-backdrop modal-backdrop fade in"></div>
                <div data-ng-if="model.currentStepInProgress" role="alert" class="login-modal modal fade in">
                  <div class="modal-sending">
                    <div class="container">
                      <h1 class="sr-only" tabindex="-1" data-auto-focus role="alert">{{ defaultTitle }}</h1>
                      <p class="h1" aria-hidden="true">{{ title }}</p>
                      <ul class="list-unstyled">
                        <li data-layout="row" ng-repeat="step in model.steps">
                          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" class="ci-spin" data-ng-if="step.running"
                            aria-hidden="true" focusable="false">
                            <path d="M18.23,12.63A0.56,0.56,0,0,1,17.79,12h0a0.56,0.56,0,0,1,.44-0.63h4.32A0.56,0.56,0,0,1,23,12h0
                            a0.56,0.56,0,0,1-.44.63H18.23Zm-16.79,0A0.56,0.56,0,0,1,1,12H1a0.56,0.56,0,0,1,.44-0.63H5.76a0.56,0.56,0,0,1,.44.63
                            h0a0.56,0.56,0,0,1-.44.63H1.44Zm9.93,5.6A0.56,0.56,0,0,1,12,17.79h0a0.56,0.56,0,0,1,.63.44v4.32A0.56,0.56,0,0,1,12,23
                            h0a0.56,0.56,0,0,1-.63-0.44V18.23Zm0-16.79A0.56,0.56,0,0,1,12,1h0a0.56,0.56,0,0,1,.63.44V5.76a0.56,0.56,0,0,1-.63.44
                            h0a0.56,0.56,0,0,1-.63-0.44V1.44ZM17,15.76A0.56,0.56,0,0,1,17,15h0a0.56,0.56,0,0,1,.71-0.31l3.7,2.23
                            a0.56,0.56,0,0,1,.06.77h0a0.56,0.56,0,0,1-.71.31ZM2.63,7.09a0.56,0.56,0,0,1-.05-0.77h0A0.56,0.56,0,0,1,3.28,6
                            L7,8.24A0.56,0.56,0,0,1,7,9H7a0.56,0.56,0,0,1-.71.31L2.63,7.09h0ZM8.24,17A0.56,0.56,0,0,1,9,17H9a0.56,0.56,0,0,1,.31.71
                            l-2.23,3.7a0.56,0.56,0,0,1-.77.05h0A0.56,0.56,0,0,1,6,20.72L8.24,17h0ZM16.9,2.63a0.56,0.56,0,0,1,.77-0.05h0
                            a0.56,0.56,0,0,1,.31.71L15.76,7A0.56,0.56,0,0,1,15,7h0a0.56,0.56,0,0,1-.31-0.7l2.23-3.7h0ZM7,15.76A0.56,0.56,0,0,0,7,15
                            H7a0.56,0.56,0,0,0-.71-0.31l-3.7,2.23a0.56,0.56,0,0,0-.05.77h0a0.56,0.56,0,0,0,.71.31ZM21.37,7.09
                            a0.56,0.56,0,0,0,.05-0.77h0A0.56,0.56,0,0,0,20.71,6L17,8.24A0.56,0.56,0,0,0,17,9h0a0.57,0.57,0,0,0,.71.31l3.7-2.23
                            h0ZM15.76,17A0.56,0.56,0,0,0,15,17h0a0.56,0.56,0,0,0-.31.71l2.23,3.7a0.57,0.57,0,0,0,.77.05h0A0.56,0.56,0,0,0,18,20.72
                            L15.76,17h0ZM7.09,2.63a0.56,0.56,0,0,0-.77-0.05h0A0.56,0.56,0,0,0,6,3.28L8.24,7A0.57,0.57,0,0,0,9,7H9
                            a0.56,0.56,0,0,0,.31-0.7L7.1,2.63h0Z"
                            />
                          </svg>
                          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" data-ng-if="step.success"
                            aria-hidden="true" focusable="false">
                            <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
                          </svg>
                          <span data-flex class="phm" role="alert">{{step.text|translate}}</span>
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>`,
		controller: function ($scope) {
			'ngInject';

			$scope.defaultTitle = $scope.title;

			$scope.model = detailedProgressService.model;

			$scope.model.onSlowProgressNotified = () => {
				$scope.title = $scope.slowProgressTitle;
			};
		},
	};
};
