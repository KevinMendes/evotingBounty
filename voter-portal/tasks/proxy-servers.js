/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/**
 * Proxy Servers Tasks
 * @module tasks/proxy-servers
 */

const gulp = require('gulp');
const browserSync = require('browser-sync');
const proxy = require('http-proxy-middleware');

const servers = require('./proxy-servers.config');

const {
	gulpNotify,
} = require('./helpers');

gulp.task('browser-sync', () => {

	Object.keys(servers).forEach((serverName) => {

		browserSync.create(serverName);

		const proxies = servers[serverName].proxies.map((p) => {

			return proxy(
				p.api,
				Object.assign(
					{},
					{
						changeOrigin: true,
						logLevel: 'debug',
						secure: false,
					},
					p
				)
			);

		});

		const serverConfig = Object.assign(
			{},
			{
				ui: {
					port: servers[serverName].port + 1,
				},
				logLevel: 'info',
				notify: true,
				logConnections: true,
				ghostMode: false,
				logFileChanges: true,
				logPrefix: serverName,
				server: {
					baseDir: servers[serverName].baseDir,
					middleware: proxies,
				},
			},
			servers[serverName]
		);

		browserSync.get(serverName).init(serverConfig);

	});

});

/**
 * Reloads the app in browser to display the last changes
 */
const reloadBrowsers = () => {

	Object.keys(servers).forEach((serverName) => {

		browserSync.get(serverName).reload();

	});

};

/**
 * Task that will reload all the browser instances opened with browser-sync
 *
 * @task browser-sync:reload
 */
gulp.task('browser-sync:reload', (done) => {

	reloadBrowsers();

	done();

});
