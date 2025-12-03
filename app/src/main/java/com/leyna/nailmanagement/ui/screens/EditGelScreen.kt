package com.leyna.nailmanagement.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.leyna.nailmanagement.data.entity.Gel

@Composable
fun EditGelContent(
    gel: Gel?,
    onSave: (name: String, price: Double, colorCode: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf(gel?.name ?: "") }
    var price by rememberSaveable { mutableStateOf(gel?.price?.toString() ?: "") }
    var colorCode by rememberSaveable { mutableStateOf(gel?.colorCode ?: "") }

    var nameError by rememberSaveable { mutableStateOf(false) }
    var priceError by rememberSaveable { mutableStateOf(false) }
    var colorCodeError by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = false
            },
            label = { Text("Name") },
            isError = nameError,
            supportingText = if (nameError) {
                { Text("Name is required") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = price,
            onValueChange = {
                price = it
                priceError = false
            },
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = priceError,
            supportingText = if (priceError) {
                { Text("Valid price is required") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = colorCode,
            onValueChange = {
                colorCode = it
                colorCodeError = false
            },
            label = { Text("Color Code (e.g., #FF5733)") },
            isError = colorCodeError,
            supportingText = if (colorCodeError) {
                { Text("Color code is required") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val isNameValid = name.isNotBlank()
                val isPriceValid = price.toDoubleOrNull() != null && price.toDouble() >= 0
                val isColorCodeValid = colorCode.isNotBlank()

                nameError = !isNameValid
                priceError = !isPriceValid
                colorCodeError = !isColorCodeValid

                if (isNameValid && isPriceValid && isColorCodeValid) {
                    onSave(name.trim(), price.toDouble(), colorCode.trim())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (gel == null) "Save Gel" else "Update Gel")
        }
    }
}