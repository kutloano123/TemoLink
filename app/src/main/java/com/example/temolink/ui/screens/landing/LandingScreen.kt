package com.example.temolink.ui.screens.landing

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temolink.ui.components.AppImage
import com.example.temolink.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: (String?) -> Unit,
    onNavigateToMarketplace: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SecondaryNavy,
                drawerContentColor = PrimaryText
            ) {
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = PanelBlue
                    )
                )
                NavigationDrawerItem(
                    label = { Text("Marketplace") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToMarketplace()
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("How It Works") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("Why TemoLink") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("Create Account") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToRegister(null)
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("Login") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToLogin()
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "TemoLink",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = PrimaryText)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
                )
            },
            containerColor = DarkNavy
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { HeroSection(onNavigateToRegister, onNavigateToMarketplace) }
                item { ImageCarousel() }
                item { HowItWorksSection() }
                item { WhyTemoLinkSection() }
                item { FooterSection() }
            }
        }
    }
}

@Composable
fun HeroSection(onNavigateToRegister: (String?) -> Unit, onExplore: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                append("Connecting Farmers with ")
                withStyle(
                    style = SpanStyle(
                        brush = Brush.horizontalGradient(listOf(PrimaryBlue, PrimaryGreen))
                    )
                ) {
                    append("Better Markets")
                }
            },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = PrimaryText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TemoLink helps small-scale farmers connect directly with buyers and make their agricultural produce easier to discover.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = SecondaryText
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onNavigateToRegister(null) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Get Started", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onExplore,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Explore Marketplace")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "I'm a Farmer",
                color = PrimaryBlue,
                modifier = Modifier.clickable { onNavigateToRegister("FARMER") },
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "I'm a Buyer",
                color = PrimaryBlue,
                modifier = Modifier.clickable { onNavigateToRegister("BUYER") },
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

data class CarouselSlide(
    val label: String,
    val title: String,
    val description: String,
    val imageUrl: String
)

@Composable
fun ImageCarousel() {
    val slides = listOf(
        CarouselSlide("FRESH PRODUCE", "Discover fresh produce", "Find agricultural products directly from local farmers.", "https://images.unsplash.com/photo-1488459711615-46802c221469?q=80&w=1000&auto=format&fit=crop"),
        CarouselSlide("LOCAL FARMERS", "Connect with local farmers", "Build direct connections between farmers and buyers.", "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?q=80&w=1000&auto=format&fit=crop"),
        CarouselSlide("BETTER MARKETS", "Better opportunities for farmers", "Make agricultural produce easier to discover and sell.", "https://images.unsplash.com/photo-1523348837708-15d4a09cfac2?q=80&w=1000&auto=format&fit=crop")
    )

    var currentSlide by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentSlide = (currentSlide + 1) % slides.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(PanelBlue)
        ) {
            AppImage(
                model = slides[currentSlide].imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = slides[currentSlide].label,
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = slides[currentSlide].title,
                    color = PrimaryText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = slides[currentSlide].description,
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            slides.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (currentSlide == index) PrimaryBlue else MutedText)
                        .clickable { currentSlide = index }
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun HowItWorksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "How It Works",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        StepItem(
            number = "1",
            title = "Register",
            description = "Create your account as a farmer or a buyer in just a few minutes."
        )
        
        StepItem(
            number = "2",
            title = "List or Browse",
            description = "Farmers list their produce. Buyers browse through the available categories."
        )
        
        StepItem(
            number = "3",
            title = "Connect",
            description = "Direct contact between parties to finalize the deal."
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun StepItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
        }
    }
}

@Composable
fun WhyTemoLinkSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PanelBlue)
            .padding(24.dp)
    ) {
        Text(
            text = "Why Choose TemoLink?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        FeatureCard("Direct Access", "Remove middlemen and connect directly with your target audience.")
        FeatureCard("Easy to Use", "Simple interface designed for everyone to navigate easily.")
        FeatureCard("Wide Reach", "Get your produce in front of more potential buyers across the region.")
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FeatureCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryNavy),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
        }
    }
}

@Composable
fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TemoLink",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )
        Text(
            text = "Empowering agriculture through technology.",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        HorizontalDivider(color = MutedText.copy(alpha = 0.2f))
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "© 2025 TemoLink. All rights reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = MutedText
        )
    }
}
