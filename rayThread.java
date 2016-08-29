import java.util.Vector;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;


public class rayThread extends Thread {
	Color3f[][] image;
	Camera camera;
	Color3f background;
	private Vector<Light> lights;
	private Vector<Shape> shapes;
	int depth;
	int i,j;
	float x,y,off_x,off_y;
	
	public rayThread(Color3f[][] im, Camera c, Color3f bg, Vector<Light> l, Vector<Shape> s, int d, int I, int J, float X, float Y, float off_X, float off_Y) {
		image = im;
		camera = c;
		lights = l;
		background = bg;
		shapes = s;
		depth = d;
		i = I;
		j = J;
		x = X;
		y = Y;
		off_x = off_X;
		off_y = off_Y;
	}
	
	boolean intersectScene(Ray ray, HitRecord hit) {
		float tmax = Float.MAX_VALUE;
		boolean hit_something = false;
		for (Shape s : shapes)
		{
			HitRecord temp_hit = s.hit(ray, 0.001f, tmax);
			if (temp_hit != null)
			{
				tmax = temp_hit.t;
				hit.set(temp_hit);
				hit_something = true;
			}
		}
		return hit_something;
	}
	
	public Color3f raytracing(Ray ray, int depth) {
		HitRecord hit  = new HitRecord();
		if(intersectScene(ray, hit)){
			return getShadingColour(hit, ray, depth);
		}
		return background;
	}
	
	Color3f getShadingColour(HitRecord hit, Ray ray, int depth) {
		int maxDepth = 5;																// maxDepth = 5
		Color3f c = new Color3f(0,0,0);
		Vector3f lightPos = new Vector3f();
		Vector3f lightDir = new Vector3f();
		Color3f I = lights.firstElement().getLight(hit.pos, lightPos, lightDir);		// Get intensity, position, & direction of 1st light
		
		//////////////
		// REFLECTION/REFRACTION
		/////////
		Color3f Kr = hit.material.Kr;													// Get reflective factor
		Color3f Kt = hit.material.Kt;													// Get refracting factor
		Vector3f R = reflect(lightDir, hit.normal);										// Calculate reflection vector
		Vector3f negLightDir = new Vector3f((-1)*lightDir.x, (-1)*lightDir.y, (-1)*lightDir.z); // Calculate negated reflection vector
		Ray refl_ray = new Ray(hit.pos, R);												// Shoot reflection ray from hit point
		Vector3f RT = refract(negLightDir, hit.normal, hit.material.ior);				// Calculate refraction vector
		Ray refr_ray = new Ray();
		if(RT == null)
			return new Color3f(0,0,0); 
		refr_ray = new Ray(hit.pos, RT);											// Shoot refraction ray from hit point
		if ((Kr.x != 0 || Kr.y != 0 || Kr.z != 0 || Kt.x != 0 || Kt.y != 0 || Kt.z != 0)&& depth < maxDepth){
			Color3f refl = raytracing(refl_ray, depth+1);								// Recurse for reflection
			c.add(new Color3f(Kr.x*refl.x, Kr.y*refl.y, Kr.z*refl.z));					// c += Kr*Reflection_Colour
			Color3f refr = raytracing(refr_ray, depth+1);								// Recurse for refraction
			c.add(new Color3f(Kt.x*refr.x, Kt.y*refr.y, Kt.z*refr.z));					// c += Kr*Reflection_Colour
		}
		
		//////////////
		// AMBIENCE
		/////////
		float Kar = hit.material.Ka.x; float Kag = hit.material.Ka.y; float Kab = hit.material.Ka.z; 		// Ambience factor
		float Kdr = hit.material.Kd.x; float Kdg = hit.material.Kd.y; float Kdb = hit.material.Kd.z; 		// Diffuse factor
		float Ksr = hit.material.Ks.x; float Ksg = hit.material.Ks.y; float Ksb = hit.material.Ks.z; 		// Specular factor
		float Ir = I.x; float Ig = I.y; float Ib = I.z; 											 		// Light Intensity
		Color3f ambient = new Color3f(Ir*Kar, Ig*Kag, Ib*Kab);										 		// Calculate ambience
		Color3f diffuse = new Color3f(0,0,0);														 		// Initialize diffuse
		Color3f specular = new Color3f(0,0,0);														 		// Initialize specular
		
		for(Light l: lights){
			I = l.getLight(hit.pos, lightPos, lightDir);											 		// Get light intensity, lightPos, & lightDir
			Ir = I.x; Ig = I.y; Ib = I.z;																	// Light intensity components
			//////////////
			// SHADOWING
			/////////
			Ray sr = new Ray(hit.pos, lightDir);																	// Shoot shadow ray from hit point
			Vector3f temp = new Vector3f(lightPos.x - hit.pos.x, lightPos.y - hit.pos.y, lightPos.z - hit.pos.z);	// Calculate distance
			float dist2Light = temp.length();																		// of hit point to light
			HitRecord shadowHit = new HitRecord();																	// Create new HitRecord
			if(intersectScene(sr, shadowHit) && shadowHit.t < dist2Light)											// Check for intersection &
				continue;																							// shadow if shadow intersection is closer
			
			//////////////
			// DIFFUSE
			/////////
			float NdotL = Math.max(0, hit.normal.dot(lightDir));						// Calculate N dot L
			diffuse.add(new Color3f(Ir*Kdr*NdotL, Ig*Kdg*NdotL, Ib*Kdb*NdotL));			// diffuse += I*Kd*(NdotL)
			
			//////////////
			// SPECULAR
			/////////
			R = reflect(lightDir, hit.normal);											// Calculate reflection vector
			Vector3f V = new Vector3f((-1)*ray.d.x, (-1)*ray.d.y, (-1)*ray.d.z);
			float RdotV = Math.max(0, lightDir.dot(V));									// Calculate R dot V 						
			float RdotV_P = (float) Math.pow((double)RdotV,(double)hit.material.phong_exp);
			//for(float i=2; i<=hit.material.phong_exp; i++)								// Calculate
			//	RdotV_P = RdotV_P * RdotV;												// (R dot V)^p
			specular.add(new Color3f(Ir*Ksr*RdotV_P, Ig*Ksg*RdotV_P, Ib*Ksb*RdotV_P));	// specular += I*Ks*(RdotV)^p	
		}
		//float shading_r = Math.min(1, ambient.x+diffuse.x+specular.x);					 
		//float shading_g = Math.min(1, ambient.y+diffuse.y+specular.y);
		//float shading_b = Math.min(1, ambient.z+diffuse.z+specular.z);
		float shading_r = ambient.x+diffuse.x+specular.x;					 
		float shading_g = ambient.y+diffuse.y+specular.y;
		float shading_b = ambient.z+diffuse.z+specular.z;
		c.add(new Color3f(shading_r, shading_g, shading_b));
		return c;
	}
	
	// reflect a direction (in) around a given normal
	/* NOTE: dir is assuming to point AWAY from the hit point
	 * if your ray direction is point INTO the hit point, you should flip
	 * the sign of the direction before calling reflect
	 */
	private Vector3f reflect(Vector3f dir, Vector3f normal)
	{
		Vector3f out = new Vector3f(normal);
		out.scale(2.f * dir.dot(normal));
		out.sub(dir);
		return out;
	}

	// refract a direction (in) around a given normal and 'index of refraction' (ior)
	/* NOTE: dir is assuming to point INTO the hit point
	 * (this is different from the reflect function above, which assumes dir is pointing away)
	 */
	private Vector3f refract(Vector3f dir, Vector3f normal, float ior)
	{
		float mu;
		mu = (normal.dot(dir) < 0) ? 1.f / ior : ior;

		float cos_thetai = dir.dot(normal);
		float sin_thetai2 = 1.f - cos_thetai*cos_thetai;

		if (mu*mu*sin_thetai2 > 1.f) return null;
		float sin_thetar = mu*(float)Math.sqrt(sin_thetai2);
		float cos_thetar = (float)Math.sqrt(1.f - sin_thetar*sin_thetar);

		Vector3f out = new Vector3f(normal);
		if (cos_thetai > 0)
		{
			out.scale(-mu*cos_thetai+cos_thetar);
			out.scaleAdd(mu, dir, out);

		} else {

			out.scale(-mu*cos_thetai-cos_thetar);
			out.scaleAdd(mu, dir, out);
		}
		out.normalize();
		return out;
	}
	
	public void run() {
		/////////////////
		// Anti-aliasing
		///////////
		Color3f p0 = raytracing(camera.getCameraRay(x, y), 0);
		p0.scale((float)1/(float)9);
		Color3f p1 = raytracing(camera.getCameraRay(x - off_x, y), 0);
		p1.scale((float)1/(float)9);
		Color3f p2 = raytracing(camera.getCameraRay(x + off_x, y), 0);
		p2.scale((float)1/(float)9);
		Color3f p3 = raytracing(camera.getCameraRay(x, y + off_y), 0);
		p3.scale((float)1/(float)9);
		Color3f p4 = raytracing(camera.getCameraRay(x, y - off_y), 0);
		p4.scale((float)1/(float)9);
		Color3f p5 = raytracing(camera.getCameraRay(x + off_x, y - off_y), 0);
		p5.scale((float)1/(float)9);
		Color3f p6 = raytracing(camera.getCameraRay(x - off_x, y - off_y), 0);
		p6.scale((float)1/(float)9);
		Color3f p7 = raytracing(camera.getCameraRay(x + off_x, y + off_y), 0);
		p7.scale((float)1/(float)9);
		Color3f p8 = raytracing(camera.getCameraRay(x - off_x, y + off_y), 0);
		p8.scale((float)1/(float)9);
		Color3f aa = new Color3f(0,0,0);
		aa.add(p0); aa.add(p1); aa.add(p2); aa.add(p3); aa.add(p4); aa.add(p5); aa.add(p6); aa.add(p7); aa.add(p8);
		image[i][j] = aa;
	}
}
