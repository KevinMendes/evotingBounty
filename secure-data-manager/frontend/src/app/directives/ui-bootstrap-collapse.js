/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/*
 * angular-ui-bootstrap
 * http://angular-ui.github.io/bootstrap/

 * Version: 2.5.0 - 2017-01-28
 * License: MIT
 */
angular
    .module('ui.bootstrap.collapse', [])

    .directive('uibCollapse', [
        '$animate',
        '$q',
        '$parse',
        '$injector',
        function ($animate, $q, $parse, $injector) {
			const $animateCss = $injector.has('$animateCss')
				? $injector.get('$animateCss')
				: null;
			return {
                link: function (scope, element, attrs) {
					const expandingExpr = $parse(attrs.expanding),
						expandedExpr = $parse(attrs.expanded),
						collapsingExpr = $parse(attrs.collapsing),
						collapsedExpr = $parse(attrs.collapsed);
					let horizontal = false,
						css = {},
						cssTo = {};

					init();

                    function init() {
                        horizontal = !!('horizontal' in attrs);
                        if (horizontal) {
                            css = {
                                width: '',
                            };
                            cssTo = {width: '0'};
                        } else {
                            css = {
                                height: '',
                            };
                            cssTo = {height: '0'};
                        }
                        if (!scope.$eval(attrs.uibCollapse)) {
                            element
                                .addClass('in')
                                .addClass('collapse')
                                .attr('aria-expanded', true) // NOSONAR Rule javascript:S1192 - False positive
                                .attr('aria-hidden', false) // NOSONAR Rule javascript:S1192 - False positive
                                .css(css);
                        }
                    }

                    function getScrollFromElement(element) {
                        if (horizontal) {
                            return {width: element.scrollWidth + 'px'};
                        }
                        return {height: element.scrollHeight + 'px'};
                    }

                    function expand() {
                        if (element.hasClass('collapse') && element.hasClass('in')) {
                            return;
                        }

                        $q.resolve(expandingExpr(scope)).then(function () {
                            element
                                .removeClass('collapse')
                                .addClass('collapsing')
                                .attr('aria-expanded', true) // NOSONAR Rule javascript:S1192 - False positive
                                .attr('aria-hidden', false); // NOSONAR Rule javascript:S1192 - False positive

                            if ($animateCss) {
                                $animateCss(element, {
                                    addClass: 'in',
                                    easing: 'ease',
                                    css: {
                                        overflow: 'hidden',
                                    },
                                    to: getScrollFromElement(element[0]),
                                })
                                    .start()
                                    ['finally'](expandDone);
                            } else {
                                $animate
                                    .addClass(element, 'in', {
                                        css: {
                                            overflow: 'hidden',
                                        },
                                        to: getScrollFromElement(element[0]),
                                    })
                                    .then(expandDone);
                            }
                        }, angular.noop);
                    }

                    function expandDone() {
                        element
                            .removeClass('collapsing')
                            .addClass('collapse')
                            .css(css);
                        expandedExpr(scope);
                    }

                    function collapse() {
                        if (!element.hasClass('collapse') && !element.hasClass('in')) {
                            return collapseDone();
                        }

                        $q.resolve(collapsingExpr(scope)).then(function () {
                            element
                                // IMPORTANT: The width must be set before adding "collapsing" class.
                                // Otherwise, the browser attempts to animate from width 0 (in
                                // collapsing class) to the given width here.
                                .css(getScrollFromElement(element[0]))
                                // initially all panel collapse have the collapse class, this removal
                                // prevents the animation from jumping to collapsed state
                                .removeClass('collapse')
                                .addClass('collapsing')
                                .attr('aria-expanded', false) // NOSONAR Rule javascript:S1192 - False positive
                                .attr('aria-hidden', true); // NOSONAR Rule javascript:S1192 - False positive

                            if ($animateCss) {
                                $animateCss(element, {
                                    removeClass: 'in',
                                    to: cssTo,
                                })
                                    .start()
                                    ['finally'](collapseDone);
                            } else {
                                $animate
                                    .removeClass(element, 'in', {
                                        to: cssTo,
                                    })
                                    .then(collapseDone);
                            }
                        }, angular.noop);
                    }

                    function collapseDone() {
                        element.css(cssTo); // Required so that collapse works when animation is disabled
                        element.removeClass('collapsing').addClass('collapse');
                        collapsedExpr(scope);
                    }

                    scope.$watch(attrs.uibCollapse, function (shouldCollapse) {
                        if (shouldCollapse) {
                            collapse();
                        } else {
                            expand();
                        }
                    });
                },
            };
        },
    ]);
