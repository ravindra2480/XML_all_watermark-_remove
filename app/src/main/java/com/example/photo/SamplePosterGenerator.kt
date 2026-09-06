package com.example.photo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface

object SamplePosterGenerator {

    /**
     * Generates a sample portrait image inspired by the user's uploaded artwork,
     * containing realistic watermark texts like "SAME SOUL DIFFERENT FORM",
     * "SHIVA IN EVERY BREATH", and "ॐ हर हर महादेव".
     */
    fun createSamplePoster(width: Int = 720, height: Int = 1280): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Sky & Celestial Background Gradient
        val skyGradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.rgb(18, 30, 49),   // Deep twilight indigo
                Color.rgb(45, 68, 98),   // Mountain blue
                Color.rgb(195, 140, 80), // Sunset golden amber
                Color.rgb(240, 185, 110),// Sun horizon
                Color.rgb(40, 35, 30)    // Foreground ground
            ),
            floatArrayOf(0f, 0.35f, 0.55f, 0.65f, 1.0f),
            Shader.TileMode.CLAMP
        )
        val skyPaint = Paint().apply { shader = skyGradient }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)

        // 2. Divine Glowing Sun on Horizon
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                width * 0.52f, height * 0.55f, width * 0.45f,
                intArrayOf(Color.argb(220, 255, 245, 180), Color.argb(120, 255, 180, 80), Color.TRANSPARENT),
                floatArrayOf(0f, 0.4f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width * 0.52f, height * 0.55f, width * 0.45f, sunPaint)

        // 3. Majestic Blue Cosmic Silhouette of Lord Shiva in the sky
        val shivaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(160, 100, 150, 210)
        }
        canvas.drawCircle(width * 0.65f, height * 0.22f, width * 0.28f, shivaPaint)

        // Crescent Moon on forehead
        val moonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 255, 230)
            style = Paint.Style.STROKE
            strokeWidth = 14f
        }
        canvas.drawArc(width * 0.54f, height * 0.05f, width * 0.68f, height * 0.12f, 20f, 160f, false, moonPaint)

        // Tripundra (Three holy lines) & Tilak
        val tripundraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(width * 0.72f, height * 0.12f, width * 0.88f, height * 0.12f, tripundraPaint)
        canvas.drawLine(width * 0.70f, height * 0.14f, width * 0.90f, height * 0.14f, tripundraPaint)
        canvas.drawLine(width * 0.72f, height * 0.16f, width * 0.88f, height * 0.16f, tripundraPaint)

        val redTilakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 38, 38)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width * 0.80f, height * 0.14f, 6f, redTilakPaint)

        // 4. Trishul & Damru Silhouette on the left
        val trishulPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(40, 35, 45)
            strokeWidth = 12f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(width * 0.18f, height * 0.05f, width * 0.18f, height * 0.65f, trishulPaint)
        // Center spear
        canvas.drawLine(width * 0.18f, height * 0.02f, width * 0.18f, height * 0.10f, trishulPaint)
        // Curves
        val trishulProng = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(40, 35, 45)
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }
        canvas.drawArc(width * 0.06f, height * 0.03f, width * 0.30f, height * 0.14f, 0f, 180f, false, trishulProng)

        // 5. Himalayan Snowy Mountain Peaks
        val mountainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(90, 110, 130)
            style = Paint.Style.FILL
        }
        val mountainPath = Path().apply {
            moveTo(0f, height * 0.62f)
            lineTo(width * 0.25f, height * 0.50f)
            lineTo(width * 0.45f, height * 0.58f)
            lineTo(width * 0.65f, height * 0.48f)
            lineTo(width * 0.88f, height * 0.54f)
            lineTo(width.toFloat(), height * 0.51f)
            lineTo(width.toFloat(), height.toFloat())
            lineTo(0f, height.toFloat())
            close()
        }
        canvas.drawPath(mountainPath, mountainPaint)

        // Mountain Snow Highlights
        val snowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(180, 240, 248, 255)
            style = Paint.Style.FILL
        }
        val snowPath = Path().apply {
            moveTo(width * 0.20f, height * 0.52f)
            lineTo(width * 0.25f, height * 0.50f)
            lineTo(width * 0.30f, height * 0.53f)
            lineTo(width * 0.25f, height * 0.56f)
            close()
        }
        canvas.drawPath(snowPath, snowPaint)

        // 6. Sacred Stone Temple (Kedarnath Style) on the Right
        val templePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(75, 70, 65)
            style = Paint.Style.FILL
        }
        val templePath = Path().apply {
            moveTo(width * 0.70f, height * 0.72f)
            lineTo(width * 0.86f, height * 0.58f) // Shikhara peak
            lineTo(width * 0.98f, height * 0.72f)
            lineTo(width * 0.98f, height * 0.80f)
            lineTo(width * 0.68f, height * 0.80f)
            close()
        }
        canvas.drawPath(templePath, templePaint)

        // Glowing Temple Sanctum Door
        val doorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(255, 200, 80)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(width * 0.78f, height * 0.70f, width * 0.84f, height * 0.78f, 10f, 10f, doorPaint)

        // 7. Foreground Devotee Silhouette (left-center)
        val devoteePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(25, 22, 28)
            style = Paint.Style.FILL
        }
        // Devotee head & body
        canvas.drawCircle(width * 0.35f, height * 0.46f, width * 0.16f, devoteePaint)
        val bodyPath = Path().apply {
            moveTo(width * 0.12f, height.toFloat())
            lineTo(width * 0.18f, height * 0.58f)
            lineTo(width * 0.55f, height * 0.60f)
            lineTo(width * 0.68f, height.toFloat())
            close()
        }
        canvas.drawPath(bodyPath, devoteePaint)

        // 8. Foreground Sacred Boulder with Inscribed Mantra
        val rockPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(45, 42, 40)
            style = Paint.Style.FILL
        }
        val rockPath = Path().apply {
            moveTo(width * 0.72f, height.toFloat())
            lineTo(width * 0.72f, height * 0.80f)
            lineTo(width * 0.96f, height * 0.76f)
            lineTo(width.toFloat(), height * 0.86f)
            lineTo(width.toFloat(), height.toFloat())
            close()
        }
        canvas.drawPath(rockPath, rockPaint)

        // 9. DRAW THE WATERMARK TEXTS (The exact ones to be erased!)
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(220, 255, 255, 255)
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 2f, 2f, Color.BLACK)
        }

        // Top-Center Watermark: "SAME SOUL DIFFERENT FORM"
        watermarkPaint.textSize = 24f
        canvas.drawText("SAME SOUL", width * 0.38f, height * 0.08f, watermarkPaint)
        canvas.drawText("DIFFERENT FORM", width * 0.38f, height * 0.11f, watermarkPaint)

        // Top-Right Watermark: "SHIVA IN EVERY BREATH"
        watermarkPaint.textSize = 22f
        canvas.drawText("SHIVA", width * 0.85f, height * 0.07f, watermarkPaint)
        canvas.drawText("IN EVERY", width * 0.85f, height * 0.10f, watermarkPaint)
        canvas.drawText("BREATH", width * 0.85f, height * 0.13f, watermarkPaint)
        canvas.drawText("ॐ", width * 0.85f, height * 0.17f, watermarkPaint)

        // Bottom-Right Rock Watermark: "ॐ हर हर महादेव"
        val hindiWatermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(230, 230, 230, 230)
            textSize = 34f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(4f, 2f, 2f, Color.BLACK)
        }
        canvas.drawText("ॐ", width * 0.87f, height * 0.82f, hindiWatermarkPaint)
        canvas.drawText("हर हर", width * 0.87f, height * 0.86f, hindiWatermarkPaint)
        canvas.drawText("महादेव", width * 0.87f, height * 0.90f, hindiWatermarkPaint)

        return bitmap
    }
}
