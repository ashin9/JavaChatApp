package ServerApp.Model.users;

import ServerApp.ServerMain;

import java.net.Socket;
import java.util.Map;

/*UserIfo类
用以储存客户端name与相对应客户端socket的集合
 */

public class UsersInfo {
    // 存放客户端socket静态HashMap
    public static Map<String, Socket> storeInfo;
    // 将客户端的信息以Map形式存入集合中

    public synchronized static void putUser(String key, Socket value) {
            //添加一个新的 客户端名 与 服务端相连的socket 的键值对
            UsersInfo.storeInfo.put(key, value);
    }

    // 将给定的输出流从共享集合中删除
    public synchronized static void removeUser(String key) {
        UsersInfo.storeInfo.remove(key);
    }

    /*
    得到用户列表字符串，形式为：
    @UserList@: + "\n当前在线人数：" +"在线人数" + username1+"\n" + ...
    其中@UserList@为标识
     */
    public synchronized static String getUsersList(){
        StringBuffer users = new StringBuffer("@UsersList@:"+"当前在线人数："+UsersInfo.storeInfo.size()+"\n\n");
        for(String user : UsersInfo.storeInfo.keySet()){
            users.append(user+UsersInfo.storeInfo.get(user).getInetAddress()+"\n\n");
        }
        return users.toString();
    }
}
