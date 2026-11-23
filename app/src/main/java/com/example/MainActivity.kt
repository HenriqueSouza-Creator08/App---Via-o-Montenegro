package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.state.AppTab
import com.example.ui.state.MainUiState
import com.example.ui.state.MainViewModel
import com.example.ui.theme.LocalMontenegroColors
import com.example.ui.theme.MontenegroTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val state by vm.uiState.collectAsStateWithLifecycle()
            val widthClass = calculateWindowSizeClass(this).widthSizeClass
            val systemDark = isSystemInDarkTheme()
            val isDark = state.darkThemeOverride ?: systemDark

            MontenegroTheme(darkTheme = isDark) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        state.isLoadingTransition -> LoadingTransitionScreen()
                        state.session == null -> LoginScreen(
                            initialEmail = state.rememberedEmail,
                            initialRemember = state.rememberMe,
                            isSubmitting = state.isSubmittingAction,
                            onSubmit = vm::login
                        )
                        else -> AuthenticatedAppShell(
                            state = state,
                            vm = vm,
                            widthClass = widthClass,
                            isDark = isDark
                        )
                    }

                    // Toast Pill
                    LaunchedEffect(state.toastMessage) {
                        if (state.toastMessage != null) {
                            delay(2600)
                            vm.clearToast()
                        }
                    }
                    AnimatedVisibility(
                        visible = state.toastMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 68.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background,
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(state.toastMessage.orEmpty(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Animated ID Badge Reader Overlay
                    CrachaOverlay(
                        event = state.crachaEvent,
                        onDismiss = vm::dismissCracha,
                        onCelebrate = vm::triggerConfetti
                    )

                    // Confetti Physics Overlay
                    ConfettiOverlay(burstId = state.confettiBurstId)

                    // Alert / Error Dialog
                    state.errorAlertMessage?.let { msg ->
                        AlertDialog(
                            onDismissRequest = vm::clearErrorAlert,
                            title = { Text("Aviso") },
                            text = { Text(msg) },
                            confirmButton = {
                                TextButton(onClick = vm::clearErrorAlert) { Text("OK") }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedAppShell(
    state: MainUiState,
    vm: MainViewModel,
    widthClass: WindowWidthSizeClass,
    isDark: Boolean
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showNotifications by remember { mutableStateOf(false) }
    var showProfileModal by remember { mutableStateOf(false) }

    val isCompact = widthClass == WindowWidthSizeClass.Compact
    val isExpanded = widthClass == WindowWidthSizeClass.Expanded

    // Handle back button to return to Inicio when on secondary tabs
    BackHandler(enabled = state.currentTab != AppTab.Inicio) {
        vm.selectTab(AppTab.Inicio)
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(state.currentTab) {
        scrollState.scrollTo(0)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MontenegroDrawerContent(
                state = state,
                onSelectTab = vm::selectTab,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                onOpenProfile = { showProfileModal = true },
                onOpenTour = vm::openTour,
                onLogout = vm::logout
            )
        }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (!isCompact) {
                val initials = state.session?.displayName
                    ?.split(" ")?.filter { it.isNotBlank() }?.take(2)
                    ?.joinToString("") { it.first().uppercase() }
                    .orEmpty()
                MontenegroNavigationRail(
                    currentTab = state.currentTab,
                    onSelectTab = vm::selectTab,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onOpenProfile = { showProfileModal = true },
                    sessionPhoto = state.session?.foto,
                    initials = initials
                )
            }

            Scaffold(
                topBar = {
                    MontenegroTopBar(
                        searchQuery = state.searchQuery,
                        isDarkTheme = isDark,
                        unreadCount = state.unreadNotifications,
                        onSearchChange = vm::updateSearchQuery,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onToggleTheme = { vm.toggleTheme(isDark) },
                        onOpenNotifications = {
                            vm.markNotificationsSeen()
                            showNotifications = true
                        }
                    )
                },
                bottomBar = {
                    if (isCompact) {
                        MontenegroBottomBar(
                            currentTab = state.currentTab,
                            onSelectTab = vm::selectTab,
                            onOpenMore = { scope.launch { drawerState.open() } }
                        )
                    }
                }
            ) { innerPadding ->
                PullToRefreshBox(
                    isRefreshing = state.isSyncing && state.hasLoadedOnce,
                    onRefresh = vm::sync,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 1080.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (state.syncErrors.isNotEmpty()) {
                                val ext = LocalMontenegroColors.current
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = ext.dangerContainer,
                                    border = BorderStroke(1.dp, ext.dangerContent.copy(alpha = 0.35f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = ext.dangerContent)
                                        Text(
                                            text = "Não foi possível carregar: ${state.syncErrors.joinToString("; ")}",
                                            fontSize = 12.sp,
                                            color = ext.dangerContent,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = vm::sync) {
                                            Text("Tentar de novo", fontWeight = FontWeight.Black, color = ext.dangerContent)
                                        }
                                    }
                                }
                            }

                            when (state.currentTab) {
                                AppTab.Inicio -> InicioScreen(
                                    state = state,
                                    isExpanded = isExpanded,
                                    onNavigateTab = vm::selectTab,
                                    onDeletePonto = { id -> vm.deleteItem("pontos", id) },
                                    onPublishEscala = vm::publishEscala,
                                    onConsumeDraft = vm::consumeEscalaDraft
                                )
                                AppTab.Escalas -> EscalasScreen(
                                    state = state,
                                    onChangeWeek = vm::changeWeek,
                                    onToggleShowAllDrivers = vm::toggleShowAllDrivers,
                                    onCopyPreviousWeek = vm::copyPreviousWeek,
                                    onQuickAddFromGrid = vm::prefillEscalaFromGrid,
                                    onDeleteEscala = { id -> vm.deleteItem("escalas", id) }
                                )
                                AppTab.Colaboradores -> ColaboradoresScreen(
                                    state = state,
                                    api = vm.api,
                                    onCreateMotorista = vm::createMotorista,
                                    onDeleteMotorista = { id -> vm.deleteItem("motoristas", id) }
                                )
                                AppTab.Frota -> FrotaScreen(
                                    state = state,
                                    onCreateFrota = vm::createFrota,
                                    onDeleteFrota = { id -> vm.deleteItem("frota", id) }
                                )
                                AppTab.Comunicados -> ComunicadosScreen(
                                    state = state,
                                    onCreateComunicado = vm::createComunicado,
                                    onDeleteComunicado = { id -> vm.deleteItem("comunicados", id) }
                                )
                                AppTab.Ocorrencias -> OcorrenciasScreen(
                                    state = state,
                                    onCreateOcorrencia = vm::createOcorrencia,
                                    onCreateJustificativa = vm::createJustificativa,
                                    onDeleteItem = vm::deleteItem
                                )
                                AppTab.Ponto -> PontoScreen(
                                    state = state,
                                    onCompressImage = vm::compressPickedImage,
                                    onRegisterPonto = vm::registerPonto,
                                    onCreatePrint = vm::createPrint,
                                    onCreateRelatorio = vm::createRelatorio,
                                    onDeletePonto = { id -> vm.deleteItem("pontos", id) }
                                )
                                AppTab.RelatoriosCompartilhados -> RelatoriosCompartilhadosScreen(
                                    state = state,
                                    api = vm.api,
                                    onRefresh = vm::sync,
                                    onLoadMore = vm::loadMoreRelatorios,
                                    onDeleteItem = vm::deleteItem
                                )
                                AppTab.Contabilidade -> ContabilidadeScreen(state = state)
                                AppTab.Wrapped -> WrappedScreen(
                                    state = state,
                                    onCelebrate = vm::triggerConfetti
                                )
                            }

                            AppFooter()
                        }
                    }
                }
            }
        }
    }

    if (showNotifications) {
        NotificationsDialog(state = state, onDismiss = { showNotifications = false })
    }

    if (showProfileModal) {
        ProfileModalDialog(
            state = state,
            onCompressImage = vm::compressPickedImage,
            onSave = vm::saveProfile,
            onDismiss = { showProfileModal = false }
        )
    }

    if (state.showTour) {
        TourModalDialog(
            step = state.tourStep,
            isAdmin = state.isAdmin,
            onNext = vm::nextTourStep,
            onSkip = vm::closeTour
        )
    }
}
