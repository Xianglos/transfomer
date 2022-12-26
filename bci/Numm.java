package bci;

public class Numm {

	public Numm(int init) {
		this.num = init;
	}

	private int num;

	public int getNum() 
	{

		return num;
	}

	public void setNum(int num) 
	{

		this.num = num;
	}

	public int add(int input) {
		return num += input;
	}

	public int add(String str) {
		return num += 10;
	}

	@Override
	public String toString() {
		return "Numm [num=" + num + "]";
	}

//	public int minus(int input) {
//		return num -= input;
//	}

}
