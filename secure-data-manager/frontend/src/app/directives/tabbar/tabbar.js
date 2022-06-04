/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('tabBar', [])

    .directive('tabBar', function (sessionService) {
        'use strict';

        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'app/directives/tabbar/tabbar.html',
            scope: {
                filterTabs: '=',
                filterActive: '=',
                onTabSelected: '=',
                filterCounter: '=',
            },
            link: function (scope) {
                // Init
                scope.selectedTab = scope.filterActive;

                scope.filterBy = function (filter) {
                    scope.onTabSelected(filter);
                };
            },
        };
    });
