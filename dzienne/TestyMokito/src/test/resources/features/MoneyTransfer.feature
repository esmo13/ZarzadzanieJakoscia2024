Feature: MoneyTransfer tests, account is not mocked

  Scenario: Internal payment, all is ok
    #Given SetUpTestEnv
    Given We have user "Tomasz" with id: 1
    Given "Tomasz" have account: 2 with: 1000 pln
    Given There is an account:3 with 100 pln
    Given Everything is authorised
    When "Tomasz" make transfer from acc: 2 to acc: 3 with amount: 200
    Then account:2 value:800.0 pln
    Then account:3 value:300.0 pln