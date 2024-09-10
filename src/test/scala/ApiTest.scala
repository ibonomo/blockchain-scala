package com.blockchain

import org.scalatra.test.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.json4s._
import org.json4s.jackson.JsonMethods._
import utils.KeyManager
import org.json4s.jackson.Serialization.write
import org.json4s.DefaultFormats
import scalaj.http._
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.json4s.jackson.Serialization.write


class ApiTest extends ScalatraFlatSpec with Matchers {

  implicit val jsonFormats: Formats = DefaultFormats

  // Inicializa o endereço do nó e adiciona o servlet da API
  val nodeAddress = Address(KeyManager.sourcePublicKey, KeyManager.sourcePrivateKey)
  addServlet(new Api(nodeAddress), "/*")

  "POST /transaction" should "process a valid transaction" in {
  // Configura as chaves e cria a transação
  val source = Address(KeyManager.sourcePublicKey, KeyManager.sourcePrivateKey)
  val destination = Address(KeyManager.destinationPublicKey, null)
  val transaction = Transaction(source, destination, 100, 1)
  val signedTransaction = transaction.copy(
    signature = transaction.signTransaction(source.privateKey, s"${source.getAddress}-${destination.getAddress}-100-1")
  )

  // Serializa a transação para JSON, garantindo que a assinatura seja tratada como uma string
  val transactionJson = write(Map(
    "source" -> signedTransaction.source.getAddress,
    "destination" -> signedTransaction.destination.getAddress,
    "amount" -> signedTransaction.amount,
    "signature" -> signedTransaction.signature,
    "nonce" -> signedTransaction.nonce
  ))

  // Faz a requisição POST para /transaction
  post("/transaction", body = transactionJson.getBytes, headers = Map("Content-Type" -> "application/json")) {
    status should equal(200)
    body should include("\"amount\":100")
  }
}

it should "fail with invalid transaction" in {
  // Configura as chaves e cria a transação
  val source = Address(KeyManager.sourcePublicKey, KeyManager.sourcePrivateKey)
  val destination = Address(KeyManager.destinationPublicKey, null)
  val transaction = Transaction(source, destination, 100, 1)
  val signedTransaction = transaction.copy(
    signature = transaction.signTransaction(source.privateKey, s"${source.getAddress}-${destination.getAddress}-100-1")
  )

  // Serializa a transação para JSON, garantindo que a assinatura seja tratada como uma string
  val invalidTransactionJson = write(Map(
    "source" -> signedTransaction.source.getAddress,
    "destination" -> signedTransaction.destination.getAddress,
    "amount" -> 1000,
    "signature" -> signedTransaction.signature,
    "nonce" -> signedTransaction.nonce
  ))

  // Faz a requisição POST para /transaction
  post("/transaction", body = invalidTransactionJson, headers = Map("Content-Type" -> "application/json")) {
      status should equal (400)
      body should include ("error")
   }
}

"POST /balance" should "return the balance for the given address" in {
val source = Address(KeyManager.sourcePublicKey, KeyManager.sourcePrivateKey)
val destination = Address(KeyManager.destinationPublicKey, null)
// Configurando o saldo inicial para 1000
Blockchain.setBalance(source.getAddress, 1000L)
val address = source.getAddress
val requestBody = write(Map("address" -> address))

post("/balance", body = requestBody.getBytes, headers = Map("Content-Type" -> "application/json")) {
  status should equal(200)
  body should include("\"balance\":1000")
}
} 
"GET /block/:index/transactions" should "retrieve transactions from a specific block" in {
  Blockchain.createGenesisBlock()
    val index = 0 // Assume that a block with this index exists

    get(s"/block/$index/transactions") {
      status should equal (200)
      body should include ("source")
    }
}

it should "return 404 if block does not exist" in {
    val index = 9999 // Assume that no block with this index exists

    get(s"/block/$index/transactions") {
      status should equal (404)
      body should include ("error")
    }
  }
} 

