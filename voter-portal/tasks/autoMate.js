/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

setTimeout(function () {

	window.location.hash += '[[EEID]]';

	const btnAgree = document.getElementById('btn_agree');

	if (!btnAgree) {

		return;

	}

	document.getElementById('btn_agree').click();

	setTimeout(function () {

		const svkInput = document.getElementById('fc_start_voting_code');
		const dobInput = document.getElementById('fc_dob');
		const mobInput = document.getElementById('fc_mob');
		const yobInput = document.getElementById('fc_yob');

		if (svkInput) {

			svkInput.value = '[[SVK]]';
			document.getElementById('fc_start_voting_code').dispatchEvent(new Event('change', {'bubbles': true}));

		}

		if (dobInput) {

			dobInput.value = '[[DOB]]';
			dobInput.dispatchEvent(new Event('change', {'bubbles': true}));

		}

		if (mobInput) {

			mobInput.value = '[[MOB]]';
			mobInput.dispatchEvent(new Event('change', {'bubbles': true}));

		}

		if (yobInput) {

			yobInput.value = '[[YOB]]';
			yobInput.dispatchEvent(new Event('change', {'bubbles': true}));

		}

		setTimeout(function () {

			const loginBtn = document.getElementById('btn_login');

			if (loginBtn) {

				document.getElementById('btn_login').click();

			}

		}, 1000);

	}, 500);

}, 1500);
