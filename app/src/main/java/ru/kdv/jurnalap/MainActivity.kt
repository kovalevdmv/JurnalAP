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
import java.nio.charset.Charset
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

        editDateDrug.setText(
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())
        )

        editDateEvents.setText(
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())
        )

        editDateFeeling.setText(
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())
        )

        onClickReadListDrug(View(this))

        onClickReadListTEvents(View(this))


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

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }

        onClickSafeToCSV(View(this))

    }

    fun onClickReport(view: View) {

        try {

            var ExDir = externalMediaDirs[0].toString()
            var f = ExDir + File.separator + "JAP.db"

            var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

            var c = db.rawQuery("""
                SELECT
                    strftime('%m-%d %H:%M', JPressure.date) as date,
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
                order by JPressure.date desc
            """, null)


            val table = findViewById<TableLayout>(R.id.tableReportPressure)


            var arr_tr = arrayListOf<TableRow>()
            var arr_tv = arrayListOf<TextView>()

            var pre_date=""
            var next_day = false

            while (c.moveToNext()) {

                arr_tr.add( TableRow(this) )

                next_day = false

                for (cur_col in c.columnNames){

                    if (cur_col == "date") {
                        next_day = pre_date != c.getString(c.getColumnIndex(cur_col))
                        pre_date = c.getString(c.getColumnIndex(cur_col))
                    }

                    arr_tv.add( TextView(this) )
                    arr_tv.last().text = c.getString(c.getColumnIndex(cur_col))
                    arr_tr.last().addView( arr_tv.last())

                    arr_tv.add( TextView(this) )
                    arr_tv.last().text =  "|"
                    arr_tr.last().addView( arr_tv.last())

                }

                table.addView(arr_tr.last())

                //arr_tr.add( TableRow(this) )

//                if (next_day){
//                    arr_tr.add( TableRow(this) )
//                    arr_tr.last().addView(TextView(this))
//                    table.addView(arr_tr.last())
//                }

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

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }
    }

    fun onClickReadListDrug(view: View) {

        try {

            var ExDir = externalMediaDirs[0].toString()
            var f = ExDir + File.separator + "JAP.db"

            var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

            var c = db.rawQuery("""
                select Drugs.id,name 
                    from Drugs 
                    left join JDrugs
                     on Drugs.id=JDrugs.id_drugs
                     group by Drugs.id,name
                     order by JDrugs.date desc
                """, null)
            var arr = arrayListOf<String>()
            arr.add("")
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

    fun onClickReadListTEvents(view: View) {

        try {

            var ExDir = externalMediaDirs[0].toString()
            var f = ExDir + File.separator + "JAP.db"

            var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

            var c = db.rawQuery("""
                select TEvents.id,name 
                    from TEvents 
                    left join JEvents
                     on TEvents.id = JEvents.id_tevents
                     group by TEvents.id,name
                     order by JEvents.date desc
                """, null)
            var arr = arrayListOf<String>()
            arr.add("")
            while (c.moveToNext()) {
                arr.add(c.getString(c.getColumnIndex("id")) +") "+c.getString(c.getColumnIndex("name")))
            }

            val s = findViewById<Spinner>(R.id.spinnerTEvents)
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
                    Toast.makeText(this, "Не выбран препарат", Toast.LENGTH_SHORT).show()
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
                Spin.setSelection(0)

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }

        onClickSafeToCSV(View(this))
    }

    fun onAddTypeEvent(view: View) {
        try {

            val name = findViewById<TextView>(R.id.editNameEvent)

            if (!name.text.isEmpty()) {

                var ExDir = externalMediaDirs[0].toString()
                var f = ExDir + File.separator + "JAP.db"

                var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

                db.execSQL(
                    "insert into TEvents (NAME) values (?)",
                    arrayOf(name.text)
                )

                db.close()

                name.setText("")

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }
    }

    fun onClickAddJEvents(view: View) {

        try {

            val Date = findViewById<EditText>(R.id.editDateEvents)
            val Spin = findViewById<Spinner>(R.id.spinnerTEvents)
            val selected = Spin.getSelectedItem()

            if (!(Date.text.isEmpty() || selected == null )) {

                val id_event = "^\\d*".toRegex().find(selected.toString())?.value ?: ""

                if (id_event?.isEmpty())
                {
                    Toast.makeText(this, "Не выбрано событие", Toast.LENGTH_SHORT).show()
                    return
                }

                var ExDir = externalMediaDirs[0].toString()
                var f = ExDir + File.separator + "JAP.db"

                var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)
                db.execSQL(
                    "insert into JEvents (date,id_tevents) values (?,?)",
                    arrayOf(Date.text, id_event)
                )

                Date.setText("")
                Spin.setSelection(0)

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }


    }

    fun onReportsOfEvents(view: View) {

        try {

            var ExDir = externalMediaDirs[0].toString()
            var f = ExDir + File.separator + "JAP.db"

            var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

            var c = db.rawQuery("""
                SELECT
                    strftime('%m-%d %H:%M', JPressure.date) as date,
                    JPressure.u,
                    JPressure.d,
                    JPressure.p,
                    strftime('%H:%M', JEvents.date) as date_event,
                    TEvents.name as name_event
                FROM JPressure
                    left join JEvents  
                        on date(JPressure.date, "start of day") = date(JEvents.date, "start of day")
                    left join TEvents
                        on JEvents.id_tevents = TEvents.id
                order by JPressure.date desc
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

                arr_tr.add( TableRow(this) )

            }

            c.close()
            db.close()

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }

    }

    fun Translit(s : String): String {
        var m = mapOf<String,String>(
            "А" to "A",
            "Б" to "B",
            "В" to "V",
            "Г" to "G",
            "Д" to "D",
            "Е" to "E",
            "Ё" to "YO",
            "Ж" to "ZH",
            "З" to "Z",
            "И" to "I",
            "Й" to "Y",
            "К" to "K",
            "Л" to "L",
            "М" to "M",
            "Н" to "N",
            "О" to "O",
            "П" to "P",
            "Р" to "R",
            "С" to "S",
            "Т" to "T",
            "У" to "U",
            "Ф" to "F",
            "Х" to "KH",
            "Ц" to "TS",
            "Ч" to "CH",
            "Ш" to "SH",
            "Щ" to "SCH",
            "Ъ" to "-",
            "Ы" to "Y",
            "Ь" to "'",
            "Э" to "E",
            "Ю" to "YU",
            "Я" to "YA"

        )

        var r = ""
        for (i in s){
         val new =m.get(i.toString().toUpperCase())
            if (new==null) {
             r += i
         } else {
             r += new
         }
        }

        return r
    }

    fun SafePressureTable(){
        try {

            var ExDir = externalMediaDirs[0].toString()
            var f = ExDir + File.separator + "JAP.db"

            var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

            var c = db.rawQuery("""
                SELECT
                    strftime('%Y-%m-%d', JPressure.date) as Дата,
                    strftime('%H:%M', JPressure.date) as Время,
                    JPressure.u as Вер,
                    JPressure.d as Ниж,
                    JPressure.p as Пул,
                    strftime('%H:%M', JDrugs.date) as Прием,
                    Drugs.name as Препарат
                FROM JPressure
                    left join JDrugs  
                        on date(JPressure.date, "start of day") = date(JDrugs.date, "start of day")
                    left join Drugs
                        on JDrugs.id_drugs = Drugs.id
                order by JPressure.date desc
            """, null)


            val table = findViewById<TableLayout>(R.id.tableReportPressure)

            var str_header=""
            var str=""

            while (c.moveToNext()) {

                if (str_header.isEmpty()){
                    for (cur_col in c.columnNames)
                        str_header += Translit( cur_col) + ","
                }

                for (cur_col in c.columnNames){

                    str += Translit( c.getString(c.getColumnIndex(cur_col)) ).replace(",",".") + ","


                }

                str += "\n"

            }

            c.close()
            db.close()

            val save_file = ExDir + File.separator + "ЖурналАД.csv"
            val end_srt =  str_header + "\n" + str
            File(save_file).writeText(String(end_srt.toByteArray(), charset("UTF-8")), Charsets.UTF_8)
            Toast.makeText(this, save_file, Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }
    }

    fun SafeFeelingTable(){
        try {

            var ExDir = externalMediaDirs[0].toString()
            var f = ExDir + File.separator + "JAP.db"

            var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

            var c = db.rawQuery("""
                select JEvents.date as Дата, TEvents.name as Событие, 0 as Изменения
                from JEvents
                left join TEvents
                on JEvents.id_tevents=TEvents.id
                union
                select Feeling.date, "", Feeling.change
                from Feeling
            """, null)


            val table = findViewById<TableLayout>(R.id.tableReportPressure)

            var str_header=""
            var str=""

            while (c.moveToNext()) {

                if (str_header.isEmpty()){
                    for (cur_col in c.columnNames)
                        str_header += Translit( cur_col) + ","
                }
                for (cur_col in c.columnNames){
                    str += Translit( c.getString(c.getColumnIndex(cur_col)) ).replace(",",".") + ","
                }
                str += "\n"
            }

            c.close()
            db.close()

            val save_file = ExDir + File.separator + "ИзмененияСамочувствия.csv"
            val end_srt =  str_header + "\n" + str
            File(save_file).writeText(String(end_srt.toByteArray(), charset("UTF-8")), Charsets.UTF_8)
            Toast.makeText(this, save_file, Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }
    }

    fun onClickSafeToCSV(view: View) {

        SafePressureTable()

        SafeFeelingTable()

    }

    fun onClickAddFeeling(view: View) {
        try {

            val date = findViewById<TextView>(R.id.editDateFeeling)
            val change = findViewById<TextView>(R.id.editFeelingChange)

            if (!(date.text.isEmpty() || change.text.isEmpty())) {

                var ExDir = externalMediaDirs[0].toString()
                var f = ExDir + File.separator + "JAP.db"

                var db = SQLiteDatabase.openDatabase(f, null, SQLiteDatabase.OPEN_READWRITE)

                db.execSQL(
                    "insert into Feeling (date,change) values (?,?)",
                    arrayOf(date.text,change.text)
                )

                db.close()

                change.setText("")

                Toast.makeText(this, "Данные добавлены", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Не все указано", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("err", e.toString())
        }
    }


}