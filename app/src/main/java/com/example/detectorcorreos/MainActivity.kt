package com.example.detectorcorreos

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

data class CasoCorreo(
    val id: Int,
    val tipo: String,
    val remitente: String,
    val asunto: String,
    val detalle: String
)

class MainActivity : AppCompatActivity() {

    private lateinit var listaContainer: LinearLayout
    private val prefs by lazy {
        getSharedPreferences("detector_correos", MODE_PRIVATE)
    }

    private val casos = listOf(
        CasoCorreo(
            1,
            "FUERTE",
            "Telpark",
            "Estacionamiento finalizado",
            "Se han detectado varios correos con el mismo remitente y asunto."
        ),
        CasoCorreo(
            2,
            "FUERTE",
            "Telpark",
            "Estacionamiento próximo a finalizar",
            "Se han detectado varios mensajes muy similares dentro del periodo analizado."
        ),
        CasoCorreo(
            3,
            "RELACIONADO",
            "Remitente de prueba",
            "URGENTE !!!",
            "El asunto se repite y conviene revisar si pertenece al mismo caso."
        ),
        CasoCorreo(
            4,
            "RELACIONADO",
            "Educamos",
            "Nueva comunicación",
            "Correos relacionados por asunto o hilo."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 24)
        }

        val titulo = TextView(this).apply {
            text = "Detector de correos"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
        }

        val resumen = TextView(this).apply {
            text = "Correos analizados: 38\nDuplicados fuertes: 4\nRelacionados: 6"
            textSize = 18f
            setPadding(0, 12, 0, 24)
        }

        val filtros = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        fun botonFiltro(texto: String, filtro: String): Button {
            return Button(this).apply {
                text = texto
                setOnClickListener { mostrarCasos(filtro) }
            }
        }

        filtros.addView(
            botonFiltro("Todos", "TODOS"),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        filtros.addView(
            botonFiltro("Fuertes", "FUERTE"),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        filtros.addView(
            botonFiltro("Relacionados", "RELACIONADO"),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )

        val scroll = ScrollView(this)

        listaContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 20, 0, 20)
        }

        scroll.addView(listaContainer)

        root.addView(titulo)
        root.addView(resumen)
        root.addView(filtros)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        mostrarCasos("TODOS")
    }

    private fun mostrarCasos(filtro: String) {
        listaContainer.removeAllViews()

        val filtrados = if (filtro == "TODOS") {
            casos
        } else {
            casos.filter { it.tipo == filtro }
        }

        filtrados.forEach { caso ->
            listaContainer.addView(crearTarjeta(caso))
        }

        if (filtrados.isEmpty()) {
            listaContainer.addView(TextView(this).apply {
                text = "No hay casos para este filtro."
                textSize = 17f
                setPadding(8, 24, 8, 24)
            })
        }
    }

    private fun crearTarjeta(caso: CasoCorreo): View {
        val revisado = prefs.getBoolean("revisado_${caso.id}", false)

        val tarjeta = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 22, 24, 22)
            setBackgroundColor(0xFFF3F3F3.toInt())
        }

        val estado = TextView(this).apply {
            text = if (revisado) "✓ REVISADO" else caso.tipo
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
        }

        val asunto = TextView(this).apply {
            text = caso.asunto
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 5, 0, 2)
        }

        val remitente = TextView(this).apply {
            text = "Remitente: ${caso.remitente}"
            textSize = 16f
        }

        val detalle = TextView(this).apply {
            text = caso.detalle
            textSize = 16f
            visibility = View.GONE
            setPadding(0, 14, 0, 8)
        }

        val botonDetalle = Button(this).apply {
            text = "Ver detalle"
            setOnClickListener {
                if (detalle.visibility == View.GONE) {
                    detalle.visibility = View.VISIBLE
                    text = "Ocultar detalle"
                } else {
                    detalle.visibility = View.GONE
                    text = "Ver detalle"
                }
            }
        }

        val botonRevisado = Button(this).apply {
            text = if (revisado) "Marcar pendiente" else "Marcar revisado"
            setOnClickListener {
                val nuevoEstado = !prefs.getBoolean("revisado_${caso.id}", false)
                prefs.edit().putBoolean("revisado_${caso.id}", nuevoEstado).apply()

                estado.text = if (nuevoEstado) "✓ REVISADO" else caso.tipo
                text = if (nuevoEstado) "Marcar pendiente" else "Marcar revisado"
            }
        }

        tarjeta.addView(estado)
        tarjeta.addView(asunto)
        tarjeta.addView(remitente)
        tarjeta.addView(detalle)
        tarjeta.addView(botonDetalle)
        tarjeta.addView(botonRevisado)

        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 18)
            addView(tarjeta)
        }

        return wrapper
    }
}
