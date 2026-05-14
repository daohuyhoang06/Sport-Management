package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RegisterStepOneScreen(
    formState: RegisterFormState,
    errors: RegisterFormErrors,
    onFormChange: (RegisterFormState) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onPickDateClick: () -> Unit
) {
    RegisterStepScaffold(
        currentStep = 1,
        title = "Thông tin tài khoản",
        subtitle = "Tạo tài khoản để bắt đầu trải nghiệm đặt sân thể thao.",
        onBackClick = onBackClick,
        primaryButtonText = "Tiếp theo",
        onPrimaryClick = onNextClick
    ) {
        AuthTextField(
            value = formState.fullName,
            onValueChange = { onFormChange(formState.copy(fullName = it)) },
            label = "Họ và tên",
            leadingIcon = Icons.Outlined.Person,
            isError = errors.fullName != null,
            errorText = errors.fullName
        )
        Spacer(Modifier.height(8.dp))
        AuthTextField(
            value = formState.email,
            onValueChange = { onFormChange(formState.copy(email = it)) },
            label = "Email",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
            isError = errors.email != null,
            errorText = errors.email
        )
        Spacer(Modifier.height(8.dp))
        AuthTextField(
            value = formState.password,
            onValueChange = { onFormChange(formState.copy(password = it)) },
            label = "Mật khẩu",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            passwordVisible = formState.passwordVisible,
            onTogglePasswordVisible = { onFormChange(formState.copy(passwordVisible = !formState.passwordVisible)) },
            isError = errors.password != null,
            errorText = errors.password
        )
        Spacer(Modifier.height(8.dp))
        AuthTextField(
            value = formState.confirmPassword,
            onValueChange = { onFormChange(formState.copy(confirmPassword = it)) },
            label = "Xác nhận mật khẩu",
            leadingIcon = Icons.Outlined.CheckCircleOutline,
            isPassword = true,
            passwordVisible = formState.confirmPasswordVisible,
            onTogglePasswordVisible = { onFormChange(formState.copy(confirmPasswordVisible = !formState.confirmPasswordVisible)) },
            isError = errors.confirmPassword != null,
            errorText = errors.confirmPassword
        )
        Spacer(Modifier.height(8.dp))
        AuthTextField(
            value = formState.phone,
            onValueChange = { onFormChange(formState.copy(phone = it)) },
            label = "Số điện thoại",
            leadingIcon = Icons.Outlined.Phone,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
            isError = errors.phone != null,
            errorText = errors.phone
        )
        Spacer(Modifier.height(8.dp))
        AuthTextField(
            value = formState.birthDate,
            onValueChange = { },
            label = "Ngày sinh",
            leadingIcon = Icons.Outlined.CalendarMonth,
            readOnly = true,
            onClick = onPickDateClick
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tiếp theo để chọn môn thể thao yêu thích.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}
