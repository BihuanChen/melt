package dt.original;

public class Expint 
{
	private static final double MAXIT = 100;
	private static final double EULER = 0.5772156649;
	private static final double FPMIN = 1.0e-30;
	private static final double EPS = 1.0e-7;

	public double exe(int n, double x) 
	{
		int i,ii,nm1;
		double a,b,c,d,del,fact,h,psi,ans;

		nm1=n-1;
		
		if (n < 0 || x < 0.0 || (x==0.0 && (n==0 || n==1))) {
			mlt.test.Profiles.add(0, true);
			throw new RuntimeException("error: n < 0 or x < 0");
		} else 
		{
			mlt.test.Profiles.add(0, false);
			if (n == 0) {
				mlt.test.Profiles.add(1, true);
				ans = Math.exp(-x)/x;
			} else 
			{
				mlt.test.Profiles.add(1, false);
				if (x == 0.0) {
					mlt.test.Profiles.add(2, true);
					ans=1.0/nm1;
				} else 
				{
					mlt.test.Profiles.add(2, false);
					if (x > 1.0) 
					{
						mlt.test.Profiles.add(3, true);
						b=x+n;
						c=1.0/FPMIN;
						d=1.0/b;
						h=d;
						
						for (i=1;i<=MAXIT;i++) 
						{
							mlt.test.Profiles.add(4, true);
							a = -i*(nm1+i);
							b += 2.0;
							d=1.0/(a*d+b);
							c=b+a/c;
							del=c*d;
							h *= del;
							
							if (Math.abs(del-1.0) < EPS) 
							{
								mlt.test.Profiles.add(5, true);
								return h*Math.exp(-x);
							} else {
								mlt.test.Profiles.add(5, false);
							}
						}
						mlt.test.Profiles.add(4, false);
						
						throw new RuntimeException("continued fraction failed in expint");	
					}
					
					else 
					{
						mlt.test.Profiles.add(3, false);
						//ans = (nm1!=0 ? 1.0/nm1 : -Math.log(x)-EULER);
						if (nm1!=0) {
							mlt.test.Profiles.add(6, true);
							ans = 1.0/nm1;
						} else {
							mlt.test.Profiles.add(6, false);
							ans = -Math.log(x)-EULER;
						}
						fact=1.0;
						
						for (i=1;i<=MAXIT;i++) {
							mlt.test.Profiles.add(7, true);
							fact *= -x/i;
							
							if (i != nm1) {
								mlt.test.Profiles.add(8, true);
								del = -fact/(i-nm1);
							} else 
							{
								mlt.test.Profiles.add(8, false);
								psi = -EULER;
								
								for (ii=1;ii<=nm1;ii++) {
									mlt.test.Profiles.add(9, true);
									psi += 1.0/ii;
								}
								mlt.test.Profiles.add(9, false);
								
								del = fact*(-Math.log(x)+psi);
							}
							
							ans += del;
							
							if (Math.abs(del) < Math.abs(ans)*EPS) 
							{
								mlt.test.Profiles.add(10, true);
								return ans;
							} else {
								mlt.test.Profiles.add(10, false);
							}
						}
						mlt.test.Profiles.add(7, false);
						throw new RuntimeException("series failed in expint");
					}
				}
			}
		}
		return ans;
	}
	
	public static void main(String[] args) {
		new Expint().exe(2, 3.0);
	}
}

