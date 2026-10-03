package com.tixku.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A gradient placeholder poster. Real posters would come from Object Storage via
 * the CDN in the system design; here we render a colored card with the title.
 */
@Composable
fun MoviePoster(
    title: String,
    posterColor: Long,
    modifier: Modifier = Modifier,
) {
    val base = Color(posterColor)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(base, base.copy(alpha = 0.65f), Color.Black.copy(alpha = 0.55f))
                )
            ),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
        )
    }
}
