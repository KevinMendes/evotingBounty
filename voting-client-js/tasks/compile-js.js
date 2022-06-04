/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/**
 * Js Tasks
 * @module tasks/compile-js
 */
const gulp = require('gulp');
const eslint = require('gulp-eslint');
const cache = require('gulp-cached');
const browserify = require('browserify');
const babelify = require('babelify');
const gulpif = require('gulp-if');
const sourcemaps = require('gulp-sourcemaps');
const source = require('vinyl-source-stream');
const buffer = require('vinyl-buffer');
const KarmaServer = require('karma').Server;
const filesize = require('gulp-size');
const concat = require('gulp-concat');
const paths = require('../paths.json');
const uglifyjs = require('uglify-es');
const composer = require('gulp-uglify/composer');

const getCommandArguments = require('./helpers').getCommandArguments;
const notificationOnError = require('./helpers').notificationOnError;

const argv = getCommandArguments();

const libBundle = browserify({
	entries: [paths.js.entryFile],
	debug: !argv.production || argv.debug,
	paths: [paths.js.src],
	cache: true,
	transform: [
		babelify.configure({
			sourceMaps: true,
		}),
	]
});

/**
 * Checks and flags code that doesn't correspond to the
 * style guidelines configured under .eslintrc file
 *
 * @task js-lint
 */
const linting = function (done) {

	if (argv['skip-linting']) {

		console.log('[!]   js-lint skipped [!]');
		done();

		return;

	}

	// ESLint ignores files with "node_modules" paths.
	// So, it's best to have gulp ignore the directory as well.
	// Also, Be sure to return the stream from the task;
	// Otherwise, the task may end before the stream has finished.
	return gulp.src([
		paths.js.src + '/**/*.js',
	])
		.pipe(cache('js-lint', {optimizeMemory: true}))
		// eslint() attaches the lint output to the "eslint" property
		// of the file object so it can be used by other modules.
		.pipe(eslint())
		// eslint.format() outputs the lint results to the console.
		// Alternatively use eslint.formatEach() (see Docs).
		.pipe(eslint.format())
		// To have the process exit with an error code (1) on
		// lint error, return the stream and pipe to failOnError last.
		.pipe(gulpif(!argv.force, eslint.failAfterError()))
		.on('error', notificationOnError('\uD83D\uDE25 Linting errors found!', '<%= error.message %>'));

};

const minify = composer(uglifyjs, console);

gulp.task('js-lint', linting);

/**
 * Runs the unit tests test/unit folder
 *
 * @task unit-tests
 */

gulp.task('unit-tests:all', function (done) {
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


gulp.task('unit-tests', gulp.series('unit-tests:all'));

/**
 * Bundles, minifies (if production flag is true), generates the
 * source maps and suffixes project's version to the resulted bundle file
 * for the provided browserify stream.
 *
 * @param {BrowserifyObject} browserifyStream
 * @param {String} bundleFilename The desired filename for the bundle
 */
const compileBundle = function (browserifyStream, bundleFilename, uglifyCode, cb) {
	if (typeof uglifyCode === 'undefined') {
		uglifyCode = true;
	}

	return browserifyStream.bundle(cb)
		.on('error', (e) => {
			console.log(e);
		})
		.pipe(source(bundleFilename))
		.pipe(buffer())
		.pipe(concat(uglifyCode ? paths.js.bundleMinFile : paths.js.bundleFile))
		.pipe(
			gulpif(
				(!argv.production || argv.debug),
				sourcemaps.init({loadMaps: true})
			)
		)
		.pipe(gulpif(uglifyCode, minify()))
		.pipe(
			gulpif(
				(!argv.production || argv.debug),
				sourcemaps.write('./', {includeContent: true, sourceRoot: '/ov-api'})
			)
		)
		.pipe(filesize({showFiles: true}))
		;

};

/**
 * Bundles, minifies (if production arg is true), generates the source maps
 * (if no production arg was passed or debug args is present along with production arg)
 * and suffixes project's version to the resulted bundle file for the provided browserify stream.
 *
 * @task build-js
 */

gulp.task('build-js', function (cb) {

	const destination = gulp.dest(paths.js.dist);

	return compileBundle(libBundle, paths.js.bundleFile, true, cb)
		.pipe(destination);

});

gulp.task('build-js:for-doc', function (cb) {

	return compileBundle(libBundle, paths.js.bundleFile, false, cb)
		.pipe(gulp.dest(paths.js.dist + '/doc/src', {sourcemaps: true}));

});


gulp.task('watch:js', function () {

	gulp.watch(
		[
			paths.js.dist + '/**/*.js',
			paths.js.dist + '/**/*.coffee',
		],
		gulp.series('build-js')
	);

});
