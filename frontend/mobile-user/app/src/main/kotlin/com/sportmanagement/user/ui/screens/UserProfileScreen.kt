package com.sportmanagement.user.ui.screens

import android.app.DatePickerDialog
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.ui.components.profile.ProfileHeaderSection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// =========================
// Color Constants
// =========================

private val ProfileTeal = Color(0xFF06A9A2)
private val ProfileNavy = Color(0xFF0D2C5A)
private val ProfileRed = Color(0xFFE64A45)
private val ProfileFieldBorder = Color(0xFFD8DEE9)
private val ProfileFieldError = Color(0xFFD32F2F)
private val ProfileFieldBackground = Color(0xFFF7F8FC)

// =========================
// Main Screen
// =========================

@Composable
fun UserProfileScreen(
    padding: PaddingValues,
    profile: UserProfile,
    onProfileUpdate: (UserProfile) -> Unit = {}
) {

    // State mở bottomsheet chỉnh sửa
    var showEditSheet by rememberSaveable {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FB))
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {

            // =========================
            // Profile Header Section
            // =========================

            item {

                ProfileHeaderSection(
                    profile = profile,
                    onEditClick = {
                        showEditSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // =========================
            // Menu Card
            // =========================

            item {
                MenuCard()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // =========================
            // Logout Button
            // =========================

            item {
                LogoutButton()
            }
        }

        // =========================
        // Bottom Sheet Edit Profile
        // =========================

        if (showEditSheet) {

            EditProfileBottomSheet(
                profile = profile,

                onDismiss = {
                    showEditSheet = false
                },

                onSave = { updatedProfile ->

                    onProfileUpdate(updatedProfile)

                    showEditSheet = false
                }
            )
        }
    }
}

// =========================
// Menu Card
// =========================

@Composable
private fun MenuCard() {

    val menuItems = listOf(

        MenuItemData(
            icon = Icons.Outlined.CalendarMonth,
            label = "Lịch sử đặt sân"
        ),

        MenuItemData(
            icon = Icons.Outlined.FavoriteBorder,
            label = "Sân yêu thích"
        ),

        MenuItemData(
            icon = Icons.Outlined.Notifications,
            label = "Thông báo"
        ),

        MenuItemData(
            icon = Icons.Outlined.CreditCard,
            label = "Phương thức thanh toán"
        ),

        MenuItemData(
            icon = Icons.Outlined.HelpOutline,
            label = "Hỗ trợ & FAQ"
        ),

        MenuItemData(
            icon = Icons.Outlined.Settings,
            label = "Cài đặt"
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),

        shape = RoundedCornerShape(16.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column {

            menuItems.forEachIndexed { index, item ->

                MenuItemRow(item = item)

                if (index < menuItems.lastIndex) {

                    HorizontalDivider(
                        color = Color(0xFFF0F2F7),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

// =========================
// Single Menu Item
// =========================

@Composable
private fun MenuItemRow(
    item: MenuItemData
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(
                horizontal = 14.dp,
                vertical = 14.dp
            ),

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = Color(0xFF576275)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2D3442)
            )
        }

        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFF8D95A5)
        )
    }
}

// =========================
// Logout Button
// =========================

@Composable
private fun LogoutButton() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),

        shape = RoundedCornerShape(14.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 14.dp),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = null,
                tint = ProfileRed
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Đăng xuất",
                color = ProfileRed,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// =========================
// Edit Profile Bottom Sheet
// =========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {

    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // =========================
    // States
    // =========================

    var editedName by rememberSaveable(profile.name) {
        mutableStateOf(profile.name)
    }

    var editedEmail by rememberSaveable(profile.email) {
        mutableStateOf(profile.email)
    }

    var editedPhone by rememberSaveable(profile.phone) {
        mutableStateOf(profile.phone)
    }

    var editedBirthday by rememberSaveable(profile.birthday) {
        mutableStateOf(profile.birthday)
    }

    var editedGender by rememberSaveable(profile.gender) {
        mutableStateOf(profile.gender)
    }

    var editedLocation by rememberSaveable(profile.location) {
        mutableStateOf(profile.location)
    }

    var emailError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var phoneError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    // =========================
    // Validate
    // =========================

    fun validateEmail(value: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(value).matches()
    }

    fun validatePhone(value: String): Boolean {
        return Regex("^[0-9]{9,11}$").matches(value)
    }

    // =========================
    // Date Formatter
    // =========================

    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    ModalBottomSheet(

        onDismissRequest = onDismiss,

        sheetState = sheetState,

        containerColor = Color.White

    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
        ) {

            // =========================
            // Top Bar
            // =========================

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,

                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Chỉnh sửa thông tin",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDismiss
                ) {

                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================
            // Name
            // =========================

            CustomTextField(
                label = "Họ và tên",
                value = editedName,
                onValueChange = {
                    editedName = it
                },
                leadingIcon = Icons.Outlined.Person
            )

            // =========================
            // Email
            // =========================

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

            // =========================
            // Phone
            // =========================

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

            // =========================
            // Birthday
            // =========================

            CustomTextField(
                label = "Ngày sinh",
                value = editedBirthday,

                onValueChange = { },

                leadingIcon = Icons.Outlined.CalendarMonth,

                readOnly = true,

                trailingIcon = Icons.Filled.ArrowDropDown,

                onClick = {

                    val calendar = Calendar.getInstance()

                    DatePickerDialog(
                        context,

                        { _, year, month, dayOfMonth ->

                            calendar.set(year, month, dayOfMonth)

                            editedBirthday =
                                dateFormatter.format(calendar.time)
                        },

                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)

                    ).show()
                }
            )

            // =========================
            // Location
            // =========================

            CustomTextField(
                label = "Khu vực",
                value = editedLocation,
                onValueChange = { },
                leadingIcon = Icons.Outlined.LocationOn
            )

            Spacer(modifier = Modifier.height(18.dp))

            // =========================
            // Save Button
            // =========================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                ProfileNavy,
                                ProfileTeal
                            )
                        )
                    )
                    .clickable {

                        val validEmail =
                            validateEmail(editedEmail)

                        val validPhone =
                            validatePhone(editedPhone)

                        emailError =
                            if (validEmail) null
                            else "Email không hợp lệ"

                        phoneError =
                            if (validPhone) null
                            else "Số điện thoại không hợp lệ"

                        if (validEmail && validPhone) {

                            onSave(
                                profile.copy(
                                    name = editedName,
                                    email = editedEmail,
                                    phone = editedPhone,
                                    birthday = editedBirthday,
                                    location = editedLocation
                                )
                            )
                        }
                    },

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Lưu thay đổi",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

// =========================
// Custom Text Field
// =========================

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

    fieldBackground: Color = Color.White,

    onClick: (() -> Unit)? = null
) {

    Column(
        modifier = modifier.padding(bottom = 10.dp)
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF566275),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        val fieldModifier =
            if (onClick != null) {
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
            } else {
                Modifier.fillMaxWidth()
            }

        OutlinedTextField(
            value = value,

            onValueChange = onValueChange,

            modifier = fieldModifier,

            readOnly = readOnly,

            singleLine = true,

            shape = RoundedCornerShape(12.dp),

            leadingIcon = {

                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null
                )
            },

            trailingIcon = {

                if (trailingIcon != null) {

                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null
                    )
                }
            },

            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),

            keyboardActions = KeyboardActions.Default,

            isError = isError,

            colors = OutlinedTextFieldDefaults.colors(

                focusedContainerColor = fieldBackground,

                unfocusedContainerColor = fieldBackground,

                focusedBorderColor =
                    if (isError) ProfileFieldError
                    else ProfileFieldBorder,

                unfocusedBorderColor =
                    if (isError) ProfileFieldError
                    else ProfileFieldBorder
            )
        )

        if (isError && !errorText.isNullOrBlank()) {

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = errorText,
                color = ProfileFieldError,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// =========================
// Menu Item Data
// =========================

private data class MenuItemData(
    val icon: ImageVector,
    val label: String
)