



public class Quaternion 
{
	
	double W, X, Y, Z;
	double tempW, tempX, tempY, tempZ;
	double[] rotoationMatrix = new double[9];
	
	public Quaternion()
	{
		loadAxisAngle(0,1,0,0);
	}
	
	public Quaternion(double w, double x, double y, double z)
	{
		W = w;
		X = x;
		Y = y;
		Z = z;
	}
	
	Quaternion(double x, double y, double z)
	{
		loadOmega(x, y, z);
	}

	//Careful... This method does not normalize the axis provided.
	public void loadAxisAngle(double x, double y, double z, double angle)
	{
	    double cos_a = Math.cos( angle / 2.0f );
	    double sin_a = Math.sin( angle / 2.0f );
	    
	    W = cos_a;
	    X = x * sin_a;
	    Y = y * sin_a;
	    Z = z * sin_a;
	}
	
	//Nifty experiment 20100111 
	//SLOOOOOOOWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW: (Divisons & Square-root)
	public void loadOmega(double omegaX, double omegaY, double omegaZ)
	{
		double mag = Math.sqrt(omegaX*omegaX + omegaY*omegaY + omegaZ*omegaZ); 

		double x = omegaX/mag;
		double y = omegaY/mag;
		double z = omegaZ/mag;
		
		loadAxisAngle(x, y, z, mag);
	}
	
	public void scale(double factor)
	{
		 W *= .5;
		 X *= .5;
		 Y *= .5;
		 Z *= .5;
	}
	
	// the statement (q1*q2 != q2*q1) is in most cases true;
	public Quaternion times (Quaternion quat)
	{
		return	new Quaternion(	W*quat.W - X*quat.X - Y*quat.Y - Z*quat.Z,
								X*quat.W + W*quat.X - Z*quat.Y + Y*quat.Z,
								Y*quat.W + Z*quat.X + W*quat.Y - X*quat.Z,
								Z*quat.W - Y*quat.X + X*quat.Y + W*quat.Z);
	}
	
	public void times (Quaternion quat, Quaternion store)
	{
		tempW = W*quat.W - X*quat.X - Y*quat.Y - Z*quat.Z;
		tempX = X*quat.W + W*quat.X - Z*quat.Y + Y*quat.Z;
		tempY = Y*quat.W + Z*quat.X + W*quat.Y - X*quat.Z;
		tempZ = Z*quat.W - Y*quat.X + X*quat.Y + W*quat.Z;
		
		store.W = tempW;
		store.X = tempX;
		store.Y = tempY;
		store.Z = tempZ;
	}

	// Convert to Axis/Angles
	public double getAxisAngle(double[] axis)
	{
		double scale = Math.sqrt(X * X + Y * Y + Z * Z);
		axis[0] = X / scale;
		axis[1] = Y / scale;
		axis[2] = Z / scale;
		return Math.acos(W) * 2.0f;
	}
	
	public Quaternion getInverse()
	{
		Quaternion q = new Quaternion(W, -X, -Y, -Z);
		q.normalize();
		return q;
	}
	
	public void loadRotationMatrix()
	{
	    normalize(); //Already normalized?

		double xx      = X * X,
			   xy      = X * Y,
			   xz      = X * Z,
			   xw      = X * W,
		
			   yy      = Y * Y,
			   yz      = Y * Z,
			   yw      = Y * W,
		
			   zz      = Z * Z,
			   zw      = Z * W;
		
		rotoationMatrix[0]  = 1.0f - 2.0f * ( yy + zz );
		rotoationMatrix[1]  =		 2.0f * ( xy - zw );
		rotoationMatrix[2]  =		 2.0f * ( xz + yw );
		
		rotoationMatrix[3]  =     2.0f * ( xy + zw );
		rotoationMatrix[4]  = 1.0f - 2.0f * ( xx + zz );
		rotoationMatrix[5]  =     2.0f * ( yz - xw );
		
		rotoationMatrix[6]  =     2.0f * ( xz - yw );
		rotoationMatrix[7]  =     2.0f * ( yz + xw );
		rotoationMatrix[8]  = 1.0f - 2.0f * ( xx + yy );
	}
	
	public void normalize()
	{
		double mag = Math.sqrt(W*W + X*X + Y*Y + Z*Z);
		W /= mag;
		X /= mag;
		Y /= mag;
		Z /= mag;
	}
	
	public void transform(double[] original, double[] store)
	{
		store[0] = original[0]*rotoationMatrix[0] + original[1]*rotoationMatrix[1] + original[2]*rotoationMatrix[2];
		store[1] = original[0]*rotoationMatrix[3] + original[1]*rotoationMatrix[4] + original[2]*rotoationMatrix[5];
		store[2] = original[0]*rotoationMatrix[6] + original[1]*rotoationMatrix[7] + original[2]*rotoationMatrix[8];
	}
	
	public void transformList(double[] vertList, double[] store, int numVerts)
	{
		for(int i = 0; i < numVerts; i++)
		{
			store[i*3+0] = vertList[i*3+0]*rotoationMatrix[0] + vertList[i*3+1]*rotoationMatrix[1] + vertList[i*3+2]*rotoationMatrix[2];
			store[i*3+1] = vertList[i*3+0]*rotoationMatrix[3] + vertList[i*3+1]*rotoationMatrix[4] + vertList[i*3+2]*rotoationMatrix[5];
			store[i*3+2] = vertList[i*3+0]*rotoationMatrix[6] + vertList[i*3+1]*rotoationMatrix[7] + vertList[i*3+2]*rotoationMatrix[8];
		}
	}
}
