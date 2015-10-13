package dt.original;

public class Triangle
{
	public int exe(int a, int b, int c)  
	{
		if (a > b) 
		{ mlt.test.Profiles.add(0, true);
		int tmp = a; a = b; b = tmp; } else {
			mlt.test.Profiles.add(0, false);
		}

		if (a > c) 
		{ mlt.test.Profiles.add(1, true);
		int tmp = a; a = c; c = tmp; } else {
			mlt.test.Profiles.add(1, false);
		}

		if (b > c) 
		{ mlt.test.Profiles.add(2, true);
		int tmp = b; b = c; c = tmp; } else {
			mlt.test.Profiles.add(2, false);
		}

		if(c >= a+b) {
			mlt.test.Profiles.add(3, true);
			return 1;
		} else
		{
			mlt.test.Profiles.add(3, false);
			if(a == b && b == c) {
				mlt.test.Profiles.add(4, true);
				return 4;
			} else {
				mlt.test.Profiles.add(4, false);
				if(a == b  || b == c) {
					mlt.test.Profiles.add(5, true);
					return 3;
				} else {
					mlt.test.Profiles.add(5, false);
					return 2;
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new Triangle().exe(3, 4, 5);
	}
}