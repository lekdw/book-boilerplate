package common.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.msgpack.annotation.Ignore;
import org.msgpack.annotation.Message;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("deprecation")
@Entity
@Table(name = "game")
@Message
public class Game implements Serializable {
	private static final long serialVersionUID = 8047627225787560045L;

	@Id
	@GeneratedValue
	@Ignore
	@JsonIgnore
	public long game_id = 0L;

	// 채널 아이디
	@Index(name="idx_channelId")
	@Ignore
	public String channelId = "";
	
	// 이름
	@Ignore
	public String nickName = "";
	
	// 마켓
	@Ignore
	public int marketId = 0;
	
	// 푸쉬 아이디
	@Ignore
	public String pushId = "";

	// 로그인 블록 상태
	public int loginBlock = 0;

	// 메시지 블록 상태
	public int mailBlock = 0;

	// 푸쉬 블록 상태
	public int pushBlock = 0;

	// 운영 보상 아이디
	public int lastRewardId = 0;
	
	// 플레이한 스테이지
	public int selStageId = 0;

	// 루비
	public int ruby = 0;

	// 코인
	public int coin = 0;

	// 하트
	public int heart = 0;
	
	// 마지막 하트를 업데이트 했던 시간
	// <!> 하트 업데이트 시 현재 시간에서 heartTime을 뺀 시간동안 채워질 코인를 계산한다.
	public long heartTime = 0L;
	
	// 친구 초대 횟수
	public int invite = 0;
		
	// 연속 출석 카운트
	public int attendCount = 0;
	
	// 연속 출석 카운트를 업데이트한 시간
	@Ignore
	public long attendTime = 0L;
	
	// 스테이지 관련
	@MapKey(name = "stage_id")
	@OneToMany(mappedBy = "game", cascade = {CascadeType.ALL}, orphanRemoval = true)
	public Map<Long, Stage> stages = new HashMap<Long, Stage>();
	
	// 생성 시간
	@Ignore
	public Date createDate = null;

	// 로그인 시간
	@Ignore
	public Date loadDate = null;

	// 시작 시간
	@Ignore
	public Date startDate = null;

	// 저장 시간
	@Ignore
	public Date saveDate = null;

	// 탈퇴 시간
	@Ignore
	public Date unregisterDate = null;
	
	@Transient
	@Ignore
	@JsonIgnore
	public Map<Long, Mail> mails = new HashMap<Long, Mail>();
	
	public Game() {
	}
}