package dt.original;
public class Triangle2
{
	public int exe(int a, int b, int c)  
	{
		if(a<=0 || b<=0 || c<=0)
		{
			mlt.test.Profiles.add(0, true);
			return 1;// was 4
		}
		else {
			mlt.test.Profiles.add(0, false);
		}
		
		int tmp = 0;
		
		if(a==b) {
			mlt.test.Profiles.add(1, true);
			tmp = tmp + 1;
		} else {
			mlt.test.Profiles.add(1, false);
		}
		
		if(a==c) {
			mlt.test.Profiles.add(2, true);
			tmp = tmp + 2;
		} else {
			mlt.test.Profiles.add(2, false);
		}
		
		if(b==c) {
			mlt.test.Profiles.add(3, true);
			tmp = tmp + 3;
		} else {
			mlt.test.Profiles.add(3, false);
		}
		
		if(tmp == 0)
		{
			mlt.test.Profiles.add(4, true);
			if((a+b<=c) || (b+c <=a) || (a+c<=b)) {
				mlt.test.Profiles.add(5, true);
				tmp = 1; //was 4
			}
			else {
				mlt.test.Profiles.add(5, false);
				tmp = 2; //was 1
			}
			return tmp;
		} else {
			mlt.test.Profiles.add(4, false);
		}
		
		if(tmp > 3) {
			mlt.test.Profiles.add(6, true);
			tmp = 4;// was 3;
		}
		else {
			mlt.test.Profiles.add(6, false);
			if(tmp==1 && (a+b>c)) {
				mlt.test.Profiles.add(7, true);
				tmp = 3; // was 2
			}
			else {
				mlt.test.Profiles.add(7, false);
				if(tmp==2 && (a+c>b)) {
					mlt.test.Profiles.add(8, true);
					tmp = 3; // was 2
				}
				else {
					mlt.test.Profiles.add(8, false);
					if(tmp==3 && (b+c>a)) {
						mlt.test.Profiles.add(9, true);
						tmp = 3; // was 2
					}
					else {
						mlt.test.Profiles.add(9, false);
						tmp = 1; // was 4
					}
				}
			}
		}
		
		return tmp;
	}
	
	public static void main(String[] args) {
		new Triangle2().exe(3, 4, 5);
	}
}