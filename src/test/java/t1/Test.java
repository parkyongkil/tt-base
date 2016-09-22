package t1;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class Test {

	public static void main(String[] args) {

		A3 a3 = new A3();
		p("A3-1", a3.s1, a3.s2, a3.s3);
		a3.s1 = "3";
		a3.s2 = "3";
		a3.s3 = "3";
		p("A3-2", a3.s1, a3.s2, a3.s3);

		A2 a2 = (A2) a3;
		p("A2-1", a2.s1, a2.s2, 0);
		a2.s1 = "2";
		a2.s2 = "2";
		//		a2.setS1("2");
		//		a2.setS2("2");
		p("A2-2", a2.s1, a2.s2, 0);

		A1 a1 = (A1) a2;
		p("A1-1", a1.s1, 0, 0);
		a1.s1 = "1";
		// a1.setS1("1");
		p("A1-2", a1.s1, 0, 0);
		
		p("A1-2", a3.s1, a3.s2, a3.s3);
		
		A3 a4 = (A3) a1;
		p("A1-2", a4.s1, a4.s2, a4.s3);
	}

	static void p(Object... oa) {
		//		ArrayList<String> ss = new ArrayList<String>();
		//		for (Object o : oa)
		//			ss.add(ToStringBuilder.reflectionToString(o));
		String s = StringUtils.join(oa, " | ");
		System.out.println(s);
	}

	@Data
	static class A1 {
		String s1 = "A";
		//String s2 = "A";
		//String s3 = "A";
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	static class A2 extends A1 {
		//String s1 = "B";
		String s2 = "B";
		//String s3 = "B";
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	static class A3 extends A2 {
		//String s1 = "C";
		//String s2 = "C";
		String s3 = "C";
	}
}
