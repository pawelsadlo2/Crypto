Exposed endpoints:

1. get exchange forecast from one crypto to others including fee

Request:POST /currencies/exchange
Request body example:  {"from":"BTC","to":["ETH","USD"],"amount":121}

Response body example:
{
    "from": "BTC",
    "to": {
        "ETH": {
            "amount": 121,
            "fee": 0.01210,
            "rate": 14.662,
            "result": 1773.92458980
        },
        "USD": {
            "amount": 121,
            "fee": 0.01210,
            "rate": 42307.208,
            "result": 5118660.25078320
        }
    }
}


2. get rates for specified crypto to selected ones (filter), if no filter is specified, will output all known rates

example request: GET http://localhost:8080/currencies/BTC?filter=USD&filter=BMD


