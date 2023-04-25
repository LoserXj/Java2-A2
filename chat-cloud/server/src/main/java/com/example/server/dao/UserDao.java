package com.example.server.dao;

import com.example.clientfx.pojo.ChatGroup;
import com.example.clientfx.pojo.Group_Msg;
import com.example.clientfx.pojo.Personal_Msg;
import com.example.common.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserDao {

    User selectUserById(Integer id);
    User selectUserByName(String userName);
    void updateStatus(String userNam,String addr,String port);
    List<User> activateUser();
    void userOffLine(String userName);

    List<ChatGroup> getGroup(Integer userId);
    void register(String userName,String password);

    void insertGroupMsg(Group_Msg groupMsg);

    void insertPersonalMsg(Personal_Msg personalMsg);

    List<Personal_Msg> personalMsgRecode(String userName,String chatWith);

    List<Group_Msg> groupMsgRecode(String groupName);

    List<ChatGroup> selectGroupMember(String groupName);

    void createGroup(Integer id,String groupName);

    void updateOffline(String name);

    User selectByAndrAndPort(String addr,String port);

    void init();
}
