package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // Collect Room database flows as state
    val serviceOffers by viewModel.serviceOffers.collectAsState()
    val faqs by viewModel.faqs.collectAsState()
    val blogPosts by viewModel.blogPosts.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val portfolioItems by viewModel.portfolioItems.collectAsState()

    var activeTab by remember { mutableStateOf("Explore") }
    
    // Dialog/Editor states
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var activeServiceEditing by remember { mutableStateOf<ServiceOffer?>(null) }
    
    var showAddPortfolioDialog by remember { mutableStateOf(false) }
    var activePortfolioEditing by remember { mutableStateOf<PortfolioItem?>(null) }

    var showAddFaqDialog by remember { mutableStateOf(false) }
    var activeFaqEditing by remember { mutableStateOf<Faq?>(null) }

    var showAddBlogDialog by remember { mutableStateOf(false) }
    var activeBlogEditing by remember { mutableStateOf<BlogPost?>(null) }

    val scope = rememberCoroutineScope()
    
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EditorialHeader(
                isCmsActive = viewModel.isCmsModeEnabled,
                onToggleCms = { viewModel.toggleCmsMode() }
            )
        },
        bottomBar = {
            EditorialBottomNav(
                activeTab = activeTab,
                onTabSelected = { 
                    activeTab = it 
                    // Reset selected service highlight if transferring tabs willingly
                    if (it != "Book" && viewModel.selectedServiceForBooking == null) {
                        viewModel.cancelActiveBookingDraft()
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tab_crossfade"
            ) { targetTab ->
                when (targetTab) {
                    "Explore" -> {
                        ExploreTabScreen(
                            services = serviceOffers,
                            faqs = faqs,
                            isCmsActive = viewModel.isCmsModeEnabled,
                            onStartBooking = { service ->
                                viewModel.startBookingWith(service)
                                activeTab = "Book"
                            },
                            onAddService = { showAddServiceDialog = true },
                            onEditService = { activeServiceEditing = it; showAddServiceDialog = true },
                            onDeleteService = { viewModel.deleteServiceOffer(it) },
                            onAddFaq = { showAddFaqDialog = true },
                            onEditFaq = { activeFaqEditing = it; showAddFaqDialog = true },
                            onDeleteFaq = { viewModel.deleteFaq(it) }
                        )
                    }
                    "Portfolio" -> {
                        PortfolioTabScreen(
                            portfolioItems = portfolioItems,
                            isCmsActive = viewModel.isCmsModeEnabled,
                            onAddPortfolioItem = { showAddPortfolioDialog = true },
                            onEditPortfolioItem = { activePortfolioEditing = it; showAddPortfolioDialog = true },
                            onDeletePortfolioItem = { viewModel.deletePortfolioItem(it) }
                        )
                    }
                    "Book" -> {
                        BookTabScreen(
                            viewModel = viewModel,
                            bookings = bookings
                        )
                    }
                    "Journal" -> {
                        JournalTabScreen(
                            posts = blogPosts,
                            isCmsActive = viewModel.isCmsModeEnabled,
                            onAddPost = { showAddBlogDialog = true },
                            onEditPost = { activeBlogEditing = it; showAddBlogDialog = true },
                            onDeletePost = { viewModel.deleteBlogPost(it) }
                        )
                    }
                }
            }

            // --- Floating CMS indicator ---
            if (viewModel.isCmsModeEnabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE25C38))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "CMS Mode Active",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "STUDIO CMS MODE ACTIVE",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // --- CMS Direct Dialog Editors ---

    // Service Dialog
    if (showAddServiceDialog) {
        ServiceEditDialog(
            service = activeServiceEditing,
            onDismiss = { 
                showAddServiceDialog = false
                activeServiceEditing = null 
            },
            onSave = { title, desc, scopeStr, cost, imgUrl ->
                viewModel.saveServiceOffer(
                    id = activeServiceEditing?.id ?: 0,
                    title = title,
                    description = desc,
                    scope = scopeStr,
                    price = cost,
                    imageUrl = imgUrl
                )
                showAddServiceDialog = false
                activeServiceEditing = null
            }
        )
    }

    // FAQ Dialog
    if (showAddFaqDialog) {
        FaqEditDialog(
            faq = activeFaqEditing,
            onDismiss = {
                showAddFaqDialog = false
                activeFaqEditing = null
            },
            onSave = { q, a ->
                viewModel.saveFaq(activeFaqEditing?.id ?: 0, q, a)
                showAddFaqDialog = false
                activeFaqEditing = null
            }
        )
    }

    // Portfolio Dialog
    if (showAddPortfolioDialog) {
        PortfolioEditDialog(
            item = activePortfolioEditing,
            onDismiss = {
                showAddPortfolioDialog = false
                activePortfolioEditing = null
            },
            onSave = { title, cat, desc, imgUrl ->
                viewModel.savePortfolioItem(
                    id = activePortfolioEditing?.id ?: 0,
                    title = title,
                    category = cat,
                    description = desc,
                    imageUrl = imgUrl
                )
                showAddPortfolioDialog = false
                activePortfolioEditing = null
            }
        )
    }

    // Post/Blog Dialog
    if (showAddBlogDialog) {
        BlogEditDialog(
            post = activeBlogEditing,
            onDismiss = {
                showAddBlogDialog = false
                activeBlogEditing = null
            },
            onSave = { title, exc, text, imgUrl ->
                viewModel.saveBlogPost(
                    id = activeBlogEditing?.id ?: 0,
                    title = title,
                    excerpt = exc,
                    content = text,
                    imageUrl = imgUrl
                )
                showAddBlogDialog = false
                activeBlogEditing = null
            }
        )
    }
}

// ==========================================
// INDIVIDUAL TAB SCREENS
// ==========================================

@Composable
fun ExploreTabScreen(
    services: List<ServiceOffer>,
    faqs: List<Faq>,
    isCmsActive: Boolean,
    onStartBooking: (ServiceOffer) -> Unit,
    onAddService: () -> Unit,
    onEditService: (ServiceOffer) -> Unit,
    onDeleteService: (ServiceOffer) -> Unit,
    onAddFaq: () -> Unit,
    onEditFaq: (Faq) -> Unit,
    onDeleteFaq: (Faq) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("explore_screen"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // Hero Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE5E0DA))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&q=80&w=800")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Studio editorial hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Sleek Gradient overlay (Editorial Style)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                startY = 120f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SIGNATURE MONOGRAPH",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The Residential Space",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        lineHeight = 32.sp
                    )
                    Text(
                        text = "Complete high-end physical architectural coordination.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Services Divider section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STUDIO SERVICES",
                        style = MaterialTheme.typography.labelLarge,
                        color = EditorialTextMuted
                    )
                    Text(
                        text = "Boutique Design Retainers",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif
                    )
                }
                if (isCmsActive) {
                    IconButton(
                        onClick = onAddService,
                        modifier = Modifier
                            .background(EditorialButtonBg, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add offer", tint = Color.White)
                    }
                }
            }
        }

        // Active Service cards list
        if (services.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .border(1.dp, EditorialBorder, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Service offers published. Turn on Studio CMS Mode above to insert some.",
                        textAlign = TextAlign.Center,
                        color = EditorialTextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(services) { service ->
                ServiceCard(
                    offer = service,
                    isCmsMode = isCmsActive,
                    onBookClick = { onStartBooking(service) },
                    onEditClick = { onEditService(service) },
                    onDeleteClick = { onDeleteService(service) }
                )
            }
        }

        // FAQs Section Header
        item {
            HorizontalDivider(color = EditorialBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PROJECT INQUIRIES",
                        style = MaterialTheme.typography.labelLarge,
                        color = EditorialTextMuted
                    )
                    Text(
                        text = "Practice & Works FAQ",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif
                    )
                }
                if (isCmsActive) {
                    IconButton(
                        onClick = onAddFaq,
                        modifier = Modifier
                            .background(EditorialButtonBg, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add FAQ", tint = Color.White)
                    }
                }
            }
        }

        // FAQ accordion items
        if (faqs.isEmpty()) {
            item {
                Text(
                    text = "No FAQs listed.",
                    color = EditorialTextMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(faqs) { faq ->
                FaqAccordionItem(
                    faq = faq,
                    isCmsActive = isCmsActive,
                    onEditClick = { onEditFaq(faq) },
                    onDeleteClick = { onDeleteFaq(faq) }
                )
            }
        }

        // Studio Ethos / About Statement block
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditorialSurface, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "THE STUDIO ETHOS",
                        style = MaterialTheme.typography.labelLarge,
                        color = EditorialTextMuted
                    )
                    Text(
                        text = "Aeterna is directed around raw organic permanence, sculptural form, and eye-friendly luxury. We restrict active bookings to four residential restorations per cycle to maintain precise client dedication.",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light,
                        lineHeight = 24.sp,
                        color = EditorialTextMain
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ABOUT THE DESIGNER \nShivakumar, Principal Creative Director",
                        style = MaterialTheme.typography.labelSmall,
                        color = EditorialTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun PortfolioTabScreen(
    portfolioItems: List<PortfolioItem>,
    isCmsActive: Boolean,
    onAddPortfolioItem: () -> Unit,
    onEditPortfolioItem: (PortfolioItem) -> Unit,
    onDeletePortfolioItem: (PortfolioItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Living Room", "Kitchen", "Bedroom", "Commercial")
    
    // Detailed item dialog zoom state
    var itemToZoom by remember { mutableStateOf<PortfolioItem?>(null) }

    val filteredItems = if (selectedCategory == "All") {
        portfolioItems
    } else {
        portfolioItems.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("portfolio_screen")
    ) {
        // Category Filters bar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) EditorialButtonBg else EditorialSurface)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) EditorialWhite else EditorialTextMain,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SELECTED HISTORIC PROJECTS",
                style = MaterialTheme.typography.labelLarge,
                color = EditorialTextMuted
            )
            
            if (isCmsActive) {
                Button(
                    onClick = onAddPortfolioItem,
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Add, 
                        contentDescription = "Add project", 
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No portfolio listings match '$selectedCategory' category.",
                    color = EditorialTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Asymmetric feed
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { proj ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(EditorialSurface)
                            .clickable { itemToZoom = proj }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(proj.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = proj.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Category Tag
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EditorialBg.copy(alpha = 0.9f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = proj.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EditorialTextMain,
                                        fontSize = 9.sp
                                    )
                                }

                                // Interactive floating action overlay in CMS mode
                                if (isCmsActive) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onEditPortfolioItem(proj) },
                                            modifier = Modifier
                                                .background(EditorialWhite, CircleShape)
                                                .size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Edit, 
                                                contentDescription = "Edit project", 
                                                tint = EditorialTextMain,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeletePortfolioItem(proj) },
                                            modifier = Modifier
                                                .background(Color(0xFFE25C38), CircleShape)
                                                .size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete, 
                                                contentDescription = "Delete project", 
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = proj.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Serif
                                )
                                Text(
                                    text = proj.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EditorialTextMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "VIEW SPATIAL SPECIFICATIONS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EditorialTextMain,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Filled.ArrowForward,
                                        contentDescription = "Open project details",
                                        modifier = Modifier.size(12.dp),
                                        tint = EditorialTextMain
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal popup design spec sheet zooms
    itemToZoom?.let { selectedProj ->
        Dialog(onDismissRequest = { itemToZoom = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = EditorialBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, EditorialBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        AsyncImage(
                            model = selectedProj.imageUrl,
                            contentDescription = selectedProj.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedProj.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = EditorialTextMuted
                            )
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Certified Editorial Design",
                                tint = EditorialGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = selectedProj.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = EditorialTextMain
                        )

                        Text(
                            text = selectedProj.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = EditorialTextMain
                        )

                        HorizontalDivider(color = EditorialBorder, thickness = 1.dp)

                        Text(
                            text = "DESIGN BLUEPRINTS & SPATIAL SYSTEMS:",
                            style = MaterialTheme.typography.labelLarge,
                            color = EditorialTextMuted
                        )

                        val materials = listOf(
                            "Warm Concrete foundations paired with hand-cut Wabi limestone slab floors",
                            "Sculptural oiled white oak millwork with heavy bronzed finish latches",
                            "High-vaulted structural roof lights with customized motorized solar drapes",
                            "Textile: Belgian raw flax linens combined with heavy bouclé seating modules"
                        )

                        materials.forEach { mat ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text("•", color = EditorialGold, fontWeight = FontWeight.Bold)
                                Text(text = mat, style = MaterialTheme.typography.bodyMedium, color = EditorialTextMain)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { itemToZoom = null },
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Return to Portfolio", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookTabScreen(
    viewModel: MainViewModel,
    bookings: List<Booking>
) {
    val selectedService = viewModel.selectedServiceForBooking

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("book_screen"),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (selectedService == null) {
            // First display state: Helper instructions prompting them to start with a service
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        tint = EditorialTextMuted,
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "STUDIO RESERVATIONS",
                        style = MaterialTheme.typography.labelLarge,
                        color = EditorialTextMuted
                    )
                    Text(
                        text = "Booking is initiated from our Boutique Service page. Select a project retainer scope to secure your calendar slot.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = EditorialTextMain
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Please explore the 'Explore' tab and press 'Start Your Project' to begin.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = EditorialTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // End-to-end active scheduling and checkout step
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SECURE BOOKING RESERVATION",
                            style = MaterialTheme.typography.labelLarge,
                            color = EditorialTextMuted
                        )
                        Text(
                            text = selectedService.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Serif
                        )
                    }
                    IconButton(onClick = { viewModel.cancelActiveBookingDraft() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel booking draft")
                    }
                }
            }

            // Step 1: Calendar Grid
            item {
                BookingCalendarSec(
                    selectedDate = viewModel.selectedDate,
                    onDateSelect = { viewModel.selectedDate = it }
                )
            }

            // Step 2: Available Hours
            item {
                BookingHoursSec(
                    selectedTime = viewModel.selectedTime,
                    onTimeSelect = { viewModel.selectedTime = it }
                )
            }

            // Step 3: Intake details
            item {
                BookingIntakeForm(
                    clientName = viewModel.clientName,
                    clientEmail = viewModel.clientEmail,
                    clientPhone = viewModel.clientPhone,
                    projectDetails = viewModel.projectDetails,
                    onNameChange = { viewModel.clientName = it },
                    onEmailChange = { viewModel.clientEmail = it },
                    onPhoneChange = { viewModel.clientPhone = it },
                    onDetailsChange = { viewModel.projectDetails = it }
                )
            }

            // Step 4: Secure dummy payment section (Stripe/PayPal imitation checkout)
            item {
                BookingCheckoutForm(
                    viewModel = viewModel,
                    paymentMethod = viewModel.paymentMethod,
                    cardNumber = viewModel.cardNumber,
                    cardExpiry = viewModel.cardExpiry,
                    cardCvc = viewModel.cardCvc,
                    price = selectedService.price,
                    onMethodSelector = { viewModel.paymentMethod = it },
                    onCardNumberChange = { viewModel.cardNumber = it },
                    onCardExpiryChange = { viewModel.cardExpiry = it },
                    onCardCvcChange = { viewModel.cardCvc = it },
                    isProcessing = viewModel.isPaymentProcessing,
                    errorMessage = viewModel.paymentErrorMessage,
                    onSubmitBooking = {
                        viewModel.submitBookingFlow {}
                    }
                )
            }
        }

        // --- Recurrence & Past Bookings Log section ---
        if (bookings.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = EditorialBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "YOUR ACTIVE TRANSACTIONS & BOOKINGS:",
                    style = MaterialTheme.typography.labelLarge,
                    color = EditorialTextMuted
                )
            }

            items(bookings) { book ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = EditorialSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EditorialBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = book.clientName.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EditorialTextMuted
                                )
                                Text(
                                    text = book.projectDetails,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                            IconButton(onClick = { viewModel.deleteBookingRecord(book) }) {
                                Icon(
                                    Icons.Filled.Delete, 
                                    contentDescription = "Delete appointment log",
                                    tint = Color(0xFFE25C38),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.DateRange, contentDescription = "Date", modifier = Modifier.size(14.dp))
                                Text(text = book.bookingDate, style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Time", modifier = Modifier.size(14.dp))
                                Text(text = book.bookingTime, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paid retainer: $${String.format("%.2f", book.amountPaid)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEAF5EA))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SECURED & CONFIRMED",
                                    color = Color(0xFF1E5C1C),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Success invoice billing overlays
    viewModel.bookingConfirmationReceipt?.let { receipt ->
        Dialog(onDismissRequest = { viewModel.bookingConfirmationReceipt = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = EditorialBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, EditorialBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFEAF5EA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle, 
                            contentDescription = "Success", 
                            tint = Color(0xFF1E5C1C),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "RESERVATION SECURED",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1E5C1C),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Thank You, ${receipt.clientName}",
                        style = MaterialTheme.typography.displayMedium,
                        color = EditorialTextMain,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Your booking for ${receipt.projectDetails} is confirmed. A dummy payment of $${String.format("%.2f", receipt.amountPaid)} was validated and processed safely.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = EditorialTextMain
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EditorialSurface, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Scheduled Date:", style = MaterialTheme.typography.bodyMedium, color = EditorialTextMuted)
                            Text(receipt.bookingDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Scheduled Time:", style = MaterialTheme.typography.bodyMedium, color = EditorialTextMuted)
                            Text(receipt.bookingTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transaction ID:", style = MaterialTheme.typography.bodyMedium, color = EditorialTextMuted)
                            Text("TXN-739${receipt.id}47A", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reserve Status:", style = MaterialTheme.typography.bodyMedium, color = EditorialTextMuted)
                            Text("PAID (MOCK)", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1E5C1C), fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "The studio director will contact you via email (${receipt.clientEmail}) within 1 business day to supply our detailed architectural styling draft survey sheet.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = EditorialTextMuted
                    )

                    Button(
                        onClick = { viewModel.cancelActiveBookingDraft() },
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun JournalTabScreen(
    posts: List<BlogPost>,
    isCmsActive: Boolean,
    onAddPost: () -> Unit,
    onEditPost: (BlogPost) -> Unit,
    onDeletePost: (BlogPost) -> Unit
) {
    var zoomedPost by remember { mutableStateOf<BlogPost?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("journal_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AETERNA READS",
                    style = MaterialTheme.typography.labelLarge,
                    color = EditorialTextMuted
                )
                Text(
                    text = "The Design Journal",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Serif
                )
            }

            if (isCmsActive) {
                IconButton(
                    onClick = onAddPost,
                    modifier = Modifier
                        .background(EditorialButtonBg, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add post", tint = Color.White)
                }
            }
        }

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "The journal space is empty. Toggle CMS Mode above to draft your first architectural essay tip.",
                    textAlign = TextAlign.Center,
                    color = EditorialTextMuted
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts) { post ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EditorialSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { zoomedPost = post }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                AsyncImage(
                                    model = post.imageUrl,
                                    contentDescription = post.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (isCmsActive) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onEditPost(post) },
                                            modifier = Modifier
                                                .background(EditorialWhite, CircleShape)
                                                .size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit post", modifier = Modifier.size(14.dp), tint = EditorialTextMain)
                                        }
                                        IconButton(
                                            onClick = { onDeletePost(post) },
                                            modifier = Modifier
                                                .background(Color(0xFFE25C38), CircleShape)
                                                .size(32.dp)
                                        ) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete post", modifier = Modifier.size(14.dp), tint = Color.White)
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = post.dateString.uppercase() + " • STYLE STUDY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EditorialTextMuted
                                )
                                Text(
                                    text = post.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialTextMain
                                )
                                Text(
                                    text = post.excerpt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EditorialTextMuted
                                )
                                Text(
                                    text = "READ ESSAY →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EditorialTextMain,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Essay Dialog reader zoom
    zoomedPost?.let { selectedPost ->
        Dialog(onDismissRequest = { zoomedPost = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = EditorialBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, EditorialBorder, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        AsyncImage(
                            model = selectedPost.imageUrl,
                            contentDescription = selectedPost.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = selectedPost.dateString.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = EditorialTextMuted
                        )
                        Text(
                            text = selectedPost.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = EditorialTextMain,
                            lineHeight = 34.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedPost.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = EditorialTextMain,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { zoomedPost = null },
                            colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Dismiss Essay", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-COMPONENTS
// ==========================================

@Composable
fun EditorialHeader(
    isCmsActive: Boolean,
    onToggleCms: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(EditorialBg)
            .border(width = 1.dp, color = EditorialBorder)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "STUDIO",
                style = MaterialTheme.typography.labelSmall,
                color = EditorialTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "AETERNA",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraLight,
                fontSize = 20.sp,
                letterSpacing = 2.sp
            )
        }

        // Top toggle bar for Owner "CMS Admin mode"
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isCmsActive) Color(0xFFE25C38) else EditorialSurface)
                .clickable { onToggleCms() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isCmsActive) Icons.Default.Lock else Icons.Default.Lock,
                    contentDescription = "Toggle owner mode",
                    modifier = Modifier.size(12.dp),
                    tint = if (isCmsActive) Color.White else EditorialTextMain
                )
                Text(
                    text = if (isCmsActive) "CLOSE CMS" else "OWNER CONFIG",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCmsActive) Color.White else EditorialTextMain
                )
            }
        }
    }
}

@Composable
fun EditorialBottomNav(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        Triple("Explore", Icons.Default.Home, Icons.Default.Home),
        Triple("Portfolio", Icons.Default.List, Icons.Default.List),
        Triple("Book", Icons.Default.DateRange, Icons.Default.DateRange),
        Triple("Journal", Icons.Default.Info, Icons.Default.Info)
    )

    NavigationBar(
        containerColor = EditorialSurface,
        modifier = Modifier
            .navigationBarsPadding()
            .border(1.dp, EditorialBorder)
    ) {
        items.forEach { (title, outlineIcon, filledIcon) ->
            val isSelected = activeTab == title
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(title) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) filledIcon else outlineIcon,
                        contentDescription = title,
                        tint = if (isSelected) EditorialButtonBg else EditorialTextMuted
                    )
                },
                label = {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) EditorialButtonBg else EditorialTextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = EditorialBorder
                )
            )
        }
    }
}

@Composable
fun ServiceCard(
    offer: ServiceOffer,
    isCmsMode: Boolean,
    onBookClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = EditorialWhite),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialBorder, RoundedCornerShape(20.dp))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(offer.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = offer.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isCmsMode) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .background(EditorialWhite, CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit offer", modifier = Modifier.size(14.dp), tint = EditorialTextMain)
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .background(Color(0xFFE25C38), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete offer", modifier = Modifier.size(14.dp), tint = Color.White)
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROJECT SCOPE: ${offer.scope.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EditorialTextMuted
                    )
                    Text(
                        text = "$${String.format("%.0f", offer.price)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp
                )

                Text(
                    text = offer.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EditorialTextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onBookClick,
                    colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "START YOUR PROJECT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = "Forward project booking",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FaqAccordionItem(
    faq: Faq,
    isCmsActive: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialBorder, RoundedCornerShape(12.dp))
            .background(EditorialWhite, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCmsActive) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit FAQ", modifier = Modifier.size(14.dp), tint = EditorialTextMain)
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete FAQ", modifier = Modifier.size(14.dp), tint = Color(0xFFE25C38))
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse FAQ" else "Expand FAQ",
                    tint = EditorialTextMuted
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                HorizontalDivider(color = EditorialBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EditorialTextMuted,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// Calendar Picker Sub-Interface
@Composable
fun BookingCalendarSec(
    selectedDate: String,
    onDateSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "STEP 1: SELECT YOUR INTAKE DATE (JUNE 2026)",
            style = MaterialTheme.typography.labelSmall,
            color = EditorialTextMuted,
            fontWeight = FontWeight.Bold
        )

        // Render days
        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = EditorialTextMuted,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // We simulate June 2026. June 1 is a Monday.
        val totalDays = 30
        val weeks = (1..totalDays).chunked(7)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    week.forEach { dayNum ->
                        val dateString = "June $dayNum, 2026"
                        val isSelected = selectedDate == dateString
                        
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) EditorialButtonBg else EditorialSurface)
                                .clickable { onDateSelect(dateString) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) EditorialWhite else EditorialTextMain
                            )
                        }
                    }
                    // Filler boxes if last week has less than 7 days
                    if (week.size < 7) {
                        repeat(7 - week.size) {
                            Box(modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }
    }
}

// Available Hours Sec
@Composable
fun BookingHoursSec(
    selectedTime: String,
    onTimeSelect: (String) -> Unit
) {
    val times = listOf("9:00 AM", "11:30 AM", "2:00 PM", "4:30 PM")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "STEP 2: SELECT AVAILABILITY",
            style = MaterialTheme.typography.labelSmall,
            color = EditorialTextMuted,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            times.forEach { time ->
                val isSelected = selectedTime == time
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) EditorialButtonBg else EditorialSurface)
                        .clickable { onTimeSelect(time) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) EditorialWhite else EditorialTextMain
                    )
                }
            }
        }
    }
}

// Booking Intake forms
@Composable
fun BookingIntakeForm(
    clientName: String,
    clientEmail: String,
    clientPhone: String,
    projectDetails: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "STEP 3: INTAKE SURVEY & DETAILS",
            style = MaterialTheme.typography.labelSmall,
            color = EditorialTextMuted,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = clientName,
            onValueChange = onNameChange,
            label = { Text("Your Full Name *") },
            placeholder = { Text("e.g. Shivakumar") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EditorialButtonBg,
                focusedLabelColor = EditorialButtonBg
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = clientEmail,
            onValueChange = onEmailChange,
            label = { Text("Your Email Address *") },
            placeholder = { Text("e.g. creative@aeterna.com") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EditorialButtonBg,
                focusedLabelColor = EditorialButtonBg
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = clientPhone,
            onValueChange = onPhoneChange,
            label = { Text("Your Phone (Optional)") },
            placeholder = { Text("e.g. +91 9845X XXXXX") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EditorialButtonBg,
                focusedLabelColor = EditorialButtonBg
            ),
            singleLine = true
        )

        OutlinedTextField(
            value = projectDetails,
            onValueChange = onDetailsChange,
            label = { Text("Spatial details & Design Aspirations") },
            placeholder = { Text("Describe room dimensions, material favorites, or structural goals...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EditorialButtonBg,
                focusedLabelColor = EditorialButtonBg
            )
        )
    }
}

// Payment checkout step
@Composable
fun BookingCheckoutForm(
    viewModel: MainViewModel,
    paymentMethod: String,
    cardNumber: String,
    cardExpiry: String,
    cardCvc: String,
    price: Double,
    onMethodSelector: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvcChange: (String) -> Unit,
    isProcessing: Boolean,
    errorMessage: String?,
    onSubmitBooking: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = EditorialSurface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, EditorialBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "STEP 4: SECURE PAYMENT DEPOSIT",
                style = MaterialTheme.typography.labelSmall,
                color = EditorialTextMuted,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditorialBg, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (paymentMethod == "card") EditorialButtonBg else Color.Transparent)
                        .clickable { onMethodSelector("card") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Credit Card (Stripe)",
                        color = if (paymentMethod == "card") EditorialWhite else EditorialTextMain,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (paymentMethod == "paypal") EditorialButtonBg else Color.Transparent)
                        .clickable { onMethodSelector("paypal") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PayPal Pro",
                        color = if (paymentMethod == "paypal") EditorialWhite else EditorialTextMain,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (paymentMethod == "card") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { if (it.length <= 16) onCardNumberChange(it) },
                        label = { Text("Card Number") },
                        placeholder = { Text("3782 8224 8292 9283") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Card") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EditorialButtonBg,
                            focusedLabelColor = EditorialButtonBg
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cardExpiry,
                            onValueChange = onCardExpiryChange,
                            label = { Text("MM/YY") },
                            placeholder = { Text("06/30") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EditorialButtonBg,
                                focusedLabelColor = EditorialButtonBg
                            )
                        )

                        OutlinedTextField(
                            value = cardCvc,
                            onValueChange = { if (it.length <= 4) onCardCvcChange(it) },
                            label = { Text("CVC") },
                            placeholder = { Text("382") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EditorialButtonBg,
                                focusedLabelColor = EditorialButtonBg
                            )
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EditorialBg, RoundedCornerShape(8.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "PayPal", tint = EditorialGold)
                        Text(
                            text = "Standard PayPal authentication will pop up upon checkout. Client verification is fully simulated with a mock success gateway.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = EditorialTextMuted
                        )
                    }
                }
            }

            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color(0xFFC62828),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onSubmitBooking,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "CONFIRM & SECURE DEPOSIT ($${String.format("%.0f", price)})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// CMS EDIT TOOL DIALOGS
// ==========================================

@Composable
fun ServiceEditDialog(
    service: ServiceOffer?,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, scope: String, cost: Double, imageUrl: String) -> Unit
) {
    var title by remember { mutableStateOf(service?.title ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var scope by remember { mutableStateOf(service?.scope ?: "") }
    var priceSec by remember { mutableStateOf(service?.price?.toString() ?: "") }
    var imageUrl by remember { mutableStateOf(service?.imageUrl ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = EditorialBg),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (service == null) "NEW SERVICE OFFER" else "EDIT SERVICE OFFER",
                    style = MaterialTheme.typography.labelLarge,
                    color = EditorialTextMuted
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Service Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Service Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = scope,
                    onValueChange = { scope = it },
                    label = { Text("Scope (e.g. 3D Renders • Install)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceSec,
                    onValueChange = { priceSec = it },
                    label = { Text("Pricing (Double)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Optional Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val priceValue = priceSec.toDoubleOrNull() ?: 100.0
                            onSave(title, description, scope, priceValue, imageUrl)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Offer", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FaqEditDialog(
    faq: Faq?,
    onDismiss: () -> Unit,
    onSave: (q: String, a: String) -> Unit
) {
    var question by remember { mutableStateOf(faq?.question ?: "") }
    var answer by remember { mutableStateOf(faq?.answer ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = EditorialBg),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (faq == null) "NEW FAQ ITEM" else "EDIT FAQ ITEM",
                    style = MaterialTheme.typography.labelLarge,
                    color = EditorialTextMuted
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("FAQ Question") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("FAQ Answer") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave(question, answer) },
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save FAQ", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioEditDialog(
    item: PortfolioItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, description: String, imageUrl: String) -> Unit
) {
    var title by remember { mutableStateOf(item?.title ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Living Room") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var imageUrl by remember { mutableStateOf(item?.imageUrl ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = EditorialBg),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (item == null) "NEW PORTFOLIO DESIGN" else "EDIT PORTFOLIO DESIGN",
                    style = MaterialTheme.typography.labelLarge,
                    color = EditorialTextMuted
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Living Room, Kitchen, Bedroom, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Architectural Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave(title, category, description, imageUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Project", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BlogEditDialog(
    post: BlogPost?,
    onDismiss: () -> Unit,
    onSave: (title: String, excerpt: String, content: String, imageUrl: String) -> Unit
) {
    var title by remember { mutableStateOf(post?.title ?: "") }
    var excerpt by remember { mutableStateOf(post?.excerpt ?: "") }
    var content by remember { mutableStateOf(post?.content ?: "") }
    var imageUrl by remember { mutableStateOf(post?.imageUrl ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = EditorialBg),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (post == null) "NEW DESIGN JOURNAL ESSAY" else "EDIT ESSAY",
                    style = MaterialTheme.typography.labelLarge,
                    color = EditorialTextMuted
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Essay Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    label = { Text("Summary Excerpt") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Full Essay Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onSave(title, excerpt, content, imageUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = EditorialButtonBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Essay", color = Color.White)
                    }
                }
            }
        }
    }
}
