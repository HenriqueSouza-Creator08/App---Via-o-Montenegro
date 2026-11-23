package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Escala
import com.example.domain.EscalaLogic
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DeleteChipButton
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.MontenegroCard
import com.example.ui.components.ShimmerBox
import com.example.ui.state.AppTab
import com.example.ui.state.MainUiState
import com.example.ui.theme.BrandPalette
import com.example.ui.theme.LocalMontenegroColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    state: MainUiState,
    isExpanded: Boolean,
    onNavigateTab: (AppTab) -> Unit,
    onDeletePonto: (String) -> Unit,
    onPublishEscala: (LocalDate, LocalDate, String, String, String, String, () -> Unit) -> Unit,
    onConsumeDraft: () -> Unit
) {
    val ext = LocalMontenegroColors.current
    val session = state.session ?: return
    val firstName = session.displayName.substringBefore(" ")
    val hojeBr = EscalaLogic.hojeBr()
    val nomeNorm = EscalaLogic.semAcento(session.displayName)

    var deletePontoId by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        // Greeting Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${EscalaLogic.saudacaoAtual()}, $firstName.",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Painel de Controle Operacional da Viação Montenegro.",
                style = MaterialTheme.typography.bodyMedium,
                color = ext.mutedText
            )
            Text(
                text = EscalaLogic.dataExtensoAtual().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = ext.mutedText
            )
        }

        // 3 Summary Cards
        val escalasHoje = remember(state.escalas, state.isAdmin, nomeNorm) {
            val base = state.escalas.filter { EscalaLogic.escalaDeHoje(it) }
            if (state.isAdmin) base else base.filter {
                val n = EscalaLogic.semAcento(it.motorista)
                n.isNotEmpty() && (n == nomeNorm || nomeNorm.contains(n) || n.contains(nomeNorm))
            }
        }

        if (!state.hasLoadedOnce) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) {
                    MontenegroCard {
                        ShimmerBox(Modifier.fillMaxWidth(0.6f).height(24.dp).clip(RoundedCornerShape(10.dp)))
                        Spacer(Modifier.height(10.dp))
                        ShimmerBox(Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(14.dp)))
                    }
                }
            }
        } else {
            val summaryCards: List<@Composable () -> Unit> = listOf(
                {
                    MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                        SummaryCardHeader(
                            icon = Icons.Rounded.CalendarMonth,
                            title = if (state.isAdmin) "Escalas em andamento" else "Sua escala de hoje",
                            badge = if (escalasHoje.isNotEmpty()) escalasHoje.size.toString() else null
                        )
                        Spacer(Modifier.height(12.dp))
                        if (escalasHoje.isEmpty()) {
                            EmptyStateBox(
                                icon = Icons.Rounded.CalendarMonth,
                                title = "Sem escala hoje",
                                subtitle = if (state.isAdmin) "Nenhuma escala em andamento." else "Você não está escalado para hoje.",
                                compact = true
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                escalasHoje.take(3).forEach { e ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = ext.subtleSurface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = e.turno.uppercase(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ext.limePillContent
                                            )
                                            Text(
                                                text = "Linha ${e.linha} • Carro ${e.prefixo}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = if (state.isAdmin) e.motorista else e.data,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ext.mutedText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                {
                    MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                        SummaryCardHeader(
                            icon = Icons.Rounded.Schedule,
                            title = if (state.isAdmin) "Pontos de hoje" else "Último ponto"
                        )
                        Spacer(Modifier.height(12.dp))
                        if (state.isAdmin) {
                            val countHoje = state.pontos.count { it.dataBr == hojeBr }
                            val ultimo = state.pontos.firstOrNull()
                            if (ultimo == null) {
                                EmptyStateBox(
                                    icon = Icons.Rounded.Schedule,
                                    title = "Nenhum ponto ainda",
                                    subtitle = "Os registros dos motoristas aparecem aqui.",
                                    compact = true
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ext.subtleSurface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("$countHoje", fontSize = 24.sp, fontWeight = FontWeight.Black)
                                            Text("registro(s) hoje", style = MaterialTheme.typography.bodySmall, color = ext.mutedText)
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Último: ${ultimo.motorista} • ${ultimo.tipo} às ${ultimo.horaBr}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ext.mutedText
                                        )
                                    }
                                }
                            }
                        } else {
                            val meuUltimo = state.pontos.firstOrNull { it.autorEmail == session.email }
                            if (meuUltimo == null) {
                                EmptyStateBox(
                                    icon = Icons.Rounded.Schedule,
                                    title = "Nenhum ponto ainda",
                                    subtitle = "Registre a entrada do seu turno.",
                                    compact = true
                                )
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ext.subtleSurface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(meuUltimo.tipo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                        Text(
                                            text = "${if (meuUltimo.dataBr == hojeBr) "Hoje" else meuUltimo.dataBr} às ${meuUltimo.horaBr}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ext.mutedText
                                        )
                                        DeleteChipButton(
                                            onClick = { deletePontoId = meuUltimo.id },
                                            label = "Apagar este ponto",
                                            testTag = "delete_last_ponto"
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { onNavigateTab(AppTab.Ponto) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalEmerald),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("go_to_ponto_button")
                            ) {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Bater ponto", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                {
                    MontenegroCard(
                        modifier = Modifier.fillMaxWidth().testTag("summary_avisos_card"),
                        onClick = { onNavigateTab(AppTab.Comunicados) }
                    ) {
                        SummaryCardHeader(
                            icon = Icons.Rounded.Campaign,
                            title = "Avisos",
                            badge = if (state.unreadNotifications > 0) {
                                val n = state.unreadNotifications
                                "$n ${if (n > 1) "novos" else "novo"}"
                            } else null,
                            badgeAlert = state.unreadNotifications > 0
                        )
                        Spacer(Modifier.height(12.dp))
                        if (state.comunicados.isEmpty()) {
                            EmptyStateBox(
                                icon = Icons.Rounded.Campaign,
                                title = "Sem avisos",
                                subtitle = "Quando houver um comunicado, ele aparece aqui.",
                                compact = true
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.comunicados.take(2).forEach { c ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = ext.subtleSurface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(c.titulo, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(c.texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text("${c.dataBr} às ${c.horaBr}", fontSize = 10.sp, color = ext.mutedText)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )

            if (isExpanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    summaryCards.forEach { card -> Box(modifier = Modifier.weight(1f)) { card() } }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    summaryCards.forEach { it() }
                }
            }
        }

        // 2 KPI Stat Cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            KpiMiniCard(
                icon = Icons.Rounded.Groups,
                iconBg = Color(0xFFECFDF5),
                iconTint = BrandPalette.OperationalEmerald,
                label = "Motoristas",
                value = state.motoristas.size.toString(),
                modifier = Modifier.weight(1f)
            )
            KpiMiniCard(
                icon = Icons.Rounded.Assignment,
                iconBg = Color(0xFFF3E8FF),
                iconTint = Color(0xFF9333EA),
                label = "Escalas cadastradas",
                value = state.escalas.size.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        // Admin Schedule Publisher
        if (state.isAdmin) {
            PublishEscalaCard(
                state = state,
                onPublishEscala = onPublishEscala,
                onConsumeDraft = onConsumeDraft
            )
        }
    }

    if (deletePontoId != null) {
        ConfirmDeleteDialog(
            message = "Apagar este registro de ponto?",
            onConfirm = {
                onDeletePonto(deletePontoId!!)
                deletePontoId = null
            },
            onDismiss = { deletePontoId = null }
        )
    }
}

@Composable
private fun SummaryCardHeader(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    badgeAlert: Boolean = false
) {
    val ext = LocalMontenegroColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ext.limePillBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ext.limePillContent, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (badgeAlert) BrandPalette.AlertRed else ext.subtleSurface
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badgeAlert) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun KpiMiniCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val ext = LocalMontenegroColors.current
    MontenegroCard(modifier = modifier, contentPadding = PaddingValues(14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ext.mutedText)
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PublishEscalaCard(
    state: MainUiState,
    onPublishEscala: (LocalDate, LocalDate, String, String, String, String, () -> Unit) -> Unit,
    onConsumeDraft: () -> Unit
) {
    val turnos = listOf("Manhã", "Tarde", "Noite", "Integral", "Final de Semana")
    var dataInicio by remember { mutableStateOf(EscalaLogic.hojeLocalDate()) }
    var dataFim by remember { mutableStateOf(EscalaLogic.hojeLocalDate()) }
    var turno by remember { mutableStateOf(turnos.first()) }
    var motorista by remember { mutableStateOf("") }
    var linha by remember { mutableStateOf("") }
    var prefixo by remember { mutableStateOf("") }

    var showTurnoMenu by remember { mutableStateOf(false) }
    var showMotoristaMenu by remember { mutableStateOf(false) }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    var conflictMessages by remember { mutableStateOf<List<String>?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val draft = state.escalaDraft
    LaunchedEffect(draft) {
        if (draft != null) {
            motorista = draft.motorista
            dataInicio = draft.dataInicio
            dataFim = draft.dataFim
            onConsumeDraft()
        }
    }

    fun trySubmit(ignoreConflicts: Boolean) {
        if (dataFim.isBefore(dataInicio)) {
            validationError = "A data de encerramento não pode ser antes do início."
            return
        }
        if (motorista.isBlank() || linha.isBlank() || prefixo.isBlank()) {
            validationError = "Preencha motorista, linha e carro/prefixo."
            return
        }
        if (!ignoreConflicts) {
            val nova = Escala(turno = turno, motorista = motorista.trim(), linha = linha.trim(), prefixo = prefixo.trim())
            val conflitos = EscalaLogic.conflitosAoPublicar(nova, dataInicio, dataFim, state.escalas)
            if (conflitos.isNotEmpty()) {
                conflictMessages = conflitos
                return
            }
        }
        onPublishEscala(dataInicio, dataFim, turno, motorista, linha, prefixo) {
            motorista = ""
            linha = ""
            prefixo = ""
        }
    }

    MontenegroCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = BrandPalette.OperationalBlue)
            Text("Publicar Escala por Período", style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider(color = LocalMontenegroColors.current.cardBorder)
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = EscalaLogic.formatBR(dataInicio),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data Início") },
                    trailingIcon = {
                        IconButton(onClick = { pickingStart = true }) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = "Selecionar data início")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).clickable { pickingStart = true }
                )
                OutlinedTextField(
                    value = EscalaLogic.formatBR(dataFim),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data Encerramento") },
                    trailingIcon = {
                        IconButton(onClick = { pickingEnd = true }) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = "Selecionar data fim")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).clickable { pickingEnd = true }
                )
            }

            ExposedDropdownMenuBox(
                expanded = showTurnoMenu,
                onExpandedChange = { showTurnoMenu = it }
            ) {
                OutlinedTextField(
                    value = turno,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Turno") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTurnoMenu) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = showTurnoMenu, onDismissRequest = { showTurnoMenu = false }) {
                    turnos.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = { turno = t; showTurnoMenu = false }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = showMotoristaMenu && state.motoristas.isNotEmpty(),
                onExpandedChange = { showMotoristaMenu = it }
            ) {
                OutlinedTextField(
                    value = motorista,
                    onValueChange = { motorista = it; showMotoristaMenu = true },
                    label = { Text("Nome do Motorista") },
                    placeholder = { Text("Selecione ou digite...") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMotoristaMenu) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.menuAnchor().fillMaxWidth().testTag("admin_escala_motorista")
                )
                val filteredDrivers = state.motoristas.filter {
                    motorista.isBlank() || EscalaLogic.semAcento(it.nome).contains(EscalaLogic.semAcento(motorista))
                }
                if (filteredDrivers.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = showMotoristaMenu,
                        onDismissRequest = { showMotoristaMenu = false }
                    ) {
                        filteredDrivers.take(8).forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.nome) },
                                onClick = { motorista = m.nome; showMotoristaMenu = false }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = linha,
                    onValueChange = { linha = it },
                    label = { Text("Linha") },
                    placeholder = { Text("Ex: 361") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("admin_escala_linha")
                )
                OutlinedTextField(
                    value = prefixo,
                    onValueChange = { prefixo = it },
                    label = { Text("Carro / Prefixo") },
                    placeholder = { Text("Ex: 04430 ao 04435") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("admin_escala_prefixo")
                )
            }

            Button(
                onClick = { trySubmit(ignoreConflicts = false) },
                enabled = !state.isSubmittingAction,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalBlue),
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("publish_escala_button")
            ) {
                Text("Publicar Escala por Período", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (pickingStart) {
        DatePickerModal(
            initialDate = dataInicio,
            onDateSelected = { dataInicio = it; if (dataFim.isBefore(it)) dataFim = it; pickingStart = false },
            onDismiss = { pickingStart = false }
        )
    }
    if (pickingEnd) {
        DatePickerModal(
            initialDate = dataFim,
            onDateSelected = { dataFim = it; pickingEnd = false },
            onDismiss = { pickingEnd = false }
        )
    }

    conflictMessages?.let { msgs ->
        AlertDialog(
            onDismissRequest = { conflictMessages = null },
            title = { Text("Atenção: conflito de escala") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Esta escala conflita com:")
                    msgs.take(5).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    if (msgs.size > 5) Text("• e mais ${msgs.size - 5}...", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(6.dp))
                    Text("Publicar mesmo assim?", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = { conflictMessages = null; trySubmit(ignoreConflicts = true) }) {
                    Text("Publicar mesmo assim")
                }
            },
            dismissButton = {
                TextButton(onClick = { conflictMessages = null }) { Text("Cancelar") }
            }
        )
    }

    validationError?.let { msg ->
        AlertDialog(
            onDismissRequest = { validationError = null },
            title = { Text("Atenção") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { validationError = null }) { Text("OK") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val millis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = millis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val sel = pickerState.selectedDateMillis
                if (sel != null) {
                    val date = Instant.ofEpochMilli(sel).atZone(ZoneOffset.UTC).toLocalDate()
                    onDateSelected(date)
                } else {
                    onDismiss()
                }
            }) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    ) {
        DatePicker(state = pickerState)
    }
}
