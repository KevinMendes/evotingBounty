/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Base64;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.votingserver.commons.beans.TestBean;
import ch.post.it.evoting.votingserver.commons.beans.TestBeanOneField;
import ch.post.it.evoting.votingserver.commons.beans.TestConstructorBean;
import ch.post.it.evoting.votingserver.commons.beans.TestNestedBean;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

public class JSONSignAndVerifyITest {

	public static final String FOO_BAR = "fooBar";

	public static final int TWO = 2;

	public static final String DOT = ".";

	public static final int THREE = 3;

	private static TestBean bean;

	private static TestBeanOneField beanWithOneField;

	private static TestNestedBean nestedBean;

	private static TestConstructorBean constructorBean;

	private static KeyPair keyPairForSigning;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@BeforeClass
	public static void setUp() {

		bean = new TestBean();
		bean.setFieldOne(FOO_BAR);
		bean.setFieldTwo(TWO);

		beanWithOneField = new TestBeanOneField();
		beanWithOneField.setFieldOne(FOO_BAR);

		constructorBean = new TestConstructorBean(FOO_BAR, TWO);

		nestedBean = new TestNestedBean();
		nestedBean.setBean(bean);
		nestedBean.setIndex(THREE);

		AsymmetricService asymmetricService = new AsymmetricService();
		keyPairForSigning = asymmetricService.getKeyPairForSigning();
	}

	@Test
	public void test_that_signs_and_verifies() {

		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), bean);

		assertThat(Jwts.parser().isSigned(signedJSON), is(true));

		TestBean recoveredBean = JsonSignatureService.verify(keyPairForSigning.getPublic(), signedJSON, TestBean.class);

		assertThat(recoveredBean.getFieldOne().equals(FOO_BAR), is(true));
		assertThat(recoveredBean.getFieldTwo() == TWO, is(true));
	}

	@Test
	public void test_that_signs_and_verifies_bean_with_nested_fields() {

		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), nestedBean);

		assertThat(Jwts.parser().isSigned(signedJSON), is(true));

		TestNestedBean recoveredBean = JsonSignatureService.verify(keyPairForSigning.getPublic(), signedJSON, TestNestedBean.class);

		assertThat(recoveredBean.getBean() != null, is(true));
		assertThat(recoveredBean.getIndex() == THREE, is(true));
		assertThat(recoveredBean.getBean().getFieldOne().equals(FOO_BAR), is(true));
		assertThat(recoveredBean.getBean().getFieldTwo() == TWO, is(true));
	}

	@Test
	public void test_that_signs_and_verifies_a_bean_with_constructor_instead_of_setters() {

		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), constructorBean);

		assertThat(Jwts.parser().isSigned(signedJSON), is(true));

		TestBean recoveredBean = JsonSignatureService.verify(keyPairForSigning.getPublic(), signedJSON, TestBean.class);

		assertThat(recoveredBean.getFieldOne().equals(FOO_BAR), is(true));
		assertThat(recoveredBean.getFieldTwo() == TWO, is(true));
	}

	@Test
	public void throw_exception_when_signs_completed_bean_and_verifies_bean_with_less_number_of_attributes() {

		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), bean);

		expectedException.expect(IllegalArgumentException.class);
		JsonSignatureService.verify(keyPairForSigning.getPublic(), signedJSON, TestBeanOneField.class);
	}

	@Test
	public void test_that_signs_bean_with_one_field_and_verifies_bean_with_two_fields() {
		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), beanWithOneField);

		assertThat(Jwts.parser().isSigned(signedJSON), is(true));

		TestBean recoveredBean = JsonSignatureService.verify(keyPairForSigning.getPublic(), signedJSON, TestBean.class);

		assertThat(recoveredBean.getFieldOne().equals(FOO_BAR), is(true));
		assertThat(recoveredBean.getFieldTwo() == null, is(true));
	}

	@Test
	public void test_that_signs_and_signed_json_without_dots_throws_parsing_exception() {
		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), bean);

		String[] fields = signedJSON.split("\\.");
		String modifiedSignedJSON = fields[0] + fields[1] + fields[2];

		expectedException.expect(MalformedJwtException.class);
		JsonSignatureService.verify(keyPairForSigning.getPublic(), modifiedSignedJSON, TestBean.class);
	}

	@Test
	public void test_that_signs_and_modified_header_throws_verification_exception() {
		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), bean);

		String[] fields = signedJSON.split("\\.");
		String modifiedHeader = fields[0] + "A=";

		String modifiedSignedJSON = modifiedHeader + DOT + fields[1] + DOT + fields[2];

		expectedException.expect(SignatureException.class);
		JsonSignatureService.verify(keyPairForSigning.getPublic(), modifiedSignedJSON, TestBean.class);
	}

	@Test
	public void test_that_signs_and_modified_payload_throws_verification_exception() {
		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), bean);

		String[] fields = signedJSON.split("\\.");
		String modifiedPayload = fields[1] + "A=";

		String modifiedSignedJSON = fields[0] + DOT + modifiedPayload + DOT + fields[2];

		expectedException.expect(SignatureException.class);
		JsonSignatureService.verify(keyPairForSigning.getPublic(), modifiedSignedJSON, TestBean.class);
	}

	@Test
	public void test_that_signs_and_if_corrupted_bean_is_introduced_as_payload_throws_verification_exception() {
		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), bean);

		String[] fields = signedJSON.split("\\.");
		String modifiedPayload = Base64.getEncoder()
				.encodeToString(decodeBase64(fields[1]).replace(FOO_BAR, "barBarFOO").getBytes(StandardCharsets.UTF_8));

		String modifiedSignedJSON = fields[0] + DOT + modifiedPayload + DOT + fields[2];

		expectedException.expect(SignatureException.class);
		JsonSignatureService.verify(keyPairForSigning.getPublic(), modifiedSignedJSON, TestBean.class);
	}

	@Test
	public void test_that_signs_and_modified_signature_throws_verification_exception() {
		String signedJSON = JsonSignatureService.sign(keyPairForSigning.getPrivate(), bean);

		String[] fields = signedJSON.split("\\.");
		String modifiedSignature = fields[2] + "A=";

		String modifiedSignedJSON = fields[0] + DOT + fields[1] + DOT + modifiedSignature;

		expectedException.expect(SignatureException.class);
		JsonSignatureService.verify(keyPairForSigning.getPublic(), modifiedSignedJSON, TestBean.class);
	}

	private String decodeBase64(final String field) {
		return new String(Base64.getDecoder().decode(field), StandardCharsets.UTF_8);
	}
}
