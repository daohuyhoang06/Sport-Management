package com.sportmanagement.user.ui.components.profile

import android.app.DatePickerDialog
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppInputCornerRadius
import com.sportmanagement.user.ui.theme.AppSheetTopCornerRadius
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditProfileBottomSheet(
    profile: UserProfile,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    val context = LocalContext.current

    var editedName by rememberSaveable(profile.name) { mutableStateOf(profile.name.ifBlank { "Nguyễn Văn An" }) }
    var editedEmail by rememberSaveable(profile.email) { mutableStateOf(profile.email.ifBlank { "user1@gmail.com" }) }
    var editedPhone by rememberSaveable(profile.phone) { mutableStateOf(profile.phone.ifBlank { "0907890123" }) }
    var editedBirthday by rememberSaveable(profile.birthday) { mutableStateOf(profile.birthday.ifBlank { "10/05/1995" }) }
    var editedGender by rememberSaveable(profile.gender) { mutableStateOf(profile.gender.ifBlank { "Nam" }) }
    var editedLocation by rememberSaveable(profile.location) { mutableStateOf(profile.location.ifBlank { "Cầu Giấy, Hà Nội" }) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var genderExpanded by rememberSaveable { mutableStateOf(false) }
    var locationExpanded by rememberSaveable { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val genderOptions = remember { listOf("Nam", "Nữ", "Khác") }
    val locationOptions = remember {
        listOf(
            "Cầu Giấy, Hà Nội",
            "Ba Đình, Hà Nội",
            "Hoàn Kiếm, Hà Nội",
            "Tây Hồ, Hà Nội"
        )
    }

    fun validateEmail(value: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(value).matches()
    fun validatePhone(value: String): Boolean = Regex("^[0-9]{9,11}$").matches(value)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(
            topStart = AppSheetTopCornerRadius,
            topEnd = AppSheetTopCornerRadius
        ),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 6.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Chỉnh sửa thông tin cá nhân",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(38.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Text(
                text = "Thay đổi ảnh đại diện",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            CustomTextField(
                label = "Họ và tên",
                value = editedName,
                onValueChange = { editedName = it },
                leadingIcon = Icons.Outlined.Person
            )

            CustomTextField(
                label = "Email",
                value = editedEmail,
                onValueChange = {
                    editedEmail = it
                    emailError = null
                },
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                isError = emailError != null,
                errorText = emailError
            )

            CustomTextField(
                label = "Số điện thoại",
                value = editedPhone,
                onValueChange = {
                    editedPhone = it
                    phoneError = null
                },
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                isError = phoneError != null,
                errorText = phoneError
            )

            CustomTextField(
                label = "Ngày sinh",
                value = editedBirthday,
                onValueChange = { },
                leadingIcon = Icons.Outlined.CalendarMonth,
                readOnly = true,
                trailingIcon = Icons.Filled.ArrowDropDown,
                onClick = {
                    val calendar = Calendar.getInstance()
                    val current = runCatching { dateFormatter.parse(editedBirthday) }.getOrNull()
                    if (current != null) {
                        calendar.time = current
                    }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth)
                            editedBirthday = dateFormatter.format(calendar.time)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            )

            CustomTextField(
                label = "Giới tính",
                value = editedGender,
                onValueChange = { },
                leadingIcon = Icons.Outlined.Person,
                readOnly = true,
                trailingIcon = Icons.Filled.ArrowDropDown,
                onClick = {
                    genderExpanded = !genderExpanded
                    if (genderExpanded) locationExpanded = false
                }
            )

            if (genderExpanded) {
                DropdownPanel(options = genderOptions) { selected ->
                    editedGender = selected
                    genderExpanded = false
                }
            }

            CustomTextField(
                label = "Khu vực",
                value = editedLocation,
                onValueChange = { },
                leadingIcon = Icons.Outlined.LocationOn,
                readOnly = true,
                trailingIcon = Icons.Filled.ArrowDropDown,
                onClick = {
                    locationExpanded = !locationExpanded
                    if (locationExpanded) genderExpanded = false
                }
            )

            if (locationExpanded) {
                DropdownPanel(options = locationOptions) { selected ->
                    editedLocation = selected
                    locationExpanded = false
                }
            }

            CustomTextField(
                label = "Hạng thành viên",
                value = profile.membership.ifBlank { "Vàng" },
                onValueChange = { },
                leadingIcon = Icons.Outlined.CheckCircleOutline,
                readOnly = true,
                fieldBackground = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppCtaWideHeight)
                    .clip(RoundedCornerShape(AppCtaCornerRadius))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        val validEmail = validateEmail(editedEmail)
                        val validPhone = validatePhone(editedPhone)
                        emailError = if (validEmail) null else "Email không hợp lệ"
                        phoneError = if (validPhone) null else "Số điện thoại không hợp lệ"
                        if (validEmail && validPhone) {
                            onSave(
                                profile.copy(
                                    name = editedName,
                                    email = editedEmail,
                                    phone = editedPhone,
                                    birthday = editedBirthday,
                                    gender = editedGender,
                                    location = editedLocation,
                                    membership = profile.membership.ifBlank { "Vàng" }
                                )
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lưu thay đổi",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    trailingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorText: String? = null,
    fieldBackground: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null
) {
    val resolvedFieldBackground = if (fieldBackground == Color.Unspecified) {
        MaterialTheme.colorScheme.surface
    } else {
        fieldBackground
    }

    Column(modifier = modifier.padding(bottom = 10.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        val fieldModifier = if (onClick != null) {
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        } else {
            Modifier.fillMaxWidth()
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier.height(54.dp),
            readOnly = readOnly,
            singleLine = true,
            shape = RoundedCornerShape(AppInputCornerRadius),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (trailingIcon != null) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            keyboardActions = KeyboardActions.Default,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = resolvedFieldBackground,
                unfocusedContainerColor = resolvedFieldBackground,
                disabledContainerColor = resolvedFieldBackground,
                focusedBorderColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                unfocusedBorderColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLeadingIconColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                unfocusedLeadingIconColor = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = MaterialTheme.colorScheme.error
            )
        )

        if (isError && !errorText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun DropdownPanel(
    options: List<String>,
    onOptionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(AppCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            options.forEachIndexed { index, option ->
                Text(
                    text = option,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionClick(option) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (index < options.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}
