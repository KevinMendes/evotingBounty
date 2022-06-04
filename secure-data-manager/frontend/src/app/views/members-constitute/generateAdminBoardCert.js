/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('generateAdminBoardCert', [
        // 'conf.globals'
    ])
    .factory('generateAdminBoardCert', function (Upload) {
        'use strict';
		const upload = function (url, data) {
			Upload.upload({
				url: url,
				data: {
					file: data.file,
					keystorePassword: data.keystorePassword,
				},
			}).then(data.callbackOK, data.callbackKO);
		};
		return {
            upload: upload,
        };
    });
