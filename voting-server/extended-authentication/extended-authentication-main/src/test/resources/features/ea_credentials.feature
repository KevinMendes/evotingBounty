Feature: Handle credentials

  Scenario: Update credential
    Given a predefined dataset for election with id ed7e622bf1804a389162e0d18658a11a
    When searching for authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 20111980
    Then the result should be the encoded start voting key p5CA4ZqAWx4D/WCHCFLYLrwt1jS9v5pcApt8pSVEVUgXEsv71Gmu1gTMGHkfuSS/1PAtkoCsj4w=
    When credential with old authId f9949cfa3b1f5785cc48b9f879ebdec5 is updated with new authId b16a37d88aa5428499ed59de2c3b4eb9 and SVK 0L3fCmcsV/A6BcDaBkrStryb4BksqyvqVHFJVJFVAjxjsPYNQbOYgEHZqC+zCljz=
    When searching for authId b16a37d88aa5428499ed59de2c3b4eb9 and extra parameter 20111980
    Then the result should be the encoded start voting key 0L3fCmcsV/A6BcDaBkrStryb4BksqyvqVHFJVJFVAjxjsPYNQbOYgEHZqC+zCljz=

  Scenario: Update non existing credential
    Given a predefined dataset for election with id ed7e622bf1804a389162e0d18658a11a
    When credential with old authId thisauthiddoesnotexist is updated with new authId f9949cfa3b1f5785cc48b9f879ebdec5 and SVK asdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdasd=
    Then credential does not exists

  Scenario: Save EA csv, single election event
    Given a clean scenario
    When uploading the csv file valid_credentials_single_ee.csv for election event 2d39baab0e964135bf4cb0f0e4b81ae5
#WRONG ELECTION EVENT
    When searching for election event ababababababababababababababababa authId f9949cfa3b1f5785cc48b9f879ebdec5 and extra parameter 11111111
    Then there is an error for wrong SVK
#WRONG EXTRA PARAMETER CREDENTIALS 1
    When searching for election event 2d39baab0e964135bf4cb0f0e4b81ae5 authId 2a894a6f51744b217fd50f71dd4c3f0f and extra parameter 11111119
    Then there is an error for wrong extra parameter, 4 attempts left
#CORRECT PARAMETERS CREDENTIALS 1
    When searching for election event 2d39baab0e964135bf4cb0f0e4b81ae5 authId 2a894a6f51744b217fd50f71dd4c3f0f and extra parameter 11111111
    Then the result should be the encoded start voting key uAZlfxB9xXhrDPMiYcWiUuIbIcQy7xGsRxcgNqB2kssmNE3sNZ3dokbMVgJjKuuF1Bx3MAzn+/c=
#CORRECT PARAMETERS CREDENTIALS 2
    When searching for election event 2d39baab0e964135bf4cb0f0e4b81ae5 authId aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa and extra parameter 12101982
    Then the result should be the encoded start voting key p5CA4ZqAWx4D/WCHCFLYLrwt1jS9v5pcApt8pSVEVUgXEsv71Gmu1gTMGHkfuSS/1111111114w=
#AGAIN CORRECT PARAMETERS CREDENTIALS 2
    When searching for election event 2d39baab0e964135bf4cb0f0e4b81ae5 authId aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa and extra parameter 12101982
    Then the result should be the encoded start voting key p5CA4ZqAWx4D/WCHCFLYLrwt1jS9v5pcApt8pSVEVUgXEsv71Gmu1gTMGHkfuSS/1111111114w=

  Scenario: Save EA csv, several election event in several files
    Given a clean scenario
#UPLOADING CREDENTIALS FILE 1
    When uploading the csv file valid_credentials_several_ee_1.csv for election event 305415d20b674e7d9de82097a5e547d5
#WRONG ELECTION EVENT: CREDENTIAL ELECTION EVENT 1
    When searching for election event w05415d20123456789082097a5e547d5 authId 1c9ce7ffd2eb4c0916d7738e3e2a848f and extra parameter 20111980
    Then there is an error for wrong SVK
#WRONG EXTRA PARAMETER: CREDENTIAL ELECTION EVENT 1
    When searching for election event 305415d20b674e7d9de82097a5e547d5 authId 1c9ce7ffd2eb4c0916d7738e3e2a848f and extra parameter 11111111
    Then there is an error for wrong extra parameter, 4 attempts left
#CORRECT PARAMETERS CREDENTIAL 1
    When searching for election event 305415d20b674e7d9de82097a5e547d5 authId 1c9ce7ffd2eb4c0916d7738e3e2a848f and extra parameter 12101982
    Then the result should be the encoded start voting key E3ZirJSzQvJn03Ghu6LKANBSG/Q73RCqJFf+3Or2alquIFoNATbeOgnPweVYahGW
#UPLOADING CREDENTIALS FILE 2
    When uploading the csv file valid_credentials_several_ee_2.csv for election event a038a421bbaf478d8381ec875ee40cdb
#WRONG ELECTION EVENT CREDENTIAL 2
    When searching for election event w05415d20123456789082097a5e547d5 authId 4ec8828ad11ed100bf1bd02485cf2883 and extra parameter 11111111
    Then there is an error for wrong SVK
#WRONG EXTRA PARAMETER CREDENTIAL 2
    When searching for election event a038a421bbaf478d8381ec875ee40cdb authId 4ec8828ad11ed100bf1bd02485cf2883 and extra parameter 12345678
    Then there is an error for wrong extra parameter, 4 attempts left
#CORRECT PARAMETERS CREDENTIAL 2
    When searching for election event a038a421bbaf478d8381ec875ee40cdb authId 4ec8828ad11ed100bf1bd02485cf2883 and extra parameter 11111111
    Then the result should be the encoded start voting key uQ5zb+gCl3e9VryYFQKROn1up2lgGzLAYEdGVbKOWi3q78aRftNduJoOtyqlrodK