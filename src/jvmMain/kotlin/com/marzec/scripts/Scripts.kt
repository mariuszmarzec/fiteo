package com.marzec.scripts

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.html.*

// Generated with AI

val SCRIPT_A_PATH = "/root/gists/c9e375096f15fec5aa3419e6534b9374/vitalia.py"
val SCRIPT_B_PATH = "/root/gists/ecc444e68c45b7d7575e9d9bd8143b21/clean_listonic.py"

fun AuthenticationConfig.scriptsBasicAuthConfig() {
    basic("auth-basic") {
        realm = "Wybór i uruchomienie skryptu"
        validate { credentials ->
            if (credentials.name == "admin" && credentials.password == "Kalka123!") {
                UserIdPrincipal(credentials.name)
            } else {
                null
            }
        }
    }
}

fun Application.scripts() {
    routing {
        authenticate("auth-basic") {

            get("/scripts") {
                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        title { +"Wybór Skryptu Pythona" }
                        style {
                            unsafe {
                                raw("""
                                    body { font-family: Arial, sans-serif; margin: 20px; }
                                    button { 
                                    
                                    width: 300px; height: 150px; font-size: 30px; font-weight: bold; padding: 10px 20px; margin: 5px; cursor: pointer; }
                                    #output { margin-top: 20px; padding: 15px; border: 1px solid #ccc; white-space: pre-wrap; background-color: #f9f9f9; }
                                """)
                            }
                        }
                    }
                    body {
                        h1 { +"Panel Wyboru Skryptu" }

                        button(classes = "script-button") {
                            id = "btn-a"
                            onClick = "runScript('/api/run_A')"
                            +"Generuj listę zakupów"
                        }

                        button(classes = "script-button") {
                            id = "btn-b"
                            onClick = "runScript('/api/run_B')"
                            +"Wyczyść listę zakupów"
                        }

                        h2 { +"Wynik Skryptu:" }
                        pre { id = "output" } // Tutaj wstawimy wynik

                        script {
                            unsafe {
                                raw("""
                                    async function runScript(endpoint) {
                                        const outputElement = document.getElementById('output');
                                        outputElement.textContent = "Ładowanie... Proszę czekać.";
                                        
                                        try {
                                            const response = await fetch(endpoint, {
                                                method: 'GET' // Lub 'POST', jeśli zmienisz endpoint
                                                // W Basic Auth przeglądarka automatycznie dodaje nagłówki auth
                                            });

                                            const text = await response.text();
                                            
                                            if (response.ok) {
                                                outputElement.textContent = "SUKCES (Kod ${'$'}{response.status}):\n" + text;
                                                outputElement.style.backgroundColor = '#e6ffe6'; // Zielone tło
                                            } else {
                                                outputElement.textContent = "BŁĄD (Kod ${'$'}{response.status}):\n" + text;
                                                outputElement.style.backgroundColor = '#ffe6e6'; // Czerwone tło
                                            }

                                        } catch (error) {
                                            outputElement.textContent = "Krytyczny błąd połączenia: " + error.message;
                                            outputElement.style.backgroundColor = '#ffe6e6';
                                        }
                                    }
                                """)
                            }
                        }
                    }
                }
            }

            get("/api/run_A") {
                val result = executePythonScript(SCRIPT_A_PATH)
                if (result.isSuccess) {
                    call.respondText(result.output, status = HttpStatusCode.OK)
                } else {
                    call.respondText(result.output, status = HttpStatusCode.InternalServerError)
                }
            }

            get("/api/run_B") {
                val result = executePythonScript(SCRIPT_B_PATH)
                if (result.isSuccess) {
                    call.respondText(result.output, status = HttpStatusCode.OK)
                } else {
                    call.respondText(result.output, status = HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}


data class ScriptExecutionResult(val output: String, val isSuccess: Boolean)

fun executePythonScript(scriptPath: String): ScriptExecutionResult {
    val pythonExecutable = "python3"

    return try {
        val process = ProcessBuilder(pythonExecutable, scriptPath)
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        val output = process.inputStream.bufferedReader().use { it.readText() }

        if (exitCode == 0) {
            ScriptExecutionResult(output, true)
        } else {
            ScriptExecutionResult("BŁĄD SKRYPTU (Kod $exitCode):\n$output", false)
        }
    } catch (e: Exception) {
        ScriptExecutionResult("KRYTYCZNY BŁĄD SYSTEMU: ${e.message}", false)
    }
}