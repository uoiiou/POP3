package rlogin.server;

import java.util.ArrayList;
import java.util.List;

public class UsersList{
    private List<User> list;

    public UsersList(){
        list = new ArrayList<>();
    }

    public void addUser(String name, String pass){
        User user = new User();
        user.setUsername(name);
        user.setPassword(pass);
        list.add(user);
    }

    public List<User> getList(){
        return list;
    }
}

