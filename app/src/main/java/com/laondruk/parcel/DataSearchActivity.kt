package com.laondruk.parcel

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity


class DataSearchActivity : AppCompatActivity() {
    private lateinit var imagePath: String
    private var grade: Int? = null
    private var klass: Int? = null
    private var number: Int? = null
    private var name: String? = null
    private var nameAnnotation: Char? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_search)

        imagePath = intent.getStringExtra("imagePath").toString()
        grade = intent.getStringExtra("grade")?.toInt()
        klass = intent.getStringExtra("klass")?.toInt()
        number = intent.getStringExtra("number")?.toInt()
        name = intent.getStringExtra("name").toString()
        nameAnnotation = intent.getCharExtra("nameAnnotation", ' ')

        if (nameAnnotation == ' ') {
            nameAnnotation = null
        }

        val nameViews = arrayOf<TextView>(
            findViewById(R.id.nameChar1),
            findViewById(R.id.nameChar2),
            findViewById(R.id.nameChar3),
            findViewById(R.id.nameChar4)
        )

        findViewById<Button>(R.id.sendButton)

        val gradeView = findViewById<NumberPicker>(R.id.gradePicker)
        gradeView.minValue = 1
        gradeView.maxValue = 3
        val klassView = findViewById<NumberPicker>(R.id.klassPicker)
        klassView.minValue = 1
        klassView.maxValue = 8
        val numberView = findViewById<NumberPicker>(R.id.numberPicker)
        numberView.minValue = 1
        numberView.maxValue = 30


        try {
            nameViews[0].text = name!![0].toString()
            nameViews[1].text = name!![1].toString()
            nameViews[2].text = name!![2].toString()
            nameViews[3].text = name!![3].toString()
            gradeView.value = grade!!
            klassView.value = klass!!
            numberView.value = number!!
        } catch (e: StringIndexOutOfBoundsException) {
        } catch (e: NullPointerException) {
        }


        val bitmap = BitmapFactory.decodeFile(imagePath)
        findViewById<ImageView>(R.id.capturedImageView).setImageBitmap(bitmap)

    }
}