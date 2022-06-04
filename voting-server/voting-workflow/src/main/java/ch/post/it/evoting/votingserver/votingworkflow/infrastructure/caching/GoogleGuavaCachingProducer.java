/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.infrastructure.caching;

import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Produces;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Produces a Guava Cache If we were using Spring we could use the Spring caching infrastructure and
 * have a more integrated solution
 */
public class GoogleGuavaCachingProducer {

	@Produces
	Cache<String, Object> createGuavaCache() {
		// return a cache for 100 items that expire after 60secs of "inactivity"
		return CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(60, TimeUnit.SECONDS).build();
	}

}
