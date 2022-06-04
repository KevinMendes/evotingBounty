/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
describe('Ballot printer ...', function () {

    'use strict';

    beforeEach(module('lodash'));
    beforeEach(module('ballotprinter'));

	const response = [{

		"id": "3c37a341fd724e36b117004619142782",
		"electionEvent": {
			"id": "90053f72699049298cf39f1b13daf9c8"
		},
		"defaultTitle": "LEGISLATIVE 2017 - T1 CC 09",
		"defaultDescription": "09",
		"alias": "09",
		"contests": [{
			"id": "0a220b1a566a4ac28f6e6faaad5c267d",
			"defaultTitle": "LEGISLATIVE 2017 - T1 CC 09",
			"defaultDescription": "09",
			"alias": "09",
			"electionEvent": {
				"id": "90053f72699049298cf39f1b13daf9c8"
			},
			"type": "votation",
			"options": [{
				"id": "7a3ab02e7dea414aac881e9dcd5007f7",
				"defaultText": "eyJvcmRlciI6MSwiY2FuZGlkYXRlIjp7Im5hbWUiOiJQcsOpbm9tIDA5MDEiLCJzdXJuYW1lIjoiTEVHSVNMQVRJVkUgMDkwMSIsInRpdGxlIjoiTS4" +
					"ifSwiYnVsbGV0aW4iOiIwOTEwOV9CdWxsZXRpbi5wbmciLCJjaXJjdWxhciI6IjA5MTA5X0NpcmN1bGFyLnBkZiIsImFsdGVybmF0ZSI6eyJuYW1lIjoiUHLDqW5vb" +
					"SBTIDA5MDEiLCJzdXJuYW1lIjoiTEVHSVNMQVRJVkUgUyAwOTAxIiwidGl0bGUiOiJNLiJ9fQ==",
				"alias": "091",
				"representation": "2",
				"attributes": [
					"bf75541c34874d24b3f2f72ebc815b6f",
					"3b2a6f9132114619a664f5b742b9aabf"
				]
			}, {
				"id": "70bf9d742a0c424695c6392f1c90bf66",
				"defaultText": "tralala",
				"alias": "092",
				"representation": "3",
				"attributes": [
					"bf75541c34874d24b3f2f72ebc815b6f",
					"3b2a6f9132114619a664f5b742b9aabf"
				]
			}],
			"optionsAttributes": [{
				"id": "3b2a6f9132114619a664f5b742b9aabf",
				"type": "contestgroup",
				"correctness": true
			}, {
				"id": "bf75541c34874d24b3f2f72ebc815b6f",
				"defaultText": "LEGISLATIVE 2017 - T1 - Question",
				"type": "group",
				"alias": "Test_Question_1",
				"correctness": true
			}],
			"clauses": [{
				"id": "fdc166491b4243f3966c3821abbc5cc1",
				"refersTo": "3b2a6f9132114619a664f5b742b9aabf",
				"type": "MIN",
				"value": 1
			}, {
				"id": "cbeb2d64956647a0b9a09d7905b5b6cf",
				"refersTo": "3b2a6f9132114619a664f5b742b9aabf",
				"type": "MAX",
				"value": 1
			}, {
				"id": "7e8e90c9f1bd46f79fc2c7cc9e16764c",
				"refersTo": "bf75541c34874d24b3f2f72ebc815b6f",
				"type": "MIN",
				"value": 1
			}, {
				"id": "4f180a291c5941e7b0f458a8cf4b7470",
				"refersTo": "bf75541c34874d24b3f2f72ebc815b6f",
				"type": "MAX",
				"value": 1
			}]
		}]
	}];


	describe('should expand option.defaultText to ...', function () {

        beforeEach(inject(function (ballotprinter) {
            ballotprinter.print(response);
        }));

        it('candidate data if it is valid b64 encoded JSON', function () {

            expect(response[0].contests[0].options[0].defaultText).toEqual({
                order: 1,
                candidate: Object({
                    name: 'Prénom 0901',
                    surname: 'LEGISLATIVE 0901',
                    title: 'M.'
                }),
                bulletin: '09109_Bulletin.png',
                circular: '09109_Circular.pdf',
                alternate: Object({
                    name: 'Prénom S 0901',
                    surname: 'LEGISLATIVE S 0901',
                    title: 'M.'
                })
            });

        });

        it('nothing if not valid b64 or JSON', function () {

            expect(response[0].contests[0].options[1].defaultText).toBe('tralala');

        });
    });

});
