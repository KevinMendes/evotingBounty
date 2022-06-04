/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
const gulp = require('gulp');
const del = require('del');
const paths = require('./paths.json');

const handleTaskSequenceError = require('./tasks/helpers').handleTaskSequenceError;

require('./tasks/compile-js');

gulp.task('clean', function () {

	return del([paths.global.dist]);

});

gulp.task('compile', gulp.series('clean', 'build-js'), function (done) {

	handleTaskSequenceError(done);

});

gulp.task('build', gulp.parallel('build-js', 'build-js:for-doc'), function (done) {

	handleTaskSequenceError(done);

});

gulp.task('dev', gulp.series('build', 'watch:js'), function (done) {

	handleTaskSequenceError(done);

});

// Run the gulp
gulp.task('default', function (done) {

	console.log('Executing gulp and getting info from package.json');
	done();

});
