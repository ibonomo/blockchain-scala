package utils

import java.security._
import java.security.spec.{X509EncodedKeySpec, PKCS8EncodedKeySpec}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.nio.file.{Files, Paths, Path}
import java.util.Base64

object KeyManager {
  Security.addProvider(new BouncyCastleProvider())

  private val keyDirectory: Path = Paths.get("keys")

  // Função para criar o diretório se ele não existir
  private def ensureKeyDirectoryExists(): Unit = {
    if (!Files.exists(keyDirectory)) {
      Files.createDirectories(keyDirectory)
    }
  }

  // Função para salvar a chave em um arquivo
  private def saveKeyToFile(key: Array[Byte], fileName: String): Unit = {
    ensureKeyDirectoryExists()
    Files.write(keyDirectory.resolve(fileName), key)
  }

  // Função para carregar a chave de um arquivo
  private def loadKeyFromFile(fileName: String): Array[Byte] = {
    Files.readAllBytes(keyDirectory.resolve(fileName))
  }

  // Função para gerar o par de chaves
  private def generateKeyPair(): KeyPair = {
    val keyGen = KeyPairGenerator.getInstance("ECDSA", "BC")
    keyGen.initialize(256, new SecureRandom())
    keyGen.generateKeyPair()
  }

  // Verifica se as chaves já existem, se não, gera e salva
  lazy val (sourcePublicKey, sourcePrivateKey) = {
    if (!Files.exists(keyDirectory.resolve("sourcePublicKey"))) {
      val keyPair = generateKeyPair()
      saveKeyToFile(keyPair.getPublic.getEncoded, "sourcePublicKey")
      saveKeyToFile(keyPair.getPrivate.getEncoded, "sourcePrivateKey")
      (keyPair.getPublic, keyPair.getPrivate)
    } else {
      val publicKeyBytes = loadKeyFromFile("sourcePublicKey")
      val privateKeyBytes = loadKeyFromFile("sourcePrivateKey")
      val keyFactory = KeyFactory.getInstance("ECDSA", "BC")
      (
        keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes)),
        keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes))
      )
    }
  }

  lazy val (destinationPublicKey, destinationPrivateKey) = {
    if (!Files.exists(keyDirectory.resolve("destinationPublicKey"))) {
      val keyPair = generateKeyPair()
      saveKeyToFile(keyPair.getPublic.getEncoded, "destinationPublicKey")
      saveKeyToFile(keyPair.getPrivate.getEncoded, "destinationPrivateKey")
      (keyPair.getPublic, keyPair.getPrivate)
    } else {
      val publicKeyBytes = loadKeyFromFile("destinationPublicKey")
      val privateKeyBytes = loadKeyFromFile("destinationPrivateKey")
      val keyFactory = KeyFactory.getInstance("ECDSA", "BC")
      (
        keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes)),
        keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes))
      )
    }
  }
}
