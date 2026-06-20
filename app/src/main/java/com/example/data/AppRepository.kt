package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val db: AppDatabase) {

    private val serviceOfferDao = db.serviceOfferDao()
    private val bookingDao = db.bookingDao()
    private val faqDao = db.faqDao()
    private val blogPostDao = db.blogPostDao()
    private val portfolioItemDao = db.portfolioItemDao()

    fun getServiceOffers(): Flow<List<ServiceOffer>> = serviceOfferDao.getAllOffersFlow()
    fun getBookings(): Flow<List<Booking>> = bookingDao.getAllBookingsFlow()
    fun getFaqs(): Flow<List<Faq>> = faqDao.getAllFaqsFlow()
    fun getBlogPosts(): Flow<List<BlogPost>> = blogPostDao.getAllBlogPostsFlow()
    fun getPortfolioItems(): Flow<List<PortfolioItem>> = portfolioItemDao.getAllPortfolioItemsFlow()

    // Service methods
    suspend fun saveServiceOffer(offer: ServiceOffer) = withContext(Dispatchers.IO) {
        if (offer.id == 0) {
            serviceOfferDao.insertOffer(offer)
        } else {
            serviceOfferDao.updateOffer(offer)
        }
    }

    suspend fun deleteServiceOffer(offer: ServiceOffer) = withContext(Dispatchers.IO) {
        serviceOfferDao.deleteOffer(offer)
    }

    // Booking methods
    suspend fun createBooking(booking: Booking): Long = withContext(Dispatchers.IO) {
        bookingDao.insertBooking(booking)
    }

    suspend fun deleteBooking(booking: Booking) = withContext(Dispatchers.IO) {
        bookingDao.deleteBooking(booking)
    }

    // FAQ methods
    suspend fun saveFaq(faq: Faq) = withContext(Dispatchers.IO) {
        if (faq.id == 0) {
            faqDao.insertFaq(faq)
        } else {
            faqDao.updateFaq(faq)
        }
    }

    suspend fun deleteFaq(faq: Faq) = withContext(Dispatchers.IO) {
        faqDao.deleteFaq(faq)
    }

    // Blog methods
    suspend fun saveBlogPost(post: BlogPost) = withContext(Dispatchers.IO) {
        if (post.id == 0) {
            blogPostDao.insertBlogPost(post)
        } else {
            blogPostDao.updateBlogPost(post)
        }
    }

    suspend fun deleteBlogPost(post: BlogPost) = withContext(Dispatchers.IO) {
        blogPostDao.deleteBlogPost(post)
    }

    // Portfolio methods
    suspend fun savePortfolioItem(item: PortfolioItem) = withContext(Dispatchers.IO) {
        if (item.id == 0) {
            portfolioItemDao.insertPortfolioItem(item)
        } else {
            portfolioItemDao.updatePortfolioItem(item)
        }
    }

    suspend fun deletePortfolioItem(item: PortfolioItem) = withContext(Dispatchers.IO) {
        portfolioItemDao.deletePortfolioItem(item)
    }

    suspend fun prepopulateDefaults() = withContext(Dispatchers.IO) {
        // Pre-populate service offers if empty
        val currentOffers = serviceOfferDao.getAllOffersFlow().first()
        if (currentOffers.isEmpty()) {
            serviceOfferDao.insertOffer(
                ServiceOffer(
                    title = "The Full-Scale Design",
                    description = "A complete, hands-on spatial metamorphosis. We craft bespoke spatial drawings, photorealistic 3D renderings, and catalog curated furnishing plans tailored around your residential architectural details. We fully coordinate vendor sourcing, transport logistics, and on-site hand install oversight.",
                    scope = "3D Renders • Full Sourcing • Install",
                    price = 4200.0,
                    priceLabel = "flat fee",
                    imageUrl = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&q=80&w=800"
                )
            )
            serviceOfferDao.insertOffer(
                ServiceOffer(
                    title = "The Signature Concept",
                    description = "Our exclusive virtual design spec package. Designed for the remote client desiring top-tier editorial design blueprints. We construct elevation drawings, an interactive tactile palette specification sheet, and direct click-and-buy links.",
                    scope = "Moodboards • Digital Layouts • Sourcing List",
                    price = 1800.0,
                    priceLabel = "per room",
                    imageUrl = "https://images.unsplash.com/photo-1617806118233-18e1db207f62?auto=format&fit=crop&q=80&w=800"
                )
            )
        }

        // Pre-populate FAQs if empty
        val currentFaqs = faqDao.getAllFaqsFlow().first()
        if (currentFaqs.isEmpty()) {
            faqDao.insertFaq(
                Faq(
                    question = "What does the Full Design Project entail?",
                    answer = "It is our premium, high-contrast signature package covering end-to-end service. Includes layout drafting, realistic 3D architectural representations, meticulous materials specification, shipping liaison, and in-person final design placement."
                )
            )
            faqDao.insertFaq(
                Faq(
                    question = "Are shipping or furnishing costs covered in the fee?",
                    answer = "No, the service represents a styling and orchestration flat retainer fee. Furniture procurement budgets are specified by clients and managed independently using trade discounts where possible."
                )
            )
            faqDao.insertFaq(
                Faq(
                    question = "How long does a full-scale redesign process take?",
                    answer = "Usually between 4 to 6 weeks from initial intake to final sourcing delivery. Custom artisan upholstery or select millwork fabrication can lengthen the installation window."
                )
            )
            faqDao.insertFaq(
                Faq(
                    question = "Are layouts customizable to commercial offices?",
                    answer = "Absolutely. While we prioritize organic residential experiences, we adapt our curated spacing and sensory concepts to boutique commercial reception foyers or hospitality suites."
                )
            )
        }

        // Pre-populate Blog posts if empty
        val currentBlogs = blogPostDao.getAllBlogPostsFlow().first()
        if (currentBlogs.isEmpty()) {
            blogPostDao.insertBlogPost(
                BlogPost(
                    title = "The Golden Rules of Textural Contrast",
                    excerpt = "How blending coarse plaster, tactile linen, and walnut millwork crafts organic warmth.",
                    content = "Layering remains the defining secret of editorial residential styling. To prevent minimalist configurations from feeling sterile, introduce a minimum of three distinct surface grains. Pair cold elements like brushed silver or fluted travertine alongside incredibly rich, warm textures like custom bouclé seating, rich mohair, or untreated raw oak cabinetry. Lighting will wrap around these tactile variations to produce cozy shadows.",
                    dateString = "June 18, 2026",
                    imageUrl = "https://images.unsplash.com/photo-1556912173-3bb406ef7e77?auto=format&fit=crop&q=80&w=800"
                )
            )
            blogPostDao.insertBlogPost(
                BlogPost(
                    title = "Choreographing Ambiance with Tiered Illumination",
                    excerpt = "Ditch single high-wattage ceiling fixtures. Create three tiers of soft golden radiance.",
                    content = "Overhead ceiling lamps are the enemies of peaceful luxury. Instead, aim to diffuse golden pools of light at three distinct heights: eye-level sconces that project upwards, intermediate reading floor lamps with spun-silk linen shades, and low ground uplights reflecting from raw clay decorative pots. Avoid cool or blue temperature indicators; strictly design with warm 2700K bulbs.",
                    dateString = "June 10, 2026",
                    imageUrl = "https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&q=80&w=800"
                )
            )
        }

        // Pre-populate Portfolio items if empty
        val currentPortfolio = portfolioItemDao.getAllPortfolioItemsFlow().first()
        if (currentPortfolio.isEmpty()) {
            portfolioItemDao.insertPortfolioItem(
                PortfolioItem(
                    title = "The Atrium Living Sanctum",
                    category = "Living Room",
                    imageUrl = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&q=80&w=800",
                    description = "An airy, sun-kissed architectural configuration that honors raw limestone, custom hand-spun rugs, and low-slung tailored Belgian linen sofas."
                )
            )
            portfolioItemDao.insertPortfolioItem(
                PortfolioItem(
                    title = "Wabi-Sabi Sleep Lounge",
                    category = "Bedroom",
                    imageUrl = "https://images.unsplash.com/photo-1617806118233-18e1db207f62?auto=format&fit=crop&q=80&w=800",
                    description = "A deeply peaceful chamber defined by earth-clay walls, Japanese paper ambient lanterns, and a custom platform carved from salvaged reclaimed timber."
                )
            )
            portfolioItemDao.insertPortfolioItem(
                PortfolioItem(
                    title = "Concrete Culinary Atelier",
                    category = "Kitchen",
                    imageUrl = "https://images.unsplash.com/photo-1556912173-3bb406ef7e77?auto=format&fit=crop&q=80&w=800",
                    description = "Washed concrete structures layered seamlessly under white oak custom cabinets, fitted with heavy matte bronze hardware detail accents."
                )
            )
        }
    }
}
