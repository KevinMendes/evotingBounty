/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

const istanbul = require('browserify-istanbul');

module.exports = function (config) {
	'use strict';
	config.set({
		basePath: '.',
		singleRun: true,
		logLevel: config.LOG_INFO,
		browserConsoleLogOptions: {
			terminal: true,
			level: ""
		},
		browserNoActivityTimeout: 5 * 60 * 1000,
		browserDisconnectTimeout: 60 * 1000,
		frameworks: ['browserify', 'jasmine'],
		files: [
			'asymmetric/**/*.js',
			'bitwise/**/*.js',
			'certificate/**/*.js',
			'codec/**/*.js',
			'cryptopolicy/**/*.js',
			'elgamal/**/*.js',
			'extendedkeystore/**/*.js',
			'mathematical/**/*.js',
			'messagedigest/**/*.js',
			'pbkdf/**/*.js',
			'securerandom/**/*.js',
			'symmetric/**/*.js'
		],
		browsers: ['ChromeHeadlessNoSandbox'],
		customLaunchers: {
			ChromeHeadlessNoSandbox: {
				base: 'ChromeHeadless',
				flags: ['--no-sandbox']
			}
		},
		preprocessors: {
			'../src/asymmetric/**/*.js': ['browserify'], 'asymmetric/**/*.js': ['browserify'],
			'../src/bitwise/**/*.js': ['browserify'], 'bitwise/**/*.js': ['browserify'],
			'../src/certificate/**/*.js': ['browserify'], 'certificate/**/*.js': ['browserify'],
			'../src/codec/**/*.js': ['browserify'], 'codec/**/*.js': ['browserify'],
			'../src/cryptopolicy/**/*.js': ['browserify'], 'cryptopolicy/**/*.js': ['browserify'],
			'../src/elgamal/**/*.js': ['browserify'], 'elgamal/**/*.js': ['browserify'],
			'../src/extendedkeystore/**/*.js': ['browserify'], 'extendedkeystore/**/*.js': ['browserify'],
			'../src/mathematical/**/*.js': ['browserify'], 'mathematical/**/*.js': ['browserify'],
			'../src/messagedigest/**/*.js': ['browserify'], 'messagedigest/**/*.js': ['browserify'],
			'../src/pbkdf/**/*.js': ['browserify'], 'pbkdf/**/*.js': ['browserify'],
			'../src/securerandom/**/*.js': ['browserify'], 'securerandom/**/*.js': ['browserify'],
			'../src/symmetric/**/*.js': ['browserify'], 'symmetric/**/*.js': ['browserify']
		},
		browserify: {
			debug: true,
			transform: [
				[
					istanbul({
						ignore: ['**/node_modules/**'],
					}),
				]
			],
			extensions: ['.js'],
		},
		reporters: ['progress', 'coverage'],
		coverageReporter: {
			reporters: [
				{
					type: 'json',
					dir: '../coverage',
					subdir: 'unit-run',
					file: 'unit-run-1.json'
				}
			],
		},
	});
};
