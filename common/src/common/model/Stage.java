package common.model;

import java.io.Serializable;

import org.msgpack.annotation.Message;

@Message
public class Stage implements Serializable {
	private static final long serialVersionUID = -7729190692509410266L;

	public int stageId = 0;
	
	public int state = 0;
	
	public int topScore = 0;

	public Stage() {
	}
}