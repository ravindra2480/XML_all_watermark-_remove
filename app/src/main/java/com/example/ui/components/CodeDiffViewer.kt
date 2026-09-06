package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WatermarkItem
import com.example.ui.theme.SyntaxAttr
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxRemoved
import com.example.ui.theme.SyntaxTag
import com.example.ui.theme.SyntaxValue

@Composable
fun CodeDiffViewer(
    originalXml: String,
    cleanedXml: String,
    removedItems: List<WatermarkItem>,
    isDiffMode: Boolean,
    modifier: Modifier = Modifier
) {
    val removedLineIndices = remember(removedItems) {
        removedItems.map { it.lineIndex }.toSet()
    }

    val displayLines = remember(originalXml, cleanedXml, isDiffMode) {
        if (isDiffMode) originalXml.lines() else cleanedXml.lines()
    }

    val horizScroll = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .padding(vertical = 8.dp)
            .testTag("code_diff_viewer")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizScroll)
        ) {
            itemsIndexed(displayLines) { index, line ->
                val isRemoved = isDiffMode && removedLineIndices.contains(index)
                CodeLineRow(
                    lineNumber = index + 1,
                    line = line,
                    isRemoved = isRemoved
                )
            }
        }
    }
}

@Composable
private fun CodeLineRow(
    lineNumber: Int,
    line: String,
    isRemoved: Boolean
) {
    val backgroundColor = if (isRemoved) Color(0x33EF4444) else Color.Transparent
    val lineNumColor = if (isRemoved) Color(0xFFEF4444) else Color(0xFF64748B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Removed Indicator (- or space)
        Text(
            text = if (isRemoved) "-" else " ",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isRemoved) Color(0xFFEF4444) else Color.Transparent,
            modifier = Modifier.width(14.dp)
        )

        // Line Number
        Text(
            text = lineNumber.toString().padStart(3, ' '),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = lineNumColor,
            modifier = Modifier.width(34.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Syntax Highlighted Text
        val annotatedText = remember(line, isRemoved) {
            buildAnnotatedXmlString(line, isRemoved)
        }

        Text(
            text = annotatedText,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

private fun buildAnnotatedXmlString(line: String, isRemoved: Boolean) = buildAnnotatedString {
    val trimmed = line.trim()

    if (isRemoved) {
        withStyle(SpanStyle(color = SyntaxRemoved, fontWeight = FontWeight.Medium)) {
            append(line)
        }
        return@buildAnnotatedString
    }

    if (trimmed.startsWith("<!--") && trimmed.endsWith("-->")) {
        withStyle(SpanStyle(color = SyntaxComment)) {
            append(line)
        }
        return@buildAnnotatedString
    }

    var i = 0
    val len = line.length
    while (i < len) {
        when {
            line[i] == '<' -> {
                val endTag = line.indexOf('>', i)
                if (endTag != -1) {
                    val tagContent = line.substring(i, endTag + 1)
                    appendColoredTag(tagContent)
                    i = endTag + 1
                } else {
                    withStyle(SpanStyle(color = SyntaxTag)) { append(line[i].toString()) }
                    i++
                }
            }
            else -> {
                withStyle(SpanStyle(color = Color(0xFFE2E8F0))) {
                    append(line[i].toString())
                }
                i++
            }
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendColoredTag(tag: String) {
    var j = 0
    val tLen = tag.length
    var inQuotes = false
    var quoteChar = '"'

    while (j < tLen) {
        val c = tag[j]
        if (c == '"' || c == '\'') {
            if (inQuotes && c == quoteChar) {
                withStyle(SpanStyle(color = SyntaxValue)) { append(c.toString()) }
                inQuotes = false
            } else if (!inQuotes) {
                inQuotes = true
                quoteChar = c
                withStyle(SpanStyle(color = SyntaxValue)) { append(c.toString()) }
            } else {
                withStyle(SpanStyle(color = SyntaxValue)) { append(c.toString()) }
            }
            j++
            continue
        }

        if (inQuotes) {
            withStyle(SpanStyle(color = SyntaxValue)) { append(c.toString()) }
            j++
            continue
        }

        when (c) {
            '<', '>', '/', '?' -> {
                withStyle(SpanStyle(color = SyntaxTag, fontWeight = FontWeight.Bold)) {
                    append(c.toString())
                }
            }
            '=' -> {
                withStyle(SpanStyle(color = Color(0xFF94A3B8))) {
                    append(c.toString())
                }
            }
            else -> {
                withStyle(SpanStyle(color = SyntaxAttr)) {
                    append(c.toString())
                }
            }
        }
        j++
    }
}
