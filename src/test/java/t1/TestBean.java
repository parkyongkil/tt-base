package t1;

import lombok.Data;

@Data
public class TestBean {

	String userId;
	String userName;

	public static void main(String[] args) {
		TestBean b = new TestBean();
		b.getUserId();
		b.setUserId("박용길");
		b.setUserName("서울도시가스");
		
		System.out.println(b);
	}
}
