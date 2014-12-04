package common.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Message;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "stage")
@Message
public class Stage implements Serializable {
	private static final long serialVersionUID = -7729190692509410266L;

	@Id
	@GeneratedValue
	@Ignore
	@JsonIgnore
	public long stage_id = 0L;
	
	@ManyToOne()
	@JoinColumn(name = "game_id", nullable = false)
	@Ignore
	@JsonIgnore
	public Game game = null;
	
	public int stageId = 0;
	
	public int state = 0;
	
	public int topScore = 0;

	public Stage() {
	}
}