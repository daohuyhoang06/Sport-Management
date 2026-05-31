package com.sportmanagement.user.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportmanagement.user.data.repository.UserRepositoryImpl
import com.sportmanagement.user.domain.model.HomeSearchCriteria
import com.sportmanagement.user.domain.model.HomeSearchFilterOptions
import com.sportmanagement.user.domain.model.SportCategory
import com.sportmanagement.user.domain.model.SportIconType
import com.sportmanagement.user.domain.model.UserField
import com.sportmanagement.user.domain.repository.UserRepository
import com.sportmanagement.user.domain.usecase.FilterHomeFieldsUseCase
import com.sportmanagement.user.ui.navigation.UserTab
import com.sportmanagement.user.ui.state.UserUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository = UserRepositoryImpl(),
    private val filterHomeFieldsUseCase: FilterHomeFieldsUseCase = FilterHomeFieldsUseCase()
) : ViewModel() {

    private var allHomeFields: List<UserField> = emptyList()
    private var allNearbyFields: List<UserField> = emptyList()
    private var homeSearchFilterOptions: HomeSearchFilterOptions = HomeSearchFilterOptions()
    private var lastHomeLatitude: Double? = null
    private var lastHomeLongitude: Double? = null
    private var currentPage: Int = 0
    private var hasMorePages: Boolean = true
    private var isLoadingPage: Boolean = false
    private var hasHandledLocationUnavailable: Boolean = false
    private var searchKeyword: String? = null
    private var searchAddress: String? = null
    private var searchSportType: String? = null
    private var searchPage: Int = 0
    private var hasMoreSearchPages: Boolean = true
    private var isLoadingSearchPage: Boolean = false
    private var searchGeneration: Int = 0
    private var preferredSportTypes: Set<SportIconType> = emptySet()

    private val _uiState = MutableStateFlow(
        UserUiState(
            isAuthenticated = repository.isLoggedIn(),
            isHomeLoading = true,
            recentFieldSearches = repository.getRecentFieldSearches(),
            profile = repository.getCachedProfile() ?: UserUiState().profile
        )
    )

    val uiState: StateFlow<UserUiState> = _uiState

    init {
        preferredSportTypes = decodePreferredSportTypes(repository.getPreferredSportTypeKeys())
        val savedLocation = repository.getSavedUserLocation()
        if (savedLocation != null) {
            lastHomeLatitude = savedLocation.first
            lastHomeLongitude = savedLocation.second
            loadInitialData(savedLocation.first, savedLocation.second)
        } else {
            loadSupportingData()
        }
    }

    fun onTabSelected(tab: UserTab) {
        _uiState.update { current -> current.copy(selectedTab = tab) }
    }

    fun clearAuthError() {
        _uiState.update { current -> current.copy(authError = null) }
    }

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(isAuthLoading = true, authError = null)
            }

            runCatching {
                repository.login(identifier = identifier, password = password)
            }.onSuccess { profile ->
                applyPreferredSports(profile.preferredSportTypeKeys)
                _uiState.update { current ->
                    current.copy(
                        isAuthenticated = true,
                        isAuthLoading = false,
                        authError = null,
                        profile = profile
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isAuthLoading = false,
                        authError = error.message ?: "Đăng nhập không thành công. Vui lòng thử lại."
                    )
                }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(isAuthLoading = true, authError = null)
            }

            runCatching {
                repository.loginWithGoogle(idToken)
            }.onSuccess { profile ->
                applyPreferredSports(profile.preferredSportTypeKeys)
                _uiState.update { current ->
                    current.copy(
                        isAuthenticated = true,
                        isAuthLoading = false,
                        authError = null,
                        profile = profile
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isAuthLoading = false,
                        authError = error.message ?: "Đăng nhập Google không thành công. Vui lòng thử lại."
                    )
                }
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        phone: String?,
        birthday: String?,
        address: String?,
        preferredSportTypeKeys: Set<String> = emptySet()
    ) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(isAuthLoading = true, authError = null)
            }

            runCatching {
                repository.register(
                    fullName = fullName,
                    email = email,
                    password = password,
                    phone = phone,
                    birthday = birthday,
                    address = address,
                    favoriteSportTypeKeys = preferredSportTypeKeys
                )
            }.onSuccess { profile ->
                applyPreferredSports(
                    if (profile.preferredSportTypeKeys.isNotEmpty()) {
                        profile.preferredSportTypeKeys
                    } else {
                        preferredSportTypeKeys
                    }
                )
                _uiState.update { current ->
                    val filteredSports = filterSportCategoriesByPreferred(current.sportCategories)
                    current.copy(
                        isAuthenticated = true,
                        isAuthLoading = false,
                        authError = null,
                        profile = profile,
                        homeFields = applyHomeSearchCriteria(current.activeHomeSearchCriteria),
                        nearbyFields = filterFieldsByPreferredSports(allNearbyFields),
                        sportCategories = filteredSports,
                        mapCategories = filteredSports.map { it.name },
                        homeSearchFilterOptions = filterHomeSearchFilterOptions(homeSearchFilterOptions)
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        isAuthLoading = false,
                        authError = error.message ?: "Đăng ký thất bại. Vui lòng thử lại."
                    )
                }
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update { current ->
            current.copy(
                isAuthenticated = false,
                authError = null,
                profile = UserUiState().profile
            )
        }
    }

    fun updateProfile(profile: com.sportmanagement.user.domain.model.UserProfile) {
        viewModelScope.launch {
            runCatching {
                repository.updateProfile(profile)
            }.onSuccess { updatedProfile ->
                applyPreferredSports(updatedProfile.preferredSportTypeKeys)
                _uiState.update { current ->
                    val filteredSports = filterSportCategoriesByPreferred(current.sportCategories)
                    current.copy(
                        profile = updatedProfile,
                        homeFields = applyHomeSearchCriteria(current.activeHomeSearchCriteria),
                        nearbyFields = filterFieldsByPreferredSports(allNearbyFields),
                        sportCategories = filteredSports,
                        mapCategories = filteredSports.map { it.name },
                        homeSearchFilterOptions = filterHomeSearchFilterOptions(homeSearchFilterOptions)
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(authError = error.message ?: "Cập nhật thông tin thất bại. Vui lòng thử lại.")
                }
            }
        }
    }

    fun onApplyHomeSearchCriteria(criteria: HomeSearchCriteria) {
        _uiState.update { current ->
            current.copy(
                activeHomeSearchCriteria = criteria,
                homeFields = applyHomeSearchCriteria(criteria)
            )
        }
    }

    fun resetHomeSearchCriteria() {
        val defaultCriteria = HomeSearchCriteria()
        clearFieldSearchResults()
        _uiState.update { current ->
            current.copy(
                activeHomeSearchCriteria = defaultCriteria,
                homeFields = applyHomeSearchCriteria(defaultCriteria),
                hasMoreHomeFields = hasMorePages
            )
        }
    }

    fun onFieldSearchOpened() {
        _uiState.update { current ->
            current.copy(recentFieldSearches = repository.getRecentFieldSearches())
        }
    }

    fun clearFieldSearchResults() {
        searchGeneration += 1
        searchKeyword = null
        searchAddress = null
        searchSportType = null
        searchPage = 0
        hasMoreSearchPages = true
        isLoadingSearchPage = false
        _uiState.update {
            it.copy(
                fieldSearchResults = emptyList(),
                isFieldSearchLoading = false,
                isFieldSearchLoadingMore = false,
                hasMoreFieldSearchResults = true
            )
        }
    }

    fun searchFields(
        keyword: String? = null,
        address: String? = null,
        sportType: String? = null
    ) {
        val hasCriteria = !keyword.isNullOrBlank() || !address.isNullOrBlank() || !sportType.isNullOrBlank()
        if (!hasCriteria) {
            clearFieldSearchResults()
            return
        }

        searchGeneration += 1
        searchKeyword = keyword?.trim()?.takeIf { it.isNotBlank() }
        searchAddress = address?.trim()?.takeIf { it.isNotBlank() }
        searchSportType = sportType?.trim()?.takeIf { it.isNotBlank() }
        searchPage = 0
        hasMoreSearchPages = true
        isLoadingSearchPage = false
        loadSearchPage(reset = true, generation = searchGeneration)
    }

    fun loadMoreFieldSearchResults() {
        if (isLoadingSearchPage || !hasMoreSearchPages || _uiState.value.isFieldSearchLoading) return
        if (searchKeyword == null && searchAddress == null && searchSportType == null) return
        loadSearchPage(reset = false, generation = searchGeneration)
    }

    fun rememberFieldSearch(query: String) {
        repository.saveRecentFieldSearch(query)
        _uiState.update { current ->
            current.copy(recentFieldSearches = repository.getRecentFieldSearches())
        }
    }

    fun onHomeLocationUpdated(latitude: Double, longitude: Double) {
        val shouldReload = lastHomeLatitude == null ||
            lastHomeLongitude == null ||
            kotlin.math.abs((lastHomeLatitude ?: 0.0) - latitude) > 0.0003 ||
            kotlin.math.abs((lastHomeLongitude ?: 0.0) - longitude) > 0.0003
        if (!shouldReload) return

        lastHomeLatitude = latitude
        lastHomeLongitude = longitude
        repository.saveUserLocation(latitude, longitude)
        hasHandledLocationUnavailable = false
        loadInitialData(latitude, longitude)
    }

    fun onHomeLocationUnavailable() {
        if (lastHomeLatitude != null && lastHomeLongitude != null) return
        if (hasHandledLocationUnavailable) return
        hasHandledLocationUnavailable = true
        allHomeFields = emptyList()
        allNearbyFields = emptyList()
        _uiState.update {
            it.copy(
                homeFields = emptyList(),
                nearbyFields = emptyList(),
                isHomeLoading = false,
                isHomeLoadingMore = false,
                hasMoreHomeFields = false
            )
        }
    }

    fun loadMoreHomeFields() {
        val state = _uiState.value
        val isDefaultFeed = state.activeHomeSearchCriteria == HomeSearchCriteria()
        if (!isDefaultFeed || isLoadingPage || !hasMorePages || state.isHomeLoading) return

        viewModelScope.launch {
            isLoadingPage = true
            _uiState.update { it.copy(isHomeLoadingMore = true) }

            val nextPage = currentPage + 1
            val latitude = lastHomeLatitude
            val longitude = lastHomeLongitude
            if (latitude == null || longitude == null) {
                _uiState.update {
                    it.copy(
                        isHomeLoadingMore = false,
                        hasMoreHomeFields = false
                    )
                }
                isLoadingPage = false
                return@launch
            }

            val pageItems = repository.getNearbyFieldsPage(
                page = nextPage,
                limit = PAGE_SIZE,
                latitude = latitude,
                longitude = longitude
            )

            if (pageItems.isNotEmpty()) {
                allHomeFields = mergeDedup(allHomeFields, pageItems)
                currentPage = nextPage
            }

            hasMorePages = pageItems.size >= PAGE_SIZE
            val updatedHome = applyHomeSearchCriteria(_uiState.value.activeHomeSearchCriteria)

            _uiState.update {
                it.copy(
                    homeFields = updatedHome,
                    nearbyFields = filterFieldsByPreferredSports(allNearbyFields),
                    isHomeLoadingMore = false,
                    hasMoreHomeFields = hasMorePages
                )
            }
            isLoadingPage = false
        }
    }

    private fun loadInitialData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            currentPage = 0
            hasMorePages = true
            isLoadingPage = false

            val cachedFields = repository.getCachedHomeFields(latitude, longitude)
            val hasCache = cachedFields.isNotEmpty()

            if (hasCache) {
                allHomeFields = cachedFields
                allNearbyFields = cachedFields
                _uiState.update { current ->
                    current.copy(
                        homeFields = applyHomeSearchCriteria(current.activeHomeSearchCriteria),
                        nearbyFields = filterFieldsByPreferredSports(allNearbyFields),
                        isHomeLoading = false,
                        isHomeLoadingMore = false,
                        hasMoreHomeFields = true
                    )
                }
            } else {
                allHomeFields = emptyList()
                allNearbyFields = emptyList()
                _uiState.update {
                    it.copy(
                        homeFields = emptyList(),
                        nearbyFields = emptyList(),
                        isHomeLoading = true,
                        isHomeLoadingMore = false,
                        hasMoreHomeFields = true
                    )
                }
            }

            val firstPage = repository.getNearbyFieldsPage(
                page = 1,
                limit = PAGE_SIZE,
                latitude = latitude,
                longitude = longitude
            )

            if (firstPage.isNotEmpty()) {
                allHomeFields = firstPage
                currentPage = 1
                hasMorePages = firstPage.size >= PAGE_SIZE
            } else if (!hasCache) {
                hasMorePages = false
            }

            val fullNearby = repository.getNearbyFields(
                latitude = latitude,
                longitude = longitude
            )
            if (fullNearby.isNotEmpty()) {
                allNearbyFields = fullNearby
            } else if (!hasCache) {
                allNearbyFields = emptyList()
            }

            homeSearchFilterOptions = repository.getHomeSearchFilterOptions()

            val loadedSports = repository.getSportCategories()
            val filteredSports = filterSportCategoriesByPreferred(loadedSports)
            val loadedFavorites = repository.getFavoriteFields()
            val loadedBookingSchedule = repository.getBookingSchedule()
            val loadedProfile = repository.getProfile()
            val loadedStats = repository.getStats()
            applyPreferredSports(loadedProfile.preferredSportTypeKeys)

            _uiState.update { current ->
                current.copy(
                    homeFields = applyHomeSearchCriteria(current.activeHomeSearchCriteria),
                    sportCategories = filteredSports,
                    mapCategories = filteredSports.map { it.name },
                    nearbyFields = filterFieldsByPreferredSports(allNearbyFields),
                    favoriteFields = loadedFavorites,
                    homeSearchFilterOptions = filterHomeSearchFilterOptions(homeSearchFilterOptions),
                    bookingSchedule = loadedBookingSchedule,
                    profile = loadedProfile,
                    stats = loadedStats,
                    isHomeLoading = false,
                    isHomeLoadingMore = false,
                    hasMoreHomeFields = hasMorePages
                )
            }
        }
    }

    private fun loadSupportingData() {
        viewModelScope.launch {
            homeSearchFilterOptions = repository.getHomeSearchFilterOptions()
            val loadedSports = repository.getSportCategories()
            val filteredSports = filterSportCategoriesByPreferred(loadedSports)
            val loadedFavorites = repository.getFavoriteFields()
            val loadedBookingSchedule = repository.getBookingSchedule()
            val loadedProfile = repository.getProfile()
            val loadedStats = repository.getStats()
            applyPreferredSports(loadedProfile.preferredSportTypeKeys)

            _uiState.update { current ->
                current.copy(
                    sportCategories = filteredSports,
                    mapCategories = filteredSports.map { it.name },
                    nearbyFields = filterFieldsByPreferredSports(allNearbyFields),
                    favoriteFields = loadedFavorites,
                    homeSearchFilterOptions = filterHomeSearchFilterOptions(homeSearchFilterOptions),
                    bookingSchedule = loadedBookingSchedule,
                    profile = loadedProfile,
                    stats = loadedStats
                )
            }
        }
    }

    private fun loadSearchPage(reset: Boolean, generation: Int) {
        viewModelScope.launch {
            if (reset) {
                _uiState.update {
                    it.copy(
                        fieldSearchResults = emptyList(),
                        isFieldSearchLoading = true,
                        isFieldSearchLoadingMore = false,
                        hasMoreFieldSearchResults = true
                    )
                }
            } else {
                _uiState.update { it.copy(isFieldSearchLoadingMore = true) }
            }

            isLoadingSearchPage = true
            val nextPage = if (reset) 1 else searchPage + 1
            val pageItems = repository.searchFieldsPage(
                keyword = searchKeyword,
                address = searchAddress,
                sportType = searchSportType,
                latitude = lastHomeLatitude,
                longitude = lastHomeLongitude,
                sortBy = if (lastHomeLatitude != null && lastHomeLongitude != null) "distance" else null,
                page = nextPage,
                limit = SEARCH_PAGE_SIZE
            )

            if (generation != searchGeneration) {
                isLoadingSearchPage = false
                return@launch
            }

            val updatedResults = if (reset) {
                pageItems
            } else {
                mergeDedup(_uiState.value.fieldSearchResults, pageItems)
            }
            searchPage = nextPage
            hasMoreSearchPages = pageItems.size >= SEARCH_PAGE_SIZE
            isLoadingSearchPage = false

            _uiState.update {
                it.copy(
                    fieldSearchResults = filterFieldsByPreferredSports(updatedResults),
                    isFieldSearchLoading = false,
                    isFieldSearchLoadingMore = false,
                    hasMoreFieldSearchResults = hasMoreSearchPages
                )
            }
        }
    }

    private fun mergeDedup(existing: List<UserField>, incoming: List<UserField>): List<UserField> {
        if (incoming.isEmpty()) return existing
        val seen = existing.map { dedupKey(it) }.toMutableSet()
        val merged = existing.toMutableList()
        incoming.forEach { item ->
            if (seen.add(dedupKey(item))) {
                merged.add(item)
            }
        }
        return merged
    }

    private fun dedupKey(field: UserField): String {
        val lat = field.latitude?.toString().orEmpty()
        val lng = field.longitude?.toString().orEmpty()
        return "${field.name}|${field.location}|$lat|$lng"
    }

    private fun applyHomeSearchCriteria(criteria: HomeSearchCriteria): List<UserField> {
        return filterHomeFieldsUseCase(
            fields = sourceFieldsForHomeCriteria(criteria),
            criteria = criteria,
            options = filterHomeSearchFilterOptions(homeSearchFilterOptions)
        )
    }

    private fun sourceFieldsForHomeCriteria(criteria: HomeSearchCriteria): List<UserField> {
        val source = if (criteria == HomeSearchCriteria()) {
            allHomeFields
        } else {
            allNearbyFields.ifEmpty { allHomeFields }
        }
        return filterFieldsByPreferredSports(source)
    }

    private fun applyPreferredSports(preferredSportTypeKeys: Set<String>) {
        repository.savePreferredSportTypeKeys(preferredSportTypeKeys)
        preferredSportTypes = decodePreferredSportTypes(preferredSportTypeKeys)
    }

    private fun decodePreferredSportTypes(keys: Set<String>): Set<SportIconType> {
        return keys.mapNotNull { key ->
            runCatching { SportIconType.valueOf(key.trim().uppercase()) }.getOrNull()
        }.toSet()
    }

    private fun filterFieldsByPreferredSports(fields: List<UserField>): List<UserField> {
        if (preferredSportTypes.isEmpty()) {
            return fields
        }
        return fields.filter { it.sportIconType in preferredSportTypes }
    }

    private fun filterSportCategoriesByPreferred(
        categories: List<SportCategory>
    ): List<SportCategory> {
        if (preferredSportTypes.isEmpty()) {
            return categories
        }
        return categories.filter { it.iconType in preferredSportTypes }
    }

    private fun filterHomeSearchFilterOptions(
        options: HomeSearchFilterOptions
    ): HomeSearchFilterOptions {
        return if (preferredSportTypes.isEmpty()) {
            options
        } else {
            options.copy(sports = options.sports.filter { it.iconType in preferredSportTypes })
        }
    }

    companion object {
        private const val PAGE_SIZE = 5
        private const val SEARCH_PAGE_SIZE = 10
    }
}
