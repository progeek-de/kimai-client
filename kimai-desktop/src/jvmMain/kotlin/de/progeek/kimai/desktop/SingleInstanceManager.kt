package de.progeek.kimai.desktop

import java.io.BufferedReader
import java.io.Closeable
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

/**
 * Manages single-instance behavior for the application using a local socket.
 */
class SingleInstanceManager(
    private val config: Config = Config(),
    private val onActivate: () -> Unit
) : Closeable {

    /**
     * Configuration for SingleInstanceManager.
     * Extracted to support testability and configurability (Open/Closed Principle).
     */
    data class Config(
        val port: Int = DEFAULT_PORT,
        val host: String = LOCALHOST,
        val socketTimeout: Int = SOCKET_TIMEOUT_MS
    )

    private var server: InstanceServer? = null

    /**
     * Starts the server to listen for activation requests.
     * @return true if server started successfully, false otherwise
     */
    fun startServer(): Boolean {
        return try {
            server = InstanceServer(config, onActivate).also { it.start() }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Stops the server and releases resources.
     */
    override fun close() {
        server?.close()
        server = null
    }

    @Deprecated("Use close() instead for Closeable compliance", ReplaceWith("close()"))
    fun stop() = close()

    companion object {
        private const val DEFAULT_PORT = 47632
        private const val LOCALHOST = "127.0.0.1"
        private const val SOCKET_TIMEOUT_MS = 2000
        internal const val ACTIVATE_COMMAND = "ACTIVATE"
        internal const val ACK_RESPONSE = "ACK"

        /**
         * Attempts to activate an existing instance.
         * @return true if another instance was found and activated, false otherwise
         */
        fun tryActivateExistingInstance(config: Config = Config()): Boolean {
            return InstanceClient(config).tryActivate()
        }
    }
}

/**
 * Client for communicating with an existing instance.
 * Single Responsibility: Only handles client-side communication.
 */
private class InstanceClient(private val config: SingleInstanceManager.Config) {

    fun tryActivate(): Boolean {
        return try {
            createSocket().use { socket ->
                val (reader, writer) = socket.createStreams()
                writer.println(SingleInstanceManager.ACTIVATE_COMMAND)
                reader.readLine() == SingleInstanceManager.ACK_RESPONSE
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun createSocket(): Socket {
        return Socket(InetAddress.getByName(config.host), config.port).apply {
            soTimeout = config.socketTimeout
        }
    }
}

/**
 * Server that listens for activation requests from other instances.
 * Single Responsibility: Only handles server-side socket management.
 */
private class InstanceServer(
    private val config: SingleInstanceManager.Config,
    private val onActivate: () -> Unit
) : Closeable {

    private var serverSocket: ServerSocket? = null
    private var serverThread: Thread? = null

    @Volatile
    private var running = false

    fun start() {
        serverSocket = ServerSocket(config.port, 1, InetAddress.getByName(config.host))
        running = true
        serverThread = thread(isDaemon = true, name = "SingleInstanceServer") {
            acceptConnections()
        }
    }

    private fun acceptConnections() {
        while (running) {
            try {
                serverSocket?.accept()?.let { handleClient(it) }
            } catch (e: SocketException) {
                if (running) e.printStackTrace()
                break
            } catch (e: Exception) {
                if (running) e.printStackTrace()
            }
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.use {
                socket.soTimeout = config.socketTimeout
                val (reader, writer) = socket.createStreams()
                if (reader.readLine() == SingleInstanceManager.ACTIVATE_COMMAND) {
                    writer.println(SingleInstanceManager.ACK_RESPONSE)
                    SwingUtilities.invokeLater(onActivate)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun close() {
        running = false
        runCatching { serverSocket?.close() }
        serverSocket = null
        serverThread = null
    }
}

/**
 * Extension function to create reader/writer pair from socket.
 * DRY: Eliminates duplicate stream creation code.
 */
private fun Socket.createStreams(): Pair<BufferedReader, PrintWriter> {
    val reader = BufferedReader(InputStreamReader(getInputStream()))
    val writer = PrintWriter(getOutputStream(), true)
    return reader to writer
}
