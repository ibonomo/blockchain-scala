// src/main/scala/TransactionTest.scala
import java.security.{KeyPairGenerator, SecureRandom, PublicKey, PrivateKey, Signature}
import java.util.Base64
import scalaj.http._
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import com.blockchain.Address
import utils.KeyManager

object TransactionTest extends App {

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

  // Gerando pares de chaves
  //val (sourceAddress, sourcePrivateKey) = generateKeyPair()
  //val (destinationAddress, _) = generateKeyPair()

  //val (sourcePublicKey, sourcePrivateKey) = generateKeyPair()
  //val (destinationPublicKey, destinationPrivateKey) = generateKeyPair()
  // Importando as chaves do KeyManager
  val sourcePublicKey = KeyManager.sourcePublicKey
  val sourcePrivateKey = KeyManager.sourcePrivateKey
  val destinationPublicKey = KeyManager.destinationPublicKey
  val destinationPrivateKey = KeyManager.destinationPrivateKey

  
  println(s"SourcKey: $sourcePublicKey")
  println(s"DestKey: $destinationPublicKey")
  val source = Address(sourcePublicKey, sourcePrivateKey)
  val destination = Address(destinationPublicKey, destinationPrivateKey)
  
  // Dados da transação
  val amount = 10
  val nonce = 1
  val dataToSign = s"${source.getAddress}-${destination.getAddress}-$amount-$nonce"
  println(s"Data to sign (Client): $dataToSign")
  // Assinando a transação
  val signature = signTransaction(sourcePrivateKey, dataToSign)
  println(s"Received signature (Server): $signature")
  // Preparando o corpo da transação
  val transactionJson = s"""
  {
    "source": "${source.getAddress}",
    "destination": "${destination.getAddress}",
    "amount": $amount,
    "signature": "$signature",
    "nonce": $nonce
  }
  """
  println(s"JSON enviado: $transactionJson")

  // Enviando a transação usando Scalaj HTTP
  val response = Http("http://localhost:8080/transaction")
    .postData(transactionJson)
    .header("Content-Type", "application/json")
    .asString

  println(s"Response: ${response.body}")
}
