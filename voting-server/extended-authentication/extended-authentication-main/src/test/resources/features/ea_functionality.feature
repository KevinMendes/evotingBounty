Feature: Extended authentication functionality

  Scenario: upload extended authentication credentials and execute a single login using a valid authId
    Given a predefined dataset for election with id ed7e622bf1804a389162e0d18658a11a
    When searching for authId, that is the following string: f9949cfa3b1f5785cc48b9f879ebdec5
    Then the result should be the encoded start voting key p5CA4ZqAWx4D/WCHCFLYLrwt1jS9v5pcApt8pSVEVUgXEsv71Gmu1gTMGHkfuSS/1PAtkoCsj4w=

  Scenario: upload extended authentication credentials and execute a single login using an invalid authId
    Given a predefined dataset for election with id ed7e622bf1804a389162e0d18658a11a
    When searching for authId, that is the following string: fffffffffffffffffffffff
    Then there is an error for wrong SVK

  Scenario: upload extended authentication credentials and execute a single login using a valid authId and extra parameter
    Given a predefined dataset for election with extra parameter 20111980 and id ed7e622bf1804a389162e0d18658a11a
    When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 20111980
    Then the result should be the encoded start voting key p5CA4ZqAWx4D/WCHCFLYLrwt1jS9v5pcApt8pSVEVUgXEsv71Gmu1gTMGHkfuSS/1PAtkoCsj4w=

  Scenario: upload extended authentication credentials and execute a single login using a valid authId and invalid extra parameter
    Given a predefined dataset for election with extra parameter 20111980 and id ed7e622bf1804a389162e0d18658a11a
    When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
    Then there is an error for wrong extra parameter, 4 attempts left

  Scenario: Exceed number of attempts for a svk
    Given a predefined dataset for election with extra parameter 20111980 and id ed7e622bf1804a389162e0d18658a11a
    When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
    Then there is an error for wrong extra parameter, 4 attempts left
    When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
    Then there is an error for wrong extra parameter, 3 attempts left
    When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
    Then there is an error for wrong extra parameter, 2 attempts left
    When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
    Then there is an error for wrong extra parameter, 1 attempts left
    When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
    Then there is an error for wrong extra parameter, 0 attempts left
    When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 20111980
    Then there is an error that user exceed the number of attempts

#Scenario: Reset number of attempts counter after successful login (not solved yet, defect ID 7263)
#Given a predefined dataset for election with extra parameter 20111980 and id ed7e622bf1804a389162e0d18658a11a
#When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
#Then there is an error for wrong extra parameter, 4 attempts left
#When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
#Then there is an error for wrong extra parameter, 3 attempts left
#When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
#Then there is an error for wrong extra parameter, 2 attempts left
#When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
#Then there is an error for wrong extra parameter, 1 attempts left
#When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 20111980
#Then the result should be the encoded start voting key p5CA4ZqAWx4D/WCHCFLYLrwt1jS9v5pcApt8pSVEVUgXEsv71Gmu1gTMGHkfuSS/1PAtkoCsj4w=
#When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
#Then there is an error for wrong extra parameter, 4 attempts left
#When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
#Then there is an error for wrong extra parameter, 3 attempts left
#When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 20111980
#Then the result should be the encoded start voting key p5CA4ZqAWx4D/WCHCFLYLrwt1jS9v5pcApt8pSVEVUgXEsv71Gmu1gTMGHkfuSS/1PAtkoCsj4w=

  Scenario: Wrong Election event ID
    Given a predefined dataset for election with authId f9949cfa3b1f5785cc48b9f879ebdec5 and election id fffffffffffffffffff
    When searching for election id aaaaaaaaaaaaaaa and authId f9949cfa3b1f5785cc48b9f879ebdec5
    Then there is an error for wrong SVK
