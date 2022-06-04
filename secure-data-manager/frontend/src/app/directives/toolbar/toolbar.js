/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('ovToolbar', [])

    .directive('ovToolbar', function (
        $mdSidenav,
        $state,
        ToolbarTitle,
        sessionService,
        gettext,
    ) {
        'use strict';
        const electionEventListName = 'election-event-list';
        const administrationBoardListName = 'administration-board-list';
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'app/directives/toolbar/toolbar.html',

            link: function (scope) {
                scope.getMainTitle = function () {
                    switch ($state.current.name) {
                        case electionEventListName:
                            return gettext('Election Events');
                        case administrationBoardListName:
                            return gettext('Administration Boards');
                        default:
                            return sessionService.getSelectedElectionEvent().defaultTitle;
                    }
                };

                scope.getElectionEventSection = function () {
                    switch ($state.current.name) {
                        case 'ballots':
                            return gettext('Ballots');
                        case 'voting-cards':
                            return gettext('Voting Card Sets');
                        case 'electoral-authorities':
                            return gettext('Electoral Authorities');
                        case 'ballot-boxes':
                            return gettext('Ballot Boxes');
                        default:
                            return gettext('Ballots');
                    }
                };

                scope.enableElectionEventSection = function () {
                    return (
                        sessionService.getSelectedElectionEvent() &&
                        $state.current.name !== administrationBoardListName &&
                        $state.current.name !== electionEventListName
                    );
                };

                scope.showImport = function () {
                    return (
                        $state.current.name == electionEventListName &&
                        sessionService.isImportExportEnabled()
                    );
                };

                scope.toggleLeftMenu = function () {
					const target = angular.element(document.getElementById('main-wrapper'));
					target.toggleClass('sidebar-is-closed');
                };
            },
        };
    })
    .factory('ToolbarTitle', function () {
        'use strict';

        return function (sessionService) {
            this.title = function () {
                return sessionService.getSelectedElectionEvent().alias;
            };

            function _has(obj) {
                return angular.isDefined(obj) && obj !== null;
            }

            this.has = function () {
                return _has(sessionService.getSelectedElectionEvent());
            };
        };
    });
