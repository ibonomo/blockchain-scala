 import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.blockchain._
import utils.KeyManager

class BlockchainTest extends AnyFlatSpec with Matchers {

  "A Transaction" should "be valid when signed correctly" in {
    val source = Address(KeyManager.sourcePublicKey, KeyManager.sourcePrivateKey)
    val destination = Address(KeyManager.destinationPublicKey, null)
    val transaction = Transaction(source, destination, 100, 1)

    // Assina a transação
    val signedTransaction = transaction.copy(signature = transaction.signTransaction(source.privateKey, s"${source.getAddress}-${destination.getAddress}-100-1"))
    
    // Verifica se a transação é válida
    signedTransaction.isValid should be(true)
  }

  it should "be invalid when the signature is incorrect" in {
    val source = Address(KeyManager.sourcePublicKey, KeyManager.sourcePrivateKey)
    val destination = Address(KeyManager.destinationPublicKey, null)
    val transaction = Transaction(source, destination, 100, 1)

    // Assina a transação com dados incorretos
    val signedTransaction = transaction.copy(signature = transaction.signTransaction(source.privateKey, s"${source.getAddress}-${destination.getAddress}-100-2"))

    // Verifica se a transação é inválida
    signedTransaction.isValid should be(false)
  }

  "A Block" should "add transactions and create a new block when limit is reached" in {
    val source = Address(KeyManager.sourcePublicKey, KeyManager.sourcePrivateKey)
    val destination = Address(KeyManager.destinationPublicKey, null)
    // Inicializa a blockchain e garante que o bloco gênesis é criado
    Blockchain.createGenesisBlock()
    // Adiciona 10 transações ao bloco
    for (i <- 1 to 10) {
      // Cria a transação
      val transaction = Transaction(source, destination, 1 * i, i)
      
      // Assina a transação
      val signedTransaction = transaction.copy(signature = transaction.signTransaction(source.privateKey, s"${source.getAddress}-${destination.getAddress}-${1 * i}-$i"))
      
      // Debugging: Verifique a transação e a assinatura
      println(s"Transaction: $signedTransaction")
      
      // Submete a transação assinada ao blockchain
      Blockchain.submitTransaction(signedTransaction)
      Blockchain.addTransactionToBlock(signedTransaction, source)
    }

    // Verifica se um novo bloco foi criado com 10 transações
    Blockchain.getLastBlock.transactions.size should be(10)

    // Adiciona mais uma transação para verificar se um novo bloco é iniciado
    val extraTransaction = Transaction(source, destination, 100, 11)
    val signedExtraTransaction = extraTransaction.copy(signature = extraTransaction.signTransaction(source.privateKey, s"${source.getAddress}-${destination.getAddress}-100-11"))
    Blockchain.submitTransaction(signedExtraTransaction)
    Blockchain.addTransactionToBlock(signedExtraTransaction, source)
    // Verifica se o novo bloco contém apenas a transação extra
    Blockchain.getCurrentBlockTransactions.size should be(1)
  }

  "A Blockchain" should "update balances correctly after transactions" in {
    val source = Address(KeyManager.sourcePublicKey, KeyManager.sourcePrivateKey)
    val destination = Address(KeyManager.destinationPublicKey, null)

    // Adiciona 1000 moedas ao ledger para o endereço source
    Blockchain.setBalance(source.getAddress, 1000L)
    Blockchain.setBalance(destination.getAddress, 0L)

    // Verifica o saldo inicial
    Blockchain.getBalance(source.getAddress) should be(1000)
    Blockchain.getBalance(destination.getAddress) should be(0)

    // Cria e adiciona uma transação válida
    val transaction = Transaction(source, destination, 100, 1)
    val signedTransaction = transaction.copy(signature = transaction.signTransaction(source.privateKey, s"${source.getAddress}-${destination.getAddress}-100-1"))
    Blockchain.submitTransaction(signedTransaction) match {
      case Right(tx) => Blockchain.addTransactionToBlock(tx, source)
      case Left(err) => fail(s"Transaction failed: $err")
    }

    // Verifica os saldos após a transação
    Blockchain.getBalance(source.getAddress) should be(900)
    Blockchain.getBalance(destination.getAddress) should be(100)
  }

  it should "reject transactions with insufficient funds" in {
    val source = Address(KeyManager.sourcePublicKey, KeyManager.sourcePrivateKey)
    val destination = Address(KeyManager.destinationPublicKey, null)

    // Tenta criar uma transação que excede o saldo disponível
    val transaction = Transaction(source, destination, 2000, 1)
    val signedTransaction = transaction.copy(signature = transaction.signTransaction(source.privateKey, s"${source.getAddress}-${destination.getAddress}-2000-1"))
    Blockchain.submitTransaction(signedTransaction) match {
      case Right(tx) => fail("Transaction should have failed due to insufficient funds")
      case Left(err) => err should be("Insufficient funds")
    }
  }
}
