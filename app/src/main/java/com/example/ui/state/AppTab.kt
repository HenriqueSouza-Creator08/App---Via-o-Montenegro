package com.example.ui.state

import kotlinx.serialization.Serializable

@Serializable
enum class AppTab(
    val routeKey: String,
    val label: String
) {
    Inicio("inicio", "Início"),
    Escalas("escalas", "Escalas"),
    Colaboradores("colaboradores", "Equipe"),
    Frota("frota", "Frota"),
    Comunicados("comunicados", "Comunicados"),
    Ocorrencias("ocorrencias", "Ocorrências"),
    Ponto("ponto", "Ponto & Relatório"),
    RelatoriosCompartilhados("relatorioscompartilhados", "Relatórios & Prints"),
    Contabilidade("contabilidade", "Faturamento"),
    Wrapped("wrapped", "Wrapped");

    val isViagensSubmenu: Boolean
        get() = this == Ponto || this == RelatoriosCompartilhados
}
