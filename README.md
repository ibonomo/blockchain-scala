# Blockchain Scala Project

## Description
This project implements a single-node blockchain solution for conventional currency-style transactions. It features an API to submit transactions, view balances, and mine blocks.

## Installation

### Prerequisites
- Scala 2.13
- SBT (Scala Build Tool)
- Java 11 or higher

### Clone the Repository
```bash
git clone https://github.com/yourusername/blockchain-scala.git
cd blockchain-scala
```


### Install Dependencies 
```bash
sbt update
```

### Compile the Project

```bash
sbt compile
```

### Run the Server

```bash
sbt run
```
This will start the server on http://localhost:8080.

### Running Tests
The project includes unit tests to ensure that the blockchain and API work as expected.

To run the tests, use the following command:
```bash
sbt test
```

## API Endpoints

### Submit a Transaction
- POST /transaction
- Description: Submits a new transaction to the blockchain.
```json
{
  "source": "<source_address>",
  "destination": "<destination_address>",
  "amount": <amount>,
  "signature": "<transaction_signature>",
  "nonce": <nonce>
}
```
- Response:
  - 200 OK with the account balance
  - 404 Not Found if the account does not exist.

### Check Balance
- POST /balance
- Description: Retrieves the balance of a given account.
```json
{
  "address": "<account_address>"
}
```
- Response:
  - 200 OK with the account balance
  - 404 Not Found if the account does not exist.
### Get Transactions from a Block
- GET /block/:index/transactions
- Description: Retrieves transactions from a specific block by its index.

- Response:
  - 200 OK with the list of transactions
  - 404 Not Found if the block does not exist.

## KeyManager
The `KeyManager.scala` file is part of the `utils` package and is responsible for managing cryptographic keys. It includes functions for generating, saving, and loading keys. 