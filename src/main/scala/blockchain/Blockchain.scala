
package com.blockchain

import java.security.{KeyPair, PrivateKey, PublicKey, Signature}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.util.Base64
import utils.KeyManager

import scala.collection.mutable

case class Address(publicKey: PublicKey, privateKey: PrivateKey) {
  def getAddress: String = {
    Base64.getEncoder.encodeToString(publicKey.getEncoded)
  }
}

case class Transaction(source: Address, destination: Address, amount: Long, nonce: Long, signature: String = "") {
  /* def signTransaction(): Transaction = {
    val data = s"${source.getAddress}-${destination.getAddress}-$amount-$nonce"
    println(s"DATA $data")
    val sig = Signature.getInstance("SHA256withECDSA", "BC")
    sig.initSign(source.privateKey)
    sig.update(data.getBytes)
    val signedData = sig.sign()
    this.copy(signature = Base64.getEncoder.encodeToString(signedData))
  } */
  def signTransaction(privateKey: PrivateKey, data: String): String = {
    val signer = Signature.getInstance("SHA256withECDSA", "BC")
    println(s"DATAST $data")
    signer.initSign(privateKey)
    signer.update(data.getBytes)
    val signatureBytes = signer.sign()
    Base64.getEncoder.encodeToString(signatureBytes)
  }

  def isValid: Boolean = {
    val data = s"${source.getAddress}-${destination.getAddress}-$amount-$nonce"
    println(s"DATAisValid $data")
    val sig = Signature.getInstance("SHA256withECDSA", "BC")
    sig.initVerify(source.publicKey)
    sig.update(data.getBytes)
    val isValidSignature = sig.verify(Base64.getDecoder.decode(signature))
  
    if (!isValidSignature) {
        println(s"Assinatura inválida para transação: $data")
        return false
    }

    // Verificação de nonce ou duplicação de transações
    if (Blockchain.isTransactionInLedger(data)) {
        println(s"Transação duplicada detectada: $data")
        return false
    }
  
     isValidSignature
  }
  
}

case class Block(index: Long, transactions: Seq[Transaction], previousHash: String, hash: String = "", signature: String = "") {
  def calculateHash: String = {
    val data = s"$index-$transactions-$previousHash"
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    digest.digest(data.getBytes).map("%02x".format(_)).mkString
  }

  def signBlock(privateKey: PrivateKey): Block = {
    val data = calculateHash
    val sig = Signature.getInstance("SHA256withECDSA", "BC")
    sig.initSign(privateKey)
    sig.update(data.getBytes)
    val signedData = sig.sign()
    this.copy(hash = data, signature = Base64.getEncoder.encodeToString(signedData))
  }

  def isValidSignature(publicKey: PublicKey): Boolean = {
    val sig = Signature.getInstance("SHA256withECDSA", "BC")
    sig.initVerify(publicKey)
    sig.update(hash.getBytes)
    sig.verify(Base64.getDecoder.decode(signature))
  }
}

object Blockchain {
  Security.addProvider(new BouncyCastleProvider())
  private val blocks: mutable.Buffer[Block] = mutable.Buffer[Block]()
  private val ledger: mutable.Map[String, Long] = mutable.Map().withDefaultValue(0L)
  private var currentBlockTransactions: mutable.Buffer[Transaction] = mutable.Buffer[Transaction]()

  // Definindo o número máximo de transações por bloco
  private val maxTransactionsPerBlock = 10

  // Inicializa o ledger com 1000 moedas para o endereço `source` do KeyManager
  private def initializeLedger(): Unit = {
    val sourceAddress = KeyManager.sourcePublicKey.getEncoded
    ledger(Base64.getEncoder.encodeToString(sourceAddress)) = 1000
  }
  // Método para acessar o ledger externamente de forma controlada
  def isTransactionInLedger(transactionData: String): Boolean = {
    ledger.contains(transactionData)
  }

  // Alternativamente, para exibir no console diretamente
  def printLedger(): Unit = {
    println("Ledger Balances:")
    ledger.foreach { case (address, balance) =>
      println(s"Address: $address, Balance: $balance")
    }
  }

  // Método para adicionar fundos a um endereço
  def addFunds(address: String, amount: Long): Unit = {
    ledger(address) += amount
  }

  def setBalance(address: String, amount: Long): Unit = {
    ledger(address) = amount
  }

  // Chama a função para inicializar o ledger
  initializeLedger()
  


  def createGenesisBlock(): Block = {
    val genesisTransaction = Transaction(Address(null, null), Address(null, null), 0, 0)
    val genesisBlock = Block(0, Seq(genesisTransaction), "0")
    blocks += genesisBlock
    genesisBlock
  }

  def getLastBlock: Block = blocks.last

  def addBlock(transactions: Seq[Transaction], nodeAddress: Address): Either[String, Block] = {
    if (transactions.exists(!_.isValid)) return Left("Invalid transaction in block")

    val previousBlock = getLastBlock
    val newBlock = Block(previousBlock.index + 1, transactions, previousBlock.hash).signBlock(nodeAddress.privateKey)

    // Update ledger
    transactions.foreach { tx =>
      if (ledger(tx.source.getAddress) >= tx.amount) {
        ledger(tx.source.getAddress) -= tx.amount
        ledger(tx.destination.getAddress) += tx.amount
      } else {
        return Left("Insufficient funds for transaction")
      }
    }

    blocks += newBlock
    currentBlockTransactions.clear() // Limpa a lista de transações após o bloco ser minerado
    Right(newBlock)
  }
  // Função para adicionar uma transação ao bloco atual
  def addTransactionToBlock(transaction: Transaction, nodeAddress: Address): Unit = {
    currentBlockTransactions += transaction
    println(s"Transação adicionada ao bloco atual: $currentBlockTransactions")
    // Suponha que mineramos um bloco a cada 10 transações, por exemplo
    if (currentBlockTransactions.size >= maxTransactionsPerBlock) {
      addBlock(currentBlockTransactions.toSeq, nodeAddress) match {
        case Right(_) => println("Bloco minerado com sucesso.")
        case Left(err) => println(s"Erro ao minerar o bloco: $err")
      }
    }
    }

  def getCurrentBlockTransactions: mutable.Buffer[Transaction] = currentBlockTransactions


  def getBalance(address: String): Long = {
    println(s"Checking balance for address: $address")
    val balance = ledger.getOrElse(address, 0L)
    println(s"Balance for $address: $balance")
    balance
  }

  def submitTransaction(transaction: Transaction): Either[String, Transaction] = {
    if (!transaction.isValid) return Left("Invalid transaction signature")
    if (ledger(transaction.source.getAddress) < transaction.amount) return Left("Insufficient funds")
    ledger(transaction.source.getAddress) -= transaction.amount
    ledger(transaction.destination.getAddress) += transaction.amount
    Right(transaction)
  }

  def getBlockByIndex(index: Long): Option[Block] = {
    blocks.find(_.index == index)
  }

  def getBlockByHash(hash: String): Option[Block] = {
        blocks.find(_.hash == hash)
  }

}
