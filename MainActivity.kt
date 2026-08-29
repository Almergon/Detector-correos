package com.example.detectorcorreos

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

data class Caso(
    val id: String,
    val estado: String,
    val remitente: String,
    val asunto: String,
    val coincidencias: Int,
    var revisado: Boolean = false
)

class MainActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var titulo: TextView
    private lateinit var resumen: TextView
    private lateinit var btnInicio: Button
    private lateinit var btnCasos: Button
    private lateinit var btnTodos: Button
    private lateinit var btnFuertes: Button
    private lateinit var btnRelacionados: Button

    private val casos = mutableListOf(
        Caso("C001", "DUPLICADO FUERTE", "Telpark", "Estacionamiento finalizado", 2),
        Caso("C002", "DUPLICADO FUERTE", "Telpark", "Estacionamiento próximo a finalizar", 2),
        Caso("C003", "REVISAR", "Comunidad / avisos", "URGENTE", 2)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.containerCasos)
        titulo = findViewById(R.id.txtTituloSeccion)
        resumen = findViewById(R.id.txtResumen)
        btnInicio = findViewById(R.id.btnInicio)
        btnCasos = findViewById(R.id.btnCasos)
        btnTodos = findViewById(R.id.btnTodos)
        btnFuertes = findViewById(R.id.btnFuertes)
        btnRelacionados = findViewById(R.id.btnRelacionados)

        btnInicio.setOnClickListener { mostrarInicio() }
        btnCasos.setOnClickListener { mostrarCasos("TODOS") }
        btnTodos.setOnClickListener { mostrarCasos("TODOS") }
        btnFuertes.setOnClickListener { mostrarCasos("DUPLICADO FUERTE") }
        btnRelacionados.setOnClickListener { mostrarCasos("REVISAR") }

        mostrarInicio()
    }

    private fun mostrarInicio() {
        titulo.text = "Resumen"
        val fuertes = casos.count { it.estado == "DUPLICADO FUERTE" }
        val relacionados = casos.count { it.estado == "REVISAR" }
        val revisados = casos.count { it.revisado }

        resumen.visibility = View.VISIBLE
        resumen.text = """
            Correos analizados: 38
            Duplicados fuertes: $fuertes
            Relacionados: $relacionados
            Casos revisados: $revisados
        """.trimIndent()

        container.removeAllViews()
        addInfoCard(
            "Prototipo Android",
            "Esta primera APK sirve para probar la navegación táctil y el flujo de revisión. Todavía no accede directamente a Gmail."
        )
    }

    private fun mostrarCasos(filtro: String) {
        titulo.text = "Casos"
        resumen.visibility = View.GONE
        container.removeAllViews()

        val filtrados = if (filtro == "TODOS") casos else casos.filter { it.estado == filtro }

        if (filtrados.isEmpty()) {
            addInfoCard("Sin casos", "No hay resultados para este filtro.")
            return
        }

        filtrados.forEach { caso ->
            val card = layoutInflater.inflate(R.layout.item_caso, container, false)

            card.findViewById<TextView>(R.id.txtEstado).text = caso.estado
            card.findViewById<TextView>(R.id.txtAsunto).text = caso.asunto
            card.findViewById<TextView>(R.id.txtRemitente).text = caso.remitente
            card.findViewById<TextView>(R.id.txtCoincidencias).text =
                "${caso.coincidencias} coincidencias"

            val btn = card.findViewById<Button>(R.id.btnRevisado)
            btn.text = if (caso.revisado) "Revisado ✓" else "Marcar revisado"

            btn.setOnClickListener {
                caso.revisado = !caso.revisado
                btn.text = if (caso.revisado) "Revisado ✓" else "Marcar revisado"
                Toast.makeText(
                    this,
                    if (caso.revisado) "Caso marcado como revisado" else "Caso pendiente",
                    Toast.LENGTH_SHORT
                ).show()
            }

            card.setOnClickListener {
                Toast.makeText(
                    this,
                    "${caso.asunto} · ${caso.coincidencias} coincidencias",
                    Toast.LENGTH_SHORT
                ).show()
            }

            container.addView(card)
        }
    }

    private fun addInfoCard(title: String, body: String) {
        val view = layoutInflater.inflate(R.layout.item_info, container, false)
        view.findViewById<TextView>(R.id.txtInfoTitle).text = title
        view.findViewById<TextView>(R.id.txtInfoBody).text = body
        container.addView(view)
    }
}