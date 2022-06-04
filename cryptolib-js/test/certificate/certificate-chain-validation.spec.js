/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const { expect } = require('chai');

const certificate = require('../../src/certificate');

const organizationalUnitValue = 'Online Voting';
const organizationValue = 'Swiss Post';

const _validChain = {
    leaf: {
        pem: '-----BEGIN CERTIFICATE-----MIIDkzCCAnugAwIBAgIJAIooUOqgVoc/MA0GCSqGSIb3DQEBCwUAMGYxCzAJBgNVBAYTAkNIMQ0wCwYDVQQIDARCZXJuMQ0wCwYDVQQHDA' +
            'RCZXJuMRMwEQYDVQQKDApTd2lzcyBQb3N0MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMQwwCgYDVQQDDANDQTIwHhcNMjAwMzA5MTQ1NzE0WhcNMjEwMzA2MTQ1NzE0WjBuMQsw' +
            'CQYDVQQGEwJDSDENMAsGA1UECAwEQmVybjENMAsGA1UEBwwEQmVybjETMBEGA1UECgwKU3dpc3MgUG9zdDEWMBQGA1UECwwNT25saW5lIFZvdGluZzEUMBIGA1UEAwwLSW50ZW' +
            'dyYXRpb24wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCrd5mgYL/o/SE4HM/VenJEHUPy1vYFCqn/XJ99iVe3+FmSXQXKwqGd18L79/m9cFfhkiNphT7knYBlX4k6' +
            '7niCq/jA4dUUEqWMvpJP/R2hrtRmR6SCJpAh3FMHPYZ52oqj6VkTjorIOg+5qVSR4mJxihsLRrG/P0A5jHGXoiYlokYXiN5sRCihTbNWhbviYD32bQSey2MjtF3v183ZRZCJeV' +
            'm4zO1inku1OgP6WtrejqJRVbbMBrYjW4nEkulG0RUFoOcg9RvAt/MnfDZDUwE8Xu4eSwjwlIiuuLYiNVdtSU6+QU/9/oDiO/TzDFyq9imAHCR94YcJhUns+jiOpZCPAgMBAAGj' +
            'PDA6MAkGA1UdEwQCMAAwHQYDVR0OBBYEFNFnWVbD8RS9bG5Co9ky//qLWEPSMA4GA1UdDwEB/wQEAwIGwDANBgkqhkiG9w0BAQsFAAOCAQEAV+liapNQm4Frdrw0PaHNm5jWjo' +
            '4QAbfRKYdScxoqKYkWmFRjGpvuXgdK1Abzuy75YBbHVC84JXjCvdqXpnEI0hbz3yTtS04fGnA+IYH0I96nS6vCPGjaxwhmgSpH/5rkGhimsjvfv0JECuHBnKlLvXQnPTHl0yoZ' +
            'b9y3TGfvxOY3T9zd1Aq5oCUcJMfkjJUAw7NyeTS0MFPXNsAHYTI2C0hHGuAO3JTi4WjnWBKiVTHFQXIQEHFEM/67Dnh/hpWZaskUIS49Ip2nm4qYNnAfdyaBWoVzudWhw6siSE' +
            'PHoQ7IypugYNH+Tr8L78TMDqEfq2AEQKSs5++nrULexZGewg==-----END CERTIFICATE-----',
        keyType: 'Sign',
        subject: {
            commonName: 'Integration',
            organizationalUnit: organizationalUnitValue,
            organization: organizationValue,
            country: 'CH'
        },
        time: '2020-11-27\'T\'00:00:00Z'
    },
    intermediates: {
        pems: [
            '-----BEGIN CERTIFICATE-----MIIDizCCAnOgAwIBAgIJALdm+j7pvPyyMA0GCSqGSIb3DQEBCwUAMGYxCzAJBgNVBAYTAkNIMQ0wCwYDVQQIDARCZXJuMQ0wCwYDVQQHDAR' +
            'CZXJuMRMwEQYDVQQKDApTd2lzcyBQb3N0MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMQwwCgYDVQQDDANDQTEwHhcNMjAwMzA5MTQ1NzEzWhcNMjEwMzA3MTQ1NzEzWjBmMQswC' +
            'QYDVQQGEwJDSDENMAsGA1UECAwEQmVybjENMAsGA1UEBwwEQmVybjETMBEGA1UECgwKU3dpc3MgUG9zdDEWMBQGA1UECwwNT25saW5lIFZvdGluZzEMMAoGA1UEAwwDQ0EyMII' +
            'BIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx0TScPK6/WvyXj3yOw0UQWD1+fhAZ1JB7EQ/In+2UrdN46b+wnRck25Vi4U98NOaTQT37EASl/n2J+6/oHKuxC1xq7xju' +
            '/etoNnUTAXAffcSeEXruL3mJgNbCkwt3h9LOIwh5hnCYXAXBqNvnVSKHgT/0qXhLHaX27SQj3XkY1HP/FfY762XTn2V83MoeyZAiwrVbKU1c1Qee/gk2ts75aNB35ohxYLxFhh' +
            'oPmQaCA0mqs4pF3wK9UImNyc3/r4wSJSZXU2FuzMp6COpaTBLo7Bt0P2pw/evEAkryeoVAcGs4jyI/kvGjbYd454h9LJIzGEw0H1cs5YLc/gCYtszvQIDAQABozwwOjAMBgNVH' +
            'RMEBTADAQH/MB0GA1UdDgQWBBQU7IZQgrVjGPfShutAlUY+N9O29zALBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQELBQADggEBAEIsXSKeXV7bYp9spJlUOuD0yShJcGPEyU/46gJ' +
            'oWcYnIMCsJb7FS1ugb6c7Zq5xyLS/FCDEkjFmt57KiIBmf7O8oGfldfaOoaA6LUze6BdIDg2BjJu2U1kz6ozvIsU78+tEF8fopIYVPqi8aCdD/ADzw+19eaobUNf+yETj4mpnO' +
            'UbF7edjI9khtamrKv1m+ys+xVho6Zm6AsznN4eagOU2BleXmelHxswgI4cE5gfNX2OnDII3vIdnkou3VN2dA9auWSbBWesJfW/d5LrOyzL0hBYvq0NpkU5PgzElxxSWIL+FwER' +
            'QQ9LNyIHhoZkJpH+gxKHejTHEX2d/y6d+ZHM=-----END CERTIFICATE-----'
        ],
        subjects: [{
            commonName: 'CA2',
            organizationalUnit: organizationalUnitValue,
            organization: organizationValue,
            country: 'CH'
        }]
    },
    root: {
        pem: '-----BEGIN CERTIFICATE-----MIIDlTCCAn2gAwIBAgIJAP3Q8BTxUlrhMA0GCSqGSIb3DQEBCwUAMHAxCzAJBgNVBAYTAkNIMQ0wCwYDVQQIDARCZXJuMQ0wCwYDVQQHDA' +
            'RCZXJuMRMwEQYDVQQKDApTd2lzcyBQb3N0MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRYwFAYDVQQDDA1JbnRlZ3JhdGlvbkNBMB4XDTIwMDMwOTE0NTcxMloXDTIxMDMwODE0' +
            'NTcxMlowZjELMAkGA1UEBhMCQ0gxDTALBgNVBAgMBEJlcm4xDTALBgNVBAcMBEJlcm4xEzARBgNVBAoMClN3aXNzIFBvc3QxFjAUBgNVBAsMDU9ubGluZSBWb3RpbmcxDDAKBg' +
            'NVBAMMA0NBMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANle6GCjwKwrFkyz1CBvB7Ely14g6AHT6KrBmrVyTQJr87GZV7LwJ44IexRFnM9qJgLpMCb3M8uHHrFB' +
            '6vquDpvTrRA12z7VT3Tl24LbvkVtHVbtKc9Nxiu/W4lBUugWQkSuSec6T/5URec9dpMhZc8X/7Z0ePMPyRLMcsh6TL9YOt57CF5GyqtDVEYmkO9YSiFUfKVzMVMWrQee/Un8Qy' +
            '92JWaOgKFciOekWxucPue2+Givdns0yyDX2oFBSkoWJ407POgFqiF1R3PNAtxzS5BtQBrRXvHwp6sCB8oHKkzS4am6RYxx7HN+DnFudTtWNj3YLFBIARWyIBQJuXrRxVsCAwEA' +
            'AaM8MDowDAYDVR0TBAUwAwEB/zAdBgNVHQ4EFgQUsSwLy6/IkiMZpt2Dig/bJFZVMM4wCwYDVR0PBAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQCoFMwTVJswcspWDYo8Y91qmL' +
            'e1UPsVzAD2XGWD9WwuhL36aZ7N9nBQrU6l7bxyHXFZAhIby6Krc0qYULtIA1OV8kDVDng3rqeoFSU1L8oilVw1miCPEVDTAdDZQrNl4tDoy8lLf3aL8adwomN7sKCDSo2wEbYn' +
            'aKDVxva+0xV0P6usM31TQyvsTpsWLKusoRTssCa8LtLW5DL3wZeyIesSFY3hcjwouzZWraA+SwPHIeeXa7ewIcvXss5S2bd4BTzWg9h2/2UkZ5vdcpeb4N2ySbztZACfQm7L9S' +
            'MlMcrxebThnLQTGKchoX3JxlQlScNCwspx+jZqy9EdkYlNpPyE-----END CERTIFICATE-----'
    }
};

const _validChainFromJava = {
    leaf: {
        pem: '-----BEGIN CERTIFICATE-----MIIDeDCCAmCgAwIBAgIVALkmCoVa+2Bqg2xERPs1ewCCV+BoMA0GCSqGSIb3DQEBCwUAMFExCzAJBgNVBAYTAkNIMRIwEAYDVQQKDAlTd2' +
            'lzc1Bvc3QxFjAUBgNVBAsMDU9ubGluZSBWb3RpbmcxFjAUBgNVBAMMDVRlbmFudCAxMDAgQ0EwHhcNMjAxMTIzMTM1NzE0WhcNMjQwMTAxMjI1OTU5WjB4MQswCQYDVQQGEwJD' +
            'SDESMBAGA1UECgwJU3dpc3NQb3N0MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMT0wOwYDVQQDDDRBZG1pbmlzdHJhdGlvbkJvYXJkIGM5OTg3MDJjZDY3NzQxNDJhNGE4OTgzMG' +
            'FiYjY5MDE2MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA00nIYS8AR7rbW1tS91KYD4rQ1Y77I95X8mKBCxzcPqUlpsIc8I1XGg4P/JWloMc4gApA/O0XghFE+HuY' +
            'IOW5MfNIYVb6wQq9YoOXO42I/t3RthH4KdJTqhnTei/O6f8CHWfbVgF0VgvfpMDTHTioOH6hsSdGmMHUOROERJIrM9BXJ3ErVH0f18NMR1xbyjKNHD8KYbDaFtwGN/p9UGIkJ8' +
            'AH0L64sjpYEBe1NtfAqmh+xSm04iNWjI/yJVrToNEO86nyYTgs8pDX+f44SDNEAGaauZPRb3BwC+NDd4MtuAGWgr0xea/go16va8x6qWD0Niv/E4qA1JZ59oeb7Pm9fQIDAQAB' +
            'oyAwHjAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIGwDANBgkqhkiG9w0BAQsFAAOCAQEAqup6PhBF6F2WQ0iXnL2wI/fPze+tx3JP6iX8PogJRrsbKTXWPAHsiO/i74DMoo' +
            '7Z9H1geFGdfGdPKrKsH7HzQf5hV6LRDXUVHeMZoRWd3f58LFowOq3tFZ7uiS8Ce5c5dGmy2PYpFFeJzCQEw4vxMKlDsEwwTzeskCHJ9v7hcxvtw5h4l+9QIvh/oFAt+OzswnMD' +
            '7JABh7YJI5SJ1gENDNPFMfiurl1dIg56faEKQqxFFrR601tgdjlpX+7kQ6cy9GZDELCY+BHwJqR/QVvK6zF/jlBIqpEjM5qZ50AihoN6pBHVoQV+vYgJt5a5xM9Ryx1OQ0OngQ' +
            'lsOiyr4+0RPA==-----END CERTIFICATE-----',
        keyType: 'Sign',
        subject: {
            commonName: 'AdministrationBoard c998702cd6774142a4a89830abb69016',
            organizationalUnit: organizationalUnitValue,
            organization: 'SwissPost',
            country: 'CH'
        },
        time: '2020-11-23\'T\'14:57:00Z'
    },
    intermediates: {
        pems: [
            '-----BEGIN CERTIFICATE-----MIIDUjCCAjqgAwIBAgIVAMkkkxmwFBrq3aAATiHRw1/8eq72MA0GCSqGSIb3DQEBCwUAME8xCzAJBgNVBAYTAkNIMRIwEAYDVQQKDAlTd2l' +
            'zc1Bvc3QxFjAUBgNVBAsMDU9ubGluZSBWb3RpbmcxFDASBgNVBAMMC2RldiBSb290IENBMB4XDTIwMDEwMTIzMDAwMFoXDTI0MDEwMTIzMDAwMFowUTELMAkGA1UEBhMCQ0gxE' +
            'jAQBgNVBAoMCVN3aXNzUG9zdDEWMBQGA1UECwwNT25saW5lIFZvdGluZzEWMBQGA1UEAwwNVGVuYW50IDEwMCBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAPP' +
            'YCTW2937GPOGF7oYoofCUOJYt2e+6nK8bOGzDSIIuxFke4uc4x3HJby5XvR6TZV8bc7hS24ixq2DrMA6Pst4684x6c/2bLB+4QrUSGuR29/0ayNNcSHdBV9NaId20hteQn3f4i' +
            'pepGTcfkUQ7V2D4FUXe6NOoSScYPGcgzDEPAwdZulhmjMVXpCVZgIoyAKNZCXsDNstmXuibAvfnaB0OWdU27DrYpqBQtFNctvRI4OXhEDc+1BRyRBdBCLPGfq6WyMCWznfs8ML' +
            'R4a5vSRJwadP939T4f71H1YuHSVRsenGU4VhHCcetyGaf4nnQK8c1Ns8wPJjttqGfrb7TQc0CAwEAAaMjMCEwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwDQYJK' +
            'oZIhvcNAQELBQADggEBAIB0IICSjnEJNqMT9d5Z0Hp+yxNKi3bUlqR97aP+KowD9g6YOBjwYMb95ei7SRxXP0CHMji6R44C4Jj4PuEAdCLaYy3pQ3HfIqiqo8HDGpxQKT6nQ5Q' +
            'wtLxDZiVCKMaC8qcBL7ArCOHoxTXBdYWFkyGgm1SmyaZ2MfraR9rHHuzbsr9j331HgEeZwe8BDWB+uTOimREVCZOvw5BoTGLSME4+nzzcvQRa7pOZb+KA6RRGjKcvWiqvILMPG' +
            'V+rpy//ugsekpLtvZ34Uie8V0tOz2Rk9o/+2QG9iQogq9uoARtsnWRl1HSNlqWuV8oSo3IdkSkYWQ1gdiRZNupwHtEDXz0=-----END CERTIFICATE-----'
        ],
        subjects: [{
            commonName: 'Tenant 100 CA',
            organizationalUnit: organizationalUnitValue,
            organization: 'SwissPost',
            country: 'CH'
        }]
    },
    root: {
        pem: '-----BEGIN CERTIFICATE-----MIIDTzCCAjegAwIBAgIUGXP9nL43cyeV3q3NVYYSgyPl9+UwDQYJKoZIhvcNAQELBQAwTzELMAkGA1UEBhMCQ0gxEjAQBgNVBAoMCVN3aX' +
            'NzUG9zdDEWMBQGA1UECwwNT25saW5lIFZvdGluZzEUMBIGA1UEAwwLZGV2IFJvb3QgQ0EwHhcNMjAwMTAxMjMwMDAwWhcNMjQwMTAxMjMwMDAwWjBPMQswCQYDVQQGEwJDSDES' +
            'MBAGA1UECgwJU3dpc3NQb3N0MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRQwEgYDVQQDDAtkZXYgUm9vdCBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAPQBIH' +
            '/RK3zlyCq30fluv8Ls8q2MjDYSp3t1bq0TfbRvgUUGcwbVFfGrD0z8Q5F+febHEiVX5o4PWj+FzQ+gpluOYWwHZRkVrhG50NL8PfOVrWi8MTtQ35JejZDwU8LJmmLKFDrwyfGx' +
            'nFpDHTTdAV6R6EeOsdhnenHHkav4n9VGbigEkdptkg3+oP0qCvVn4fiQrp5U0rutgALqFe4nF9mfXMiu5CaZsD5H6qG3swOH0lnAhESz+28qunicS1J4C2mNeX3Tz4Pc9uMsRo' +
            '5UBf3Dp3aznzuldw+QYA6s8l5zimT0DqwT+5dWrsr0mRZjNmG9cSEQX4c7VW/g+VYj8bMCAwEAAaMjMCEwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZI' +
            'hvcNAQELBQADggEBACgWAvyFw2cxt6iRGmEHanbi7ZkVEFR8J19XCMbNrRs+0BwaJZ5cbSB6m6UgboD8pVDvMizS/imPtzzN38j1E5W6rXgpAzgJXc5qi5Pk7Djvga+QVuT0HK' +
            'jLYqhasYCWAD5GVglzYbe9qlNcHwxNwZbN4bSoq3M8yep/c495BCRU1d44piHlxR/F3kQEw1m/vPIRwRbUl5cJNZeLys8c9Tp4esjXF+F/kYf/E7tABtuikvwSoTSvgPHFK6Qx' +
            'XMbjEsRWlX8Z6pVZf8PDKdu/pakyk6By43dXzkRm9nlRHxgA/r+m0r4y9/QZOpRuiDEYGPEElW4VN3OaAaoJ/3FI+BE=-----END CERTIFICATE-----'
    }
};

const _timeCheckChain = {
    leaf: {
        pem: '-----BEGIN CERTIFICATE-----MIIDkzCCAnugAwIBAgIJAIooUOqgVoc/MA0GCSqGSIb3DQEBCwUAMGYxCzAJBgNVBAYTAkNIMQ0wCwYDVQQIDARCZXJuMQ0wCwYDVQQHDA' +
            'RCZXJuMRMwEQYDVQQKDApTd2lzcyBQb3N0MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMQwwCgYDVQQDDANDQTIwHhcNMjAwMzA5MTQ1NzE0WhcNMjEwMzA2MTQ1NzE0WjBuMQsw' +
            'CQYDVQQGEwJDSDENMAsGA1UECAwEQmVybjENMAsGA1UEBwwEQmVybjETMBEGA1UECgwKU3dpc3MgUG9zdDEWMBQGA1UECwwNT25saW5lIFZvdGluZzEUMBIGA1UEAwwLSW50ZW' +
            'dyYXRpb24wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCrd5mgYL/o/SE4HM/VenJEHUPy1vYFCqn/XJ99iVe3+FmSXQXKwqGd18L79/m9cFfhkiNphT7knYBlX4k6' +
            '7niCq/jA4dUUEqWMvpJP/R2hrtRmR6SCJpAh3FMHPYZ52oqj6VkTjorIOg+5qVSR4mJxihsLRrG/P0A5jHGXoiYlokYXiN5sRCihTbNWhbviYD32bQSey2MjtF3v183ZRZCJeV' +
            'm4zO1inku1OgP6WtrejqJRVbbMBrYjW4nEkulG0RUFoOcg9RvAt/MnfDZDUwE8Xu4eSwjwlIiuuLYiNVdtSU6+QU/9/oDiO/TzDFyq9imAHCR94YcJhUns+jiOpZCPAgMBAAGj' +
            'PDA6MAkGA1UdEwQCMAAwHQYDVR0OBBYEFNFnWVbD8RS9bG5Co9ky//qLWEPSMA4GA1UdDwEB/wQEAwIGwDANBgkqhkiG9w0BAQsFAAOCAQEAV+liapNQm4Frdrw0PaHNm5jWjo' +
            '4QAbfRKYdScxoqKYkWmFRjGpvuXgdK1Abzuy75YBbHVC84JXjCvdqXpnEI0hbz3yTtS04fGnA+IYH0I96nS6vCPGjaxwhmgSpH/5rkGhimsjvfv0JECuHBnKlLvXQnPTHl0yoZ' +
            'b9y3TGfvxOY3T9zd1Aq5oCUcJMfkjJUAw7NyeTS0MFPXNsAHYTI2C0hHGuAO3JTi4WjnWBKiVTHFQXIQEHFEM/67Dnh/hpWZaskUIS49Ip2nm4qYNnAfdyaBWoVzudWhw6siSE' +
            'PHoQ7IypugYNH+Tr8L78TMDqEfq2AEQKSs5++nrULexZGewg==-----END CERTIFICATE-----',
        keyType: 'Sign',
        subject: {
            commonName: 'Integration',
            organizationalUnit: organizationalUnitValue,
            organization: organizationValue,
            country: 'CH'
        },
        time: '2020-11-27\'T\'14:18:28Z'
    },
    intermediates: {
        pems: [
            '-----BEGIN CERTIFICATE-----MIIDizCCAnOgAwIBAgIJALdm+j7pvPyyMA0GCSqGSIb3DQEBCwUAMGYxCzAJBgNVBAYTAkNIMQ0wCwYDVQQIDARCZXJuMQ0wCwYDVQQHDAR' +
            'CZXJuMRMwEQYDVQQKDApTd2lzcyBQb3N0MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMQwwCgYDVQQDDANDQTEwHhcNMjAwMzA5MTQ1NzEzWhcNMjEwMzA3MTQ1NzEzWjBmMQswC' +
            'QYDVQQGEwJDSDENMAsGA1UECAwEQmVybjENMAsGA1UEBwwEQmVybjETMBEGA1UECgwKU3dpc3MgUG9zdDEWMBQGA1UECwwNT25saW5lIFZvdGluZzEMMAoGA1UEAwwDQ0EyMII' +
            'BIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAx0TScPK6/WvyXj3yOw0UQWD1+fhAZ1JB7EQ/In+2UrdN46b+wnRck25Vi4U98NOaTQT37EASl/n2J+6/oHKuxC1xq7xju' +
            '/etoNnUTAXAffcSeEXruL3mJgNbCkwt3h9LOIwh5hnCYXAXBqNvnVSKHgT/0qXhLHaX27SQj3XkY1HP/FfY762XTn2V83MoeyZAiwrVbKU1c1Qee/gk2ts75aNB35ohxYLxFhh' +
            'oPmQaCA0mqs4pF3wK9UImNyc3/r4wSJSZXU2FuzMp6COpaTBLo7Bt0P2pw/evEAkryeoVAcGs4jyI/kvGjbYd454h9LJIzGEw0H1cs5YLc/gCYtszvQIDAQABozwwOjAMBgNVH' +
            'RMEBTADAQH/MB0GA1UdDgQWBBQU7IZQgrVjGPfShutAlUY+N9O29zALBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQELBQADggEBAEIsXSKeXV7bYp9spJlUOuD0yShJcGPEyU/46gJ' +
            'oWcYnIMCsJb7FS1ugb6c7Zq5xyLS/FCDEkjFmt57KiIBmf7O8oGfldfaOoaA6LUze6BdIDg2BjJu2U1kz6ozvIsU78+tEF8fopIYVPqi8aCdD/ADzw+19eaobUNf+yETj4mpnO' +
            'UbF7edjI9khtamrKv1m+ys+xVho6Zm6AsznN4eagOU2BleXmelHxswgI4cE5gfNX2OnDII3vIdnkou3VN2dA9auWSbBWesJfW/d5LrOyzL0hBYvq0NpkU5PgzElxxSWIL+FwER' +
            'QQ9LNyIHhoZkJpH+gxKHejTHEX2d/y6d+ZHM=-----END CERTIFICATE-----'
        ],
        subjects: [{
            commonName: 'CA2',
            organizationalUnit: organizationalUnitValue,
            organization: organizationValue,
            country: 'CH'
        }]
    },
    root: {
        pem: '-----BEGIN CERTIFICATE-----MIIDlTCCAn2gAwIBAgIJAP3Q8BTxUlrhMA0GCSqGSIb3DQEBCwUAMHAxCzAJBgNVBAYTAkNIMQ0wCwYDVQQIDARCZXJuMQ0wCwYDVQQHDA' +
            'RCZXJuMRMwEQYDVQQKDApTd2lzcyBQb3N0MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRYwFAYDVQQDDA1JbnRlZ3JhdGlvbkNBMB4XDTIwMDMwOTE0NTcxMloXDTIxMDMwODE0' +
            'NTcxMlowZjELMAkGA1UEBhMCQ0gxDTALBgNVBAgMBEJlcm4xDTALBgNVBAcMBEJlcm4xEzARBgNVBAoMClN3aXNzIFBvc3QxFjAUBgNVBAsMDU9ubGluZSBWb3RpbmcxDDAKBg' +
            'NVBAMMA0NBMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANle6GCjwKwrFkyz1CBvB7Ely14g6AHT6KrBmrVyTQJr87GZV7LwJ44IexRFnM9qJgLpMCb3M8uHHrFB' +
            '6vquDpvTrRA12z7VT3Tl24LbvkVtHVbtKc9Nxiu/W4lBUugWQkSuSec6T/5URec9dpMhZc8X/7Z0ePMPyRLMcsh6TL9YOt57CF5GyqtDVEYmkO9YSiFUfKVzMVMWrQee/Un8Qy' +
            '92JWaOgKFciOekWxucPue2+Givdns0yyDX2oFBSkoWJ407POgFqiF1R3PNAtxzS5BtQBrRXvHwp6sCB8oHKkzS4am6RYxx7HN+DnFudTtWNj3YLFBIARWyIBQJuXrRxVsCAwEA' +
            'AaM8MDowDAYDVR0TBAUwAwEB/zAdBgNVHQ4EFgQUsSwLy6/IkiMZpt2Dig/bJFZVMM4wCwYDVR0PBAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQCoFMwTVJswcspWDYo8Y91qmL' +
            'e1UPsVzAD2XGWD9WwuhL36aZ7N9nBQrU6l7bxyHXFZAhIby6Krc0qYULtIA1OV8kDVDng3rqeoFSU1L8oilVw1miCPEVDTAdDZQrNl4tDoy8lLf3aL8adwomN7sKCDSo2wEbYn' +
            'aKDVxva+0xV0P6usM31TQyvsTpsWLKusoRTssCa8LtLW5DL3wZeyIesSFY3hcjwouzZWraA+SwPHIeeXa7ewIcvXss5S2bd4BTzWg9h2/2UkZ5vdcpeb4N2ySbztZACfQm7L9S' +
            'MlMcrxebThnLQTGKchoX3JxlQlScNCwspx+jZqy9EdkYlNpPyE-----END CERTIFICATE-----'
    }
};

const _intermediateCertificatePem = '-----BEGIN CERTIFICATE-----MIICGjCCAYOgAwIBAgIBATANBgkqhkiG9w0BAQUFADA9MREwDwYDVQQDDAh3aGl0ZSBjYTEOMAwGA1UECgwFc' +
    '2N5dGwxCzAJBgNVBAsMAnNlMQswCQYDVQQGEwJFUzAeFw0xNDA5MDUwOTM1MThaFw0yNDA4MjMwOTM1MThaMEcxGzAZBgNVBAMMEndoaXRlIGludGVybWVkaWF0ZTEOMAwGA1UECgwFc2N' +
    '5dGwxCzAJBgNVBAsMAnNlMQswCQYDVQQGEwJFUzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAwUmpmT/rNcxIP8Xulj0RcbdvaNGJNoEPMzEGztguB+mKsTPgJIlrKvdskY5NIPd1+' +
    'hzS+HjvW11uwyT2FnmyfKBqND4EH5BqU8Ph4bAyrT950DPcOGE4vG0fLO0jTD3h0n4d4yugWmrND2sScFZQsGLbgOYX4QM8vV+owoKNRxMCAwEAAaMgMB4wDwYDVR0TBAgwBgEB/wIBADA' +
    'LBgNVHQ8EBAMCAQYwDQYJKoZIhvcNAQEFBQADgYEAF0B5LQDo3uRs9dSkxp39ER64XIa6XYV0zdbq+fqHg8Qcoggpox7vwmc6YoCNQpfY2zLiIUMbb6VMP4pkp7jZMju9TBKPOjMFrQKom' +
    '0cowT/gYdokgnsk8yUYOdo46GJR43PjFLTiQk5JDawWaECbhcgyLf59AalX9z9dbY0AwqI=-----END CERTIFICATE-----';

const _tooEarlyStartLeafCertificatePem = '-----BEGIN CERTIFICATE-----MIIDszCCApugAwIBAgIJAJZdx2FxigSQMA0GCSqGSIb3DQEBCwUAME8xCzAJBgNVBAYTAkVTMRMwEQYD' +
    'VQQIDApUZXN0LVN0YXRlMQ4wDAYDVQQKDAVTY3l0bDELMAkGA1UECwwCUUExDjAMBgNVBAMMBVNlY1FBMB4XDTE0MTAyMzE1MzYxOVoXDTE1MTAyMzE1MzYxOVowVjELMAkGA1UEBhMCRV' +
    'MxEzARBgNVBAgMClRlc3QtU3RhdGUxDjAMBgNVBAoMBVNjeXRsMQswCQYDVQQLDAJRQTEVMBMGA1UEAwwMU2VjRW5kRW50aXR5MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA' +
    'wFcomvOSzm2HkrOodZ93pyCxkpW7CVUMYC9zusJ7SdHEhcMib+44+rcfjnxPLmSO9xZTRYQo1DXwktEqkMutNih+YB51MPddKFylvMP9cUKkT1sIJzoIhRXC+xOtPAILfYx2XsNJCuRrEK' +
    'M27JrC2xIVCBIiJuNz6UyFe7ZvtKmt+n40Nsi4Mcx0DhGRuZ3uhuA5OEotbG6VZZVhAovYFh09x+GyT1Dpug8NRD+mM+g1qJkhNZ+MST/rtw5DB++S+VwVLyVfxrTNycUafWWHAIMsjRlp' +
    '6qlgfK32yY6N0haZXvP4RcXYSIWZK5ON+6r5lIY/FBR2m7eiGxlxdPkr7QIDAQABo4GKMIGHMAkGA1UdEwQCMAAwCwYDVR0PBAQDAgbAMC0GCWCGSAGG+EIBDQQgFh5TY3l0bCBRQSBHZW' +
    '5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYEFAOC8miWiUpLGw0TQzpjMUhAyYtvMB8GA1UdIwQYMBaAFDK0r97umjjsFvU6NAkgEvKeaIMVMA0GCSqGSIb3DQEBCwUAA4IBAQAQH64q' +
    '51KivF3lUd78nOvHzuv04pUGihcVnYm3MTnXiODpwj4f6s5sZnAUWB+BOCJ1btaJvNAWUpnOH36nLr+qKV5mIxOyiKQWxY0tunySctsOpSscW/OfatvOUdkjshC7QuNq0fhjYh8SGO9Bk4' +
    'ePO0uaIEWWywaH0wHVJPFaETklFXg3TZr61T5scrDEfs+lk+fQjkJ0YiS4e5VKfS7uVPLtDLgT9sx3l1QlVV2xZ8jF7hPpTy0JX8adp0NEQd+kfFFQuuSnY0QfLKtut9LHIjrl2q2sLfi0' +
    'ibJ+uokCY85XZeyPzovIaSu3b2941E3dxL6UbDCEJyjlffybakyN-----END CERTIFICATE-----';
const _tooEarlyStartIntermediateCertificatePem = '-----BEGIN CERTIFICATE-----MIIDsDCCApigAwIBAgIJAJZdx2FxigSPMA0GCSqGSIb3DQEBCwUAMFAxCzAJBgNVBAYTAkVT' +
    'MRMwEQYDVQQIDApUZXN0LVN0YXRlMQ4wDAYDVQQKDAVTY3l0bDELMAkGA1UECwwCUUExDzANBgNVBAMMBlNlY0RldjAeFw0xNDEwMjMxNTQyMDJaFw0xNTEwMjMxNTQyMDJaME8xCzAJBg' +
    'NVBAYTAkVTMRMwEQYDVQQIDApUZXN0LVN0YXRlMQ4wDAYDVQQKDAVTY3l0bDELMAkGA1UECwwCUUExDjAMBgNVBAMMBVNlY1FBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA' +
    'lvRn5OjWMaQfWCknqnsTzupcpx6zPQCLDe9DbizYRBghJcr4UlxI1uLsCHHzEMXCta5rJ+PSQ+TZ71PP7Ep3JyLAYObhVM8ltQyAKm8fgZa/FhCer46O3CohiD0Cdjg3r+qGOB+OKcGigX' +
    'nNSoEsBlJ7HTzi5egiSg6D1Mx4uNw35F15ahTDxzIT/HN/Dnl3fEGzuJFebhSxj3u/+EEFJQrYvl62bUzzpXKlg/1C8BUYUXcPqhuUwZhyxE/DKyunA489XwpvN11y9NgygKb45qFAi9Wm' +
    'Xil7q5JkW8iBqhXWbAoGiYf8nG5OanQBHERmAYAzJBI3QCK+6WiDXCRT/wIDAQABo4GNMIGKMAwGA1UdEwQFMAMBAf8wLQYJYIZIAYb4QgENBCAWHlNjeXRsIFFBIEdlbmVyYXRlZCBDZX' +
    'J0aWZpY2F0ZTAdBgNVHQ4EFgQUMrSv3u6aOOwW9To0CSAS8p5ogxUwHwYDVR0jBBgwFoAUqHgXrevwre4z3tVx05iDe/KVIfMwCwYDVR0PBAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQA/' +
    'Jxb3/gHb6mGyTEti+g3Zs5nrX64dtbFPWEgpyKu2jZ0ZoyllAUThIBjwvPw0hI3LqTOG06CCrgrPmndakasc5bJTEokshoI/IxTp/itME3ZBx/TUpu6v3baHYQ5C3HcKGhJxScikeO/Gh1' +
    '7A71Q4wA1PbLQ8jHCgfAFIODbXNRcYj8BVpv+7rXY/px4ai2UEf6ZAQZ3H6OyN9PGRA4EABJEiBsRNJvzcUcfq1JHF02FNlhFn8ZZXxOzpSc9bYX8trJGgB6GXDj2BPnFzsz1+ksNW6CaL' +
    'U9AuuS2Bj/NseF2WVRwi3nCIvAPbpgAhivIHZOxY/eNRjJijo7+OKG40-----END CERTIFICATE-----';
const _tooLateStartRootCertificatePem = '-----BEGIN CERTIFICATE-----MIIDgDCCAmigAwIBAgIJAJZdx2FxigSOMA0GCSqGSIb3DQEBCwUAMFAxCzAJBgNVBAYTAkVTMRMwEQYDV' +
    'QQIDApUZXN0LVN0YXRlMQ4wDAYDVQQKDAVTY3l0bDELMAkGA1UECwwCUUExDzANBgNVBAMMBlNlY0RldjAeFw0xNDEwMjMxNTQ0MzdaFw0yNDEwMjAxNTQ0MzdaMFAxCzAJBgNVBAYTAkV' +
    'TMRMwEQYDVQQIDApUZXN0LVN0YXRlMQ4wDAYDVQQKDAVTY3l0bDELMAkGA1UECwwCUUExDzANBgNVBAMMBlNlY0RldjCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAL4xWFqJp' +
    'uQtcbywy9aYeH1X8cvlueJNPG/ixZsZjXY5KUdq7bjBLDlns49UWw96uxbU+JEL5aKMibtA8sJP9ZhDKY15ps9+wtceFnkT0g0IZ1shupQPEOKH2UfIS5/2teL7vjCplX3mDX8YOgzLqlq' +
    'FrVtifejH2ETRzpDFc7K/P3KtSQqGKYUEZw5olCdLcg9Fp+I9QVkNGGr6Ds16BGWnb6Pgm5NOegdoOJLF6vZkhvyTYz9/Tg2men/HJLCVCXhbYwfUcu0ezj8w5G6zCdxVrqSTvjHVzp0ks' +
    'kVGRZjlrEOFLvswOGTjAon/U4iuBtqR+pLZg4Law+ETujPJPs0CAwEAAaNdMFswHQYDVR0OBBYEFKh4F63r8K3uM97VcdOYg3vylSHzMB8GA1UdIwQYMBaAFKh4F63r8K3uM97VcdOYg3v' +
    'ylSHzMAwGA1UdEwQFMAMBAf8wCwYDVR0PBAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQAq2bLqJ+cLtnivXrNP5j9YL031zAO6xQDehxqasKOb7e6pdB8NKAOwa7smXCRsJxphT2vTv+Vse' +
    'cD/b8D3irEV000pbemeVmyrnOyaJxTCjBRvhjIbOmaBZ5k0KfxDubT8xx9H8Im+jxX0tZU5IQuTBG+Izd+5Dm5kxAhh7JDzclUYfUMDYrv43/7zm5wPkRqqK5SY7J4CPMiW3Uzxpc+uPCw' +
    'ydMSTYbDTdNN5nf0eDGepm2h/iewfickgkCFoNe6b3nxlQiK8EL9FUSKeq+1AWVnT3riC8C/CXTZwKo3ULXkKe3Jl3mvVYjkz/O/3NzeTfFdktuNaSh28Wrzlb9h4-----END CERTIFIC' +
    'ATE-----';

let _certificateValidator;

function clone(object) {
    return JSON.parse(JSON.stringify(object));
}

describe('The certificate module should be able to ...', function () {

    beforeEach(function () {
        _certificateValidator = certificate.newService().newValidator();
    });

    describe('create a certificate service that should be able to ..', function () {
        describe('create a CertificateValidator object that should be able to', function () {
            it('successfully validate a valid certificate chain', function () {
                const validations = _certificateValidator.validateChain(_validChain);
                expect(validations).to.deep.equal([[], []]);
            });

            it('successfully validate a valid certificate chain, generated with Java',
                function () {
                    const validations = _certificateValidator.validateChain(_validChainFromJava);
                    expect(validations).to.deep.equal([[], []]);
                });

            it('flatten a two-dimensional array of certificate chain failed validations',
                function () {
                    const validationFailed = [['SIGNATURE', 'NOT_AFTER'], ['NOT_BEFORE']];
                    const validationFailedFlattened = ['signature_0', 'not_after_0', 'not_before_1'];

                    expect(_certificateValidator.flattenFailedValidations(validationFailed)).to.deep.equal(validationFailedFlattened);
                });

            it('unsuccessfully validate a certificate chain for which the leaf certificate subject common name is invalid',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.leaf.subject.commonName = 'Invalid common name';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('subject_0');
                });

            it('unsuccessfully validate a certificate chain for which the leaf certificate subject organizational unit is invalid',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.leaf.subject.organizationalUnit = 'Invalid organizational unit';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('subject_0');
                });

            it('unsuccessfully validate a certificate chain for which the leaf certificate subject organization is invalid',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.leaf.subject.organization = 'Invalid organization';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('subject_0');
                });

            it('unsuccessfully validate a certificate chain for which the leaf certificate country is invalid',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.leaf.subject.country = 'Invalid country';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('subject_0');
                });

            it('unsuccessfully validate a certificate chain for which an intermediate certificate subject common name is invalid',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.intermediates.subjects[0].commonName = 'Invalid common name';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)) .to.include('subject_1');
                });

            it('unsuccessfully validate a certificate chain for which an intermediate certificate subject organizational unit is invalid',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.intermediates.subjects[0].organizationalUnit = 'Invalid organizational unit';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('subject_1');
                });

            it('unsuccessfully validate a certificate chain for which an intermediate certificate subject organization is invalid',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.intermediates.subjects[0].organization = 'Invalid organization';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('subject_1');
                });

            it('unsuccessfully validate a certificate chain for which an intermediate certificate subject country is invalid',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.intermediates.subjects[0].country = 'Invalid country';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('subject_1');
                });

            it('unsuccessfully validate a certificate chain for which a leaf certificate with a Sign key type is validated as being an CA' +
                ' key type',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.leaf.keyType = 'CA';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('key_type_0');
                });

            it('unsuccessfully validate a certificate chain for which a leaf certificate with a Sign key type is validated as being an' +
                ' Encryption key type',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.leaf.keyType = 'Encryption';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('key_type_0');
                });

            it('unsuccessfully validate a certificate chain for which the leaf certificate signature cannot be verified by the next' +
                ' certificate in the chain',
                function () {
                    const invalidChain = clone(_validChain);
                    invalidChain.leaf.pem = _intermediateCertificatePem;

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('signature_0');
                });

            // leaf certificate of timeCheckChain_ does not edn after intermediate certificate
            // it('unsuccessfully validate a certificate chain for which the leaf certificate ending time of validity is after that of an' +
            //     ' intermediate certificate',
            //     function () {
            //         var validations =certificateValidator_.validateChain(timeCheckChain_);
            //         expect(certificateValidator_.flattenFailedValidations(validations)).to.deep.equal(['not_after_0']);
            //     });

            it('unsuccessfully validate a certificate chain for which the leaf and intermediate certificate starting times of validity' +
                ' are earlier than that of the root certificate',
                function () {
                    const invalidChain = clone(_timeCheckChain);
                    invalidChain.leaf.pem = _tooEarlyStartLeafCertificatePem;
                    invalidChain.intermediates.pems[0] = _tooEarlyStartIntermediateCertificatePem;
                    invalidChain.root.pem = _tooLateStartRootCertificatePem;

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('not_before_0');
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.include('not_before_1');
                });

            it('successfully validate a certificate chain time reference if the time reference is set to the leaf certificate starting' +
                ' time of validity',
                function () {
                    const chain = clone(_timeCheckChain);
                    chain.leaf.time = '2020-03-09\'T\'14:57:14Z';

                    const validations = _certificateValidator.validateChain(chain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.not.contain('time_0');
                });

            it('successfully validate a certificate chain time reference if the time reference is set to the leaf certificate ending time' +
                ' of validity',
                function () {
                    const chain = clone(_timeCheckChain);
                    chain.leaf.time = '2021-03-06\'T\'14:57:14Z';

                    const validations = _certificateValidator.validateChain(chain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.not.contain('time_0');
                });

            it('unsuccessfully validate a certificate chain time reference if the time reference is set to 1 second before the leaf' +
                ' certificate starting time of validity',
                function () {
                    const invalidChain = clone(_timeCheckChain);
                    invalidChain.leaf.time = '2003-09-14\'T\'14:57:09Z';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.contain('time_0');
                });

            it('unsuccessfully validate a certificate chain time reference if the time reference is set to 1 second after the leaf' +
                ' certificate ending time of validity',
                function () {
                    const invalidChain = clone(_timeCheckChain);
                    invalidChain.leaf.time = '2103-08-14\'T\'14:57:15Z';

                    const validations = _certificateValidator.validateChain(invalidChain);
                    expect(_certificateValidator.flattenFailedValidations(validations)).to.contain('time_0');
                });
        });
    });
});
