package common.storage;

public interface AppMySQLHandler {
	public void onMySQLStart();
	public void onMySQLStop();
	public void onMySQLError();
}