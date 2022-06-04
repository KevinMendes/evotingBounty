/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

(function () {
    'use strict';

    angular
        .module('jobqueue', [])

        .factory('jobqueue', function (
            _,
            $http,
            $interval,
            $timeout,
            endpoints,
            $rootScope,
        ) {
			let pollInterval = null;
			const pollIntervalMs = 3000;

			const jobs = {};

			let batchCount = 0;
			let batchSummary = {};
			let batchSummaryErrorCallback = null;
			let batchSummarySuccessCallback = null;
			let batchesTotals = {};


			// set the start time stamp for a job

			const setStartTS = function (job) {
				if (job && job.status && !job.status.startTS) {
					job.status.startTS = new Date().getTime();
				}
			};

			// set the end time stamp for a job

			const setEndTS = function (job) {
				if (job && job.status && !job.status.endTS) {
					job.status.endTS = new Date().getTime();
				}
			};

			// check for complete batches

			const _batchSummary = function () {
				_.each(batchSummary, function (b, k, bs) {
					// Check completion state

					if (b.total <= b.ok + b.err) {
						// Check for error

						if (
							b.err &&
							batchSummaryErrorCallback &&
							typeof batchSummaryErrorCallback === 'function'
						) {
							batchSummaryErrorCallback(b.type, _.uniq(b.errors.sort()));
						}

						// report progress

						if (
							b.ok > 0 &&
							batchSummarySuccessCallback &&
							typeof batchSummarySuccessCallback === 'function'
						) {
							batchSummarySuccessCallback(b.type, b.ok);
						}

						// Batch completed

						delete bs[k];
					}
				});
			};

			// update progress info for one job

			const updateProgress = function (job, jobInfo) {
				// status

				switch (jobInfo.status) {
					case 'UNKNOWN':
						job.status.queued = false;
						job.status.processing = true;
						job.status.completed = false;
						job.status.error = false;
						break;
					case 'STARTING':
					case 'STARTED':
						job.status.queued = false;
						job.status.processing = true;
						job.status.completed = false;
						job.status.error = false;
						setStartTS(job);
						break;
					case 'COMPLETED':
						job.status.queued = false;
						job.status.processing = false;
						job.status.error = false;
						if (!job.status.completed) {
							if (batchSummary[job.batch]) {
								batchSummary[job.batch].ok++;
							}
							job.status.completed = true;
						}
						setEndTS(job);
						break;
					case 'FAILED':
						job.status.queued = false;
						job.status.processing = false;
						job.status.completed = true;
						if (!job.status.error) {
							if (batchSummary[job.batch]) {
								batchSummary[job.batch].err++;
								batchSummary[job.batch].errors.push(job.alias);
							}
							job.status.error = true;
						}
						setEndTS(job);
						break;
					default:
						console.log('unexpected job status: ' + jobInfo.status);
						break;
				}

				// completion

				try {
					const remaining = parseInt(jobInfo.progressDetails.remainingWork, 10);
					const total = parseInt(jobInfo.progressDetails.totalWorkAmount, 10);
					job.status.pct = Math.abs(((total - remaining) * 100) / total);
					if (job.status.pct > 100) {
						job.status.pct = 100;
					}
				} catch (e) {
					job.status.pct = 0;
				}

				// estimated time to completion

				try {
					job.status.eta = parseInt(
						jobInfo.progressDetails.estimatedTimeToCompletionInMillis,
						10,
					);
				} catch (ignore) {
					job.status.eta = 0;
				}

				// items && error count
				// every job returns these counts with different properties
				// we test what properties are present and infer the job type,
				// then populate our itemCount, itemCount2 and errorCount

				job.status.itemCount = 0;
				job.status.itemCount2 = 0;
				job.status.errorCount = 0;

				// voting card generation

				if (jobInfo.generatedCount !== undefined) {
					try {
						job.status.itemCount = parseInt(jobInfo.generatedCount, 10);
						job.status.errorCount = parseInt(jobInfo.errorCount, 10);
					} catch (ignore) {
					}
				}

				// decryption

				if (jobInfo.auditableVotesCount !== undefined) {
					try {
						job.status.itemCount = parseInt(jobInfo.auditableVotesCount, 10);
						job.status.itemCount2 = parseInt(jobInfo.decryptedVotesCount, 10);
						job.status.errorCount = parseInt(jobInfo.errorCount, 10);
					} catch (ignore) {
					}
				}

				// if the job just completed, call the callback if supplied

				if (job.status.completed) {
					if (job.callback && typeof job.callback === 'function') {
						try {
							job.callback(job.alias);
							job.callback = null;
						} catch (ignore) {
						}
					}
				}

				$rootScope.log(
					'j;' +
					job.alias +
					';' +
					job.status.eta +
					';' +
					job.status.pct +
					';' +
					jobInfo.status,
				);
			};

			// update progress info for a batch

			const updateBatchEstimate = function (batch) {
				let activeJobs = 0,
					activeJobsMS = 0,
					completedJobs = 0,
					completedJobsMS = 0,
					completedPctSum = 0,
					batchStartTS = batch.startTS,
					queuedJobs = 0,
					maxEta = 0;

				// collect job info

				_.filter(jobs, {
					batch: batch.batch,
				}).forEach(function (j) {
					if (j.status.endTS) {
						// job completed

						if (j.status.startTS && j.status.startTS < batchStartTS) {
							batchStartTS = j.status.startTS;
						}
						completedJobsMS += j.status.endTS - j.status.startTS;
						completedJobs++;
						completedPctSum += 100;
					} else if (j.status.eta > 0) {
						// job in progress

						if (j.status.startTS && j.status.startTS < batchStartTS) {
							batchStartTS = j.status.startTS;
						}
						activeJobsMS +=
							new Date().getTime() - j.status.startTS + j.status.eta;
						activeJobs++;
						if (j.status.eta > maxEta) {
							maxEta = j.status.eta;
						}
						completedPctSum += j.status.pct;
					} else {
						// queued

						queuedJobs++;
					}
				});

				// compute global progress

				batch.completedPct = batch.total ? completedPctSum / batch.total : 0;

				// estimate remaining time for the batch

				if (!queuedJobs && activeJobs === 1) {
					// if queue is empty and there is only one job, eta is the job's eta

					batch.remainingMS = maxEta;
				} else {
					// else, eta is a proportion of total elapsed time and total % complete up to now

					const totalElapsed = new Date().getTime() - batchStartTS;
					batch.remainingMS = Math.ceil(
						batch.completedPct
							? (totalElapsed * 100) / batch.completedPct - totalElapsed
							: 0,
					);
				}

				batch.totalItems = batch.total;
				batch.processedItems = batch.err + batch.ok;

				// batch log
				$rootScope.log(
					'b;' +
					batch.type +
					batch.batch +
					';' +
					batch.remainingMS +
					';' +
					batch.completedPct +
					';' +
					queuedJobs,
				);
			};

			// poll status for all active jobs in the queue

			const updateQueue = function () {
				const activeJobs = _.filter(jobs, function (j) {
					return !j.status.completed && !j.status.error;
				});

				// Check for active jobs

				if (!activeJobs.length) {
					// If there are no active jobs, cancel status polling

					$interval.cancel(pollInterval);
					pollInterval = null;
					batchSummary = {};
					batchesTotals = {};
				} else {
					// query status for each job type

					const activeTypes = _.groupBy(activeJobs, 'type');

					_.forOwn(activeTypes, function (value, type) {
						const endpoint =
							endpoints.host() + endpoints.progress_bulk.replace('{type}', type);
						$http.get(endpoint).then(
							function (jobInfoResponse) {
								try {
									// update status for each returned job

									jobInfoResponse.data.forEach(function (jobinfo) {
										const job = _.find(jobs, {
											jobid: jobinfo.jobId,
										});
										if (job) {
											updateProgress(job, jobinfo);
										}
									});
								} catch (e) {
									console.log('unparseable job status response');
								}

								// check error notifications for each 'batch' (group of jobs of same interaction)

								_batchSummary();

								// update consolidated progress of batches

								const bt = {};
								_.each(batchSummary, function (batch) {
									// estimate progress info for this batch
									updateBatchEstimate(batch);

									// add up totals for all batches
									if (!bt[batch.type]) {
										bt[batch.type] = {
											type: batch.type,
											total: 0,
											processed: 0,
										};
									}
									bt[batch.type].total += batch.totalItems;
									bt[batch.type].processed += batch.processedItems;
								});
								batchesTotals = bt;
							},
							function (errorResponse) {
								console.log('job status request error', errorResponse);
							},
						);
					});
				}
			};

			// add a new job to the queue

            const job = function (objid, jobid, type, alias, callback) {
                // add to jobqueue (will replace any existing job for this same obj)

                jobs[objid] = {
                    objid: objid,
                    jobid: jobid,
                    type: type,
                    alias: alias,
                    callback: callback,
                    batch: batchCount,
                    status: {
                        queued: true,
                        processing: false,
                        completed: false,
                        error: false,
                        pct: 0,
                        eta: 0,
                        itmeCount: 0,
                        itmeCount2: 0,
                        errorCount: 0,
                        startTS: 0,
                        endTS: 0,
                    },
                };

                // keep track of 'batches' of jobs so we can give error feedback per batch

                if (!batchSummary[batchCount]) {
                    batchSummary[batchCount] = {
                        batch: batchCount,
                        type: type,
                        total: 1,
                        ok: 0,
                        err: 0,
                        errors: [],
                        startTS: new Date().getTime(),
                        remainingMS: 0,
                        completedPct: 0,
                    };
                } else {
                    batchSummary[batchCount].total++;
                }

                // start polling status

                if (!pollInterval) {
                    pollInterval = $interval(updateQueue, pollIntervalMs);
                }
            };

            // signal a new batch number

            // note both callbacks are 'static', i.e.: every invocation of 'batch'
            // will override the callbacks.

            const batch = function (summarySuccessCallback, summaryErrorCallback) {
                batchCount++;
                batchSummaryErrorCallback = summaryErrorCallback;
                batchSummarySuccessCallback = summarySuccessCallback;
            };

            // get progress info for a job (by object id)

			const getJobStatus = function (objid) {
				const job = jobs[objid];
				return job && job.status ? job.status : undefined;
			};

			// get batches
            // @param types String|Array of String|nothing
            // @return array of batches (possibly empty)

			const getBatches = function (types) {
				if (types && typeof types === 'string') {
					types = [types];
				}
				const batches = _.filter(batchSummary, function (b) {
					return types
						? _.findIndex(types, function (t) {
						return t == b.type;
					}) >= 0
						: true;
				});
				return batches || [];
			};

			// get grand totals of batches
            // @param types String|Array of String|nothing
            // @return array of batchesTotals (possibly empty)

			const getBatchesTotals = function (types) {
				if (types && typeof types === 'string') {
					types = [types];
				}
				const bt = _.filter(batchesTotals, function (b) {
					return types
						? _.findIndex(types, function (t) {
						return t == b.type;
					}) >= 0
						: true;
				});
				return bt || [];
			};


			// export

            return {
                job: job,
                batch: batch,
                getJobStatus: getJobStatus,
                getBatches: getBatches,
                getBatchesTotals: getBatchesTotals,
            };
        });
})();
