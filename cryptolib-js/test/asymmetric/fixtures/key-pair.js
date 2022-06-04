/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

module.exports = KeyPair;

/**
 * Defines an RSA key pair with 2048 bits.
 */
function KeyPair() {
	const PUBLIC_KEY = [
		'-----BEGIN PUBLIC KEY-----',
		'MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAglypZU45bnf2opnRI+Y5',
		'1VqouBpvDT33xIB/OtbwKzwVpi+JrjBFtfk33tE9t/dSRs79CK94HRhCWcOiLa2q',
		'WPrjeZ9SBiEyScrhIvRZVBF41zBgwQNuRvJCsKmAqlZaFNJDZxEP4repmlBn1CfV',
		'FmfrXmOKqwP5F7l9ZtucveRzsfmF1yVPFkW8TMuB3YqMiyymyqHlS8ujCsu5I8tp',
		'gPbwuxdMOY94fNhSXrYkY8IuX1g1zdq/Z1jluOaR/UqK4UpnbuJaH/F0VgDNiWh6',
		'cTD0DFGEk0b70i5wU4Q3L/S6XZQRvSuADoCbhwBKuFL5pW5n865oLVb5S3wuVdWa',
		'GwIDAQAB', '-----END PUBLIC KEY-----'
	].join('\n');

	const PRIVATE_KEY = [
		'-----BEGIN RSA PRIVATE KEY-----',
		'MIIEowIBAAKCAQEAglypZU45bnf2opnRI+Y51VqouBpvDT33xIB/OtbwKzwVpi+J',
		'rjBFtfk33tE9t/dSRs79CK94HRhCWcOiLa2qWPrjeZ9SBiEyScrhIvRZVBF41zBg',
		'wQNuRvJCsKmAqlZaFNJDZxEP4repmlBn1CfVFmfrXmOKqwP5F7l9ZtucveRzsfmF',
		'1yVPFkW8TMuB3YqMiyymyqHlS8ujCsu5I8tpgPbwuxdMOY94fNhSXrYkY8IuX1g1',
		'zdq/Z1jluOaR/UqK4UpnbuJaH/F0VgDNiWh6cTD0DFGEk0b70i5wU4Q3L/S6XZQR',
		'vSuADoCbhwBKuFL5pW5n865oLVb5S3wuVdWaGwIDAQABAoIBAC/tn34Wf3kE9BGe',
		'Gc1oFLVDaqqdVVz5/oEpeR2J7q0GnzMFYUpAhzC7WvY52cYsUPyll1Q9Jx0TUTmt',
		'eo/uvKWQQFfz4nVMeS+2PoXabolBDzuWlsv/1eiRo0FOYHa/3siu8YcQN9X0DpAk',
		'pbfTmT1uoZOHZ3EuucMmOFu7vGn38Grw8bSxpR0uvTtnb8ygC+aB51y38RMyhzQQ',
		'anrM8FMeAfDAy6IB0Yo7b0c50Cxa6Ax4nqn9LXyGakr5WeAMkgTIOA/GId9SZD4e',
		'5eRpq+628pOeR4O9datFltgl6r1+A4ii2VrJsDqeatGtODlX6KRKqwFHoGIa2Tjg',
		'SZLuorECgYEAxeSZDOOgFsI5mB7RkRzZaQ9znJ15sgdyZiAFZAOUah4hSGdAXNAn',
		'ZTlrdacduXEu3EfkpuPToX7xZSv5FRYwfBwMwCLeytlGLPjQzWejZGbo4+KqgzWb',
		'9fECDYVtDPlJ/+yLih9nt67BHweJKxYydl18rVigdVyy22X86NijSykCgYEAqKPU',
		'rXZAo+TJvmTw4tgsibJgvXBYBhmsej8mGNQw+Nyp2gV28sgm61ifIeXKS8teq+MF',
		'wGA6cHQedbsCqhMHokdhESZmlbWxhSFLihQcewBxwvrBwbaxI23yXRzwMewznZFL',
		'032PpcbqrmwFmcSSEZ3nmbvTH6ShqLW+pzDNp6MCgYBQLzdgxJ7qedqSa/JohTMG',
		'4e7rh9d2rpPJE7J7ewPZF8pOpx+qO+Gqn2COdJ+Ts2vUcAETKn9nEaPIZc/wnmQY',
		'9dioxbhWo0FPGaaphBPtq9Ez/XUv4zoFppk5V1X/isdUPsmvttf00oeIBiqrXbwm',
		'v+yz5JRn2Z7TTXjz9Ev+OQKBgQCUuoCMRzl1EgcXIqEL/0kwW6BUEqufHa9u1Ri9',
		'Vw6lvL8T6DPipMEmWK9nzuid9gtVns/ovTVtDgv7GuabplLaPQePf4WDzY11c0rS',
		'yS/hDyBFrK+LL5uEOqhAlJAGB2HyOj1clWVF+GvrTpuV5LZKUS/79pmZU7G7QCaX',
		'/0Ow7wKBgC/kDH7cmWQnWvvJ5izrx/7PogQVPOLELeUIGLu/hjsSdDKiFCxCUZ94',
		'8+9NuG+DnpXDWzw//r8mPBRRGGsqFws5Aipp7yjQ3kRDCCzGelPCVhHyfmKqA+8e',
		'wXPulKS3/wIyHIvaXmsuAtTfurHtpRyzjKmCBK1Y6WQ3trIXvo7s',
		'-----END RSA PRIVATE KEY-----'
	].join('\n');

	this.getPublic = function () {
        return PUBLIC_KEY;
    };

    this.getPrivate = function () {
        return PRIVATE_KEY;
    };
}
