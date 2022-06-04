/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('svSidebar', [])

    .directive('svSidebar', function (
        $rootScope,
        $state,
        sessionService,
        entitiesCounterService,
        configElectionConstants,
    ) {
        'use strict';

        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'app/directives/sv-sidebar/sv-sidebar.html',
            scope: {
                titleLong: '=',
                titleShort: '=',
            },
            link: function (scope) {
                // init
                scope.state = $state.current.name;

                // get the numbers of each group of items
                scope.entitiesCounterService = entitiesCounterService.model;
                entitiesCounterService.getTheMainItemsCount();

                // listen when an election event is selected and populate the nav
                $rootScope.$on('$stateChangeSuccess', function (event, toState) {
                    scope.state = toState.name;
                    if (
                        toState.name == configElectionConstants.TO_STATE.BALLOTS ||
                        toState.name == configElectionConstants.TO_STATE.VOTING_CARDS ||
                        toState.name ==
                        configElectionConstants.TO_STATE.ELECTORAL_AUTHORITIES ||
                        toState.name == configElectionConstants.TO_STATE.BALLOT_BOXES
                    ) {
                        entitiesCounterService.setElectionEventNav(true);
                        entitiesCounterService.getElectionEventInfo(
                            sessionService.getSelectedElectionEvent(),
                        );
                    } else {
                        entitiesCounterService.setElectionEventNav(false);
                        entitiesCounterService.resetElectionEventInfo();
                    }
                });

                // nav function
                scope.setState = function (target) {
                    $state.go(target);
                };
            },
        };
    });
