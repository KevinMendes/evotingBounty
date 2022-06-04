/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link DerivedKeyCache}.
 */
class DerivedKeyCacheImpl implements DerivedKeyCache {

	private final Map<String, KeyAndCounter> keyAndCounters = new HashMap<>();

	private final Map<Owner, String> passwords = new HashMap<>();

	@Override
	public byte[] get(final char[] password) {
		final KeyAndCounter keyAndCounter = keyAndCounters.get(new String(password));
		return keyAndCounter != null ? keyAndCounter.key : null;
	}

	@Override
	public void putForElGamalPrivateKey(final String alias, final char[] password, final byte[] key) {
		put(Owner.newElGamalPrivateKeyInstance(alias), password, key);
	}

	@Override
	public void putForKeyStore(final char[] password, final byte[] key) {
		put(Owner.getKeyStoreInstance(), password, key);
	}

	@Override
	public void putForPrivateKey(final String alias, final char[] password, final byte[] key) {
		put(Owner.newPrivateKeyInstance(alias), password, key);
	}

	@Override
	public void putForSecretKey(final String alias, final char[] password, final byte[] key) {
		put(Owner.newSecretKeyInstance(alias), password, key);
	}

	@Override
	public void removeForElGamalPrivateKey(final String alias) {
		remove(Owner.newElGamalPrivateKeyInstance(alias));
	}

	@Override
	public void removeForPrivateKey(final String alias) {
		remove(Owner.newPrivateKeyInstance(alias));
	}

	@Override
	public void removeForSecretKey(final String alias) {
		remove(Owner.newSecretKeyInstance(alias));
	}

	private void put(final Owner owner, final char[] password, final byte[] key) {
		final String newPassword = new String(password);
		final String oldPassword = passwords.put(owner, newPassword);
		if (!newPassword.equals(oldPassword)) {
			if (oldPassword != null) {
				final KeyAndCounter keyAndCounter = keyAndCounters.get(oldPassword);
				if (keyAndCounter != null && --keyAndCounter.counter == 0) {
					keyAndCounters.remove(oldPassword);
				}
			}
			KeyAndCounter keyAndCounter = keyAndCounters.get(newPassword);
			if (keyAndCounter == null) {
				keyAndCounter = new KeyAndCounter(key);
				keyAndCounters.put(newPassword, keyAndCounter);
			}
			keyAndCounter.counter++;
		}
	}

	private void remove(final Owner owner) {
		final String password = passwords.remove(owner);
		if (password != null) {
			final KeyAndCounter keyAndCounter = keyAndCounters.get(password);
			if (keyAndCounter != null && --keyAndCounter.counter == 0) {
				keyAndCounters.remove(password);
			}
		}
	}

	private enum OwnerType {
		KEY_STORE,
		SECRET_KEY,
		PRIVATE_KEY,
		EL_GAMAL_PRIVATE_KEY,
	}

	private static final class KeyAndCounter {
		final byte[] key;

		int counter;

		KeyAndCounter(final byte[] key) {
			this.key = key;
		}
	}

	private static class Owner {
		private static final Owner KEY_STORE_INSTANCE = new Owner(OwnerType.KEY_STORE, null);

		private final OwnerType type;

		private final String alias;

		private Owner(final OwnerType type, final String alias) {
			this.type = type;
			this.alias = alias;
		}

		static Owner getKeyStoreInstance() {
			return KEY_STORE_INSTANCE;
		}

		static Owner newElGamalPrivateKeyInstance(final String alias) {
			return new Owner(OwnerType.EL_GAMAL_PRIVATE_KEY, alias);
		}

		static Owner newPrivateKeyInstance(final String alias) {
			return new Owner(OwnerType.PRIVATE_KEY, alias);
		}

		static Owner newSecretKeyInstance(final String alias) {
			return new Owner(OwnerType.SECRET_KEY, alias);
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
			final Owner other = (Owner) obj;
			if (alias == null) {
				if (other.alias != null) {
					return false;
				}
			} else if (!alias.equals(other.alias)) {
				return false;
			}
			return type == other.type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((alias == null) ? 0 : alias.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
	}
}
