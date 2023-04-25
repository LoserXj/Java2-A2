package com.example.clientfx.pojo;

import java.io.Serializable;
import lombok.Data;


/**

 .表示一个聊天群组的实体类
 */
@Data
public class ChatGroup implements Serializable {
  /**

     .聊天群组的唯一标识。
     */
  private int id;
  /**

     .聊天群组的名称。
     */
  private String groupName;
  /**

     .聊天群组的成员ID。
     */
  private int memberId;
}
