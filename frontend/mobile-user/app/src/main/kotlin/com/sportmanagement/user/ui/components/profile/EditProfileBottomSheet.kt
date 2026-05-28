package com.sportmanagement.user.ui.components.profile

import android.app.DatePickerDialog
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sportmanagement.user.R
import com.sportmanagement.user.domain.model.UserProfile
import com.sportmanagement.user.ui.theme.AppCardCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaCornerRadius
import com.sportmanagement.user.ui.theme.AppCtaWideHeight
import com.sportmanagement.user.ui.theme.AppSheetTopCornerRadius
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

private data class SportOptionUi(
    val key: String,
    val label: String,
    val iconRes: Int
)

private fun sportOptions(): List<SportOptionUi> = listOf(
    SportOptionUi("FOOTBALL", "Bóng đá", R.drawable.football_25),
    SportOptionUi("BADMINTON", "Cầu lông", R.drawable.badminton_25),
    SportOptionUi("TENNIS", "Tennis", R.drawable.tennis_25),
    SportOptionUi("PICKLEBALL", "Pickleball", R.drawable.pickleball),
    SportOptionUi("VOLLEYBALL", "Bóng chuyền", R.drawable.volleyball_25)
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditProfileBottomSheet(
    profile: UserProfile,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    val context = LocalContext.current

    var editedName by rememberSaveable(profile.name) { mutableStateOf(profile.name) }
    var editedEmail by rememberSaveable(profile.email) { mutableStateOf(profile.email) }
    var editedPhone by rememberSaveable(profile.phone) { mutableStateOf(profile.phone) }
    var editedBirthday by rememberSaveable(profile.birthday) { mutableStateOf(profile.birthday) }
    var editedGender by rememberSaveable(profile.gender) { mutableStateOf(profile.gender.ifBlank { "Nam" }) }
    var editedLocation by rememberSaveable(profile.location) { mutableStateOf(profile.location) }
    var selectedAvatarUri by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var genderExpanded by rememberSaveable { mutableStateOf(false) }
    var showSportPicker by rememberSaveable { mutableStateOf(false) }
    var sportSearchQuery by rememberSaveable { mutableStateOf("") }

    val preferredSportOptions = remember { sportOptions() }
    val selectedSportKeys = remember(profile.preferredSportTypeKeys) {
        mutableStateListOf<String>().apply { addAll(profile.preferredSportTypeKeys) }
    }
    val draftSportKeys = remember { mutableStateListOf<String>() }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val genderOptions = remember { listOf("Nam", "Nữ", "Khác") }
    val formScrollState = androidx.compose.foundation.rememberScrollState()
    val scope = rememberCoroutineScope()
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedAvatarUri = uri?.toString()
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
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(formScrollState)
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
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { avatarPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val avatarPreview = selectedAvatarUri ?: profile.avatarUrl
                if (avatarPreview.isNotBlank()) {
                    AsyncImage(
                        model = avatarPreview,
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }
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
                readOnly = true,
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
                onClick = { genderExpanded = !genderExpanded }
            )

            if (genderExpanded) {
                DropdownPanel(options = genderOptions) { selected ->
                    editedGender = selected
                    genderExpanded = false
                }
            }

            CustomTextField(
                label = "Địa chỉ",
                value = editedLocation,
                onValueChange = { editedLocation = it },
                leadingIcon = Icons.Outlined.LocationOn
            )

            Text(
                text = "Môn thể thao yêu thích",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            SportPreferenceSummaryCard(
                options = preferredSportOptions,
                selectedKeys = selectedSportKeys.toSet(),
                onClick = {
                    scope.launch { formScrollState.animateScrollTo(0) }
                    draftSportKeys.clear()
                    draftSportKeys.addAll(selectedSportKeys)
                    sportSearchQuery = ""
                    showSportPicker = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppCtaWideHeight)
                    .clip(RoundedCornerShape(AppCtaCornerRadius))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        val validEmail = editedEmail.isBlank() || validateEmail(editedEmail)
                        val validPhone = validatePhone(editedPhone)
                        emailError = if (validEmail) null else "Email không hợp lệ"
                        phoneError = if (validPhone) null else "Số điện thoại không hợp lệ"
                        if (validEmail && validPhone) {
                            onSave(
                                profile.copy(
                                    name = editedName.trim(),
                                    email = editedEmail.trim(),
                                    phone = editedPhone.trim(),
                                    birthday = editedBirthday.trim(),
                                    gender = editedGender.trim(),
                                    location = editedLocation.trim(),
                                    preferredSportTypeKeys = selectedSportKeys.toSet(),
                                    avatarUrl = selectedAvatarUri ?: profile.avatarUrl
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

    if (showSportPicker) {
        SportPreferencePickerSheet(
            options = preferredSportOptions,
            selectedKeys = draftSportKeys.toSet(),
            searchQuery = sportSearchQuery,
            onSearchQueryChange = { sportSearchQuery = it },
            onToggle = { sportKey ->
                if (draftSportKeys.contains(sportKey)) {
                    draftSportKeys.remove(sportKey)
                } else {
                    draftSportKeys.add(sportKey)
                }
            },
            onDismiss = { showSportPicker = false },
            onSave = {
                selectedSportKeys.clear()
                selectedSportKeys.addAll(draftSportKeys)
                showSportPicker = false
            }
        )
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        val borderColor = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppCtaWideHeight)
                .let {
                    if (onClick != null) {
                        it.clickable(onClick = onClick)
                    } else {
                        it
                    }
                },
            shape = RoundedCornerShape(AppCtaCornerRadius),
            color = resolvedFieldBackground,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = borderColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Spacer(modifier = Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        readOnly = readOnly,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (isError && !errorText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
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

@Composable
private fun SportPreferenceSummaryCard(
    options: List<SportOptionUi>,
    selectedKeys: Set<String>,
    onClick: () -> Unit
) {
    val selected = options.filter { selectedKeys.contains(it.key) }
    val visible = selected.take(3)
    val remainingCount = (selected.size - visible.size).coerceAtLeast(0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AppCtaWideHeight)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(AppCtaCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (selected.isEmpty()) {
                    Text(
                        text = "Chọn môn thể thao yêu thích",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        visible.forEach { option ->
                            SportSelectionChip(option = option)
                        }
                        if (remainingCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                )
                            ) {
                                Text(
                                    text = "+$remainingCount môn khác",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SportSelectionChip(
    option: SportOptionUi,
    onRemove: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Image(
                painter = painterResource(id = option.iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            if (onRemove != null) {
                Surface(
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onRemove),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportPreferencePickerSheet(
    options: List<SportOptionUi>,
    selectedKeys: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val selected = options.filter { selectedKeys.contains(it.key) }
    val filteredOptions = options.filter { option ->
        searchQuery.isBlank() || option.label.contains(searchQuery, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.96f),
        sheetState = sheetState,
        contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0) },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
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
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Chọn môn thể thao yêu thích",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (selected.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selected.forEach { option ->
                        SportSelectionChip(option = option, onRemove = { onToggle(option.key) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppCtaWideHeight),
                shape = RoundedCornerShape(AppCtaCornerRadius),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isBlank()) {
                            Text(
                                text = "Tìm kiếm môn thể thao",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tất cả môn thể thao",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                items(filteredOptions, key = { it.key }) { option ->
                    val isSelected = selectedKeys.contains(option.key)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggle(option.key) }
                            .padding(horizontal = 4.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = option.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = option.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            modifier = Modifier.size(22.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                        thickness = 1.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppCtaWideHeight)
                    .clip(RoundedCornerShape(AppCtaCornerRadius))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onSave),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lưu (${selected.size})",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
