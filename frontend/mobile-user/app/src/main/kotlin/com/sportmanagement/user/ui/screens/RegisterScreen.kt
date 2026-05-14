package com.sportmanagement.user.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.ui.components.auth.AuthStepProgress
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class RegisterFormState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val birthDate: String = "Ngày sinh",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false
)

data class RegisterFormErrors(
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val phone: String? = null
)

data class RegisterProfileState(
    val region: String = "Quận 1",
    val notifyEnabled: Boolean = true,
    val emailOffers: Boolean = true
)

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }

    var currentStep by rememberSaveable { androidx.compose.runtime.mutableIntStateOf(1) }
    var formState by remember { mutableStateOf(RegisterFormState()) }
    var formErrors by remember { mutableStateOf(RegisterFormErrors()) }
    var profileState by remember { mutableStateOf(RegisterProfileState()) }
    val selectedSports = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { mutableStateListOf<String>().apply { addAll(it) } }
        )
    ) { mutableStateListOf<String>() }

    fun validateStepOne(): Boolean {
        val fullNameError = if (formState.fullName.isBlank()) "Vui lòng nhập họ tên" else null
        val emailError = when {
            formState.email.isBlank() -> "Vui lòng nhập email"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(formState.email).matches() -> "Email không hợp lệ"
            else -> null
        }
        val passwordError = when {
            formState.password.length < 6 -> "Mật khẩu tối thiểu 6 ký tự"
            else -> null
        }
        val confirmError = when {
            formState.confirmPassword != formState.password -> "Mật khẩu xác nhận chưa khớp"
            else -> null
        }
        val phoneError = when {
            formState.phone.length < 9 -> "Số điện thoại không hợp lệ"
            else -> null
        }

        formErrors = RegisterFormErrors(
            fullName = fullNameError,
            email = emailError,
            password = passwordError,
            confirmPassword = confirmError,
            phone = phoneError
        )
        return listOf(fullNameError, emailError, passwordError, confirmError, phoneError).all { it == null }
    }

    when (currentStep) {
        1 -> RegisterStepOneScreen(
            formState = formState,
            errors = formErrors,
            onFormChange = { formState = it },
            onBackClick = onNavigateToLogin,
            onNextClick = {
                if (validateStepOne()) currentStep = 2
            },
            onPickDateClick = {
                val now = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        formState = formState.copy(birthDate = dateFormatter.format(calendar.time))
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        )

        2 -> RegisterStepTwoScreen(
            selectedSports = selectedSports.toSet(),
            onToggleSport = { sport ->
                if (selectedSports.contains(sport)) selectedSports.remove(sport) else selectedSports.add(sport)
            },
            onBackClick = { currentStep = 1 },
            onSkipClick = { currentStep = 3 },
            onNextClick = { currentStep = 3 }
        )

        else -> RegisterStepThreeScreen(
            profile = profileState,
            onProfileChange = { profileState = it },
            onBackClick = { currentStep = 2 },
            onSkipClick = {
                Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            },
            onCompleteClick = {
                Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            }
        )
    }
}

@Composable
fun RegisterStepScaffold(
    currentStep: Int,
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(start = 8.dp, top = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(10.dp))
            AuthStepProgress(currentStep = currentStep, totalSteps = 3)
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            content()
            Spacer(Modifier.height(14.dp))
            AuthPrimaryButton(text = primaryButtonText, onClick = onPrimaryClick)
            if (secondaryButtonText != null && onSecondaryClick != null) {
                Text(
                    text = secondaryButtonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 6.dp)
                        .clickable { onSecondaryClick() }
                )
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}
