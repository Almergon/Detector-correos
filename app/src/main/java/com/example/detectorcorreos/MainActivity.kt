package com.example.detectorcorreos

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFFF4F8FC)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val SurfaceSoft = Color(0xFFF8FBFF)
private val TextPrimary = Color(0xFF10233F)
private val TextSecondary = Color(0xFF6F8097)
private val BorderBlue = Color(0xFFDCE6F1)

private val Blue950 = Color(0xFF0B2341)
private val Blue900 = Color(0xFF12375F)
private val Blue800 = Color(0xFF174B7A)
private val Blue700 = Color(0xFF1F639C)
private val Blue600 = Color(0xFF2C7BB8)
private val Blue500 = Color(0xFF4397CB)
private val Blue100 = Color(0xFFEAF4FB)

private val Danger = Color(0xFFAD4258)
private val DangerBackground = Color(0xFFFBECEF)
private val Warning = Color(0xFFB17B27)
private val WarningBackground = Color(0xFFFFF5DF)
private val Success = Color(0xFF2F7D65)
private val SuccessBackground = Color(0xFFEAF6F1)

private enum class CaseType {
    STRONG,
    RELATED
}

private enum class AppSection(
    val label: String,
    val icon: ImageVector
) {
    SUMMARY("Resumen", Icons.Outlined.Home),
    CASES("Casos", Icons.Outlined.Email),
    SETTINGS("Ajustes", Icons.Outlined.Settings)
}

private enum class CaseFilter(val label: String) {
    ALL("Todos"),
    STRONG("Duplicados"),
    RELATED("Relacionados"),
    REVIEWED("Revisados")
}

private data class MailCase(
    val id: Int,
    val type: CaseType,
    val sender: String,
    val subject: String,
    val detail: String,
    val moment: String,
    val matches: Int
)

private val demoCases = listOf(
    MailCase(
        id = 1,
        type = CaseType.STRONG,
        sender = "Telpark",
        subject = "Estacionamiento finalizado",
        detail = "Se han detectado correos con el mismo remitente y el mismo asunto. Conviene revisar si ya se respondió o si son duplicados del mismo caso.",
        moment = "Hoy · 08:12",
        matches = 2
    ),
    MailCase(
        id = 2,
        type = CaseType.STRONG,
        sender = "Telpark",
        subject = "Estacionamiento próximo a finalizar",
        detail = "Mensajes muy similares detectados dentro del periodo analizado. Posible duplicidad de aviso automático.",
        moment = "Hoy · 07:43",
        matches = 2
    ),
    MailCase(
        id = 3,
        type = CaseType.RELATED,
        sender = "Comunidad / avisos",
        subject = "URGENTE !!!",
        detail = "Asunto repetido. Puede pertenecer al mismo hilo o a una gestión muy parecida. Revisión manual recomendada.",
        moment = "Ayer · 18:31",
        matches = 2
    ),
    MailCase(
        id = 4,
        type = CaseType.RELATED,
        sender = "Educamos",
        subject = "Nueva comunicación",
        detail = "Caso relacionado por asunto o hilo. Revisar si existe ya una respuesta previa o un caso asociado.",
        moment = "Ayer · 16:05",
        matches = 2
    )
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DetectorTheme {
                DetectorApp()
            }
        }
    }
}

@Composable
private fun DetectorTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = Blue900,
        onPrimary = Color.White,
        secondary = Blue700,
        background = Background,
        surface = SurfaceWhite,
        onSurface = TextPrimary
    )

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetectorApp() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "detector_correos",
            Context.MODE_PRIVATE
        )
    }

    val reviewedCases = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            demoCases.forEach { case ->
                val currentKey = "reviewed_${case.id}"
                val oldKey = "revisado_${case.id}"

                this[case.id] = when {
                    preferences.contains(currentKey) ->
                        preferences.getBoolean(currentKey, false)

                    preferences.contains(oldKey) ->
                        preferences.getBoolean(oldKey, false)

                    else -> false
                }
            }
        }
    }

    var currentSection by remember {
        mutableStateOf(AppSection.SUMMARY)
    }

    var currentFilter by remember {
        mutableStateOf(CaseFilter.ALL)
    }

    var selectedCase by remember {
        mutableStateOf<MailCase?>(null)
    }

    fun changeReviewedState(case: MailCase) {
        val newValue = !(reviewedCases[case.id] ?: false)

        reviewedCases[case.id] = newValue

        // Conservamos las dos claves para migrar versiones anteriores.
        preferences.edit()
            .putBoolean("reviewed_${case.id}", newValue)
            .putBoolean("revisado_${case.id}", newValue)
            .apply()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FBFE),
                        Color(0xFFF2F6FB)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                DetectorBottomNavigation(
                    currentSection = currentSection,
                    onSectionSelected = {
                        currentSection = it
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                DetectorHeader()

                when (currentSection) {
                    AppSection.SUMMARY -> SummaryScreen(
                        reviewedCases = reviewedCases,
                        onCaseSelected = { selectedCase = it },
                        onViewAll = { currentSection = AppSection.CASES }
                    )

                    AppSection.CASES -> CasesScreen(
                        reviewedCases = reviewedCases,
                        currentFilter = currentFilter,
                        onFilterSelected = { currentFilter = it },
                        onCaseSelected = { selectedCase = it }
                    )

                    AppSection.SETTINGS -> SettingsScreen()
                }
            }
        }
    }

    selectedCase?.let { case ->
        CaseDetailSheet(
            case = case,
            reviewed = reviewedCases[case.id] ?: false,
            onDismiss = { selectedCase = null },
            onToggleReviewed = {
                changeReviewedState(case)
                selectedCase = null
            }
        )
    }
}

@Composable
private fun DetectorHeader() {
    Box(
        modifier = Modifier
            .padding(
                start = 18.dp,
                top = 14.dp,
                end = 18.dp,
                bottom = 10.dp
            )
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Blue950,
                        Blue800,
                        Blue600
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 42.dp, y = (-42).dp)
                .size(145.dp)
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 17.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(
                            color = Color(0xFF7FD4FF),
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "MONITORIZACIÓN ACTIVA",
                    color = Color(0xFFC8E4F6),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Detector de correos",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Última revisión · Hoy 08:00",
                color = Color(0xFFD6E9F7),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SummaryScreen(
    reviewedCases: SnapshotStateMap<Int, Boolean>,
    onCaseSelected: (MailCase) -> Unit,
    onViewAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 22.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            KpiRow()
        }

        item {
            Spacer(modifier = Modifier.height(3.dp))

            SectionHeader(
                title = "Casos recientes",
                subtitle = "Prioridad de revisión",
                actionText = "Ver todos",
                onAction = onViewAll
            )
        }

        items(
            items = demoCases.take(3),
            key = { it.id }
        ) { case ->
            CaseCard(
                case = case,
                reviewed = reviewedCases[case.id] ?: false,
                compact = true,
                onClick = { onCaseSelected(case) }
            )
        }
    }
}

@Composable
private fun CasesScreen(
    reviewedCases: SnapshotStateMap<Int, Boolean>,
    currentFilter: CaseFilter,
    onFilterSelected: (CaseFilter) -> Unit,
    onCaseSelected: (MailCase) -> Unit
) {
    val filteredCases = when (currentFilter) {
        CaseFilter.ALL -> demoCases

        CaseFilter.STRONG ->
            demoCases.filter { it.type == CaseType.STRONG }

        CaseFilter.RELATED ->
            demoCases.filter { it.type == CaseType.RELATED }

        CaseFilter.REVIEWED ->
            demoCases.filter { reviewedCases[it.id] == true }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 22.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SectionHeader(
                title = "Bandeja de revisión",
                subtitle = "${filteredCases.size} casos visibles"
            )
        }

        item {
            FilterRow(
                selectedFilter = currentFilter,
                onFilterSelected = onFilterSelected
            )
        }

        if (filteredCases.isEmpty()) {
            item {
                EmptyCard()
            }
        } else {
            items(
                items = filteredCases,
                key = { it.id }
            ) { case ->
                CaseCard(
                    case = case,
                    reviewed = reviewedCases[case.id] ?: false,
                    compact = false,
                    onClick = { onCaseSelected(case) }
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp,
            end = 18.dp,
            bottom = 22.dp
        ),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        item {
            SectionHeader(
                title = "Ajustes",
                subtitle = "Configuración del detector"
            )
        }

        item {
            InformationCard(
                title = "Versión actual",
                body = "Detector de correos v0.5. Interfaz desarrollada con Jetpack Compose."
            )
        }

        item {
            InformationCard(
                title = "Origen de datos",
                body = "Esta versión utiliza datos simulados. La conexión con Gmail y la adaptación a Outlook/Exchange se incorporarán más adelante."
            )
        }

        item {
            InformationCard(
                title = "Estado de la detección",
                body = "Los filtros, el detalle de casos y el estado revisado funcionan localmente en el dispositivo."
            )
        }
    }
}

@Composable
private fun KpiRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiCard(
            number = "38",
            label = "Analizados",
            modifier = Modifier.weight(1f)
        )

        KpiCard(
            number = "4",
            label = "Duplicados",
            modifier = Modifier.weight(1f)
        )

        KpiCard(
            number = "6",
            label = "Relacionados",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun KpiCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SurfaceWhite.copy(alpha = 0.92f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderBlue),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 12.dp
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number,
                color = Blue900,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                color = TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                color = Blue950,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 5.dp, vertical = 5.dp),
                color = Blue700,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun FilterRow(
    selectedFilter: CaseFilter,
    onFilterSelected: (CaseFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        CaseFilter.entries.forEach { filter ->
            FilterPill(
                label = filter.label,
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (selected) Blue900 else SurfaceWhite.copy(alpha = 0.9f),
        contentColor = if (selected) Color.White else Color(0xFF50647E),
        shape = RoundedCornerShape(50),
        border = if (selected) {
            BorderStroke(1.dp, Blue800)
        } else {
            BorderStroke(1.dp, BorderBlue)
        },
        shadowElevation = if (selected) 3.dp else 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CaseCard(
    case: MailCase,
    reviewed: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = SurfaceWhite.copy(alpha = 0.95f),
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, BorderBlue),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(Blue500.copy(alpha = 0.7f))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = 12.dp,
                        top = 12.dp,
                        end = 14.dp,
                        bottom = 12.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusBadge(
                        case = case,
                        reviewed = reviewed
                    )

                    Text(
                        text = "${case.matches} coincidencias",
                        color = Color(0xFF8492A5),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = case.sender,
                    color = Blue700,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = case.subject,
                    color = Blue950,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(7.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = case.moment,
                        color = Color(0xFF8795A7),
                        fontSize = 10.sp
                    )

                    if (!compact) {
                        Text(
                            text = if (reviewed) "Revisado" else "Abrir detalle",
                            color = if (reviewed) Success else TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    case: MailCase,
    reviewed: Boolean
) {
    val label: String
    val foreground: Color
    val background: Color

    when {
        reviewed -> {
            label = "REVISADO"
            foreground = Success
            background = SuccessBackground
        }

        case.type == CaseType.STRONG -> {
            label = "DUPLICADO"
            foreground = Danger
            background = DangerBackground
        }

        else -> {
            label = "RELACIONADO"
            foreground = Warning
            background = WarningBackground
        }
    }

    Text(
        text = label,
        modifier = Modifier
            .background(
                color = background,
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = 9.dp,
                vertical = 5.dp
            ),
        color = foreground,
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.4.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaseDetailSheet(
    case: MailCase,
    reviewed: Boolean,
    onDismiss: () -> Unit,
    onToggleReviewed: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFBFDFF),
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 26.dp
        ),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp)
                .padding(
                    start = 20.dp,
                    top = 22.dp,
                    end = 20.dp,
                    bottom = 24.dp
                )
        ) {
            StatusBadge(
                case = case,
                reviewed = reviewed
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = case.subject,
                color = Blue950,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            DetailLine(
                label = "Remitente",
                value = case.sender
            )

            DetailLine(
                label = "Fecha",
                value = case.moment
            )

            DetailLine(
                label = "Coincidencias detectadas",
                value = case.matches.toString()
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = BorderBlue)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = case.detail,
                color = Color(0xFF33475F),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, BorderBlue)
                ) {
                    Text("Cerrar")
                }

                Button(
                    onClick = onToggleReviewed,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue900,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (reviewed) {
                            "Marcar pendiente"
                        } else {
                            "Marcar revisado"
                        },
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String
) {
    Text(
        text = "$label: $value",
        color = TextSecondary,
        fontSize = 12.sp,
        lineHeight = 19.sp
    )
}

@Composable
private fun InformationCard(
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceWhite.copy(alpha = 0.94f),
        shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, BorderBlue),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                text = title,
                color = Blue900,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = body,
                color = Color(0xFF5F7188),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun EmptyCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceWhite.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderBlue)
    ) {
        Text(
            text = "No hay casos para este filtro.",
            modifier = Modifier.padding(22.dp),
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DetectorBottomNavigation(
    currentSection: AppSection,
    onSectionSelected: (AppSection) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceSoft.copy(alpha = 0.98f),
        tonalElevation = 6.dp
    ) {
        AppSection.entries.forEach { section ->
            val selected = currentSection == section

            NavigationBarItem(
                selected = selected,
                onClick = { onSectionSelected(section) },
                icon = {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = section.label
                    )
                },
                label = {
                    Text(
                        text = section.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Blue800,
                    selectedTextColor = Blue800,
                    indicatorColor = Blue100,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}
