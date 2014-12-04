package common.storage;

public interface AppCouchbaseHandler {
	public void onCouchbaseStart();
	public void onCouchbaseStop();
	public void onCouchbaseError();
}