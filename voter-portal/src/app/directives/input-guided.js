/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function () {
	'ngInject';

	return {
		restrict: 'A',
		replace: true,
		require: 'ngModel',
		link: function (scope, element, attr, ngModelCtrl) {
			if (!ngModelCtrl) {
				return;
			}

			let oldViewModel = '';
			const format = parseInt(attr.format);
			let selectionStartBefore = 0;
			let selectionEndBefore = 0;
			let selectionStartAfter = 0;
			let selectionEndAfter = 0;

			/**
			 * Executed whenever the bound ngModel expression changes programmatically.
			 * Used to format/convert the $modelValue for display in the control.
			 */
			ngModelCtrl.$formatters.push(function (modelValue) {
				return getGuidMask(modelValue);
			});

			/**
			 * Called whenever the control updates the ngModelController
			 * with a new $viewValue from the DOM, usually via user input
			 */
			ngModelCtrl.$parsers.push(function (viewValue) {
				updateGuid(viewValue);

				ngModelCtrl.$setValidity('minlength', false);
				ngModelCtrl.$setValidity('maxlength', false);

				const model = getGuid(viewValue);

				if (model && model.trim()) {
					if (model.trim().length <= parseInt(attr.modelMaxlength)) {
						ngModelCtrl.$setValidity('maxlength', true);
					}

					if (model.trim().length >= parseInt(attr.modelMinlength)) {
						ngModelCtrl.$setValidity('minlength', true);
					}
				}

				return model;
			});

			/**
			 * Updates the view value with the formatted value
			 *
			 * @param {any} viewValue
			 */
			function updateGuid(viewValue) {
				const currentViewModel = getGuidMask(getGuid(viewValue));

				// Used to retain cursor position when the view value is modified
				let selectionStart = element[0].selectionStart;
				let selectionEnd = element[0].selectionEnd;
				const selectionDirection = element[0].selectionDirection;

				ngModelCtrl.$setViewValue(currentViewModel);
				ngModelCtrl.$render();

				if (currentViewModel === oldViewModel) {
					return;
				}

				// Don't modify cursor's position if is on position 0
				if (selectionStart !== 0) {
					// If view value is modified by deletion, ensure
					// cursor is being placed before blank spaces
					if (oldViewModel.length > currentViewModel.length) {
						if (selectionStart % (format + 1) === 0) {
							selectionStart--;
							selectionEnd--;
						}

						// On modifying the view value by adding new chars,
						// cursor has to skip the blank spaces
					} else {
						if (selectionStart % (format + 1) === 0) {
							selectionStart++;
							selectionEnd++;
						}
					}
				}

				setSelectionRange(
					element[0],
					selectionStart,
					selectionEnd,
					selectionDirection,
				);
				oldViewModel = currentViewModel;
			}

			function isCursorModifierKey(keyCode) {
				// arrows: left 37, up 38,right 39, down 40
				// delete (46) and backspace (8) should not be treated here
				return [37, 38, 39, 40].indexOf(keyCode) > -1;
			}

			function setSelectionRange(
				inputElement,
				selectionStart,
				selectionEnd,
				selectionDirection,
			) {
				setTimeout(function () {
					inputElement.setSelectionRange(
						selectionStart,
						selectionEnd,
						selectionDirection,
					);
				}, 0);
			}

			/**
			 * Function to modify the cursor position using navigation keys to avoid
			 * placing the cursor in a position where editing the value will affect the
			 * blank spaces instead of actual model value.
			 * E.g. Given the view value: "123 |456", having the cursor after the blank space,
			 * when hitting backspace this will delete the space which is not part of the model
			 * value (123456) resulting in no change of the model value.
			 *
			 * @param {Boolean}                shiftKeyPressed Shift key is pressed
			 * @param {['forward'|'backward']} direction For simple cursor navigation (arrow keys)
			 *                             the direction represents which way the cursor is moving
			 *                             when a key is pressed. In case of selection (shift key
			 *                             + arrow keys) the direction represents the selection direction.
			 *                             E.g. shift + left key = ltr shift + right key = rtl
			 *                             shift + left + left + right = ltr (selection is still left
			 *                             to right; last right key did was deselecting a char)
			 */
			function handlerCursorPosition(shiftKeyPressed, direction) {
				if (shiftKeyPressed) {
					// If selection direction is "forward" (left to right)
					if (
						selectionEndAfter % (format + 1) === 0 &&
						String(direction) === 'forward'
					) {
						// If selection's cursor moves to the right (selects more)
						if (selectionEndBefore < selectionEndAfter) {
							// Jump over blank space
							setSelectionRange(
								element[0],
								selectionStartAfter,
								selectionEndAfter + 1,
							);
							// If selection's cursor moves to the left (deselect)
						} else {
							// Jump over blank space
							setSelectionRange(
								element[0],
								selectionStartAfter,
								selectionEndAfter - 1,
							);
						}
					}

					// If selection direction is "backward" (right to left)
					if (
						selectionStartAfter % (format + 1) === format &&
						String(direction) === 'backward'
					) {
						// If selection's cursor moves to the left (selects more)
						if (selectionStartBefore < selectionStartAfter) {
							// Jump over blank space
							setSelectionRange(
								element[0],
								selectionStartAfter + 1,
								selectionEndAfter,
								'backward',
							);
							// If selection's cursor moves to the right (deselect)
						} else {
							// Jump over blank space
							setSelectionRange(
								element[0],
								selectionStartAfter - 1,
								selectionEndAfter,
								'backward',
							);
						}
					}
				} else {
					// If cursor ends up at the beginning, do nothing
					if (selectionEndAfter === 0) {
						return;
					}

					// If cursor is coming from left to right
					if (
						selectionEndAfter % (format + 1) === 0 &&
						String(direction) === 'forward'
					) {
						// Jump over blank space
						setSelectionRange(
							element[0],
							selectionEndAfter + 1,
							selectionEndAfter + 1,
						);
					}

					// If cursor is coming from right to left
					if (
						selectionEndAfter % (format + 1) === 0 &&
						String(direction) === 'backward'
					) {
						// Jump over blank space
						setSelectionRange(
							element[0],
							selectionEndAfter - 1,
							selectionEndAfter - 1,
						);
					}
				}
			}

			element[0].addEventListener('keydown', function (event) {
				if (isCursorModifierKey(event.keyCode)) {
					selectionStartBefore = element[0].selectionStart;
					selectionEndBefore = element[0].selectionEnd;
				}
			});

			element[0].addEventListener('keyup', function (event) {
				if (isCursorModifierKey(event.keyCode)) {
					selectionStartAfter = element[0].selectionStart;
					selectionEndAfter = element[0].selectionEnd;

					let direction;

					if (event.shiftKey) {
						direction =
							selectionStartBefore === selectionStartAfter
								? 'forward'
								: 'backward';
					} else {
						direction =
							selectionEndBefore < selectionEndAfter ? 'forward' : 'backward';
					}

					handlerCursorPosition(event.shiftKey, direction);
				}
			});

			/**
			 * Strips all spaces from the given string
			 *
			 * @param {any} text The string to be formatted
			 * @returns For a string like '1234 56' returns '123456'
			 */
			function getGuid(text) {
				return (text || '')
					.split(' ')
					.join('')
					.slice(0, parseInt(attr.modelMaxlength));
			}

			/**
			 * Formats string based on the configured format
			 *
			 * @param {String} guid A string representing a guid
			 * @returns Given format = 4 and guid = '123456', returns '1234 56'
			 */
			function getGuidMask(guid) {
				const regexp = new RegExp(`.{1,${attr.format}}`, 'g');
				const guidChunk = guid.match(regexp);

				return (guidChunk || []).join(' ');
			}
		},
	};
};
