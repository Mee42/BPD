import com.carson.MessageHandler
import com.carson.Utils
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.ActivityType
import sx.blah.discord.handle.obj.IRole
import sx.blah.discord.handle.obj.StatusType
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RateLimitException
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.StringBuilder
import java.util.*
import java.util.Arrays




fun main(args: Array<String>) {
    val client= Utils.buildClient()
    client.changePresence(StatusType.ONLINE,ActivityType.PLAYING,"music")
    val handle = Handle()
    Utils.registerListener(client,handle)
    client.login()
}

class Handle : MessageHandler() {
    override fun onMessage(event: MessageReceivedEvent?) {
        if (event == null) return
        val content = event.message.content.toLowerCase()
        if(content == "-help" || content == "~help"){
            sendMessage(event,"Use `~getrole *role name*` to add or remove a role.\nAvailable roles:\n```\n${getRoles(event)}\n```")
            return
        }
        if(!event.message.mentionsEveryone() &&
            !event.message.mentionsHere() &&
                event.message.mentions.contains(event.client.ourUser)){
            //mentions our user but not @everyone and @here
            sendMessage(event,"Hi, I'm `<@525692419220439060>`. I can assign instrument roles for you, see `~help` for more information.")
        }
        if (!(content.startsWith("~getrole") || content.startsWith("-getrole"))) return
        val name = content.substring("~getrole ".length)
        if (name.isBlank()) return
        //find the role with name name
        val role: IRole? = getRole(event,name)
        val top = event.guild.getRolesByName("Bot")[0]
        val bottom = event.guild.getRolesByName("Member")[0]
        if (role == null) {
            val temp = getRoleList(event).map { Pair(it,getDistance(it,name)) }
//            temp.forEach { println(it.first + ":" + it.second) }
            val suggested = temp.sortedBy { it.second }[0].first
            sendMessage(event,"I can't find role $name, did you mean $suggested?")

            return
        }
        if (role.position >= top.position || role.position <= bottom.position) {
            sendMessage(event,"Role ${role.name} is not an assignable role.")
            return
        }
        try {
            if (event.author.hasRole(role)) {
                event.author.removeRole(role)
                sendMessage(event,"Role removed.")
            } else {
                event.author.addRole(role)
                sendMessage(event,"Role added :+1:")
            }
        }catch(e :MissingPermissionsException){
            sendMessage(event,"Error while assigning role: Missing permissions.")
        }catch(e : DiscordException){
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            val sStackTrace = sw.toString()
            sendMessage(event,"Error while assigning role:```\n$sStackTrace\n```")
        }catch(e :RateLimitException){
            sendMessage(event,"Rate limit reached, please try again in a couple minutes.")
        }

    }

    private fun getRole(event: MessageReceivedEvent, name :String): IRole? {
        return event.guild.roles
            .map {Pair(it,it.name.toLowerCase())}.firstOrNull { it.second == name.toLowerCase() }?.first
    }

    private fun getDistance(x: String, y: String): Int {
        val dp = Array(x.length + 1) { IntArray(y.length + 1) }

        for (i in 0..x.length) {
            for (j in 0..y.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> dp[i][j] = min(
                        dp[i - 1][j - 1] + costOfSubstitution(x[i - 1], y[j - 1]),
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1
                    )
                }
            }
        }

        return dp[x.length][y.length]
    }

    private fun costOfSubstitution(a: Char, b: Char) = if (a == b) 0 else 1

    private fun min(vararg numbers: Int) = Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE)

    private fun getRoles(event: MessageReceivedEvent): String {
        val b = StringBuilder()
        for(role in getRoleList(event)){
            b.append("- ").append(role).append('\n')
        }
        return b.toString()
    }


    private fun getRoleList(event :MessageReceivedEvent) :List<String> {
        val list = mutableListOf<String>()
        val top= event.guild.getRolesByName("Bot")[0]
        val bottom = event.guild.getRolesByName("Member")[0]
        for(role in event.guild.roles){
            if(role.position < top.position && role.position > bottom.position){
                list+=role.name
            }
        }
        return list
    }
}