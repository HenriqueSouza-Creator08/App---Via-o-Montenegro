package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.EscalaLogic
import com.example.ui.state.MainUiState
import com.example.ui.theme.BrandPalette
import java.util.Locale

@Composable
fun ContabilidadeScreen(state: MainUiState) {
    val stats = remember(state.relatorios, state.motoristas) {
        EscalaLogic.calcularContabilidade(state.relatorios, state.motoristas)
    }
    val ptBr = remember { Locale("pt", "BR") }

    Surface(
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF064E3B), Color(0xFF134E4A), BrandPalette.Slate900)
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "CONTABILIDADE & GESTÃO FINANCEIRA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFF34D399)
                )
                Text(
                    text = "Faturamento Montenegro",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Cálculo automático: R$ 5,40 por inteira e R$ 1,50 por meia.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStatTile(
                    icon = Icons.Rounded.ConfirmationNumber,
                    value = stats.totalCatracados.toString(),
                    label = "Total Catracados",
                    valueColor = Color.White
                )
                HeroStatTile(
                    icon = Icons.Rounded.AddRoad,
                    value = String.format(ptBr, "%.1f KM", stats.totalKm),
                    label = "Quilometragem Rodada",
                    valueColor = Color.White
                )
                HeroStatTile(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    value = EscalaLogic.formatBrl(stats.faturamentoTotal),
                    label = "Faturamento Acumulado",
                    valueColor = Color(0xFF34D399)
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "MÉDIA DE FATURAMENTO MENSAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF6EE7B7)
                    )
                    Text(
                        text = EscalaLogic.formatBrl(stats.mediaMensal),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Média dos meses que têm relatórios registrados.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "PROJEÇÃO ANUAL (12 MESES)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF5EEAD4)
                    )
                    Text(
                        text = EscalaLogic.formatBrl(stats.projecaoAnual),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Média mensal × 12.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}

@Composable
fun WrappedScreen(
    state: MainUiState,
    onCelebrate: () -> Unit
) {
    val stats = remember(state.relatorios, state.motoristas) {
        EscalaLogic.calcularContabilidade(state.relatorios, state.motoristas)
    }
    val ptBr = remember { Locale("pt", "BR") }

    Surface(
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF1E3A8A), Color(0xFF312E81), BrandPalette.Slate900)
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "RETROSPECTIVA MONTENEGRO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color(0xFF60A5FA)
                )
                Text(
                    text = "Montenegro Wrapped",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Resumo automático das conquistas da frota e da equipe.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HeroStatTile(
                    icon = Icons.Rounded.AddRoad,
                    value = String.format(ptBr, "%.1f KM", stats.totalKm),
                    label = "Distância Total Percorrida",
                    valueColor = Color.White
                )
                HeroStatTile(
                    icon = Icons.Rounded.Star,
                    value = stats.destaqueMotorista,
                    label = "Destaque (mais relatórios)",
                    valueColor = Color.White
                )
                HeroStatTile(
                    icon = Icons.Rounded.Flag,
                    value = stats.totalViagens.toString(),
                    label = "Viagens Reportadas",
                    valueColor = Color.White
                )
            }

            Button(
                onClick = onCelebrate,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.OperationalEmerald),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .height(50.dp)
                    .testTag("celebrate_wrapped_button")
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Celebrar Conquistas", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeroStatTile(
    icon: ImageVector,
    value: String,
    label: String,
    valueColor: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}
