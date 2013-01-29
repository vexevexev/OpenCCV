
public class Spring //with damper ;)
{
	public double springConstant = 8.0;
	public double springLength = 0;
	public double frictionConstant = 3;
	
	public Spring(double constant, double length, double friction)
	{
		springConstant = constant;
		springLength = length;
		frictionConstant = friction;
	}
	
	public boolean getForce(double[] pA, double[] pB, double[] storeForce)
	{
		double[] springVector = new double[3];
		Math3D.sub3x1minus3x1(pA, pB, springVector);		
		double r = Math3D.magnitude(springVector);
		
		if (r >= .01)
		{
			storeForce[0] = (springVector[0] / r) * (r - springLength) * (-springConstant);
			storeForce[1] = (springVector[1] / r) * (r - springLength) * (-springConstant);
			storeForce[2] = (springVector[2] / r) * (r - springLength) * (-springConstant);
			return true;
		}
		return false;
	}
}

class Math3D
{
	public static void sub3x1minus3x1(double[] va, double[] vb, double[] store)
	{
		store[0] = va[0] - vb[0];
		store[1] = va[1] - vb[1];
		store[2] = va[2] - vb[2];
	}

	public static double magnitude(double[] v)
	{
		return Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
	}
}