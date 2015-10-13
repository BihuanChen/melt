package dt.original;
/*
 double fisher(m, n, x)
int m, n;
double x;
  {
  int a, b, i, j;
  double w, y, z, zk, d, p;
  a = 2*(m/2)-m+2;
  b = 2*(n/2)-n+2;
  w = (x*m)/n;
  z = 1.0/(1.0+w);
  if(a == 1)
    {
    if(b == 1)
      {
      p = sqrt(w);
      y = 0.3183098862;
      d = y*z/p;
      p = 2.0*y*atan(p);
      }
    else
      {
      p = sqrt(w*z);
      d = 0.5*p*z/w;
      }
    }
  else if(b == 1)
    {
    p = sqrt(z);
    d = 0.5*z*p;
    p = 1.0-p;
    }
  else
    {
    d = z*z;
    p = w*z;
    }
  y = 2.0*w/z;
  if(a == 1)
    for(j = b+2; j <= n; j += 2)
      {
      d *= (1.0+1.0/(j-2))*z;
      p += d*y/(j-1);
      }
  else
    {
    zk = pow(z, (double)((n-1)/2));
    d *= (zk*n)/b;
    p = p*zk+w*z*(zk-1.0)/(z-1.0);
    }
  y = w*z;
  z = 2.0/z;
  b = n-2;
  for(i = a+2; i <= m; i += 2)
    {
    j = i+b;
    d *= (y*j)/(i-2);
    p -= z*d/j;
    }
  return(p<0.0? 0.0: p>1.0? 1.0: p);
  } 
 */


public class Fisher
{
	public double exe(int m, int n, double x)
	{
		int a, b, i, j;
		double w, y, z, zk, d, p;
		
		a = 2*(m/2)-m+2;
		b = 2*(n/2)-n+2;
		w = (x*m)/n;
		z = 1.0/(1.0+w);
		
		if(a == 1)
		{
			mlt.test.Profiles.add(0, true);
			if(b == 1)
			{
				mlt.test.Profiles.add(1, true);
				p = Math.sqrt(w);
				y = 0.3183098862;
				d = y*z/p;
				p = 2.0*y*Math.atan(p);
			}
			else
			{
				mlt.test.Profiles.add(1, false);
				p = Math.sqrt(w*z);
				d = 0.5*p*z/w;
			}
		} else {
			mlt.test.Profiles.add(0, false);
			if(b == 1)
			{
				mlt.test.Profiles.add(2, true);
				p = Math.sqrt(z);
				d = 0.5*z*p;
				p = 1.0-p;
			}
			else
			{
				mlt.test.Profiles.add(2, false);
				d = z*z;
				p = w*z;
			}
		}
		
		y = 2.0*w/z;
		
		if(a == 1) {
			mlt.test.Profiles.add(3, true);
			for(j = b+2; j <= n; j += 2)
			{
				mlt.test.Profiles.add(4, true);
				d *= (1.0+1.0/(j-2))*z;
				p += d*y/(j-1);
			}
			mlt.test.Profiles.add(4, false);
		} else
		{
			mlt.test.Profiles.add(3, false);
			zk = Math.pow(z, (double)((n-1)/2));
			d *= (zk*n)/b;
			p = p*zk+w*z*(zk-1.0)/(z-1.0);
		}
		
		y = w*z;
		z = 2.0/z;
		b = n-2;
		for(i = a+2; i <= m; i += 2)
		{
			mlt.test.Profiles.add(5, true);
			j = i+b;
			d *= (y*j)/(i-2);
			p -= z*d/j;
		}
		mlt.test.Profiles.add(5, false);
		
		if(p<0.0) {
			mlt.test.Profiles.add(6, true);
			return 0.0;
		} else {
			mlt.test.Profiles.add(6, false);
			if(p>1.0) {
				mlt.test.Profiles.add(7, true);
				return 1.0;
			} else {
				mlt.test.Profiles.add(7, false);
				return p;
			}
		}
	}
	
	public static void main(String[] args) {
		new Fisher().exe(2, 2, 3);
	}
}
