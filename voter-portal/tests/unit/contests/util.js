/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

function addParentsReferences(contest) {

	contest.lists.forEach(list => {

		list.parent = contest;
		list.getQualifiedId = () => list.id;

		list.candidates.forEach(candidate => {

			candidate.parent = list;
			candidate.getQualifiedId = () => candidate.id;

		});

	});

}

module.exports = {
	addParentsReferences,
};
