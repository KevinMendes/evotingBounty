/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('dialogsCustom', [])

    .config([
        '$mdDialogProvider',
        function ($mdDialogProvider) {
            $mdDialogProvider.addPreset('customAlert', {
                options: function () {
                    return {
                        templateUrl: 'app/views/dialogs/dialog-custom-alert-template.html',
                        controller: DialogController,
                        clickOutsideToClose: true,
                        escapeToClose: true,
                    };
                },
            });
            $mdDialogProvider.addPreset('customConfirm', {
                options: function () {
                    return {
                        templateUrl:
                            'app/views/dialogs/dialog-custom-confirm-template.html',
                        controller: DialogController,
                        clickOutsideToClose: false,
                        escapeToClose: true,
                    };
                },
            });

            function DialogController($scope, $mdDialog, locals) {
                $scope.title = locals.title;
                $scope.content = locals.content;
                $scope.ok = locals.ok;
                $scope.cancel = function () {
                    $mdDialog.cancel();
                };
                $scope.hide = function () {
                    $mdDialog.hide();
                };
            }
        },
    ]);
