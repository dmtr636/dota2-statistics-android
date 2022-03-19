package com.kodimstudio.dota2stats.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonParser
import com.kodimstudio.dota2stats.R
import com.kodimstudio.dota2stats.data.Data
import com.kodimstudio.dota2stats.dataaccess.AppDatabase
import com.kodimstudio.dota2stats.databinding.ActivityMainBinding
import com.kodimstudio.dota2stats.model.Hero
import com.kodimstudio.dota2stats.model.Item
import com.kodimstudio.dota2stats.ui.search.SearchResultActivity


class MainActivity : AppCompatActivity() {

    companion object{
        lateinit var db: AppDatabase
        lateinit var appContext: Context
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app-db"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

        Companion.db = db
        appContext = applicationContext

        binding.searchBtn.setOnClickListener {
            if (binding.inputField.text.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.fill_search_field), Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, SearchResultActivity::class.java)
            intent.putExtra("steamUrl", binding.inputField.text.toString())
            startActivity(intent)
        }

        binding.inputField.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                binding.inputField.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                //Assign your image again to the view, otherwise it will always be gone even if the text is 0 again.
                binding.inputField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lupa_find_2_0, 0, 0, 0);
            }
        }

        binding.favouritePlayers.setOnClickListener {
            startActivity(Intent(this, FavouritePlayersActivity::class.java))
        }

        binding.helpButton.setOnClickListener {
            val dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.dialog_help)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

        init()
    }

    private fun init() {
        // Loading heroes
        var file = assets.open("heroes.json").bufferedReader().readText()
        val jsonParser = JsonParser()
        val heroes = jsonParser.parse(file).asJsonObject

        for (key in heroes.keySet()) {
            val heroId = key.toInt()
            val heroJson = heroes.getAsJsonObject(key)
            val heroName = heroJson["localized_name"].asString
            val heroIcon = heroJson["img"].asString.split("/").last().replace(".png?", "")
            val drawable = ResourcesCompat.getDrawable(
                resources,
                resources.getIdentifier(heroIcon, "drawable", packageName),
                null)
            val hero = Hero(heroId, heroName, drawable!!)

            Data.HEROES[key.toInt()] = hero
            Data.HEROES_S[heroName] = hero
        }

        // Loading items
        file = assets.open("items.json").bufferedReader().readText()
        val items = jsonParser.parse(file).asJsonObject

        for (key in items.keySet()) {
            try {
                val itemJson = items.getAsJsonObject(key)
                val itemId = itemJson["id"].asInt
                val itemName = itemJson["dname"].asString
                val itemIcon = itemJson["img"].asString.split("/").last().split(".")[0]

                val drawable = ResourcesCompat.getDrawable(
                    resources,
                    resources.getIdentifier(itemIcon, "drawable", packageName),
                    null)
                val item = Item(itemId, itemName, drawable!!)

                Data.ITEMS[itemId] = item
                Data.ITEMS_S[itemName] = item
            }
            catch (e: Exception) {

            }
        }
    }
}