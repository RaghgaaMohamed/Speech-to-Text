
package com.example.speechtotext

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.RecognizerIntent
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

    var mic: ImageView? = null
    var tv_Speech_to_text: TextView? = null
    var text :TextView?=null
    var savebtn: Button?=null
    var pageheight=400
    var pagewidth=200

    val REQUEST_CODE_SPEECH_INPUT = 1
    val PERMISSION_REQUEST_CODE = 200
    var m_Text=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mic = findViewById(R.id.iv_mic);
        tv_Speech_to_text = findViewById(R.id.tv_speech_to_text);
        text=findViewById(R.id.mytext)
        savebtn=findViewById(R.id.save_btn)

        val micr =mic
        if (micr ==null) return
        micr.setOnClickListener {

                val intent =Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

             try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)

            } catch(e:Exception) {
                Toast.makeText(this@MainActivity, " " + e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }

        if(chechPermission()){
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        val save =savebtn
        if(save==null) return
        save.setOnClickListener {
            val builder =AlertDialog.Builder(this)
            builder.setTitle("Enter name of PDF:")
            val input=EditText(this)
            input.inputType=InputType.TYPE_CLASS_TEXT
            builder.setView(input)
            builder.setPositiveButton("OK", DialogInterface.OnClickListener{ dialog, which ->
                     m_Text=input.text.toString()
                     generatePDF()

            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener{dialog, which ->
                dialog.dismiss()
            })
            builder.show()

        }


    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT){
            if (resultCode == RESULT_OK && data != null){
                var result =data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                text?.setText(Objects.requireNonNull(result)?.get(0))
            }
        }
    }


    private fun generatePDF() {
        Log.e("I'm Here","YES")
        val pdfDocument=PdfDocument()
        val title=Paint()
        val mypageInfo= PdfDocument.PageInfo.Builder(pagewidth,pageheight,1).create()
        val myPage :PdfDocument.Page=pdfDocument.startPage(mypageInfo)
        val canvas=myPage.canvas
        title.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.NORMAL))
        title.textSize=10f
        title.setColor(ContextCompat.getColor(this,R.color.black))
        canvas.drawText(text?.text.toString(),20f,20f,title)
        pdfDocument.finishPage(myPage)
        val file= File(Environment.getExternalStorageDirectory(),m_Text+".pdf")

        try {
            Log.e("I'm in","try")
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this@MainActivity,"PDF file generated successfully.",Toast.LENGTH_LONG).show()

        }catch (e:IOException){
         Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_LONG).show()
        }
        pdfDocument.close()
    }

    private fun chechPermission(): Boolean {
        val permission1=ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permission2=ContextCompat.checkSelfPermission(applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission() {
        val arr_permi:Array<String> = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.READ_EXTERNAL_STORAGE)
          ActivityCompat.requestPermissions(this,arr_permi,PERMISSION_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0) {
                val writeStorage: Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readStorage: Boolean = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Denined.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}

