/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.serialization;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

@JsonIgnoreProperties({ "anotherTestString", "anotherTestInteger" })
public class SerializableTestClass extends AbstractJsonSerializable {

	@JsonProperty("testString")
	private final String _testString;

	@JsonProperty("testInteger")
	private final Integer _testInteger;

	@JsonProperty("testBigInteger")
	private final BigInteger _testBigInteger;

	@JsonProperty("testBigIntegerList")
	private final List<BigInteger> _testBigIntegerList;

	private String _anotherTestString;

	private Integer _anotherTestInteger;

	@SuppressWarnings("unused")
	private SerializableTestClass() {
		super();

		_testString = "";
		_testInteger = 0;
		_testBigInteger = BigInteger.ZERO;
		_testBigIntegerList = new ArrayList<>();
		_anotherTestString = "";
		_anotherTestInteger = 0;
	}

	public SerializableTestClass(final String testString, final Integer testInteger, final BigInteger testBigInteger,
			final List<BigInteger> testBigIntegerList) {
		super();

		_testString = testString;
		_testInteger = testInteger;
		_testBigInteger = testBigInteger;
		_testBigIntegerList = testBigIntegerList;
		_anotherTestString = "";
		_anotherTestInteger = 0;
	}

	public SerializableTestClass(final String jsonStr) throws GeneralCryptoLibException {
		super();

		final JsonMapper serializer = new JsonMapper();
		final SerializableTestClass objFromJson = serializer.fromJson(SerializableTestClass.class, jsonStr);

		_testString = objFromJson.getTestString();
		_testInteger = objFromJson.getTestInteger();
		_testBigInteger = objFromJson.getTestBigInteger();
		_testBigIntegerList = objFromJson.getTestBigIntegerList();
		_anotherTestString = "";
		_anotherTestInteger = 0;
	}

	public String getTestString() {

		return _testString;
	}

	public int getTestInteger() {

		return _testInteger;
	}

	public BigInteger getTestBigInteger() {

		return _testBigInteger;
	}

	public List<BigInteger> getTestBigIntegerList() {

		return _testBigIntegerList;
	}

	public String getAnotherTestString() {

		return _anotherTestString;
	}

	public void setAnotherTestString(final String anotherTestString) {

		_anotherTestString = anotherTestString;
	}

	public int getAnotherTestInteger() {

		return _anotherTestInteger;
	}

	public void setAnotherTestInteger(final Integer anotherTestInteger) {

		_anotherTestInteger = anotherTestInteger;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_testBigInteger == null) ? 0 : _testBigInteger.hashCode());
		result = prime * result + ((_testBigIntegerList == null) ? 0 : _testBigIntegerList.hashCode());
		result = prime * result + ((_testInteger == null) ? 0 : _testInteger.hashCode());
		result = prime * result + ((_testString == null) ? 0 : _testString.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SerializableTestClass other = (SerializableTestClass) obj;
		if (_testBigInteger == null) {
			if (other._testBigInteger != null) {
				return false;
			}
		} else if (!_testBigInteger.equals(other._testBigInteger)) {
			return false;
		}
		if (_testBigIntegerList == null) {
			if (other._testBigIntegerList != null) {
				return false;
			}
		} else if (!_testBigIntegerList.equals(other._testBigIntegerList)) {
			return false;
		}
		if (_testInteger == null) {
			if (other._testInteger != null) {
				return false;
			}
		} else if (!_testInteger.equals(other._testInteger)) {
			return false;
		}
		if (_testString == null) {
			return other._testString == null;
		} else {
			return _testString.equals(other._testString);
		}
	}
}
