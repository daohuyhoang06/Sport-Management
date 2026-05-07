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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    var selectedDate by rememberSaveable { mutableStateOf("Ngày sinh") }
    var isAccepted by rememberSaveable { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AuthScreenScaffold(
        title = "Tạo tài khoản",
        subtitle = "Tham gia và khám phá những sân bóng tuyệt vời",
        heroTitle = "Đăng ký",
        modifier = modifier
    ) {
        AuthTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = "Họ và tên",
            leadingIcon = Icons.Outlined.Person
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Số điện thoại",
            leadingIcon = Icons.Outlined.Phone,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Mật khẩu",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisible = { passwordVisible = !passwordVisible }
        )

        Spacer(Modifier.height(12.dp))

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Nhập lại mật khẩu",
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
                    checkmarkColor = androidx.compose.ui.graphics.Color.White
                )
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Tôi đồng ý với điều khoản sử dụng",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(10.dp))

        AuthPrimaryButton(text = "Đăng ký", onClick = {
            Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
            onRegisterSuccess()
        })

        AuthDivider()

        SocialAuthButton(
            text = "Đăng ký với Google",
            accentText = "G",
            accentColor = Color(0xFFDB4437)
        )

        Spacer(Modifier.height(10.dp))

        SocialAuthButton(
            text = "Đăng ký với Facebook",
            accentText = "f",
            accentColor = Color(0xFF1877F2)
        )

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đã có tài khoản? ",
                color = Color(0xFF6B7280)
            )
            Text(
                text = "Đăng nhập ngay",
                color = SportPrimary,
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
        label = "Ngày sinh",
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
