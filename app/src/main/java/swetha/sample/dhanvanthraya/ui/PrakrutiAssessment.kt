package swetha.sample.dhanvanthraya.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.DetectIntentRequest
import com.google.cloud.dialogflow.v2.DetectIntentResponse
import com.google.cloud.dialogflow.v2.QueryInput
import com.google.cloud.dialogflow.v2.SessionName
import com.google.cloud.dialogflow.v2.SessionsClient
import com.google.cloud.dialogflow.v2.SessionsSettings
import com.google.cloud.dialogflow.v2.TextInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import swetha.sample.ayushokya.R
import swetha.sample.dhanvanthraya.models.Message
import java.util.ArrayList
import java.util.UUID

class PrakrutiAssessment : AppCompatActivity() {

    private var messageList: ArrayList<Message> = ArrayList()

    //dialogFlow
    private var sessionsClient: SessionsClient? = null
    private var sessionName: SessionName? = null
    private val uuid = UUID.randomUUID().toString()
    private val TAG = "mainactivity"
    private lateinit var chatAdapter: MessagingAdapter
    private lateinit var pachatView: RecyclerView
    private lateinit var paeditMessage: EditText
    private lateinit var pabtnSend: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.prakruti_assessment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val messageTextView = findViewById<TextView>(R.id.messageTextView)
        val editText = findViewById<EditText>(R.id.paeditMessage)
        // Initialize views
        pachatView = findViewById(R.id.pachatView)
        pabtnSend = findViewById(R.id.pabtnSend)
        paeditMessage= findViewById(R.id.paeditMessage)
        // Listen for changes in the EditText
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Hide the message when the user starts typing
                messageTextView.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        })



        // Setting adapter to recyclerview
        chatAdapter = MessagingAdapter(this, messageList)
        pachatView.adapter = chatAdapter

        // Set layout manager to RecyclerView
        pachatView.layoutManager = LinearLayoutManager(this)

        //onclick listener to update the list and call dialogflow
        pabtnSend.setOnClickListener {
            val message: String = paeditMessage.text.toString()
            if (message.isNotEmpty()) {
                addMessageToList(message, false)
                sendMessageToBot(message)
            } else {
                Toast.makeText(this@PrakrutiAssessment, "Please enter text!", Toast.LENGTH_SHORT).show()
            }
        }

        //initialize bot config
        setUpBot()
    }


    private fun determinePrakruti(userResponses: List<String>): String {
        // Initialize dosha scores
        var vataScore = 0
        var pittaScore = 0
        var kaphaScore = 0

        // Analyze user responses and update dosha scores
        for (response in userResponses) {
            when (response) {
                // Example: If user prefers warm and spicy foods, increment Pitta score
                "Lean and thin" -> vataScore++
                "Dry and Rough" -> vataScore++
                "Warm and spicy foods" -> pittaScore++
                "Medium build, well-proportioned" -> pittaScore++
                "Normal, neither oily nor dry" -> pittaScore++
                "Stocky or broad build" -> kaphaScore++
                "Oily and prone to blemishes" -> kaphaScore++
                // Add more cases for other responses and update scores accordingly
                // ...
            }
        }

        // Determine dominant dosha
        val maxScore = maxOf(vataScore, pittaScore, kaphaScore)
        var dominantDosha = ""
        if (vataScore == maxScore) dominantDosha += "Vata "
        if (pittaScore == maxScore) dominantDosha += "Pitta "
        if (kaphaScore == maxScore) dominantDosha += "Kapha"

        return if (dominantDosha.isBlank()) "Unknown Prakruti" else dominantDosha.trim()
    }

    private fun getUserResponses(): List<String> {
        // Implement logic to retrieve user responses
        // For example, you might retrieve them from the messageList
        val userResponses: MutableList<String> = mutableListOf()
        for (message in messageList) {
            // Assuming messages with user responses are tagged accordingly
            if (!message.isReceived) {
                userResponses.add(message.content)
            }
        }
        return userResponses
    }

    // Function to display Prakruti result
    private fun displayPrakrutiResult(prakruti: String) {
        // Implement logic to display Prakruti result
        // For example, you might update a TextView with the result
        val prakrutiTextView = findViewById<TextView>(R.id.pachatView)
        prakrutiTextView.text = prakruti
    }

    private fun processUserResponses() {
        // Assuming all user responses are stored in a list
        val userResponses: List<String> = getUserResponses()

        // Determine Prakruti based on user responses
        val prakruti = determinePrakruti(userResponses)

        // Display Prakruti result to the user
        displayPrakrutiResult(prakruti)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addMessageToList(message: String, isReceived: Boolean) {
        messageList.add(Message(message, isReceived))
        paeditMessage.setText("")
        chatAdapter.notifyDataSetChanged()
        pachatView.layoutManager?.scrollToPosition(messageList.size - 1)
    }

    private fun setUpBot() {
        try {
            val stream = this.resources.openRawResource(R.raw.credential)
            val credentials: GoogleCredentials = GoogleCredentials.fromStream(stream)
                .createScoped("https://www.googleapis.com/auth/cloud-platform")
            val projectId: String = (credentials as ServiceAccountCredentials).projectId
            val settingsBuilder: SessionsSettings.Builder = SessionsSettings.newBuilder()
            val sessionsSettings: SessionsSettings = settingsBuilder.setCredentialsProvider(
                FixedCredentialsProvider.create(credentials)
            ).build()
            sessionsClient = SessionsClient.create(sessionsSettings)
            sessionName = SessionName.of(projectId, uuid)
            Log.d(TAG, "projectId : $projectId")
        } catch (e: Exception) {
            Log.d(TAG, "setUpBot: " + e.message)
        }
    }

    private fun sendMessageToBot(message: String) {
        val input = QueryInput.newBuilder()
            .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build()
        GlobalScope.launch {
            sendMessageInBg(input)
        }
    }

    private suspend fun sendMessageInBg(
        queryInput: QueryInput
    ) {
        withContext(Dispatchers.Default) {
            try {
                val detectIntentRequest = DetectIntentRequest.newBuilder()
                    .setSession(sessionName.toString())
                    .setQueryInput(queryInput)
                    .build()
                val result = sessionsClient?.detectIntent(detectIntentRequest)
                if (result != null) {
                    runOnUiThread {
                        updateUI(result)
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "doInBackground: " + e.message)
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(response: DetectIntentResponse) {
        val botReply: String = response.queryResult.fulfillmentText
        if (botReply.isNotEmpty()) {
            addMessageToList(botReply, true)
        } else {
            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

}