package com.aiagent.app.ui.animation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aiagent.app.ui.theme.ShapeTokens

object AnimationSpecs {
    const val SHORT_DURATION = 150
    const val MEDIUM_DURATION = 300
    const val LONG_DURATION = 500

    fun <T> tweenMedium(): TweenSpec<T> = tween(
        durationMillis = MEDIUM_DURATION,
        easing = FastOutSlowInEasing
    )

    fun <T> tweenShort(): TweenSpec<T> = tween(
        durationMillis = SHORT_DURATION,
        easing = FastOutSlowInEasing
    )

    fun <T> tweenLong(): TweenSpec<T> = tween(
        durationMillis = LONG_DURATION,
        easing = FastOutSlowInEasing
    )
}

enum class ShapeMorphType {
    EXTRA_SMALL,
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE,
    FULL
}

@Composable
fun animateShapeMorph(
    targetType: ShapeMorphType
): Shape {
    val targetRadius = when (targetType) {
        ShapeMorphType.EXTRA_SMALL -> ShapeTokens.CornerExtraSmall
        ShapeMorphType.SMALL -> ShapeTokens.CornerSmall
        ShapeMorphType.MEDIUM -> ShapeTokens.CornerMedium
        ShapeMorphType.LARGE -> ShapeTokens.CornerLarge
        ShapeMorphType.EXTRA_LARGE -> ShapeTokens.CornerExtraLarge
        ShapeMorphType.FULL -> ShapeTokens.CornerFull
    }

    val animatedRadius by animateDpAsState(
        targetValue = targetRadius,
        animationSpec = AnimationSpecs.tweenMedium(),
        label = "shapeMorph"
    )

    return RoundedCornerShape(animatedRadius)
}

fun Modifier.swipeToDismiss(
    onDismiss: () -> Unit,
    dismissThreshold: Dp = 100.dp
): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onHorizontalDrag = { _, dragAmount ->
                totalDrag += dragAmount
            },
            onDragEnd = {
                if (totalDrag < -dismissThreshold.toPx()) {
                    onDismiss()
                }
            }
        )
    }
)

fun Modifier.swipeAction(
    onSwipeRight: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    swipeThreshold: Dp = 100.dp
): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onHorizontalDrag = { _, dragAmount ->
                totalDrag += dragAmount
            },
            onDragEnd = {
                when {
                    totalDrag > swipeThreshold.toPx() -> onSwipeRight()
                    totalDrag < -swipeThreshold.toPx() -> onSwipeLeft()
                }
            }
        )
    }
)

@Composable
fun Modifier.animateShapeChange(
    shapeType: ShapeMorphType
): Modifier = this.then(
    Modifier
        .animateContentSize(animationSpec = AnimationSpecs.tweenMedium())
        .clip(animateShapeMorph(shapeType))
)
