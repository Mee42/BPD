import com.google.gson.GsonBuilder
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.StringBuilder



fun getLatestHash() :String {
    val random = Math.random().times(10000).toString()
    val args = arrayOf("/bin/bash", "-c", "curl -o /tmp/commit-$random.txt https://api.github.com/repos/mee42/bpd/commits/master")
    val proc = ProcessBuilder(*args).start()
    proc.waitFor()
    val buffer = BufferedReader(FileReader(File("/tmp/commit-$random.txt")))
    val b = StringBuilder()
    while(true) b.append(buffer.readLine()?: break)
    val obj = GsonBuilder().create().fromJson(b.toString(),ShaContainer::class.java)
    return obj.sha
}

class ShaContainer(val sha: String)

fun main(args: Array<String>) {
    println(getLatestHash())
}