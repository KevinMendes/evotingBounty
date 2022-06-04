/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/**
 * Index Tasks
 * @module tasks/compile-index
 */

const gulp = require('gulp');
const inject = require('gulp-inject');

const paths = require('../paths.json');

const {
	notificationTask,
} = require('./helpers');

/**
 * Copies index.html file to dist folder
 *
 * @task index:copy
 */
gulp.task('index:copy', () => {

	return gulp.src(paths.index.src)
		.pipe(gulp.dest(paths.global.dist));

});

/**
 * Injects all the assets into the placeholders specified in index.html
 *
 *  @task inject-files-index
 */
gulp.task('inject-files-index', () => {

	const appFiles = gulp.src(
		[
			`${paths.global.dist}/*.js`,
			`${paths.global.dist}/**/*.css`,
			`!${paths.global.dist}/**/*custom*.css`,
			`!${paths.global.dist}/*vendors*.js`,
			`!${paths.global.dist}/autoMate.js`,
		],
		{read: false}
	);

	const vendorFiles = gulp.src(
		[
			`${paths.global.dist}/*vendors*.js`,
			`${paths.global.dist}/crypto/*`,
		],
		{read: false}
	);

	// css/custom.css has to be loaded after app's css.
	// Make sure the custom injection placeholder (<!-- custom:css -->)
	// in index.html is after the app's one (<!-- app:css -->)
	const custom = gulp.src(
		[
			`${paths.global.dist}/**/*custom*.css`,
		],
		{read: false}
	);


	return gulp.src(paths.index.dist)
		.pipe(inject(vendorFiles, {relative: true, name: 'vendors'}))
		.pipe(inject(appFiles, {relative: true, name: 'app'}))
		.pipe(inject(custom, {relative: true, name: 'custom'}))
		.pipe(gulp.dest(paths.global.dist));

});

/**
 * Call the compy of index and inject
 *
 *  @task build-index
 */
gulp.task('build-index', gulp.series('index:copy', 'inject-files-index'));

/**
 * Watches for changes in index.html file and runs the build-index task if any change occurs.
 * If browser-sync task is also running, reloads the app in browser to display the last changes.
 *
 * @task index:watch
 */
gulp.task('index:watch', () => {

	gulp.watch(paths.index.src, () => {

		gulp.series(
			'build-index',
			'browser-sync:reload',
			notificationTask(
				'build-index:notify',
				'\uD83C\uDF89 Building done!',
				'Index has been successfully built.'
			)
		);

	});

});
