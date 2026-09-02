package first.util.loggerUtil.tunables;

public interface Tunable<T> {
	public LoggedTunable<T> makeTunable(String key);
}
