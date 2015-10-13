package dt.original;
public class Remainder
{
	public int exe(int a, int b)
	{
		int r = 0-1;
		int cy = 0;
		int ny = 0;

		if (a==0) {
			mlt.test.Profiles.add(0, true);
			;
		} else {
			mlt.test.Profiles.add(0, false);
			if (b==0) {
				mlt.test.Profiles.add(1, true);
				;
			} else {
				mlt.test.Profiles.add(1, false);
				if (a>0) {
					mlt.test.Profiles.add(2, true);
					if (b>0) {
						mlt.test.Profiles.add(3, true);
						while((a-ny)>=b)
						{
							mlt.test.Profiles.add(4, true);
							ny=ny+b;
							r=a-ny;
							cy=cy+1;
						}
						mlt.test.Profiles.add(4, false);
					} else {
						mlt.test.Profiles.add(3, false);
						while((a+ny)>=Math.abs(b))
						//while((a+ny)>= ((b>=0) ? b : -b))
						{
							mlt.test.Profiles.add(5, true);
							ny=ny+b;
							r=a+ny;
							cy=cy-1;
						}
						mlt.test.Profiles.add(5, false);
					}
				} else {
					mlt.test.Profiles.add(2, false);
					if (b>0) {
						mlt.test.Profiles.add(6, true);
						while(Math.abs(a+ny)>=b)
						//while( ((a+ny)>=0 ? (a+ny) : -(a+ny))   >=b)
						{
							mlt.test.Profiles.add(7, true);
							ny=ny+b;
							r=a+ny;
							cy=cy-1;
						}
						mlt.test.Profiles.add(7, false);
					} else {
						mlt.test.Profiles.add(6, false);
						while(b>=(a-ny))
						{
							mlt.test.Profiles.add(8, true);
							ny=ny+b;
							r=Math.abs(a-ny);
							//r= ((a-ny)>=0 ? (a-ny) : -(a-ny));
							cy=cy+1;
						}
						mlt.test.Profiles.add(8, false);
					}
				}
			}
		}
		return r;
	}
	
	public static void main(String[] args) {
		new Remainder().exe(10, 5);
	}
}