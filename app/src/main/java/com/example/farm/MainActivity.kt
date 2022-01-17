package com.example.farm

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar;


class MainActivity : AppCompatActivity() {
    private lateinit var music:MediaPlayer;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        Farm.start(this);
        Thread(Runnable{
            Farm.go(this);
        }).start()
        music = MediaPlayer.create(this, R.raw.farm)
        music.isLooping = true
        music.setVolume(0.4F,0.4F)
        music.seekTo(15000)
        music.start()
    }
    override fun onPause() {
        super.onPause()
        music.pause();
    }

    override fun onResume() {
        super.onResume()
        if(!pause)
            music.start()
    }
    override fun onRestart() {
        super.onRestart()
        Farm.load(this)
    }

    private var pause: Boolean = false
    fun play(view: View)
    {
        if(music.isPlaying){
            music.pause()
            view.background = getDrawable(R.drawable.disnote)
            pause = true
        }
        else {
            music.seekTo(15000)
            music.start()
            view.background = getDrawable(R.drawable.note)
            pause = false
        }
    }
    fun createMaleGuy(view: View)
    {
        if(Farm.money >= 100) {
            Farm.addGuy("MaleGuy", this, 100);
        }
    }
    fun createFemaleGuy(view: View)
    {
        if(Farm.money >= 200) {
            Farm.addGuy("FemaleGuy", this, 200);
        }
    }
    fun createDefaultGuy(view: View)
    {
        if(Farm.money >= 20) {
            Farm.addGuy("DefaultGuy", this, 20);
        }
    }
    fun createPredatorGuy(view: View)
    {
        if(Farm.money >= 1000) {
            Farm.addGuy("PredatorGuy", this, 1000);
        }
    }
    fun newGame(view: View)
    {
        Farm.newGame(this)
    }
    fun buyFood(view:View)
    {
        if(Farm.money >= 15) {
            Farm.money -= 15;
            Farm.food ++;
        }
    }
}