/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/**
 * Proxy Server Config file
 */

const paths = require('../paths.json');

module.exports = {
	dev: {
		open: false,
		port: 3020,
		startPath: '/#/legal-terms/',
		baseDir: paths.global.dist,
		proxies: [
			{
				api: '/ag-ws-rest',
				target: 'http://',
				pathRewrite: {
					'^/ag-ws-rest': '/ag-ws-rest',
				},
			},
		],
	},
	qa: {
		open: false,
		port: 3030,
		baseDir: paths.global.dist,
		proxies: [
			{
				api: '/api1',
				target: 'http://someurl.tld:8080/api1',
			},
			{
				api: '/api2',
				target: 'http://someurl.tld:8080/api2',
			},
		],
	},
};
