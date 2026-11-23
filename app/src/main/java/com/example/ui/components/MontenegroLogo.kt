package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandPalette
import com.example.ui.theme.PlusJakartaSansFontFamily

@Composable
fun MontenegroLogo(
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 112.dp, height = 56.dp)) {
            val sx = size.width / 270f
            val sy = size.height / 130f

            // Hill 1 (olive green)
            val hill1 = Path().apply {
                moveTo(30f * sx, 95f * sy)
                quadraticTo(110f * sx, 25f * sy, 190f * sx, 95f * sy)
                close()
            }
            drawPath(hill1, color = BrandPalette.MontenegroOlive)

            // Hill 2 (lime green)
            val hill2 = Path().apply {
                moveTo(140f * sx, 95f * sy)
                quadraticTo(200f * sx, 40f * sy, 260f * sx, 95f * sy)
                close()
            }
            drawPath(hill2, color = BrandPalette.MontenegroLime)

            // Hill 3 (dark forest green)
            val hill3 = Path().apply {
                moveTo(10f * sx, 95f * sy)
                cubicTo(10f * sx, 75f * sy, 50f * sx, 65f * sy, 75f * sx, 95f * sy)
                close()
            }
            drawPath(hill3, color = BrandPalette.MontenegroDarkGreen)

            // Road swoosh
            val swoosh = Path().apply {
                moveTo(260f * sx, 95f * sy)
                cubicTo(200f * sx, 95f * sy, 180f * sx, 105f * sy, 90f * sx, 105f * sy)
                cubicTo(140f * sx, 115f * sy, 210f * sx, 110f * sy, 250f * sx, 98f * sy)
                cubicTo(210f * sx, 95f * sy, 180f * sx, 82f * sy, 250f * sx, 82f * sy)
                close()
            }
            drawPath(swoosh, color = textColor)
        }

        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "VIAÇÃO",
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                letterSpacing = 1.6.sp,
                color = textColor,
                lineHeight = 14.sp
            )
            Text(
                text = "MONTENEGRO",
                fontFamily = PlusJakartaSansFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 0.4.sp,
                color = textColor,
                lineHeight = 18.sp
            )
        }
    }
}
