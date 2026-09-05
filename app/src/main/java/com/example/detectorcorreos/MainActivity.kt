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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import java.text.Normalizer
import kotlin.math.abs

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

private enum class MatchType { STRONG, RELATED }

private enum class AppSection(val label: String, val icon: ImageVector) {
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

private data class EmailMessage(
    val id: Int,
    val logicalSender: String,
    val technicalSender: String,
    val subject: String,
    val contentKey: String,
    val eventKey: String,
    val minute: Int,
    val moment: String
)

private data class DetectionSignal(
    val name: String,
    val points: Int,
    val maximum: Int,
    val detail: String
) {
    val matched: Boolean get() = points > 0
}

private data class DetectionCase(
    val id: String,
    val type: MatchType,
    val first: EmailMessage,
    val second: EmailMessage,
    val score: Int,
    val signals: List<DetectionSignal>,
    val explanation: String
)

private val demoEmails = listOf(
    EmailMessage(1, "Nextdoor", "reply@rs.email.es.nextdoor.com", "Me encontr\u00E9 esta cartera con dinero hay 920\u20AC en...", "nextdoor_wallet_920", "", 492, "Hoy \u00B7 08:12"),
    EmailMessage(2, "Nextdoor", "no-reply@is.email.es.nextdoor.com", "Me encontr\u00E9 esta cartera con dinero hay 920\u20AC en...", "nextdoor_wallet_920", "", 488, "Hoy \u00B7 08:08"),
    EmailMessage(3, "Telpark", "messages@telpark.com", "Telpark - Estacionamiento pr\u00F3ximo a finalizar", "parking_session_771", "parking_771", 463, "Hoy \u00B7 07:43"),
    EmailMessage(4, "Telpark", "messages@telpark.com", "Estacionamiento finalizado", "parking_session_771", "parking_771", 470, "Hoy \u00B7 07:50"),
    EmailMessage(5, "Tienda Demo", "pedidos@tienda-demo.es", "Pedido 4582 enviado", "order_4582_sent", "order_4582", 650, "Hoy \u00B7 10:50"),
    EmailMessage(6, "Tienda Demo", "pedidos@tienda-demo.es", "Pedido 4582 enviado", "order_4582_sent", "order_4582", 651, "Hoy \u00B7 10:51"),
    EmailMessage(7, "Educamos", "avisos@educamos.com", "Novedades Educamos 01/09/2026", "educamos_daily", "educamos_news", -950, "Ayer \u00B7 08:10"),
    EmailMessage(8, "Educamos", "avisos@educamos.com", "Novedades Educamos 31/08/2026", "educamos_daily", "educamos_news", -2390, "Hace 2 d\u00EDas \u00B7 08:10"),
    EmailMessage(9, "CaixaBank", "avisos@caixabank.es", "Nuevo extracto disponible", "bank_statement", "statement_sep", 540, "Hoy \u00B7 09:00"),
    EmailMessage(10, "Amazon", "shipment-tracking@amazon.es", "Tu pedido est\u00E1 en camino", "amazon_shipment", "amazon_9301", 575, "Hoy \u00B7 09:35"),
    EmailMessage(11, "Google", "no-reply@accounts.google.com", "Alerta de seguridad", "google_security", "google_login", 590, "Hoy \u00B7 09:50"),
    EmailMessage(12, "Spotify", "no-reply@spotify.com", "Novedades de tu cuenta", "spotify_account", "spotify_account", 605, "Hoy \u00B7 10:05"),
    EmailMessage(13, "OSCAR", "promociones@oscar.es", "Oferta de alquiler para septiembre", "oscar_offer_sep", "", 620, "Hoy \u00B7 10:20"),
    EmailMessage(14, "OSCAR", "promociones@oscar.es", "\u00DAltimos d\u00EDas de descuento", "oscar_discount", "", -820, "Ayer \u00B7 10:20"),
    EmailMessage(15, "AliExpress", "transaction@notice.aliexpress.com", "Tu pedido ha salido del almac\u00E9n", "aliexpress_shipping", "ali_887", 635, "Hoy \u00B7 10:35"),
    EmailMessage(16, "Guarder\u00EDa", "comunicaciones@guarderia-demo.es", "Men\u00FA semanal", "nursery_menu", "menu_week_36", 660, "Hoy \u00B7 11:00"),
    EmailMessage(17, "Seguro Hogar", "clientes@seguro-demo.es", "Renovaci\u00F3n de su p\u00F3liza", "insurance_renewal", "policy_2026", 690, "Hoy \u00B7 11:30"),
    EmailMessage(18, "Bolet\u00EDn Tecnolog\u00EDa", "newsletter@tecnologia-demo.es", "Resumen semanal de tecnolog\u00EDa", "tech_newsletter", "tech_week_36", 720, "Hoy \u00B7 12:00")
)

private object DetectionEngine {
    fun detect(emails: List<EmailMessage>): List<DetectionCase> {
        val cases = mutableListOf<DetectionCase>()
        for (leftIndex in emails.indices) {
            for (rightIndex in leftIndex + 1 until emails.size) {
                compare(emails[leftIndex], emails[rightIndex])?.let(cases::add)
            }
        }
        return cases.sortedByDescending { it.score }
    }

    private fun compare(first: EmailMessage, second: EmailMessage): DetectionCase? {
        val exactTechnicalSender = first.technicalSender.equals(second.technicalSender, true)
        val sameLogicalSender = first.logicalSender.equals(second.logicalSender, true)
        val firstSubject = normalize(first.subject)
        val secondSubject = normalize(second.subject)
        val exactSubject = firstSubject == secondSubject
        val similarSubject = !exactSubject && similarity(firstSubject, secondSubject) >= 0.45
        val sameContent = first.contentKey.isNotBlank() && first.contentKey == second.contentKey
        val sameEvent = first.eventKey.isNotBlank() && first.eventKey == second.eventKey
        val minuteDistance = abs(first.minute - second.minute)

        val signals = listOf(
            DetectionSignal(
                "Remitente",
                when { exactTechnicalSender -> 20; sameLogicalSender -> 12; else -> 0 },
                20,
                when { exactTechnicalSender -> "Misma direcci\u00F3n t\u00E9cnica"; sameLogicalSender -> "Mismo emisor l\u00F3gico con direcciones t\u00E9cnicas distintas"; else -> "Remitentes distintos" }
            ),
            DetectionSignal(
                "Asunto",
                when { exactSubject -> 30; similarSubject -> 15; else -> 0 },
                30,
                when { exactSubject -> "Asunto normalizado id\u00E9ntico"; similarSubject -> "Asuntos parcialmente similares"; else -> "Asuntos diferentes" }
            ),
            DetectionSignal("Contenido", if (sameContent) 20 else 0, 20, if (sameContent) "Mismo contenido o plantilla" else "Contenido diferente"),
            DetectionSignal("Evento o hilo", if (sameEvent) 20 else 0, 20, if (sameEvent) "Mismo evento, operaci\u00F3n o serie" else "Sin evento com\u00FAn identificado"),
            DetectionSignal(
                "Proximidad temporal",
                when { minuteDistance <= 15 -> 10; minuteDistance <= 1_440 -> 5; else -> 0 },
                10,
                when { minuteDistance <= 15 -> "Separados por $minuteDistance minutos"; minuteDistance <= 1_440 -> "Recibidos dentro de 24 horas"; else -> "Fuera de la ventana temporal" }
            )
        )

        val score = signals.sumOf { it.points }
        val type = when {
            exactTechnicalSender && exactSubject && score >= 70 -> MatchType.STRONG
            score >= 45 -> MatchType.RELATED
            else -> return null
        }
        val reasons = signals.filter { it.matched }.joinToString(", ") { it.name.lowercase() }
        return DetectionCase(
            id = "${minOf(first.id, second.id)}_${maxOf(first.id, second.id)}",
            type = type,
            first = first,
            second = second,
            score = score,
            signals = signals,
            explanation = if (type == MatchType.STRONG) {
                "Duplicado fuerte: coincide la direcci\u00F3n t\u00E9cnica, el asunto normalizado y otras se\u00F1ales ($reasons)."
            } else {
                "Correos relacionados por $reasons. Requieren revisi\u00F3n antes de tratarlos como duplicados."
            }
        )
    }

    private fun normalize(value: String): String = Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun similarity(first: String, second: String): Double {
        val a = first.split(" ").filter { it.length > 2 }.toSet()
        val b = second.split(" ").filter { it.length > 2 }.toSet()
        if (a.isEmpty() || b.isEmpty()) return 0.0
        return a.intersect(b).size.toDouble() / a.union(b).size.toDouble()
    }
}

private val detectedCases = DetectionEngine.detect(demoEmails)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { DetectorTheme { DetectorApp() } }
    }
}

@Composable
private fun DetectorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Blue900,
            onPrimary = Color.White,
            secondary = Blue700,
            background = Background,
            surface = SurfaceWhite,
            onSurface = TextPrimary
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetectorApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { context.getSharedPreferences("detector_correos", Context.MODE_PRIVATE) }
    val reviewed = remember {
        mutableStateMapOf<String, Boolean>().apply {
            detectedCases.forEachIndexed { index, case ->
                val newKey = "reviewed_case_${case.id}"
                this[case.id] = when {
                    preferences.contains(newKey) -> preferences.getBoolean(newKey, false)
                    preferences.contains("reviewed_${index + 1}") -> preferences.getBoolean("reviewed_${index + 1}", false)
                    preferences.contains("revisado_${index + 1}") -> preferences.getBoolean("revisado_${index + 1}", false)
                    else -> false
                }
            }
        }
    }
    var section by remember { mutableStateOf(AppSection.SUMMARY) }
    var filter by remember { mutableStateOf(CaseFilter.ALL) }
    var selected by remember { mutableStateOf<DetectionCase?>(null) }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFF8FBFE), Color(0xFFF2F6FB)))
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomNavigation(section) { section = it } }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Header()
                when (section) {
                    AppSection.SUMMARY -> SummaryScreen(reviewed, { selected = it }) { section = AppSection.CASES }
                    AppSection.CASES -> CasesScreen(reviewed, filter, { filter = it }) { selected = it }
                    AppSection.SETTINGS -> SettingsScreen()
                }
            }
        }
    }

    selected?.let { case ->
        DetailSheet(
            case = case,
            reviewed = reviewed[case.id] == true,
            onDismiss = { selected = null },
            onToggle = {
                val value = reviewed[case.id] != true
                reviewed[case.id] = value
                preferences.edit().putBoolean("reviewed_case_${case.id}", value).apply()
                selected = null
            }
        )
    }
}

@Composable
private fun Header() {
    Box(
        Modifier.padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 10.dp)
            .fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Blue950, Blue800, Blue600)))
    ) {
        Box(
            Modifier.align(Alignment.TopEnd).offset(x = 42.dp, y = (-42).dp).size(145.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Column(Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(Color(0xFF7FD4FF), CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text("MONITORIZACI\u00D3N ACTIVA", color = Color(0xFFD3E9F8), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                }
                Surface(color = Color.White.copy(alpha = 0.10f), shape = RoundedCornerShape(50), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))) {
                    Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(Color(0xFF73DDB5), CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text("Activo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Detector de correos", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(4.dp))
            Text("\u00DAltima revisi\u00F3n \u00B7 Hoy 08:00", color = Color(0xFFD6E9F7), fontSize = 12.sp)
        }
    }
}

@Composable
private fun SummaryScreen(reviewed: SnapshotStateMap<String, Boolean>, onCase: (DetectionCase) -> Unit, onAll: () -> Unit) {
    val strong = detectedCases.count { it.type == MatchType.STRONG }
    val related = detectedCases.count { it.type == MatchType.RELATED }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SyncCard(detectedCases.count { reviewed[it.id] != true }) }
        item { KpiRow(demoEmails.size, strong, related) }
        item { SectionHeader("Casos recientes", "Prioridad de revisi\u00F3n", "Ver todos", onAll) }
        items(detectedCases.take(3), key = { it.id }) { case ->
            CaseCard(case, reviewed[case.id] == true, true) { onCase(case) }
        }
    }
}

@Composable
private fun SyncCard(pending: Int) {
    Surface(
        Modifier.fillMaxWidth(),
        color = SurfaceWhite.copy(alpha = 0.76f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderBlue)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(pending.toString(), color = Blue950, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(4.dp))
                Text("casos requieren revisi\u00F3n prioritaria", color = TextSecondary, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).background(Color(0xFF69C7A5), CircleShape))
                Spacer(Modifier.width(5.dp))
                Text("Sincronizado", color = Success, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun CasesScreen(
    reviewed: SnapshotStateMap<String, Boolean>,
    filter: CaseFilter,
    onFilter: (CaseFilter) -> Unit,
    onCase: (DetectionCase) -> Unit
) {
    val visible = when (filter) {
        CaseFilter.ALL -> detectedCases
        CaseFilter.STRONG -> detectedCases.filter { it.type == MatchType.STRONG }
        CaseFilter.RELATED -> detectedCases.filter { it.type == MatchType.RELATED }
        CaseFilter.REVIEWED -> detectedCases.filter { reviewed[it.id] == true }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SectionHeader("Bandeja de revisi\u00F3n", "${visible.size} casos visibles") }
        item { FilterRow(filter, onFilter) }
        if (visible.isEmpty()) item { EmptyCard() }
        else items(visible, key = { it.id }) { case -> CaseCard(case, reviewed[case.id] == true, false) { onCase(case) } }
    }
}

@Composable
private fun SettingsScreen() {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        item { SectionHeader("Ajustes", "Configuraci\u00F3n del detector") }
        item { InfoCard("Versi\u00F3n", "Detector de correos v0.7 operativa. Interfaz Android creada con Jetpack Compose.") }
        item { InfoCard("Motor de detecci\u00F3n", "Compara cada pareja mediante cinco se\u00F1ales: remitente, asunto, contenido, evento o hilo y proximidad temporal.") }
        item { InfoCard("Privacidad", "Los 18 mensajes son simulados. Esta versi\u00F3n no accede a Gmail, Outlook ni a buzones corporativos.") }
        item { InfoCard("Pr\u00F3xima integraci\u00F3n", "El motor local queda preparado para sustituir los datos simulados por mensajes obtenidos mediante una conexi\u00F3n autorizada.") }
    }
}

@Composable
private fun KpiRow(analyzed: Int, strong: Int, related: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard(analyzed.toString(), "Analizados", Icons.Outlined.Email, Modifier.weight(1f))
        KpiCard(strong.toString(), "Duplicados", Icons.Outlined.ContentCopy, Modifier.weight(1f))
        KpiCard(related.toString(), "Relacionados", Icons.Outlined.SwapHoriz, Modifier.weight(1f))
    }
}

@Composable
private fun KpiCard(number: String, label: String, icon: ImageVector, modifier: Modifier) {
    Surface(modifier, color = SurfaceWhite.copy(alpha = 0.94f), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, BorderBlue), shadowElevation = 2.dp) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 13.dp)) {
            Box(Modifier.size(30.dp).background(Blue100, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Blue800, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(number, color = Blue950, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(2.dp))
            Text(label, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(title, color = Blue950, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        if (action != null && onAction != null) {
            Text(action, Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onAction).padding(5.dp), color = Blue700, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun FilterRow(selected: CaseFilter, onSelected: (CaseFilter) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        CaseFilter.entries.forEach { filter ->
            Surface(
                Modifier.clickable { onSelected(filter) },
                color = if (selected == filter) Blue900 else SurfaceWhite,
                contentColor = if (selected == filter) Color.White else Color(0xFF50647E),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, if (selected == filter) Blue800 else BorderBlue)
            ) {
                Text(filter.label, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CaseCard(case: DetectionCase, reviewed: Boolean, compact: Boolean, onClick: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = SurfaceWhite.copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderBlue),
        shadowElevation = 2.dp
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(Color(0xFF79C6E7)))
            Column(Modifier.weight(1f).padding(start = 13.dp, top = 14.dp, end = 15.dp, bottom = 14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    StatusBadge(case.type, reviewed)
                    Text("${case.score}% \u00B7 2 mensajes", color = Color(0xFF8190A2), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(8.dp))
                Text(case.first.logicalSender, color = Blue700, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(case.first.subject, color = Blue950, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (!compact) Text("\u2194 ${case.second.subject}", color = TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(case.first.moment, color = Color(0xFF8795A7), fontSize = 10.sp)
                    if (!compact) Text(if (reviewed) "Revisado" else "Ver explicaci\u00F3n", color = if (reviewed) Success else TextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(type: MatchType, reviewed: Boolean) {
    val label: String
    val foreground: Color
    val background: Color
    when {
        reviewed -> { label = "REVISADO"; foreground = Success; background = SuccessBackground }
        type == MatchType.STRONG -> { label = "DUPLICADO"; foreground = Danger; background = DangerBackground }
        else -> { label = "RELACIONADO"; foreground = Warning; background = WarningBackground }
    }
    Text(label, Modifier.background(background, RoundedCornerShape(50)).padding(horizontal = 9.dp, vertical = 5.dp), color = foreground, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.4.sp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailSheet(case: DetectionCase, reviewed: Boolean, onDismiss: () -> Unit, onToggle: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFFFBFDFF), shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp), dragHandle = null) {
        LazyColumn(
            Modifier.fillMaxWidth().heightIn(max = 680.dp),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(case.type, reviewed)
                    Text("Confianza ${case.score}/100", color = Blue700, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            item { Text(case.explanation, color = Blue950, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold) }
            item { EmailBlock("Correo 1", case.first) }
            item { EmailBlock("Correo 2", case.second) }
            item { HorizontalDivider(color = BorderBlue) }
            item { Text("Cinco se\u00F1ales analizadas", color = Blue900, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold) }
            items(case.signals) { signal -> SignalRow(signal) }
            item {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(onDismiss, Modifier.weight(1f), shape = RoundedCornerShape(13.dp), border = BorderStroke(1.dp, BorderBlue)) { Text("Cerrar") }
                    Button(onToggle, Modifier.weight(1f), shape = RoundedCornerShape(13.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue900)) {
                        Text(if (reviewed) "Marcar pendiente" else "Marcar revisado", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailBlock(title: String, email: EmailMessage) {
    Surface(Modifier.fillMaxWidth(), color = Blue100.copy(alpha = 0.65f), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, color = Blue700, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            Text(email.subject, color = Blue950, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(email.technicalSender, color = TextSecondary, fontSize = 10.sp)
            Text(email.moment, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SignalRow(signal: DetectionSignal) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text((if (signal.matched) "\u2713 " else "\u2014 ") + signal.name, color = if (signal.matched) Success else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(signal.detail, color = TextSecondary, fontSize = 10.sp)
        }
        Text("${signal.points}/${signal.maximum}", color = if (signal.matched) Blue700 else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Surface(Modifier.fillMaxWidth(), color = SurfaceWhite.copy(alpha = 0.94f), shape = RoundedCornerShape(17.dp), border = BorderStroke(1.dp, BorderBlue), shadowElevation = 2.dp) {
        Column(Modifier.padding(15.dp)) {
            Text(title, color = Blue900, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(body, color = Color(0xFF5F7188), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun EmptyCard() {
    Surface(Modifier.fillMaxWidth(), color = SurfaceWhite, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, BorderBlue)) {
        Text("No hay casos para este filtro.", Modifier.padding(22.dp), color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun BottomNavigation(current: AppSection, onSelected: (AppSection) -> Unit) {
    Column(Modifier.fillMaxWidth().background(SurfaceSoft.copy(alpha = 0.98f))) {
        HorizontalDivider(color = BorderBlue, thickness = 1.dp)
        Row(
            Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AppSection.entries.forEach { section ->
                val selected = current == section
                Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) Blue100 else Color.Transparent)
                        .clickable { onSelected(section) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        section.icon,
                        contentDescription = section.label,
                        tint = if (selected) Blue800 else Color(0xFF8A99AA),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        section.label,
                        color = if (selected) Blue800 else Color(0xFF7F8FA1),
                        fontSize = 9.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
