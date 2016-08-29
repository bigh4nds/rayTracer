// Sphere class
// defines a Sphere shape

import javax.vecmath.*;

public class Sphere extends Shape {
	private Vector3f center;	// center of sphere
	private float radius;		// radius of sphere

	public Sphere() {
	}
	public Sphere(Vector3f pos, float r, Material mat) {
		center = new Vector3f(pos);
		radius = r;
		material = mat;
	}
	public HitRecord hit(Ray ray, float tmin, float tmax) {
		float A = ray.d.dot(ray.d);					// A = |D|^2
		Vector3f OC = ray.o;						// OC = ray origin
		OC.sub(center);								// OC = ray origin - sphere center
		float B = 2*OC.dot(ray.d);					// B = 2*(ray origin - sphere center) dot (ray direction)
		float C = OC.dot(OC) - radius*radius;		// (ray origin - sphere center) dot (ray origin - sphere center) - (radius)^2
		float discriminant = B*B - 4*A*C;			// discriminant = B^2 - 4*A*C
		float t = 0, t1 = -1, t2 = -1;				
		if(discriminant > 0 && A != 0){				// Check for solutions
			t1 = ((-1)*B + discriminant)/(2*A);		// Calculate solution 1
			t2 = ((-1)*B - discriminant)/(2*A);		// Calculate solution 2
		}
		else
			return null;							// No solution
		
		if(t1 >= 0 && (t1 < tmin || t1 > tmax))
			t = t1;									// t1>=0 && in range
		if(t2 >= 0 && (t2 < tmin || t2 > tmax))
			t = t2;									// t2>=0 && in range
		if(t1 >= 0 && t2 >=0 && (t1 < tmin || t1 > tmax) && (t2 < tmin || t2 > tmax))
			t = Math.min(t1,t2);					// both are >=0 && in range => find min
		
		HitRecord rec = new HitRecord();			// Create hitRecord
		rec.pos = ray.pointAt(t);					// position of hit point
		rec.t = t;									// parameter t (distance along the ray)
		rec.material = material;					// material
		Vector3f N = ray.pointAt(t);				// N = ray point(t)
		N.sub(center);								// N = ray point(t) - sphere center
		N.normalize();								// normalize
		rec.normal = N;								// normal at the hit point	
		return rec;
	}
}
