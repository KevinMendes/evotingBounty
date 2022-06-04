/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.google.common.collect.ImmutableList;

@SpringBootApplication(scanBasePackages = { "ch.post.it.evoting" })
@EnableJpaRepositories("ch.post.it.evoting")
@EntityScan("ch.post.it.evoting")
public class MessageBrokerOrchestratorApplication {

	public static final ImmutableList<Integer> NODE_IDS = ImmutableList.of(1, 2, 3, 4);

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication.run(MessageBrokerOrchestratorApplication.class);
	}
}
