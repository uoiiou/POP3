package rlogin.server;

public class Message {
    private String message;
    private int msize;

    public Message(){
        message = "";
        msize = 0;
    }

    public void setMessage(String mess) {
        message = mess;
    }

    public String getMessage() {
        return message;
    }

    public void setMsize(int size) {
        msize = size;
    }

    public int getMsize() {
        return msize;
    }
}

