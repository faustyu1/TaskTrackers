package ru.faustyu.tasktrackers.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 base palette - used as fallback when dynamic colors not available
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Tag group header colors
val ReadinessGroupColor = Color(0xFF42A5F5)
val ImportanceGroupColor = Color(0xFFFF7043)
val UrgencyGroupColor = Color(0xFFFFA726)
val SphereGroupColor = Color(0xFF5C6BC0)
val CustomGroupColor = Color(0xFF78909C)

// Tag chip colors - Readiness
val TagNotStarted = Color(0xFF78909C)
val TagInProgress = Color(0xFF42A5F5)
val TagDone = Color(0xFF66BB6A)

// Tag chip colors - Importance
val TagLow = Color(0xFFA5D6A7)
val TagMedium = Color(0xFFFFD54F)
val TagHigh = Color(0xFFFF7043)
val TagCritical = Color(0xFFEF5350)

// Tag chip colors - Urgency
val TagNotUrgent = Color(0xFF81C784)
val TagUrgent = Color(0xFFFFA726)
val TagOnFire = Color(0xFFF44336)

// Tag chip colors - Sphere
val TagWork = Color(0xFF5C6BC0)
val TagPersonal = Color(0xFFAB47BC)
val TagHome = Color(0xFF8D6E63)
val TagShopping = Color(0xFF26A69A)
val TagHealth = Color(0xFFEC407A)
val TagFinance = Color(0xFFFFA000)
val TagEducation = Color(0xFF29B6F6)

// Task card states
val CompletedTaskOverlay = Color(0x1A66BB6A)
val CompletedTaskOverlayDark = Color(0x2666BB6A)

// Swipe delete background
val SwipeDeleteBackground = Color(0xFFEF5350)
val SwipeDeleteBackgroundDark = Color(0xFFD32F2F)

// Custom accent colors for premium feel
val GradientStart = Color(0xFF6366F1)
val GradientEnd = Color(0xFF8B5CF6)
val GradientStartDark = Color(0xFF818CF8)
val GradientEndDark = Color(0xFFA78BFA)

// Parse hex color from tag data
fun parseTagColor(hexColor: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: Exception) {
        Color(0xFF78909C)
    }
}