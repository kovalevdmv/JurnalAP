package ru.kdv.jurnalap

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.Drawable
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

        editDate.setText(
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())
        )


    }

    override fun onRestart() {

        editDate.setText(
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())
        )

        //val dd = findViewById<EditText>(R.id.editDateDrug)

        super.onRestart()
    }


    fun onCliclAdd(view: View) {

        try {

            val Date = findViewById<EditText>(R.id.editDate)
            val Up = findViewById<EditText>(R.id.editUp)
            val Down = findViewById<EditText>(R.id.editDown)
            val Puls = findViewById<EditText>(R.id.editPuls)

            if (!(Up.text.isEmpty() || Down.text.isEmpty() || Puls.text.isEmpty())) {

                var ExDir = externalMediaDirs[0].toString()
                var f = ExDir + File.separator + "JAP.db"

                var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)
                db.execSQL(
                    "insert into JPressure (date,u,d,p) values (?,?,?,?)",
                    arrayOf(Date.text, Up.text, Down.text, Puls.text)
                )

                Up.setText("")
                Down.setText("")
                Puls.setText("")

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

            var c = db.rawQuery("""
                SELECT
                    strftime('%m-%d', JPressure.date) as date,
                    JPressure.u,
                    JPressure.d,
                    JPressure.p,
                    strftime('%H:%M', JDrugs.date) as date_drug,
                    Drugs.name as name_drug
                FROM JPressure
                    left join JDrugs  
                        on date(JPressure.date, "start of day") = date(JDrugs.date, "start of day")
                    left join Drugs
                        on JDrugs.id_drugs = Drugs.id
                order by JPressure.date
            """, null)


            val table = findViewById<TableLayout>(R.id.tableReportPressure)


            var arr_tr = arrayListOf<TableRow>()
            var arr_tv = arrayListOf<TextView>()

            while (c.moveToNext()) {

                arr_tr.add( TableRow(this) )

                for (cur_col in c.columnNames){

                    arr_tv.add( TextView(this) )
                    arr_tv.last().text = c.getString(c.getColumnIndex(cur_col))
                    arr_tr.last().addView( arr_tv.last())

                    arr_tv.add( TextView(this) )
                    arr_tv.last().text =  "|"
                    arr_tr.last().addView( arr_tv.last())
                //arr_tr.last().addView( View(this))
                }

                table.addView(arr_tr.last())

            }

            c.close()
            db.close()

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }

    }



    fun onClickAddDrug(view: View) {

        try {

            val drug_name = findViewById<TextView>(R.id.editNameDrug)

            if (!drug_name.text.isEmpty()) {

                var ExDir = externalMediaDirs[0].toString()
                var f = ExDir + File.separator + "JAP.db"

                var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

                db.execSQL(
                    "insert into Drugs (NAME) values (?)",
                    arrayOf(drug_name.text)
                )

                db.close()

                drug_name.setText("")

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }
    }

    fun onClickDrugCurDate(view: View) {

        try {

            val _editDateDrug = findViewById<TextView>(R.id.editDateDrug)
            if (_editDateDrug != null)
                _editDateDrug.setText(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))

            val _editDate = findViewById<TextView>(R.id.editDate)
            if (_editDate != null)
                _editDate.setText(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))

        } catch (e: Exception) {
            Log.e("1", e.toString())
        }

    }

    fun onClickReadListDrug(view: View) {

        try {

            var ExDir = externalMediaDirs[0].toString()
            var f = ExDir + File.separator + "JAP.db"

            var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

            var c = db.rawQuery("select id,name from Drugs", null)
            var arr = arrayListOf<String>()
            while (c.moveToNext()) {
                arr.add(c.getString(c.getColumnIndex("id")) +") "+c.getString(c.getColumnIndex("name")))
            }

            val s = findViewById<Spinner>(R.id.spinner)
            s.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, arr)

            c.close()
            db.close()

        } catch (e: Exception) {
            Log.e("1", e.toString())
        }

    }

    fun onClickAddDruginJurnal(view: View) {
        try {

            val Date = findViewById<EditText>(R.id.editDateDrug)
            val Spin = findViewById<Spinner>(R.id.spinner)
            val selected = Spin.getSelectedItem()

            if (!(Date.text.isEmpty() || selected == null )) {

                val id_drug = "^\\d*".toRegex().find(selected.toString())?.value ?: ""

                if (id_drug?.isEmpty())
                {
                    Toast.makeText(this, "Данные добавлены", Toast.LENGTH_LONG).show()
                    return
                }

                var ExDir = externalMediaDirs[0].toString()
                var f = ExDir + File.separator + "JAP.db"

                var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)
                db.execSQL(
                    "insert into JDrugs (date,id_drugs) values (?,?)",
                    arrayOf(Date.text, id_drug)
                )

                Date.setText("")

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }
    }

}