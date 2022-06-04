/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;

public class AuthenticationTokenResourceTest {

	@Test
	public void testPassValidation() throws JsonProcessingException {


		ValidationResult valid = new ValidationResult(true);
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		String json = objectMapper.writeValueAsString(valid);
		System.out.println(json);
		// {"result":true,"validationError":{"validationErrorType":"SUCCESS"}}
		String expectedValid = "{\"result\":true,\"validationError\":{\"validationErrorType\":\"SUCCESS\"}}";
		Assert.assertEquals(expectedValid.replace("\\", ""), json);

		ValidationResult invalid = new ValidationResult(false);
		ValidationError validationError = new ValidationError();
		validationError.setValidationErrorType(ValidationErrorType.INVALID_CERTIFICATE_CHAIN);
		invalid.setValidationError(validationError);
		json = objectMapper.writeValueAsString(invalid);
		System.out.println(json);
		// {"result":false,"validationError":{"validationErrorType":"INVALID_CERTIFICATE_CHAIN"}}
		String expectedInvalid = "{\"result\":false,\"validationError\":{\"validationErrorType\":\"INVALID_CERTIFICATE_CHAIN\"}}";
		Assert.assertEquals(expectedInvalid.replace("\\", ""), json);

	}
}
