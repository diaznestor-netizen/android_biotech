package com.biobox.biotech.presentation.components.textfields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.biobox.biotech.presentation.theme.*

@Composable
fun BioTechSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Buscar...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryCyan) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondaryDark)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryCyan,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun BioTechTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = PrimaryCyan) } },
            isError = isError,
            shape = RoundedCornerShape(16.dp),
            singleLine = singleLine,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = PrimaryCyan
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = Error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun BioTechPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    passwordVisible: Boolean,
    onPasswordToggle: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = PrimaryCyan) } },
            trailingIcon = {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            isError = isError,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = PrimaryCyan
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = Error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
