/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('datatableData', [])

    .directive('datatableData', function () {
        'use strict';
        return {
            restrict: 'C',
            link: function (scope, element) {
				const target = element[0].getElementsByTagName('thead');
				element[0].onscroll = function () {
                    target[0].style.transform =
                        'translateY(' + element[0].scrollTop + 'px)';
                    if (element[0].scrollTop > 0) {
                        angular.element(target[0]).addClass('has-shadow');
                    } else {
                        angular.element(target[0]).removeClass('has-shadow');
                    }
                };
            },
        };
    });
