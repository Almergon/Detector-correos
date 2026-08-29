package com.example.detectorcorreos

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Detector de correos"
            textSize = 24f
        }

        val summary = TextView(this).apply {
            text = """
                Correos analizados: 38
                Duplicados fuertes: 4
                Relacionados: 6
            """.trimIndent()
            textSize = 18f
        }

        val case1 = TextView(this).apply {
            text = "DUPLICADO FUERTE · Telpark"
            textSize = 17f
        }

        val case2 = TextView(this).apply {
            text = "REVISAR · URGENTE"
            textSize = 17f
        }

        layout.addView(title)
        layout.addView(summary)
        layout.addView(case1)
        layout.addView(case2)

        setContentView(layout)
    }
}
