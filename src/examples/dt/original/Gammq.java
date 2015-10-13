package dt.original;
public class Gammq {
	private static final int ITMAX = 100;
	private static final double EPS = 3.0e-7;
	private static final double FPMIN = 1.0e-30;
	
	private double gamser, gammcf, gln;
	
	private double gammln(double xx) {
		
		double x,y,tmp,ser;
		
		double cof[]  = {76.18009172947146,-86.50532032941677,24.01409824083091,-1.231739572450155,0.1208650973866179e-2,-0.5395239384953e-5};
		
		int j;
	
		y=x=xx;
		tmp=x+5.5;
		tmp -= (x+0.5)*Math.log(tmp);
		ser=1.000000000190015;
		for (j=0;j<=5;j++) {
			mlt.test.Profiles.add(0, true);
			ser += cof[j]/++y;
		}
		mlt.test.Profiles.add(0, false);
		return -tmp+Math.log(2.5066282746310005*ser/x);
	}

	private void gcf(double a, double x)
	{
		int i;
		double an,b,c,d,del,h;

		gln=gammln(a);
		b=x+1.0-a;
		c=1.0/FPMIN;
		d=1.0/b;
		h=d;
		for (i=1;i<=ITMAX;i++) {
			mlt.test.Profiles.add(1, true);
			an = -i*(i-a);
			b += 2.0;
			d=an*d+b;
			if (Math.abs(d) < FPMIN) {
				mlt.test.Profiles.add(2, true);
				d=FPMIN;
			} else {
				mlt.test.Profiles.add(2, false);
			}
			c=b+an/c;
			if (Math.abs(c) < FPMIN) {
				mlt.test.Profiles.add(3, true);
				c=FPMIN;
			} else {
				mlt.test.Profiles.add(3, false);
			}
			d=1.0/d;
			del=d*c;
			h *= del;
			if (Math.abs(del-1.0) < EPS) {
				mlt.test.Profiles.add(4, true);
				break;
			} else {
				mlt.test.Profiles.add(4, false);
			}
		}
		mlt.test.Profiles.add(1, false);
		if (i > ITMAX) {
			mlt.test.Profiles.add(5, true);
			throw new RuntimeException ("a too large, ITMAX too small in gcf");
		} else {
			mlt.test.Profiles.add(5, false);
		}
		gammcf=Math.exp(-x+a*Math.log(x)-gln)*h;
	}

	private void gser(double a, double x) {
	
		int n;
		double sum,del,ap;

		gln=gammln(a);
	
		if (x <= 0.0) {
			mlt.test.Profiles.add(6, true);
			if (x < 0.0) {
				mlt.test.Profiles.add(7, true);
				throw new RuntimeException ("x less than 0 in routine gser");
			} else {
				mlt.test.Profiles.add(7, false);
			}
			gamser=0.0;
			return;
		} 
		else {
			mlt.test.Profiles.add(6, false);
			ap=a;
			del=sum=1.0/a;
			for (n=1;n<=ITMAX;n++) {
				mlt.test.Profiles.add(8, true);
				++ap;
				del *= x/ap;
				sum += del;
				if (Math.abs(del) < Math.abs(sum)*EPS) {
					mlt.test.Profiles.add(9, true);
					gamser=sum*Math.exp(-x+a*Math.log(x)-gln);
					return;
				} else {
					mlt.test.Profiles.add(9, false);
				}
			}
			mlt.test.Profiles.add(8, false);
			throw new RuntimeException ("a too large, ITMAX too small in routine gser");
		}
	}

	public double exe(double a, double x) {
		if (x < 0.0 || a <= 0.0) {
			mlt.test.Profiles.add(10, true);
			throw new RuntimeException("Invalid arguments in routine gammq");
		} else {
			mlt.test.Profiles.add(10, false);
		}
		if (x < (a+1.0)) {
			mlt.test.Profiles.add(11, true);
			gser(a,x);
			return 1-gamser;
		} 
		else {
			mlt.test.Profiles.add(11, false);
			gcf(a,x);
			return gammcf;
		}
	}
	
	public static void main(String[] args) {
		new Gammq().exe(2, 2);
	}

}

