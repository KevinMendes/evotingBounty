/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
(function () {
    'use strict';

    angular
        .module('app.dialogs', [])
        .factory('CustomDialog', function ($mdDialog, gettextCatalog, gettext) {
            return function CustomDialog() {
				let _title = '';
				let _content = '';

				this.title = function (title) {
                    _title = title;
                    return this;
                };

                this.cannotPerform = function (entityName) {
					const CANNOT_PERFORM = gettextCatalog.getString(
						gettext('You cannot perform the action for one or more'),
					);
					const name = ' $NAME. ';
					const review = gettextCatalog.getString(
						gettext('Review your selections'),
					);
					_content = CANNOT_PERFORM.concat(name)
                        .concat(review)
                        .replace('$NAME', entityName);
                    return this;
                };

                this.error = function () {
					const ERROR = gettextCatalog.getString(
						gettext('Something went wrong. Contact with Support'),
					);
					_content = ERROR;
                    return this;
                };

                this.show = function () {
                    return $mdDialog.show(
                        $mdDialog.customAlert({
                            locals: {
                                title: _title,
                                content: _content,
                            },
                        }),
                    );
                };
            };
        });
})();
