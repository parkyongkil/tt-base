package tt.lang;

import tt.lang.string.builder.TStringBuilder;

public class TObject {

	@Override
	public String toString() {
		TStringBuilder sb = new TStringBuilder();
		sb.write(this);
		return sb.buildString();
	}
}
