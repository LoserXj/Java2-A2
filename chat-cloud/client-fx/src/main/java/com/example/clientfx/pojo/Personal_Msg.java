package com.example.clientfx.pojo;


import java.io.Serializable;
import lombok.Data;


/**
 * .个人记录
 */
@Data
public class Personal_Msg implements Serializable {
  /**
     * .个人记录
     */
  private String sendBy;
  /**
     * .个人记录
     */
  private String sendTo;
  /**
     * .个人记录
     */
  private String content;
   /**
     * .个人记录
     */
  private long timeStamp;
}
