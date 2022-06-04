/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

const chai = require('chai');

chai.config.includeStack = true;

global.expect = chai.expect;
global.AssertionError = chai.AssertionError;
global.Assertion = chai.Assertion;
global.assert = chai.assert;
