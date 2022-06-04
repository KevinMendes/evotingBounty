/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
describe('path-converter.js', function () {

    beforeEach(module('pathConverter'));

	let pathConverter;
	const UNIX_TEST_PATH = "/my/test/path";
	const WIN_TEST_PATH = "C:\\my\\test\\path";

	beforeEach(inject(function (_pathConverter) {
        pathConverter = _pathConverter;
    }));

    it('should convert Unix file paths to folder path', function () {

		let pathResult = pathConverter.toFolderPath("/my/test/path/file.json");
		expect(pathResult).toEqual(UNIX_TEST_PATH);

        pathResult = pathConverter.toFolderPath("/my/other.test/path123/my-other_file123.json");
        expect(pathResult).toEqual("/my/other.test/path123");

        pathResult = pathConverter.toFolderPath("/my/other test/path 123/file with spaces.json");
        expect(pathResult).toEqual("/my/other test/path 123");

    });

    it('should convert Windows file paths to folder path', function () {

		let pathResult = pathConverter.toFolderPath("C:\\my\\test\\path\\file.json");
		expect(pathResult).toEqual(WIN_TEST_PATH);

        pathResult = pathConverter.toFolderPath("C:\\my\\other_test\\path-123\\my-other_file123.json");
        expect(pathResult).toEqual("C:\\my\\other_test\\path-123");

        pathResult = pathConverter.toFolderPath("C:\\my\\other test\\path 123\\file with spaces.json");
        expect(pathResult).toEqual("C:\\my\\other test\\path 123");

    });

    it('should keep Unix folder paths unchanged', function () {

		let pathResult = pathConverter.toFolderPath("/my/test/path/");
		expect(pathResult).toEqual("/my/test/path/");

        pathResult = pathConverter.toFolderPath(UNIX_TEST_PATH);
        expect(pathResult).toEqual(UNIX_TEST_PATH);

    });

    it('should keep Windows folder paths unchanged', function () {

		let pathResult = _pathConverter.toFolderPath("C:\\my\\test\\path\\");
		expect(pathResult).toEqual("C:\\my\\test\\path\\");

        pathResult = _pathConverter.toFolderPath(WIN_TEST_PATH);
        expect(pathResult).toEqual(WIN_TEST_PATH);
    });

});
