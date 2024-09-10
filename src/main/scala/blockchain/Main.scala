package com.blockchain

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import java.security.{KeyPairGenerator, SecureRandom, Security}
import org.bouncycastle.jce.provider.BouncyCastleProvider

object Main {
  def main(args: Array[String]): Unit = {
    // Registre o provedor de segurança BouncyCastle
    Security.addProvider(new BouncyCastleProvider())

    // Configurando o gerador de chaves para o endereço do nó
    val keyGen = KeyPairGenerator.getInstance("ECDSA", "BC")
    keyGen.initialize(256, new SecureRandom())
    val keyPair = keyGen.generateKeyPair()

    val nodeAddress = Address(keyPair.getPublic, keyPair.getPrivate)
    Blockchain.createGenesisBlock()

    // Configuração do Jetty Server
    val port = 8080
    val server = new Server(port)

    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")

    // Configurando a API
    context.addServlet(new ServletHolder(new Api(nodeAddress)), "/*")

    server.setHandler(context)
    server.start()
    server.join()
  }
}
