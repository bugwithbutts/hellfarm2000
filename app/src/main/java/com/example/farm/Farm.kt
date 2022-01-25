package com.example.farm

import android.app.Activity
import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.*
import java.lang.Math.max
import java.lang.Math.min
import kotlin.math.pow
import kotlin.math.sqrt

class Farm {

    class Guy
    {
        var hungry: Long = 0;
        private var layout: ConstraintLayout;
        private var button: ImageView;
        private var cloud: ImageView;
        var type:String;
        var X:Int
        var Y:Int
        constructor(typeTo: String, context: Context, x: Int = -1, y:Int = -1)
        {
            type = typeTo
            button = ImageView(context);
            cloud = ImageView(context);
            when(type)
            {
                "PredatorGuy" -> button.setImageResource(R.drawable.chad_guy);
                "MaleGuy" -> button.setImageResource(R.drawable.male_guy);
                "FemaleGuy" -> button.setImageResource(R.drawable.female_guy);
                else -> button.setImageResource(R.drawable.default_guy);
            }
            cloud.setImageResource(R.drawable.hungry);
            layout = (context as Activity).findViewById(R.id.spawn);
            if(x == -1 || y == -1) {
                var num = 0
                while (true) {
                    num++
                    button.x = (-30 until (layout.width - button.drawable.minimumWidth)).random().toFloat()
                    button.y = (220 until (layout.height - button.drawable.minimumHeight)).random().toFloat()
                    var ok = true;
                    for (guy in guys) {
                        if (sqrt((guy.button.x - button.x).pow(2) + (guy.button.y - button.y).pow(2))
                            < context.resources.getDrawable(R.drawable.default_guy).minimumHeight)
                            ok = false;
                    }
                    if (ok)
                        break;
                    if(num == 10000) throw Exception()
                }
            }
            else{
                button.x = x.toFloat()
                button.y = y.toFloat()
            }
            cloud.x = button.x + 100;
            cloud.y = button.y - 50;
            button.setOnClickListener{
                if(isHungry && food != 0) {
                    removeCloud(context)
                    food --;
                    hungry = 0;
                }
            }
            X = button.x.toInt()
            Y = button.y.toInt()
            layout.addView(button);
        }
        private var isHungry: Boolean = false;
        fun showCloud(context: Context)
        {
            if(!isHungry) {
                (context as Activity).runOnUiThread{layout.addView(cloud)}
                isHungry = true;
            }
        }
        private fun removeCloud(context: Context)
        {
            if(isHungry) {
                (context as Activity).runOnUiThread{layout.removeView(cloud)}
                isHungry = false;
            }
        }

        fun delete(context: Context) {
            (context as Activity).runOnUiThread{layout.removeView(button)}
            if(isHungry)
                context.runOnUiThread{layout.removeView(cloud)}
        }
    }

    companion object
    {
        var money: Float = 20F;
        var guys:MutableList<Guy> = mutableListOf();
        var food: Int = 0
        fun addGuy(name: String, context: Context, cost: Int)
        {
            if(guys.size < 10) {
                try {
                    guys.add(Guy(name, context));
                    money -= cost.toFloat();
                }
                catch(e: Exception){

                }
            }
        }
        var isRunning = false
        fun go(context: Context)
        {
            while(isRunning)
            {
                val toDel:MutableList<Guy> = mutableListOf()
                for(curGuy in guys)
                {
                    curGuy.hungry++;
                    if(money < 9999F)
                    money += when(curGuy.type) {
                        "PredatorGuy" -> 0.04F
                        "MaleGuy" -> 0.02F
                        "FemaleGuy" -> 0.03F
                        else -> 0.01F
                    }
                    money = min(money, 9999F)
                    if(curGuy.hungry >= 5000)
                        toDel.add(curGuy)
                    else if(curGuy.hungry >= 2000)
                        curGuy.showCloud(context)
                }
                for(delGuy in toDel)
                {
                    delGuy.delete(context)
                    guys.remove(delGuy)
                }
                save(context)
                Thread.sleep(1800);
            }
        }

        fun newGame(context: Context) {
            for(curGuy in guys) curGuy.delete(context)
            guys.clear()
            money = 20F;
            food = 0
        }

        fun start(context: Context) {
            Thread(Runnable{
                val mny = (context as Activity).findViewById<TextView>(R.id.mny)
                val fdd = (context).findViewById<TextView>(R.id.textfood)
                while(true) {
                  context.runOnUiThread { mny.text = String.format("%.2f", money) }
                  context.runOnUiThread { fdd.text = "$food" }
                  Thread.sleep(10)
                }
            }).start();
        }
        private fun save(context: Context){
            try {
                val guysCopy = guys
                val sv = context.getSharedPreferences("DATA", Context.MODE_PRIVATE)
                val ed = sv.edit()
                ed.putInt("FOOD", food)
                ed.putFloat("MNY", money)
                ed.putInt("NUM", guysCopy.size)
                ed.putLong("TIME", System.currentTimeMillis())
                for (i in 0 until guysCopy.size) {
                    ed.putLong("food$i", guysCopy[i].hungry)
                    ed.putInt("x$i", guysCopy[i].X)
                    ed.putInt("y$i", guysCopy[i].Y)
                    ed.putString("type$i", guysCopy[i].type)
                }
                ed.apply()
            }
            finally{}
        }

        fun load(context: Context) {
            try {
                val sv = context.getSharedPreferences("DATA", Context.MODE_PRIVATE)
                food = sv.getInt("FOOD", 0)
                money = sv.getFloat("MNY", 20F)
                val num = sv.getInt("NUM", 0)
                val time = sv.getLong("TIME", System.currentTimeMillis())
                for(guy in guys) guy.delete(context)
                guys.clear()
                for (i in 0 until num) {
                    val guy = Guy(
                        sv.getString("type$i", "").toString(), context,
                        sv.getInt("x$i", 0), sv.getInt("y$i", 0)
                    )
                    val dop = (System.currentTimeMillis() - time) / 1800
                    val dop2 = min(max(5000 - sv.getLong("food$i", 5000), 0), dop)
                    money += when (guy.type) {
                        "PredatorGuy" ->
                            0.04F * dop2
                        "MaleGuy" -> 0.02F * dop2
                        "FemaleGuy" -> 0.03F * dop2
                        else -> 0.01F * dop2
                    }
                    guy.hungry = sv.getLong("food$i", 0) + dop
                    guys.add(guy)
                }
            }
            finally{}
        }
    }
}