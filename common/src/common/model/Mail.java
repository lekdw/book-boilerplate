package common.model;

import java.io.Serializable;

import org.msgpack.annotation.Message;

// key의 형식은 "mail:채널아이디:타임스탬프"를 갖는다. 
@Message
public class Mail implements Serializable {
	private static final long serialVersionUID = -3483326931968686716L;

	public long timestamp = 0L;
	
	public String from = "";
	
	public int type = 0;
	
	public int rewardType = 0;
	
	public int rewardId = 0;
	
	public int rewardCount = 0;
	
	public Mail() {
	}
}