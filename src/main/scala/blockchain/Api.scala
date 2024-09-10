
package com.blockchain

import org.scalatra._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import org.slf4j.LoggerFactory
import java.security.{KeyPairGenerator, SecureRandom, PublicKey, PrivateKey, Security,Signature}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.util.Base64
import utils.KeyManager
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.json4s.jackson.Serialization.write

class Api(nodeAddress: Address) extends ScalatraServlet {
val logger = LoggerFactory.getLogger(getClass)
  implicit val formats: Formats = DefaultFormats
    // Adiciona o provedor BouncyCastle
  Security.addProvider(new BouncyCastleProvider())

  // Função para gerar um par de chaves temporárias
  def generateKeyPair(): (PublicKey, PrivateKey) = {
    val keyGen = KeyPairGenerator.getInstance("ECDSA", "BC")
    keyGen.initialize(256, new SecureRandom())
    val keyPair = keyGen.generateKeyPair()
    (keyPair.getPublic, keyPair.getPrivate)
  }

  // Função para assinar os dados da transação
  def signTransaction(privateKey: PrivateKey, data: String): String = {
    val signer = Signature.getInstance("SHA256withECDSA", "BC")
    signer.initSign(privateKey)
    signer.update(data.getBytes)
    val signatureBytes = signer.sign()
    Base64.getEncoder.encodeToString(signatureBytes)
  }

  // Exibindo informações de source e destination
  /* logger.info(s"Source Address: ${source.getAddress}")
  logger.info(s"Destination Address: ${destination.getAddress}")
  logger.info(s"Signature Address: ${destination.getAddress}") */

  post("/transaction") {
  try {
    // Exemplo de logging detalhado
    logger.info("Iniciando o processamento da transação.")
    val json = parse(request.body)
    val sourceAddress = (json \ "source").extract[String]
    val destinationAddress = (json \ "destination").extract[String]
    val amount = (json \ "amount").extract[Long]
    val nonce = (json \ "nonce").extract[Long]

    logger.info(s"Dados recebidos: sourceAddress=$sourceAddress, destinationAddress=$destinationAddress, amount=$amount, nonce=$nonce")
    println(s"JSON enviado: $json")
    // Gerando pares de chaves temporários para teste
    val sourcePublicKey = KeyManager.sourcePublicKey
    val sourcePrivateKey = KeyManager.sourcePrivateKey
    val destinationPublicKey = KeyManager.destinationPublicKey
    val destinationPrivateKey = KeyManager.destinationPrivateKey
    //val (sourcePublicKey, sourcePrivateKey) = generateKeyPair()
    //val (destinationPublicKey, destinationPrivateKey) = generateKeyPair()
    println(s"SourcKey: $sourcePublicKey")
    println(s"DestKey: $destinationPublicKey")
    val source = Address(sourcePublicKey, sourcePrivateKey)
    val destination = Address(destinationPublicKey, destinationPrivateKey)

    // Assinando os dados
    val dataToSign = s"${sourceAddress}-${destinationAddress}-$amount-$nonce"
    println(s"Data to sign (Client): $dataToSign")
    val signature = signTransaction(sourcePrivateKey, dataToSign)
    println(s"Generated signature (Client): $signature")
    logger.info(s"Assinatura gerada: $signature")

    

    // Criando a transação
    val transaction = Transaction(source, destination, amount, nonce, signature)
    println(s"Transaction: $transaction")
    Blockchain.submitTransaction(transaction) match {
      case Right(tx) => 
        logger.info("Transação processada com sucesso.")
        Blockchain.addTransactionToBlock(tx, source)
        Ok(write(tx))
      case Left(err) => 
        logger.error(s"Erro ao processar a transação: $err")
        BadRequest(write(Map("error" -> err)))
    }
  } catch {
    case e: Exception =>
      logger.error("Erro interno ao processar a transação", e)
      InternalServerError("Internal server error")
  }
}


  /* get("/balance/:address") {
    val encodedAddress  = params("address")
    val address = URLDecoder.decode(encodedAddress, StandardCharsets.UTF_8.toString)
    println(s"Received address: $encodedAddress, Decoded: $address")
    Ok(write(Map("balance" -> Blockchain.getBalance(address))))
  } */

  post("/balance") {
    try {
      val json = parse(request.body)
      val address = (json \ "address").extract[String]
      println(s"Received address: $address")
      Ok(write(Map("balance" -> Blockchain.getBalance(address))))
    } catch {
      case e: Exception =>
        logger.error("Erro interno ao processar a requisição de balance", e)
        InternalServerError("Internal server error")
    }
  }


    get("/block/:index/transactions") {
    try {
        val index = params("index").toLong  // Pega o índice do bloco a partir dos parâmetros da URL

        // Busca o bloco pelo índice
        val blockOption = Blockchain.getBlockByIndex(index)

        blockOption match {
            case Some(block) =>
                // Retorna as transações do bloco encontrado
                Ok(write(block.transactions))
            case None =>
                NotFound(write(Map("error" -> s"Block with index $index not found")))
        }
    } catch {
        case e: Exception =>
            logger.error("Error retrieving transactions from block", e)
            InternalServerError("Internal server error")
    }
}


}
