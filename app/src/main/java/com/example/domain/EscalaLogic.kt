package com.example.domain

import com.example.data.model.Escala
import com.example.data.model.Motorista
import com.example.data.model.Relatorio
import java.text.Normalizer
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class EscalaComPeriodo(
    val escala: Escala,
    val inicio: LocalDate,
    val fim: LocalDate
)

data class ConflitoInfo(
    val tipo: String, // "motorista" or "carro"
    val carros: List<String> = emptyList()
)

data class SemanaAnalysis(
    val marcacoes: Map<String, Set<String>>,
    val mensagens: List<String>
)

data class ContabilidadeStats(
    val totalCatracados: Int = 0,
    val totalKm: Double = 0.0,
    val faturamentoTotal: Double = 0.0,
    val mediaMensal: Double = 0.0,
    val projecaoAnual: Double = 0.0,
    val destaqueMotorista: String = "—",
    val totalViagens: Int = 0
)

object EscalaLogic {
    private val PT_BR = Locale("pt", "BR")
    val ZONE_BRASILIA: ZoneId = ZoneId.of("America/Sao_Paulo")
    private val FMT_DM = DateTimeFormatter.ofPattern("dd/MM", PT_BR)
    private val FMT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR)
    private val FMT_HORA = DateTimeFormatter.ofPattern("HH:mm", PT_BR)
    private val FMT_RELOGIO = DateTimeFormatter.ofPattern("HH:mm:ss", PT_BR)
    private val FMT_EXTENSO = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", PT_BR)
    private val PERIODO_REGEX = Regex("""(\d{2})/(\d{2})/(\d{4}).*?(\d{2})/(\d{2})/(\d{4})""")
    private val RANGE_REGEX = Regex("""(\d+)\s*(?:ao?|-|até)\s*(\d+)""", RegexOption.IGNORE_CASE)
    private val NUM_REGEX = Regex("""\d+""")

    private val SLOTS = mapOf(
        "Manhã" to setOf("M"),
        "Tarde" to setOf("T"),
        "Noite" to setOf("N"),
        "Integral" to setOf("M", "T"),
        "Final de Semana" to setOf("M", "T", "N")
    )

    private val DIAS_ABREV = mapOf(
        DayOfWeek.SUNDAY to "Dom",
        DayOfWeek.MONDAY to "Seg",
        DayOfWeek.TUESDAY to "Ter",
        DayOfWeek.WEDNESDAY to "Qua",
        DayOfWeek.THURSDAY to "Qui",
        DayOfWeek.FRIDAY to "Sex",
        DayOfWeek.SATURDAY to "Sáb"
    )

    fun semAcento(s: String?): String {
        if (s.isNullOrBlank()) return ""
        val normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
        return normalized.replace(Regex("""[\u0300-\u036f]"""), "").lowercase(PT_BR).trim()
    }

    fun saudacaoAtual(): String {
        val h = ZonedDateTime.now(ZONE_BRASILIA).hour
        return when {
            h in 6..11 -> "Bom dia"
            h in 12..17 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    fun dataExtensoAtual(): String =
        ZonedDateTime.now(ZONE_BRASILIA).format(FMT_EXTENSO)

    fun relogioBrasiliaAtual(): String =
        ZonedDateTime.now(ZONE_BRASILIA).format(FMT_RELOGIO)

    fun agoraBr(): Pair<String, String> {
        val now = ZonedDateTime.now(ZONE_BRASILIA)
        return now.format(FMT_BR) to now.format(FMT_HORA)
    }

    fun hojeLocalDate(): LocalDate = ZonedDateTime.now(ZONE_BRASILIA).toLocalDate()

    fun hojeBr(): String = hojeLocalDate().format(FMT_BR)

    fun formatDM(d: LocalDate): String = d.format(FMT_DM)

    fun formatBR(d: LocalDate): String = d.format(FMT_BR)

    fun diaAbrev(d: LocalDate): String = DIAS_ABREV[d.dayOfWeek] ?: ""

    fun inicioSemana(d: LocalDate): LocalDate =
        d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun dataBrParaExibicao(iso: String): String =
        iso.split("-").reversed().joinToString("/")

    fun periodoDe(e: Escala): Pair<LocalDate, LocalDate>? {
        val m = PERIODO_REGEX.find(e.data) ?: return null
        val g = m.groupValues
        return runCatching {
            val ini = LocalDate.of(g[3].toInt(), g[2].toInt(), g[1].toInt())
            val fim = LocalDate.of(g[6].toInt(), g[5].toInt(), g[4].toInt())
            ini to fim
        }.getOrNull()
    }

    fun escalaDeHoje(e: Escala, hoje: LocalDate = hojeLocalDate()): Boolean {
        val (ini, fim) = periodoDe(e) ?: return false
        return !hoje.isBefore(ini) && !hoje.isAfter(fim)
    }

    fun carrosDe(txt: String?): Set<String> {
        val out = linkedSetOf<String>()
        val source = txt.orEmpty()
        val resto = RANGE_REGEX.replace(source) { match ->
            val a = match.groupValues[1].toIntOrNull()
            val b = match.groupValues[2].toIntOrNull()
            if (a != null && b != null && b >= a && (b - a) <= 300) {
                for (n in a..b) out.add(n.toString())
            }
            " "
        }
        NUM_REGEX.findAll(resto).forEach { match ->
            val n = match.value.toIntOrNull()
            if (n != null) out.add(n.toString())
        }
        return out
    }

    fun conflitoEntre(a: Escala, b: Escala): ConflitoInfo? {
        if (a.id.isNotBlank() && a.id == b.id) return null
        val sa = SLOTS[a.turno] ?: setOf("M", "T", "N")
        val sb = SLOTS[b.turno] ?: setOf("M", "T", "N")
        if (sa.none { it in sb }) return null
        val ma = semAcento(a.motorista)
        if (ma.isNotEmpty() && ma == semAcento(b.motorista)) {
            return ConflitoInfo(tipo = "motorista")
        }
        val cb = carrosDe(b.prefixo)
        val comum = carrosDe(a.prefixo).filter { it in cb }
        return if (comum.isNotEmpty()) ConflitoInfo(tipo = "carro", carros = comum) else null
    }

    fun analisarSemana(dias: List<LocalDate>, ativos: List<EscalaComPeriodo>): SemanaAnalysis {
        val marcacoes = mutableMapOf<String, MutableSet<String>>()
        val grupos = linkedMapOf<String, MutableList<String>>()
        for (d in dias) {
            val doDia = ativos.filter { !d.isBefore(it.inicio) && !d.isAfter(it.fim) }
            for (i in doDia.indices) {
                for (j in i + 1 until doDia.size) {
                    val c = conflitoEntre(doDia[i].escala, doDia[j].escala) ?: continue
                    val eA = doDia[i].escala
                    val eB = doDia[j].escala
                    listOf(eA, eB).forEach { esc ->
                        if (esc.id.isNotBlank()) {
                            marcacoes.getOrPut(esc.id) { mutableSetOf() }.add(c.tipo)
                        }
                    }
                    val base = if (c.tipo == "motorista") {
                        "${eA.motorista} aparece em 2 escalas (${eA.turno} e ${eB.turno})"
                    } else {
                        "Carro ${c.carros.joinToString(", ")} em 2 escalas (${eA.motorista} e ${eB.motorista})"
                    }
                    grupos.getOrPut(base) { mutableListOf() }.add(diaAbrev(d))
                }
            }
        }
        val msgs = grupos.map { (base, ds) -> "$base — ${ds.joinToString(", ")}" }
        return SemanaAnalysis(marcacoes = marcacoes, mensagens = msgs)
    }

    fun conflitosAoPublicar(
        nova: Escala,
        ini: LocalDate,
        fim: LocalDate,
        existentes: List<Escala>
    ): List<String> {
        val ativos = existentes.mapNotNull { e -> periodoDe(e)?.let { EscalaComPeriodo(e, it.first, it.second) } }
        val grupos = linkedMapOf<String, MutableList<String>>()
        var d = ini
        var count = 0
        while (!d.isAfter(fim) && count < 366) {
            for (x in ativos) {
                if (d.isBefore(x.inicio) || d.isAfter(x.fim)) continue
                val c = conflitoEntre(nova, x.escala) ?: continue
                val base = if (c.tipo == "motorista") {
                    "${x.escala.motorista} já está escalado (${x.escala.turno}, linha ${x.escala.linha}, carro ${x.escala.prefixo})"
                } else {
                    "Carro ${c.carros.joinToString(", ")} já está com ${x.escala.motorista} (${x.escala.turno}, linha ${x.escala.linha})"
                }
                grupos.getOrPut(base) { mutableListOf() }.add(formatDM(d))
            }
            d = d.plusDays(1)
            count++
        }
        return grupos.map { (base, ds) ->
            val range = if (ds.size > 3) "${ds.first()} a ${ds.last()}" else ds.joinToString(", ")
            "$base em $range"
        }
    }

    fun faturamento(r: Relatorio): Double = r.catracados * 5.40 + r.meia * 1.50

    fun calcularContabilidade(relatorios: List<Relatorio>, motoristas: List<Motorista>): ContabilidadeStats {
        var inteira = 0
        var meia = 0
        var km = 0.0
        var total = 0.0
        val meses = mutableMapOf<String, Double>()
        val porMotorista = mutableMapOf<String, Int>()
        for (r in relatorios) {
            inteira += r.catracados
            meia += r.meia
            km += r.km
            val f = faturamento(r)
            total += f
            val mes = r.createdAt.take(7)
            meses[mes] = (meses[mes] ?: 0.0) + f
            if (r.autor.isNotBlank()) {
                porMotorista[r.autor] = (porMotorista[r.autor] ?: 0) + 1
            }
        }
        val media = if (meses.isNotEmpty()) total / meses.size else 0.0
        val top = porMotorista.entries.maxByOrNull { it.value }?.key
            ?: motoristas.firstOrNull()?.nome
            ?: "—"
        return ContabilidadeStats(
            totalCatracados = inteira + meia,
            totalKm = km,
            faturamentoTotal = total,
            mediaMensal = media,
            projecaoAnual = media * 12.0,
            destaqueMotorista = top,
            totalViagens = relatorios.size
        )
    }

    fun formatBrl(v: Double): String =
        NumberFormat.getCurrencyInstance(PT_BR).format(v)

    fun formatCompactBrl(v: Double): String {
        if (v <= 0.0) return ""
        return when {
            v >= 1_000_000 -> String.format(PT_BR, "R$ %.1f mi", v / 1_000_000.0)
            v >= 1_000 -> String.format(PT_BR, "R$ %.1f mil", v / 1_000.0)
            else -> formatBrl(v)
        }
    }
}
