package com.sportmanagement.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.manager.data.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isChangingPassword: Boolean = false,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String? = null,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val successMessage: String? = null,
    val error: String? = null,
    val passwordError: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            AppContainer.profileRepository.getProfile()
                .onSuccess { dto ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        name = dto.personName ?: "",
                        email = dto.email ?: "",
                        phone = dto.phone ?: "",
                        avatarUrl = dto.avatarUrl
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun onNameChanged(value: String) { _uiState.value = _uiState.value.copy(name = value) }
    fun onEmailChanged(value: String) { _uiState.value = _uiState.value.copy(email = value) }
    fun onPhoneChanged(value: String) { _uiState.value = _uiState.value.copy(phone = value) }
    fun onCurrentPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(currentPassword = v, passwordError = null) }
    fun onNewPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(newPassword = v, passwordError = null) }
    fun onConfirmPasswordChanged(v: String) { _uiState.value = _uiState.value.copy(confirmPassword = v, passwordError = null) }
    fun dismissMessages() { _uiState.value = _uiState.value.copy(successMessage = null, error = null, passwordError = null) }

    fun onSaveProfile() {
        val s = _uiState.value
        if (s.name.isBlank()) { _uiState.value = s.copy(error = "Vui lòng nhập tên"); return }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            AppContainer.profileRepository.updateProfile(
                personName = s.name.trim(),
                email = s.email.trim().ifBlank { null },
                phone = s.phone.trim().ifBlank { null }
            ).onSuccess { dto ->
                AppContainer.authRepository.updateName(dto.personName ?: s.name)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    name = dto.personName ?: s.name,
                    email = dto.email ?: s.email,
                    phone = dto.phone ?: s.phone,
                    successMessage = "Cập nhật hồ sơ thành công!"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun onChangePassword() {
        val s = _uiState.value
        if (s.currentPassword.isBlank() || s.newPassword.isBlank()) {
            _uiState.value = s.copy(passwordError = "Vui lòng nhập đầy đủ mật khẩu"); return
        }
        if (s.newPassword.length < 6) {
            _uiState.value = s.copy(passwordError = "Mật khẩu mới phải có ít nhất 6 ký tự"); return
        }
        if (s.newPassword != s.confirmPassword) {
            _uiState.value = s.copy(passwordError = "Xác nhận mật khẩu không khớp"); return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChangingPassword = true, passwordError = null)
            AppContainer.profileRepository.changePassword(s.currentPassword, s.newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isChangingPassword = false,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        successMessage = "Đổi mật khẩu thành công!"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isChangingPassword = false, passwordError = e.message)
                }
        }
    }
}
