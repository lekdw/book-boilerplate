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
@Table(name = "item")
@Message
public class Item implements Serializable {
	private static final long serialVersionUID = 2509903378966824879L;

	@Id
	@GeneratedValue
	@Ignore
	@JsonIgnore
	public Long item_id = 0L;
	
	@ManyToOne()
	@JoinColumn(name = "game_id", nullable = false)
	@Ignore
	@JsonIgnore
	public Game game = null;
	
	public int itemId = 0;
	
	public int count = 0;

	public Item() {
	}
}