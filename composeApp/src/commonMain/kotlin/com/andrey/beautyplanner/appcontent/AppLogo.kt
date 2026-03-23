package com.andrey.beautyplanner.appcontent

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppLogo {

    val BPHand: ImageVector by lazy {
        ImageVector.Builder(
            name = "BPHand",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 1024f,
            viewportHeight = 1024f
        ).apply {

            path(
                fill = SolidColor(Color(0xFFFFA5C8)),
                stroke = null,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(196f, 250f)
                curveTo(170f, 220f, 178f, 178f, 220f, 158f)
                curveTo(290f, 125f, 390f, 112f, 510f, 136f)
                curveTo(626f, 158f, 742f, 150f, 828f, 190f)
                curveTo(892f, 220f, 902f, 286f, 866f, 332f)
                curveTo(820f, 392f, 822f, 468f, 860f, 538f)
                curveTo(900f, 618f, 882f, 716f, 802f, 750f)
                curveTo(690f, 798f, 552f, 832f, 416f, 814f)
                curveTo(308f, 800f, 242f, 820f, 194f, 764f)
                curveTo(136f, 696f, 176f, 612f, 170f, 524f)
                curveTo(162f, 414f, 246f, 332f, 196f, 250f)
                close()
            }

            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF1B1F2A)),
                strokeLineWidth = 18f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(740f, 330f)
                curveTo(792f, 278f, 864f, 276f, 914f, 330f)
            }

            path(
                fill = SolidColor(Color(0xFFFFD8CF)),
                stroke = SolidColor(Color(0xFF1B1F2A)),
                strokeLineWidth = 18f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(430f, 800f)
                curveTo(365f, 780f, 330f, 742f, 322f, 686f)
                curveTo(312f, 616f, 334f, 560f, 376f, 512f)
                curveTo(418f, 466f, 452f, 418f, 444f, 356f)
                curveTo(438f, 308f, 466f, 280f, 504f, 286f)
                curveTo(540f, 292f, 554f, 330f, 556f, 374f)
                curveTo(560f, 452f, 542f, 540f, 588f, 600f)
                curveTo(624f, 646f, 656f, 656f, 708f, 640f)
                curveTo(744f, 628f, 784f, 644f, 790f, 682f)
                curveTo(798f, 734f, 768f, 784f, 712f, 806f)
                curveTo(640f, 834f, 524f, 840f, 430f, 800f)
                close()
            }

            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFFE58AAE)),
                strokeLineWidth = 20f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(426f, 706f)
                lineTo(510f, 776f)
                lineTo(592f, 706f)
            }

            fun nail(cx: Float, cy: Float, w: Float, h: Float) {
                path(
                    fill = SolidColor(Color(0xFF8B0056)),
                    stroke = SolidColor(Color(0xFF1B1F2A)),
                    strokeLineWidth = 14f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                    pathFillType = PathFillType.NonZero
                ) {
                    moveTo(cx, cy - h * 0.55f)
                    curveTo(cx + w * 0.55f, cy - h * 0.55f, cx + w * 0.70f, cy, cx, cy + h * 0.55f)
                    curveTo(cx - w * 0.70f, cy, cx - w * 0.55f, cy - h * 0.55f, cx, cy - h * 0.55f)
                    close()
                }
            }

            nail(520f, 320f, 92f, 150f)
            nail(450f, 360f, 78f, 130f)
            nail(390f, 420f, 74f, 120f)
            nail(640f, 360f, 102f, 170f)

            path(
                fill = SolidColor(Color(0x00000000)),
                stroke = SolidColor(Color(0xFF1B1F2A)),
                strokeLineWidth = 18f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(230f, 820f)
                lineTo(230f, 680f)
                curveTo(230f, 640f, 265f, 612f, 308f, 612f)
                curveTo(352f, 612f, 386f, 640f, 386f, 680f)
                curveTo(386f, 720f, 352f, 748f, 308f, 748f)
                lineTo(270f, 748f)
                moveTo(270f, 748f)
                lineTo(270f, 820f)

                moveTo(450f, 820f)
                lineTo(450f, 612f)
                lineTo(520f, 612f)
                curveTo(576f, 612f, 612f, 644f, 612f, 690f)
                curveTo(612f, 736f, 576f, 768f, 520f, 768f)
                lineTo(450f, 768f)
            }
        }.build()
    }
}