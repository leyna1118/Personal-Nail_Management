package com.leyna.nailmanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.leyna.nailmanagement.data.entity.Gel

@Composable
fun GelDetailContent(
    gel: Gel,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Color preview
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(parseColor(gel.colorCode))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                DetailRow(label = "Name", value = gel.name)
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = "Price", value = "$${String.format("%.2f", gel.price)}")
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow(label = "Color Code", value = gel.colorCode)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Edit")
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun parseColor(colorCode: String): Color {
    return try {
        val cleanCode = colorCode.removePrefix("#")
        val colorLong = cleanCode.toLong(16)
        when (cleanCode.length) {
            6 -> Color(0xFF000000 or colorLong)
            8 -> Color(colorLong)
            else -> Color.Gray
        }
    } catch (e: Exception) {
        Color.Gray
    }
}