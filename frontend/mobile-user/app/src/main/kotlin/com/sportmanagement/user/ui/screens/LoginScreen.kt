package com.sportmanagement.user.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sportmanagement.user.R
import com.sportmanagement.user.ui.theme.AppControlCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppHeaderGradientEnd
import com.sportmanagement.user.ui.theme.AppHeaderGradientStart
import com.sportmanagement.user.ui.theme.AppPanelCornerRadius
import com.sportmanagement.user.ui.theme.AppScreenHorizontalPadding

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    AuthScreenScaffold(
        title = stringResource(R.string.auth_login_welcome),
        subtitle = stringResource(R.string.auth_login_subtitle),
        heroTitle = stringResource(R.string.auth_login_hero),
        modifier = modifier
    ) {
        AuthTextField(
            value = phone,
            onValueChange = { phone = it },
            label = stringResource(R.string.auth_phone_label),
            leadingIcon = Icons.Outlined.Phone,
            keyboardType = KeyboardType.Phone
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

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Quên mật khẩu?",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { }
        )

        Spacer(Modifier.height(18.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.auth_login_button),
            onClick = {
                Toast.makeText(
                    context,
                    R.string.auth_login_success_toast,
                    Toast.LENGTH_SHORT
                ).show()
                onLoginSuccess()
            }
        )

        AuthDivider()

        SocialAuthButton(
            text = stringResource(R.string.auth_login_google),
            accentText = stringResource(R.string.auth_social_google_short),
            accentColor = Color(0xFFDB4437)
        )

        Spacer(Modifier.height(10.dp))

        SocialAuthButton(
            text = stringResource(R.string.auth_login_facebook),
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
                text = "Bạn chưa có tài khoản? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Text(
                text = "Đăng ký ngay",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

@Composable
fun AuthScreenScaffold(
    title: String,
    subtitle: String,
    heroTitle: String,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            AuthHeader(
                title = title,
                subtitle = subtitle,
                heroTitle = heroTitle
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-22).dp)
                    .padding(horizontal = AppScreenHorizontalPadding),
                shape = RoundedCornerShape(AppPanelCornerRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = heroTitle,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(18.dp))
                    content()
                    Spacer(Modifier.height(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    heroTitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(AppHeaderGradientStart, AppHeaderGradientEnd)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .size(112.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp, top = 8.dp)
                .size(180.dp)
                .clip(RoundedCornerShape(36.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner_app),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = AppScreenHorizontalPadding, vertical = 26.dp)
                .fillMaxWidth(0.72f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }

        Text(
            text = heroTitle,
            color = Color.White.copy(alpha = 0.16f),
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = AppScreenHorizontalPadding, top = 8.dp)
        )
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisible: (() -> Unit)? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null
) {
    val containerModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable { onClick() }
    } else {
        modifier.fillMaxWidth()
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = containerModifier,
        isError = isError,
        readOnly = readOnly,
        singleLine = true,
        shape = RoundedCornerShape(AppControlCornerRadius),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null
            )
        },
        trailingIcon = {
            if (isPassword && onTogglePasswordVisible != null) {
                IconButton(onClick = onTogglePasswordVisible) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = null
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        supportingText = {
            if (errorText != null) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(AppCtaWideHeight),
        shape = RoundedCornerShape(AppCtaCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AuthDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        Text(
            text = stringResource(R.string.auth_divider_login_with),
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
fun SocialAuthButton(
    text: String,
    accentText: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(AppControlCornerRadius),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Surface(
            color = accentColor.copy(alpha = 0.12f),
            shape = CircleShape,
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = accentText,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
