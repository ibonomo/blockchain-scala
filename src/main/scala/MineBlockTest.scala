import scalaj.http._
import org.json4s._
import org.json4s.jackson.Serialization.write
import java.security._
import utils.KeyManager
import java.util.Base64

case class Transaction(source: String, destination: String, amount: Long, nonce: Long, signature: String)

object MineBlockTest extends App {
  implicit val formats: DefaultFormats.type = DefaultFormats

  // Função para gerar uma assinatura para a transação
  def signTransaction(privateKey: PrivateKey, data: String): String = {
    val signer = Signature.getInstance("SHA256withECDSA", "BC")
    signer.initSign(privateKey)
    signer.update(data.getBytes)
    val signatureBytes = signer.sign()
    Base64.getEncoder.encodeToString(signatureBytes)
  }

  // Exemplo de dados de transações
  val sourcePublicKey = KeyManager.sourcePublicKey
  val sourcePrivateKey = KeyManager.sourcePrivateKey
  val destinationPublicKey = KeyManager.destinationPublicKey

  // Criar transações fictícias
  val transactions = Seq(
    {
      val dataToSign = s"${Base64.getEncoder.encodeToString(sourcePublicKey.getEncoded)}-${Base64.getEncoder.encodeToString(destinationPublicKey.getEncoded)}-100-1"
      val signature = signTransaction(sourcePrivateKey, dataToSign)
      Transaction(
        source = Base64.getEncoder.encodeToString(sourcePublicKey.getEncoded),
        destination = Base64.getEncoder.encodeToString(destinationPublicKey.getEncoded),
        amount = 100,
        nonce = 1,
        signature = signature
      )
    }
  )

  // Serializa a lista de transações para JSON
  val jsonPayload = write(transactions)

  // Envia a requisição POST para o endpoint /mineBlock
  val response = Http("http://localhost:8080/mine")
    .postData(jsonPayload)
    .header("Content-Type", "application/json")
    .asString

  // Imprime a resposta do servidor
  println(s"Response: ${response.body}")
}
