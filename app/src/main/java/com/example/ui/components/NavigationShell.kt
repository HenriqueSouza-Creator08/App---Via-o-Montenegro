package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.EscalaLogic
import com.example.ui.state.AppTab
import com.example.ui.state.MainUiState
import com.example.ui.theme.BrandPalette
import com.example.ui.theme.LocalMontenegroColors
import kotlinx.coroutines.launch

private data class DrawerMenuEntry(
    val tab: AppTab?,
    val title: String,
    val icon: ImageVector,
    val badge: String? = null,
    val children: List<DrawerMenuEntry> = emptyList()
)

@Composable
fun MontenegroTopBar(
    searchQuery: String,
    isDarkTheme: Boolean,
    unreadCount: Int,
    onSearchChange: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val ext = LocalMontenegroColors.current
    Surface(
        color = ext.glassBackground,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.size(44.dp).testTag("open_drawer_button")
                ) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Abrir menu")
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Pesquisar...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Limpar pesquisa", modifier = Modifier.size(16.dp))
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = BrandPalette.OperationalBlue.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("global_search_input")
                )

                FilledTonalIconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.size(44.dp).testTag("toggle_theme_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = if (isDarkTheme) "Modo claro" else "Modo escuro"
                    )
                }

                BadgedBox(
                    badge = {
                        if (unreadCount > 0) {
                            Badge(containerColor = BrandPalette.AlertRed) {
                                Text(unreadCount.toString(), color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                ) {
                    FilledTonalIconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.size(44.dp).testTag("open_notifications_button")
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Notificações")
                    }
                }
            }
            HorizontalDivider(color = ext.cardBorder)
        }
    }
}

@Composable
fun MontenegroBottomBar(
    currentTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    onOpenMore: () -> Unit
) {
    NavigationBar(
        containerColor = LocalMontenegroColors.current.glassBackground,
        windowInsets = WindowInsets.navigationBars
    ) {
        NavigationBarItem(
            selected = currentTab == AppTab.Inicio,
            onClick = { onSelectTab(AppTab.Inicio) },
            icon = { Icon(Icons.Rounded.Home, contentDescription = "Início") },
            label = { Text("Início", fontSize = 11.sp) },
            modifier = Modifier.testTag("bottom_tab_inicio")
        )
        NavigationBarItem(
            selected = currentTab == AppTab.Escalas,
            onClick = { onSelectTab(AppTab.Escalas) },
            icon = { Icon(Icons.Rounded.Assignment, contentDescription = "Escalas") },
            label = { Text("Escalas", fontSize = 11.sp) },
            modifier = Modifier.testTag("bottom_tab_escalas")
        )
        NavigationBarItem(
            selected = currentTab == AppTab.Ponto,
            onClick = { onSelectTab(AppTab.Ponto) },
            icon = { Icon(Icons.Rounded.Schedule, contentDescription = "Ponto") },
            label = { Text("Ponto", fontSize = 11.sp) },
            modifier = Modifier.testTag("bottom_tab_ponto")
        )
        NavigationBarItem(
            selected = currentTab !in listOf(AppTab.Inicio, AppTab.Escalas, AppTab.Ponto),
            onClick = onOpenMore,
            icon = { Icon(Icons.Rounded.Menu, contentDescription = "Mais") },
            label = { Text("Mais", fontSize = 11.sp) },
            modifier = Modifier.testTag("bottom_tab_mais")
        )
    }
}

@Composable
fun MontenegroNavigationRail(
    currentTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenProfile: () -> Unit,
    sessionPhoto: String?,
    initials: String
) {
    val ext = LocalMontenegroColors.current
    NavigationRail(
        containerColor = ext.glassBackground,
        header = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BrandPalette.Slate900)
            ) {
                Icon(Icons.Rounded.DirectionsBus, contentDescription = "Abrir menu", tint = BrandPalette.MontenegroBrightLime)
            }
        }
    ) {
        val railItems = listOf(
            AppTab.Inicio to Icons.Rounded.Home,
            AppTab.Escalas to Icons.Rounded.Assignment,
            AppTab.Colaboradores to Icons.Rounded.Groups,
            AppTab.Frota to Icons.Rounded.DirectionsBus,
            AppTab.Comunicados to Icons.Rounded.Campaign,
            AppTab.Ocorrencias to Icons.Rounded.WarningAmber,
            AppTab.Ponto to Icons.Rounded.Schedule,
            AppTab.Contabilidade to Icons.Rounded.AccountBalanceWallet,
            AppTab.Wrapped to Icons.Rounded.Star
        )
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            railItems.forEach { (tab, icon) ->
                val selected = currentTab == tab || (tab == AppTab.Ponto && currentTab == AppTab.RelatoriosCompartilhados)
                NavigationRailItem(
                    selected = selected,
                    onClick = { onSelectTab(tab) },
                    icon = { Icon(icon, contentDescription = tab.label) }
                )
            }
        }
        IconButton(
            onClick = onOpenProfile,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            DataUriImage(dataUri = sessionPhoto, contentDescription = "Meu perfil", modifier = Modifier.fillMaxSize().clip(CircleShape)) {
                Text(initials, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MontenegroDrawerContent(
    state: MainUiState,
    onSelectTab: (AppTab) -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTour: () -> Unit,
    onLogout: () -> Unit
) {
    val ext = LocalMontenegroColors.current
    var menuSearch by remember { mutableStateOf("") }
    var viagensExpanded by remember(state.currentTab) {
        mutableStateOf(state.currentTab.isViagensSubmenu)
    }

    val totalRevenue = remember(state.relatorios) {
        state.relatorios.sumOf { EscalaLogic.faturamento(it) }
    }
    val compactRevenue = remember(totalRevenue) { EscalaLogic.formatCompactBrl(totalRevenue) }

    val entries = remember(state) {
        listOf(
            DrawerMenuEntry(AppTab.Inicio, "Início", Icons.Rounded.Home),
            DrawerMenuEntry(AppTab.Escalas, "Escalas", Icons.Rounded.Assignment, badge = state.escalas.size.takeIf { it > 0 }?.toString()),
            DrawerMenuEntry(AppTab.Colaboradores, "Equipe", Icons.Rounded.Groups, badge = state.motoristas.size.takeIf { it > 0 }?.toString()),
            DrawerMenuEntry(AppTab.Frota, "Frota", Icons.Rounded.DirectionsBus, badge = state.frota.size.takeIf { it > 0 }?.toString()),
            DrawerMenuEntry(
                AppTab.Comunicados,
                "Comunicados",
                Icons.Rounded.Campaign,
                badge = state.unreadNotifications.takeIf { it > 0 }?.let { "$it novos" }
            ),
            DrawerMenuEntry(AppTab.Ocorrencias, "Ocorrências", Icons.Rounded.WarningAmber, badge = state.ocorrencias.size.takeIf { it > 0 }?.toString()),
            DrawerMenuEntry(
                null,
                "Viagens",
                Icons.Rounded.ReceiptLong,
                children = listOf(
                    DrawerMenuEntry(AppTab.Ponto, "Ponto & Relatório", Icons.Rounded.Schedule),
                    DrawerMenuEntry(
                        AppTab.RelatoriosCompartilhados,
                        "Relatórios & Prints",
                        Icons.Rounded.CameraAlt,
                        badge = state.relatorios.size.takeIf { it > 0 }?.toString()
                    )
                )
            ),
            DrawerMenuEntry(AppTab.Contabilidade, "Faturamento", Icons.Rounded.AccountBalanceWallet, badge = compactRevenue.ifBlank { null }),
            DrawerMenuEntry(AppTab.Wrapped, "Wrapped", Icons.Rounded.Star)
        )
    }

    val q = menuSearch.trim().lowercase()
    LaunchedEffect(q) { if (q.isNotEmpty()) viagensExpanded = true }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(304.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandPalette.Slate900),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.DirectionsBus, contentDescription = null, tint = BrandPalette.MontenegroBrightLime)
                    }
                    Column {
                        Text("montenegro", fontWeight = FontWeight.Black, fontSize = 17.sp)
                        Text("Portal Operacional", fontSize = 10.sp, color = ext.mutedText)
                    }
                }
                IconButton(onClick = onCloseDrawer) {
                    Icon(Icons.Rounded.Close, contentDescription = "Fechar menu")
                }
            }

            OutlinedTextField(
                value = menuSearch,
                onValueChange = { menuSearch = it },
                placeholder = { Text("Buscar no menu...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Scrollable menu list
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                entries.forEach { entry ->
                    if (entry.children.isEmpty()) {
                        if (q.isEmpty() || entry.title.lowercase().contains(q)) {
                            val selected = state.currentTab == entry.tab
                            DrawerRowItem(
                                title = entry.title,
                                icon = entry.icon,
                                badge = entry.badge,
                                selected = selected,
                                onClick = {
                                    entry.tab?.let(onSelectTab)
                                    onCloseDrawer()
                                }
                            )
                        }
                    } else {
                        val visibleChildren = entry.children.filter { q.isEmpty() || it.title.lowercase().contains(q) }
                        if (q.isEmpty() || entry.title.lowercase().contains(q) || visibleChildren.isNotEmpty()) {
                            Surface(
                                onClick = { viagensExpanded = !viagensExpanded },
                                shape = CircleShape,
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(34.dp).clip(CircleShape)
                                            .background(if (state.currentTab.isViagensSubmenu) BrandPalette.MontenegroBrightLime else ext.subtleSurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            entry.icon,
                                            contentDescription = null,
                                            tint = if (state.currentTab.isViagensSubmenu) BrandPalette.Slate900 else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(entry.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = if (viagensExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                        contentDescription = null,
                                        tint = ext.mutedText
                                    )
                                }
                            }
                            AnimatedVisibility(visible = viagensExpanded) {
                                Column(modifier = Modifier.padding(start = 18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    visibleChildren.forEach { child ->
                                        val selected = state.currentTab == child.tab
                                        DrawerRowItem(
                                            title = child.title,
                                            icon = child.icon,
                                            badge = child.badge,
                                            selected = selected,
                                            onClick = {
                                                child.tab?.let(onSelectTab)
                                                onCloseDrawer()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Profile + Help + Logout footer
            val session = state.session
            if (session != null) {
                Surface(
                    onClick = { onCloseDrawer(); onOpenProfile() },
                    shape = RoundedCornerShape(16.dp),
                    color = ext.subtleSurface,
                    modifier = Modifier.fillMaxWidth().testTag("drawer_profile_card")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            DataUriImage(dataUri = session.foto, contentDescription = session.displayName, modifier = Modifier.fillMaxSize().clip(CircleShape)) {
                                Icon(Icons.Rounded.Person, contentDescription = null)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(session.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                session.bio.ifBlank { if (session.isAdmin) "Administrador" else "Motorista Montenegro" },
                                fontSize = 10.sp,
                                color = ext.mutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { onCloseDrawer(); onOpenTour() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("open_tour_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Como usar o app", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onCloseDrawer(); onLogout() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ext.dangerContainer,
                    contentColor = ext.dangerContent
                ),
                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("logout_button")
            ) {
                Text("Sair da Conta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DrawerRowItem(
    title: String,
    icon: ImageVector,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val ext = LocalMontenegroColors.current
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) BrandPalette.Slate900 else Color.Transparent,
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (selected) BrandPalette.MontenegroBrightLime else ext.subtleSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) BrandPalette.Slate900 else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (!badge.isNullOrBlank()) {
                Surface(
                    shape = CircleShape,
                    color = if (selected) Color.White.copy(alpha = 0.16f) else ext.subtleSurface
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) Color.White else ext.mutedText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationsDialog(
    state: MainUiState,
    onDismiss: () -> Unit
) {
    val ext = LocalMontenegroColors.current
    Dialog(onDismissRequest = onDismiss) {
        MontenegroCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CENTRAL DE NOTIFICAÇÕES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Fechar")
                }
            }
            HorizontalDivider(color = ext.cardBorder)
            Spacer(Modifier.height(10.dp))
            if (state.comunicados.isEmpty()) {
                EmptyStateBox(
                    icon = Icons.Rounded.Notifications,
                    title = "Tudo em dia",
                    subtitle = "Nenhuma notificação por enquanto.",
                    compact = true
                )
            } else {
                Column(
                    modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.comunicados.take(15).forEach { c ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BrandPalette.OperationalBlue.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, BrandPalette.OperationalBlue.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(c.titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ext.blueAccent, modifier = Modifier.weight(1f))
                                    Text(c.horaBr, fontSize = 10.sp, color = ext.mutedText)
                                }
                                Text(c.texto, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileModalDialog(
    state: MainUiState,
    onCompressImage: suspend (android.net.Uri, Int) -> Result<String>,
    onSave: (nome: String, bio: String, newPhoto: String?, onDone: () -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val session = state.session ?: return
    val scope = rememberCoroutineScope()
    var nome by remember(session.nomePersonalizado) { mutableStateOf(session.nomePersonalizado) }
    var bio by remember(session.bio) { mutableStateOf(session.bio) }
    var pickedPhoto by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                onCompressImage(uri, 256).onSuccess { pickedPhoto = it }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        MontenegroCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Personalizar o Meu Perfil", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Fechar")
                }
            }
            Spacer(Modifier.height(10.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(3.dp, BrandPalette.OperationalBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    DataUriImage(
                        dataUri = pickedPhoto ?: session.foto,
                        contentDescription = session.displayName,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    ) {
                        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(38.dp))
                    }
                }

                FilledTonalButton(
                    onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Escolher arquivo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome de Exibição") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Breve Descrição / Bio") },
                    placeholder = { Text("Ex: Gestão Operacional") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onSave(nome, bio, pickedPhoto, onDismiss) },
                    enabled = !state.isSubmittingAction,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalBlue),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Guardar Alterações", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TourModalDialog(
    step: Int,
    isAdmin: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val ext = LocalMontenegroColors.current
    val items = listOf(
        Triple(
            Icons.Rounded.Schedule,
            "Bata o ponto",
            "Na aba Ponto, registre a entrada, o almoço e o fim do turno. O horário fica salvo para a administração."
        ),
        Triple(
            Icons.Rounded.BarChart,
            "Envie o relatório da viagem",
            "Informe os passageiros (inteira e meia), os KM e anexe o print. O relatório aparece para toda a equipe."
        ),
        Triple(
            Icons.Rounded.CalendarMonth,
            "Confira a escala",
            if (isAdmin) {
                "Monte a semana na grade de Escalas, copie a semana passada com um toque e veja os avisos de conflito de motorista e carro."
            } else {
                "Em Escalas, a grade da semana mostra onde você está escalado. Na tela inicial, o resumo do dia mostra a escala de hoje."
            }
        )
    )
    val (icon, title, desc) = items[step.coerceIn(0, 2)]
    val isLast = step >= 2

    Dialog(onDismissRequest = onSkip) {
        MontenegroCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ext.limePillBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = ext.limePillContent, modifier = Modifier.size(30.dp))
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..2).forEach { i ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (i == step) 24.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i == step) BrandPalette.MontenegroLime else ext.cardBorder)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Pular", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onNext,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.MontenegroOlive),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (isLast) "Começar" else "Próximo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
