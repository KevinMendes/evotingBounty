/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const gulp = require('gulp');
const del = require('del');
const {notificationTask} = require('./tasks/helpers');

const {
	getCommandArguments,
	addVersionToFilename,
	handleTaskSequenceError,
} = require('./tasks/helpers');

const paths = require('./paths.json');

require('./tasks/compile-js');
require('./tasks/compile-views');
require('./tasks/compile-less');
require('./tasks/compile-index');
require('./tasks/compile-i18n');
require('./tasks/proxy-servers');

gulp.task('clean', () => {

	return del([paths.global.dist]);

});

gulp.task('lint', (done) => {

	const sanityCheckTasks = [];

	if (!getCommandArguments()['skip-linting']) {

		sanityCheckTasks.push([
			'js-lint',
			'views-lint',
		]);

	} else {

		console.log('[!] Linting skipped [!]');

	}

	if (!sanityCheckTasks.length) {

		done();

		return;

	}

	gulp.series(
		...sanityCheckTasks,
		handleTaskSequenceError(done)
	);

});

gulp.task('test', (done) => {

	gulp.series(
		['lint', 'unit-tests'],
		handleTaskSequenceError(done)
	);

});

gulp.task('build', gulp.series(
	'build-js',
	'build-js:vendors',
	'build-js:ov-api',
	'build-views',
	'compile:less',
	'copy:styles',
	'copy:assets',
	'copy:configs',
	'i18n:pot',
	'i18n:po',
	'build-index'),
	(done) => {
		handleTaskSequenceError(done);
		done();
	});

gulp.task('compile', gulp.series(
	'clean',
	'build',
), (done) => {

	handleTaskSequenceError(done);
	done();

});

gulp.task('dev', (done) => {

	return gulp.series(
		'test',
		'compile',
		'browser-sync',
		['js:watch', 'js-vendors:watch', 'less:watch', 'views:watch', 'configs:watch', 'i18n:watch', 'index:watch'],
		notificationTask('dev:notify', '\uD83C\uDF89 Success!', 'App is up and running!'),
		handleTaskSequenceError(done)
	);
	done();

});

gulp.task('default', gulp.series('dev'));
