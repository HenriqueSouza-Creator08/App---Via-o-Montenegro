package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.SupabaseApi
import com.example.domain.ImageUtils
import com.example.ui.state.CrachaEvent
import com.example.ui.theme.BrandPalette
import com.example.ui.theme.LocalMontenegroColors
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MontenegroCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val ext = LocalMontenegroColors.current
    Surface(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = ext.cardBackground,
        border = BorderStroke(1.dp, ext.cardBorder),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun EmptyStateBox(
    icon: ImageVector,
    title: String,
    subtitle: String,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ext = LocalMontenegroColors.current
    val inf = rememberInfiniteTransition(label = "floaty")
    val offsetY by inf.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 10.dp else 28.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .offset(y = offsetY.dp)
                .size(if (compact) 54.dp else 76.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ext.limePillBackground, BrandPalette.OperationalEmerald.copy(alpha = 0.14f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ext.limePillContent,
                    modifier = Modifier.size(if (compact) 24.dp else 32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(if (compact) 8.dp else 14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = ext.mutedText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DeleteChipButton(
    onClick: () -> Unit,
    label: String = "Excluir",
    testTag: String = "delete_button"
) {
    val ext = LocalMontenegroColors.current
    Surface(
        onClick = onClick,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = ext.dangerContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = label,
                tint = ext.dangerContent,
                modifier = Modifier.size(14.dp)
            )
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ext.dangerContent
                )
            }
        }
    }
}

@Composable
fun LazySupabaseImage(
    api: SupabaseApi,
    table: String,
    id: String,
    column: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: (@Composable () -> Unit)? = null
) {
    var bitmap by remember(table, id, column) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(table, id, column) { mutableStateOf(true) }

    LaunchedEffect(table, id, column) {
        loading = true
        val raw = api.getCachedImage(table, id) ?: api.fetchLazyImage(table, id, column)
        bitmap = ImageUtils.decodeDataUri(raw)
        loading = false
    }

    if (loading) {
        ShimmerBox(modifier = modifier.height(height).clip(RoundedCornerShape(16.dp)))
    } else if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
                .height(height)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, LocalMontenegroColors.current.cardBorder, RoundedCornerShape(16.dp))
        )
    } else {
        fallback?.invoke()
    }
}

@Composable
fun DataUriImage(
    dataUri: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable () -> Unit
) {
    var bitmap by remember(dataUri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(dataUri) {
        bitmap = ImageUtils.decodeDataUri(dataUri)
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        fallback()
    }
}

@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val ext = LocalMontenegroColors.current
    val inf = rememberInfiniteTransition(label = "shimmer")
    val alpha by inf.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )
    Box(
        modifier = modifier
            .background(ext.cardBorder.copy(alpha = alpha))
    )
}

@Composable
fun ConfirmDeleteDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar exclusão", style = MaterialTheme.typography.titleMedium) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPalette.AlertRed)
            ) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun ConfettiOverlay(burstId: Long) {
    if (burstId == 0L) return
    var active by remember(burstId) { mutableStateOf(true) }
    val progress = remember(burstId) { Animatable(0f) }

    val particles = remember(burstId) {
        val rnd = Random(burstId)
        val palette = listOf(
            Color(0xFF88B822), Color(0xFFB9EA6B), Color(0xFF2563EB),
            Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEC4899)
        )
        List(70) {
            val angle = rnd.nextDouble(-Math.PI * 0.9, -Math.PI * 0.1)
            val speed = rnd.nextFloat() * 0.65f + 0.35f
            ConfettiParticle(
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                size = rnd.nextFloat() * 10f + 8f,
                color = palette[rnd.nextInt(palette.size)],
                spin = rnd.nextFloat() * 720f - 360f
            )
        }
    }

    LaunchedEffect(burstId) {
        active = true
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(2200, easing = LinearEasing))
        active = false
    }

    if (active) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = progress.value
            val originX = size.width * 0.5f
            val originY = size.height * 0.62f
            val gravity = size.height * 1.4f
            particles.forEach { p ->
                val x = originX + p.vx * size.width * t
                val y = originY + p.vy * size.height * t + 0.5f * gravity * t * t
                val alpha = (1f - t).coerceIn(0f, 1f)
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size / 2f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val spin: Float
)

@Composable
fun CrachaOverlay(
    event: CrachaEvent?,
    onDismiss: () -> Unit,
    onCelebrate: () -> Unit
) {
    if (event == null) return
    val context = LocalContext.current
    val isEntrada = event.tipo == "Entrada de Turno"
    val accent = if (isEntrada) BrandPalette.OperationalEmerald else BrandPalette.OperationalBlue
    var readerOk by remember(event.id) { mutableStateOf(false) }

    val dropOffset = remember(event.id) { Animatable(-650f) }
    val dropRot = remember(event.id) { Animatable(-8f) }
    val tapScale = remember(event.id) { Animatable(1f) }
    val ringScale = remember(event.id) { Animatable(0.5f) }
    val ringAlpha = remember(event.id) { Animatable(0f) }

    LaunchedEffect(event.id) {
        readerOk = false
        dropOffset.animateTo(0f, spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow))
        dropRot.animateTo(0f, tween(250))
        delay(200)
        // Badge tap on reader
        dropOffset.animateTo(56f, tween(240))
        tapScale.animateTo(0.93f, tween(120))
        readerOk = true
        // Vibrate
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(VibratorManager::class.java)
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Vibrator::class.java)
                v?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
        onCelebrate()
        ringAlpha.snapTo(0.85f)
        ringScale.snapTo(0.5f)
        ringScale.animateTo(2.6f, tween(650))
        ringAlpha.animateTo(0f, tween(650))
        dropOffset.animateTo(0f, tween(260))
        tapScale.animateTo(1f, tween(200))
        delay(1750)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A).copy(alpha = 0.78f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Cracha card
            Surface(
                modifier = Modifier
                    .offset(y = dropOffset.value.dp)
                    .rotate(dropRot.value)
                    .scale(tapScale.value)
                    .width(216.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 24.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .width(48.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(BrandPalette.Slate200)
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accent)
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VIAÇÃO MONTENEGRO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.6.sp
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    val initials = event.nome.split(" ").filter { it.isNotBlank() }
                        .take(2).joinToString("") { it.first().uppercase() }
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(BrandPalette.Slate200)
                            .border(4.dp, accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        DataUriImage(
                            dataUri = event.foto,
                            contentDescription = event.nome,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        ) {
                            Text(
                                text = initials,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = BrandPalette.Slate700
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = event.nome,
                        color = BrandPalette.Slate900,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Text(
                        text = event.cargo.uppercase(),
                        color = BrandPalette.Slate500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    // Barcode strip
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(24.dp)
                    ) {
                        var x = 0f
                        val bars = listOf(4f, 2f, 6f, 3f, 2f, 5f, 2f, 4f, 3f, 6f, 2f, 4f, 3f, 5f, 2f)
                        var idx = 0
                        while (x < size.width) {
                            val w = bars[idx % bars.size] * 2.2f
                            if (idx % 2 == 0) {
                                drawRect(
                                    color = BrandPalette.Slate900,
                                    topLeft = Offset(x, 0f),
                                    size = androidx.compose.ui.geometry.Size(w.coerceAtMost(size.width - x), size.height)
                                )
                            }
                            x += w
                            idx++
                        }
                    }
                }
            }

            // Card reader
            Box(
                modifier = Modifier
                    .width(176.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(BrandPalette.Slate800)
                    .border(2.dp, BrandPalette.Slate700, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(40.dp)) {
                    if (ringAlpha.value > 0f) {
                        drawCircle(
                            color = BrandPalette.OperationalEmerald.copy(alpha = ringAlpha.value),
                            radius = 16.dp.toPx() * ringScale.value,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (readerOk) BrandPalette.OperationalEmerald else BrandPalette.AlertRed)
                )
            }

            if (readerOk) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEntrada) "Entrada de turno registrada" else "Fim de turno registrado",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = event.hora,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AppFooter(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val ext = LocalMontenegroColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(color = ext.cardBorder)
        Text(
            text = "Viação Montenegro • Versão do App: v2.4.1",
            style = MaterialTheme.typography.bodySmall,
            color = ext.mutedText
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/grupo.souza_/")))
            }) {
                Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Instagram da Empresa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/FrHcSQKZ8ST77B0c5vTXJP")))
            }) {
                Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, tint = ext.emeraldAccent, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Grupo do WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ext.emeraldAccent)
            }
        }
    }
}
