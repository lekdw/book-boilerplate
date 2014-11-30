package common;

public interface AppCouchbaseHandler {
	public void onCouchbaseStart();
	public void onCouchbaseStop();
	public void onCouchbaseError();
}