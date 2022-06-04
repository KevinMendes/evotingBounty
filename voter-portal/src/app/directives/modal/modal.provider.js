/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function () {
	const $modalProvider = {
		options: {
			backdrop: true, // can be also false or 'static'
			keyboard: true,
		},
		$get: [
			'$injector',
			'$rootScope',
			'$q',
			'$http',
			'$templateCache',
			'$controller',
			'$modalStack',
			function (
				$injector,
				$rootScope,
				$q,
				$http,
				$templateCache,
				$controller,
				$modalStack,
			) {
				const $modal = {};

				function getTemplatePromise(options) {
					return options.template
						? $q.when(options.template)
						: $http
							.get(
								angular.isFunction(options.templateUrl)
									? options.templateUrl()
									: options.templateUrl,
								{
									cache: $templateCache,
								},
							)
							.then(function (result) {
								return result.data;
							});
				}

				function getResolvePromises(resolves) {
					const promisesArr = [];

					angular.forEach(resolves, function (value) {
						if (angular.isFunction(value) || angular.isArray(value)) {
							promisesArr.push($q.when($injector.invoke(value)));
						}
					});

					return promisesArr;
				}

				$modal.open = function (modalOptions) {
					const modalResultDeferred = $q.defer();
					const modalOpenedDeferred = $q.defer();

					// prepare an instance of a modal to be injected into controllers and returned to a caller
					const modalInstance = {
						result: modalResultDeferred.promise,
						opened: modalOpenedDeferred.promise,
						close: function (result) {
							$modalStack.close(modalInstance, result);
						},
						dismiss: function (reason) {
							$modalStack.dismiss(modalInstance, reason);
						},
					};

					// merge and clean up options
					modalOptions = angular.extend(
						{},
						$modalProvider.options,
						modalOptions,
					);
					modalOptions.resolve = modalOptions.resolve || {};

					// verify options
					if (!modalOptions.template && !modalOptions.templateUrl) {
						throw new Error(
							'One of template or templateUrl options is required.',
						);
					}

					const templateAndResolvePromise = $q.all(
						[getTemplatePromise(modalOptions)].concat(
							getResolvePromises(modalOptions.resolve),
						),
					);

					templateAndResolvePromise.then(
						function resolveSuccess(tplAndVars) {
							const modalScope = (modalOptions.scope || $rootScope).$new();

							modalScope.$close = modalInstance.close;
							modalScope.$dismiss = modalInstance.dismiss;

							let ctrlInstance;
							const ctrlLocals = {};
							let resolveIter = 1;

							// controllers
							if (modalOptions.controller) {
								ctrlLocals.$scope = modalScope;
								ctrlLocals.$modalInstance = modalInstance;
								angular.forEach(modalOptions.resolve, function (value, key) {
									ctrlLocals[key] = tplAndVars[resolveIter++];
								});

								ctrlInstance = $controller(modalOptions.controller, ctrlLocals);
								if (modalOptions.controllerAs) {
									modalScope[modalOptions.controllerAs] = ctrlInstance;
								}
							}

							$modalStack.open(modalInstance, {
								scope: modalScope,
								deferred: modalResultDeferred,
								content: tplAndVars[0],
								backdrop: modalOptions.backdrop,
								keyboard: modalOptions.keyboard,
								backdropClass: modalOptions.backdropClass,
								windowClass: modalOptions.windowClass,
								windowTemplateUrl: modalOptions.windowTemplateUrl,
								size: modalOptions.size,
							});
						},
						function resolveError(reason) {
							modalResultDeferred.reject(reason);
						},
					);

					templateAndResolvePromise.then(
						function () {
							modalOpenedDeferred.resolve(true);
						},
						function () {
							modalOpenedDeferred.reject(false);
						},
					);

					return modalInstance;
				};

				return $modal;
			},
		],
	};

	return $modalProvider;
};
