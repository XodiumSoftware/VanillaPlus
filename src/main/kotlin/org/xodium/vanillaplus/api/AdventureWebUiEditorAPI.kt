@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.api

import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

/** API for interacting with the Adventure Web UI Editor. */
internal object AdventureWebUiEditorAPI {
    private val root: URI = URI.create("https://editor.xodium.org/")
    private val client: HttpClient = HttpClient.newHttpClient()

    /**
     * Starts a new session with the given input, command, and application.
     * @param input the input string.
     * @param command the command string.
     * @param application the application string.
     * @return the resulting token in a completable future.
     */
    fun startSession(
        input: String,
        command: String,
        application: String,
    ): CompletableFuture<String?> {
        val request =
            HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(constructBody(input, command, application)))
                .uri(root.resolve(URI.create("/api/editor/input")))
                .build()

        return client
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response ->
                when (response.statusCode()) {
                    200 -> extractToken(response.body())
                    else -> throw IOException("The server could not handle the request.")
                }
            }
    }

    /**
     * Retrieves the result of a session, given a token.
     * @param token the token
     * @return the resulting MiniMessage string in a completable future
     */
    fun retrieveSession(token: String): CompletableFuture<String?> {
        val request =
            HttpRequest
                .newBuilder()
                .GET()
                .uri(root.resolve(URI.create("/api/editor/output?token=$token")))
                .build()

        return client
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response ->
                when (response.statusCode()) {
                    404 -> null
                    200 -> response.body()
                    else -> throw IOException("The server could not handle the request.")
                }
            }
    }

    /**
     * Extracts the token from the response body.
     * @param body the response body.
     * @return the extracted token.
     */
    private fun extractToken(body: String): String {
        val tokenRegex = """"token"\s*:\s*"([^"]+)"""".toRegex()

        return tokenRegex.find(body)?.groupValues?.get(1)
            ?: throw IOException("The result did not contain a token.")
    }

    /**
     * Constructs the body of the POST request.
     * @param input the input string.
     * @param command the command string.
     * @param application the application string.
     * @return the constructed body string.
     */
    private fun constructBody(
        input: String,
        command: String,
        application: String,
    ): String = """{"input":"$input","command":"$command","application":"$application"}"""
}
