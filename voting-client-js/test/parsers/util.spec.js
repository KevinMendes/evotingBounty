/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

describe('Util functions', function () {
	const ballot = require('./mocks/ballot.json');

	describe('getText()', function () {
		const getText = require('../../src/parsers/util.js').getText;

		it('returns an empty string if translations collection has not been specified', function () {
			const text = getText();

			expect(text).toBe('');
		});

		it('returns an empty string if translation attribute id has not been specified', function () {
			const text = getText(ballot.ballotTexts[0].texts);

			expect(text).toBe('');
		});

		it('returns an empty string if translation attribute property has not been specified', function () {
			const text = getText(
				ballot.ballotTexts[0].texts,
				'220e11ebf42a47088dc5a7cad00bbd9a',
			);

			expect(text).toBe('');
		});

		it('returns an empty string if translation attribute property does not exist', function () {
			const text = getText(
				ballot.ballotTexts[0].texts,
				'dff31b210f734fa3973c43b277d4d39d',
				'missingProp',
			);

			expect(text).toBe('');
		});

		it('returns an empty string if attribute property is not a string and translation key is null', function () {
			const text = getText(
				ballot.ballotTexts[0].texts,
				'220e11ebf42a47088dc5a7cad00bbd9a',
				'data',
			);

			expect(text).toBe('');
		});

		it('returns an empty string if translation key was not found', function () {
			const text = getText(
				ballot.ballotTexts[0].texts,
				'220e11ebf42a47088dc5a7cad00bbd9a',
				'data',
				'missingKey',
			);

			expect(text).toBe('');
		});

		it('returns the translation for a specific attribute property', function () {
			const text = getText(
				ballot.ballotTexts[0].texts,
				'dff31b210f734fa3973c43b277d4d39d',
				'title',
			);

			expect(text).toBe(
				ballot.ballotTexts[0].texts['dff31b210f734fa3973c43b277d4d39d'].title,
			);
		});

		it('returns the translation for a specific key', function () {
			const text = getText(
				ballot.ballotTexts[0].texts,
				'220e11ebf42a47088dc5a7cad00bbd9a',
				'data',
				'firstName',
			);

			expect(text).toBe(
				ballot.ballotTexts[0].texts['220e11ebf42a47088dc5a7cad00bbd9a'].data[0]
					.value,
			);
		});

		it('returns the translation for a specific key index', function () {
			const text = getText(
				ballot.ballotTexts[0].texts,
				'220e11ebf42a47088dc5a7cad00bbd9a',
				'data',
				0,
			);

			expect(text).toBe(
				ballot.ballotTexts[0].texts['220e11ebf42a47088dc5a7cad00bbd9a'].data[0]
					.value,
			);
		});
	});

	describe('parseAttributeTranslations()', function () {
		const mockTranslations = {
			a: 2,
			b: {firstName: 'FIRST_NAME'},
			c: [{firstName: 'FIRST_NAME'}, {lastName: 'LAST_NAME'}],
			d: [
				{
					firstName: 'FIRST_NAME',
					lastName: 'LAST_NAME',
				},
				{middleName: 'MIDDLE_NAME'},
			],
		};

		const parseAttributeTranslations = require('../../src/parsers/util.js')
			.parseAttributeTranslations;

		it('returns an empty object if translations collection has not been specified', function () {
			const texts = parseAttributeTranslations();

			expect(texts).toEqual({});
		});

		it('returns an empty object if translation attribute id has not been specified', function () {
			const texts = parseAttributeTranslations(mockTranslations);

			expect(texts).toEqual({});
		});

		it('returns an empty object if translation attribute id does not exist', function () {
			const texts = parseAttributeTranslations(mockTranslations, 'NO_ID');

			expect(texts).toEqual({});
		});

		it('returns an empty object if translation attribute is not an array', function () {
			const texts = parseAttributeTranslations(mockTranslations, 'a');
			expect(texts).toEqual({});
		});

		it('returns an object representing the translations for an attribute representing an array of simple objects', function () {
			const texts = parseAttributeTranslations(mockTranslations, 'c');

			expect(texts).toEqual({
				firstName: 'FIRST_NAME',
				lastName: 'LAST_NAME',
			});
		});
		it('returns an object representing the translations for an attribute represents a array of more complex objects', function () {
			const texts = parseAttributeTranslations(mockTranslations, 'd');

			expect(texts).toEqual({
				firstName: 'FIRST_NAME',
				middleName: 'MIDDLE_NAME',
				lastName: 'LAST_NAME',
			});
		});
	});
});
