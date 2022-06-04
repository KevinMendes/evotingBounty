/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/**
 * Js Tasks
 * @module tasks/compile-js
 */

const gulp = require('gulp');
const eslint = require('gulp-eslint');
const browserify = require('browserify');
const ngAnnotate = require('browserify-ngannotate');
const babelify = require('babelify');
const gutil = require('gulp-util');
const gulpif = require('gulp-if');
const uglify = require('gulp-uglify');
const sourcemaps = require('gulp-sourcemaps');
const source = require('vinyl-source-stream');
const buffer = require('vinyl-buffer');
const runSequence = require('run-sequence');
const KarmaServer = require('karma').Server;
const protractor = require('gulp-protractor').protractor;
const filesize = require('gulp-size');

const paths = require('../paths.json');

const {
	getCommandArguments,
	notificationTask,
	notificationOnError,
} = require('./helpers');

const argv = getCommandArguments();

/**
 * Checks and flags code that doesn't correspond to the
 * style guidelines configured under .eslintrc file
 *
 * @task js-lint
 */
gulp.task('js-lint', (done) => {

	if (argv['skip-linting']) {

		console.log('[!] js-lint skipped [!]');

		done();

		return;

	}

	// ESLint ignores files with "node_modules" paths.
	// So, it's best to have gulp ignore the directory as well.
	// Also, Be sure to return the stream from the task;
	// Otherwise, the task may end before the stream has finished.
	return gulp.src([
		`${paths.js.src}/**/*.js`,
		`${paths.global.common}/**/*.js`,
		'!./doc/**',
		`!${paths.global.node_modules}/**`,
		`!${paths.global.dist}/**`,
		`!${paths.global.vendors}/**`,
	])
		.pipe(eslint())
		.pipe(eslint.format())
		.pipe(gulpif(!argv.force, eslint.failAfterError()))
		.on('error', notificationOnError('\uD83D\uDE25 Linting errors found!', '<%= error.message %>'));

});

/**
 * Runs the unit tests test/unit folder
 *
 * @task unit-tests
 */
gulp.task('unit-tests', (done) => {

	if (argv['skip-unit-tests']) {

		console.log('[!] unit-tests skipped [!]');

		done();

		return;

	}

	new KarmaServer({
		configFile: __dirname + '/../karma.conf.js',
		singleRun: true,
	}, done).start();

});

/**
 * Runs the unit tests test/unit folder
 *
 * @task e2e-tests
 */
gulp.task('e2e-tests', () => {

	gulp.src(['./test/e2e/**/*.js'])
		.pipe(protractor({
			configFile: 'protractor.conf.js',
			args: ['--baseUrl', argv.baseUrl],
		}))
		.on('error', function (e) {

			throw e;

		});

});

/**
 * Bundles, minifies (if production flag is true), generates the
 * source maps and suffixes project's version to the resulted bundle file
 * for the provided browserify stream.
 *
 * @param {BrowserifyObject} browserifyStream
 * @param {String} bundleFilename The desired filename for the bundle
 */
const createBundle = (browserifyStream, bundleFilename, noSourceMap = false) => {

	return browserifyStream.bundle()
		.on('error', gutil.log)
		.pipe(source(bundleFilename))
		.pipe(buffer())
		.pipe(
			gulpif(
				(!argv.production || argv.debug) && !noSourceMap,
				sourcemaps.init({loadMaps: true})
			)
		)
		.pipe(
			gulpif(
				argv.production,
				uglify()
			)
		)
		.pipe(
			gulpif(
				(!argv.production || argv.debug) && !noSourceMap,
				sourcemaps.write('./', {includeContent: true, sourceRoot: '/'})
			)
		)
		.pipe(filesize({showFiles: true}));

};

/**
 * Bundles, minifies (if production arg is true), generates the source maps
 * (if no production arg was passed or debug args is present along with production arg)
 * and suffixes project's version to the resulted bundle file for the provided browserify stream.
 *
 * @task build-js
 */
gulp.task('build-js', () => {

	const b = browserify({
		entries: [paths.js.entryFile],
		debug: !argv.production || argv.debug,
		paths: [paths.js.src],
		cache: true,
		transform: [
			babelify.configure({
				presets: ['@babel/preset-env'],
				sourceMaps: true,
			}),
			ngAnnotate,
		],
	});

	return createBundle(b, `${paths.js.bundleFile}`)
		.pipe(gulp.dest(`${paths.js.dist}`));

});

/**
 * Bundles the modules required by `src/app/vendors.js` (as npm modules)
 * and creates a separate bundle with them. This way we can better
 * orchestrate the order of loading our scripts in the app.
 *
 * @task build-js:compile-vendors
 */
gulp.task('build-js:compile-vendors', () => {

	const b = browserify({
		entries: paths.js.vendorsEntryFile,
		debug: !argv.production,
		paths: [paths.js.src],
		cache: true,
	});

	return createBundle(b, paths.js.vendorsBundleFile, true)
		.pipe(gulp.dest(`${paths.js.dist}`));

});

gulp.task('build-js:vendors', gulp.series('build-js:compile-vendors'));

gulp.task('build-js:ov-api', () => {

	return gulp.src([paths.global.ovApi + '/*.js'])
		.pipe(gulp.dest(`${paths.js.dist}/crypto`));

});

/**
 * Watches for changes in any file found under src/app folder and runs the linting, js building
 * and i18n pot compiling tasks if any change occurs. If browser-sync task is also running,
 * reloads the app in browser to display the last changes.
 *
 * @task js:watch
 */
gulp.task('js:watch', () => {

	return gulp.watch([
		`${paths.js.src}/**/*.js`,
		`${paths.global.common}/**/*.js`,
		`${paths.global.ovApi}/**/*.js`,
		`!${paths.js.vendorsEntryFile}`,
	], () => {

		runSequence(
			'js-lint',
			['build-js', 'i18n:pot'],
			'browser-sync:reload',
			notificationTask(
				'compile-js:notify',
				'\uD83C\uDF89 Building done!',
				'All .js and .pot files have been successfully built.'
			)
		);

	});

});

/**
 * Watches for changes in vendors.js file found under src/app folder and
 * bundles the references found there. If browser-sync task is also running,
 * reloads the app in browser to display the last changes.
 *
 * @task js-vendors:watch
 */
gulp.task('js-vendors:watch', () => {

	gulp.watch([paths.js.vendorsEntryFile], () => {

		runSequence(
			'build-js:vendors',
			'browser-sync:reload',
			notificationTask(
				'compile-js:notify',
				'\uD83C\uDF89 Building done!',
				'Vendors scripts been successfully built.'
			)
		);

	});

});
