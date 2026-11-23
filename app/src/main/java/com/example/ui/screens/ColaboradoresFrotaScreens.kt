package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.SupabaseApi
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.DeleteChipButton
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.LazySupabaseImage
import com.example.ui.components.MontenegroCard
import com.example.ui.state.MainUiState
import com.example.ui.theme.BrandPalette
import com.example.ui.theme.LocalMontenegroColors

@Composable
fun ColaboradoresScreen(
    state: MainUiState,
    api: SupabaseApi,
    onCreateMotorista: (nome: String, email: String, senha: String, onSuccess: () -> Unit) -> Unit,
    onDeleteMotorista: (String) -> Unit
) {
    val ext = LocalMontenegroColors.current
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<String?>(null) }

    val q = state.searchQuery.trim().lowercase()
    val list = remember(state.motoristas, q) {
        if (q.isEmpty()) state.motoristas
        else state.motoristas.filter { "${it.nome} ${it.email}".lowercase().contains(q) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (state.isAdmin) {
            MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = BrandPalette.OperationalEmerald)
                    Text("Cadastrar Novo Motorista", style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider(color = ext.cardBorder)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = it },
                        label = { Text("Nome Completo") },
                        placeholder = { Text("Ex: João Silva") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_driver_name")
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail de Acesso") },
                        placeholder = { Text("joao@montenegro.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_driver_email")
                    )
                    OutlinedTextField(
                        value = senha,
                        onValueChange = { senha = it },
                        label = { Text("Senha Inicial") },
                        placeholder = { Text("••••••••") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_driver_password")
                    )
                    Button(
                        onClick = {
                            onCreateMotorista(nome, email, senha) {
                                nome = ""; email = ""; senha = ""
                            }
                        },
                        enabled = !state.isSubmittingAction && nome.isNotBlank() && email.isNotBlank() && senha.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalEmerald),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_driver_button")
                    ) {
                        Text("Salvar Novo Motorista", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("Equipe de Motoristas", style = MaterialTheme.typography.headlineSmall)

        if (list.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Rounded.Groups,
                title = "Equipe vazia",
                subtitle = "Cadastre o primeiro motorista para começar."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                list.forEach { m ->
                    MontenegroCard(contentPadding = PaddingValues(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                LazySupabaseImage(
                                    api = api,
                                    table = "motoristas",
                                    id = m.id,
                                    column = "foto",
                                    contentDescription = m.nome,
                                    height = 48.dp,
                                    modifier = Modifier.size(48.dp).clip(CircleShape),
                                    fallback = {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(ext.subtleSurface)
                                                .border(1.dp, ext.cardBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Rounded.Person, contentDescription = null, tint = ext.mutedText)
                                        }
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = m.nome,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = m.email,
                                        fontSize = 11.sp,
                                        color = ext.mutedText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            if (state.isAdmin) {
                                DeleteChipButton(onClick = { deleteId = m.id }, label = "")
                            }
                        }
                    }
                }
            }
        }
    }

    deleteId?.let { id ->
        ConfirmDeleteDialog(
            message = "Tem certeza que deseja excluir este motorista?",
            onConfirm = { onDeleteMotorista(id); deleteId = null },
            onDismiss = { deleteId = null }
        )
    }
}

@Composable
fun FrotaScreen(
    state: MainUiState,
    onCreateFrota: (modelo: String, prefixo: String, chassi: String, onSuccess: () -> Unit) -> Unit,
    onDeleteFrota: (String) -> Unit
) {
    val ext = LocalMontenegroColors.current
    var modelo by remember { mutableStateOf("") }
    var prefixo by remember { mutableStateOf("") }
    var chassi by remember { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<String?>(null) }

    val q = state.searchQuery.trim().lowercase()
    val list = remember(state.frota, q) {
        if (q.isEmpty()) state.frota
        else state.frota.filter { "${it.modelo} ${it.prefixo} ${it.chassi}".lowercase().contains(q) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (state.isAdmin) {
            MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(Icons.Rounded.DirectionsBus, contentDescription = null, tint = BrandPalette.OperationalBlue)
                    Text("Cadastrar Veículo", style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider(color = ext.cardBorder)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = modelo,
                        onValueChange = { modelo = it },
                        label = { Text("Modelo do Ônibus") },
                        placeholder = { Text("Ex: Marcopolo Torino") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("frota_modelo")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = prefixo,
                            onValueChange = { prefixo = it },
                            label = { Text("Prefixos") },
                            placeholder = { Text("Ex: 04430 ao 04435") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).testTag("frota_prefixo")
                        )
                        OutlinedTextField(
                            value = chassi,
                            onValueChange = { chassi = it },
                            label = { Text("Chassi") },
                            placeholder = { Text("Ex: Volkswagen") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).testTag("frota_chassi")
                        )
                    }
                    Button(
                        onClick = {
                            onCreateFrota(modelo, prefixo, chassi) {
                                modelo = ""; prefixo = ""; chassi = ""
                            }
                        },
                        enabled = !state.isSubmittingAction && modelo.isNotBlank() && prefixo.isNotBlank() && chassi.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalBlue),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("save_frota_button")
                    ) {
                        Text("Salvar Veículo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text("Frota Montenegro", style = MaterialTheme.typography.headlineSmall)

        if (list.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Rounded.DirectionsBus,
                title = "Nenhum veículo na frota",
                subtitle = "Os ônibus cadastrados aparecem aqui."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                list.forEach { v ->
                    MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.DirectionsBus, contentDescription = null, tint = ext.blueAccent, modifier = Modifier.size(18.dp))
                                    Text(v.modelo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "Prefixos: ${v.prefixo} | Chassi: ${v.chassi}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (state.isAdmin) {
                                DeleteChipButton(onClick = { deleteId = v.id })
                            }
                        }
                    }
                }
            }
        }
    }

    deleteId?.let { id ->
        ConfirmDeleteDialog(
            message = "Tem certeza que deseja excluir este veículo?",
            onConfirm = { onDeleteFrota(id); deleteId = null },
            onDismiss = { deleteId = null }
        )
    }
}
