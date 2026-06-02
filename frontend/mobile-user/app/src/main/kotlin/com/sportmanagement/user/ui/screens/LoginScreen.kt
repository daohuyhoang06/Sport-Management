package com.sportmanagement.user.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sportmanagement.user.R
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppScreenHorizontalPadding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginSubmit: (identifier: String, password: String) -> Unit,
    onGoogleLoginSubmit: (idToken: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var loginWithEmail by rememberSaveable { mutableStateOf(true) }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val googleSignInClient = remember(context) {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .build()
        GoogleSignIn.getClient(context, options)
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Toast.makeText(
                    context,
                    "Đăng nhập Google không thành công. Vui lòng thử lại.",
                    Toast.LENGTH_SHORT
                ).show()
                return@rememberLauncherForActivityResult
            }
            onGoogleLoginSubmit(idToken)
        } catch (error: ApiException) {
            Toast.makeText(
                context,
                "Đăng nhập Google không thành công. Vui lòng thử lại.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AuthScreenScaffold(
            title = stringResource(R.string.auth_login_hero),
            onBackClick = onBackClick,
            belowCardContent = {
                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bạn chưa có tài khoản? ",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Đăng ký",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SocialAuthButton(
                        text = "Google",
                        iconRes = R.drawable.ic_google_official,
                        onClick = {
                            if (isLoading) {
                                return@SocialAuthButton
                            }
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SocialAuthButton(
                        text = "Facebook",
                        iconRes = R.drawable.ic_facebook_official,
                        onClick = {
                            Toast.makeText(
                                context,
                                "Facebook login tren Android can them Facebook app id va SDK config.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        ) {
            CustomLoginTabSelector(
                loginWithEmail = loginWithEmail,
                onChange = { loginWithEmail = it }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                LoginSectionLabel(
                    text = if (loginWithEmail) "Email của bạn?" else "Số điện thoại của bạn?"
                )

                Spacer(Modifier.height(12.dp))

                LoginInputField(
                    value = if (loginWithEmail) email else phone,
                    onValueChange = {
                        if (loginWithEmail) {
                            email = it
                        } else {
                            phone = it
                        }
                    },
                    placeholder = if (loginWithEmail) {
                        "Nhập email của bạn (*)"
                    } else {
                        "Nhập số điện thoại (*)"
                    },
                    keyboardType = if (loginWithEmail) KeyboardType.Email else KeyboardType.Phone,
                    trailingIcon = {
                        val hasValue = if (loginWithEmail) email.isNotBlank() else phone.isNotBlank()
                        if (hasValue) {
                            IconButton(
                                onClick = {
                                    if (loginWithEmail) email = "" else phone = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )

                Spacer(Modifier.height(22.dp))

                LoginSectionLabel(text = "Mật khẩu (*)")

                Spacer(Modifier.height(12.dp))

                LoginInputField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Nhập mật khẩu (*)",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                AuthPrimaryButton(
                    text = stringResource(R.string.auth_login_button),
                    onClick = {
                        if (isLoading) {
                            return@AuthPrimaryButton
                        }
                        val resolvedIdentifier = if (loginWithEmail) email.trim() else phone.trim()
                        onLoginSubmit(resolvedIdentifier, password)
                    }
                )

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bạn quên mật khẩu? ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Quên mật khẩu",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AuthScreenScaffold(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    belowCardContent: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {},
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            AuthHeader(
                title = title,
                onBackClick = onBackClick
            )

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppScreenHorizontalPadding),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppScreenHorizontalPadding)
            ) {
                Spacer(Modifier.height(24.dp))
                belowCardContent()
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
fun CustomLoginTabSelector(
    loginWithEmail: Boolean,
    onChange: (Boolean) -> Unit
) {
    val headerHeight = 58.dp
    val outerRadius = 6.dp
    val shoulderWidth = 18.dp
    val inactiveInnerRadius = 18.dp
    val inactiveColor = Color(0xFFE6E8EC)
    val inactiveOffsetY = 0.dp
    val inactiveBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
    val density = LocalDensity.current
    val activeLeftShape = remember(density) {
        createActiveLoginTabShape(
            isLeft = true,
            outerRadiusPx = with(density) { outerRadius.toPx() },
            shoulderWidthPx = with(density) { shoulderWidth.toPx() }
        )
    }
    val activeRightShape = remember(density) {
        createActiveLoginTabShape(
            isLeft = false,
            outerRadiusPx = with(density) { outerRadius.toPx() },
            shoulderWidthPx = with(density) { shoulderWidth.toPx() }
        )
    }
    val inactiveLeftShape = remember(density) {
        createInactiveLoginTabShape(
            isLeft = true,
            outerRadiusPx = with(density) { outerRadius.toPx() },
            innerCornerPx = with(density) { inactiveInnerRadius.toPx() }
        )
    }
    val inactiveRightShape = remember(density) {
        createInactiveLoginTabShape(
            isLeft = false,
            outerRadiusPx = with(density) { outerRadius.toPx() },
            innerCornerPx = with(density) { inactiveInnerRadius.toPx() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        Surface(
            modifier = Modifier
                .align(if (loginWithEmail) Alignment.TopStart else Alignment.TopEnd)
                .fillMaxWidth(0.5f)
                .offset(y = inactiveOffsetY)
                .height(headerHeight - inactiveOffsetY),
            shape = if (loginWithEmail) inactiveLeftShape else inactiveRightShape,
            color = inactiveColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, inactiveBorderColor),
            tonalElevation = 0.dp,
            shadowElevation = 7.dp
        ) {}

        Surface(
            modifier = Modifier
                .align(if (loginWithEmail) Alignment.TopEnd else Alignment.TopStart)
                .fillMaxWidth(0.5f)
                .height(headerHeight)
                .zIndex(1f),
            shape = if (loginWithEmail) activeRightShape else activeLeftShape,
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {}

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .zIndex(2f)
        ) {
            LoginTabLabel(
                text = stringResource(R.string.auth_phone_label),
                selected = !loginWithEmail,
                modifier = Modifier.weight(1f),
                onClick = { onChange(false) }
            )
            LoginTabLabel(
                text = "Email",
                selected = loginWithEmail,
                modifier = Modifier.weight(1f),
                onClick = { onChange(true) }
            )
        }
    }
}

private fun createActiveLoginTabShape(
    isLeft: Boolean,
    outerRadiusPx: Float,
    shoulderWidthPx: Float
) = GenericShape { size, _ ->
    val outerRadius = outerRadiusPx.coerceAtMost(size.height / 2f)
    val shoulderWidth = shoulderWidthPx.coerceAtMost(size.width / 3f)

    if (isLeft) {
        moveTo(outerRadius, 0f)
        lineTo(size.width - shoulderWidth, 0f)
        cubicTo(
            size.width - shoulderWidth * 0.35f,
            0f,
            size.width - shoulderWidth * 0.12f,
            size.height * 0.72f,
            size.width,
            size.height
        )
        lineTo(0f, size.height)
        lineTo(0f, outerRadius)
        quadraticBezierTo(0f, 0f, outerRadius, 0f)
    } else {
        moveTo(shoulderWidth, 0f)
        lineTo(size.width - outerRadius, 0f)
        quadraticBezierTo(size.width, 0f, size.width, outerRadius)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        cubicTo(
            shoulderWidth * 0.12f,
            size.height * 0.72f,
            shoulderWidth * 0.35f,
            0f,
            shoulderWidth,
            0f
        )
    }
    close()
}

private fun createInactiveLoginTabShape(
    isLeft: Boolean,
    outerRadiusPx: Float,
    innerCornerPx: Float
) = GenericShape { size, _ ->
    val outerRadius = outerRadiusPx.coerceAtMost(size.height / 2f)
    val innerCorner = innerCornerPx.coerceAtMost(size.height / 2f)

    if (isLeft) {
        moveTo(outerRadius, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height - innerCorner)
        cubicTo(
            size.width,
            size.height - innerCorner * 0.35f,
            size.width - innerCorner * 0.35f,
            size.height,
            size.width - innerCorner,
            size.height
        )
        lineTo(0f, size.height)
        lineTo(0f, outerRadius)
        quadraticBezierTo(0f, 0f, outerRadius, 0f)
    } else {
        moveTo(outerRadius, 0f)
        lineTo(size.width - outerRadius, 0f)
        quadraticBezierTo(size.width, 0f, size.width, outerRadius)
        lineTo(size.width, size.height)
        lineTo(innerCorner, size.height)
        cubicTo(
            innerCorner * 0.35f,
            size.height,
            0f,
            size.height - innerCorner * 0.35f,
            0f,
            size.height - innerCorner
        )
        lineTo(0f, outerRadius)
        quadraticBezierTo(0f, 0f, outerRadius, 0f)
    }
    close()
}

@Composable
fun LoginTabLabel(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
            },
            fontSize = 17.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
fun LoginSectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(AppCtaWideHeight),
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
            Box(modifier = Modifier.weight(1f)) {
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = visualTransformation,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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

@Composable
fun AuthHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.booking_back_content_description),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Center)
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
        modifier = containerModifier.heightIn(min = AppCtaWideHeight),
        isError = isError,
        readOnly = readOnly,
        singleLine = true,
        shape = RoundedCornerShape(AppCtaCornerRadius),
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
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 170.dp
        val iconSize = if (compact) 20.dp else 24.dp
        val spacing = if (compact) 7.dp else 10.dp
        val horizontalPadding = if (compact) 10.dp else 16.dp
        val textSize = if (compact) 13.sp else 14.sp

        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(AppCtaWideHeight),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(Modifier.width(spacing))
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = textSize,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
