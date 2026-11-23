package com.example.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

object FlexibleIdSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        return if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonPrimitive) element.contentOrNull ?: "" else element.toString()
        } else {
            decoder.decodeString()
        }
    }
}

@Serializable
data class UserSession(
    val email: String,
    val cargo: String, // "motorista" or "admin"
    val motoristaId: String? = null,
    val nomePersonalizado: String,
    val bio: String = "",
    val foto: String? = null
) {
    val isAdmin: Boolean get() = cargo == "admin"
    val displayName: String get() = nomePersonalizado.ifBlank { email }
}

@Serializable
data class Escala(
    @Serializable(with = FlexibleIdSerializer::class)
    val id: String = "",
    val data: String = "",
    val turno: String = "",
    val motorista: String = "",
    val linha: String = "",
    val prefixo: String = "",
    val tipo: String = "Período"
)

@Serializable
data class EscalaInsert(
    val data: String,
    val turno: String,
    val motorista: String,
    val linha: String,
    val prefixo: String,
    val tipo: String = "Período"
)

@Serializable
data class Motorista(
    @Serializable(with = FlexibleIdSerializer::class)
    val id: String = "",
    val nome: String = "",
    val email: String = "",
    val senha: String? = null,
    val foto: String? = null
)

@Serializable
data class MotoristaInsert(
    val nome: String,
    val email: String,
    val senha: String
)

@Serializable
data class MotoristaProfileUpdate(
    val nome: String,
    val foto: String? = null
)

@Serializable
data class Relatorio(
    @Serializable(with = FlexibleIdSerializer::class)
    val id: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val autor: String = "",
    @SerialName("autor_email") val autorEmail: String = "",
    val linha: String = "",
    val prefixo: String = "",
    val catracados: Int = 0,
    val meia: Int = 0,
    val km: Double = 0.0,
    @SerialName("url_imagem") val urlImagem: String? = null,
    @SerialName("data_br") val dataBr: String = "",
    @SerialName("hora_br") val horaBr: String = ""
)

@Serializable
data class RelatorioInsert(
    val autor: String,
    @SerialName("autor_email") val autorEmail: String,
    val linha: String,
    val prefixo: String,
    val catracados: Int,
    val meia: Int,
    val km: Double,
    @SerialName("url_imagem") val urlImagem: String? = null,
    @SerialName("data_br") val dataBr: String,
    @SerialName("hora_br") val horaBr: String
)

@Serializable
data class PrintViagem(
    @Serializable(with = FlexibleIdSerializer::class)
    val id: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val motorista: String = "",
    @SerialName("autor_email") val autorEmail: String = "",
    val estagio: String = "",
    val linha: String = "",
    val prefixo: String = "",
    val quilometragem: String = "",
    @SerialName("url_imagem") val urlImagem: String? = null,
    @SerialName("data_br") val dataBr: String = "",
    @SerialName("hora_br") val horaBr: String = ""
)

@Serializable
data class PrintInsert(
    val motorista: String,
    @SerialName("autor_email") val autorEmail: String,
    val estagio: String,
    val linha: String,
    val prefixo: String,
    val quilometragem: String,
    @SerialName("url_imagem") val urlImagem: String,
    @SerialName("data_br") val dataBr: String,
    @SerialName("hora_br") val horaBr: String
)

@Serializable
data class Comunicado(
    @Serializable(with = FlexibleIdSerializer::class)
    val id: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val titulo: String = "",
    val texto: String = "",
    @SerialName("data_br") val dataBr: String = "",
    @SerialName("hora_br") val horaBr: String = ""
)

@Serializable
data class ComunicadoInsert(
    val titulo: String,
    val texto: String,
    @SerialName("data_br") val dataBr: String,
    @SerialName("hora_br") val horaBr: String
)

@Serializable
data class Ocorrencia(
    @Serializable(with = FlexibleIdSerializer::class)
    val id: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val autor: String = "",
    @SerialName("autor_email") val autorEmail: String = "",
    val texto: String = "",
    @SerialName("data_br") val dataBr: String = "",
    @SerialName("hora_br") val horaBr: String = ""
)

@Serializable
data class OcorrenciaInsert(
    val autor: String,
    @SerialName("autor_email") val autorEmail: String,
    val texto: String,
    @SerialName("data_br") val dataBr: String,
    @SerialName("hora_br") val horaBr: String
)

@Serializable
data class Justificativa(
    @Serializable(with = FlexibleIdSerializer::class)
    val id: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val motorista: String = "",
    @SerialName("autor_email") val autorEmail: String = "",
    @SerialName("data_falta") val dataFalta: String = "",
    val motivo: String = ""
)

@Serializable
data class JustificativaInsert(
    val motorista: String,
    @SerialName("autor_email") val autorEmail: String,
    @SerialName("data_falta") val dataFalta: String,
    val motivo: String
)

@Serializable
data class VeiculoFrota(
    @Serializable(with = FlexibleIdSerializer::class)
    val id: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val modelo: String = "",
    val prefixo: String = "",
    val chassi: String = ""
)

@Serializable
data class FrotaInsert(
    val modelo: String,
    val prefixo: String,
    val chassi: String
)

@Serializable
data class PontoRegistro(
    @Serializable(with = FlexibleIdSerializer::class)
    val id: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val motorista: String = "",
    @SerialName("autor_email") val autorEmail: String = "",
    val tipo: String = "",
    @SerialName("data_br") val dataBr: String = "",
    @SerialName("hora_br") val horaBr: String = ""
)

@Serializable
data class PontoInsert(
    val motorista: String,
    @SerialName("autor_email") val autorEmail: String,
    val tipo: String,
    @SerialName("data_br") val dataBr: String,
    @SerialName("hora_br") val horaBr: String
)
