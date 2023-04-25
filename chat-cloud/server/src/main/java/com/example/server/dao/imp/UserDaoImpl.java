package com.example.server.dao.imp;

import com.example.clientfx.pojo.ChatGroup;
import com.example.clientfx.pojo.Group_Msg;
import com.example.clientfx.pojo.Personal_Msg;
import com.example.common.pojo.User;
import com.example.server.dao.UserDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {
  private Connection connection =null;

  public UserDaoImpl(){
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "xujian123";
        try{
        this.connection = DriverManager.getConnection(url,user,password);
        }catch (Exception e){
            System.out.println("error in connecting database");
        }
    }

  public static void main(String[] args) {
        new UserDaoImpl();
    }

    /**
     * select user by name
     * @param id
     * @return User
     */
  @Override
  public User selectUserById(Integer id){
        try (PreparedStatement preparedStatement =
                     this.connection.prepareStatement("select * from users where userid = ?")) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userid"));
                user.setUserName(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setState(resultSet.getInt("state"));
                user.setAddr(resultSet.getString("addr"));
                user.setPort(resultSet.getString("port"));
                return user;
            }
        } catch (SQLException e) {
            // 处理异常
            e.printStackTrace();
        }
        return null;
  }

  @Override
  public synchronized User selectUserByName(String userName){
        try (PreparedStatement preparedStatement =
                     this.connection.prepareStatement("SELECT * FROM users where username = ?")) {
            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userid"));
                user.setUserName(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setState(resultSet.getInt("state"));
                user.setAddr(resultSet.getString("addr"));
                user.setPort(resultSet.getString("port"));
                return user;
            }
        } catch (SQLException e) {
            // 处理异常
            e.printStackTrace();
        }
        return null;
    }

  @Override
  public User selectByAndrAndPort(String addr,String port){
        try (PreparedStatement preparedStatement =
                     this.connection.prepareStatement("select * from users where addr = ? and port = ?")) {
            preparedStatement.setString(1, addr);
            preparedStatement.setString(2,port);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userid"));
                user.setUserName(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setState(resultSet.getInt("state"));
                user.setAddr(resultSet.getString("addr"));
                user.setPort(resultSet.getString("port"));
                return user;
            }
        } catch (SQLException e) {
            // 处理异常
            e.printStackTrace();
        }
        return null;
  }

    /**
     * update user status
     * @param userName
     * @return void
     */
  @Override
  public synchronized void updateStatus(String userName,String addr,String port){
        try{
            String sql =  "UPDATE users SET state = ? , addr = ?, port = ?  WHERE userName = ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setInt(1,1);
            stmt.setString(2,addr);
            stmt.setString(3,port);
            stmt.setString(4 ,userName);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("update state success "+userName);
        }catch (SQLException e){
            e.printStackTrace();
        }
  }
  @Override
  public void updateOffline(String name){
        try{
            String sql =  "UPDATE users SET state = ?  WHERE userName = ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setInt(1,0);
            stmt.setString(2,name);
            int rowsAffected = stmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
  }

  @Override
  public synchronized void userOffLine(String userName){
        try{
            String sql =  "UPDATE users SET state = 0 WHERE userName = ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1,userName);
            int rowsAffected = stmt.executeUpdate();
            System.out.println(userName+" is offline");
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
    /**
     * select user who status is 1
     */
  @Override
  public List<User> activateUser(){
        List<User> users = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     this.connection.prepareStatement("SELECT * FROM users where state = 1")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userid"));
                user.setUserName(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password"));
                user.setState(resultSet.getInt("state"));
                user.setAddr(resultSet.getString("addr"));
                user.setPort(resultSet.getString("port"));
                users.add(user);
            }
        } catch (SQLException e) {
            // 处理异常
            e.printStackTrace();
        }
        return users;
    }

  @Override
  public synchronized void register(String userName,String password){
        try{
            String sql = "INSERT INTO users (userName, password) VALUES (?, ?)";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1,userName);
            stmt.setString(2,password);
            int row = stmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

  @Override
  public List<ChatGroup> getGroup(Integer userId){
        List<ChatGroup> groups = new ArrayList<>();
        try (PreparedStatement preparedStatement =
                     this.connection.prepareStatement("SELECT * FROM group_chat where memberid = ?")) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ChatGroup chatGroup = new ChatGroup();
                chatGroup.setGroupName(resultSet.getString("groupname"));
                chatGroup.setId(resultSet.getInt("id"));
                chatGroup.setMemberId(resultSet.getInt("memberid"));
                groups.add(chatGroup);
            }
            return groups;
        } catch (SQLException e) {
            // 处理异常
            e.printStackTrace();
        }
        return null;
    }

  @Override
  public void insertGroupMsg(Group_Msg groupMsg){
        try{
            String sql = "insert into group_msg (groupname, username, content, timestamp) VALUES (?,?,?,?)";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1,groupMsg.getGroupName());
            stmt.setString(2,groupMsg.getUserName());
            stmt.setString(3,groupMsg.getContent());
            stmt.setLong(4,groupMsg.getTimeStamp());
            int row = stmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

  @Override
  public void insertPersonalMsg(Personal_Msg personalMsg){
        try{
            String sql = "insert into personal_msg ( sendby, sendto, timestamp, content) VALUES (?,?,?,?)";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1,personalMsg.getSendBy());
            stmt.setString(2,personalMsg.getSendTo());
            stmt.setString(4,personalMsg.getContent());
            stmt.setLong(3,personalMsg.getTimeStamp());
            int row = stmt.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

  @Override
  public List<Personal_Msg> personalMsgRecode(String userName,String chatWith){
            List<Personal_Msg> pmList = new ArrayList<>();
        try{
            String sql = "select * from personal_msg where sendby = ? and sendto = ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1,userName);
            stmt.setString(2,chatWith);
            ResultSet resultSet  = stmt.executeQuery();
            while(resultSet.next()){
                Personal_Msg pm = new Personal_Msg();
                pm.setContent(resultSet.getString("content"));
                pm.setTimeStamp(resultSet.getLong("timestamp"));
                pm.setSendBy(resultSet.getString("sendby"));
                pm.setSendTo(resultSet.getString("sendto"));
                pmList.add(pm);
            }
            return pmList;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

  @Override
  public List<Group_Msg> groupMsgRecode(String groupName){
        List<Group_Msg> gmList = new ArrayList<>();
        try{
            String sql = "select * from group_msg where groupname = ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1,groupName);
            ResultSet resultSet  = stmt.executeQuery();
            while(resultSet.next()){
                Group_Msg gm = new Group_Msg();
                gm.setGroupName(resultSet.getString("groupname"));
                gm.setUserName(resultSet.getString("username"));
                gm.setContent(resultSet.getString("content"));
                gm.setTimeStamp(resultSet.getLong("timestamp"));
                gmList.add(gm);
            }
            return gmList;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

  @Override
  public  List<ChatGroup> selectGroupMember(String groupName){
        List<ChatGroup> cgList = new ArrayList<>();
        try{
            String sql = "select * from group_chat where groupname = ?";
            PreparedStatement stmt = this.connection.prepareStatement(sql);
            stmt.setString(1,groupName);
            ResultSet resultSet  = stmt.executeQuery();
            while(resultSet.next()){
                ChatGroup cg = new ChatGroup();
                cg.setGroupName(resultSet.getString("groupname"));
                cg.setMemberId(resultSet.getInt("memberid"));
                cgList.add(cg);
            }
            return cgList;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

  @Override
  public void createGroup(Integer id,String groupName){
       try{
           String sql = "insert into group_chat (groupname, memberid) VALUES (?,?)";
           PreparedStatement stmt = this.connection.prepareStatement(sql);
           stmt.setString(1,groupName);
           stmt.setInt(2,id);
           int row = stmt.executeUpdate();
       }catch (SQLException e){
           e.printStackTrace();
       }
   }

  @Override
  public void init(){
       try{
           String sql =  "update users set state = 0 where state != 0";
           PreparedStatement stmt = this.connection.prepareStatement(sql);
           System.out.println("init....");
           int rowsAffected = stmt.executeUpdate();
       }catch (SQLException e){
           e.printStackTrace();
       }
    }
}
