package io.icw.pow;

import java.util.List;
import java.util.ArrayList;

public class Test {

	public static void main(String[] args) {
		List test = new ArrayList();
		test.add(1);
		test.add(2);
		test.add(3);
		test.add(4);
		test.add(5);
		test.add(6);
		test.add(7);
		test.add(8);
		test.add(9);
		test.add(10);
		
		System.out.println(test);
		
		List test2 = new ArrayList();
		test2.add(11);
		test2.add(12);
		test2.add(13);
		test2.add(14);
		test2.add(15);
		test2.add(16);
		test.addAll(test2.subList(0, 15 - test.size()));
		
		System.out.println(test);
	}

}
