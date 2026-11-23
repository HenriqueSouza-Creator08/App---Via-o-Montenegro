package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EscalaInsert
import com.example.domain.EscalaComPeriodo
import com.example.domain.EscalaLogic
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DeleteChipButton
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.MontenegroCard
import com.example.ui.state.MainUiState
import com.example.ui.theme.BrandPalette
import com.example.ui.theme.LocalMontenegroColors
import java.time.LocalDate

@Composable
fun EscalasScreen(
    state: MainUiState,
    onChangeWeek: (Int) -> Unit,
    onToggleShowAllDrivers: (Boolean) -> Unit,
    onCopyPreviousWeek: (List<EscalaInsert>) -> Unit,
    onQuickAddFromGrid: (String, LocalDate) -> Unit,
    onDeleteEscala: (String) -> Unit
) {
    val ext = LocalMontenegroColors.current
    val dias = remember(state.semanaRef) { (0L..6L).map { state.semanaRef.plusDays(it) } }
    val hoje = EscalaLogic.hojeLocalDate()
    val ativos = remember(state.escalas) {
        state.escalas.mapNotNull { e -> EscalaLogic.periodoDe(e)?.let { EscalaComPeriodo(e, it.first, it.second) } }
    }
    val analysis = remember(dias, ativos) { EscalaLogic.analisarSemana(dias, ativos) }

    val linhas = remember(state.motoristas, ativos, dias, state.showAllDriversInGrid) {
        val mapa = linkedMapOf<String, String>()
        state.motoristas.forEach { m ->
            val k = EscalaLogic.semAcento(m.nome)
            if (k.isNotEmpty()) mapa[k] = m.nome
        }
        ativos.forEach { x ->
            val k = EscalaLogic.semAcento(x.escala.motorista)
            if (k.isNotEmpty() && k !in mapa) mapa[k] = x.escala.motorista
        }
        val rows = mapa.map { (k, nome) ->
            val cells = dias.map { d ->
                ativos.filter { EscalaLogic.semAcento(it.escala.motorista) == k && !d.isBefore(it.inicio) && !d.isAfter(it.fim) }
            }
            Triple(k, nome, cells)
        }
        val filtered = if (state.showAllDriversInGrid) rows else rows.filter { row -> row.third.any { it.isNotEmpty() } }
        filtered.sortedBy { it.second.lowercase() }
    }

    var pendingCopyList by remember { mutableStateOf<List<EscalaInsert>?>(null) }
    var deleteId by remember { mutableStateOf<String?>(null) }

    val searchLower = state.searchQuery.trim().lowercase()
    val filteredEscalas = remember(state.escalas, searchLower) {
        if (searchLower.isEmpty()) state.escalas
        else state.escalas.filter {
            "${it.data} ${it.turno} ${it.motorista} ${it.linha} ${it.prefixo}".lowercase().contains(searchLower)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Header + week nav
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Escalas da Semana", style = MaterialTheme.typography.headlineSmall)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalIconButton(
                    onClick = { onChangeWeek(-1) },
                    modifier = Modifier.size(40.dp).testTag("prev_week_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Semana anterior")
                }
                Text(
                    text = "${EscalaLogic.formatDM(dias.first())} – ${EscalaLogic.formatDM(dias.last())}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 90.dp)
                )
                FilledTonalIconButton(
                    onClick = { onChangeWeek(1) },
                    modifier = Modifier.size(40.dp).testTag("next_week_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Próxima semana")
                }
                OutlinedButton(
                    onClick = { onChangeWeek(0) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(40.dp).testTag("today_week_button")
                ) {
                    Text("Hoje", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.isAdmin) {
                Button(
                    onClick = {
                        val pIni = state.semanaRef.minusDays(7)
                        val pFim = state.semanaRef.minusDays(1)
                        val novas = mutableListOf<EscalaInsert>()
                        state.escalas.forEach { e ->
                            val per = EscalaLogic.periodoDe(e) ?: return@forEach
                            if (per.second.isBefore(pIni) || per.first.isAfter(pFim)) return@forEach
                            val ni = (if (per.first.isBefore(pIni)) pIni else per.first).plusDays(7)
                            val nf = (if (per.second.isAfter(pFim)) pFim else per.second).plusDays(7)
                            val jaCobre = state.escalas.any { x ->
                                val q = EscalaLogic.periodoDe(x)
                                q != null &&
                                    x.motorista == e.motorista &&
                                    x.turno == e.turno &&
                                    x.linha == e.linha &&
                                    x.prefixo == e.prefixo &&
                                    !q.first.isAfter(ni) && !q.second.isBefore(nf)
                            }
                            if (!jaCobre) {
                                novas.add(
                                    EscalaInsert(
                                        data = "${EscalaLogic.formatBR(ni)} até ${EscalaLogic.formatBR(nf)}",
                                        turno = e.turno,
                                        motorista = e.motorista,
                                        linha = e.linha,
                                        prefixo = e.prefixo,
                                        tipo = "Período"
                                    )
                                )
                            }
                        }
                        if (novas.isEmpty()) {
                            onCopyPreviousWeek(emptyList())
                        } else {
                            pendingCopyList = novas
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalBlue),
                    modifier = Modifier.height(44.dp).testTag("copy_prev_week_button")
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copiar semana passada", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleShowAllDrivers(!state.showAllDriversInGrid) }
                    .padding(end = 8.dp)
            ) {
                Checkbox(
                    checked = state.showAllDriversInGrid,
                    onCheckedChange = onToggleShowAllDrivers,
                    modifier = Modifier.testTag("show_all_drivers_checkbox")
                )
                Text(
                    text = "Mostrar todos os motoristas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Conflict Alert Banner
        if (analysis.mensagens.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = ext.dangerContainer,
                border = BorderStroke(1.dp, ext.dangerContent.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = ext.dangerContent, modifier = Modifier.size(16.dp))
                        Text(
                            text = "${analysis.mensagens.size} conflito(s) nesta semana",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = ext.dangerContent
                        )
                    }
                    analysis.mensagens.take(6).forEach { msg ->
                        Text("• $msg", fontSize = 11.sp, color = ext.dangerContent)
                    }
                    if (analysis.mensagens.size > 6) {
                        Text("• e mais ${analysis.mensagens.size - 6}...", fontSize = 11.sp, color = ext.dangerContent)
                    }
                }
            }
        }

        // Weekly Grid
        MontenegroCard(contentPadding = PaddingValues(10.dp), modifier = Modifier.fillMaxWidth()) {
            if (linhas.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Rounded.CalendarMonth,
                    title = "Semana livre",
                    subtitle = "Nenhuma escala nesta semana." + if (state.isAdmin) " Use \"Copiar semana passada\" ou publique uma escala na tela inicial." else ""
                )
            } else {
                val myNorm = EscalaLogic.semAcento(state.session?.displayName)
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.horizontalScroll(scrollState)) {
                    // Header Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "MOTORISTA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ext.mutedText,
                            modifier = Modifier.width(120.dp).padding(horizontal = 8.dp)
                        )
                        dias.forEach { d ->
                            val isToday = d == hoje
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isToday) ext.limePillBackground else Color.Transparent,
                                modifier = Modifier.width(104.dp).padding(horizontal = 2.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = EscalaLogic.diaAbrev(d).uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) ext.limePillContent else ext.mutedText
                                    )
                                    Text(
                                        text = EscalaLogic.formatDM(d),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isToday) ext.limePillContent else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Driver Rows
                    linhas.forEach { (k, nome, cells) ->
                        val isMe = myNorm.isNotEmpty() && (k == myNorm || myNorm.contains(k))
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Text(
                                text = nome,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isMe) ext.limePillContent else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(120.dp).padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                            cells.forEachIndexed { idx, itens ->
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.width(104.dp).padding(horizontal = 2.dp)
                                ) {
                                    if (itens.isNotEmpty()) {
                                        itens.forEach { x ->
                                            val hasConflict = analysis.marcacoes[x.escala.id]?.isNotEmpty() == true
                                            val bg = if (hasConflict) ext.dangerContainer else ext.limePillBackground
                                            val fg = if (hasConflict) ext.dangerContent else ext.limePillContent
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = bg,
                                                border = if (hasConflict) BorderStroke(1.dp, ext.dangerContent.copy(alpha = 0.5f)) else null,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                                    Text(
                                                        text = if (x.escala.turno == "Final de Semana") "FDS" else x.escala.turno.uppercase(),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = fg
                                                    )
                                                    Text(
                                                        text = "L ${x.escala.linha} · ${x.escala.prefixo}",
                                                        fontSize = 10.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = fg
                                                    )
                                                }
                                            }
                                        }
                                    } else if (state.isAdmin) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, ext.cardBorder, RoundedCornerShape(12.dp))
                                                .clickable { onQuickAddFromGrid(nome, dias[idx]) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.Add,
                                                contentDescription = "Adicionar escala para $nome",
                                                tint = ext.mutedText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(Modifier.height(38.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lista de escalas
        Text("Lista de escalas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp))
        if (filteredEscalas.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Rounded.CalendarMonth,
                title = "Nenhuma escala por aqui",
                subtitle = "Quando o administrador publicar uma escala, ela aparece nesta tela."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredEscalas.forEach { e ->
                    MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "${e.data} (${e.turno})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ext.blueAccent
                                )
                                Text(
                                    text = "Motorista: ${e.motorista}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Linha: ${e.linha} | Carro: ${e.prefixo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (state.isAdmin) {
                                DeleteChipButton(onClick = { deleteId = e.id })
                            }
                        }
                    }
                }
            }
        }
    }

    pendingCopyList?.let { novas ->
        val pIni = state.semanaRef.minusDays(7)
        val pFim = state.semanaRef.minusDays(1)
        AlertDialog(
            onDismissRequest = { pendingCopyList = null },
            title = { Text("Copiar semana passada") },
            text = {
                Text(
                    "Copiar ${novas.size} escala(s) de ${EscalaLogic.formatDM(pIni)}–${EscalaLogic.formatDM(pFim)} " +
                        "para ${EscalaLogic.formatDM(state.semanaRef)}–${EscalaLogic.formatDM(state.semanaRef.plusDays(6))}?"
                )
            },
            confirmButton = {
                Button(onClick = {
                    val list = novas
                    pendingCopyList = null
                    onCopyPreviousWeek(list)
                }) { Text("Copiar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingCopyList = null }) { Text("Cancelar") }
            }
        )
    }

    deleteId?.let { id ->
        ConfirmDeleteDialog(
            message = "Tem certeza que deseja excluir esta escala?",
            onConfirm = { onDeleteEscala(id); deleteId = null },
            onDismiss = { deleteId = null }
        )
    }
}
