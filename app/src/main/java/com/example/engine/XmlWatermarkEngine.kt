package com.example.engine

import com.example.model.CleanOptions
import com.example.model.CleanResult
import com.example.model.WatermarkItem
import com.example.model.WatermarkType
import java.util.UUID
import java.util.regex.Pattern

object XmlWatermarkEngine {

    // Regex patterns for detecting watermarks
    private val USER_HANDLE_REGEX = Pattern.compile("@[a-zA-Z0-9_]{3,30}")
    private val SOCIAL_LINK_REGEX = Pattern.compile("(?i)(https?://)?(www\\.)?(instagram\\.com|youtube\\.com|t\\.me|telegram\\.me|tiktok\\.com|fb\\.com|facebook\\.com|bit\\.ly)/[a-zA-Z0-9_./-]+")
    private val CREDIT_PHRASES = listOf(
        "created by", "made by", "preset by", "edited by", "edit by",
        "do not copy", "subscribe", "follow on", "follow me",
        "watermark", "credits", "credit:", "creator:", "author:"
    )

    private val WATERMARK_TAG_KEYWORDS = listOf(
        "watermark", "wm_", "_wm", "brand_badge", "creator_watermark",
        "logo_watermark", "overlay_wm", "author_credit"
    )

    /**
     * Scans XML string and returns list of detected watermark items.
     */
    fun scanXml(xml: String, customKeywords: List<String> = emptyList()): List<WatermarkItem> {
        val items = mutableListOf<WatermarkItem>()
        val lines = xml.lines()

        for (i in lines.indices) {
            val line = lines[i]
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // 1. Check for XML Comments
            if (trimmed.startsWith("<!--") && trimmed.endsWith("-->")) {
                val lower = trimmed.lowercase()
                val isWatermarkComment = CREDIT_PHRASES.any { lower.contains(it) } ||
                        USER_HANDLE_REGEX.matcher(trimmed).find() ||
                        SOCIAL_LINK_REGEX.matcher(trimmed).find() ||
                        customKeywords.any { it.isNotBlank() && lower.contains(it.lowercase().trim()) }

                if (isWatermarkComment) {
                    items.add(
                        WatermarkItem(
                            id = "comment_$i",
                            type = WatermarkType.COMMENT_METADATA,
                            matchedText = trimmed.removePrefix("<!--").removeSuffix("-->").trim(),
                            lineIndex = i,
                            lineNumber = i + 1,
                            rawSnippet = line,
                            isFullElement = true
                        )
                    )
                    continue
                }
            }

            // 2. Check for Watermark Layer / Shape tags
            val lowerLine = line.lowercase()
            val isWatermarkLayer = WATERMARK_TAG_KEYWORDS.any { kw ->
                lowerLine.contains("label=\"$kw") ||
                lowerLine.contains("label=\"*$kw") ||
                lowerLine.contains("id=\"$kw") ||
                lowerLine.contains("name=\"$kw") ||
                lowerLine.contains("<watermark") ||
                (lowerLine.contains("watermark") && (lowerLine.contains("<shape") || lowerLine.contains("<clipitem") || lowerLine.contains("<layer") || lowerLine.contains("<g ")))
            }

            if (isWatermarkLayer) {
                items.add(
                    WatermarkItem(
                        id = "layer_$i",
                        type = WatermarkType.WATERMARK_LAYER,
                        matchedText = extractAttribute(line, "label") ?: extractAttribute(line, "id") ?: extractAttribute(line, "name") ?: "Watermark Layer",
                        lineIndex = i,
                        lineNumber = i + 1,
                        rawSnippet = line,
                        isFullElement = true
                    )
                )
                continue
            }

            // 3. Check for Social Links in line
            val socialMatcher = SOCIAL_LINK_REGEX.matcher(line)
            if (socialMatcher.find()) {
                val matched = socialMatcher.group()
                items.add(
                    WatermarkItem(
                        id = "social_$i",
                        type = WatermarkType.SOCIAL_LINK,
                        matchedText = matched,
                        lineIndex = i,
                        lineNumber = i + 1,
                        rawSnippet = line,
                        isFullElement = line.contains("<text") || line.contains("<string") || line.contains("<link")
                    )
                )
                continue
            }

            // 4. Check for User Handles (@username)
            val handleMatcher = USER_HANDLE_REGEX.matcher(line)
            if (handleMatcher.find()) {
                val matched = handleMatcher.group()
                items.add(
                    WatermarkItem(
                        id = "handle_$i",
                        type = WatermarkType.AUTHOR_CREDIT,
                        matchedText = matched,
                        lineIndex = i,
                        lineNumber = i + 1,
                        rawSnippet = line,
                        isFullElement = line.contains("<text") || line.contains("<string")
                    )
                )
                continue
            }

            // 5. Check for Credit Phrases
            val foundPhrase = CREDIT_PHRASES.firstOrNull { phrase -> lowerLine.contains(phrase) }
            if (foundPhrase != null) {
                val extractedText = extractAttribute(line, "text") ?: extractTagContent(line) ?: foundPhrase
                items.add(
                    WatermarkItem(
                        id = "text_$i",
                        type = WatermarkType.TEXT_WATERMARK,
                        matchedText = extractedText.take(50),
                        lineIndex = i,
                        lineNumber = i + 1,
                        rawSnippet = line,
                        isFullElement = line.contains("<text") || line.contains("<string") || line.contains("<clipitem")
                    )
                )
                continue
            }

            // 6. Check for Custom Keywords
            for (kw in customKeywords) {
                val cleanKw = kw.trim()
                if (cleanKw.isNotEmpty() && lowerLine.contains(cleanKw.lowercase())) {
                    items.add(
                        WatermarkItem(
                            id = "custom_${i}_$cleanKw",
                            type = WatermarkType.CUSTOM_TAG,
                            matchedText = cleanKw,
                            lineIndex = i,
                            lineNumber = i + 1,
                            rawSnippet = line,
                            isFullElement = false
                        )
                    )
                    break
                }
            }
        }

        return items
    }

    /**
     * Cleans the XML using the configured options and selected items.
     */
    fun cleanXml(
        originalXml: String,
        detectedItems: List<WatermarkItem>,
        options: CleanOptions
    ): CleanResult {
        val selectedItemMap = detectedItems.filter { it.isSelected }.associateBy { it.lineIndex }
        val lines = originalXml.lines().toMutableList()
        val cleanedLines = mutableListOf<String>()

        var inSkippingMultiLineBlock = false
        var blockCloseTag: String? = null
        val removedItems = mutableListOf<WatermarkItem>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            // If we are inside a multi-line element that was marked for removal
            if (inSkippingMultiLineBlock) {
                val targetTag = blockCloseTag
                if (targetTag != null && line.contains(targetTag)) {
                    inSkippingMultiLineBlock = false
                    blockCloseTag = null
                }
                i++
                continue
            }

            val item = selectedItemMap[i]
            if (item != null) {
                // Check if option allows removing this type
                val shouldRemove = when (item.type) {
                    WatermarkType.TEXT_WATERMARK -> options.removeTextWatermarks
                    WatermarkType.AUTHOR_CREDIT -> options.removeTextWatermarks
                    WatermarkType.SOCIAL_LINK -> options.removeAuthorLinks
                    WatermarkType.WATERMARK_LAYER -> options.removeWatermarkShapes
                    WatermarkType.COMMENT_METADATA -> options.removeComments
                    WatermarkType.CUSTOM_TAG -> true
                }

                if (shouldRemove) {
                    removedItems.add(item)

                    // Case A: Custom Replacement
                    if (options.customReplacementText.isNotBlank()) {
                        val replacedLine = if (item.matchedText.isNotBlank() && line.contains(item.matchedText)) {
                            line.replace(item.matchedText, options.customReplacementText)
                        } else {
                            // Replace text="..." attribute
                            line.replace(Regex("text=\"[^\"]*\""), "text=\"${options.customReplacementText}\"")
                        }
                        cleanedLines.add(replacedLine)
                        i++
                        continue
                    }

                    // Case B: Blank out text attribute instead of removing layer (preserves video timing)
                    if (options.blankOutInsteadOfDelete && (line.contains("text=") || line.contains("<text>"))) {
                        var blankedLine = line.replace(Regex("text=\"[^\"]*\""), "text=\"\"")
                        blankedLine = blankedLine.replace(Regex("<text>[^<]*</text>"), "<text></text>")
                        cleanedLines.add(blankedLine)
                        i++
                        continue
                    }

                    // Case C: Check if this is a single self-closing line or opening of a multi-line block
                    val isSelfClosing = trimmed.endsWith("/>") ||
                            trimmed.endsWith("-->") ||
                            (trimmed.startsWith("<") && trimmed.contains("</") && trimmed.endsWith(">"))

                    if (!isSelfClosing) {
                        // Determine opening tag name like <shape, <layer, <text, <clipitem, <g
                        val tagMatch = Regex("<([a-zA-Z0-9_:-]+)").find(trimmed)
                        if (tagMatch != null) {
                            val tagName = tagMatch.groupValues[1]
                            // If line doesn't have closing tag </tagName>
                            if (!line.contains("</$tagName>")) {
                                inSkippingMultiLineBlock = true
                                blockCloseTag = "</$tagName>"
                            }
                        }
                    }
                    // Skip this line!
                    i++
                    continue
                }
            }

            cleanedLines.add(line)
            i++
        }

        val cleanedXml = cleanedLines.joinToString("\n")
        return CleanResult(
            originalXml = originalXml,
            cleanedXml = cleanedXml,
            removedCount = removedItems.size,
            originalBytes = originalXml.toByteArray(Charsets.UTF_8).size,
            cleanedBytes = cleanedXml.toByteArray(Charsets.UTF_8).size,
            removedItems = removedItems
        )
    }

    private fun extractAttribute(line: String, attrName: String): String? {
        val pattern = Pattern.compile("$attrName=\"([^\"]+)\"")
        val matcher = pattern.matcher(line)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractTagContent(line: String): String? {
        val pattern = Pattern.compile(">([^<]+)<")
        val matcher = pattern.matcher(line)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }
}
