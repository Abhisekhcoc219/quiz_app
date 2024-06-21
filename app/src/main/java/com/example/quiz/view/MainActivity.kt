package com.example.quiz.view

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.quiz.R
import com.example.quiz.R.id.continueButton
import com.example.quiz.databinding.ActivityMainBinding
import com.example.quiz.databinding.CustomDialogBinding
import com.example.quiz.databinding.TimeupBinding
import com.example.quiz.model.QuizModel
import com.example.quiz.viewModel.MainViewModel
import com.example.quiz.viewModel.MainViewModelFactor
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONArray

@Suppress("UNREACHABLE_CODE")
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var quizList:List<QuizModel>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mainViewModel=ViewModelProvider(this,MainViewModelFactor(this)).get(MainViewModel::class.java)
        val jsonString = mainViewModel.readJsonFromAssets("quizs.json")
        quizList=mainViewModel.parseJsonUsingOrgJson(jsonString)
        mainViewModel.minutesElapsed.observe(this, Observer {
           binding.timer.text=it
        })
        binding.apply {
            questionNumbers.text="Question(${mainViewModel.questionNumber+1}/10)"
            mainQuestion.text=quizList.get(mainViewModel.questionNumber).question
            op1.text=quizList.get(mainViewModel.questionNumber).option.get(0)
            op2.text=quizList.get(mainViewModel.questionNumber).option.get(1)
            op3.text=quizList.get(mainViewModel.questionNumber).option.get(2)
            op4.text=quizList.get(mainViewModel.questionNumber).option.get(3)
        }
        binding.apply {
            continueButton.setOnClickListener(this@MainActivity)
            op1.setOnClickListener(this@MainActivity)
            op2.setOnClickListener(this@MainActivity)
            op3.setOnClickListener(this@MainActivity)
            op4.setOnClickListener(this@MainActivity)
        }
    }
    private fun nextQuestions(index:Int){
        binding.apply {
            questionNumbers.text="Question(${index+1}/10)"
            mainQuestion.text=quizList.get(mainViewModel.questionNumber).question
            op1.text=quizList.get(index).option.get(0)
            op2.text=quizList.get(index).option.get(1)
            op3.text=quizList.get(index).option.get(2)
            op4.text=quizList.get(index).option.get(3)
        }
    }
    override fun onClick(v: View?) {
        binding.apply {
            changeButtonShapeColor(ContextCompat.getColor(this@MainActivity,R.color.white),op1)
            changeButtonShapeColor(ContextCompat.getColor(this@MainActivity,R.color.white),op2)
            changeButtonShapeColor(ContextCompat.getColor(this@MainActivity,R.color.white),op3)
            changeButtonShapeColor(ContextCompat.getColor(this@MainActivity,R.color.white),op4)
        }
        var answer=""
        val currentBtn= v as AppCompatButton
        if(v.id== continueButton){
            val size=quizList.size
            mainViewModel.questionNumber++
            if(mainViewModel.questionNumber<size){
                nextQuestions(mainViewModel.questionNumber)
                startAnim()
            }
            else{
               showDialog()
            }
        }
        else{
            answer=v.text.toString()
            if(mainViewModel.questionNumber<quizList.size){
                if(answer==quizList.get(mainViewModel.questionNumber).ans){
                    mainViewModel.Score++
                }
            }
            else{
             showDialog()
            }
            changeButtonShapeColor(ContextCompat.getColor(this@MainActivity,R.color.orange),currentBtn)
        }
    }

    fun changeButtonShapeColor(color: Int, myButton: AppCompatButton) {
        val shapeDrawable = ContextCompat.getDrawable(this, R.drawable.radius_option)?.mutate() as GradientDrawable?
        shapeDrawable?.setColor(color)
        myButton.background = shapeDrawable
    }
    private fun startAnim(){
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.sliding_right)
        binding.apply {
            linearLayout1.startAnimation(slideIn)
            op1.startAnimation(slideIn)
            op2.startAnimation(slideIn)
            op3.startAnimation(slideIn)
            op4.startAnimation(slideIn)
            mainQuestion.startAnimation(slideIn)
            continueButton.startAnimation(slideIn)
            questionNumbers.startAnimation(slideIn)
        }
    }
    private fun showDialog(){
        val dialog=CustomDialogBinding.inflate(layoutInflater)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialog.root)
        val alertDialog=dialogBuilder.create()
        if(mainViewModel.Score>4){
            dialog.quizTotal.text="Congrats You win Quiz (${mainViewModel.Score}/10)"
        }
        else{
            dialog.quizTotal.text="Sorry You lose Quiz (${mainViewModel.Score}/10)"
        }
        dialog.Finish.setOnClickListener {
            mainViewModel.Score=0
            mainViewModel.questionNumber=0
            startAnim()
            binding.apply {
                questionNumbers.text="Question(${mainViewModel.questionNumber+1}/10)"
                mainQuestion.text=quizList.get(mainViewModel.questionNumber).question
                op1.text=quizList.get(mainViewModel.questionNumber).option.get(0)
                op2.text=quizList.get(mainViewModel.questionNumber).option.get(1)
                op3.text=quizList.get(mainViewModel.questionNumber).option.get(2)
                op4.text=quizList.get(mainViewModel.questionNumber).option.get(3)
            }
            mainViewModel.cancel()
            alertDialog.dismiss()
            mainViewModel.startTimer()
        }
       alertDialog.show()
    }
    private fun timeUpshowDialog(){
        val dialog=TimeupBinding.inflate(layoutInflater)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialog.root)
        val alertDialog=dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(getDrawable(R.drawable.radius_shape))
        alertDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.setCancelable(false)
        if(mainViewModel.Score>4){
            dialog.quizTotal.text="Congrats You win Quiz (${mainViewModel.Score}/10)"
        }
        else{
            dialog.quizTotal.text="Sorry You lose Quiz (${mainViewModel.Score}/10)"
        }
        dialog.Finish.setOnClickListener {
            mainViewModel.Score=0
            mainViewModel.questionNumber=0
            startAnim()
            alertDialog.dismiss()
            mainViewModel.startTimer()
        }
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.isTimeUp.observe(this ,Observer{
            if(it){
                Log.e("DEBUGS","true")
                timeUpshowDialog()
                mainViewModel.isTimeUp.value=false
            }
        })
    }
}