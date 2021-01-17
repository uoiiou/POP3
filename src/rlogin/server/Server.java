package rlogin.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private static UsersList usersList;
    private static String uname = "", pword = "";
    private static int mcount = 0, msize = 0, index = 0;
    private static Vector<Integer> deleteitems = new Vector();

    public static void main(String[] argv){
        try {
            usersList = new UsersList();
            usersList.addUser("Name", "Surname");
            usersList.getList().get(0).setMessage("KYKY", 103);
            usersList.getList().get(0).setMessage("AAAA", 10);

            ServerSocket serverSocket = new ServerSocket(110);
            Socket socket = serverSocket.accept();
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            dataOutputStream.writeUTF("+OK POP3 server ready");

            String command = "";
            while(!command.equals("QUIT"))
            {
                command = dataInputStream.readUTF();
                switch (getcommand(command)){
                    case "USER":{
                        String username = getdata(command);
                        if (!username.equals(""))
                        {
                            if (finduserinlist(username))
                            {
                                uname = username;
                                dataOutputStream.writeUTF("+OK " + username + " user found");
                            }
                            else
                            {
                                uname = "";
                                dataOutputStream.writeUTF("-ERR sorry, no mailbox for " + username + " here");
                            }
                        }
                        else
                        {
                            uname = "";
                            dataOutputStream.writeUTF("-ERR sorry, you did not enter a username");
                        }

                        break;
                    }
                    case "PASS":{
                        if (!uname.equals(""))
                        {
                            String password = getdata(command);
                            if (!password.equals(""))
                            {
                                if (findpassinlist(uname, password))
                                {
                                    pword = password;
                                    mcount = findcountmessage(uname, pword);
                                    msize = findsizemessage(uname, pword);
                                    index = findindexuser(uname, pword);
                                    dataOutputStream.writeUTF("+OK " + uname + "'s maildrop has " + mcount +
                                            " messages (" + msize + " bytes)");
                                    System.out.println(index);
                                }
                                else
                                {
                                    uname = "";
                                    pword = "";
                                    dataOutputStream.writeUTF("-ERR sorry, incorrect password");
                                }
                            }
                            else
                            {
                                uname = "";
                                pword = "";
                                dataOutputStream.writeUTF("-ERR sorry, you did not enter a password");
                            }
                        }
                        else
                            dataOutputStream.writeUTF("-ERR sorry, you did not enter a username");

                        break;
                    }
                    case "QUIT":{
                        dataOutputStream.writeUTF("+OK POP3 server shutting down");
                        command = "QUIT";
                        deletemessages();
                        break;
                    }
                    case "STAT":{
                        if ((!uname.equals("")) && (!pword.equals("")))
                        {
                            mcount = findcountmessage(uname, pword);
                            msize = findsizemessage(uname, pword);
                            dataOutputStream.writeUTF("+OK " + mcount + " " + msize);
                        }
                        else
                            dataOutputStream.writeUTF("-ERR sorry, you didn't enter username or password");

                        break;
                    }
                    case "LIST":{
                        if ((!uname.equals("")) && (!pword.equals("")))
                        {
                            mcount = findcountmessage(uname, pword);
                            msize = findsizemessage(uname, pword);
                            String result = getmessages(index, mcount);
                            dataOutputStream.writeUTF("+OK " + mcount + " messages (" + msize + " bytes)\n" + result + '.');
                        }
                        else
                            dataOutputStream.writeUTF("-ERR sorry, you didn't enter username or password");

                        break;
                    }
                    case "NOOP":{
                        dataOutputStream.writeUTF("+OK");
                        break;
                    }
                    case "RETR":{
                        if ((!uname.equals("")) && (!pword.equals("")))
                        {
                            int messagenumb = getmessagenumb(command);
                            if (messagenumb <= mcount)
                            {
                                dataOutputStream.writeUTF("+OK " + usersList.getList().get(index).getMessageSize(messagenumb - 1) +
                                        '\n' + usersList.getList().get(index).getMessage(messagenumb - 1) + '\n' + '.');
                            }
                            else
                                dataOutputStream.writeUTF("-ERR sorry, message number " + messagenumb + " not found");
                        }
                        else
                            dataOutputStream.writeUTF("-ERR sorry, you didn't enter username or password");

                        break;
                    }
                    case "DELE":{
                        if ((!uname.equals("")) && (!pword.equals("")))
                        {
                            int messagenumb = getmessagenumb(command);
                            if (messagenumb <= mcount)
                            {
                                if ((deleteitems.size() != 0) && (deleteitems.contains(messagenumb - 1)))
                                {
                                    dataOutputStream.writeUTF("-ERR message " + messagenumb + " already deleted");
                                }
                                else
                                {
                                    deleteitems.add(messagenumb - 1);
                                    dataOutputStream.writeUTF("+OK message " + messagenumb + " deleted");
                                }
                            }
                            else
                                dataOutputStream.writeUTF("-ERR sorry, message number " + messagenumb + " not found");
                        }
                        else
                            dataOutputStream.writeUTF("-ERR sorry, you didn't enter username or password");

                        break;
                    }
                    case "": {
                        dataOutputStream.writeUTF("-ERR, command " + command + " does not exist");
                        break;
                    }
                }
            }

            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
            serverSocket.close();
        } catch (IOException ex) {
            System.out.println("Server - " + ex);
        }
    }

    private static String getcommand(String string){
        String result = "";
        int i = 0;

        if (string.length() >= 4)
        {
            while(i < 4)
            {
                if (string.charAt(i) != ' ')
                    result += string.charAt(i++);
                else
                    break;
            }

            if (string.length() > 4)
                if (string.charAt(4) != ' ')
                    result = "";
        }

        return result;
    }

    private static String getdata(String string){
        String result = "";
        int i = 5;

        if (string.length() > 5)
        {
            while (i < string.length())
                result += string.charAt(i++);
        }

        return result;
    }

    private static boolean finduserinlist(String string){
        int i = 0, len = usersList.getList().size();

        while (i < len)
            if (usersList.getList().get(i++).getUsername().equals(string))
                return true;

        return false;
    }

    private static boolean findpassinlist(String user, String pass){
        int i = 0, len = usersList.getList().size();

        while (i < len)
        {
            if (usersList.getList().get(i).getUsername().equals(user))
                if (usersList.getList().get(i).getPassword().equals(pass))
                    return true;
            ++i;
        }

        return false;
    }

    private static int findcountmessage(String user, String pass){
        int i = 0, len = usersList.getList().size(), index = 0;

        while (i < len)
        {
            if (usersList.getList().get(i).getUsername().equals(user))
                if (usersList.getList().get(i).getPassword().equals(pass))
                {
                    index = i;
                    break;
                }
            ++i;
        }

        return usersList.getList().get(index).getMessages().size();
    }

    private static int findsizemessage(String user, String pass){
        int i = 0, len = usersList.getList().size(), index = 0, count, size = 0;

        while (i < len)
        {
            if (usersList.getList().get(i).getUsername().equals(user))
                if (usersList.getList().get(i).getPassword().equals(pass))
                {
                    index = i;
                    break;
                }
            ++i;
        }

        count = usersList.getList().get(index).getMessages().size();

        i = 0;
        while (i < count)
            size += usersList.getList().get(index).getMessages().get(i++).getMsize();

        return size;
    }

    private static int findindexuser(String user, String pass){
        int i = 0, len = usersList.getList().size();

        while (i < len)
        {
            if (usersList.getList().get(i).getUsername().equals(user))
                if (usersList.getList().get(i).getPassword().equals(pass))
                {
                    index = i;
                    break;
                }
            ++i;
        }

        return i;
    }

    private static int getmessagenumb(String string){
        int i = 5, len = string.length();
        String result = "";

        while (i < len)
            result +=  string.charAt(i++);

        return Integer.parseInt(result);
    }

    private static String getmessages(int ind, int count){
        String res = "";
        int i = 0;

        while (i < count)
        {
            res += String.valueOf(usersList.getList().get(ind).getMessages().get(i).getMessage());
            res += ' ';
            res += String.valueOf(usersList.getList().get(ind).getMessages().get(i).getMsize());
            res += '\n';
            ++i;
        }

        return res;
    }

    private static void deletemessages(){
        int n = 0;

        for(int i:deleteitems)
        {
            usersList.getList().get(index).deletemessage(i - n);
            ++n;
        }
    }
}