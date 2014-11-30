package common;

public interface AppRedisHandler {
	public void onRedisStart();
	public void onRedisStop();
	public void onRedisError();
	public void onRedisMessage(String channel, String message);
}