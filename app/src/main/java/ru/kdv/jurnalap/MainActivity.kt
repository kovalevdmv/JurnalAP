package ru.kdv.jurnalap

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.*
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        editDate.setText( SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) )


    }

    override fun onRestart() {

        editDate.setText( SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) )

        //val dd = findViewById<EditText>(R.id.editDateDrug)

        super.onRestart()
    }



    fun onCliclAdd(view: View) {

        try {

            val Date = findViewById<EditText>(R.id.editDate)
            val Up = findViewById<EditText>(R.id.editUp)
            val Down = findViewById<EditText>(R.id.editDown)
            val Puls = findViewById<EditText>(R.id.editPuls)
            val Comm = findViewById<EditText>(R.id.editTextTextMultiLine)

            if (!(Up.text.isEmpty() || Down.text.isEmpty() || Puls.text.isEmpty())) {

                var ExDir = externalMediaDirs[0].toString()
                var f = ExDir + File.separator + "JAP.db"

                var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)
                db.execSQL(
                    "insert into main (date,U,D,P,COMMENT) values (?,?,?,?,?)",
                    arrayOf(Date.text, Up.text, Down.text, Puls.text, Comm.text)
                )

                Up.setText("")
                Down.setText("")
                Puls.setText("")
                Comm.setText("")

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }

    }

    fun onClickReport(view: View) {

        try {

            var ExDir = externalMediaDirs[0].toString()
            var f = ExDir + File.separator + "JAP.db"

            var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

            var c = db.rawQuery("select * from main", null)
            var str = ""
            while (c.moveToNext()) {
                str +=
                    c.getString(c.getColumnIndex("date")) + ";" +
                            c.getString(c.getColumnIndex("U")) + ";" +
                            c.getString(c.getColumnIndex("D")) + ";" +
                            c.getString(c.getColumnIndex("P")) + ";" +
                            c.getString(c.getColumnIndex("COMMENT")) + "\n"
            }

            c.close()
            db.close()

            findViewById<EditText>(R.id.editReport).setText(str)

            Log.d("db", str)

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }

    }

    fun AddDrug(exist : Boolean){
        try {

            val Date = findViewById<EditText>(R.id.editDateDrug)
            val NameDrug = findViewById<EditText>(R.id.editNameDrug)

            var drug_name = NameDrug.text.toString()

            val s = findViewById<Spinner>(R.id.spinner)

            if (exist) {
                drug_name=""
                val select = s.getSelectedItem()
                if (select != null)
                    drug_name = select.toString()
            }

            if (!(Date.text.isEmpty() || drug_name.isEmpty())) {

                var ExDir = externalMediaDirs[0].toString()
                var f = ExDir + File.separator + "JAP.db"

                var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

                db.execSQL(
                    "insert into DRUGS (NAME,TIME) values (?,?)",
                    arrayOf(drug_name, Date.text)
                )

                db.close()

                NameDrug.setText("")

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }
    }

    fun onClickAddDrug(view: View) {

        AddDrug(false)
    }

    fun onClickDrugCurDate(view: View) {

        try {

            val _editDateDrug = findViewById<TextView>(R.id.editDateDrug)
            if (_editDateDrug != null)
                _editDateDrug.setText(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))

            val _editDate = findViewById<TextView>(R.id.editDate)
            if (_editDate != null)
                _editDate.setText(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))

        } catch (e:Exception){
            Log.e("1", e.toString())
        }

    }

    fun onClickReadListDrug(view: View) {

        try {

            var ExDir = externalMediaDirs[0].toString()
            var f = ExDir + File.separator + "JAP.db"

            var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

            var c = db.rawQuery("select NAME from DRUGS group by NAME order by TIME desc", null)
            var arr = arrayListOf<String>()
            while (c.moveToNext()) {
                    arr.add(c.getString(c.getColumnIndex("NAME")))
            }

            val s = findViewById<Spinner>(R.id.spinner)
            s.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arr)

            c.close()
            db.close()

        } catch (e:Exception) {
            Log.e("1", e.toString())
        }

    }

    fun onClickAddExsist(view: View) {
        AddDrug(true)
    }

}