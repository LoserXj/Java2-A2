package com.example.clientfx.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * .传输对象
 */
@Data
public class Message implements Serializable {
  /**
     * .数据类型
      */
  private String type;
  /**
     * .用户名
     */
  private String userName;
  /**
     * .密码
     */
  private String password;
  /**
     * .接收者
     */
  private String sendTo;
  /**
     * .数据类型
     */
  private List<String> userList = new ArrayList<>();
  /**
     * .数据类型
     */
  private List<String> groupList = new ArrayList<>();
  /**
     * .数据类型
     */
  private String statusCode;
  /**
     * .数据类型
     */
  private String errorInfo;
  /**
     * .数据类型
     */
  private List<Group_Msg> GroupMsg = new ArrayList<>();
  /**
     * .数据类型
     */
  private List<Personal_Msg> PersonalMsg = new ArrayList<>();
  /**
     * .数据类型
     */
  private String groupName;
  /**
     * .数据类型
     */
  private boolean needReminded = false;
  /**
     * .数据类型
     */
  public Message(){
  }

}
