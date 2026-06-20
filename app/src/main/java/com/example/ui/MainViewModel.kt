package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    // Reactive streams from database
    val serviceOffers: StateFlow<List<ServiceOffer>> = repository.getServiceOffers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val faqs: StateFlow<List<Faq>> = repository.getFaqs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blogPosts: StateFlow<List<BlogPost>> = repository.getBlogPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookings: StateFlow<List<Booking>> = repository.getBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val portfolioItems: StateFlow<List<PortfolioItem>> = repository.getPortfolioItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // CMS Mode Toggle
    var isCmsModeEnabled by mutableStateOf(false)
        private set

    fun toggleCmsMode() {
        isCmsModeEnabled = !isCmsModeEnabled
    }

    // Active Booking draft flow state
    var selectedServiceForBooking by mutableStateOf<ServiceOffer?>(null)
    var selectedDate by mutableStateOf("")
    var selectedTime by mutableStateOf("")
    var clientName by mutableStateOf("")
    var clientEmail by mutableStateOf("")
    var clientPhone by mutableStateOf("")
    var projectDetails by mutableStateOf("")

    // Simulated Stripe/PayPal Payment credentials
    var cardNumber by mutableStateOf("")
    var cardExpiry by mutableStateOf("")
    var cardCvc by mutableStateOf("")
    var paymentMethod by mutableStateOf("card") // "card" or "paypal"

    var isPaymentProcessing by mutableStateOf(false)
    var bookingConfirmationReceipt by mutableStateOf<Booking?>(null)
    var paymentErrorMessage by mutableStateOf<String?>(null)

    init {
        // Prepopulate with elegant layout items if database is freshly built
         viewModelScope.launch {
             repository.prepopulateDefaults()
         }
    }

    fun startBookingWith(service: ServiceOffer) {
        selectedServiceForBooking = service
        // Reset booking process states
        selectedDate = ""
        selectedTime = ""
        clientName = ""
        clientEmail = ""
        clientPhone = ""
        projectDetails = ""
        cardNumber = ""
        cardExpiry = ""
        cardCvc = ""
        bookingConfirmationReceipt = null
        paymentErrorMessage = null
    }

    fun submitBookingFlow(onSuccess: () -> Unit) {
        if (clientName.isBlank() || clientEmail.isBlank() || selectedDate.isBlank() || selectedTime.isBlank()) {
            paymentErrorMessage = "Please fill in all mandatory reservation details."
            return
        }

        if (paymentMethod == "card") {
            if (cardNumber.length < 16 || cardExpiry.isBlank() || cardCvc.length < 3) {
                paymentErrorMessage = "Please present a valid 16-digit card number, expiry date, and CVC code."
                return
            }
        }

        viewModelScope.launch {
            isPaymentProcessing = true
            paymentErrorMessage = null
            
            // Artificial delay to simulate processing with real payment gateway (Stripe/Paypal)
            kotlinx.coroutines.delay(2000)

            val chargeAmount = selectedServiceForBooking?.price ?: 0.0
            val newBooking = Booking(
                clientName = clientName,
                clientEmail = clientEmail,
                clientPhone = clientPhone,
                projectDetails = "${selectedServiceForBooking?.title ?: "Full-Scale Setup"} - $projectDetails",
                bookingDate = selectedDate,
                bookingTime = selectedTime,
                status = "Confirmed (Mock Paid - $paymentMethod)",
                amountPaid = chargeAmount
            )

            val bookingId = repository.createBooking(newBooking)
            val savedBooking = newBooking.copy(id = bookingId.toInt())
            bookingConfirmationReceipt = savedBooking
            isPaymentProcessing = false
            onSuccess()
        }
    }

    fun cancelActiveBookingDraft() {
        selectedServiceForBooking = null
        bookingConfirmationReceipt = null
    }

    // CMS functions: Service offers
    fun saveServiceOffer(id: Int, title: String, description: String, scope: String, price: Double, imageUrl: String) {
        viewModelScope.launch {
            val offer = ServiceOffer(
                id = id,
                title = title,
                description = description,
                scope = scope,
                price = price,
                priceLabel = if (title.contains("Room", ignoreCase = true)) "per room" else "flat fee",
                imageUrl = if (imageUrl.isBlank()) "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&q=80&w=800" else imageUrl
            )
            repository.saveServiceOffer(offer)
        }
    }

    fun deleteServiceOffer(offer: ServiceOffer) {
        viewModelScope.launch {
            repository.deleteServiceOffer(offer)
        }
    }

    // CMS functions: Portfolio
    fun savePortfolioItem(id: Int, title: String, category: String, description: String, imageUrl: String) {
        viewModelScope.launch {
            val item = PortfolioItem(
                id = id,
                title = title,
                category = category,
                description = description,
                imageUrl = if (imageUrl.isBlank()) "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&q=80&w=800" else imageUrl
            )
            repository.savePortfolioItem(item)
        }
    }

    fun deletePortfolioItem(item: PortfolioItem) {
        viewModelScope.launch {
            repository.deletePortfolioItem(item)
        }
    }

    // CMS functions: FAQs
    fun saveFaq(id: Int, question: String, answer: String) {
        viewModelScope.launch {
            val faq = Faq(id = id, question = question, answer = answer)
            repository.saveFaq(faq)
        }
    }

    fun deleteFaq(faq: Faq) {
        viewModelScope.launch {
            repository.deleteFaq(faq)
        }
    }

    // CMS functions: Blog
    fun saveBlogPost(id: Int, title: String, excerpt: String, content: String, imageUrl: String) {
        viewModelScope.launch {
            val post = BlogPost(
                id = id,
                title = title,
                excerpt = excerpt,
                content = content,
                dateString = "June 20, 2026",
                imageUrl = if (imageUrl.isBlank()) "https://images.unsplash.com/photo-1556912173-3bb406ef7e77?auto=format&fit=crop&q=80&w=800" else imageUrl
            )
            repository.saveBlogPost(post)
        }
    }

    fun deleteBlogPost(post: BlogPost) {
        viewModelScope.launch {
            repository.deleteBlogPost(post)
        }
    }

    // Delete existing booked reservation
    fun deleteBookingRecord(booking: Booking) {
        viewModelScope.launch {
            repository.deleteBooking(booking)
        }
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
