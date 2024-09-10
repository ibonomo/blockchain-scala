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
git clone https://github.com/ibonomo/blockchain-scala.git
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

## Architecture and Design

### Overview

The project is designed around a simple blockchain architecture that includes:

- **Block**: A block contains a sequence of transactions, a hash reference to the previous block, and a digital signature of the block's data.
- **Transaction**: Represents a value transfer from a source address to a destination address, signed by the source's private key.
- **Ledger**: Maintains account balances and is updated with each valid transaction.
- **API**: A REST API built Scalatra servlet.

### Key Components

- **Address**: Represents a public/private key pair.
- **Transaction**: Contains details about the value transfer, including the source and destination addresses, amount, nonce, and signature.
- **Block**: A container for transactions, referencing the previous block's hash.
- **Blockchain**: Manages the chain of blocks, validates transactions, and maintains the ledger.
- **Api**: Exposes the blockchain functionality via HTTP endpoints.

### Design Decisions

- **ECDSA for Signing**: The project uses ECDSA for signing transactions and blocks to ensure authenticity and integrity.
- **In-Memory Ledger**: The ledger and blockchain are stored in memory for simplicity and speed. This is suitable for a prototype but would need to be persisted in a database for production.
- **Single Node**: The blockchain runs as a single node. For a production environment, this would be extended to support multiple nodes and consensus mechanisms.

## Future Work and Production Readiness

To prepare this project for a production environment, consider the following improvements:

- **Persistence Layer**: Implement a database to persist the blockchain and ledger state.
- **Scalability**: Extend the solution to support multiple nodes, implementing a consensus mechanism like Proof of Work (PoW) or Proof of Stake (PoS).
- **Security Enhancements**: Implement additional security features such as encrypted communication, rate limiting, and robust error handling.
- **API Improvements**: Enhance the API with features like pagination for transaction history, authentication, and improved error messages.
- **Testing**: Add more comprehensive tests, including integration and performance tests.