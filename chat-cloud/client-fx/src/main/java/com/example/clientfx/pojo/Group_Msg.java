package com.example.clientfx.pojo;
import java.io.Serializable;
import lombok.Data;


/**
 * .群聊聊天记录
 */
@Data
public class Group_Msg implements Serializable {
  /**
     *.发送者
      */
  private String userName;
  /**
     *.群聊名字
      */
  private String groupName;
  /**
     *.内容
     */
  private String content;
  /**
     *.时间戳
     */
  private long timeStamp;
}
