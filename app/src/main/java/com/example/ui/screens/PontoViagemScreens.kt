package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrintViagem
import com.example.data.network.SupabaseApi
import com.example.domain.EscalaLogic
import com.example.ui.components.*
import com.example.ui.state.MainUiState
import com.example.ui.theme.BrandPalette
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalMontenegroColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PontoScreen(
    state: MainUiState,
    onCompressImage: suspend (android.net.Uri, Int) -> Result<String>,
    onRegisterPonto: (tipo: String, onSuccess: () -> Unit) -> Unit,
    onCreatePrint: (estagio: String, linha: String, prefixo: String, km: String, img: String, onSuccess: () -> Unit) -> Unit,
    onCreateRelatorio: (linha: String, prefixo: String, inteira: Int, meia: Int, km: Double, img: String?, onSuccess: () -> Unit) -> Unit,
    onDeletePonto: (String) -> Unit
) {
    val ext = LocalMontenegroColors.current
    val scope = rememberCoroutineScope()
    val session = state.session ?: return

    val pontoTipos = listOf("Entrada de Turno", "Saída para Almoço", "Retorno de Almoço", "Fim de Turno")
    var pontoTipo by remember { mutableStateOf(pontoTipos.first()) }
    var pontoMenuOpen by remember { mutableStateOf(false) }
    var recentStatus by remember { mutableStateOf<String?>(null) }
    var deletePontoId by remember { mutableStateOf<String?>(null) }

    // Print form state
    val estagios = listOf("Início de Viagem", "Meio de Viagem", "Fim de Viagem")
    var estagio by remember { mutableStateOf(estagios.first()) }
    var estagioMenuOpen by remember { mutableStateOf(false) }
    var printLinha by remember { mutableStateOf("") }
    var printPrefixo by remember { mutableStateOf("") }
    var printKm by remember { mutableStateOf("") }
    var printImageUri by remember { mutableStateOf<String?>(null) }
    var printImageLabel by remember { mutableStateOf("Nenhum arquivo") }
    var printError by remember { mutableStateOf<String?>(null) }

    val pickPrintLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                onCompressImage(uri, 900).fold(
                    onSuccess = { dataUri ->
                        printImageUri = dataUri
                        printImageLabel = "Imagem selecionada"
                    },
                    onFailure = { printError = "Não foi possível ler essa imagem." }
                )
            }
        }
    }

    // Relatorio form state
    var relLinha by remember { mutableStateOf("") }
    var relPrefixo by remember { mutableStateOf("") }
    var relCatracados by remember { mutableStateOf("") }
    var relMeia by remember { mutableStateOf("0") }
    var relKm by remember { mutableStateOf("") }
    var relImageUri by remember { mutableStateOf<String?>(null) }
    var relImageLabel by remember { mutableStateOf("Nenhum arquivo") }

    val pickRelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                onCompressImage(uri, 900).fold(
                    onSuccess = { dataUri ->
                        relImageUri = dataUri
                        relImageLabel = "Imagem selecionada"
                    },
                    onFailure = { printError = "Não foi possível ler essa imagem." }
                )
            }
        }
    }

    val pontosVisiveis = remember(state.pontos, state.isAdmin, session.email) {
        val base = if (state.isAdmin) state.pontos else state.pontos.filter { it.autorEmail == session.email }
        base.take(30)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Card 1: Ponto digital
        MontenegroCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "HORA OFICIAL DE BRASÍLIA",
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.mutedText
                )
                Text(
                    text = state.currentClock,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 42.sp,
                    letterSpacing = 1.sp
                )

                ExposedDropdownMenuBox(
                    expanded = pontoMenuOpen,
                    onExpandedChange = { pontoMenuOpen = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = pontoTipo,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pontoMenuOpen) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth().testTag("ponto_tipo_dropdown")
                    )
                    ExposedDropdownMenu(expanded = pontoMenuOpen, onDismissRequest = { pontoMenuOpen = false }) {
                        pontoTipos.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { pontoTipo = t; pontoMenuOpen = false })
                        }
                    }
                }

                Button(
                    onClick = {
                        val selected = pontoTipo
                        onRegisterPonto(selected) {
                            recentStatus = "\"$selected\" registrado às ${EscalaLogic.agoraBr().second}!"
                        }
                    },
                    enabled = !state.isSubmittingAction,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalEmerald),
                    modifier = Modifier.fillMaxWidth().height(54.dp).testTag("register_ponto_button")
                ) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar Ponto Agora", fontWeight = FontWeight.Bold)
                }

                recentStatus?.let { status ->
                    Text(
                        text = status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ext.emeraldAccent
                    )
                }

                if (pontosVisiveis.isEmpty()) {
                    Text(
                        text = "Nenhum ponto registrado ainda.",
                        fontSize = 11.sp,
                        color = ext.mutedText,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pontosVisiveis.forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ext.subtleSurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = (if (state.isAdmin) "${p.motorista} • " else "") + p.tipo,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text("${p.dataBr} ${p.horaBr}", fontSize = 11.sp, color = ext.mutedText)
                                    }
                                    if (state.isAdmin || p.autorEmail == session.email) {
                                        DeleteChipButton(onClick = { deletePontoId = p.id }, label = "Apagar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card 2: Anexar Print
        MontenegroCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = BrandPalette.OperationalBlue)
                Text("Anexar Print (Início / Meio / Fim de Viagem)", style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(color = ext.cardBorder)
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = estagioMenuOpen,
                    onExpandedChange = { estagioMenuOpen = it }
                ) {
                    OutlinedTextField(
                        value = estagio,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estágio da Viagem") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estagioMenuOpen) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = estagioMenuOpen, onDismissRequest = { estagioMenuOpen = false }) {
                        estagios.forEach { s ->
                            DropdownMenuItem(text = { Text(s) }, onClick = { estagio = s; estagioMenuOpen = false })
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = printLinha,
                        onValueChange = { printLinha = it },
                        label = { Text("Linha") },
                        placeholder = { Text("Ex: 361") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("print_linha")
                    )
                    OutlinedTextField(
                        value = printPrefixo,
                        onValueChange = { printPrefixo = it },
                        label = { Text("Carro") },
                        placeholder = { Text("Ex: 04430") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("print_prefixo")
                    )
                }

                OutlinedTextField(
                    value = printKm,
                    onValueChange = { printKm = it },
                    label = { Text("Quilometragem Percorrida (KM)") },
                    placeholder = { Text("Ex: 145.2") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("print_km")
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = {
                            pickPrintLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp).testTag("pick_print_image")
                    ) {
                        Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Selecionar da Galeria", fontSize = 12.sp)
                    }
                    Text(
                        text = printImageLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (printImageUri != null) {
                    DataUriImage(
                        dataUri = printImageUri,
                        contentDescription = "Pré-visualização do print",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, ext.cardBorder, RoundedCornerShape(16.dp))
                    ) {}
                }

                Button(
                    onClick = {
                        val img = printImageUri
                        if (img == null) {
                            printError = "Selecione um print da galeria!"
                            return@Button
                        }
                        onCreatePrint(estagio, printLinha, printPrefixo, printKm, img) {
                            printLinha = ""; printPrefixo = ""; printKm = ""
                            printImageUri = null; printImageLabel = "Nenhum arquivo"
                        }
                    },
                    enabled = !state.isSubmittingAction && printLinha.isNotBlank() && printPrefixo.isNotBlank() && printKm.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalBlue),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_print_button")
                ) {
                    Text("Postar Print para Todos", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Card 3: Adicionar Relatorio de Viagem
        MontenegroCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = BrandPalette.OperationalBlue)
                Text("Adicionar Relatório de Viagem, Passageiros & Print", style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(color = ext.cardBorder)
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = relLinha,
                        onValueChange = { relLinha = it },
                        label = { Text("Linha") },
                        placeholder = { Text("Ex: 361") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("rel_linha")
                    )
                    OutlinedTextField(
                        value = relPrefixo,
                        onValueChange = { relPrefixo = it },
                        label = { Text("Carro / Prefixo") },
                        placeholder = { Text("Ex: 04430") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("rel_prefixo")
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = relCatracados,
                        onValueChange = { relCatracados = it.filter(Char::isDigit) },
                        label = { Text("Inteira (R$ 5,40)") },
                        placeholder = { Text("60") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("rel_inteira")
                    )
                    OutlinedTextField(
                        value = relMeia,
                        onValueChange = { relMeia = it.filter(Char::isDigit) },
                        label = { Text("Meia (R$ 1,50)") },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).testTag("rel_meia")
                    )
                }

                OutlinedTextField(
                    value = relKm,
                    onValueChange = { relKm = it },
                    label = { Text("Quilômetros (KM)") },
                    placeholder = { Text("Ex: 120.5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("rel_km")
                )

                Text(
                    text = "ANEXAR PRINT DA VIAGEM (OPCIONAL)",
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.mutedText
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(
                        onClick = {
                            pickRelLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp).testTag("pick_rel_image")
                    ) {
                        Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Selecionar da Galeria", fontSize = 12.sp)
                    }
                    Text(
                        text = relImageLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (relImageUri != null) {
                    DataUriImage(
                        dataUri = relImageUri,
                        contentDescription = "Pré-visualização do relatório",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, ext.cardBorder, RoundedCornerShape(16.dp))
                    ) {}
                }

                Button(
                    onClick = {
                        val inteira = relCatracados.toIntOrNull() ?: 0
                        val meia = relMeia.toIntOrNull() ?: 0
                        val km = relKm.replace(',', '.').toDoubleOrNull() ?: 0.0
                        onCreateRelatorio(relLinha, relPrefixo, inteira, meia, km, relImageUri) {
                            relLinha = ""; relPrefixo = ""; relCatracados = ""; relMeia = "0"; relKm = ""
                            relImageUri = null; relImageLabel = "Nenhum arquivo"
                        }
                    },
                    enabled = !state.isSubmittingAction && relLinha.isNotBlank() && relPrefixo.isNotBlank() && relCatracados.isNotBlank() && relKm.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalBlue),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("submit_relatorio_button")
                ) {
                    Text("Publicar Relatório & Print na Equipe", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    deletePontoId?.let { id ->
        ConfirmDeleteDialog(
            message = "Apagar este registro de ponto?",
            onConfirm = { onDeletePonto(id); deletePontoId = null },
            onDismiss = { deletePontoId = null }
        )
    }

    printError?.let { msg ->
        AlertDialog(
            onDismissRequest = { printError = null },
            title = { Text("Atenção") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { printError = null }) { Text("OK") } }
        )
    }
}

private data class PrintGroup(
    val motorista: String,
    val data: String,
    val linha: String,
    val prefixo: String,
    val km: String,
    val itens: List<PrintViagem>
)

@Composable
fun RelatoriosCompartilhadosScreen(
    state: MainUiState,
    api: SupabaseApi,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onDeleteItem: (table: String, id: String) -> Unit
) {
    val ext = LocalMontenegroColors.current
    var deleteTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    val q = state.searchQuery.trim().lowercase()

    val filteredRelatorios = remember(state.relatorios, q) {
        if (q.isEmpty()) state.relatorios
        else state.relatorios.filter {
            "${it.autor} ${it.linha} ${it.prefixo} ${it.dataBr}".lowercase().contains(q)
        }
    }

    val printGroups = remember(state.prints, q) {
        val grouped = linkedMapOf<String, MutableList<PrintViagem>>()
        state.prints.forEach { p ->
            val key = listOf(p.autorEmail, p.dataBr, p.linha, p.prefixo).joinToString("|")
            grouped.getOrPut(key) { mutableListOf() }.add(p)
        }
        grouped.values.map { list ->
            val first = list.first()
            PrintGroup(first.motorista, first.dataBr, first.linha, first.prefixo, first.quilometragem, list)
        }.filter { g ->
            q.isEmpty() || "${g.motorista} ${g.linha} ${g.prefixo} ${g.data}".lowercase().contains(q)
        }
    }

    fun canDelete(email: String): Boolean =
        state.isAdmin || (state.session != null && email == state.session.email)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Relatórios & Prints Compartilhados", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = onRefresh) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Atualizar", fontWeight = FontWeight.Bold)
            }
        }

        if (filteredRelatorios.isEmpty() && printGroups.isEmpty()) {
            EmptyStateBox(
                icon = Icons.Rounded.CameraAlt,
                title = "Nada compartilhado ainda",
                subtitle = "Relatórios e prints de viagem postados pela equipe aparecem aqui."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                filteredRelatorios.take(state.relatoriosLimit).forEach { r ->
                    MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Relatório • ${r.autor}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ext.blueAccent
                                )
                                Text(
                                    text = "Linha: ${r.linha} | Carro: ${r.prefixo} | ${r.dataBr} às ${r.horaBr}",
                                    fontSize = 11.sp,
                                    color = ext.mutedText
                                )
                            }
                            if (canDelete(r.autorEmail)) {
                                DeleteChipButton(onClick = { deleteTarget = "relatorios" to r.id })
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ext.subtleSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("Inteira: ${r.catracados}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text("Meia: ${r.meia}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                }
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("KM: ${r.km}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text(
                                        text = EscalaLogic.formatBrl(EscalaLogic.faturamento(r)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ext.emeraldAccent,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        LazySupabaseImage(
                            api = api,
                            table = "relatorios",
                            id = r.id,
                            column = "url_imagem",
                            contentDescription = "Print da viagem",
                            height = 210.dp,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().background(ext.subtleSurface, RoundedCornerShape(16.dp))
                        )
                    }
                }

                printGroups.forEach { g ->
                    MontenegroCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Motorista: ${g.motorista}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Linha: ${g.linha} | Carro: ${g.prefixo} | KM: ${g.km}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = ext.subtleSurface
                            ) {
                                Text(
                                    text = g.data,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ext.mutedText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            g.itens.forEach { item ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ext.subtleSurface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = BrandPalette.OperationalBlue.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = item.estagio,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ext.blueAccent,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text(item.horaBr, fontSize = 11.sp, color = ext.mutedText)
                                                if (canDelete(item.autorEmail)) {
                                                    DeleteChipButton(onClick = { deleteTarget = "prints" to item.id }, label = "")
                                                }
                                            }
                                        }
                                        LazySupabaseImage(
                                            api = api,
                                            table = "prints",
                                            id = item.id,
                                            column = "url_imagem",
                                            contentDescription = item.estagio,
                                            height = 160.dp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (filteredRelatorios.size > state.relatoriosLimit) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = onLoadMore,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.Slate700)
                        ) {
                            Text("Ver mais relatórios (${filteredRelatorios.size - state.relatoriosLimit})")
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { (table, id) ->
        ConfirmDeleteDialog(
            message = "Tem certeza que deseja excluir?",
            onConfirm = { onDeleteItem(table, id); deleteTarget = null },
            onDismiss = { deleteTarget = null }
        )
    }
}
