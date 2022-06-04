/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('routes', ['ui.router'])
    .config(function ($urlRouterProvider, $stateProvider, $locationProvider) {
        'use strict';

        // Keep the URL without prefix '!'
        $locationProvider.hashPrefix('');

        $urlRouterProvider.otherwise('/splash');

        $stateProvider

            .state('body', {
                abstract: true,
            })

            .state('splash', {
                parent: 'body',
                url: '/splash',
                views: {
                    'view@body': {
                        templateUrl: 'app/views/splash/splash.html',
                        controller: 'splash',
                    },
                },
            })

            .state('home-manage', { // NOSONAR Rule javascript:S1192 - False positive
                parent: 'body',
                views: {
                    'view@body': {
                        templateUrl: 'app/views/home-manage/home-manage.html',
                        controller: 'home-manage', // NOSONAR Rule javascript:S1192 - False positive
                    },
                },
            })

            // home-manage - election-event-list
            .state('election-event-list', {
                parent: 'home-manage', // NOSONAR Rule javascript:S1192 - False positive
                url: '/election-event-list',
                views: {
                    'election-event-list': {
                        templateUrl:
                            'app/views/election-event-list/election-event-list.html',
                        controller: 'election-event-list',
                    },
                },
            })

            // home-manage - administration-board-list
            .state('administration-board-list', {
                parent: 'home-manage', // NOSONAR Rule javascript:S1192 - False positive
                url: '/administration-board-list',
                views: {
                    'administration-board-list': {
                        templateUrl:
                            'app/views/administration-board-list/administration-board-list.html',
                        controller: 'administration-board-list',
                    },
                },
            })

            // election-event-manage ========================================
            // ==============================================================
            .state('election-event-manage', { // NOSONAR Rule javascript:S1192 - False positive
                parent: 'body',
                url: '/election-event-manage',
                views: {
                    'view@body': {
                        templateUrl:
                            'app/views/election-event-manage/election-event-manage.html',
                        controller: 'election-event-manage', // NOSONAR Rule javascript:S1192 - False positive
                    },
                },
            })

            // election-event-manage - ballots
            .state('ballots', {
                parent: 'election-event-manage', // NOSONAR Rule javascript:S1192 - False positive
                url: '/ballots',
                views: {
                    ballots: {
                        templateUrl: 'app/views/ballots/ballots.html',
                        controller: 'ballots',
                    },
                },
            })

            // election-event-manage - voting-cards
            .state('voting-cards', {
                parent: 'election-event-manage', // NOSONAR Rule javascript:S1192 - False positive
                url: '/voting-cards',
                views: {
                    'voting-cards': {
                        templateUrl: 'app/views/voting-cards/voting-cards.html',
                        controller: 'voting-cards',
                    },
                },
            })

            // election-event-manage - electoral-authorities
            .state('electoral-authorities', {
                parent: 'election-event-manage', // NOSONAR Rule javascript:S1192 - False positive
                url: '/electoral-authorities',
                views: {
                    'electoral-authorities': {
                        templateUrl:
                            'app/views/electoral-authorities/electoral-authorities.html',
                        controller: 'electoral-authorities',
                    },
                },
            })

            // election-event-manage - ballots-boxes
            .state('ballot-boxes', {
                parent: 'election-event-manage', // NOSONAR Rule javascript:S1192 - False positive
                url: '/ballot-boxes',
                views: {
                    'ballot-boxes': {
                        templateUrl: 'app/views/ballot-boxes/ballot-boxes.html',
                        controller: 'ballot-boxes',
                    },
                },
            });
    });
