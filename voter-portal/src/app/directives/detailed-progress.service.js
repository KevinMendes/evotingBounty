/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = $timeout => {
	'ngInject';

	const model = {
		steps: [],
	};

	const init = steps => {
		model.steps = steps.map(s => {
			if (!s || !s.running || !s.success) {
				throw new Error(
					'A step has to be provided with translations for running and success phases',
				);
			}

			return Object.assign(
				{},
				{
					i18n: s,
				},
				{
					running: false,
					success: false,
				},
			);
		});
	};

	const _notifySlowProgress = () => {
		if (typeof model.onSlowProgressNotified === 'function') {
			model.onSlowProgressNotified();
		}
	};

	const stopProgress = () => {
		model.currentStepInProgress.running = false;
		model.currentStepInProgress.success = true;
		model.currentStepInProgress.text = model.currentStepInProgress.i18n.success;
		$timeout(() => {
			model.currentStepInProgress = null;
		}, 100);
	};

	const _setStepDone = () => {
		model.currentStepInProgress.running = false;
		model.currentStepInProgress.success = true;
		model.currentStepInProgress.text = model.currentStepInProgress.i18n.success;

		if (model.currentStepInProgress.index + 1 === model.steps.length) {
			stopProgress();
		}
	};

	const _setStepInProgress = stepIndex => {
		if (model.currentStepInProgress) {
			_setStepDone();
		}

		model.currentStepInProgress = model.steps[stepIndex];
		model.currentStepInProgress.index = stepIndex;
		model.currentStepInProgress.text = model.currentStepInProgress.i18n.running;
		model.currentStepInProgress.running = true;
	};

	const startProgressOnNextStep = () => {
		if (!model.currentStepInProgress) {
			$timeout(() => {
				_notifySlowProgress();
			}, 5000);
		}

		_setStepInProgress(model.currentStepInProgress ? model.currentStepInProgress.index + 1 : 0);
	};

	return {
		model,
		init,
		startProgressOnNextStep,
		stopProgress,
	};
};
