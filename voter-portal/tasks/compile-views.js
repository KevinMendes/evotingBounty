/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/**
 * Views Tasks
 * @module tasks/compile-views
 */

const gulp = require('gulp');
const gulpif = require('gulp-if');
const cache = require('gulp-cached');
const buffer = require('vinyl-buffer');
const ngHtml2Js = require('gulp-ng-html2js');
const htmlMin = require('gulp-minify-html');
const concat = require('gulp-concat');
const uglify = require('gulp-uglify');
const htmlhint = require('gulp-htmlhint');
const runSequence = require('run-sequence');
const filesize = require('gulp-size');
const sort = require('gulp-sort');

const paths = require('../paths.json');

const {
	getCommandArguments,
	notificationOnError,
	notificationTask,
} = require('./helpers');

const argv = getCommandArguments();

/**
 * Copies static files (like images, fonts etc) into dist/assets folder.
 *
 * @task copy:assets
 */
gulp.task('copy:assets', () => {

	return gulp.src([`${paths.assets.src}/**/*`])
		.pipe(gulp.dest(`${paths.assets.dist}`));

});

/**
 * Copies static files (like images, fonts etc) into dist/assets folder.
 *
 * @task copy:assets
 */
gulp.task('copy:configs', () => {

	return gulp.src([
		`${paths.global.src}/config.json`,
		`${paths.global.src}/i18n.json`,
	])
		.pipe(gulp.dest(`${paths.global.dist}`));

});

/**
 * Checks and flags markup that does not correspond to the
 * style guidelines configured under .htmlhintrc file
 *
 * @task views-lint
 */
gulp.task('views-lint', (done) => {

	if (argv['skip-linting']) {

		console.log('[!] views-lint skipped [!]');

		done();

		return;

	}

	return gulp.src(`${paths.views.src}/**/*.html`)
		.pipe(cache('views-lint', {optimizeMemory: true}))
		.pipe(htmlhint('.htmlhintrc'))
		.pipe(htmlhint.reporter('htmlhint-stylish'))
		.pipe(gulpif(!argv.force, htmlhint.failReporter()))
		.on('error', notificationOnError('\uD83D\uDE25 HTML errors found!', 'Html breaking the rules was found. Check console for details'));

});

/**
 * Minifies .html files and generates an AngularJS module,
 * which pre-loads the HTML code into the $templateCache.
 *
 * @task build-views
 */
gulp.task('build-views', () => {

	return gulp.src([`${paths.views.src}/**/*.html`, `${paths.global.common}/**/*.html`, `!${paths.views.src}/**/index.html`])
		.pipe(sort())
		.pipe(htmlMin({
			empty: true,
			spare: true,
			quotes: true,
		}))
		.pipe(ngHtml2Js({
			moduleName: paths.views.moduleName,
			prefix: paths.views.prefix,
		}))
		.pipe(concat(paths.views.bundleFile))
		.pipe(buffer())
		.pipe(uglify())
		.pipe(filesize({showFiles: true}))
		.pipe(gulp.dest(`${paths.views.dist}`));

});

/**
 * Watches for changes in .html files and for any change that occurs,
 * lints and bundles the .html files and updates the translation
 * template file (.pot) with the annotated strings found in views.
 * If browser-sync task is also running, reloads the app in browser
 * to display the last changes.
 *
 * @task views:watch
 */
gulp.task('views:watch', () => {

	gulp.watch(
		[
			`${paths.global.common}/**/*.html`,
			`${paths.views.src}/**/*.html`,
			`${paths.assets.src}/**/*`,
		],
		() => {

			runSequence(
				'views-lint',
				['build-views', 'copy:assets', 'i18n:pot'],
				'browser-sync:reload',
				notificationTask(
					'compile-views:notify',
					'\uD83C\uDF89 Building done!',
					'Views have been successfully built.'
				)
			);

		}
	);

});

/**
 * Watches for changes in .html files and for any change that occurs,
 * lints and bundles the .html files and updates the translation
 * template file (.pot) with the annotated strings found in views.
 * If browser-sync task is also running, reloads the app in browser
 * to display the last changes.
 *
 * @task views:watch
 */
gulp.task('configs:watch', () => {

	gulp.watch(
		[
			`${paths.global.src}/*.json`,
		],
		() => {

			runSequence(
				'copy:configs',
				'browser-sync:reload',
				notificationTask(
					'compile-views:notify',
					'\uD83C\uDF89 Building done!',
					'Configs have been successfully updated.'
				)
			);

		}
	);

});
