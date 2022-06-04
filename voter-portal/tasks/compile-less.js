/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/**
 * Less Tasks
 * @module tasks/compile-sass
 */

const gulp = require('gulp');
const less = require('gulp-less');
const sourcemaps = require('gulp-sourcemaps');
const runSequence = require('run-sequence');
const filesize = require('gulp-size');
const concat = require('gulp-concat');
const gulpif = require('gulp-if');
const cleanCSS = require('gulp-clean-css');
const stripCSSComments = require('gulp-strip-css-comments');

const {
	getCommandArguments,
	notificationTask,
	notificationOnError,
} = require('./helpers');

const argv = getCommandArguments();

const paths = require('../paths.json');

/**
 * Preprocess .less files and copies
 * the resulted .css files in dist/css folder.
 *
 * @task compile:less
 */
gulp.task('compile:less', () => {

	return gulp.src(`${paths.less.src}/${paths.less.entryFile}`)
		.pipe(concat(paths.less.bundleFile))
		.pipe(gulpif(!argv.production || argv.debug, sourcemaps.init()))
		.pipe(less().on('error', notificationOnError('\uD83D\uDE25 Linting errors found!', '<%= error.message %>')))
		.pipe(cleanCSS())
		.pipe(stripCSSComments({preserve: false}))
		.pipe(gulpif(!argv.production || argv.debug, sourcemaps.write('./', {includeContent: true})))
		.pipe(filesize({showFiles: true}))
		.pipe(gulp.dest(`${paths.less.dist}`));

});

/**
 * Preprocess .less files and copies
 * the resulted .css files in dist/css folder.
 *
 * @task compile:less
 */
gulp.task('copy:styles', () => {

	return gulp.src(`${paths.css.src}/**/*`)
		.pipe(cleanCSS())
		.pipe(stripCSSComments({preserve: false}))
		.pipe(gulp.dest(`${paths.css.dist}`));

});

/**
 * Watches for changes in any .less file and runs the compile:less task if any change occurs.
 * If browser-sync task is also running, reloads the app in browser to display the last changes.
 *
 * @task less:watch
 */
gulp.task('less:watch', () => {

	gulp.watch(`${paths.less.src}/**/*.less`, () => {

		runSequence(
			'compile:less',
			'browser-sync:reload',
			notificationTask(
				'compile-less:notify',
				'\uD83C\uDF89 Building done!',
				'Styles have been successfully built.'
			)
		);

	});

});
