/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
const paths = require('./paths.json');
const istanbul = require('browserify-istanbul');
module.exports = function (config) {

	config.set({

		basePath: '',

		frameworks: ['browserify', 'jasmine', 'es6-shim'],

		// Possible browsers: 'FirefoxHeadless', 'ChromeHeadless', 'ChromeHeadlessNoSandbox'
		// NOTE: For "Windows Subsystem for Linux", only 'ChromeHeadlessNoSandbox' is presently
		// available, and environment variable CHROME_BIN must be set to the Windows Google Chrome
		// executable path. See the "readme.md" file for details.
		browsers: ['ChromeHeadlessNoSandbox'],
		customLaunchers: {
			ChromeHeadlessNoSandbox: {
				base: 'ChromeHeadless',
				flags: ['--no-sandbox']
			}
		},

		files: [
			paths.js.entryFile,
			`${paths.unitTests.baseDir}/mocks/testdata.js`,
			`${paths.unitTests.baseDir}/**/*.spec.js`,
		],

		preprocessors: {
			[paths.js.entryFile]: ['browserify'],
			[`${paths.unitTests.baseDir}/**/*.js`]: ['browserify'],
		},

		browserify: {
			debug: true,
			transform: [
				[
					istanbul({
						ignore: ['tests/**', '**/node_modules/**', 'dist/**'],
					}),
				],
				'babelify',
			],
			extensions: ['.js'],
		},

		reporters: ['spec', 'coverage'],

		coverageReporter: {
			reporters: [
				{
					type: 'lcov',
					dir: 'coverage',
					subdir: '.',
				},
				{
					type: 'text-summary',
				},
			],
		},

		singleRun: true,

		browserDisconnectTolerance: 1,
		browserDisconnectTimeout: 100000,
		browserNoActivityTimeout: 100000,
		captureTimeout: 100000,

		// level of logging
		// possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN
		//                  || config.LOG_INFO || config.LOG_DEBUG
		logLevel: config.LOG_INFO,

	});

};
