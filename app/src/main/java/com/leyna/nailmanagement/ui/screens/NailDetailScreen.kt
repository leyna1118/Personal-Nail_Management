package com.leyna.nailmanagement.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.leyna.nailmanagement.data.entity.NailStyleWithGels
import com.leyna.nailmanagement.ui.viewmodel.NailStyleViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NailDetailContent(
    modifier: Modifier = Modifier,
    nailStyleWithGels: NailStyleWithGels,
    onEditClick: () -> Unit,
    onGelClick: (Long) -> Unit,
    showEditButton: Boolean = true,
) {
    val nailStyle = nailStyleWithGels.nailStyle
    val gels = nailStyleWithGels.gels
    val steps = NailStyleViewModel.parseSteps(nailStyle.steps)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Main finished image
        if (nailStyle.imagePath != null) {
            AsyncImage(
                model = nailStyle.imagePath,
                contentDescription = "Finished nail style",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Style Name
        Text(
            text = nailStyle.name,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gel Tags
        if (gels.isNotEmpty()) {
            Text(
                text = "Gels Used",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                gels.forEach { gel ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { onGelClick(gel.id) }
                    ) {
                        Text(
                            text = gel.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Steps
        if (steps.isNotEmpty()) {
            Text(
                text = "Steps",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            steps.forEachIndexed { index, step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Step ${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Step image if available
                        if (step.imagePath != null) {
                            AsyncImage(
                                model = step.imagePath,
                                contentDescription = "Step ${index + 1} image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(
                            text = step.text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (showEditButton) {
            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit")
            }
        }
    }
}
