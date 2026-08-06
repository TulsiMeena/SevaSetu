package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.ServiceItem
import com.example.data.entity.User
import com.example.data.repository.MarketplaceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MarketplaceViewModel(
    application: Application,
    private val repository: MarketplaceRepository
) : AndroidViewModel(application) {

    // Login Session State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Screen State Feedback
    private val _signUpStatus = MutableStateFlow<AuthStatus>(AuthStatus.Idle)
    val signUpStatus: StateFlow<AuthStatus> = _signUpStatus.asStateFlow()

    private val _loginStatus = MutableStateFlow<AuthStatus>(AuthStatus.Idle)
    val loginStatus: StateFlow<AuthStatus> = _loginStatus.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Combine or switch flatMapLatest based on searchQuery
    @OptIn(ExperimentalCoroutinesApi::class)
    val serviceItems: StateFlow<List<ServiceItem>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allServices
            } else {
                repository.searchServices(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val myServices: StateFlow<List<ServiceItem>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user ->
            repository.getServicesBySeller(user.username)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Prepopulate with outstanding default services if the marketplace is empty
        viewModelScope.launch {
            repository.allServices.first().let { items ->
                if (items.isEmpty()) {
                    val defaultServices = listOf(
                        ServiceItem(
                            title = "Professional Home Plumbing (नलसाजी मरम्मत)",
                            category = "Local Repairs (मरम्मत कार्य)",
                            description = "Leaking faucets, pipeline repairs, sanitary installations, and bathroom fitting maintenance with guaranteed quality.",
                            location = "New Delhi, NCR",
                            priceRate = "₹350 / hour",
                            sellerFullName = "Ramesh Kumar",
                            sellerUsername = "ramesh",
                            contactPhone = "9876543210"
                        ),
                        ServiceItem(
                            title = "High school Mathematics & Physics Classes (गणित शिक्षक)",
                            category = "Education (शिक्षा)",
                            description = "Expert interactive personal tutoring for class 8th to 12th students. Concepts clarity and past papers revision included.",
                            location = "Mumbai, Maharashtra",
                            priceRate = "₹600 / session",
                            sellerFullName = "Sunita Sharma",
                            sellerUsername = "sunita",
                            contactPhone = "9988776655"
                        ),
                        ServiceItem(
                            title = "Complete House Deep Cleaning (घर की सफाई)",
                            category = "Cleaning (घर की सफाई)",
                            description = "Full 3BHK deep cleaning, floor sanitization, bathroom scrub, kitchen grease clear, and window glass polish.",
                            location = "Bengaluru, Karnataka",
                            priceRate = "₹2499 fixed",
                            sellerFullName = "Karan Singh",
                            sellerUsername = "karan",
                            contactPhone = "9898989898"
                        ),
                        ServiceItem(
                            title = "Modern Fullstack Mobile or Web Development",
                            category = "IT & Tech (कंप्यूटर / आईटी)",
                            description = "Build clean, professional mobile apps using Kotlin and Compose or beautiful modern web dashboards matching UI guidelines.",
                            location = "Remote (हर जगह)",
                            priceRate = "₹1500 / day",
                            sellerFullName = "Aman Meena",
                            sellerUsername = "aman",
                            contactPhone = "9112233445"
                        )
                    )
                    // Insert default sellers
                    repository.registerUser(User("ramesh", "123", "Ramesh Kumar", "9876543210"))
                    repository.registerUser(User("sunita", "123", "Sunita Sharma", "9988776655"))
                    repository.registerUser(User("karan", "123", "Karan Singh", "9898989898"))
                    repository.registerUser(User("aman", "123", "Aman Meena", "9112233445"))

                    // Insert default services
                    defaultServices.forEach { service ->
                        repository.insertService(service)
                    }
                }
            }
        }
    }

    // Operations
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun register(username: String, pass: String, fullName: String, phone: String) {
        if (username.isBlank() || pass.isBlank() || fullName.isBlank() || phone.isBlank()) {
            _signUpStatus.value = AuthStatus.Error("सभी फील्ड भरना अनिवार्य है! (All fields are required!)")
            return
        }
        _signUpStatus.value = AuthStatus.Loading
        viewModelScope.launch {
            val success = repository.registerUser(
                User(
                    username = username.trim(),
                    passwordHash = pass, // For prototype security, simple representation
                    fullName = fullName.trim(),
                    phoneNumber = phone.trim()
                )
            )
            if (success) {
                _signUpStatus.value = AuthStatus.Success("सफलतापूर्वक रजिस्टर हो गया! कृपया लॉगिन करें।")
            } else {
                _signUpStatus.value = AuthStatus.Error("यह यूजरनेम पहले से मौजूद है! (Username already exists!)")
            }
        }
    }

    fun login(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _loginStatus.value = AuthStatus.Error("यूजरनेम और पासवर्ड भरें! (Enter credentials!)")
            return
        }
        _loginStatus.value = AuthStatus.Loading
        viewModelScope.launch {
            val user = repository.loginUser(username.trim(), pass)
            if (user != null) {
                _currentUser.value = user
                _loginStatus.value = AuthStatus.Success("लॉगिन सफल! (Logged in successfully!)")
            } else {
                _loginStatus.value = AuthStatus.Error("गलत यूजरनेम या पासवर्ड! (Wrong credentials!)")
            }
        }
    }

    fun postService(title: String, category: String, description: String, location: String, priceRate: String) {
        val user = _currentUser.value ?: return
        if (title.isBlank() || category.isBlank() || description.isBlank() || location.isBlank() || priceRate.isBlank()) {
            return
        }
        viewModelScope.launch {
            repository.insertService(
                ServiceItem(
                    title = title.trim(),
                    category = category,
                    description = description.trim(),
                    location = location.trim(),
                    priceRate = priceRate.trim(),
                    sellerFullName = user.fullName,
                    sellerUsername = user.username,
                    contactPhone = user.phoneNumber
                )
            )
        }
    }

    fun deleteService(id: Int) {
        viewModelScope.launch {
            repository.deleteServiceById(id)
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginStatus.value = AuthStatus.Idle
        _signUpStatus.value = AuthStatus.Idle
    }

    fun clearAuthStatus() {
        _signUpStatus.value = AuthStatus.Idle
        _loginStatus.value = AuthStatus.Idle
    }

    // Factory Class
    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = AppDatabase.getDatabase(application)
                    val repository = MarketplaceRepository(database.userDao(), database.serviceDao())
                    return MarketplaceViewModel(application, repository) as T
                }
            }
    }
}

sealed interface AuthStatus {
    object Idle : AuthStatus
    object Loading : AuthStatus
    data class Success(val message: String) : AuthStatus
    data class Error(val message: String) : AuthStatus
}
