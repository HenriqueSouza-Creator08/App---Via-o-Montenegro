package com.example.ui.state

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SessionPreferences
import com.example.data.model.*
import com.example.data.network.SupabaseApi
import com.example.domain.EscalaLogic
import com.example.domain.ImageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EscalaDraft(
    val motorista: String = "",
    val dataInicio: LocalDate = EscalaLogic.hojeLocalDate(),
    val dataFim: LocalDate = EscalaLogic.hojeLocalDate(),
    val focusLineTrigger: Long = 0L
)

data class CrachaEvent(
    val id: Long = System.currentTimeMillis(),
    val tipo: String,
    val nome: String,
    val cargo: String,
    val foto: String?,
    val hora: String
)

data class MainUiState(
    val session: UserSession? = null,
    val isLoadingTransition: Boolean = false,
    val isSyncing: Boolean = false,
    val hasLoadedOnce: Boolean = false,
    val syncErrors: List<String> = emptyList(),
    val currentTab: AppTab = AppTab.Inicio,
    val searchQuery: String = "",
    val rememberedEmail: String = "",
    val rememberMe: Boolean = true,
    val darkThemeOverride: Boolean? = null,
    val notifVistas: Int = 0,
    val showTour: Boolean = false,
    val tourStep: Int = 0,
    val currentClock: String = EscalaLogic.relogioBrasiliaAtual(),
    val semanaRef: LocalDate = EscalaLogic.inicioSemana(EscalaLogic.hojeLocalDate()),
    val showAllDriversInGrid: Boolean = false,
    val relatoriosLimit: Int = 40,
    val escalaDraft: EscalaDraft? = null,
    val toastMessage: String? = null,
    val errorAlertMessage: String? = null,
    val crachaEvent: CrachaEvent? = null,
    val confettiBurstId: Long = 0L,
    val isSubmittingAction: Boolean = false,
    val escalas: List<Escala> = emptyList(),
    val motoristas: List<Motorista> = emptyList(),
    val relatorios: List<Relatorio> = emptyList(),
    val prints: List<PrintViagem> = emptyList(),
    val comunicados: List<Comunicado> = emptyList(),
    val ocorrencias: List<Ocorrencia> = emptyList(),
    val justificativas: List<Justificativa> = emptyList(),
    val frota: List<VeiculoFrota> = emptyList(),
    val pontos: List<PontoRegistro> = emptyList()
) {
    val isAdmin: Boolean get() = session?.isAdmin == true
    val unreadNotifications: Int get() = maxOf(0, comunicados.size - notifVistas)
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = SessionPreferences(application.applicationContext)
    val api = SupabaseApi()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedSession = prefs.getSavedSession()
            val rememberMe = prefs.getLembrarMe()
            val lastEmail = prefs.getUltimoEmail()
            val darkPref = prefs.darkThemeFlow.first()
            val vistas = prefs.notifVistasFlow.first()
            _uiState.update {
                it.copy(
                    session = savedSession,
                    rememberMe = rememberMe,
                    rememberedEmail = lastEmail,
                    darkThemeOverride = darkPref,
                    notifVistas = vistas
                )
            }
            if (savedSession != null) {
                startAuthenticatedSession(savedSession)
            }
        }

        viewModelScope.launch {
            prefs.darkThemeFlow.collect { dark ->
                _uiState.update { it.copy(darkThemeOverride = dark) }
            }
        }

        viewModelScope.launch {
            prefs.notifVistasFlow.collect { v ->
                _uiState.update { it.copy(notifVistas = v) }
            }
        }

        // Live Brasilia clock ticker (1s)
        viewModelScope.launch {
            while (isActive) {
                _uiState.update { it.copy(currentClock = EscalaLogic.relogioBrasiliaAtual()) }
                delay(1000)
            }
        }

        // Auto-sync every 20s while logged in
        viewModelScope.launch {
            while (isActive) {
                delay(20_000)
                if (_uiState.value.session != null && !_uiState.value.isSyncing) {
                    sync()
                }
            }
        }
    }

    fun login(emailRaw: String, senha: String, tipoAcesso: String, lembrar: Boolean) {
        val email = emailRaw.trim().lowercase()
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingAction = true) }
            val sessionResult: Result<UserSession> = if (tipoAcesso == "admin") {
                if (!email.contains(".admin@montenegro.com") || senha != "admin123") {
                    Result.failure(IllegalArgumentException("Credenciais de Administrador incorretas!"))
                } else {
                    val base = email.substringBefore(".admin")
                    val pretty = base.replaceFirstChar { it.uppercase() } + " (Administração)"
                    val previous = prefs.getSavedSession()
                    val keptPhoto = if (previous?.email == email) previous.foto else null
                    Result.success(
                        UserSession(
                            email = email,
                            cargo = "admin",
                            nomePersonalizado = pretty,
                            bio = "Gestão Operacional",
                            foto = keptPhoto
                        )
                    )
                }
            } else {
                api.loginMotorista(email).mapCatching { mot ->
                    if (mot == null || mot.senha != senha) {
                        throw IllegalArgumentException("E-mail ou senha incorretos!")
                    }
                    UserSession(
                        email = mot.email,
                        cargo = "motorista",
                        motoristaId = mot.id,
                        nomePersonalizado = mot.nome,
                        bio = "Motorista Montenegro",
                        foto = mot.foto
                    )
                }
            }

            sessionResult.fold(
                onSuccess = { session ->
                    prefs.saveSession(session, lembrar, email)
                    _uiState.update {
                        it.copy(
                            isSubmittingAction = false,
                            isLoadingTransition = true,
                            rememberMe = lembrar,
                            rememberedEmail = if (lembrar) email else "",
                            hasLoadedOnce = false
                        )
                    }
                    delay(800)
                    _uiState.update { it.copy(isLoadingTransition = false, session = session) }
                    startAuthenticatedSession(session)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSubmittingAction = false,
                            errorAlertMessage = err.message ?: "Erro ao fazer login"
                        )
                    }
                }
            )
        }
    }

    private fun startAuthenticatedSession(session: UserSession) {
        _uiState.update { it.copy(currentTab = AppTab.Inicio) }
        sync()
        viewModelScope.launch {
            if (!prefs.isTourSeen(session.email)) {
                delay(900)
                if (_uiState.value.session?.email == session.email) {
                    _uiState.update { it.copy(showTour = true, tourStep = 0) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefs.clearSession()
            _uiState.update {
                MainUiState(
                    rememberMe = it.rememberMe,
                    rememberedEmail = it.rememberedEmail,
                    darkThemeOverride = it.darkThemeOverride,
                    notifVistas = it.notifVistas
                )
            }
        }
    }

    fun sync() {
        if (_uiState.value.session == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val snap = api.syncAll()
            _uiState.update {
                it.copy(
                    isSyncing = false,
                    hasLoadedOnce = true,
                    syncErrors = snap.errors,
                    escalas = snap.escalas,
                    motoristas = snap.motoristas,
                    relatorios = snap.relatorios,
                    prints = snap.prints,
                    comunicados = snap.comunicados,
                    ocorrencias = snap.ocorrencias,
                    justificativas = snap.justificativas,
                    frota = snap.frota,
                    pontos = snap.pontos
                )
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleTheme(currentlyDark: Boolean) {
        viewModelScope.launch { prefs.setDarkTheme(!currentlyDark) }
    }

    fun markNotificationsSeen() {
        val count = _uiState.value.comunicados.size
        viewModelScope.launch { prefs.setNotifVistas(count) }
    }

    fun openTour() {
        _uiState.update { it.copy(showTour = true, tourStep = 0) }
    }

    fun nextTourStep() {
        val cur = _uiState.value.tourStep
        if (cur >= 2) closeTour() else _uiState.update { it.copy(tourStep = cur + 1) }
    }

    fun closeTour() {
        val email = _uiState.value.session?.email
        _uiState.update { it.copy(showTour = false) }
        if (email != null) {
            viewModelScope.launch { prefs.markTourSeen(email) }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun clearErrorAlert() {
        _uiState.update { it.copy(errorAlertMessage = null) }
    }

    fun dismissCracha() {
        _uiState.update { it.copy(crachaEvent = null) }
    }

    fun triggerConfetti() {
        _uiState.update { it.copy(confettiBurstId = System.currentTimeMillis()) }
    }

    fun changeWeek(delta: Int) {
        val next = if (delta == 0) {
            EscalaLogic.inicioSemana(EscalaLogic.hojeLocalDate())
        } else {
            _uiState.value.semanaRef.plusWeeks(delta.toLong())
        }
        _uiState.update { it.copy(semanaRef = next) }
    }

    fun toggleShowAllDrivers(show: Boolean) {
        _uiState.update { it.copy(showAllDriversInGrid = show) }
    }

    fun loadMoreRelatorios() {
        _uiState.update { it.copy(relatoriosLimit = it.relatoriosLimit + 40) }
    }

    fun prefillEscalaFromGrid(motorista: String, dia: LocalDate) {
        _uiState.update {
            it.copy(
                currentTab = AppTab.Inicio,
                escalaDraft = EscalaDraft(
                    motorista = motorista,
                    dataInicio = dia,
                    dataFim = dia,
                    focusLineTrigger = System.currentTimeMillis()
                )
            )
        }
    }

    fun consumeEscalaDraft() {
        _uiState.update { it.copy(escalaDraft = null) }
    }

    fun saveProfile(nome: String, bio: String, newPhotoDataUri: String?, onDone: () -> Unit) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            val updated = session.copy(
                nomePersonalizado = nome.trim().ifBlank { session.nomePersonalizado },
                bio = bio.trim().ifBlank { session.bio },
                foto = newPhotoDataUri ?: session.foto
            )
            if (session.motoristaId != null) {
                _uiState.update { it.copy(isSubmittingAction = true) }
                val res = api.updateMotoristaProfile(
                    session.motoristaId,
                    MotoristaProfileUpdate(nome = updated.nomePersonalizado, foto = updated.foto)
                )
                _uiState.update { it.copy(isSubmittingAction = false) }
                if (res.isFailure) {
                    _uiState.update {
                        it.copy(errorAlertMessage = "Erro ao salvar perfil: ${res.exceptionOrNull()?.message}")
                    }
                    return@launch
                }
                if (!updated.foto.isNullOrBlank()) {
                    api.cacheImage("motoristas", session.motoristaId, updated.foto)
                }
            }
            prefs.updatePersistedSessionIfRemembered(updated)
            _uiState.update { it.copy(session = updated, toastMessage = "Perfil atualizado com sucesso!") }
            onDone()
            sync()
        }
    }

    suspend fun compressPickedImage(uri: Uri, maxDim: Int): Result<String> =
        ImageUtils.compressUriToDataUri(getApplication(), uri, maxDim = maxDim)

    fun publishEscala(
        ini: LocalDate,
        fim: LocalDate,
        turno: String,
        motorista: String,
        linha: String,
        prefixo: String,
        onSuccess: () -> Unit
    ) {
        val payload = EscalaInsert(
            data = "${EscalaLogic.formatBR(ini)} até ${EscalaLogic.formatBR(fim)}",
            turno = turno,
            motorista = motorista.trim(),
            linha = linha.trim(),
            prefixo = prefixo.trim(),
            tipo = "Período"
        )
        runInsert("escalas", payload, "Escala publicada com sucesso!", onSuccess)
    }

    fun copyPreviousWeek(novas: List<EscalaInsert>) {
        if (novas.isEmpty()) {
            _uiState.update { it.copy(toastMessage = "Nada novo para copiar da semana passada.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingAction = true) }
            val res = api.insert("escalas", novas)
            _uiState.update { it.copy(isSubmittingAction = false) }
            res.fold(
                onSuccess = {
                    _uiState.update { it.copy(toastMessage = "${novas.size} escala(s) copiadas") }
                    sync()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorAlertMessage = "Erro ao copiar a semana: ${err.message}") }
                }
            )
        }
    }

    fun createMotorista(nome: String, email: String, senha: String, onSuccess: () -> Unit) {
        runInsert(
            "motoristas",
            MotoristaInsert(nome.trim(), email.trim().lowercase(), senha),
            "Motorista cadastrado com sucesso!",
            onSuccess
        )
    }

    fun createFrota(modelo: String, prefixo: String, chassi: String, onSuccess: () -> Unit) {
        runInsert(
            "frota",
            FrotaInsert(modelo.trim(), prefixo.trim(), chassi.trim()),
            "Veículo cadastrado com sucesso!",
            onSuccess
        )
    }

    fun createComunicado(titulo: String, texto: String, onSuccess: () -> Unit) {
        val (d, h) = EscalaLogic.agoraBr()
        runInsert(
            "comunicados",
            ComunicadoInsert(titulo.trim(), texto.trim(), d, h),
            "Comunicado publicado e enviado para o sino de notificações!",
            onSuccess
        )
    }

    fun createOcorrencia(texto: String, onSuccess: () -> Unit) {
        val session = _uiState.value.session ?: return
        val (d, h) = EscalaLogic.agoraBr()
        runInsert(
            "ocorrencias",
            OcorrenciaInsert(session.displayName, session.email, texto.trim(), d, h),
            "Ocorrência registrada!",
            onSuccess
        )
    }

    fun createJustificativa(dataFaltaIso: String, motivo: String, onSuccess: () -> Unit) {
        val session = _uiState.value.session ?: return
        runInsert(
            "justificativas",
            JustificativaInsert(session.displayName, session.email, dataFaltaIso, motivo.trim()),
            "Justificativa enviada!",
            onSuccess
        )
    }

    fun registerPonto(tipo: String, onSuccess: () -> Unit) {
        val session = _uiState.value.session ?: return
        val (d, h) = EscalaLogic.agoraBr()
        val payload = PontoInsert(session.displayName, session.email, tipo, d, h)
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingAction = true) }
            val res = api.insert("pontos", payload)
            _uiState.update { it.copy(isSubmittingAction = false) }
            res.fold(
                onSuccess = {
                    val cracha = if (tipo == "Entrada de Turno" || tipo == "Fim de Turno") {
                        CrachaEvent(
                            tipo = tipo,
                            nome = session.displayName,
                            cargo = if (session.isAdmin) "Administração" else "Motorista",
                            foto = session.foto,
                            hora = h
                        )
                    } else null
                    _uiState.update {
                        it.copy(
                            toastMessage = "\"$tipo\" registrado às $h!",
                            crachaEvent = cracha
                        )
                    }
                    onSuccess()
                    sync()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorAlertMessage = "Erro ao registrar ponto: ${err.message}") }
                }
            )
        }
    }

    fun createPrint(
        estagio: String,
        linha: String,
        prefixo: String,
        quilometragem: String,
        imageDataUri: String,
        onSuccess: () -> Unit
    ) {
        val session = _uiState.value.session ?: return
        val (d, h) = EscalaLogic.agoraBr()
        runInsert(
            "prints",
            PrintInsert(
                motorista = session.displayName,
                autorEmail = session.email,
                estagio = estagio,
                linha = linha.trim(),
                prefixo = prefixo.trim(),
                quilometragem = quilometragem.trim(),
                urlImagem = imageDataUri,
                dataBr = d,
                horaBr = h
            ),
            "Print compartilhado com a equipe!",
            onSuccess
        )
    }

    fun createRelatorio(
        linha: String,
        prefixo: String,
        catracados: Int,
        meia: Int,
        km: Double,
        imageDataUri: String?,
        onSuccess: () -> Unit
    ) {
        val session = _uiState.value.session ?: return
        val (d, h) = EscalaLogic.agoraBr()
        runInsert(
            "relatorios",
            RelatorioInsert(
                autor = session.displayName,
                autorEmail = session.email,
                linha = linha.trim(),
                prefixo = prefixo.trim(),
                catracados = catracados,
                meia = meia,
                km = km,
                urlImagem = imageDataUri,
                dataBr = d,
                horaBr = h
            ),
            "Relatório publicado com sucesso!",
            onSuccess
        )
    }

    fun deleteItem(table: String, id: String) {
        viewModelScope.launch {
            val res = api.delete(table, id)
            res.fold(
                onSuccess = {
                    _uiState.update { it.copy(toastMessage = "Excluído") }
                    sync()
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorAlertMessage = "Erro ao excluir: ${err.message}") }
                }
            )
        }
    }

    private inline fun <reified T> runInsert(
        table: String,
        payload: T,
        okMsg: String,
        crossinline onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingAction = true) }
            val res = api.insert(table, payload)
            _uiState.update { it.copy(isSubmittingAction = false) }
            res.fold(
                onSuccess = {
                    _uiState.update { it.copy(toastMessage = okMsg) }
                    onSuccess()
                    sync()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(errorAlertMessage = "Erro ao salvar ($table): ${err.message}")
                    }
                }
            )
        }
    }
}
