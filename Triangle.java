// Triangle class
// defines a Triangle shape

import javax.vecmath.*;

public class Triangle extends Shape {
	private Vector3f p0, p1, p2;	// three vertices make a triangle
	private Vector3f n0, n1, n2;	// normal at each vertex

	public Triangle() {
	}
	public Triangle(Vector3f _p0, Vector3f _p1, Vector3f _p2, Material mat) {
		p0 = new Vector3f(_p0);
		p1 = new Vector3f(_p1);
		p2 = new Vector3f(_p2);
		material = mat;
		Vector3f normal = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		v1.sub(p1, p0);
		v2.sub(p2, p0);
		normal.cross(v1, v2);
		normal.normalize();				// compute default normal:
		n0 = new Vector3f(normal);		// the normal of the plane defined by the triangle
		n1 = new Vector3f(normal);
		n2 = new Vector3f(normal);
	}
	public Triangle(Vector3f _p0, Vector3f _p1, Vector3f _p2,
					Vector3f _n0, Vector3f _n1, Vector3f _n2,
					Material mat) {
		p0 = new Vector3f(_p0);
		p1 = new Vector3f(_p1);
		p2 = new Vector3f(_p2);
		material = mat;
		n0 = new Vector3f(_n0);		// the normal of the plane defined by the triangle
		n1 = new Vector3f(_n1);
		n2 = new Vector3f(_n2);
	}
	public HitRecord hit(Ray ray, float tmin, float tmax) {
		Vector3f P1P0 = new Vector3f(p1.x-p0.x,p1.y-p0.y,p1.z-p0.z);	// P1P0 = p1 - p0
		Vector3f P2P0 = new Vector3f(p2.x-p0.x,p2.y-p0.y,p2.z-p0.z);	// P2P0 = p2 - p0
		Vector3f plane_normal = new Vector3f();
		plane_normal.cross(P1P0, P2P0);									// plane_normal = (p1-p0)x(p2-p0)
		plane_normal.normalize();										// plane_normal
		Shape s = new Plane(p0, plane_normal, material);				// Make plane
		HitRecord rec = s.hit(ray, tmin, tmax);							// Check for intersection w/ plane
		if(rec != null){												// Found intersection w/ plane
			Vector3f D = ray.d;											// D = ray direction
			Vector3f P2P1 = new Vector3f(p2.x-p1.x,p2.y-p1.y,p2.z-p1.z);				// P2P1 = p2 - p1
			Vector3f P2O = new Vector3f(p2.x-ray.o.x,p2.y-ray.o.y,p2.z-ray.o.z);		// P2O = p2 - ray origin
			Vector3f cross = new Vector3f();
			cross.cross(P2P0, P2P1);						// cross = (p2-p0)x(p2-p1)
			float determinate = D.dot(cross);				// determinate = D dot (p2-p0)x(p2-p1)
			if(determinate == 0)
				return null;								// No solution
			float determinate1 = P2O.dot(cross);			// determinate1 = (p2 - ray origin) dot (p2-p0)x(p2-p1)
			float t = determinate1/determinate;				// t = determinate1/determinate
			cross.cross(P2O, P2P1);							// cross = (P2 - ray origin)x(p2-p1)
			determinate1 = D.dot(cross);					// determinate1 = D dot (P2 - ray origin)x(p2-p1)
			float alpha = determinate1/determinate;			// alpha = determinate1/determinate
			cross.cross(P2P0, P2O);							// cross = (p2-p0)x(p2 - ray origin)
			determinate1 = D.dot(cross);					// determinate1 = D dot (p2-p0)x(p2 - ray origin)
			float beta = determinate1/determinate;			// beta = determinate1/determinate
			if(alpha < 0 || beta < 0 || alpha + beta > 1 || t < 0)
				return null;
			// normal =  alpha * n0 + beta * n1 + (1-alpha-beta) * n2
			Vector3f norm = (new Vector3f(alpha*n0.x + beta*n1.x + (1.0f-alpha-beta)*(n2.x), alpha*n0.y + beta*n1.y + (1.0f-alpha-beta)*(n2.y), alpha*n0.z + beta*n1.z + (1.0f-alpha-beta)*(n2.z)));
			norm.normalize();								// normalize
			rec.normal = norm;								// Set normal
			return rec;										// Return HitRecord
		}
		return null;										// No intersection w/ plane
	}
}