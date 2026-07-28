package com.aiagent.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ShapeTokens = object {
    val CornerExtraSmall = 4.dp
    val CornerSmall = 8.dp
    val CornerMedium = 12.dp
    val CornerLarge = 16.dp
    val CornerExtraLarge = 28.dp
    val CornerFull = 9999.dp
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(ShapeTokens.CornerExtraSmall),
    small = RoundedCornerShape(ShapeTokens.CornerSmall),
    medium = RoundedCornerShape(ShapeTokens.CornerMedium),
    large = RoundedCornerShape(ShapeTokens.CornerLarge),
    extraLarge = RoundedCornerShape(ShapeTokens.CornerExtraLarge),
)
