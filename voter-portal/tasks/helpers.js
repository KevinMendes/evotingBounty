/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/**
 * Tasks Helpers
 * @module tasks/helpers
 */

const gulp = require('gulp');
const notify = require('gulp-notify');
const rename = require('gulp-rename');
const fs = require('fs');

const getCommandArguments = () => {

	return require('yargs')
		.boolean(['force', 'production', 'debug', 'skip-unit-tests'])

		.alias('force', 'f')
		.describe('force', 'Prevents tasks to exit on errors')

		.alias('skip-linting', 'sl')
		.describe('skip-linting', 'Doesn\'t run the linter agains js and html files')

		.alias('skip-unit-tests', 'sut')
		.describe('skip-unit-tests', 'Doesn\'t run the unit tests')

		.alias('production', 'prod')
		.describe('production', 'To minify, uglify and many other things for production')

		.describe('debug', 'Useful to debug production code or gulp tasks')

		.choices('type', ['patch', 'minor', 'major', 'prerelease'])
		.alias('type', 't')
		.describe('type', 'Used to indicate types of version increment. To be used only with release task otherwise will have no effect.')
		.help()
		.argv;

};

/**
 * Used to create the notification options to be used by gulp-notify
 *
 * @param   {String}  title   The title of the notification
 * @param   {String}  message The message of the notification
 * @param   {Boolean} onLast  If the notification should only happen on the last file of the stream
 * @returns {Object}          Notification options
 */
const notification = (title, message, isError, onLast) => {

	if (!message) {

		message = title;
		title = 'Notification';

	}

	return {
		title,
		message,
		sound: 'Frog',
		onLast,
	};

};

/**
 * Notifies and ends the stream automatically on error
 *
 * @param {String} title   The title of the notification
 * @param {String} message The message of the notification
 *
 * @example gulp.pipe(...).on('error', notificationOnError('Title', '<%= error.message %>'));
 */
const notificationOnError = (title, message) => {

	return notify.onError(
		notification(title, message, true)
	);

};

const gulpNotify = (title, message) => {

	gulp.src('./')
		.pipe(
			notify(notification(title, message))
		);

};

/**
 * A fake task just to notify on demand whenever needed in a series of tasks that are being ran
 *
 * @param   {String}    taskName  The title of the notification
 * @param   {String}    title     The title of the notification
 * @param   {String}    message   The message of the notification
 * @returns {String}              taskName
 *
 * @example runSequence(
 *   'someTask',
 *   notificationTask('taskName', 'Title', 'Message')
 * );
 */
const notificationTask = (taskName, title, message) => {

	gulp.task(taskName, () => {

		gulpNotify(title, message);

	});

	return taskName;

};


/**
 * Used to handle failed task better. To be used as last parameter for runSequence
 *
 * @param {Function} done Gulp done callback
 */
const handleTaskSequenceError = (done) => {

	return (err) => {

		if (err && !getCommandArguments().debug) {

			return done();

		}

		return done(err);

	};

};

/**
 * Returns the app's version found in package.json
 *
 * @returns {String}
 */
const getPackageVersion = () => {

	return JSON.parse(fs.readFileSync('./package.json', 'utf8')).version;

};

/**
 * Renames a file's name by suffixing the project's version (from package.json) to it.
 * Very helpful for making browsers drop old cached versions of the files,
 * so there will be no strange behaviors due to stale cached files
 *
 * @returns {Stream}
 */
const addVersionToFilename = () => {

	return rename((filePath) => {

		filePath.basename += `-${getPackageVersion()}`;

	});

};

module.exports = {
	getCommandArguments,
	getPackageVersion,
	addVersionToFilename,
	notificationTask,
	gulpNotify,
	notificationOnError,
	handleTaskSequenceError,
};
