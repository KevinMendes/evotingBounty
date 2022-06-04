/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/**
 * i18n Tasks
 * @module tasks/compile-i18n
 */

const path = require('path');
const gulp = require('gulp');
const gulpif = require('gulp-if');
const buffer = require('vinyl-buffer');
const uglify = require('gulp-uglify');
const gettext = require('gulp-angular-gettext');
const runSequence = require('run-sequence');
const rename = require('gulp-rename');
const foreach = require('gulp-flatmap');
const filesize = require('gulp-size');
const paths = require('../paths.json');

const {
	notificationTask,
	getCommandArguments,
} = require('./helpers');

const argv = getCommandArguments();

/**
 * For each .po file (which indicate the supported languages
 * by the app) copy the related angular i18n file to dist
 *
 * @task i18n:angular
 */
gulp.task('i18n:angular', () => {

	return gulp.src(`${paths.i18n.src}/**/*.po`)
		.pipe(foreach((stream, file) => {

			const fileName = path.basename(file.path, '.po');

			const [
				localeLang,
				localeTerritory,
			] = fileName.split('_');

			const angularI18n = `./node_modules/angular-i18n/angular-locale_${localeLang}${localeTerritory ? '-' + localeTerritory.toLowerCase() : ''}.js`;


			return gulp.src(angularI18n)
				// Needed because angular's locale codes are in the format of xx-xx,
				// while po files are generated with xx_XX. Thus in order to be able
				// to use same locale code in the app, it's easier to rename
				// the angular locale filename than the .po file's header
				.pipe(rename((filePath) => {

					filePath.basename = `${localeLang}${localeTerritory ? '_' + localeTerritory.toUpperCase() : ''}`;

				}));

		}))
		.pipe(gulpif(argv.production, buffer()))
		.pipe(gulpif(argv.production, uglify()))
		.pipe(gulp.dest(paths.i18n.dist));

});


/**
 * Converts translated .po files into angular-compatible
 * JavaScript files and copies them to i18n dist folder
 *
 * @task i18n:po
 */
gulp.task('i18n:po', () => {

	return gulp.src(`${paths.i18n.src}/*.po`)
		// Transform po files to json	files
		.pipe(gettext.compile({
			format: 'json',
		}))
		.pipe(filesize({showFiles: true}))
		.pipe(gulp.dest(paths.i18n.dist));

});

/**
 * Extracts all the annotated strings into a translation template.
 * For more info on how to annotate code to make it translatable:
 * https://angular-gettext.rocketeer.be/dev-guide/annotate/
 * https://angular-gettext.rocketeer.be/dev-guide/annotate-js/
 *
 * @task i18n:pot
 */
gulp.task('i18n:pot', () => {

	return gulp.src([
		`${paths.global.common}/**/*.html`,
		`${paths.global.common}/**/*.js`,
		`${paths.views.src}/**/*.html`,
		`${paths.js.src}/**/*.js`,
	])
		.pipe(gettext.extract('template.pot', {
			// options to pass to angular-gettext-tools...
		}))
		.pipe(gulp.dest(paths.i18n.src));

});

/**
 * Watches for changes in .po files and runs the i18n:po task if any change occurs.
 * If browser-sync task is also running, reloads the app in browser to display the last changes.
 *
 * @task i18n:watch
 */
gulp.task('i18n:watch', () => {

	gulp.watch([`${paths.i18n.src}/**/*.po`], () => {

		runSequence(
			'i18n:po',
			'browser-sync:reload',
			notificationTask(
				'compile-i18n:notify',
				'\uD83C\uDF89 Building done!',
				'Translations have been successfully updated.'
			)
		);

	});

});
