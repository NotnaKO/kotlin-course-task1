package org.example

import kotlinx.coroutines.*
import java.util.UUID

class Generator {
    fun generate(template: String, data: List<Map<String, Any>>): Map<UUID, String> {
        return data.associate {
            Pair(UUID.randomUUID(), fill(template, it))
        }
    }

    private fun fill(template: String, data: Map<String, Any>): String {
        var result = template
        data.forEach {
            result = result.replace("{{${it.key}}}", it.value.toString())
        }

        return result
    }
}

interface PromptClient {
    suspend fun sendRequest(id: UUID, req: String): String
}


class EmulatedClient : PromptClient {
    override suspend fun sendRequest(id: UUID, req: String): String {
        println("Doing sendRequest with id: $id")
        val time = (50..1000).random().toLong()
        delay(time)
        println("Done for $id")
        return getRandomString(time)
    }

    private fun getRandomString(length: Long): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}

fun main() = runBlocking {
    val generator = Generator()
    val data = listOf(
        mapOf("name" to "Alice", "subject" to "Kotlin", "time" to 10),
        mapOf("name" to "Bob", "subject" to "Rust", "time" to 20),
        mapOf("name" to "Charlie", "subject" to "Go", "time" to 30),
    )
    val template = "Hello, I'm {{name}}, please help me in {{subject}} task. I have {{time}} minutes to answer."

    val prompts = generator.generate(template, data)
    println("Generated Prompts:")
    prompts.forEach { println("${it.key}: ${it.value}") }

    val client = EmulatedClient()
    val results = prompts.map {
        async {
            Pair(it.key, client.sendRequest(it.key, it.value))
        }
    }.awaitAll()

    println("\nResults:")
    results.forEach { (id, response) ->
        println("$id -> $response")
    }
}


