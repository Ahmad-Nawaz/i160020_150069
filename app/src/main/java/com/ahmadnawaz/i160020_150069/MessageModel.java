package com.ahmadnawaz.i160020_150069;

public class MessageModel {
    String message;
    String image_url;
    String sender_phone;   // used to differentiate the messages of sender and receiver
    byte[] photo; // to deal with loading the picture fastly over the chat
    String voiceurl;
    String message_sent_time;

    public MessageModel(){

    }

    public MessageModel(String message,String image_url,String sender_phone,byte[] photo,String voiceurl,String message_sent_time) {
        this.message = message;
        this.image_url=image_url;
        this.sender_phone=sender_phone;
        this.photo=photo;
        this.voiceurl=voiceurl;
        this.message_sent_time=message_sent_time;
    }

    public String getMessage() {
        return message;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getSender_phone() {
        return sender_phone;
    }

    public byte[] getimage() {
        return photo;
    }

    public String getVoiceurl() {
        return voiceurl;
    }

    public String getMessage_sent_time() {
        return message_sent_time;
    }

    public boolean isSender(String current_phone){

        if(sender_phone!=null && sender_phone.equals(current_phone)){
            return true;
        }
        else{
            return false;
        }
    }


}
