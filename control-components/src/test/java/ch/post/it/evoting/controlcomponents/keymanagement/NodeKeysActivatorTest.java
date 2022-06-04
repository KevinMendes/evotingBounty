/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.KeyManagementException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;

import ch.post.it.evoting.controlcomponents.keymanagement.NodeKeyTestService.IntermediateCAKeystore;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.NodeKeysEntity;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.NodeKeysEntityRepository;

@EnableRetry
@SpringBootTest
@ActiveProfiles("test")
class NodeKeysActivatorTest {


	@Autowired
	NodeKeysActivator nodeKeysActivator;

	@Autowired
	private NodeKeysActivator activator;


	@Autowired
	NodeKeysEntityRepository nodeKeysEntityRepository;

	@BeforeEach
	public void setUp() throws IOException {

	}

	@RepeatedTest(5)
	void testConcurrentThreadNodeKeyCreation() throws IOException, InterruptedException {

		int NO_OF_THREADS = 5;

		//Note the contents of each intermediate keystore are the same however each thread must have its own copy
		List<IntermediateCAKeystore> intermediateCAkeystores = NodeKeyTestService.copyIntermediateCAKeystore(NO_OF_THREADS);

		List<Integer> nodeKeyHashes = new ArrayList<>();
		CountDownLatch countDownLatch = new CountDownLatch(NO_OF_THREADS);
		ExecutorService executorService = Executors.newFixedThreadPool(NO_OF_THREADS);

		for (IntermediateCAKeystore key : intermediateCAkeystores) {
			executorService.execute(() -> {
				try {
					activator.activateNodeKeys(key.getKeyStoreFile(), NodeKeyTestService.NODE_ALIAS, key.getPasswordFile());

					//This find is prone to race conditions but given enough repetitions these race conditions will be exposed.
					Optional<NodeKeysEntity> nodeKeysEntity = nodeKeysEntityRepository.findById("1");
					assertTrue(nodeKeysEntity.isPresent());
					byte[] nodeKey = nodeKeysEntity.get().getKeys();
					nodeKeyHashes.add(Arrays.hashCode(nodeKey));

					countDownLatch.countDown();
				} catch (KeyManagementException | IOException | InterruptedException | TimeoutException e) {
					throw new RuntimeException(e);
				}
			});
		}

		countDownLatch.await();
		long count = nodeKeyHashes.stream().distinct().count();
		assertEquals(1, count);
	}
}