Insert into AUTHENTICATION.PLATFORM_CERTIFICATE (ID,PLATFORM_NAME,CERTIFICATE_NAME,CERTIFICATE_CONTENT) values (PLATFORM_CERTIFICATE_SEQ.NEXTVAL,'SwissPost','dev Root CA',TO_CLOB(q'[-----BEGIN CERTIFICATE-----
MIIDTzCCAjegAwIBAgIUGXP9nL43cyeV3q3NVYYSgyPl9+UwDQYJKoZIhvcNAQEL
BQAwTzELMAkGA1UEBhMCQ0gxEjAQBgNVBAoMCVN3aXNzUG9zdDEWMBQGA1UECwwN
T25saW5lIFZvdGluZzEUMBIGA1UEAwwLZGV2IFJvb3QgQ0EwHhcNMjAwMTAxMjMw
MDAwWhcNMjQwMTAxMjMwMDAwWjBPMQswCQYDVQQGEwJDSDESMBAGA1UECgwJU3dp
c3NQb3N0MRYwFAYDVQQLDA1PbmxpbmUgVm90aW5nMRQwEgYDVQQDDAtkZXYgUm9v
dCBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAPQBIH/RK3zlyCq3
0fluv8Ls8q2MjDYSp3t1bq0TfbRvgUUGcwbVFfGrD0z8Q5F+febHEiVX5o4PWj+F
zQ+gpluOYWwHZRkVr]')
|| TO_CLOB(q'[hG50NL8PfOVrWi8MTtQ35JejZDwU8LJmmLKFDrwyfGxnFpD
HTTdAV6R6EeOsdhnenHHkav4n9VGbigEkdptkg3+oP0qCvVn4fiQrp5U0rutgALq
Fe4nF9mfXMiu5CaZsD5H6qG3swOH0lnAhESz+28qunicS1J4C2mNeX3Tz4Pc9uMs
Ro5UBf3Dp3aznzuldw+QYA6s8l5zimT0DqwT+5dWrsr0mRZjNmG9cSEQX4c7VW/g
+VYj8bMCAwEAAaMjMCEwDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8w
DQYJKoZIhvcNAQELBQADggEBACgWAvyFw2cxt6iRGmEHanbi7ZkVEFR8J19XCMbN
rRs+0BwaJZ5cbSB6m6UgboD8pVDvMizS/imPtzzN38j1E5W6rXgpAzgJXc5qi5Pk
7Djvga+QVuT0HKjLYqhasYCWAD5GVglzYbe9qlNcHwxNwZbN4bSoq3M8yep/c4]')
|| TO_CLOB(q'[95
BCRU1d44piHlxR/F3kQEw1m/vPIRwRbUl5cJNZeLys8c9Tp4esjXF+F/kYf/E7tA
BtuikvwSoTSvgPHFK6QxXMbjEsRWlX8Z6pVZf8PDKdu/pakyk6By43dXzkRm9nlR
HxgA/r+m0r4y9/QZOpRuiDEYGPEElW4VN3OaAaoJ/3FI+BE=
-----END CERTIFICATE-----
]'));

Insert into AUTHENTICATION.TENANT_KEYSTORE (ID,PLATFORM_NAME,TENANT_ID,KEY_TYPE,KEYSTORE_CONTENT) values (TENANT_KEYSTORE_SEQ.NEXTVAL,null,'100','ENCRYPT',TO_CLOB(q'[UEsDBBQACAgIABtvmFAAAAAAAAAAAAAAAAAEAAAAU0FMVAEgAN//8Ze9LMLwoddFbNXI5edt2Awmg1soRVFvpQN75AwYsaBQSwcI/kmx1SUAAAAgAAAAUEsDBBQACAgIABtvmFAAAAAAAAAAAAAAAAAFAAAAU1RPUkVVlGc0G4AahhMiZsyYVbUlZlCriqaqpWqVolaMoirUij0idlEjqFV7zxpF7drUrFXUXjVqV9So23vPvefe++M77/me9zvfzweBowYSAYkROEgjmFwwVDWUAAGSArNwkFIQDlL4F+cicCQe/1eRvAThSF78xeZ/xwRM+e+KigJIlIUDXSBwIAICDqb4D/77/AYISue4XVNaCtjzcfRzXg9QYvSzrb5LRAzo1AbhQH2E40FS3qmY0Y8sLhEDocK9necH8wV4GIJBQOxzc4gKI4obP0ah8phYr7lIpP9GjFZmdX9kuv+ylxoHfTfZotmkPHwUXlSh023D2lXcOx9p]')
|| TO_CLOB(q'[k5Tt/PSBu/IDBUf6Cho4x9H4/A36P6hO5Srp1JRs2/UaYBql77vHWQeM2Mv8Bu8Bz6S6rgqFqKww20bZBBGd1mFKBaD72hnyXeoKMC/p+bO3azvkXT+6Gpfhm8LmwTEZLm0vHernMpXQRt17MgdNgWemvi8KgqnU2kgra2fZ3vuLTDO3MMQ3YbjcQIOb840O/NFb+g/N+S9+BuPXtRN3iOTThaOtawb47Q9Oiz/Xc0RXbQy9WrPokIVa7+WMK1nVvkgWeReBZ/Nelj4CUPnvrdL9zglHaTYG6Co3CJyhRKTwcXdEzF7+CncbGeEfUDzOpi+jQltlP+ELxjq4c7EJhwS+SoMdwCNs6FwVyH5yR1aXxvRMF/Izs3niZryRf6hlCiui13U3RkVJiFk89plneSDMijJaku/UHnSYX17yOO8vcYVll1tIwPHc5tYPpABJhu2tBkLPOBrQCWcihditlh2kNMlVusSH56n6b54ErqqjLwZYelxV]')
|| TO_CLOB(q'[wWHzQ5NGBEGhTvZOak0nDm95+YfueRxsVOFxlj+0w5i7kECu6arGUrwXbfoaVEGWNK/9oi0T0X+NqWJi2XPLroHAI2Gc93Tu8gQNTumPBClpqXzDl4GEfnRvcKf2I828P0yFK++/bbm/aZoDq4/puuBAy78poMTkXVztciuRV647Uc8xA33KZoqXRH3JsYk6ys1fufDHFGb0gGcMnnRIUhfEyTSn5Wa+LK24MeOXcoPFE+3YvsKyb7LBtBIRjNyss0xLTfBv/KPDW/0z61obcTyfCCQSfPwuVF56zd9/i4R988AW334FQXZ94l+6AKlq3qYOl+Dv94FH1C49iOV9VEn0I8nIfmj8N5Br14lst6No5PDjGB9FjJ+m8OFpacwla9wtbtVThutVWxKVcFt6GjZdvmEvhCki6nXPZ61YlKijgZLCR/ZyD3uNzg3OPkb8HhWvSSfD17rzTH/YWQmL54Yn+y0mkBDrRQJJJJ449D7GNND5atGl]')
|| TO_CLOB(q'[e9/c9WusHVLxRVZy+eSciQS1ErW86NyYQ34DDcNjJ1A0kEoThLSAD+sbZvU28Nu8L5Icf9hAfBMtBF81dCEt1NVGz6I5CuoWno2GXFjTm1Fdfk9tVbirsYejwGZAfq85ax9fUQTK8HTuMsOOXC/kBG5eY/D5QZ+8y8kNfd9t/n7Nb8QP9VqX8t6NgIt7ZVM0g8MrRu3jSgp31s0kv+0Jek7YYVbUP3uxSpZkrg2dO+JfX5EEOAdcHD0s6WYJ08o/tLPqnf9c8ITgXs6DgnOhwDY3sp8auLS6gUMe9pYwmqsrmj0vsmaxGNoNGLEKpwFz9ktlYmdmKHPc8WJ3W+vfbKp3hY0qbOdzp4uechilVfyeK4oXi8PCYhXnVLvvH7mpTFcL9Nqexv+UXfSP7yakQbaJ3i8lCJ4cbOhhPHTHTSeLHrW90fvyHVEZPp3DOaLiOYvfk1UfBfq5kiVFv0cpxDL3LX4IOgqOEQ2Tm0t7i9CXb5xXqjZG]')
|| TO_CLOB(q'[Bbf14vtR2W3RChPziJ63eadG9uKaf8X3H1WSQ8XZOdgA1gBHgBXABeAFcAK4AewAr//u9n+pF4Lrv6eM4lAQvZ6dgzWnuJSsjIyE3G3x29ISMggcGe9/1QvOwpEx/kV0REDA36D4Hyn/n2/B//St2/vrANTbtEyhmwzu1c41lvv4Oc9/+RaLIx3VGydlQ61PaYWz8wHuzXSsVS2LyK5+8QdXPyBs2iAan+UmNSrkFHzazGX6tQKqYzmZoWLD7IZ5sl6CSf54ahdSkL+dpnr7wqhcLok27JZW628XPaUBWaj7JpXtuHm/9nKm8c1JA63Ooh1eaXIqyBO5CDKmiec6JWpiJO2hAtU7kOtQ5dDM0UUZoQC4YwxFvBn2LDt+7WbDtEn955XWQb7qAkRUJlOLPKwl8ivM2tf2Mftg9bb3oBZVbtQfSeib+g0djL40t/yg4QqZ82zTMSUE7qGgb6yChZ3hvC0qp+XEn2/DFj5b9RkMCh1U9TOh]')
|| TO_CLOB(q'[oUHob6RpoVgkXyk3AHQS7KrXkUqz2a5J3YU1PVGaviwgb/rSgwmNm2tkB1AIfZjcrJVJMW79xkSs6DruclLas23xXWId9As7JXSxeit9QHObiCR6UobZqFp8MbSUqTRWt5Z6vzlArAnt3aixt1e2L0h4Nvn4OorUX50vxefpr6kJLZ08danhdnIL0c4nnK06fZUc/XMyEOcJMF3xJvAyUNtp3HYTPqAVcGjZc7hgpf/72acNDVgjyc0Kd0hKoZN9RWzdpJWkqmU0Yb91xSTLQ4LRvZ7RIn3FTHHSPzbe+tR6QZ/hIZJlYtS0sl6/LUxhZdk0huYeWymg/sWTR4NfMR+T5171Y2hgVoEYzpwQNgsfpyaPLoTeuftTVW7yy3ETybayxWZNaVprZI44k12evkfF6eGlCMf7Up448wlryoGSVmtxwc6grnddybiVMpvJdmII1rq7eFdYMa2Cxz374MUVleN+ecmnixd1eLF8v4m3YFtDYkG0]')
|| TO_CLOB(q'[rdJxTTLZ6Sc+EtYpyORn8iv9/vTURuYWFRANTnzKaokVxmkbpktt1ZqYyi9X+dSXZOUMpC1pyPm8fB1ps90Zv5bdqHq9pOyY3MF+Pi3ne03Y1cZAx44Xaoh9VQK17J7RvKSkwY6yvUxN3NlrzEC2ZIczDRtQ6rc7GXD6fEurtOu7xTy96yAaNEypRC/7NDwsci9Fv0fu9oDuMmxtPmRVzfTh5XiohjCRP5HEHMMrloLuP5RezLn0/KIxT9QfA4HYVhdDGgvOMjef+Fn3HcaGK9+l/Z9DtT+ht3s8Uvyt1m2GDr+ix/3LTJzUiHJJy3sd8DGIJJU3tkNqQ7CjaAN3ovurXMcJqJcQntbbpm3rhyaUiThu3E0E7ZmpVkeevf+DYr7iGcEdbqOVmbvZNTkjIifn/NUR0WDdPn+/IvEzuQjQqbrrup5xd7LxB1GObXRlf0i83WVFKTpZwyC9hS7ajKAcva5d88T+J+UBqRU5i1KSpH/454Hx]')
|| TO_CLOB(q'[WXgcK+/R9JxhKr3l1tIqi6XA3R6jsuUJUW058+cZihkBogomKp3ldX0HUXIN58Q3LGO4VOPCTWUYRBNOFJs5YcqCCX1hpm/RnaJ6itVQfidSmptfNcZVzUeQkXtJCQ6m/u0ZbjWaCz9AcNp2yw/4MfatHgaBxpkvXwb9GsRlE8kbVGaZXXOa3eDoI8dNVbXmbdvsYhMZzfEs3eXcuD/qxqvB3zeTki2c6M2luX71l1sro8pDYh9xZJMPh+vk5lkfytfs5m6sOZ44zD0lSAF01GxdOwBcqhTdYwGDI0IiQrdzhobrTsiCEGupUNUPQVBH7M/a7NC9kCIAIep7dSHmTqDNHYH346UVNuRJkpxMCZRK1tJimrfGXQfX7dKsr9UYiacrKftM0kVaqlwfY4SErTK18SZ7oK502smxMlYFpoc9ucb0VcLNkyxEkaZcdI4KsAp2RtfjB4WB/lennW61cb5FHvqs33m80mhNXhNJbfltr3HyhZrp]')
|| TO_CLOB(q'[9VcwnBxIUfK29z/jRgmIIOl+mDW8mWBA4++5d9hh/L0as5t36jH3cRv0RPPJSe07Wse9YM4JaqGqtZ20+SPkjO40b+WNqdw8/6+VIotyWRpnGfdU+JlWrnCYmCz1hiQqAyH9LfOSvAgj0+dmsdjmgAA5e3By5a3Rsy+zmWc8ot+huiOuCCOXGZQt8tZQWq39nB+9zpvgi4XFe2xkyyvqUFR+NJo79CqNz/HTgIkzH4+KhcYpoTlII6EOwuYT84pB/4pj4s6UIKHLTeoOP51wa+aaE5K0PuvUPPR4JcuX9VjerV3NwrJXhuTFKTPto8iC4biuCx0C9OY+HY+5+95gYh1MAwpG5J5t6Tz4c0wnZqsqDh+2EfMfE7RoM5CKtABG8qMyEqOKntJ41nBLWY0d3xKTKWywr6ZhztBt3TKKocxPbWJ9ejfKFRH0weWaGGuwFJc89j740X25e6adXkmZGDNYVM7jgUZRIGbSRCqMmOwuKINOCrYA]')
|| TO_CLOB(q'[1w0wvOHq4yT/0eokjMH9DiQldkeOPNdHB6yMthqo8onk7VENzsrX2aKa+CCO0oBylc0eG6cQTp+E+94Ghd/YocsPCtPSyJ4417Y80AqvoN2gLpaWIlsGCRAfBHTkQobcUgElyVrEyoSHcBPY5DuXUhGaRIv4fgRSlU36d+2FgYdqfuEhr83CvGBIA++4L1tSQrwlLe7VF8Uf4i2vczyanK+bWpskmhOZO+oP9F5tzMenv2mRFT0iOGjUSQ7/UQn9tfKauujXNPZIb7LEFzTUdfnD2ntV7d5HAoqX1z1L7qxyccR7VJx6accvst0wuXhN3ctlI1P5Dvom7Cd0TVpnP01H207iHKGI4EKQg0mEqImJ2EgAIKiHTwpooYyZEC2qpz/3ldlv5FJFDQS1/KGTYgB26yyQuTqfEN45LyypMiQiBoZm/QNQSwcINlTKPPsNAAAFDgAAUEsBAhQAFAAICAgAG2+YUP5JsdUlAAAAIAAAAAQAAAAA]')
|| TO_CLOB(q'[AAAAAAAAAAAAAAAAAFNBTFRQSwECFAAUAAgICAAbb5hQNlTKPPsNAAAFDgAABQAAAAAAAAAAAAAAAABXAAAAU1RPUkVQSwUGAAAAAAIAAgBlAAAAhQ4AAAAA]'));