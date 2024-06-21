package com.example.quiz.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quiz.model.QuizModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class MainViewModel(val context:Context): ViewModel() {
    val minutesElapsed: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val isTimeUp:MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    private lateinit var countDownTimer: CountDownTimer
    init {
        isTimeUp.value=false
        startTimer()
    }
    var questionNumber:Int=0
    var Score:Int=0
    fun readJsonFromAssets(fileName: String): String {
        try {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
        }
        catch (e: IOException){
            return e.toString()
        }
    }

    fun startTimer() {
        val totalTime = 10 * 60 * 1000L // 10 minutes in milliseconds
        val interval = 1000L // Update every second

        countDownTimer=object : CountDownTimer(totalTime, interval) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = millisUntilFinished / 1000 % 60
                val remainingMinutes = millisUntilFinished / (1000 * 60) % 60

                // Update UI with formatted time (e.g., "02:30")
                minutesElapsed.value= String.format("%02d:%02d", remainingMinutes, remainingSeconds)
            }

            override fun onFinish() {
                Log.e("DEBUGS","X")
                if(isTimeUp.value==false){
                    isTimeUp.value=true
                }
            }
        }.start()
    }
    fun parseJsonUsingOrgJson(jsonString: String): List<QuizModel> {
        val jsonArray = JSONArray(jsonString)
        val questionList = mutableListOf<QuizModel>()

        for (i in 0 until jsonArray.length()) {
            val item: JSONObject = jsonArray.getJSONObject(i)
            val question = item.getString("question")
            val options = item.getJSONArray("options")
            val answer = item.getString("answer")

            val optionsList = mutableListOf<String>()
            for (j in 0 until options.length()) {
                optionsList.add(options.getString(j))
            }

            questionList.add(QuizModel(question, optionsList, answer))
        }

        return questionList
    }
    fun cancel(){
        countDownTimer.cancel()
        isTimeUp.value=false
    }

}