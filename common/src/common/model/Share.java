package common.model;

import java.io.Serializable;

import org.msgpack.annotation.Message;

// key의 형식은 "share:채널아이디"를 갖는다. 
@Message
public class Share implements Serializable {
	private static final long serialVersionUID = 1850004294053307560L;

	public int selStageId = 0;
	
	public int prevWeekTopScore = 0;

	public long prevWeekTime = 0L;

	public int lastWeekTopScore = 0;
	
	public long lastWeekTime = 0L;
	
	public Share() {
	}
}