package rlogin.server;

import java.util.ArrayList;
import java.util.List;

public class User{
    private String username;
    private String password;
    private List<Message> messages;

    public User(){
        username = "";
        password = "";
        messages = new ArrayList<>();
    }

    public void setUsername(String name){
        username = name;
    }

    public String getUsername(){
        return username;
    }

    public void setPassword(String pass){
        password = pass;
    }

    public String getPassword(){
        return password;
    }

    public void setMessage(String string, int msize){
        Message message = new Message();
        message.setMessage(string);
        message.setMsize(msize);
        messages.add(message);
    }

    public String getMessage(int index){
        return messages.get(index).getMessage();
    }

    public int getMessageSize(int index){
        return messages.get(index).getMsize();
    }

    public List<Message> getMessages(){
        return messages;
    }

    public void deletemessage(int index){
        messages.remove(index);
    }
}

