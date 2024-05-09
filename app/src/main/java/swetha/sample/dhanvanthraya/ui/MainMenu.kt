package swetha.sample.dhanvanthraya.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import swetha.sample.ayushokya.R

class MainMenu : AppCompatActivity() {

    private lateinit var chatbot: LinearLayout
    private lateinit var shopsnearby: LinearLayout
    private lateinit var prakrutibtn: LinearLayout
    private lateinit var medicationhisbtn: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        chatbot= findViewById(R.id.chatbot)
        shopsnearby= findViewById(R.id.shopsnearby)
        prakrutibtn= findViewById(R.id.prakrutibtn)
        medicationhisbtn= findViewById(R.id.medicationhisbtn)

        chatbot.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        shopsnearby.setOnClickListener{
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        prakrutibtn.setOnClickListener{
            val intent = Intent(this, PrakrutiAssessment::class.java)
            startActivity(intent)
        }
        medicationhisbtn.setOnClickListener{
            val intent = Intent(this, MedicationHistory::class.java)
            startActivity(intent)
        }

    }
}