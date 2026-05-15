package com.sportmanagement.user.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight

@Composable
fun RegisterStepOneScreen(
    formState: RegisterFormState,
    errors: RegisterFormErrors,
    onFormChange: (RegisterFormState) -> Unit,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onPickDateClick: () -> Unit
) {
    val birthDateValue = if (formState.birthDate == "Ngày sinh") "" else formState.birthDate

    AuthScreenScaffold(
        title = "Đăng ký",
        onBackClick = onBackClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            RegisterStepProgressCompact(
                currentStep = 1,
                totalSteps = 3
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Thông tin tài khoản",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Tạo tài khoản để bắt đầu trải nghiệm đặt sân thể thao.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            RegisterStepField(
                label = "Họ và tên",
                value = formState.fullName,
                onValueChange = { onFormChange(formState.copy(fullName = it)) },
                placeholder = "Nhập họ và tên (*)",
                errorText = errors.fullName
            )

            Spacer(Modifier.height(12.dp))

            RegisterStepField(
                label = "Số điện thoại",
                value = formState.phone,
                onValueChange = { onFormChange(formState.copy(phone = it)) },
                placeholder = "Nhập số điện thoại (*)",
                keyboardType = KeyboardType.Phone,
                errorText = errors.phone
            )

            Spacer(Modifier.height(12.dp))

            RegisterStepField(
                label = "Email",
                value = formState.email,
                onValueChange = { onFormChange(formState.copy(email = it)) },
                placeholder = "Nhập email (*)",
                keyboardType = KeyboardType.Email,
                errorText = errors.email
            )

            Spacer(Modifier.height(12.dp))

            RegisterStepField(
                label = "Mật khẩu",
                value = formState.password,
                onValueChange = { onFormChange(formState.copy(password = it)) },
                placeholder = "Nhập mật khẩu (*)",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (formState.passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onFormChange(formState.copy(passwordVisible = !formState.passwordVisible))
                        }
                    ) {
                        Icon(
                            imageVector = if (formState.passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                errorText = errors.password
            )

            Spacer(Modifier.height(12.dp))

            RegisterStepField(
                label = "Xác nhận mật khẩu",
                value = formState.confirmPassword,
                onValueChange = { onFormChange(formState.copy(confirmPassword = it)) },
                placeholder = "Nhập lại mật khẩu (*)",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (formState.confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onFormChange(
                                formState.copy(
                                    confirmPasswordVisible = !formState.confirmPasswordVisible
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (formState.confirmPasswordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                errorText = errors.confirmPassword
            )

            Spacer(Modifier.height(12.dp))

            RegisterStepField(
                label = "Ngày sinh",
                value = birthDateValue,
                onValueChange = { },
                placeholder = "Chọn ngày sinh",
                readOnly = true,
                onClick = onPickDateClick,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Spacer(Modifier.height(22.dp))

            AuthPrimaryButton(
                text = "Tiếp theo",
                onClick = onNextClick
            )
        }
    }
}

@Composable
private fun RegisterStepProgressCompact(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val color = if (index < currentStep) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            }

            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(5.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}

@Composable
private fun RegisterStepField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorText: String? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        if (readOnly) {
            RegisterReadOnlyField(
                value = value,
                placeholder = placeholder,
                trailingIcon = trailingIcon,
                onClick = onClick
            )
        } else {
            LoginInputField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                keyboardType = keyboardType,
                visualTransformation = visualTransformation,
                trailingIcon = trailingIcon
            )
        }

        if (errorText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun RegisterReadOnlyField(
    value: String,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)?,
    onClick: (() -> Unit)?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppCtaWideHeight)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(AppCtaCornerRadius),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (value.isBlank()) placeholder else value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )

            if (trailingIcon != null) {
                Box(
                    modifier = Modifier.padding(start = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    trailingIcon()
                }
            }
        }
    }
}
