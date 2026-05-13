package com.sportmanagement.user.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fullName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable { mutableStateOf(context.getString(R.string.auth_birth_date_label)) }
    var isAccepted by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AuthScreenScaffold(
        title = stringResource(R.string.auth_register_title),
        subtitle = stringResource(R.string.auth_register_subtitle),
        heroTitle = stringResource(R.string.auth_register_hero),
        modifier = modifier
    ) {
        AuthTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = stringResource(R.string.auth_full_name_label),
            leadingIcon = Icons.Outlined.Person
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = phone,
            onValueChange = { phone = it },
            label = stringResource(R.string.auth_phone_label),
            leadingIcon = Icons.Outlined.Phone,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.auth_password_label),
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisible = { passwordVisible = !passwordVisible }
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = stringResource(R.string.auth_confirm_password_label),
            leadingIcon = Icons.Outlined.CheckCircleOutline,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onTogglePasswordVisible = { confirmPasswordVisible = !confirmPasswordVisible }
        )

        Spacer(Modifier.height(12.dp))

        AuthDateField(
            value = selectedDate,
            onValueChange = { selectedDate = it },
            dateFormatter = dateFormatter
        )

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Checkbox(
                checked = isAccepted,
                onCheckedChange = { isAccepted = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = SportPrimary,
                    uncheckedColor = SportPrimary,
                    checkmarkColor = Color.White
                )
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.auth_terms_accept),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(10.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_register_button),
            onClick = {
                Toast.makeText(
                    context,
                    context.getString(R.string.auth_register_success_toast),
                    Toast.LENGTH_SHORT
                ).show()
                onRegisterSuccess()
            }
        )

        AuthDivider()

        SocialAuthButton(
            text = stringResource(R.string.auth_register_google),
            accentText = stringResource(R.string.auth_social_google_short),
            accentColor = Color(0xFFDB4437)
        )

        Spacer(Modifier.height(10.dp))

        SocialAuthButton(
            text = stringResource(R.string.auth_register_facebook),
            accentText = stringResource(R.string.auth_social_facebook_short),
            accentColor = Color(0xFF1877F2)
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.auth_have_account),
                color = Color(0xFF6B7280),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.auth_login_now),
                color = SportPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}

@Composable
private fun AuthDateField(
    value: String,
    onValueChange: (String) -> Unit,
    dateFormatter: SimpleDateFormat
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    AuthTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.auth_birth_date_label),
        leadingIcon = Icons.Outlined.CalendarMonth,
        readOnly = true,
        onClick = {
            val now = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    onValueChange(dateFormatter.format(calendar.time))
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    )
}
