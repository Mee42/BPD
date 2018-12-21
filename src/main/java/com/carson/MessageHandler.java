package com.carson;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;

public abstract class MessageHandler {
    private IDiscordClient client;

    public MessageHandler() {

    }

    public void setClient(IDiscordClient client) {
        this.client = client;
    }

    @EventSubscriber
    public void onMessageRecivedEvent(MessageReceivedEvent event){
       onMessage(event);
    }

    public abstract void onMessage(MessageReceivedEvent event);



    public void sendMessage(IChannel channel, String content){
        RequestBuffer.request(() -> channel.sendMessage(content));
    }
    public void sendMessage(MessageReceivedEvent event, String content){
        sendMessage(event.getChannel(),content);
    }
    public IMessage sendMessageAndGet(IChannel channel, String content){
        return RequestBuffer.request(() -> channel.sendMessage(content)).get();
    }






}
