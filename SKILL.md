# Jetpack Compose Development Skill

## Overview
This skill provides comprehensive guidance for building modern, polished Android applications using Jetpack Compose. It addresses common convergence patterns in AI-generated Compose code and encourages distinctive, production-ready UI implementations.

## The Problem: Distributional Convergence
When asked to build Android UIs without guidance, LLMs often converge toward generic patterns:
- Default Material Design 3 components without customization
- Purple/Blue primary colors with minimal theming
- Standard Roboto fonts without typographic hierarchy
- Basic layouts lacking polish and micro-interactions
- Predictable navigation patterns

## Core Principles

### 1. Typography & Text Styling
Typography signals quality and brand identity in Android apps. Go beyond defaults.

**Avoid Generic Choices:**
- Default Roboto without variation
- Using only `MaterialTheme.typography.bodyLarge` everywhere
- No weight or size hierarchy
- Missing custom font families

**Better Approaches:**
```kotlin
// Define distinctive typography with clear hierarchy
val CustomTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif, // Or custom font
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace, // For code/technical content
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
```

**Font Pairing Principles:**
- High contrast creates interest: Serif headers + Sans body, or Display + Monospace
- Use weight extremes: FontWeight.Light (300) vs FontWeight.Black (900)
- Size jumps of 3x+ for impact, not just 1.5x

**Loading Custom Fonts:**
```kotlin
// res/font/font_family.xml
val CustomFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.space_grotesk_medium, FontWeight.Medium)
)
```

### 2. Color Schemes & Theming
Create cohesive, memorable color systems that go beyond Material Design defaults.

**Avoid:**
- Generic purple/blue Material color schemes
- Using only primary/secondary without customization
- Lack of semantic color tokens
- No dark/light theme variation

**Better Approaches:**
```kotlin
// Define distinctive color palette
private val CustomLightColors = lightColorScheme(
    primary = Color(0xFF006B5B), // Teal instead of purple
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF7FF9DD),
    secondary = Color(0xFF4A6360),
    tertiary = Color(0xFF4B5F7C),
    background = Color(0xFFFAFDFB),
    surface = Color(0xFFF5FBF8),
    error = Color(0xFFBA1A1A)
)

private val CustomDarkColors = darkColorScheme(
    primary = Color(0xFF5DDCBC),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005144),
    secondary = Color(0xFFB0CCC7),
    tertiary = Color(0xFFB1C8E6),
    background = Color(0xFF191C1B),
    surface = Color(0xFF191C1B),
    error = Color(0xFFFFB4AB)
)
```

**Semantic Color System:**
```kotlin
// Create custom color tokens for specific use cases
data class ExtendedColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val surfaceVariant2: Color
)

val LocalExtendedColors = compositionLocalOf {
    ExtendedColors(
        success = Color(0xFF4CAF50),
        warning = Color(0xFFFF9800),
        info = Color(0xFF2196F3),
        surfaceVariant2 = Color(0xFFF5F5F5)
    )
}
```

### 3. Layouts & Composition
Build thoughtful, responsive layouts that feel intentionally designed.

**Avoid:**
- Flat Column/Row layouts without depth
- No spacing/padding hierarchy
- Missing elevation and shadows
- Generic card designs
- No responsive breakpoints

**Better Approaches:**
```kotlin
// Use layered composition with depth
@Composable
fun RichCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

// Implement responsive layouts
@Composable
fun AdaptiveLayout(content: @Composable (WindowSizeClass) -> Unit) {
    val windowSize = calculateWindowSizeClass()
    content(windowSize)
}
```

**Spacing System:**
```kotlin
object Spacing {
    val xs = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
```

### 4. Motion & Animations
Add polish through purposeful animations and micro-interactions.

**Key Principles:**
- Animate state changes, not just appearance
- Use easing for natural motion
- Coordinate related animations with delays
- Provide immediate feedback for interactions

**Examples:**
```kotlin
// Smooth state transitions
@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.scale(scale)
    ) {
        content()
    }
}

// Coordinated entrance animations
@Composable
fun StaggeredList(items: List<String>) {
    LazyColumn {
        itemsIndexed(items) { index, item ->
            val animatedVisibility = remember { Animatable(0f) }
            
            LaunchedEffect(key1 = item) {
                delay(index * 50L) // Stagger delay
                animatedVisibility.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 300)
                )
            }
            
            Box(
                modifier = Modifier
                    .alpha(animatedVisibility.value)
                    .offset(y = ((1 - animatedVisibility.value) * 20).dp)
            ) {
                Text(item)
            }
        }
    }
}

// Interactive feedback
@Composable
fun PressableCard(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp
    )
    
    Card(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        content()
    }
}
```

### 5. Backgrounds & Visual Depth
Create atmosphere beyond solid colors.

**Techniques:**
```kotlin
// Gradient backgrounds
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2)
    )
) {
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(colors)
        )
    )
}

// Mesh gradient effect
@Composable
fun MeshGradientBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1a1a2e),
                    Color(0xFF16213e),
                    Color(0xFF0f3460)
                ),
                center = Offset(size.width * 0.3f, size.height * 0.4f),
                radius = size.maxDimension * 0.8f
            )
        )
    }
}

// Pattern overlays
@Composable
fun PatternedBackground(
    modifier: Modifier = Modifier,
    baseColor: Color = MaterialTheme.colorScheme.surface
) {
    Box(modifier = modifier) {
        // Base layer
        Box(modifier = Modifier
            .fillMaxSize()
            .background(baseColor)
        )
        
        // Pattern layer with reduced opacity
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
            val gridSize = 40f
            for (x in 0 until (size.width / gridSize).toInt()) {
                for (y in 0 until (size.height / gridSize).toInt()) {
                    drawCircle(
                        color = Color.White,
                        radius = 2f,
                        center = Offset(x * gridSize, y * gridSize)
                    )
                }
            }
        }
    }
}
```

### 6. Component Design Patterns

**Avoid Generic Components:**
- Basic Card with no customization
- Standard Button without states
- TextField with default styling
- List items without hierarchy

**Create Custom Components:**
```kotlin
// Custom input field with states
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon,
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        AnimatedVisibility(visible = error != null) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// Rich list item with hierarchy
@Composable
fun RichListItem(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            trailing?.invoke()
        }
    }
}
```

## Architecture & State Management

### Recommended Patterns
```kotlin
// ViewModel with proper state handling
class FeatureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    sealed class UiState {
        object Loading : UiState()
        data class Success(val data: List<Item>) : UiState()
        data class Error(val message: String) : UiState()
    }
}

// Composable with proper state handling
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is UiState.Loading -> LoadingView()
        is UiState.Success -> SuccessView((uiState as UiState.Success).data)
        is UiState.Error -> ErrorView((uiState as UiState.Error).message)
    }
}
```

## Performance Optimization

**Key Practices:**
```kotlin
// Use remember for expensive computations
@Composable
fun ExpensiveList(items: List<Item>) {
    val processedItems = remember(items) {
        items.map { processExpensiveOperation(it) }
    }
    // ...
}

// Stable keys for LazyColumn
LazyColumn {
    items(
        items = itemList,
        key = { item -> item.id } // Stable key
    ) { item ->
        ItemView(item)
    }
}

// Avoid unnecessary recompositions
@Composable
fun OptimizedComponent(
    data: Data,
    onEvent: (Event) -> Unit
) {
    val stableOnEvent = rememberUpdatedState(onEvent)
    // Use stableOnEvent.value in callbacks
}
```

## Testing Strategy

```kotlin
// Composable test
@Test
fun testButtonClick() {
    composeTestRule.setContent {
        CustomButton(onClick = { /* action */ }) {
            Text("Click Me")
        }
    }
    
    composeTestRule
        .onNodeWithText("Click Me")
        .performClick()
        .assertExists()
}

// Screenshot testing with Paparazzi or Roborazzi
@Test
fun testScreenAppearance() {
    paparazzi.snapshot {
        MaterialTheme {
            FeatureScreen()
        }
    }
}
```

## Accessibility

**Always Include:**
```kotlin
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.semantics {
            this.contentDescription = contentDescription
            role = Role.Button
        }
    ) {
        content()
    }
}

// Ensure sufficient color contrast
// Minimum contrast ratio: 4.5:1 for normal text, 3:1 for large text

// Support dynamic type scaling
Text(
    text = "Content",
    style = MaterialTheme.typography.bodyLarge.copy(
        fontSize = with(LocalDensity.current) {
            MaterialTheme.typography.bodyLarge.fontSize * 
            LocalConfiguration.current.fontScale
        }
    )
)
```

## Critical Reminders

1. **Avoid Generic Patterns**: Every Compose app should feel intentionally designed, not AI-generated
2. **Typography First**: Start with distinctive fonts and clear hierarchy
3. **Color with Purpose**: Create semantic color systems beyond Material defaults
4. **Animate Intentionally**: Every animation should serve UX, not just look cool
5. **Think Mobile-First**: Consider touch targets, one-handed use, landscape mode
6. **Test on Real Devices**: Emulators don't show performance issues
7. **Accessibility Matters**: Design for everyone, not just ideal conditions
8. **Stay Current**: Follow Material Design 3 updates and Compose releases

## When to Use This Skill

- Creating new Compose UI screens
- Refactoring existing Compose code
- Implementing custom components
- Building design systems
- Optimizing Compose performance
- Setting up animations and transitions

## Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Compose Samples](https://github.com/android/compose-samples)
- [Now in Android App](https://github.com/android/nowinandroid)

---

**Remember**: The goal is to create Android apps that feel polished, intentional, and distinctive. Think beyond defaults, embrace creative design choices, and build interfaces that users will remember.
