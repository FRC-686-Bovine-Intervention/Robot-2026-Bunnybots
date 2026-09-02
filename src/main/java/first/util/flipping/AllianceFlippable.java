package first.util.flipping;

import first.util.flipping.AllianceFlipUtil.FieldFlipType;

public interface AllianceFlippable<T> {
	public T flip(FieldFlipType flipType);
	public default T flip() {
		return this.flip(AllianceFlipUtil.defaultFlipType);
	}
}
