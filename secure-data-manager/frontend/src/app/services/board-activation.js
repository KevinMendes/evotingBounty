/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('boardActivation', [
        // 'conf.globals'
    ])

    .factory('boardActivation', function (
        sessionService,
        $mdDialog,
        gettextCatalog,
        $templateCache,
        $http,
        endpoints,
    ) {
        'use strict';

		let selectedAuthority = null,
			listOfMembers = null,
			boardIssuerPublicKey = null,
			boardSubjectPublicKey = null,
			sharesType = null,
			adminboardid = null;

		function init(type) {
            sharesType = type;

            if (sharesType == 'adminBoard') {
                adminboardid = sessionService.getSelectedElectionEvent()
                    .administrationAuthority.id;

				let adminauthority;
				sessionService.adminBoards.map(function (board) {
                    if (board.id === adminboardid) {
                        adminauthority = board;
                    }
                });

                selectedAuthority = adminauthority;
                listOfMembers = adminauthority.administrationBoard;
            }

            selectedAuthority.ready = false;
        }

        function successCallback(response) {
            selectedAuthority.ready = true;

            if (sharesType == 'adminBoard') {
                sessionService.setSelectedAdminBoard(selectedAuthority);
            }

            boardIssuerPublicKey = response.data.issuerPublicKeyPEM;
            boardSubjectPublicKey = response.data.serializedSubjectPublicKey;
        }

        function errorCallback(error) {
            console.log('Error callback: ' + sharesType + ' ', error);
            return error;
        }

        function adminBoardActivate() {
            if (adminboardid.length > 0) {
                try {
					const url =
						endpoints.host() +
						endpoints.activateAdminBoardShare.replace(
							'{adminBoardId}',
							adminboardid,
						);
					return $http
                        .post(url)
                        .then(successCallback)
                        .catch(errorCallback);
                } catch (e) {
                    console.log('Error', e);
                }
            }
        }

        /**
         * Opens the admin board for activating
         * @return {Promise}
         */
        function openBoardAdmin() {
            return $mdDialog.show({
                controller: 'reconstructMembers',
                template: $templateCache.get(
                    'app/views/members-reconstruct/members-reconstruct.html',
                ),
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                sessionService: sessionService,
                escapeToClose: false,
                preserveScope: true,
            });
        }

        /**
         * Get the listOfMembers of the board
         * @return {Object} listOfMembers
         */
        function getListOfMembers() {
            return listOfMembers;
        }

        /**
         * Get the selectedAuthority of the board
         * @return {Object} selectedAuthority
         */
        function getSelectedAuthority() {
            return selectedAuthority;
        }

        /**
         * Get the type of the board
         * @return {String} sharesType
         */
        function getSharesType() {
            return sharesType;
        }

        /**
         * Get the board issuer public key of the board
         * @return {String} boardIssuerPublicKey
         */
        function getBoardIssuerPublicKey() {
            return boardIssuerPublicKey;
        }

        /**
         * Get the board subject public key of the board
         * @return {String} boardSubjectPublicKey
         */
        function getBoardSubjectPublicKey() {
            return boardSubjectPublicKey;
        }

        return {
            init: init,
            adminBoardActivate: adminBoardActivate,
            openBoardAdmin: openBoardAdmin,
            selectedAuthority: selectedAuthority,
            listOfMembers: listOfMembers,
            getListOfMembers: getListOfMembers,
            getSelectedAuthority: getSelectedAuthority,
            getBoardIssuerPublicKey: getBoardIssuerPublicKey,
            getBoardSubjectPublicKey: getBoardSubjectPublicKey,
            getSharesType: getSharesType,
            sharesType: sharesType,
            boardIssuerPublicKey: boardIssuerPublicKey,
            boardSubjectPublicKey: boardSubjectPublicKey,
        };
    });
