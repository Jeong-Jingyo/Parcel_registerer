package com.laondruk.parcel

import android.content.Context
import androidx.room.*
import com.laondruk.parcel.MainActivity.Companion.dataSearchExecutor

@Database(entities = [Student::class], version = 1)
abstract class StudentDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
}

private var Instance: StudentDatabase? = null
fun getInstance(context: Context): StudentDatabase {
    dataSearchExecutor.run {
        if (Instance == null) {
            Instance =
                Room.databaseBuilder(context, StudentDatabase::class.java, "Student")
                    .createFromAsset("database/Student.db")
                    .build()
        }
    }
    return Instance!!
}

@Dao
interface StudentDao {
    @Query("Select * FROM Student")
    fun getAll(): List<Student>

    @Query("SELECT * FROM Student WHERE Name LIKE (:character) || '%'")
    fun findBy1stChar(character: String): List<Student>

    @Query("SELECT * FROM Student WHERE Name LIKE '_' || (:character) || '%'")
    fun findBy2ndChar(character: String): List<Student>

    @Query("SELECT * FROM Student WHERE Name LIKE '__' || (:character) || '%'")
    fun findBy3rdChar(character: String): List<Student>

    @Query("SELECT * FROM Student WHERE Name LIKE '___' || (:character) || '%'")
    fun findBy4thChar(character: String): List<Student>
}

@Entity(tableName = "Student")
data class Student(
    @PrimaryKey val PK: Int,
    @ColumnInfo(name = "Grade") val Grade: Int,
    @ColumnInfo(name = "Klass") val Klass: Int,
    @ColumnInfo(name = "Number") val Number: Int,
    @ColumnInfo(name = "Name") val Name: String,
    @ColumnInfo(name = "NameAnnotation") val NameAnnotation: Char? = null
)