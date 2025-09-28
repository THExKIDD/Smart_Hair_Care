@file:OptIn(ExperimentalMaterial3Api::class)

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data classes for oil recommendations
data class OilRecommendation(
    val name: String,
    val description: String,
    val price: String = "₹299"
)

data class HairTip(
    val title: String,
    val description: String
)

// Oil recommendation logic
object OilRecommendationEngine {
    private val oils = mapOf(
        "moroccanoil" to OilRecommendation(
            "Moroccanoil",
            "Premium argan oil for all hair types"
        ),
        "indulekha" to OilRecommendation(
            "Indulekha Bringha Oil",
            "Ayurvedic blend for hair growth"
        ),
        "rosemary" to OilRecommendation(
            "Rosemary Hair Oil",
            "Natural stimulant for hair follicles"
        ),
        "jojoba" to OilRecommendation(
            "Jojoba Oil",
            "Lightweight moisturizer for scalp"
        ),
        "thrive" to OilRecommendation(
            "Thrive Frizz Free Oil",
            "Anti-frizz formula for smooth hair"
        )
    )

    fun getRecommendations(
        hairType: String,
        dandruffLevel: String,
        hairLossStage: String
    ): List<OilRecommendation> {
        val recommendations = mutableSetOf<String>()

        // Base recommendations by hair type
        when (hairType.lowercase()) {
            "wavy", "curly" -> {
                recommendations.add("thrive")
                recommendations.add("jojoba")
            }
            "straight" -> {
                recommendations.add("moroccanoil")
                recommendations.add("jojoba")
            }
            "dry" -> {
                recommendations.add("moroccanoil")
                recommendations.add("jojoba")
            }
            else -> {
                recommendations.add("moroccanoil")
                recommendations.add("jojoba")
            }
        }

        // Add oils based on dandruff level
        when (dandruffLevel.lowercase()) {
            "high" -> {
                recommendations.add("rosemary")
                recommendations.add("jojoba")
            }
            "mid" -> {
                recommendations.add("rosemary")
            }
        }

        // Add oils based on hair loss stage
        when (hairLossStage) {
            "Stage 3", "Stage 4" -> {
                recommendations.add("indulekha")
                recommendations.add("rosemary")
            }
            "Stage 2" -> {
                recommendations.add("indulekha")
            }
        }

        // Ensure we have at least 3 recommendations
        if (recommendations.size < 3) {
            recommendations.add("moroccanoil")
            recommendations.add("indulekha")
            recommendations.add("rosemary")
        }

        return recommendations.take(3).mapNotNull { oils[it] }
    }

    fun getTips(hairType: String, dandruffLevel: String, hairLossStage: String): List<HairTip> {
        val tips = mutableListOf<HairTip>()

        // Hair type specific tips
        when (hairType.lowercase()) {
            "wavy", "curly" -> {
                tips.add(HairTip("Gentle Cleansing", "Use sulfate-free shampoo to maintain natural oils"))
                tips.add(HairTip("Moisture Lock", "Apply leave-in conditioner while hair is damp"))
            }
            "straight" -> {
                tips.add(HairTip("Volume Boost", "Use volumizing products at the roots"))
                tips.add(HairTip("Heat Protection", "Always use heat protectant before styling"))
            }
            else -> {
                tips.add(HairTip("Regular Oiling", "Massage scalp with oil 2-3 times per week"))
                tips.add(HairTip("Gentle Handling", "Avoid harsh brushing when hair is wet"))
            }
        }

        // Dandruff specific tips
        when (dandruffLevel.lowercase()) {
            "high" -> {
                tips.add(HairTip("Anti-Dandruff Care", "Use medicated shampoo twice a week"))
            }
            "mid" -> {
                tips.add(HairTip("Scalp Health", "Regular scalp massage to improve circulation"))
            }
        }

        // Hair loss specific tips
        when (hairLossStage) {
            "Stage 3", "Stage 4" -> {
                tips.add(HairTip("Professional Care", "Consider consulting a trichologist"))
            }
            "Stage 2" -> {
                tips.add(HairTip("Early Intervention", "Use growth-promoting oils regularly"))
            }
        }

        return tips.take(4)
    }
}

@Composable
fun ResultsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    imageUri: Uri,
    scanResponse: ScanResponse?,
    userSelections: UserSelections
) {

    LaunchedEffect(scanResponse) {
        Log.d("ResultsScreen", "Received scanResponse: $scanResponse")
        Log.d("ResultsScreen", "scanResponse.result: ${scanResponse?.result}")
        Log.d("ResultsScreen", "Hair type: ${scanResponse?.result?.predicted_hair_type}")
        Log.d("ResultsScreen", "Confidence: ${scanResponse?.result?.confidence_percentage}")
    }
    val hairType = scanResponse?.result?.predicted_hair_type ?: "Unknown"
    val confidence = scanResponse?.result?.confidence_percentage ?: "0%"

    val recommendedOils = remember {
        OilRecommendationEngine.getRecommendations(
            hairType = hairType,
            dandruffLevel = userSelections.dandruffLevel,
            hairLossStage = userSelections.hairLossStage
        )
    }

    val tips = remember {
        OilRecommendationEngine.getTips(
            hairType = hairType,
            dandruffLevel = userSelections.dandruffLevel,
            hairLossStage = userSelections.hairLossStage
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hair Analysis Results",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hair Analysis Results Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Hair Type Detected",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    hairType.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    confidence,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // User selections summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Dandruff",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        userSelections.dandruffLevel,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Hair Loss",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        userSelections.hairLossStage,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recommended Oils Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recommended for You",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Based on your hair type and preferences",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // Oil Cards
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(recommendedOils) { oil ->
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Oil Icon",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        oil.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        oil.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        oil.price,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { /* Handle buy now */ },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Buy Now",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tips Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Personalized Hair Care Tips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Tips Cards
            items(tips) { tip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            tip.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            tip.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Scan Again",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Button(
                        onClick = onNavigateToHome,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Back to Home",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}