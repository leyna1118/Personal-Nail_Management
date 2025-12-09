package com.leyna.nailmanagement.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.leyna.nailmanagement.data.entity.Gel
import java.time.format.TextStyle

/**
 * Storage format for mentions: [[gel:ID]]
 * Display format: @GelName (bold)
 *
 * Example:
 * - Storage: "Apply [[gel:123]] then [[gel:456]]"
 * - Display: "Apply @RedGel then @BlueGel"
 */
object MentionFormat {
    private val MENTION_PATTERN = Regex("\\[\\[gel:(\\d+)]]")

    /**
     * Represents a parsed mention with its position in the storage text
     */
    data class ParsedMention(
        val gelId: Long,
        val gelName: String,
        val storageStart: Int,
        val storageEnd: Int,
        val displayStart: Int,
        val displayEnd: Int
    )

    /**
     * Parses all mentions from storage format text
     */
    fun parseMentions(storageText: String, gelsById: Map<Long, Gel>): List<ParsedMention> {
        val mentions = mutableListOf<ParsedMention>()
        var displayOffset = 0

        MENTION_PATTERN.findAll(storageText).forEach { match ->
            val gelId = match.groupValues[1].toLongOrNull() ?: return@forEach
            val gel = gelsById[gelId] ?: return@forEach

            val storageStart = match.range.first
            val storageEnd = match.range.last + 1
            val storageLength = storageEnd - storageStart
            val displayText = "@${gel.name}"
            val displayLength = displayText.length

            // Calculate display position considering previous replacements
            val displayStart = storageStart - displayOffset
            val displayEnd = displayStart + displayLength

            mentions.add(
                ParsedMention(
                    gelId = gelId,
                    gelName = gel.name,
                    storageStart = storageStart,
                    storageEnd = storageEnd,
                    displayStart = displayStart,
                    displayEnd = displayEnd
                )
            )

            // Update offset for subsequent mentions
            displayOffset += storageLength - displayLength
        }

        return mentions
    }

    /**
     * Creates the storage format mention string for a gel
     */
    fun createMention(gelId: Long): String = "[[gel:$gelId]]"
}

/**
 * Creates a VisualTransformation that converts storage format to display format
 * with bold styling for mentions
 */
class MentionVisualTransformation(
    private val gelsById: Map<Long, Gel>,
    private val mentionColor: Color = Color.Unspecified
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val storageText = text.text
        val mentions = MentionFormat.parseMentions(storageText, gelsById)

        if (mentions.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Build display text with styling
        val displayText = buildAnnotatedString {
            var lastEnd = 0
            mentions.forEach { mention ->
                // Append text before this mention
                if (mention.storageStart > lastEnd) {
                    append(storageText.substring(lastEnd, mention.storageStart))
                }
                // Append styled mention
                val mentionText = "@${mention.gelName}"
                val start = length
                append(mentionText)
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold, color = mentionColor),
                    start,
                    start + mentionText.length
                )
                lastEnd = mention.storageEnd
            }
            // Append remaining text
            if (lastEnd < storageText.length) {
                append(storageText.substring(lastEnd))
            }
        }

        val offsetMapping = MentionOffsetMapping(storageText, displayText.text, mentions)

        return TransformedText(displayText, offsetMapping)
    }
}

/**
 * Handles offset mapping between storage format and display format
 */
class MentionOffsetMapping(
    private val storageText: String,
    private val displayText: String,
    mentions: List<MentionFormat.ParsedMention>
) : OffsetMapping {

    // Build lookup tables for efficient mapping
    private val storageToDisplayMap: IntArray = IntArray(storageText.length + 1)
    private val displayToStorageMap: IntArray = IntArray(displayText.length + 1)

    init {

        var storageIdx = 0
        var displayIdx = 0
        var mentionIdx = 0

        while (storageIdx <= storageText.length) {
            if (mentionIdx < mentions.size) {
                val mention = mentions[mentionIdx]
                if (storageIdx == mention.storageStart) {
                    // Map entire mention region
                    val displayMentionText = "@${mention.gelName}"
                    val storageLen = mention.storageEnd - mention.storageStart
                    val displayLen = displayMentionText.length

                    // Map storage positions to display positions within mention
                    for (i in 0 until storageLen) {
                        val proportionalPos = (i * displayLen) / storageLen
                        storageToDisplayMap[storageIdx + i] = displayIdx + proportionalPos
                    }
                    storageToDisplayMap[storageIdx + storageLen] = displayIdx + displayLen

                    // Map display positions to storage positions within mention
                    for (i in 0 until displayLen) {
                        val proportionalPos = (i * storageLen) / displayLen
                        displayToStorageMap[displayIdx + i] = storageIdx + proportionalPos
                    }
                    displayToStorageMap[displayIdx + displayLen] = storageIdx + storageLen

                    storageIdx = mention.storageEnd
                    displayIdx += displayLen
                    mentionIdx++
                    continue
                }
            }

            if (storageIdx < storageText.length) {
                storageToDisplayMap[storageIdx] = displayIdx
                if (displayIdx < displayText.length) {
                    displayToStorageMap[displayIdx] = storageIdx
                }
                storageIdx++
                displayIdx++
            } else {
                storageToDisplayMap[storageIdx] = displayIdx
                if (displayIdx <= displayText.length) {
                    displayToStorageMap[displayIdx] = storageIdx
                }
                break
            }
        }

        // Fill remaining display positions
        while (displayIdx <= displayText.length) {
            displayToStorageMap[displayIdx] = storageText.length
            displayIdx++
        }
    }

    override fun originalToTransformed(offset: Int): Int {
        return storageToDisplayMap.getOrElse(offset.coerceIn(0, storageText.length)) {
            displayText.length
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        return displayToStorageMap.getOrElse(offset.coerceIn(0, displayText.length)) {
            storageText.length
        }
    }
}

/**
 * A TextField that supports @mentions with ID-based storage
 *
 * @param storageText The raw text with [[gel:ID]] format mentions
 * @param onStorageTextChange Called when the storage text changes
 * @param allGels List of all available gels for mention suggestions
 * @param onMentionClick Optional callback when a mention is clicked
 * @param modifier Modifier for the TextField
 * @param label Optional label for the TextField
 * @param placeholder Optional placeholder text
 * @param minLines Minimum number of lines to display
 */
@Composable
fun MentionTextField(
    modifier: Modifier = Modifier,
    storageText: String,
    onStorageTextChange: (String) -> Unit,
    allGels: List<Gel>,
    onMentionClick: ((Long) -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    minLines: Int = 1
) {
    val gelsById = remember(allGels) { allGels.associateBy { it.id } }

    // Use remember without key to preserve cursor position
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = storageText)) }

    // Track the last text we sent to parent to distinguish external changes from echo-backs
    var lastEmittedText by remember { mutableStateOf(storageText) }

    // Sync text from parent only if it changed externally (not echoing our own edit back)
    if (storageText != lastEmittedText) {
        textFieldValue = textFieldValue.copy(text = storageText)
        lastEmittedText = storageText
    }

    // Dropdown state
    var showDropdown by remember { mutableStateOf(false) }
    var dropdownGels by remember { mutableStateOf<List<Gel>>(emptyList()) }
    var atPosition by remember { mutableIntStateOf(-1) }

    val visualTransformation = remember(gelsById) {
        MentionVisualTransformation(gelsById)
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                var processedValue = newValue
                var processedText = newValue.text

                // If user edited inside a mention, remove the entire mention
                val oldText = textFieldValue.text
                if (processedText != oldText) {
                    val cleaned = removePartialMentions(processedText)
                    if (cleaned != processedText) {
                        // Calculate cursor adjustment: shift cursor left by removed chars before cursor
                        val cursorPos = processedValue.selection.start
                        val removedBeforeCursor = processedText.substring(0, cursorPos.coerceAtMost(processedText.length)).length -
                            cleaned.substring(0, mapCursorAfterClean(processedText, cleaned, cursorPos)).length
                        val newCursorPos = (cursorPos - removedBeforeCursor).coerceIn(0, cleaned.length)
                        processedText = cleaned
                        processedValue = TextFieldValue(
                            text = cleaned,
                            selection = TextRange(newCursorPos)
                        )
                    }
                }

                val cursorPos = processedValue.selection.start

                // Detect if user is typing after @
                val atIdx = findAtSymbolBeforeCursor(processedText, cursorPos)

                if (atIdx >= 0) {
                    val afterAt = processedText.substring(atIdx + 1, cursorPos)
                    if (afterAt.length < 30 && !afterAt.contains("[[") && !afterAt.contains("]]")) {
                        atPosition = atIdx
                        dropdownGels = if (afterAt.isEmpty()) {
                            allGels
                        } else {
                            allGels.filter { it.name.contains(afterAt, ignoreCase = true) }
                        }
                        showDropdown = dropdownGels.isNotEmpty()
                    } else {
                        showDropdown = false
                    }
                } else {
                    showDropdown = false
                }

                textFieldValue = processedValue
                lastEmittedText = processedText
                onStorageTextChange(processedText)
            },
            modifier = Modifier.fillMaxWidth(),
            label = label,
            placeholder = placeholder,
            minLines = minLines,
            visualTransformation = visualTransformation
        )

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            dropdownGels.forEach { gel ->
                DropdownMenuItem(
                    text = { Text(gel.name) },
                    onClick = {
                        val currentText = textFieldValue.text

                        // Find the end of what the user typed after @
                        val endIdx = findMentionEndPosition(currentText, atPosition)

                        // Replace @query with [[gel:ID]]
                        val mention = MentionFormat.createMention(gel.id)
                        val newText = currentText.replaceRange(atPosition, endIdx, "$mention ")
                        val newCursorPos = atPosition + mention.length + 1

                        textFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newCursorPos)
                        )
                        lastEmittedText = newText
                        onStorageTextChange(newText)
                        showDropdown = false
                    }
                )
            }
        }
    }
}

/**
 * Find the @ symbol that starts a potential mention before the cursor
 */
private fun findAtSymbolBeforeCursor(text: String, cursorPos: Int): Int {
    for (i in cursorPos - 1 downTo 0) {
        val char = text[i]
        when {
            char == '@' -> {
                // Only trigger if @ is at start of text or preceded by whitespace
                return if (i == 0 || text[i - 1].isWhitespace()) i else -1
            }
            char.isWhitespace() -> return -1
            char == '[' || char == ']' -> return -1
        }
    }
    return -1
}

/**
 * Find where the mention text ends (next whitespace or end of string)
 */
private fun findMentionEndPosition(text: String, atPosition: Int): Int {
    for (i in atPosition + 1 until text.length) {
        if (text[i].isWhitespace()) {
            return i
        }
    }
    return text.length
}

private val COMPLETE_MENTION = Regex("\\[\\[gel:\\d+]]")

/**
 * Remove any broken/partial mentions from text, keeping only complete [[gel:ID]] mentions
 */
private fun removePartialMentions(text: String): String {
    // Find all complete mentions and their ranges
    val completeRanges = COMPLETE_MENTION.findAll(text).map { it.range }.toList()

    // Check if any position is inside a complete mention
    fun isInCompleteMention(pos: Int): Boolean =
        completeRanges.any { pos in it }

    // Scan for broken mention fragments: [[ not followed by complete gel:ID]]
    val result = StringBuilder()
    var i = 0
    while (i < text.length) {
        if (isInCompleteMention(i)) {
            // Find which complete mention this belongs to and append it whole
            val range = completeRanges.first { i in it }
            result.append(text.substring(range.first, range.last + 1))
            i = range.last + 1
        } else if (text[i] == '[' && i + 1 < text.length && text[i + 1] == '[') {
            // Start of [[ but not a complete mention — skip until ]] or end
            var j = i + 2
            while (j < text.length) {
                if (text[j] == ']' && j + 1 < text.length && text[j + 1] == ']') {
                    j += 2
                    break
                }
                // If we hit another [[, stop here
                if (text[j] == '[' && j + 1 < text.length && text[j + 1] == '[') {
                    break
                }
                j++
            }
            // Skip the entire broken mention
            i = j
        } else if (text[i] == ']' && i + 1 < text.length && text[i + 1] == ']') {
            // Stray ]] not part of a complete mention — skip
            i += 2
        } else {
            result.append(text[i])
            i++
        }
    }
    return result.toString()
}

/**
 * Map cursor position from original text to cleaned text position
 */
private fun mapCursorAfterClean(original: String, cleaned: String, cursorPos: Int): Int {
    // Count non-removed characters before cursor in original
    val beforeCursor = original.substring(0, cursorPos.coerceAtMost(original.length))
    val cleanedBeforeCursor = removePartialMentions(beforeCursor)
    return cleanedBeforeCursor.length.coerceAtMost(cleaned.length)
}

/**
 * A read-only Text component that displays storage format text with mentions
 * rendered as clickable @GelName with bold styling
 *
 * @param storageText The raw text with [[gel:ID]] format mentions
 * @param allGels List of all gels for resolving IDs to names
 * @param onMentionClick Callback when a mention is clicked
 * @param modifier Modifier for the Text
 * @param color Optional text color override
 */
@Composable
fun MentionText(
    modifier: Modifier = Modifier,
    storageText: String,
    allGels: List<Gel>,
    onMentionClick: ((Long) -> Unit)? = null,
    color: Color = Color.Unspecified
) {
    val gelsById = remember(allGels) { allGels.associateBy { it.id } }

    // Parse mentions from storageText ([[gel:ID]])
    val mentions = remember(storageText, gelsById) {
        MentionFormat.parseMentions(storageText, gelsById)
    }

    // Build AnnotatedString with StringAnnotation for clickable mentions
    val annotatedText = remember(storageText, mentions) {
        buildAnnotatedString {
            var lastEnd = 0
            mentions.forEach { mention ->
                // Append normal text before the mention
                if (mention.storageStart > lastEnd) {
                    append(storageText.substring(lastEnd, mention.storageStart))
                }
                // Append styled mention
                val mentionText = "@${mention.gelName}"
                pushStringAnnotation(tag = "gel_mention", annotation = mention.gelId.toString())
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(mentionText)
                pop() // pop style
                pop() // pop annotation
                lastEnd = mention.storageEnd
            }
            // Append remaining text
            if (lastEnd < storageText.length) {
                append(storageText.substring(lastEnd))
            }
        }
    }

    Text(
        text = annotatedText,
        onTextLayout = {},
        maxLines = Int.MAX_VALUE,
        softWrap = true,
        inlineContent = mapOf(),
        modifier = modifier
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { }
    )
}