package com.example.ui.tabs

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppLanguage
import com.example.ui.XmlWatermarkViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.SuccessGreen
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoEraserTab(
    viewModel: XmlWatermarkViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isHindi = language == AppLanguage.HINDI

    val sourceBitmap by viewModel.photoSourceBitmap.collectAsStateWithLifecycle()
    val currentBitmap by viewModel.photoCurrentBitmap.collectAsStateWithLifecycle()
    val brushSize by viewModel.brushSize.collectAsStateWithLifecycle()
    val isErasing by viewModel.isErasingPhoto.collectAsStateWithLifecycle()
    val compareOriginal by viewModel.compareOriginal.collectAsStateWithLifecycle()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        viewModel.setPhotoFromBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                viewModel.showToast(if (isHindi) "फोटो लोड करने में त्रुटि" else "Error loading photo")
            }
        }
    }

    // Export cleaned photo launcher
    val exportPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri: Uri? ->
        if (uri != null && currentBitmap != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    currentBitmap!!.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                    viewModel.showToast(if (isHindi) "फोटो सफलतापूर्वक सेव हो गई!" else "Cleaned photo saved successfully!")
                }
            } catch (e: Exception) {
                viewModel.showToast(if (isHindi) "सेव करने में त्रुटि" else "Save failed: ${e.message}")
            }
        }
    }

    // Local brush path tracking for drawing strokes onto canvas
    var activeTouchPath by remember { mutableStateOf<Path?>(null) }
    val touchPoints = remember { mutableStateListOf<Offset>() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header Card with Quick Erase Presets & Gallery Import
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "फोटो वाटरमार्क रिमूवर" else "Photo Watermark Remover",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isHindi) "वाटरमार्क पर ब्रश चलाएं या 1-टैप में हटाएं" else "Brush over watermarks to inpaint & erase",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Pick Image Button
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_pick_photo")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isHindi) "गैलरी" else "Gallery", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1-Tap Quick Erase Chips for Sample Watermarks
                    Text(
                        text = if (isHindi) "त्वरित वाटरमार्क हटाएं (Quick Clean):" else "Quick 1-Tap Watermark Eraser:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.eraseSampleWatermarkByZone("top_soul") },
                            label = { Text("SAME SOUL", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.eraseSampleWatermarkByZone("shiva_breath") },
                            label = { Text("SHIVA IN BREATH", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.eraseSampleWatermarkByZone("mahadev_rock") },
                            label = { Text("ॐ हर हर महादेव", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.loadSamplePoster() },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            label = { Text(if (isHindi) "रीसेट पोस्टर" else "Reset Sample", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // 2. Interactive Photo Canvas with Touch Brush Masking
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val displayBitmap = if (compareOriginal) sourceBitmap else currentBitmap

                    if (displayBitmap != null) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(displayBitmap.width.toFloat() / displayBitmap.height.toFloat())
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                        ) {
                            val containerWidth = maxWidth
                            val containerHeight = maxHeight

                            // Canvas Image & Overlay
                            Image(
                                bitmap = displayBitmap.asImageBitmap(),
                                contentDescription = if (isHindi) "फोटो" else "Photo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Interactive Drawing Canvas Overlay
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(displayBitmap) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                touchPoints.clear()
                                                touchPoints.add(offset)
                                            },
                                            onDrag = { change, _ ->
                                                touchPoints.add(change.position)
                                            },
                                            onDragEnd = {
                                                if (touchPoints.size > 1) {
                                                    // Map canvas scale to bitmap scale
                                                    val scaleX = displayBitmap.width / size.width.toFloat()
                                                    val scaleY = displayBitmap.height / size.height.toFloat()

                                                    val path = Path()
                                                    path.moveTo(touchPoints[0].x * scaleX, touchPoints[0].y * scaleY)
                                                    for (p in touchPoints) {
                                                        path.lineTo(p.x * scaleX, p.y * scaleY)
                                                    }
                                                    viewModel.addMaskStroke(path, brushSize * scaleX)
                                                }
                                                touchPoints.clear()
                                            }
                                        )
                                    }
                            ) {
                                // Draw active drag line in glowing coral
                                if (touchPoints.size > 1) {
                                    for (i in 0 until touchPoints.size - 1) {
                                        drawLine(
                                            color = Color(0x99EF4444),
                                            start = touchPoints[i],
                                            end = touchPoints[i + 1],
                                            strokeWidth = brushSize
                                        )
                                    }
                                }
                            }

                            // Original Comparison Badge (if holding compare button)
                            if (compareOriginal) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xCC000000))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isHindi) "मूल फोटो (Original)" else "Original Photo",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Brush Controls: Size Slider & Undo / Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "ब्रश आकार:" else "Brush:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = brushSize,
                            onValueChange = { viewModel.setBrushSize(it) },
                            valueRange = 16f..72f,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryBlueLight,
                                activeTrackColor = PrimaryBlue
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Undo Button
                        IconButton(
                            onClick = { viewModel.undoPhoto() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = if (isHindi) "पूर्ववत" else "Undo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Clear Mask
                        IconButton(
                            onClick = { viewModel.clearMask() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = if (isHindi) "मास्क हटाएं" else "Clear mask",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary Erase Button
                    Button(
                        onClick = { viewModel.erasePhotoWatermark() },
                        enabled = !isErasing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_erase_photo_watermark"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        if (isErasing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isHindi) "मिटाया जा रहा है..." else "Erasing Watermark...", fontSize = 15.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "वाटरमार्क मिटाएं (Erase Watermark)" else "Erase Watermark",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Compare Original Button & Save Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Compare Button (Toggle)
                        OutlinedButton(
                            onClick = { viewModel.setCompareOriginal(!compareOriginal) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (compareOriginal) {
                                    if (isHindi) "साफ फोटो" else "Show Clean"
                                } else {
                                    if (isHindi) "मूल देखें" else "Original"
                                },
                                fontSize = 12.sp
                            )
                        }

                        // Save Cleaned Photo
                        Button(
                            onClick = { exportPhotoLauncher.launch("cleaned_poster.jpg") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isHindi) "फोटो सेव करें" else "Save Photo", fontSize = 12.sp)
                        }

                        // Share Photo
                        OutlinedButton(
                            onClick = {
                                if (currentBitmap != null) {
                                    try {
                                        val cachePath = File(context.cacheDir, "images")
                                        cachePath.mkdirs()
                                        val file = File(cachePath, "cleaned_image.png")
                                        FileOutputStream(file).use { stream ->
                                            currentBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                        }
                                        val contentUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/png"
                                            putExtra(Intent.EXTRA_STREAM, contentUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, if (isHindi) "फोटो शेयर करें" else "Share Cleaned Photo"))
                                    } catch (e: Exception) {
                                        viewModel.showToast(if (isHindi) "शेयर करने में त्रुटि" else "Share failed: ${e.message}")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
