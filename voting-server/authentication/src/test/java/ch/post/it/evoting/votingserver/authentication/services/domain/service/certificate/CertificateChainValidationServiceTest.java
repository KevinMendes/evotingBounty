/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.certificate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateChainValidator;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCerts;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCertsRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.utils.AuthenticationUtils;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;

@RunWith(MockitoJUnitRunner.class)
public class CertificateChainValidationServiceTest {

	public static final X509Certificate[] EMPTY_ARRAY_CERTIFICATES = {};

	public static final X509DistinguishedName[] EMPTY_ARRAY_DISTINGUISHED_NAMES = {};
	private final String json = "{\r\n  \"electionEventId\" : \"314bd34dcf6e4de4b771a92fa3849d3d\",\r\n  \"authenticationTokenSignerCert\" : \"-----BEGIN CERTIFICATE-----\\r\\nMIIDqzCCApOgAwIBAgIUTeNKibchIPT8H+hIeeCvFnPXam8wDQYJKoZIhvcNAQEL\\r\\nBQAwezE1MDMGA1UEAwwsU2VydmljZXMgQ0EgMzE0YmQzNGRjZjZlNGRlNGI3NzFh\\r\\nOTJmYTM4NDlkM2QxFjAUBgNVBAsMDU9ubGluZSBWb3RpbmcxEjAQBgNVBAoMCVN3\\r\\naXNzUG9zdDEJMAcGA1UEBwwAMQswCQYDVQQGEwJDSDAeFw0xNTA2MjYwOTI1NTda\\r\\nFw0xNjA2MDExMDE1MzBaMIGBMTswOQYDVQQDDDJBdXRoIFRva2VuIFNpZ25lciAz\\r\\nMTRiZDM0ZGNmNmU0ZGU0Yjc3MWE5MmZhMzg0OWQzZDEWMBQGA1UECwwNT25saW5l\\r\\nIFZvdGluZzESMBAGA1UECgwJU3dpc3NQb3N0MQkwBwYDVQQHDAAxCzAJBgNVBAYT\\r\\nAkNIMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmJskYCliK68dWkNQ\\r\\ndyhD7NYqQ/k1jL4TfY9jq+zut36r8SL6b2cs4hXXbrhv70+cR8xOmdGXCduTy9RH\\r\\nbF+SjOBVMVIelluK+F8cjziNuQZbewkwDcEbEo65pvWq3rf/EBNo9eMs2qtvBQGl\\r\\n0KSChKpvVqUY4yT4XEJuhmGQ9vidY5C7rnn2mDFlUDqs9pC5hZwQBXPT1m2IZfFG\\r\\ncpi0iA/OC6rj2Y87+b/OwwEckxToYbQV0iyByusqOLA3AJwdacvQyCVCHEgamXgR\\r\\nCP+KJAy5myzTKViewQyQeqzcVbLEbDSoQB6QRTC7bAaQpZu0bEpeGstvhvDYqR6Q\\r\\nSYuDzwIDAQABoyAwHjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIGwDANBgkq\\r\\nhkiG9w0BAQsFAAOCAQEAaEpLkDkj534FEXUNIAZ3pX/EhTGlmJvwCUZZAXQ2kG7o\\r\\noAcWw3xWAemM1fQVVQedZrt7eQpnrc04Ivc+478pUBzAMeMwbRPIFJzDdzDwEb1i\\r\\n1Tm8FbL3ozVwKnWACbuOYgoaMtIGr9kZg5K/KUb5D9g9qG1uvdGZzkemcTACcpIu\\r\\nHe4s4kEzWnZ5yW/xppJELo6euIi96ybKT2iNfycGbq/idR/pxLu1nB+x1qBTkJmf\\r\\nOKu7MShOwtzcQwK7J8JZKPPBAfUJRDl6nvMduXCKQMZeiqASX5B31DIb3+2BYIGY\\r\\nsczIDfxo+eJDCPbEfR+8GMkb57qQ/rPJJiNMctzg2g==\\r\\n-----END CERTIFICATE-----\\r\\n\",\r\n  \"electionRootCA\" : \"-----BEGIN CERTIFICATE-----\\r\\nMIIDtTCCAp2gAwIBAgIUQ1mHMD98WCqbPDcdZBcYhzIGJtMwDQYJKoZIhvcNAQEL\\r\\nBQAwgYExOzA5BgNVBAMMMkVsZWN0aW9uIEV2ZW50IENBIDMxNGJkMzRkY2Y2ZTRk\\r\\nZTRiNzcxYTkyZmEzODQ5ZDNkMRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRIwEAYD\\r\\nVQQKDAlTd2lzc1Bvc3QxCTAHBgNVBAcMADELMAkGA1UEBhMCQ0gwHhcNMTUwNjI2\\r\\nMDkyNTU3WhcNMTYwNjAxMTAxNTMwWjCBgTE7MDkGA1UEAwwyRWxlY3Rpb24gRXZl\\r\\nbnQgQ0EgMzE0YmQzNGRjZjZlNGRlNGI3NzFhOTJmYTM4NDlkM2QxFjAUBgNVBAsM\\r\\nDU9ubGluZSBWb3RpbmcxEjAQBgNVBAoMCVN3aXNzUG9zdDEJMAcGA1UEBwwAMQsw\\r\\nCQYDVQQGEwJDSDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJkDpELP\\r\\n+DUf4LDSzaPf1d70aeXu5mhhiThzU2k+kHkVru2gIwVh35jR/CKkxMTVBcGXpor6\\r\\n639BU3Hv4501/ryMb9FjB8siGaZBXZVdp7dY2sJnqx03eRtwMvAK9TUVgrTQRtEe\\r\\nnX618nz/L9arTXv3ZYn8ELV9CLJfuctgkZ8/lVfdqLZSCFx6mlyLJ9CYcVztsoMg\\r\\nKV87+W2B4I9PieDxhbQWOkmd+WCOG9hIAQICj3enMFcC4gXMJ8Rds/aB040jBd+F\\r\\nLtTbfBkd9cW+oAu9OGG3eE4tOkkWWSUiT9YtW6msoyUawHjtv5gps0T/MvlvhDd0\\r\\nSWkZsOaSFAryVGMCAwEAAaMjMCEwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8E\\r\\nBAMCAQYwDQYJKoZIhvcNAQELBQADggEBABFqSmjnspogfl9GoSe4gexZMdOqmKxA\\r\\npY4yTEi//U/B39z9mc/nqxD5+Fwxh44wk6H2MgXNTSZQajui5RuvToVx0a8lg0j0\\r\\nEVZ7GQjZIe21PFi6vfNSGYfrVqC6zyjdOc+ZofhvF4yp0V8aQu71/VT8tpG3bFTa\\r\\nrH+gpKlaUhwOKgZbhr15aMVomEOWxJpR7PMEcvzju8dKE/QxNnY/BzAOQTTt2HFU\\r\\nRWbSYrCbFpCcBcMSNGE0yqVwdzem5qe0++RecAgT1yvRe6ZKzJNSCwYZdyhE73K8\\r\\nKuHjQuYm37YZ0JHiu7GId8Xb4Pr1xrXc9shqm58ZAgWIrhOE3A0xv7s=\\r\\n-----END CERTIFICATE-----\\r\\n\",\r\n  \"authoritiesCA\" : \"-----BEGIN CERTIFICATE-----\\r\\nMIIDsjCCApqgAwIBAgIVAOtpDPC6nnHzs7dfSn7sqbvhE/1BMA0GCSqGSIb3DQEB\\r\\nCwUAMIGBMTswOQYDVQQDDDJFbGVjdGlvbiBFdmVudCBDQSAzMTRiZDM0ZGNmNmU0\\r\\nZGU0Yjc3MWE5MmZhMzg0OWQzZDEWMBQGA1UECwwNT25saW5lIFZvdGluZzESMBAG\\r\\nA1UECgwJU3dpc3NQb3N0MQkwBwYDVQQHDAAxCzAJBgNVBAYTAkNIMB4XDTE1MDYy\\r\\nNjA5MjU1N1oXDTE2MDYwMTEwMTUzMFowfjE4MDYGA1UEAwwvQXV0aG9yaXRpZXMg\\r\\nQ0EgMzE0YmQzNGRjZjZlNGRlNGI3NzFhOTJmYTM4NDlkM2QxFjAUBgNVBAsMDU9u\\r\\nbGluZSBWb3RpbmcxEjAQBgNVBAoMCVN3aXNzUG9zdDEJMAcGA1UEBwwAMQswCQYD\\r\\nVQQGEwJDSDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJS+T7bjec/E\\r\\nX9WnQpyxhw6ooYoEhxmP+m02hoQ9NgiXEhZydpPpelykSJg+RCad75MgaLE0kTJ0\\r\\nAirT7XZR6hZh4j3U4cqnTblerQO3hY65dWhTCBWlhotTBdJ7Qz39sfwYRGnH2pC3\\r\\njgmwumbweZLxrj3pwbB68LzV5eFKMnBfQZEkrt6Al+PFbgn0EVdOe1koTo6RiqyD\\r\\nXfZtZD8QqwIdGttQHLyjBFnkckWkMtBu+BJ3ywujssn7EQMhHEtJ+bhS0jvdqlcY\\r\\n78JxOZbaDbC/qiw2St1+cpCUSXjjicpYimDnupQdbcWtv0PL70k/e/1xxT7gypGM\\r\\nAXWc16dX3mcCAwEAAaMjMCEwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMC\\r\\nAQYwDQYJKoZIhvcNAQELBQADggEBAHZN4bPztk9XB4eWSVPyMDuVJB2F2J3Q/5AG\\r\\nw0HK8myRb+BtBX5Nws/McKJFAolBsTzitos2V7BLFfeuzMXWs1RnUSuzUiraUp5u\\r\\nb/Rl8dLsBvfZXN6OcrSpD/M5XaBLyv32QW9mbKpfRcaGNWA7l4TFwO9KLzU9TAlJ\\r\\noQi/Sz7m0eR7XwooQyRwbnv22ekBHy9t254nggRsIhaYZcJHvnDbz7JxRgnWD8Ci\\r\\n2KqNGCuceeuETsdZDV8VIyqbkkscxZZltiTVGb8TbnLC9Au4kn0H23Z9ubyf9Uxw\\r\\nZ8GYLJKEDSCI04BM4RpB5w7i/iKh8OVokdHMuiaGozK3A5zzckM=\\r\\n-----END CERTIFICATE-----\\r\\n\",\r\n  \"adminBoard\" : \"-----BEGIN CERTIFICATE-----\\r\\nMIIDnjCCAoagAwIBAgIUDiTSvVm7fCturt5eLp5KAzbVbYkwDQYJKoZIhvcNAQEL\\r\\nBQAwfjE4MDYGA1UEAwwvQXV0aG9yaXRpZXMgQ0EgMzE0YmQzNGRjZjZlNGRlNGI3\\r\\nNzFhOTJmYTM4NDlkM2QxFjAUBgNVBAsMDU9ubGluZSBWb3RpbmcxEjAQBgNVBAoM\\r\\nCVN3aXNzUG9zdDEJMAcGA1UEBwwAMQswCQYDVQQGEwJDSDAeFw0xNTA2MjYwOTI1\\r\\nNTdaFw0xNjA2MDExMDE1MzBaMHIxLDAqBgNVBAMMI0FCIDMxNGJkMzRkY2Y2ZTRk\\r\\nZTRiNzcxYTkyZmEzODQ5ZDNkMRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRIwEAYD\\r\\nVQQKDAlTd2lzc1Bvc3QxCTAHBgNVBAcMADELMAkGA1UEBhMCQ0gwggEiMA0GCSqG\\r\\nSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCvSnDrG+n72v2OhhuuuuUISy4E5ShhhdRF\\r\\ndNrJbHbJUg4eRVu/r0gTUnRvvH0UEOzG2crzFBly7AfY8LuArpk8+Nke9oSv4Zk1\\r\\nOx3q9ChsHlVJ1HDFIcQDQUy3f16JwXdjr3K71JsbYhbkUzANDrWZWc5a2j70QDZw\\r\\nvsAqtGYtAOjUBtIqt+Y3JoM/KdP9TjHq1JGRsoLa31hN5jkjfp+EAPl/lkeche1p\\r\\njxdD49Y+I8Sqt5KGlEr24Wp/MicZRHSSmsdV1ADNIpKKy13Gp75J5nb4aHK0Dl3f\\r\\nZVC9zToC7fvgFUTNc0bImxuuhqmSVlK9ZqkEfo9JI8adRZQ7CGq1AgMBAAGjIDAe\\r\\nMAwGA1UdEwEB/wQCMAAwDgYDVR0PAQH/BAQDAgbAMA0GCSqGSIb3DQEBCwUAA4IB\\r\\nAQAvwF8ORbJheZ9ktjRAt0Z7PX+l+Kg52NY+jgNYrOqsHNgf0O8UXr6UcH8EfvSa\\r\\nChpfZfP0tt0E87xVVcaaYNPXDDNksRIEaUdAVw7C7Lecnly+1T9aOcTjb+lFlYw3\\r\\nfa5idBvSG3Ypq82Zgt0Iu2B9/7fUqjJ6ieBJ/LyweuVZ/ryA2P+m9QTdiulvfOez\\r\\nH1rGl/Rdc3IaA9zwTKeyahsWd1jc0bdOYkG6yl+bqE69PHPhISc45timYwpm0g9w\\r\\n4mrkeV7oJ/R0lvM06fshf5J6u8ftje0BDfuYvOJ5RxZFRQ08s+na3kYiA0xe8HJ8\\r\\nt3SuK+neIQYqxuWfa6uuprUO\\r\\n-----END CERTIFICATE-----\\r\\n\",\r\n  \"credentialsCA\" : \"-----BEGIN CERTIFICATE-----\\r\\nMIIDsTCCApmgAwIBAgIUHQiTQxVFQdPtlef+Ny4n72x2bq0wDQYJKoZIhvcNAQEL\\r\\nBQAwgYExOzA5BgNVBAMMMkVsZWN0aW9uIEV2ZW50IENBIDMxNGJkMzRkY2Y2ZTRk\\r\\nZTRiNzcxYTkyZmEzODQ5ZDNkMRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRIwEAYD\\r\\nVQQKDAlTd2lzc1Bvc3QxCTAHBgNVBAcMADELMAkGA1UEBhMCQ0gwHhcNMTUwNjI2\\r\\nMDkyNTU3WhcNMTYwNjAxMTAxNTMwWjB+MTgwNgYDVQQDDC9DcmVkZW50aWFscyBD\\r\\nQSAzMTRiZDM0ZGNmNmU0ZGU0Yjc3MWE5MmZhMzg0OWQzZDEWMBQGA1UECwwNT25s\\r\\naW5lIFZvdGluZzESMBAGA1UECgwJU3dpc3NQb3N0MQkwBwYDVQQHDAAxCzAJBgNV\\r\\nBAYTAkNIMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiKTWrboTwMUD\\r\\nj27zcyUwhd/CspIBDQ5a8kHWjQimlU0dCecmNJPIUBcOXat2qP7da3P6CHdNLJGE\\r\\n4llvXvVetUQjzqlVQHl9M25KgQbH3nQ+U3HdfmCSyJsz+5qof1ScPnggrVLjsRuC\\r\\nqoZgRmJThG28ux3JI7ZKzWVo1I/FCem4shFjxYi6YNXP9sWdkwsbitjM3YPi4UEU\\r\\noQ5WRjk6zMjnWLo+S7KwgNFV073jdvqgurONkbcdhvU+kJgqNUsQjkNqIqxP5h41\\r\\nz08BEvs0k4Mit64bBl1ljfTeqiGc6DNQn/7oLAfHK9rttRHPzvpn5AeYSetJZ4KJ\\r\\nOVBvk0zUUQIDAQABoyMwITAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIB\\r\\nBjANBgkqhkiG9w0BAQsFAAOCAQEAPywctiODIOmBeqdcA70Plpzjem3yL5LSjQiv\\r\\nc1WuCvDkzKll6wNplOZJPmmWNXU+8nPsEkJT9laI8pjHzxr4gwcSR8xywllh2WPc\\r\\nAyOT6XlIt4DFgDtNOzT4qV5rhCuc3Yw33SPsNLpvOLGa2WJt2vCdVnK0GmIfDtkJ\\r\\n9xS6rt+yTeriynrEAztEL9yNrr2uJugxp5oKXEccUL2uslVALEktB68Lw+816HkH\\r\\nYI5KcDKIyL6FJzLTH0NpRzN5O7uFg0qGjE/qhrRx4Wiz67RQO7Fp5X/Igv3dj4RG\\r\\naFOyFb0Qw4GoN5Gf/9yJfljF19iGDM0HilkSEDtBSyyFEP3Myg==\\r\\n-----END CERTIFICATE-----\\r\\n\",\r\n  \"servicesCA\" : \"-----BEGIN CERTIFICATE-----\\r\\nMIIDrjCCApagAwIBAgIUXdpNxV3YTCXlStOZEuZvscC42yQwDQYJKoZIhvcNAQEL\\r\\nBQAwgYExOzA5BgNVBAMMMkVsZWN0aW9uIEV2ZW50IENBIDMxNGJkMzRkY2Y2ZTRk\\r\\nZTRiNzcxYTkyZmEzODQ5ZDNkMRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRIwEAYD\\r\\nVQQKDAlTd2lzc1Bvc3QxCTAHBgNVBAcMADELMAkGA1UEBhMCQ0gwHhcNMTUwNjI2\\r\\nMDkyNTU3WhcNMTYwNjAxMTAxNTMwWjB7MTUwMwYDVQQDDCxTZXJ2aWNlcyBDQSAz\\r\\nMTRiZDM0ZGNmNmU0ZGU0Yjc3MWE5MmZhMzg0OWQzZDEWMBQGA1UECwwNT25saW5l\\r\\nIFZvdGluZzESMBAGA1UECgwJU3dpc3NQb3N0MQkwBwYDVQQHDAAxCzAJBgNVBAYT\\r\\nAkNIMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApD4Tr82hOzAFNBSv\\r\\n8L6iqE03ojfT6P24sWAdmM/YnGAIor9Syu0J7xtlbIXxnz6q5SC/xgIvl3pa2iFc\\r\\njFsoexUJZaEXx2ULVhuNxnbAiF/Rhwk2GgA2j6QkBnNO06kNa3dj+QiZjxz/c6hJ\\r\\nVlaLUA5h+fooaT5oEI+Ba1Bij2GBcZ3xGvjOTzyItKGoZ4vJ1vZbHKBs8QaxOlkG\\r\\n6nH5PoicxjGhVedyueIs2M3jhJbbnMlbT4XFViZqu97zmwUkhMqptNlQ6Jh9BQE6\\r\\n4tBird5ArIEHbKW0bpi8c1fV9Oqr4fkUY2qgFIqntP9+cYdmCS9hST5xHxYkUAT6\\r\\nvGAg1wIDAQABoyMwITAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAN\\r\\nBgkqhkiG9w0BAQsFAAOCAQEAlIDq1NZuYVUftfqUB9Vl0tZwB2D7Uzk3XjuScVRD\\r\\nFiB4S+7XcqWu1TATxhu78/Eo6u38m2y+52IBOrUPdm6sawelAZh7k8pAQn7EDiwR\\r\\nOY5so2vcZekauFYt5kN6EMhQglUySOgaQlFP7qXSkRo2JM6f86z6/tagYmZo0Cfk\\r\\n35cWAcjwbNZJmuSlvirxQHP/x08D6NM8AcRe5Twg6JYQ6wHyNqRtg5heXiOWx2DO\\r\\npkKVBNc2HWi1xisOmuW/Qgckyxn0AgahGHhfXAJYKNtO2lnp3GG815WpuhHC1hZt\\r\\nFhgMZCY+M7TIPkcZ0x7nOR3ZuoOLjKSTuorrdPIhdop+hw==\\r\\n-----END CERTIFICATE-----\\r\\n\",\r\n  \"signature\" : null\r\n}";
	private final String cert = "-----BEGIN CERTIFICATE-----\r\nMIIDqzCCApOgAwIBAgIUfsSys3uhBqZ/40LXtkdDJigZyeUwDQYJKoZIhvcNAQEL\r\nBQAwfjE4MDYGA1UEAwwvQ3JlZGVudGlhbHMgQ0EgMzE0YmQzNGRjZjZlNGRlNGI3\r\nNzFhOTJmYTM4NDlkM2QxFjAUBgNVBAsMDU9ubGluZSBWb3RpbmcxEjAQBgNVBAoM\r\nCVN3aXNzUG9zdDEJMAcGA1UEBwwAMQswCQYDVQQGEwJDSDAeFw0xNTA2MjYwOTI4\r\nMjhaFw0xNjA2MDExMDE1MzBaMH8xOTA3BgNVBAMMMENyZWRlbnRpYWwgQXV0aCAz\r\nMTRiZDM0ZGNmNmU0ZGU0Yjc3MWE5MmZhMzg0OWQzZDEWMBQGA1UECwwNT25saW5l\r\nIFZvdGluZzESMBAGA1UECgwJU3dpc3NQb3N0MQkwBwYDVQQHDAAxCzAJBgNVBAYT\r\nAkNIMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqLWP/rV7OmHh6r1o\r\nFQGi+W+pcOMz8RbNp4czgVy0fs/TMYwPiHoCUmcut05awJNneWyt3b68nbtuEW7f\r\ncxtKz/i73rj7oMLnmFvrIWS+yvvzwvZe9jeife+ODIbu5gBhGdq3oNvFN1YyIrCv\r\nybo7YaGJr5OBVdjgFr7fMJ7942cEeB/Qj/MmGlNo0DRttdKGx6prbQC9+GZ13zWK\r\nAKcDe9CLGfYAwhhir4V5fCJTr+rkB1ajSvSZD0h/jjYAOUHxTCndFPk9sMGlcTff\r\n7JNWTM/NoVqgqdecNVyK4R6ruvYn5sOdZ1bLpb2CEusnVwsjS7tijYtkGslS7j84\r\nmAM2cQIDAQABoyAwHjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIGwDANBgkq\r\nhkiG9w0BAQsFAAOCAQEAYiddXkmYt0n96JySL8+adUUZVifx8BFWO9JAJlbd1jl3\r\nfNPNdLFl2j3UJyQlsYr3NFeNT65MjDK13IbZFRghgEpCMQEcn/iHO3Kix5GrImo5\r\nll7OsJVAJuo8ReDbxglTNJZ1eDX89xKqfaI/Ng0meqrmHnz/Y0zq8f8OE48DyKxQ\r\nlEqgRP+QCpNCmPJ9/l+h9j/hHMOLH54BI3dlAJuTpYCFETVfL5matqqQm2vQ3Bub\r\nxVAqgJfM+0h5m1rnBAEuhA7xnueHYYPbtOfuND4Yyvon8r9Ar8lFL8T0GFCZgsqQ\r\nudu1o+P7rtvbWxMA+5Gf8x/UFolVwGc83TWiF56UDg==\r\n-----END CERTIFICATE-----\r\n";
	private final String tenantId = "100";
	private final String electionEventId = "100";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	Logger logger;
	@Spy
	@Inject
	@InjectMocks
	private CertificateChainValidationService certificateChainValidationService;
	@Mock
	private AuthenticationCertsRepository authenticationCertsRepositoryMock;
	@Mock
	private X509CertificateChainValidator certificateChainValidatorMock;
	@Mock
	private X509CertificateChainValidator certificateChainValidatorMock2;
	@Mock
	private X509CertificateChainValidator certificateChainValidatorAlwaysFalse;

	@Test
	public void validateOK1() throws ResourceNotFoundException, GeneralCryptoLibException, SyntaxErrorException, SemanticErrorException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(json);
		doNothing().when(certificateChainValidationService).validateCerts(authCerts);

		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId)).thenReturn(authCerts);
		doReturn(certificateChainValidatorMock).when(certificateChainValidationService)
				.createCertificateChainValidator(any(X509Certificate.class), any(X509DistinguishedName.class), any(X509CertificateType.class),
						any(X509Certificate[].class), any(X509DistinguishedName[].class), any(X509Certificate.class));
		when(certificateChainValidatorMock.validate()).thenReturn(null);

		ValidationResult result = certificateChainValidationService.validate(tenantId, electionEventId, cert);

		assertTrue(result.isResult());
	}

	@Test
	public void validateOK2() throws ResourceNotFoundException, GeneralCryptoLibException, SyntaxErrorException, SemanticErrorException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(json);
		doNothing().when(certificateChainValidationService).validateCerts(authCerts);

		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId)).thenReturn(authCerts);
		doReturn(certificateChainValidatorMock).when(certificateChainValidationService)
				.createCertificateChainValidator(any(X509Certificate.class), any(X509DistinguishedName.class), any(X509CertificateType.class),
						any(X509Certificate[].class), any(X509DistinguishedName[].class), any(X509Certificate.class));
		when(certificateChainValidatorMock.validate()).thenReturn(new ArrayList<String>());

		ValidationResult result = certificateChainValidationService.validate(tenantId, electionEventId, cert);

		assertTrue(result.isResult());
	}

	@Test
	public void validateCertsNotFound() throws ResourceNotFoundException, GeneralCryptoLibException, SyntaxErrorException, SemanticErrorException {
		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId)).thenThrow(new ResourceNotFoundException(""));

		ValidationResult result = certificateChainValidationService.validate(tenantId, electionEventId, cert);

		assertFalse(result.isResult());
	}

	@Test
	public void validateSyntaxFailed() throws ResourceNotFoundException, GeneralCryptoLibException, SyntaxErrorException, SemanticErrorException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(null);
		doThrow(new SyntaxErrorException(null)).when(certificateChainValidationService).validateCerts(authCerts);

		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId)).thenReturn(authCerts);

		ValidationResult result = certificateChainValidationService.validate(tenantId, electionEventId, cert);

		assertFalse(result.isResult());
	}

	@Test
	public void validateSemanticFailed() throws ResourceNotFoundException, GeneralCryptoLibException, SyntaxErrorException, SemanticErrorException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(null);
		doThrow(new SemanticErrorException(null)).when(certificateChainValidationService).validateCerts(authCerts);

		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId)).thenReturn(authCerts);

		ValidationResult result = certificateChainValidationService.validate(tenantId, electionEventId, cert);

		assertFalse(result.isResult());
	}

	@Test
	public void validateRootValidationFailed()
			throws ResourceNotFoundException, GeneralCryptoLibException, SyntaxErrorException, SemanticErrorException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(json);
		doNothing().when(certificateChainValidationService).validateCerts(authCerts);

		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId)).thenReturn(authCerts);
		doReturn(certificateChainValidatorMock).when(certificateChainValidationService)
				.createCertificateChainValidator(any(X509Certificate.class), any(X509DistinguishedName.class), any(X509CertificateType.class),
						any(X509Certificate[].class), any(X509DistinguishedName[].class), any(X509Certificate.class));
		List<String> listErrors = new ArrayList<String>();
		listErrors.add("ERROR");
		when(certificateChainValidatorMock.validate()).thenReturn(listErrors);

		ValidationResult result = certificateChainValidationService.validate(tenantId, electionEventId, cert);

		assertFalse(result.isResult());
	}

	@Test
	public void validateRootValidationCryptoException()
			throws ResourceNotFoundException, GeneralCryptoLibException, SyntaxErrorException, SemanticErrorException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(json);
		doNothing().when(certificateChainValidationService).validateCerts(authCerts);

		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId)).thenReturn(authCerts);
		doThrow(new GeneralCryptoLibException("")).when(certificateChainValidationService)
				.createCertificateChainValidator(any(X509Certificate.class), any(X509DistinguishedName.class), any(X509CertificateType.class),
						any(X509Certificate[].class), any(X509DistinguishedName[].class), any(X509Certificate.class));

		ValidationResult result = certificateChainValidationService.validate(tenantId, electionEventId, cert);

		assertFalse(result.isResult());
	}

	@Test
	public void validationFailed()
			throws ResourceNotFoundException, GeneralCryptoLibException, SyntaxErrorException, SemanticErrorException, CertificateException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(json);
		doNothing().when(certificateChainValidationService).validateCerts(authCerts);

		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId)).thenReturn(authCerts);
		final String rootCA = AuthenticationUtils.getRootCA(authCerts.getJson());
		final String credentialsCA = AuthenticationUtils.getCredentialCA(authCerts.getJson());
		final X509Certificate rootCACert = AuthenticationUtils.getX509Cert(rootCA);
		final X509Certificate credentialsCACert = AuthenticationUtils.getX509Cert(credentialsCA);
		final X509DistinguishedName rootCADistinguishedName = AuthenticationUtils.getDistinguishName(rootCACert);
		final X509DistinguishedName credentialsCADistinguishedName = AuthenticationUtils.getDistinguishName(credentialsCACert);

		final X509Certificate x509Cert = AuthenticationUtils.getX509Cert(cert);
		final X509DistinguishedName distinguishedName = AuthenticationUtils.getDistinguishName(x509Cert);
		final X509Certificate[] certificates = { credentialsCACert };
		final X509DistinguishedName[] distinguishedNames = { credentialsCADistinguishedName };

		doReturn(certificateChainValidatorMock).when(certificateChainValidationService)
				.createCertificateChainValidator(rootCACert, rootCADistinguishedName, X509CertificateType.CERTIFICATE_AUTHORITY,
						EMPTY_ARRAY_CERTIFICATES, EMPTY_ARRAY_DISTINGUISHED_NAMES, rootCACert

				);

		List<String> listErrors = new ArrayList<String>();
		listErrors.add("ERROR");
		when(certificateChainValidatorAlwaysFalse.validate()).thenReturn(listErrors);
		doReturn(certificateChainValidatorAlwaysFalse).when(certificateChainValidationService)
				.createCertificateChainValidator(x509Cert, distinguishedName, X509CertificateType.SIGN, certificates, distinguishedNames, rootCACert

				);
		ValidationResult result = certificateChainValidationService.validate(tenantId, electionEventId, cert);

		assertFalse(result.isResult());
	}

	@Test
	public void createCertificateChainValidator() throws CertificateException, GeneralCryptoLibException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(json);
		final String rootCA = AuthenticationUtils.getRootCA(authCerts.getJson());
		final X509Certificate rootCACert = AuthenticationUtils.getX509Cert(rootCA);
		final X509DistinguishedName rootCADistinguishedName = AuthenticationUtils.getDistinguishName(rootCACert);

		doCallRealMethod().when(certificateChainValidationService)
				.createCertificateChainValidator(rootCACert, rootCADistinguishedName, X509CertificateType.CERTIFICATE_AUTHORITY,
						EMPTY_ARRAY_CERTIFICATES, EMPTY_ARRAY_DISTINGUISHED_NAMES, rootCACert

				);

		final X509CertificateChainValidator certificateChainValidator = certificateChainValidationService
				.createCertificateChainValidator(rootCACert, rootCADistinguishedName, X509CertificateType.CERTIFICATE_AUTHORITY,
						EMPTY_ARRAY_CERTIFICATES, EMPTY_ARRAY_DISTINGUISHED_NAMES, rootCACert);
		assertNotNull(certificateChainValidator);

	}

	@Test
	public void validateCertificates() throws SemanticErrorException, SyntaxErrorException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(json);
		authCerts.setElectionEventId("100");
		authCerts.setTenantId("100");
		authCerts.setSignature("signature");
		certificateChainValidationService.validateCerts(authCerts);

	}

	@Test
	public void validateFailInChain() throws ResourceNotFoundException, GeneralCryptoLibException, SyntaxErrorException, SemanticErrorException {
		AuthenticationCerts authCerts = new AuthenticationCerts();
		authCerts.setJson(json);
		doNothing().when(certificateChainValidationService).validateCerts(authCerts);

		when(authenticationCertsRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId)).thenReturn(authCerts);

		doReturn(certificateChainValidatorMock).when(certificateChainValidationService)
				.createCertificateChainValidator(any(X509Certificate.class), any(X509DistinguishedName.class),
						eq(X509CertificateType.CERTIFICATE_AUTHORITY), any(X509Certificate[].class), any(X509DistinguishedName[].class),
						any(X509Certificate.class));
		when(certificateChainValidatorMock.validate()).thenReturn(new ArrayList<String>());
		doReturn(certificateChainValidatorMock2).when(certificateChainValidationService)
				.createCertificateChainValidator(any(X509Certificate.class), any(X509DistinguishedName.class), eq(X509CertificateType.SIGN),
						any(X509Certificate[].class), any(X509DistinguishedName[].class), any(X509Certificate.class));
		ArrayList<String> listErrors = new ArrayList<>();
		listErrors.add("ERROR");
		when(certificateChainValidatorMock2.validate()).thenReturn(listErrors);

		ValidationResult result = certificateChainValidationService.validate(tenantId, electionEventId, cert);

		assertFalse(result.isResult());
	}

	@Test
	public void createCertificatesValidator() throws GeneralCryptoLibException {
		expectedException.expect(GeneralCryptoLibException.class);
		certificateChainValidationService.createCertificateChainValidator(null, null, null, null, null, null);
	}

}
