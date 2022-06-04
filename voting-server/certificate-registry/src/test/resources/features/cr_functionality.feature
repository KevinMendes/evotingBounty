Feature: Certificate Registry functionality

  Scenario: configure platform
    Given that the service is up and running, but not configured
    When upon configuring the plaform
    Then the service replies with successfull healthcheck

  Scenario: Check health
    Given that the service is up and running
    When health check is performed
    Then the system returns 200

  Scenario: Upload a tenant certificate
    Given that the service is up and running
    Given there is no tenant configured with id 100
    When uploading tenant certificate for tenant with id 100
    Then the certificate for tenant with id 100 can be retrieved

  Scenario: Saves a certificate in the repository and then retrieve
    Given that the service is up and running
    When saving a certificate with name test01 in the repository
    Then certificate with name test01 can be saved
    Given that the certificate with name test01 is available in the repository
    When downloading a certificate with name test01
    Then the certificate with name can test01 be retrieved

  Scenario: Saves a certificate in the repository and then retrieve so that system returns an error
    Given that the service is up and running
    When saving a certificate with name test01 in the repository
    Then certificate with name test01 can be saved
    Given that the certificate with name test01 is available in the repository
    When downloading a certificate with name test02
    Then system returns an error on retrieving a certificate

  Scenario: Saves a certificate in the repository and then retrieve a tenant certificate from the repository so that system returns an error
    Given that the service is up and running
    When saving a certificate with name test02 in the repository
    Then certificate with name test02 can be saved
    Given that the certificate with name test02 is available in the repository
    When downloading the certificate for tenant with id 101
    Then system returns an error on retrieving a tenant 101 certificate

  Scenario: Saves a certificate in the repository and then retrieve a tenant certificate and election id from the repository so that system returns an error
    Given that the service is up and running
    When saving a certificate with name test03 in the repository
    Then certificate with name test03 can be saved
    Given that the certificate with name test03 is available in the repository
    When downloading the certificate for tenant 100 and election id 123456789 with name test03
    Then system returns an error on retrieving the certificate for tenant 100 and election id 123456789 with name test03

  Scenario: Saves a tenant and certificate name as parameter in the repository and then retrieve
    Given that the service is up and running
    When saving a tenant 100 and certificate with name test04 in the repository
    Then tenant 100 and certificate with name test04 can be saved
    Given that tenant 100 and certificate with name test04 is available in the repository
    When downloading a tenant 100 and certificate test04 with name
    Then the tenant 100 and certificate with name test04 can be retrieved

  Scenario: Saves a tenant and certificate name as parameter in the repository and then retrieve a tenant, election id and certificate as parameter so that system returns an error
    Given that the service is up and running
    When saving a tenant 100 and certificate with name test05 in the repository
    Then tenant 100 and certificate with name test05 can be saved
    Given that tenant 100 and certificate with name test05 is available in the repository
    When downloading the certificate for tenant 100 and election id 123456789 with name test05
    Then system returns an error on retrieving the certificate for tenant 100 and election id 123456789 with name test05

  Scenario: Saves a tenant, election id and certificate name as parameter in the repository and then retrieve
    Given that the service is up and running
    When saving a tenant 100, election id 123456789 and certificate with name test06 in the repository
    Then tenant 100, election id 123456789 and certificate with name test06 can be saved
    Given that tenant 100, election id 123456789 and certificate with name test06 are available in the repository
    When downloading the certificate for tenant 100 and election id 123456789 with name test06
    Then the certificate for tenant 100 election 123456789 and with name test06 can be retrieved
