package com.marzec.scripts

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.html.InputType
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.onClick
import kotlinx.html.pre
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.unsafe
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

val SCRIPTS_DIR = "/root/shoppingListGenerator"
val SCRIPT_A_PATH = "$SCRIPTS_DIR/vitalia.py"
val SCRIPT_B_PATH = "$SCRIPTS_DIR/clean_listonic.py"
val SCRIPT_MEDIAN_PATH = "$SCRIPTS_DIR/median_list.py"

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
                                    .button-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-top: 10px; }
                                    .script-button { display: inline-flex; align-items: center; justify-content: center; width: 300px; height: 150px; font-size: 30px; font-weight: bold; padding: 10px 20px; margin: 0; box-sizing: border-box; vertical-align: middle; }
                                    #output { margin-top: 20px; padding: 15px; border: 1px solid #ccc; white-space: pre-wrap; background-color: #f9f9f9; }
                                    .date-container { margin-bottom: 20px; padding: 10px; border: 1px solid #ddd; display: inline-block; }
                                    .date-input { font-size: 18px; padding: 5px; margin: 5px; }
                                    .clear-btn { width: auto; height: auto; font-size: 16px; padding: 5px 10px; margin-left: 10px; }
                                """)
                            }
                        }
                    }
                    body {
                        h1 { +"Panel Wyboru Skryptu" }

                        div(classes = "date-container") {
                            // --- POPRAWKA TUTAJ: Najpierw atrybut htmlFor, potem tekst ---
                            label { htmlFor = "date-from"; +"Data od: " }
                            input(type = InputType.date, classes = "date-input") { id = "date-from" }

                            // --- POPRAWKA TUTAJ: Najpierw atrybut htmlFor, potem tekst ---
                            label { htmlFor = "date-to"; +" Data do: " }
                            input(type = InputType.date, classes = "date-input") { id = "date-to" }

                            button(classes = "clear-btn") {
                                onClick = "clearDates()"
                                +"Wyczyść daty"
                            }
                        }
                        br {}

                        div(classes = "button-row") {
                            button(classes = "script-button") {
                                id = "btn-a"
                                onClick = "runScriptA()"
                                +"Generuj listę zakupów"
                            }

                            button(classes = "script-button") {
                                id = "btn-b"
                                onClick = "runScript('/api/run_B')"
                                +"Wyczyść listę zakupów"
                            }

                            // New button for median list generation
                            button(classes = "script-button") {
                                id = "btn-median"
                                onClick = "runScript('/api/run_median')"
                                +"Generuj listę na podstawie mediany"
                            }
                        }

                        div(classes = "button-row") {
                            label {
                                input(type = InputType.checkBox, classes = "date-input") { id = "only-todo" }
                                +" only-todo"
                            }
                        }

                        h2 { +"Wynik Skryptu:" }
                        pre { id = "output" }

                        script {
                            unsafe {
                                raw("""
                                    function clearDates() {
                                        document.getElementById('date-from').value = '';
                                        document.getElementById('date-to').value = '';
                                    }

                                    function runScriptA() {
                                        const dateFrom = document.getElementById('date-from').value;
                                        const dateTo = document.getElementById('date-to').value;

                                        let url = '/api/run_A';
                                        const params = [];
                                        if (dateFrom) params.push('dateFrom=' + dateFrom);
                                        if (dateTo) params.push('dateTo=' + dateTo);

                                        if (params.length > 0) {
                                            url += '?' + params.join('&');
                                        }

                                        const onlyTodo = document.getElementById('only-todo').checked;
                                        if (onlyTodo) {
                                            url += (url.includes('?') ? '&' : '?') + 'onlyTodo=true';
                                        }

                                        runScript(url);
                                    }

                                    async function runScript(endpoint) {
                                        const outputElement = document.getElementById('output');
                                        outputElement.textContent = "Ładowanie... Proszę czekać.";
                                        
                                        try {
                                            const response = await fetch(endpoint, {
                                                method: 'GET'
                                            });

                                            const text = await response.text();
                                            
                                            // POPRAWKA JS: Używamy standardowego łączenia stringów
                                            if (response.ok) {
                                                outputElement.textContent = "SUKCES (Kod " + response.status + "):\n" + text;
                                                outputElement.style.backgroundColor = '#e6ffe6';
                                            } else {
                                                outputElement.textContent = "BŁĄD (Kod " + response.status + "):\n" + text;
                                                outputElement.style.backgroundColor = '#ffe6e6';
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
            // ... reszta endpointów /api/run_A i /api/run_B bez zmian ...
            get("/api/run_A") {
                val dateFrom = call.request.queryParameters["dateFrom"]
                val dateTo = call.request.queryParameters["dateTo"]
                val onlyTodo = call.request.queryParameters["onlyTodo"]
                val args = try {
                    buildRunAScriptArguments(dateFrom, dateTo, onlyTodo)
                } catch (exception: IllegalArgumentException) {
                    call.respondText(
                        exception.message ?: "Dates must use YYYY-MM-DD and dateTo must not precede dateFrom.",
                        status = HttpStatusCode.BadRequest
                    )
                    return@get
                }

                val result = executePythonScript(SCRIPT_A_PATH, args)
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

            // Endpoint for median list generation script
            get("/api/run_median") {
                val result = executePythonScript(SCRIPT_MEDIAN_PATH)
                if (result.isSuccess) {
                    call.respondText(result.output, status = HttpStatusCode.OK)
                } else {
                    call.respondText(result.output, status = HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

private val SCRIPT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

/**
 * Converts the scripts-panel query parameters into the positional date arguments
 * and flags accepted by vitalia.py.
 *
 * If only dateFrom is provided, vitalia.py uses its existing seven-day default.
 * If only dateTo is provided, use the seven-day window ending on dateTo.
 */
internal fun buildRunAScriptArguments(
    dateFrom: String?,
    dateTo: String?,
    onlyTodo: String?
): List<String> {
    fun parseDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDate.parse(value)
        } catch (exception: DateTimeParseException) {
            throw IllegalArgumentException("Dates must use YYYY-MM-DD format.", exception)
        }
    }

    val from = parseDate(dateFrom)
    val to = parseDate(dateTo)

    require(from == null || to == null || !to.isBefore(from)) {
        "dateTo must be on or after dateFrom."
    }

    val args = mutableListOf<String>()
    when {
        from != null && to != null -> {
            args += from.format(SCRIPT_DATE_FORMATTER)
            args += to.format(SCRIPT_DATE_FORMATTER)
        }
        from != null -> args += from.format(SCRIPT_DATE_FORMATTER)
        to != null -> {
            args += to.minusDays(7).format(SCRIPT_DATE_FORMATTER)
            args += to.format(SCRIPT_DATE_FORMATTER)
        }
    }

    if (onlyTodo.equals("true", ignoreCase = true)) {
        args += "--only-todo"
    }

    return args
}

data class ScriptExecutionResult(val output: String, val isSuccess: Boolean)

suspend fun executePythonScript(scriptPath: String, args: List<String> = emptyList()): ScriptExecutionResult {
    return withContext(Dispatchers.IO) {
        val pythonExecutable = "python3"
        val command = mutableListOf(pythonExecutable, scriptPath)
        command.addAll(args)

        try {
            val process = ProcessBuilder(command)
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
}