package com.example.voiceassistant

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceassistant.LoginActivity.Companion.randomCode
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.dataconnect.serializers.UUIDSerializer
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import kotlinx.serialization.Serializable
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import opennlp.tools.tokenize.SimpleTokenizer
import java.io.InputStreamReader
import java.util.*
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import java.util.UUID
import android.app.AlertDialog
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager


class MainActivity : AppCompatActivity() {

    private lateinit var btnSpeak: Button
    private lateinit var tvResult: TextView
    private lateinit var tvSpeechInput: TextView
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech
    private lateinit var btnViewCart: Button // Declare the button

    private var menuItems: List<MenuItem> = emptyList() // Initialize with an empty list
    //private lateinit var menuItems: List<MenuItem>
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter

    val supabase = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Postgrest)
        install(Auth)
    }

    @Serializable
    data class MenuItem(
        @SerialName("food_name") val foodName: String,
        @SerialName("category") val category: String,
        @SerialName("taste") val taste: String,
        @SerialName("price") val price: Double
    )

    @Serializable
    data class CartItem(
        @SerialName("user_id") @Serializable(with = UUIDSerializer::class) val user_id: UUID,
        @SerialName("order_id") val order_id: String,
        @SerialName("food_name") val foodName: String,
        @SerialName("category") val category: String,
        @SerialName("taste") val taste: String,
        @SerialName("price") val price: Double,
        @SerialName("quantity") val quantity: Int,
        @SerialName("payment_status") val paymentStatus: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSpeak = findViewById(R.id.btnSpeak)
        tvResult = findViewById(R.id.tvResult)
        tvSpeechInput = findViewById(R.id.tvSpeechInput)
        recyclerView = findViewById(R.id.recyclerView)
        //btnViewCart = findViewById(R.id.btnViewCart) // Initialize the button

        // Initialize RecyclerView
        //recyclerView.layoutManager = LinearLayoutManager(this)
        //recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Initialize RecyclerView with an empty list
        menuAdapter = MenuAdapter(emptyList()) { menuItem ->
            addToCart(menuItem)
        }
        recyclerView.adapter = menuAdapter

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Initialize TextToSpeech
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language is not supported")
                }
            } else {
                Log.e(TAG, "Initialization failed")
            }
        }

        // Load menu items from Supabase
        loadMenuItems()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    // Handle Home navigation
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_search -> {
                    // Handle Search navigation
                    true
                }
                R.id.action_cart -> {
                    // Navigate to CartActivity
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_profile -> {
                    // Handle Profile navigation
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USER_ID", "some-user-id")
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        btnSpeak.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognition()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSIONS_REQUEST_RECORD_AUDIO)
            }
        }

        // Set up SpeechRecognizer listener
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Optional: Handle UI changes or feedback based on microphone input
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Optional: Handle audio buffer received
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                Log.e(TAG, "onError: $error")
                speechRecognizer.cancel()
            }

            override fun onResults(results: Bundle?) {
                results?.let {
                    val voiceCommands = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    voiceCommands?.let { commands ->
                        if (commands.isNotEmpty()) {
                            val voiceCommand = commands[0]
                            tvSpeechInput.text = voiceCommand // Update TextView with recognized speech
                            handleVoiceCommand(voiceCommand)
                        }
                    }
                }
                speechRecognizer.cancel()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Optional: Handle partial recognition results
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Optional: Handle events related to recognition
            }
        })
        //btnViewCart.setOnClickListener {
        //    val intent = Intent(this, CartActivity::class.java)
        //    startActivity(intent)
        //}

        // Initialize menuAdapter and set it to recyclerView
        menuAdapter = MenuAdapter(menuItems) { menuItem ->
            addToCart(menuItem)
        }
        recyclerView.adapter = menuAdapter

        // In MainActivity or any other activity
        val uuid = LoginActivity.UserData.uuid
        Log.d("MainActivity", "User UUID: $uuid")
    }

    private fun startVoiceRecognition() {
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        speechRecognizer.startListening(recognizerIntent)
    }

    private fun handleVoiceCommand(command: String) {
        Log.d(TAG, "Voice command received: $command")

        // Normalize the command for exact matching
        val normalizedCommand = command.trim().toLowerCase()
        Log.d(TAG, "Normalized command: $normalizedCommand")

        val response: String
        val itemsToDisplay: List<MenuItem>

        if (normalizedCommand.contains("best seller", ignoreCase = true)) {
            Log.d(TAG, "Processing best seller command")
            val bestSellers = getBestSellers(menuItems)
            if (bestSellers.isNotEmpty()) {
                itemsToDisplay = bestSellers
                response = "Here are the best sellers."
            } else {
                response = "No best sellers available."
                itemsToDisplay = emptyList()
            }
        } else if (normalizedCommand.contains("recommend", ignoreCase = true) ||
            normalizedCommand.contains("want", ignoreCase = true) ||
            normalizedCommand.contains("give", ignoreCase = true) ||
            normalizedCommand.contains("suggest", ignoreCase = true)) {
            Log.d(TAG, "Processing recommendation command")
            val (category, taste) = extractFeatures(normalizedCommand)
            Log.d(TAG, "Extracted category: $category, taste: $taste")
            if (category == null && taste == null) {
                response = "Sorry, that is not on our menu."
                itemsToDisplay = emptyList()
            } else {
                val recommendations = recommendItems(menuItems, category, taste)
                if (recommendations.isNotEmpty()) {
                    itemsToDisplay = recommendations
                    response = "Here are some recommendations."
                } else {
                    response = "No recommendations available."
                    itemsToDisplay = emptyList()
                }
            }
        } else if (normalizedCommand.contains("hello", ignoreCase = true)) {
            response = "Hello! How can I assist you?"
            itemsToDisplay = emptyList()
        } else {
            response = "Sorry, I didn't understand that."
            itemsToDisplay = emptyList()
        }

        // Display response in TextView and optionally speak it out loud
        tvResult.text = response
        if (response.isNotEmpty()) speak(response)

        // Update the RecyclerView with the items to display
        menuAdapter = MenuAdapter(itemsToDisplay) { menuItem ->
            addToCart(menuItem)
        }
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = menuAdapter

    }

    private fun addToCart(menuItem: MenuItem) {
        showAlertDialog(this@MainActivity, "Item Added to Cart!", "Mcdonalds app notification")

        val uuid = LoginActivity.UserData.uuid
        val uuid3 = extractUUID(uuid)
        Log.d(TAG, "$uuid3")
        val uuid2: UUID = UUID.fromString(uuid3)


        CoroutineScope(Dispatchers.IO).launch {
            try {
                // val userId = "some_user_id" // Ensure this is valid

                val cartItem = CartItem(
                    user_id = uuid2,
                    order_id = randomCode,
                    foodName = menuItem.foodName,
                    category = menuItem.category,
                    taste = menuItem.taste,
                    price = menuItem.price,
                    quantity = 1,
                    paymentStatus = "Not Paid"
                )

                Log.d(TAG, "Data being inserted: $cartItem")

                // val response = supabase.postgrest.from("cart").insert(data).execute()
                val result = supabase.postgrest.from("cart").insert(cartItem)
                Log.d(TAG, "Supabase query result: $result")

            } catch (e: Exception) {
                Log.e(TAG, "Exception in addToCart", e)
                // Handle exception (e.g., show user feedback)
            }
        }
    }

    fun showAlertDialog(context: Context, title: String, message: String) {
        val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
        builder.setTitle(title)
        builder.setMessage(message)


        val dialog = builder.create()
        dialog.show()

        // Position the dialog at the top of the screen
        val window = dialog.window
        window?.let {
            val layoutParams = WindowManager.LayoutParams().apply {
                copyFrom(it.attributes)
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 150 // Adjust this value to position the dialog vertically
                width = 650
                height = 400
            }
            it.attributes = layoutParams
        }

        // Automatically dismiss the dialog after less than 1 second
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 820) // 820 milliseconds delay
    }




    fun extractUUID(input: String): String? {
        // Define the regex pattern for UUID
        val regex = """([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})""".toRegex()

        // Find the first match
        val matchResult = regex.find(input)

        // Return the matched value, or null if no match is found
        return matchResult?.value
    }

    private fun speak(text: String) {
        val result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Failed to speak: $text")
        }
    }

    override fun onDestroy() {
        // Shutdown SpeechRecognizer and TextToSpeech when closing the app
        speechRecognizer.destroy()

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startVoiceRecognition()
                } else {
                    Log.e(TAG, "Permission denied: RECORD_AUDIO")
                }
                return
            }
            else -> {
                // Handle other permissions if needed
            }
        }
    }

    /*
    private fun loadMenuItems(context: Context): List<MenuItem> {
        val inputStream = context.assets.open("mcdelivery_menu.csv")
        val reader = InputStreamReader(inputStream)
        val csvParser = CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())

        // Log headers
        val headers = csvParser.headerMap.keys
        Log.d(TAG, "CSV Headers: $headers")

        val menuItems = mutableListOf<MenuItem>()

        for (record in csvParser) {
            val foodName = record.get("food_name")
            val category = record.get("category")
            val taste = record.get("taste")
            val price = record.get("price").toDouble()

            Log.d(TAG, "Loaded item: Name=$foodName, Category=$category, Taste=$taste, Price=$price")

            menuItems.add(MenuItem(foodName, category, taste, price))
        }

        csvParser.close()
        reader.close()

        return menuItems
    }
    */

    private fun loadMenuItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabase.postgrest.from("mcdo_menu").select()
                Log.d(TAG, "Supabase query result: $result")

                val items = result.decodeList<MenuItem>()
                Log.d(TAG, "Decoded items: $items")

                withContext(Dispatchers.Main) {
                    menuItems = items
                    menuAdapter = MenuAdapter(menuItems) { menuItem ->
                        addToCart(menuItem)
                    }
                    recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 2)
                    recyclerView.adapter = menuAdapter
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading menu items", e)
            }
        }
    }



    private fun recommendItems(items: List<MenuItem>, category: String?, taste: String?): List<MenuItem> {
        return if (category == null && taste == null) {
            emptyList() // Return an empty list if no meaningful criteria are provided
        } else {
            items.filter {
                (category == null || it.category.equals(category, ignoreCase = true)) &&
                        (taste == null || it.taste.equals(taste, ignoreCase = true))
            }
        }
    }

    private fun getBestSellers(items: List<MenuItem>): List<MenuItem> {
        return items.filter { it.category.equals("best seller", ignoreCase = true) }
    }

    private fun tokenizeText(text: String): List<String> {
        val tokenizer = SimpleTokenizer.INSTANCE
        val stopWords = setOf("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now")

        val tokens = tokenizer.tokenize(text.toLowerCase())
        Log.d(TAG, "Raw tokens: $tokens")
        val filteredTokens = tokens.filter { it.isNotEmpty() && it !in stopWords }
        Log.d(TAG, "Filtered tokens: $filteredTokens")

        return filteredTokens
    }

    private fun extractFeatures(userInput: String): Pair<String?, String?> {
        val tokens = tokenizeText(userInput)

        val categoryMapping = mapOf(
            "burgers" to "burger",
            "burger" to "burger",
            "side" to "sides",
            "pasta" to "pasta",
            "dessert" to "dessert",
            "desserts" to "dessert",
            "beverage" to "beverages",
            "drinks" to "drinks",
            "drink" to "drinks",
            "chickens"  to "chicken",
            "chicken" to "chicken"
        )

        val tastes = listOf("savory", "salty", "sweet", "bitter")

        var category: String? = null
        var taste: String? = null

        for (token in tokens) {
            category = categoryMapping[token] ?: category
            taste = if (token in tastes) token else taste
        }

        Log.d(TAG, "Extracted tokens: $tokens")
        Log.d(TAG, "Determined category: $category, taste: $taste")

        return Pair(category, taste)
    }

    companion object {

        const val TAG = "MainActivity"
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }
}