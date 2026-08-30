package com.example.detectorcorreos

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

data class CasoCorreo(
    val id: Int,
    val tipo: String,
    val remitente: String,
    val asunto: String,
    val detalle: String,
    val momento: String,
    val coincidencias: Int
)

class MainActivity : AppCompatActivity() {

    private lateinit var mainRoot: LinearLayout
    private lateinit var contentContainer: LinearLayout
    private lateinit var navResumen: Button
    private lateinit var navCasos: Button
    private lateinit var navAjustes: Button

    private lateinit var filtroTodos: Button
    private lateinit var filtroFuertes: Button
    private lateinit var filtroRelacionados: Button
    private lateinit var filtroRevisados: Button

    private var filtroActual = "TODOS"
    private var seccionActual = "RESUMEN"

    private val prefs by lazy {
        getSharedPreferences("detector_correos", MODE_PRIVATE)
    }

    private val casos = listOf(
        CasoCorreo(
            1,
            "FUERTE",
            "Telpark",
            "Estacionamiento finalizado",
            "Se han detectado correos con el mismo remitente y el mismo asunto. Conviene revisar si ya se respondió o si son duplicados del mismo caso.",
            "Hoy · 08:12",
            2
        ),
        CasoCorreo(
            2,
            "FUERTE",
            "Telpark",
            "Estacionamiento próximo a finalizar",
            "Mensajes muy similares detectados dentro del periodo analizado. Posible duplicidad de aviso automático.",
            "Hoy · 07:43",
            2
        ),
        CasoCorreo(
            3,
            "RELACIONADO",
            "Comunidad / avisos",
            "URGENTE !!!",
            "Asunto repetido. Puede pertenecer al mismo hilo o a una gestión muy parecida. Revisión manual recomendada.",
            "Ayer · 18:31",
            2
        ),
        CasoCorreo(
            4,
            "RELACIONADO",
            "Educamos",
            "Nueva comunicación",
            "Caso relacionado por asunto/hilo. Revisar si existe ya una respuesta previa o un caso asociado.",
            "Ayer · 16:05",
            2
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainRoot = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#F4F6F8"))
        }

        val contentScroll = ScrollView(this).apply {
            isFillViewport = true
        }

        contentContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(100))
        }

        contentScroll.addView(contentContainer)

        mainRoot.addView(
            contentScroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        mainRoot.addView(createBottomNav())
        setContentView(mainRoot)

        renderScreen()
    }

    private fun renderScreen() {
        contentContainer.removeAllViews()
        if (seccionActual == "RESUMEN") {
            renderResumen()
        } else if (seccionActual == "CASOS") {
            renderCasos()
        } else {
            renderAjustes()
        }
        updateBottomNav()
    }

    private fun renderResumen() {
        contentContainer.addView(createHeader())

        contentContainer.addView(space(10))

        val kpiRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        kpiRow.addView(createKpiCard("38", "Analizados"), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = dp(6)
        })
        kpiRow.addView(createKpiCard("4", "Duplicados"), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = dp(3)
            marginEnd = dp(3)
        })
        kpiRow.addView(createKpiCard("6", "Relacionados"), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = dp(6)
        })

        contentContainer.addView(kpiRow)
        contentContainer.addView(space(18))
        contentContainer.addView(sectionTitle("Casos recientes"))

        getFilteredCases("TODOS").take(3).forEach {
            contentContainer.addView(createCaseCard(it, compact = true))
        }
    }

    private fun renderCasos() {
        contentContainer.addView(createHeader())
        contentContainer.addView(space(10))
        contentContainer.addView(sectionTitle("Bandeja de revisión"))
        contentContainer.addView(space(8))
        contentContainer.addView(createFilters())

        val filtered = getFilteredCases(filtroActual)

        if (filtered.isEmpty()) {
            contentContainer.addView(space(14))
            contentContainer.addView(createEmptyCard("No hay casos para este filtro."))
            return
        }

        contentContainer.addView(space(8))
        filtered.forEach {
            contentContainer.addView(createCaseCard(it, compact = false))
        }
    }

    private fun renderAjustes() {
        contentContainer.addView(createHeader())
        contentContainer.addView(space(10))
        contentContainer.addView(sectionTitle("Ajustes"))
        contentContainer.addView(space(10))

        contentContainer.addView(createInfoCard(
            "Estado actual",
            "Esta versión es una demo visual avanzada. Todavía trabaja con datos de prueba y sirve para pulir la interfaz antes de conectar Gmail."
        ))

        contentContainer.addView(space(10))

        contentContainer.addView(createInfoCard(
            "Siguiente paso",
            "Conectar datos reales del correo, mantener la misma interfaz y añadir última revisión, sincronización y detección automática diaria."
        ))
    }

    private fun createHeader(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedDrawable("#9B1C1C", 20f)
            setPadding(dp(18), dp(18), dp(18), dp(18))

            addView(TextView(this@MainActivity).apply {
                text = "Detector de correos"
                textSize = 24f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = "Última revisión · Hoy 08:00"
                textSize = 13f
                setTextColor(Color.parseColor("#F9DADA"))
                setPadding(0, dp(6), 0, 0)
            })
        }
    }

    private fun createKpiCard(number: String, label: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = roundedDrawable("#FFFFFF", 18f, "#E3E7EB", 1)
            setPadding(dp(8), dp(16), dp(8), dp(16))

            addView(TextView(this@MainActivity).apply {
                text = number
                textSize = 24f
                setTextColor(Color.parseColor("#1F2937"))
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            })

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 12f
                setTextColor(Color.parseColor("#6B7280"))
                gravity = Gravity.CENTER
            })
        }
    }

    private fun createFilters(): HorizontalScrollView {
        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        filtroTodos = createFilterButton("Todos") { setFilter("TODOS") }
        filtroFuertes = createFilterButton("Duplicados") { setFilter("FUERTE") }
        filtroRelacionados = createFilterButton("Relacionados") { setFilter("RELACIONADO") }
        filtroRevisados = createFilterButton("Revisados") { setFilter("REVISADOS") }

        row.addView(filtroTodos)
        row.addView(filtroFuertes)
        row.addView(filtroRelacionados)
        row.addView(filtroRevisados)

        scroll.addView(row)
        updateFilterButtons()
        return scroll
    }

    private fun createFilterButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { onClick() }
            textSize = 12f
            isAllCaps = false
            setPadding(dp(14), dp(10), dp(14), dp(10))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.marginEnd = dp(8)
            layoutParams = lp
        }
    }

    private fun setFilter(filter: String) {
        filtroActual = filter
        updateFilterButtons()
        renderScreen()
    }

    private fun updateFilterButtons() {
        if (!::filtroTodos.isInitialized) return

        styleFilterButton(filtroTodos, filtroActual == "TODOS")
        styleFilterButton(filtroFuertes, filtroActual == "FUERTE")
        styleFilterButton(filtroRelacionados, filtroActual == "RELACIONADO")
        styleFilterButton(filtroRevisados, filtroActual == "REVISADOS")
    }

    private fun styleFilterButton(button: Button, active: Boolean) {
        button.background = if (active) {
            roundedDrawable("#9B1C1C", 999f)
        } else {
            roundedDrawable("#FFFFFF", 999f, "#D7DCE2", 1)
        }
        button.setTextColor(if (active) Color.WHITE else Color.parseColor("#374151"))
    }

    private fun createCaseCard(caso: CasoCorreo, compact: Boolean): LinearLayout {
        val revisado = isReviewed(caso.id)

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedDrawable("#FFFFFF", 18f, "#E3E7EB", 1)
            setPadding(dp(16), dp(16), dp(16), dp(16))

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(10)
            layoutParams = lp

            val topRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            topRow.addView(createBadge(caso, revisado))

            topRow.addView(TextView(this@MainActivity).apply {
                text = "${caso.coincidencias}x"
                textSize = 12f
                setTextColor(Color.parseColor("#6B7280"))
                gravity = Gravity.END
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

            addView(topRow)

            addView(TextView(this@MainActivity).apply {
                text = caso.remitente
                textSize = 14f
                setTextColor(Color.parseColor("#6B7280"))
                setPadding(0, dp(10), 0, 0)
            })

            addView(TextView(this@MainActivity).apply {
                text = caso.asunto
                textSize = 20f
                setTextColor(Color.parseColor("#111827"))
                setTypeface(null, Typeface.BOLD)
                setPadding(0, dp(2), 0, 0)
            })

            addView(TextView(this@MainActivity).apply {
                text = caso.momento
                textSize = 12f
                setTextColor(Color.parseColor("#8A94A6"))
                setPadding(0, dp(6), 0, 0)
            })

            if (!compact) {
                addView(TextView(this@MainActivity).apply {
                    text = if (revisado) "Marcado como revisado" else "Toca para ver detalle"
                    textSize = 13f
                    setTextColor(if (revisado) Color.parseColor("#1E7A45") else Color.parseColor("#6B7280"))
                    setPadding(0, dp(12), 0, 0)
                })
            }

            setOnClickListener {
                showDetailDialog(caso)
            }
        }
    }

    private fun createBadge(caso: CasoCorreo, revisado: Boolean): TextView {
        return TextView(this).apply {
            text = when {
                revisado -> "REVISADO"
                caso.tipo == "FUERTE" -> "DUPLICADO"
                else -> "RELACIONADO"
            }
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(10), dp(6), dp(10), dp(6))
            background = when {
                revisado -> roundedDrawable("#E8F6EE", 999f)
                caso.tipo == "FUERTE" -> roundedDrawable("#FDE8E8", 999f)
                else -> roundedDrawable("#FEF3D6", 999f)
            }
            setTextColor(
                when {
                    revisado -> Color.parseColor("#1E7A45")
                    caso.tipo == "FUERTE" -> Color.parseColor("#B42318")
                    else -> Color.parseColor("#B26A00")
                }
            )
        }
    }

    private fun showDetailDialog(caso: CasoCorreo) {
        val revisado = isReviewed(caso.id)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(4))
        }

        layout.addView(TextView(this).apply {
            text = caso.asunto
            textSize = 21f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#111827"))
        })

        layout.addView(TextView(this).apply {
            text = "Remitente: ${caso.remitente}"
            textSize = 14f
            setTextColor(Color.parseColor("#6B7280"))
            setPadding(0, dp(8), 0, 0)
        })

        layout.addView(TextView(this).apply {
            text = "Fecha: ${caso.momento}"
            textSize = 14f
            setTextColor(Color.parseColor("#6B7280"))
            setPadding(0, dp(4), 0, 0)
        })

        layout.addView(TextView(this).apply {
            text = "Coincidencias detectadas: ${caso.coincidencias}"
            textSize = 14f
            setTextColor(Color.parseColor("#6B7280"))
            setPadding(0, dp(4), 0, 0)
        })

        layout.addView(TextView(this).apply {
            text = caso.detalle
            textSize = 15f
            setTextColor(Color.parseColor("#1F2937"))
            setPadding(0, dp(16), 0, dp(4))
        })

        AlertDialog.Builder(this)
            .setTitle(if (caso.tipo == "FUERTE") "Detalle de duplicado" else "Detalle relacionado")
            .setView(layout)
            .setPositiveButton(if (revisado) "Marcar pendiente" else "Marcar revisado") { _, _ ->
                setReviewed(caso.id, !revisado)
                renderScreen()
                Toast.makeText(
                    this,
                    if (!revisado) "Caso marcado como revisado" else "Caso marcado como pendiente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun createInfoCard(title: String, body: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedDrawable("#FFFFFF", 18f, "#E3E7EB", 1)
            setPadding(dp(16), dp(16), dp(16), dp(16))

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 17f
                setTextColor(Color.parseColor("#111827"))
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = body
                textSize = 14f
                setTextColor(Color.parseColor("#4B5563"))
                setPadding(0, dp(8), 0, 0)
            })
        }
    }

    private fun createEmptyCard(text: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = roundedDrawable("#FFFFFF", 18f, "#E3E7EB", 1)
            setPadding(dp(16), dp(24), dp(16), dp(24))

            addView(TextView(this@MainActivity).apply {
                this.text = text
                textSize = 15f
                setTextColor(Color.parseColor("#6B7280"))
                gravity = Gravity.CENTER
            })
        }
    }

    private fun sectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            setTextColor(Color.parseColor("#111827"))
            setTypeface(null, Typeface.BOLD)
        }
    }

    private fun createBottomNav(): LinearLayout {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(10), dp(10), dp(10), dp(14))
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        navResumen = createNavButton("Resumen") {
            seccionActual = "RESUMEN"
            renderScreen()
        }

        navCasos = createNavButton("Casos") {
            seccionActual = "CASOS"
            renderScreen()
        }

        navAjustes = createNavButton("Ajustes") {
            seccionActual = "AJUSTES"
            renderScreen()
        }

        nav.addView(navResumen, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        nav.addView(navCasos, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        nav.addView(navAjustes, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        return nav
    }

    private fun createNavButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 12f
            isAllCaps = false
            setOnClickListener { onClick() }
            setPadding(dp(8), dp(10), dp(8), dp(10))
        }
    }

    private fun updateBottomNav() {
        styleNavButton(navResumen, seccionActual == "RESUMEN")
        styleNavButton(navCasos, seccionActual == "CASOS")
        styleNavButton(navAjustes, seccionActual == "AJUSTES")
    }

    private fun styleNavButton(button: Button, active: Boolean) {
        button.background = if (active) {
            roundedDrawable("#FDE8E8", 999f)
        } else {
            roundedDrawable("#FFFFFF", 999f)
        }
        button.setTextColor(if (active) Color.parseColor("#9B1C1C") else Color.parseColor("#6B7280"))
    }

    private fun getFilteredCases(filtro: String): List<CasoCorreo> {
        return when (filtro) {
            "FUERTE" -> casos.filter { it.tipo == "FUERTE" }
            "RELACIONADO" -> casos.filter { it.tipo == "RELACIONADO" }
            "REVISADOS" -> casos.filter { isReviewed(it.id) }
            else -> casos
        }
    }

    private fun isReviewed(id: Int): Boolean {
        return prefs.getBoolean("reviewed_$id", false)
    }

    private fun setReviewed(id: Int, value: Boolean) {
        prefs.edit().putBoolean("reviewed_$id", value).apply()
    }

    private fun roundedDrawable(
        fillColor: String,
        radiusDp: Float,
        strokeColor: String? = null,
        strokeWidthDp: Int = 0
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp.toInt()).toFloat()
            setColor(Color.parseColor(fillColor))
            if (strokeColor != null && strokeWidthDp > 0) {
                setStroke(dp(strokeWidthDp), Color.parseColor(strokeColor))
            }
        }
    }

    private fun space(heightDp: Int): TextView {
        return TextView(this).apply {
            height = dp(heightDp)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
