# Funds Transfer

This project aims to show a different way to create and run a ***self-contained microservice***, moving away from Springframework, but still leveraging on the Dependency Injection an other professional-oriented tools like [Hibernate](https://hibernate.org/)

All the application is built upon [Dropwizard](https://www.dropwizard.io), that by default does not provide the dependency injection. 
Therefore [Guice](https://github.com/google/guice/wiki/GettingStarted) has been integrated to achieve this capability.

Furthermore, [Derby](https://db.apache.org/derby/) provides an *in-memory* Data Store layer and [Hibernate](https://hibernate.org/) is used to access to it.

The application populates the in-memory database during the bootstrap thanks to [Flyway](https://flywaydb.org/).

Although this is a simple use case the integration of [Dropwizard](https://www.dropwizard.io) with [Guice](https://github.com/google/guice/wiki/GettingStarted), 
[Hibernate](https://hibernate.org/) and [Flyway](https://flywaydb.org/) can be the base to create a production-ready micro-service. 

## Build
Build with the command:
```shell
	mvn clean verify
```
It will execute the tests and generate an executable **fat** jar.

## Execute
The build result is an executable .jar file: `funds-transfer.jar`.

```shell
	java -jar funds-transfer.jar server config.yml
``` 

## Endpoints
Base path
```shell
  http://localhost:8080/api
```

#### Customers
Retrieves the list of all the customers in the database
```shell
    GET customers/
```

```shell
    GET customers/<customer uuid>
```
###### Example
```
    GET customers/52bc1e5d-af14-4fa0-8641-a1a3622e69e1
```

#### Accounts
This API contains a list of endpoint for retrieving information about the accounts existing in the system.
##### Customer 
Retrieves the list of the accounts available per customer
```shell
    GET customers/<customer uuid>/accounts
```

###### Example
```
    GET customers/52bc1e5d-af14-4fa0-8641-a1a3622e69e1/accounts
```

##### Account Details
Retrieves the account detail by its account id
```shell
    GET accounts/<account uuid>
```

###### Example
```
    GET accounts/7f9adf98-93fb-4868-8cd8-cfe0f53628c1
```

#### Transactions
Retrieves the list of transactions made per account
```shell
   GET accounts/<account id>/transactions
```
###### Example
```
    GET accounts/7f9adf98-93fb-4868-8cd8-cfe0f53628c1/transactions
```

#### Funds Transfer
The most important operation that the API provides is the ***funds transfer*** by which a customer can transfer an arbitrary amount of money from an account to another
```shell
   POST accounts/<account id>/transfer
```
###### Example
```
    POST accounts/7f9adf98-93fb-4868-8cd8-cfe0f53628c1/transfer
```
###### Request Body
```json
{
	"amount":{
		"value": 100.00,
		"currency": "GBP"
	},
	"beneficiaryAccountId": "85216e45-8dd1-41d7-a890-3cf71acd2630"
}
```
###### Response Body
```json
{
    "debtorAccount": {
        "ibanNumber": "GB40REVO60161331926819",
        "totalAmount": 20,
        "currency": "GBP",
        "id": "7f9adf98-93fb-4868-8cd8-cfe0f53628c1"
    },
    "beneficiaryAccount": {
        "ibanNumber": "GB40REVO00991232026772",
        "currency": "GBP",
        "id": "85216e45-8dd1-41d7-a890-3cf71acd2630"
    },
    "transactions": [
        {
            "amount": 100,
            "currency": "GBP",
            "dateTime": "2019-05-06T19:16:14 UTC",
            "type": "DEBIT",
            "id": "696f0073-dc6a-4d55-b04d-cdff9d9b87ff"
        },
        {
            "amount": 100,
            "currency": "GBP",
            "dateTime": "2019-05-06T19:16:14 UTC",
            "type": "CREDIT",
            "id": "50b4cf29-5631-4854-b7bb-582352b64547"
        }
    ]
}
```
