/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

public class SigningTestData {

	public static final String PUBLIC_KEY_PEM = "-----BEGIN PUBLIC KEY-----\n" + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvtfglb/eQNYGMHGA+4HO\n"
			+ "b74hjawscpc68H0gn1HVSiZ7osUd4vD/9EmpMEMuHqAjcLkFVd8GP61M1sx0VTQ6\n"
			+ "j9ypQQzdqiIuDHRBzg8U9ejXX3xvH9WxxAnXYNgTT3FwcoW2R4ng0Idz3jvrPkdT\n"
			+ "YAEu/DNoiM6BqpSHSMhqc2L1gW+kGsGhm/W4vP2CCvmIhxzmU4IK/xZXdjBO0YHc\n"
			+ "qqLlajhgcdrrejd42YvjtwnXItV5QIPkA9pSyHbvqi7QpfWiGmFkEdrmnY0W84wn\n"
			+ "NApZyviwfQDoYUo7ZXwXsJAmV9xhEXD7TXpcdFfLFRgmMOxo5U+YOuR8R4b9WiTq\n" + "YQIDAQAB\n" + "-----END PUBLIC KEY-----";

	public static final String PRIVATE_KEY_PEM =
			"-----BEGIN RSA PRIVATE KEY-----\n" + "MIIEpQIBAAKCAQEAvtfglb/eQNYGMHGA+4HOb74hjawscpc68H0gn1HVSiZ7osUd\n"
					+ "4vD/9EmpMEMuHqAjcLkFVd8GP61M1sx0VTQ6j9ypQQzdqiIuDHRBzg8U9ejXX3xv\n"
					+ "H9WxxAnXYNgTT3FwcoW2R4ng0Idz3jvrPkdTYAEu/DNoiM6BqpSHSMhqc2L1gW+k\n"
					+ "GsGhm/W4vP2CCvmIhxzmU4IK/xZXdjBO0YHcqqLlajhgcdrrejd42YvjtwnXItV5\n"
					+ "QIPkA9pSyHbvqi7QpfWiGmFkEdrmnY0W84wnNApZyviwfQDoYUo7ZXwXsJAmV9xh\n"
					+ "EXD7TXpcdFfLFRgmMOxo5U+YOuR8R4b9WiTqYQIDAQABAoIBAQCo8q/tojf9qfs8\n"
					+ "s57+CMJahjVqGDwZDeytrfhNUsLBrCPTyzFUEQpmlzdidHbFwfrd9c3VYWVExgS6\n"
					+ "O7HZJC2b4jssTVmHHk8p01nWM9/Ye1L/Q0eVJTcEV7oGEAO4VrK3j8v4tQUDVoaq\n"
					+ "/Nya+8XBvgp8Vn56bma4Plktg89JaY2T2wgoFVC17arfAvi/ktlzbHTgwpY0MqF8\n"
					+ "sA6J/U6ks3lbnwmfmMhD4L/tYPHPoxH17D6JOe2Nvoy81K5u57Ekd8MxGpxGi87j\n"
					+ "7lHtu/uTqy29XA8EgfzesIlsICw2t6R3NNAd5AJcSJC79hP1Ad6/I/RffKcO/Dxm\n"
					+ "tb6KfcFhAoGBAPo23QMzegPFUJkm5yY4vh+zYBkq8BqvDi1puScunl9GoDAqid65\n"
					+ "kwpmYRlXNduBGKQ5OQqEYj/wCCntwjYPWTLxgcU5w79H56cI1LvstvdzWC1IhA3g\n"
					+ "33k7oGev70GGlLw9MBQrSOSK6hJMywCrV7d5SjAa5KhQaONhXm3mfIjNAoGBAMNB\n"
					+ "kZiRX0gC5mNwAkG5RvTpWZdUkrhJ/fSOM5hfnhRf/GXhz11t5PQKB0yEegX7+gjJ\n"
					+ "g6H5av1XmqhyWs2WCsAPL4iUIbgXsBpDBe94HiLBWH+VlrlSkjEmSuXJ0pYwUaeM\n"
					+ "QIRFmMWz1vaf/yk7NdAYVVDAq6o+0O+trcQSxLflAoGBAOqt4oVKFrrKqlm63zo+\n"
					+ "JjRdbVitqR2d6tI1Qu+5DdmWyE/k1gMMUxmCBLhbz6vdXVtKQHHY0L3fSAjrcyh5\n"
					+ "JNRfJ7PwBeS2cFN+OOk5kDJvANkYFqNdsrxmbIBTxzfSHlafnSSNpISWtgwL8qri\n"
					+ "ChePr0GHN8eWA4qgnIy/jTZRAoGAfUlhZwhAnzBdLIj+qbV89kGVHylBS8cOkRF9\n"
					+ "wdP41xIQHj+ak1SkiIK21D5dHeHlOiYztIcaQ50mu9dEtI4GL96OnZCPYlSwxuki\n"
					+ "sdndXGe7pYISyK9W5vqvLGsoGP7AJkY0T6tor9MJqA5Z59b6XKCAfeRjQkY0Qtaq\n"
					+ "nRl96JECgYEA7gNjlzl7jwBrFMqjLwEvTWfkinvPThCQhUlQk0VD2S6SCPINgbSy\n"
					+ "VWrsKAmhQGyX2l+vSZsiycGTit5Oz9BRccZ1cgT7TEuC3rQdJQ3QyfVASliAT+mc\n"
					+ "g0vac4FYNnifuQfshyIY3w6dx7jFaJW+Zrt6q/WYUo3apt9MfjbpwkQ=\n" + "-----END RSA PRIVATE KEY-----";
}
