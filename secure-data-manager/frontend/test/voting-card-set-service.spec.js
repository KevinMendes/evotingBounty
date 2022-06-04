/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
describe("Voting card set service", function () {
	let votingCardSetService, httpBackend;

	beforeEach(module("votingCardSet"));

    beforeEach(inject(function (_votingCardSetService, $httpBackend) {
        votingCardSetService = _votingCardSetService;
        httpBackend = $httpBackend;
    }));

    it("should change status", function () {
        // Define a minimal voting card set.
		const votingCardSet = {
			id: "votingCard",
			electionEvent: {
				id: "electionEvent"
			},
			status: 'LOCKED'
		};
		// Attempt to change status.
        httpBackend.whenPUT("/votingcardset/electionevent/electionEvent/votingcardset/votingCardSet").respond({
            data: {
                status: 'PRECOMPUTED'
            }
        });
        votingCardSetService.changeStatus(votingCardSet, "PRECOMPUTED").then(function (data) {
            expect(data.status).toEqual('PRECOMPUTED');
        });
        httpBackend.flush();
    });

});
