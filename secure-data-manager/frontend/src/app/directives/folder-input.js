/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('ovFolderInputDirective', [])

    .directive('ovFolderInput', function ($parse) {
        'use strict';
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                element.bind('click', function () {
                    angular
                        .element(document.querySelector('#' + attrs.ovFolderInput))[0]
                        .click();
                });
            },
        };
    })
    .directive('ovFolderInputModel', function ($parse) {
        'use strict';
        return {
            restrict: 'A',
            scope: {CtrlFn: '&callback'},
            link: function (scope, element, attrs) {
                function extractPath(e) {
					const path = e.path[0].files[0].path || '';
					scope.CtrlFn({path: path});
                    element.val('');
                }

                element.on('change', extractPath);
            },
        };
    });
