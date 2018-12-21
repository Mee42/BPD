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
            sendMessage(event,"Use ~getrole *role name* to add or remove a role\navailable roles\n```\n${getRoles(event)}\n```")
            return
        }
        if(!event.message.mentionsEveryone() &&
            !event.message.mentionsHere() &&
                event.message.mentions.contains(event.client.ourUser)){
            //mentions our user but not @everyone and @here
            sendMessage(event,"Hi, I'm `<@mention>`. I can assign instrument roles for you, see `~help` for more information")
        }
        if (!(content.startsWith("~getrole") || content.startsWith("-getrole"))) return
        var name = content.substring("~getrole".length)
        if (name.isBlank()) return
        val chars = name.toCharArray()
        if (chars.isEmpty()) return
        chars[0] = chars[0].toUpperCase()
        name = String(chars)
        //find the role with name name
        val role: IRole? = event.guild.getRolesByName(name).getOrNull(0)
        val top = event.guild.getRolesByName("Bot")[0]
        val bottom = event.guild.getRolesByName("Member")[0]
        if (role == null) {
            sendMessage(event,"I can't find role $role")
            return
        }
        if (role.position >= top.position || role.position <= bottom.position) {
            sendMessage(event,"Role $role is not an assignable role")
            return
        }
        try {
            if (event.author.hasRole(role))
                event.author.removeRole(role)
            else
                event.author.addRole(role)
        }catch(e :MissingPermissionsException){
            sendMessage(event,"Error while assigning role: Missing permissions")
        }catch(e : DiscordException){
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            val sStackTrace = sw.toString()
            sendMessage(event,"Error while assigning role:```\n$sStackTrace\n```")
        }catch(e :RateLimitException){
            sendMessage(event,"Rate limit reached, please try again in a couple minutes")
        }

    }

    private fun getRoles(event: MessageReceivedEvent): String {
        val b = StringBuilder()
        val top= event.guild.getRolesByName("Bot")[0]
        val bottom = event.guild.getRolesByName("Member")[0]
        for(role in event.guild.roles){
            if(role.position < top.position && role.position > bottom.position){
                b.append("- ").append(role.name).append('\n')
            }
        }
        return b.toString()
    }
}