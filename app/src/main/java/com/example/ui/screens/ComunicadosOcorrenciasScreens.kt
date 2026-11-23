package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.EscalaLogic
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DeleteChipButton
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.MontenegroCard
import com.example.ui.state.MainUiState
import com.example.ui.theme.BrandPalette
import com.example.ui.theme.LocalMontenegroColors
import java.time.format.DateTimeFormatter

@Composable
fun ComunicadosScreen(
    state: MainUiState,
    onCreateComunicado: (titulo: String, texto: String, onSuccess: () -> Unit) -> Unit,
    onDeleteComunicado: (String) -> Unit
) {
    val ext = LocalMontenegroColors.current
    var titulo by remember { mutableStateOf("") }
    var texto by remember { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<String?>(null) }

    val q = state.searchQuery.trim().lowercase()
    val list = remember(state.comunicados, q) {
        if (q.isEmpty()) state.comunicados
        else state.comunicados.filter { "${it.titulo} ${it.texto}".lowercase().contains(q) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Mural de Comunicados & Avisos", style = MaterialTheme.typography.headlineSmall)

        if (state.isAdmin) {
            MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(Icons.Rounded.Campaign, contentDescription = null, tint = BrandPalette.OperationalBlue)
                    Text("Novo Comunicado", style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider(color = ext.cardBorder)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título do Aviso") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("comunicado_titulo")
                    )
                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = it },
                        label = { Text("Detalhes do comunicado...") },
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("comunicado_texto")
                    )
                    Button(
                        onClick = {
                            onCreateComunicado(titulo, texto) { titulo = ""; texto = "" }
                        },
                        enabled = !state.isSubmittingAction && titulo.isNotBlank() && texto.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalBlue),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("publish_comunicado_button")
                    ) {
                        Text("Publicar Comunicado & Notificar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (list.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Rounded.Campaign,
                title = "Sem comunicados",
                subtitle = "Quando houver um aviso importante, ele aparece aqui."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                list.forEach { c ->
                    MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.Campaign, contentDescription = null, tint = ext.blueAccent, modifier = Modifier.size(16.dp))
                                    Text(c.titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ext.blueAccent)
                                }
                                Text("${c.dataBr} às ${c.horaBr}", fontSize = 10.sp, color = ext.mutedText)
                            }
                            if (state.isAdmin) {
                                DeleteChipButton(onClick = { deleteId = c.id })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(c.texto, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    deleteId?.let { id ->
        ConfirmDeleteDialog(
            message = "Tem certeza que deseja excluir este comunicado?",
            onConfirm = { onDeleteComunicado(id); deleteId = null },
            onDismiss = { deleteId = null }
        )
    }
}

@Composable
fun OcorrenciasScreen(
    state: MainUiState,
    onCreateOcorrencia: (texto: String, onSuccess: () -> Unit) -> Unit,
    onCreateJustificativa: (dataIso: String, motivo: String, onSuccess: () -> Unit) -> Unit,
    onDeleteItem: (table: String, id: String) -> Unit
) {
    val ext = LocalMontenegroColors.current
    var textoOcorrencia by remember { mutableStateOf("") }
    var dataFalta by remember { mutableStateOf(EscalaLogic.hojeLocalDate()) }
    var pickingDate by remember { mutableStateOf(false) }
    var motivoFalta by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Pair<String, String>?>(null) }

    val q = state.searchQuery.trim().lowercase()
    val ocorrencias = remember(state.ocorrencias, q) {
        if (q.isEmpty()) state.ocorrencias
        else state.ocorrencias.filter { "${it.autor} ${it.texto}".lowercase().contains(q) }
    }
    val justificativas = remember(state.justificativas, q) {
        if (q.isEmpty()) state.justificativas
        else state.justificativas.filter { "${it.motorista} ${it.motivo} ${it.dataFalta}".lowercase().contains(q) }
    }

    fun canDelete(authorEmail: String): Boolean =
        state.isAdmin || (state.session != null && authorEmail == state.session.email)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Ocorrências Operacionais & Justificativas", style = MaterialTheme.typography.headlineSmall)

        // Form 1: Registrar Ocorrencia
        MontenegroCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "REGISTRAR OCORRÊNCIA",
                style = MaterialTheme.typography.labelSmall,
                color = ext.mutedText
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = textoOcorrencia,
                onValueChange = { textoOcorrencia = it },
                label = { Text("Descreva a ocorrência...") },
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("ocorrencia_texto")
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { onCreateOcorrencia(textoOcorrencia) { textoOcorrencia = "" } },
                enabled = !state.isSubmittingAction && textoOcorrencia.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalBlue),
                modifier = Modifier.height(48.dp).testTag("submit_ocorrencia_button")
            ) {
                Text("Registrar Ocorrência", fontWeight = FontWeight.Bold)
            }
        }

        // Form 2: Justificar Falta
        MontenegroCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "JUSTIFICAR FALTA",
                style = MaterialTheme.typography.labelSmall,
                color = ext.mutedText
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = EscalaLogic.formatBR(dataFalta),
                onValueChange = {},
                readOnly = true,
                label = { Text("Data da Falta") },
                trailingIcon = {
                    IconButton(onClick = { pickingDate = true }) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = "Selecionar data")
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().clickable { pickingDate = true }
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = motivoFalta,
                onValueChange = { motivoFalta = it },
                label = { Text("Motivo da ausência...") },
                minLines = 2,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("justificativa_motivo")
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    val iso = dataFalta.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    onCreateJustificativa(iso, motivoFalta) { motivoFalta = "" }
                },
                enabled = !state.isSubmittingAction && motivoFalta.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalBlue),
                modifier = Modifier.height(48.dp).testTag("submit_justificativa_button")
            ) {
                Text("Enviar Justificativa", fontWeight = FontWeight.Bold)
            }
        }

        // Ocorrencias List
        Text("Ocorrências registradas", style = MaterialTheme.typography.titleMedium)
        if (ocorrencias.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Rounded.WarningAmber,
                title = "Nenhuma ocorrência",
                subtitle = "Está tudo tranquilo por aqui."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ocorrencias.forEach { o ->
                    MontenegroCard(contentPadding = PaddingValues(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${o.autor} • ${o.dataBr} ${o.horaBr}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ext.mutedText)
                            if (canDelete(o.autorEmail)) {
                                DeleteChipButton(onClick = { deleteTarget = "ocorrencias" to o.id })
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(o.texto, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Justificativas List
        Text("Justificativas de falta", style = MaterialTheme.typography.titleMedium)
        if (justificativas.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Rounded.Assignment,
                title = "Nenhuma justificativa",
                subtitle = "As justificativas de falta aparecem aqui."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                justificativas.forEach { j ->
                    MontenegroCard(contentPadding = PaddingValues(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${j.motorista} • Falta em: ${EscalaLogic.dataBrParaExibicao(j.dataFalta)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ext.mutedText
                            )
                            if (canDelete(j.autorEmail)) {
                                DeleteChipButton(onClick = { deleteTarget = "justificativas" to j.id })
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(j.motivo, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    if (pickingDate) {
        DatePickerModal(
            initialDate = dataFalta,
            onDateSelected = { dataFalta = it; pickingDate = false },
            onDismiss = { pickingDate = false }
        )
    }

    deleteTarget?.let { (table, id) ->
        ConfirmDeleteDialog(
            message = "Tem certeza que deseja excluir este registro?",
            onConfirm = { onDeleteItem(table, id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}
