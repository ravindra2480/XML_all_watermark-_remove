package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WatermarkItem
import com.example.model.WatermarkType
import com.example.ui.AppLanguage
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PrimaryBlueLight

@Composable
fun WatermarkItemCard(
    item: WatermarkItem,
    language: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHindi = language == AppLanguage.HINDI

    val (badgeColor, badgeBg, typeIcon) = when (item.type) {
        WatermarkType.AUTHOR_CREDIT -> Triple(PrimaryBlueLight, Color(0x2638BDF8), Icons.Default.AlternateEmail)
        WatermarkType.WATERMARK_LAYER -> Triple(AccentAmber, Color(0x26F59E0B), Icons.Default.Layers)
        WatermarkType.SOCIAL_LINK -> Triple(AccentCyan, Color(0x2606B6D4), Icons.Default.Link)
        WatermarkType.TEXT_WATERMARK -> Triple(DangerRed, Color(0x26EF4444), Icons.Default.TextFields)
        WatermarkType.COMMENT_METADATA -> Triple(Color(0xFF94A3B8), Color(0x2664748B), Icons.Default.Comment)
        WatermarkType.CUSTOM_TAG -> Triple(Color(0xFFA855F7), Color(0x26A855F7), Icons.Default.VpnKey)
    }

    val typeLabel = if (isHindi) item.type.labelHi else item.type.labelEn

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (item.isSelected) 1.5.dp else 1.dp,
                color = if (item.isSelected) badgeColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
            .testTag("watermark_item_${item.id}"),
        color = if (item.isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = badgeColor,
                    checkmarkColor = Color.Black
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = typeLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }

                    // Line Number Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF334155))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "L${item.lineNumber}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Matched Text
                Text(
                    text = item.matchedText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Code snippet hint
                Text(
                    text = item.rawSnippet.trim(),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
