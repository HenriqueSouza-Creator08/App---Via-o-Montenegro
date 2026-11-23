package com.example.data.network

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

data class SyncSnapshot(
    val escalas: List<Escala> = emptyList(),
    val motoristas: List<Motorista> = emptyList(),
    val relatorios: List<Relatorio> = emptyList(),
    val prints: List<PrintViagem> = emptyList(),
    val comunicados: List<Comunicado> = emptyList(),
    val ocorrencias: List<Ocorrencia> = emptyList(),
    val justificativas: List<Justificativa> = emptyList(),
    val frota: List<VeiculoFrota> = emptyList(),
    val pontos: List<PontoRegistro> = emptyList(),
    val errors: List<String> = emptyList()
)

class SupabaseApi {
    companion object {
        private const val BASE_URL = "https://peacycxxkbmryvqakxzs.supabase.co/rest/v1"
        private const val API_KEY = "sb_publishable_cWapXMTPZYvX0gCwUKOULg_f_iCQCcU"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val imageCache = ConcurrentHashMap<String, String>()

    fun getCachedImage(table: String, id: String): String? = imageCache["$table:$id"]

    fun cacheImage(table: String, id: String, dataUri: String) {
        imageCache["$table:$id"] = dataUri
    }

    private fun baseRequest(url: String): Request.Builder =
        Request.Builder()
            .url(url)
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .header("Accept", "application/json")

    private suspend fun executeGet(table: String, params: Map<String, String>): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val urlBuilder = "$BASE_URL/$table".toHttpUrl().newBuilder()
                params.forEach { (k, v) -> urlBuilder.addQueryParameter(k, v) }
                val req = baseRequest(urlBuilder.build().toString()).get().build()
                client.newCall(req).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) throw IllegalStateException(parseError(body, resp.code))
                    body
                }
            }
        }

    private suspend fun <T> fetchWithRetry(
        table: String,
        params: Map<String, String>,
        decode: (String) -> List<T>
    ): Result<List<T>> {
        var res = executeGet(table, params)
        if (res.isFailure) {
            delay(800)
            res = executeGet(table, params)
        }
        return res.mapCatching { decode(it) }
    }

    suspend fun loginMotorista(email: String): Result<Motorista?> = withContext(Dispatchers.IO) {
        executeGet(
            "motoristas",
            mapOf("select" to "*", "email" to "eq.${email.trim().lowercase()}")
        ).mapCatching { body ->
            json.decodeFromString<List<Motorista>>(body).firstOrNull()
        }
    }

    suspend fun syncAll(): SyncSnapshot = coroutineScope {
        val escalasDef = async {
            fetchWithRetry("escalas", mapOf("select" to "*")) {
                json.decodeFromString<List<Escala>>(it)
            }
        }
        val motoristasDef = async {
            fetchWithRetry("motoristas", mapOf("select" to "id,nome,email")) {
                json.decodeFromString<List<Motorista>>(it)
            }
        }
        val relatoriosDef = async {
            fetchWithRetry(
                "relatorios",
                mapOf(
                    "select" to "id,created_at,autor,autor_email,linha,prefixo,catracados,meia,km,data_br,hora_br",
                    "order" to "created_at.desc"
                )
            ) { json.decodeFromString<List<Relatorio>>(it) }
        }
        val printsDef = async {
            fetchWithRetry(
                "prints",
                mapOf(
                    "select" to "id,created_at,motorista,autor_email,estagio,linha,prefixo,quilometragem,data_br,hora_br",
                    "order" to "created_at.desc",
                    "limit" to "60"
                )
            ) { json.decodeFromString<List<PrintViagem>>(it) }
        }
        val comunicadosDef = async {
            fetchWithRetry(
                "comunicados",
                mapOf("select" to "*", "order" to "created_at.desc", "limit" to "100")
            ) { json.decodeFromString<List<Comunicado>>(it) }
        }
        val ocorrenciasDef = async {
            fetchWithRetry(
                "ocorrencias",
                mapOf("select" to "*", "order" to "created_at.desc", "limit" to "200")
            ) { json.decodeFromString<List<Ocorrencia>>(it) }
        }
        val justificativasDef = async {
            fetchWithRetry(
                "justificativas",
                mapOf("select" to "*", "order" to "created_at.desc", "limit" to "200")
            ) { json.decodeFromString<List<Justificativa>>(it) }
        }
        val frotaDef = async {
            fetchWithRetry(
                "frota",
                mapOf("select" to "*", "order" to "created_at.desc")
            ) { json.decodeFromString<List<VeiculoFrota>>(it) }
        }
        val pontosDef = async {
            fetchWithRetry(
                "pontos",
                mapOf("select" to "*", "order" to "created_at.desc", "limit" to "300")
            ) { json.decodeFromString<List<PontoRegistro>>(it) }
        }

        val errors = mutableListOf<String>()
        fun <T> unwrap(name: String, r: Result<List<T>>): List<T> =
            r.getOrElse {
                errors += "$name (${it.message ?: "erro"})"
                emptyList()
            }

        SyncSnapshot(
            escalas = unwrap("escalas", escalasDef.await()),
            motoristas = unwrap("motoristas", motoristasDef.await()),
            relatorios = unwrap("relatorios", relatoriosDef.await()),
            prints = unwrap("prints", printsDef.await()),
            comunicados = unwrap("comunicados", comunicadosDef.await()),
            ocorrencias = unwrap("ocorrencias", ocorrenciasDef.await()),
            justificativas = unwrap("justificativas", justificativasDef.await()),
            frota = unwrap("frota", frotaDef.await()),
            pontos = unwrap("pontos", pontosDef.await()),
            errors = errors
        )
    }

    suspend fun fetchLazyImage(table: String, id: String, column: String): String {
        val key = "$table:$id"
        imageCache[key]?.let { return it }
        val result = executeGet(
            table,
            mapOf("select" to column, "id" to "eq.$id", "limit" to "1")
        )
        return result.fold(
            onSuccess = { body ->
                val value = runCatching {
                    val arr = json.parseToJsonElement(body).jsonArray
                    val obj = arr.firstOrNull()?.jsonObject
                    obj?.get(column)?.jsonPrimitive?.contentOrNull.orEmpty()
                }.getOrDefault("")
                imageCache[key] = value
                value
            },
            onFailure = { "" }
        )
    }

    suspend inline fun <reified T> insert(table: String, payload: T): Result<Unit> =
        insertRaw(table, json.encodeToString(payload))

    suspend fun insertRaw(table: String, jsonBody: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val req = baseRequest("$BASE_URL/$table")
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .post(jsonBody.toRequestBody(JSON_MEDIA))
                    .build()
                client.newCall(req).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) throw IllegalStateException(parseError(body, resp.code))
                }
            }
        }

    suspend fun updateMotoristaProfile(id: String, payload: MotoristaProfileUpdate): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "$BASE_URL/motoristas".toHttpUrl().newBuilder()
                    .addQueryParameter("id", "eq.$id")
                    .build()
                val req = baseRequest(url.toString())
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .patch(json.encodeToString(payload).toRequestBody(JSON_MEDIA))
                    .build()
                client.newCall(req).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) throw IllegalStateException(parseError(body, resp.code))
                }
            }
        }

    suspend fun delete(table: String, id: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "$BASE_URL/$table".toHttpUrl().newBuilder()
                    .addQueryParameter("id", "eq.$id")
                    .build()
                val req = baseRequest(url.toString()).delete().build()
                client.newCall(req).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) throw IllegalStateException(parseError(body, resp.code))
                }
            }
        }

    private fun parseError(body: String, code: Int): String {
        return runCatching {
            val obj: JsonObject = json.parseToJsonElement(body).jsonObject
            obj["message"]?.jsonPrimitive?.contentOrNull
                ?: obj["error"]?.jsonPrimitive?.contentOrNull
                ?: "HTTP $code"
        }.getOrDefault(if (body.isNotBlank()) body.take(160) else "HTTP $code")
    }
}
