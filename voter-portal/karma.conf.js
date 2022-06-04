/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const istanbul = require('browserify-istanbul');
const paths = require('./paths.json');

module.exports = function (config) {

	config.set({

		// base path that will be used to resolve all patterns (eg. files, exclude)
		basePath: '',

		// frameworks to use
		// available frameworks: https://npmjs.org/browse/keyword/karma-adapter
		frameworks: ['browserify', 'jasmine', 'es6-shim'],

		browsers: ['ChromeHeadlessNoSandbox'],
		customLaunchers: {
			ChromeHeadlessNoSandbox: {
				base: 'ChromeHeadless',
				flags: ['--no-sandbox']
			}
		},

		// list of files / patterns to load in the browser
		files: [
			paths.js.vendorsEntryFile,
			paths.unitTests.angularMocks,
			`${paths.global.vendors}/**/*.js`,
			paths.js.entryFile,
			`${paths.js}/**/*.js`,
			`${paths.views.src}/**/*.html`,
			`${paths.unitTests.baseDir}/**/*.js`,
			{pattern: './src/*.json', watched: false, included: false, served: true},
		],

		// preprocess matching files before serving them to the browser
		// available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
		preprocessors: {
			[paths.js.vendorsEntryFile]: ['browserify'],
			[paths.js.entryFile]: ['browserify'],
			[`${paths.views.src}/**/*.html`]: ['ng-html2js'],
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

		ngHtml2JsPreprocessor: {
			stripPrefix: `${paths.views.baseDir}/`,
			prependPrefix: '',
			moduleName: paths.views.moduleName,
		},

		browserNoActivityTimeout: 100000,

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

		proxies: {
			'/config.json': '/base/src/config.json',
			'/i18n.json': '/base/src/i18n.json',
		},

		// enable / disable colors in the output (reporters and logs)
		colors: true,

		// level of logging
		// possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN
		//                  || config.LOG_INFO || config.LOG_DEBUG
		logLevel: config.LOG_INFO,

		browserConsoleLogOptions: {
			level: 'log',
			format: '%b %T: %m',
			terminal: true,
		},

		// enable / disable watching file and executing tests whenever any file changes
		autoWatch: false,

		// Continuous Integration mode
		// if true, Karma captures browsers, runs the tests and exits
		singleRun: true,

		// Concurrency level
		// how many browser should be started simultaneous
		concurrency: Infinity,
	});

};
