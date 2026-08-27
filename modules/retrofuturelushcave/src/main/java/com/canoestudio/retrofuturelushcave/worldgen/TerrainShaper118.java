package com.canoestudio.retrofuturelushcave.worldgen;

/** 1.18.2 TerrainShaper/CubicSpline 的运行时精简移植，仅保留 Overworld slopedCheese 所需功能。 */
public final class TerrainShaper118 {
    public static final class Point {
        final double continents, erosion, ridges, weirdness;
        Point(double continents, double erosion, double ridges, double weirdness) {
            this.continents = continents; this.erosion = erosion;
            this.ridges = ridges; this.weirdness = weirdness;
        }
    }

    private interface Coordinate { double value(Point p); }
    private static final Coordinate CONTINENTS = p -> p.continents;
    private static final Coordinate EROSION = p -> p.erosion;
    private static final Coordinate RIDGES = p -> p.ridges;
    private static final Coordinate WEIRDNESS = p -> p.weirdness;

    private final Spline offsetSampler;
    private final Spline factorSampler;
    private final Spline jaggednessSampler;

    public TerrainShaper118() {
        Spline s0 = buildErosionOffsetSpline(-.15, 0, 0, .1, 0, -.03, false, false);
        Spline s1 = buildErosionOffsetSpline(-.1, .03, .1, .1, .01, -.03, false, false);
        Spline s2 = buildErosionOffsetSpline(-.1, .03, .1, .7, .01, -.03, true, true);
        Spline s3 = buildErosionOffsetSpline(-.05, .03, .1, 1, .01, .01, true, true);
        offsetSampler = b(CONTINENTS).p(-1.1,.044,0).p(-1.02,-.2222,0).p(-.51,-.2222,0)
                .p(-.44,-.12,0).p(-.18,-.12,0).p(-.16,s0,0).p(-.15,s0,0).p(-.1,s1,0)
                .p(.25,s2,0).p(1,s3,0).build();
        factorSampler = b(CONTINENTS).p(-.19,3.95,0)
                .p(-.15,getErosionFactor(6.25,true),0).p(-.1,getErosionFactor(5.47,true),0)
                .p(.03,getErosionFactor(5.08,true),0).p(.06,getErosionFactor(4.69,false),0).build();
        jaggednessSampler = b(CONTINENTS).p(-.11,0,0)
                .p(.03,buildErosionJaggednessSpline(1,.5,0,0),0)
                .p(.65,buildErosionJaggednessSpline(1,1,1,0),0).build();
    }

    public double offset(Point p) { return offsetSampler.apply(p) - .50375; }
    public double factor(Point p) { return factorSampler.apply(p); }
    public double jaggedness(Point p) { return jaggednessSampler.apply(p); }

    public static Point point(double continents, double erosion, double weirdness) {
        return new Point(continents, erosion, peaksAndValleys(weirdness), weirdness);
    }

    static double peaksAndValleys(double v) { return -(Math.abs(Math.abs(v)-.6666667)-.33333334)*3; }

    private static Spline buildErosionJaggednessSpline(double a,double b,double c,double d) {
        Spline s0=buildRidgeJaggednessSpline(a,c), s1=buildRidgeJaggednessSpline(b,d);
        return b(EROSION).p(-1,s0,0).p(-.78,s1,0).p(-.5775,s1,0).p(-.375,0,0).build();
    }

    private static Spline buildRidgeJaggednessSpline(double a,double b0) {
        double p0=peaksAndValleys(.4),p1=peaksAndValleys(.56666666),mid=(p0+p1)/2;
        Builder b=b(RIDGES).p(p0,0,0);
        b.p(mid,b0>0?buildWeirdnessJaggednessSpline(b0):Spline.constant(0),0);
        return b.p(1,a>0?buildWeirdnessJaggednessSpline(a):Spline.constant(0),0).build();
    }

    private static Spline buildWeirdnessJaggednessSpline(double v) {
        return b(WEIRDNESS).p(-.01,.63*v,0).p(.01,.3*v,0).build();
    }

    private static Spline getErosionFactor(double v, boolean flag) {
        Spline base=b(WEIRDNESS).p(-.2,6.3,0).p(.2,v,0).build();
        Builder out=b(EROSION).p(-.6,base,0)
                .p(-.5,b(WEIRDNESS).p(-.05,6.3,0).p(.05,2.67,0).build(),0)
                .p(-.35,base,0).p(-.25,base,0)
                .p(-.1,b(WEIRDNESS).p(-.05,2.67,0).p(.05,6.3,0).build(),0).p(.03,base,0);
        if(flag) {
            Spline w=b(WEIRDNESS).p(0,v,0).p(.1,.625,0).build();
            Spline r=b(RIDGES).p(-.9,v,0).p(-.69,w,0).build();
            out.p(.35,v,0).p(.45,r,0).p(.55,r,0).p(.62,v,0);
        } else {
            Spline r0=b(RIDGES).p(-.7,base,0).p(-.15,1.37,0).build();
            Spline r1=b(RIDGES).p(.45,base,0).p(.7,1.56,0).build();
            out.p(.05,r1,0).p(.4,r1,0).p(.45,r0,0).p(.55,r0,0).p(.58,v,0);
        }
        return out.build();
    }

    private static Spline buildErosionOffsetSpline(double a,double b0,double c,double d,double e,double f,boolean g,boolean h) {
        Spline s0=buildMountainRidgeSpline(lerp(d,.6,1.5),h),s1=buildMountainRidgeSpline(lerp(d,.6,1),h),s2=buildMountainRidgeSpline(d,h);
        Spline s3=ridgeSpline(a-.15,.5*d,.5*d,.5*d,.6*d,.5);
        Spline s4=ridgeSpline(a,e*d,b0*d,.5*d,.6*d,.5);
        Spline s5=ridgeSpline(a,e,e,b0,c,.5);
        Spline s7=b(RIDGES).p(-1,a,0).p(-.4,s5,0).p(0,c+.07,0).build();
        Spline s8=ridgeSpline(-.02,f,f,b0,c,0);
        Builder out=b(EROSION).p(-.85,s0,0).p(-.7,s1,0).p(-.4,s2,0).p(-.35,s3,0).p(-.1,s4,0).p(.2,s5,0);
        if(g) out.p(.4,s5,0).p(.45,s7,0).p(.55,s7,0).p(.58,s5,0);
        return out.p(.7,s8,0).build();
    }

    private static Spline buildMountainRidgeSpline(double v,boolean flag) {
        double a=mountain(-1,v,-.7), bValue=mountain(1,v,-.7), zero=mountainZero(v);
        if(zero>-.65 && zero<1) {
            double m65=mountain(-.65,v,-.7),m75=mountain(-.75,v,-.7),s0=slope(a,m75,-1,-.75),at=mountain(zero,v,-.7),s1=slope(at,bValue,zero,1);
            return b(RIDGES).p(-1,a,s0).p(-.75,m75,0).p(-.65,m65,0).p(zero-.01,at,0).p(zero,at,s1).p(1,bValue,s1).build();
        }
        double s=slope(a,bValue,-1,1); Builder out=b(RIDGES);
        if(flag) out.p(-1,Math.max(.2,a),s).p(0,lerp(.5,a,bValue),s); else out.p(-1,a,s);
        return out.p(1,bValue,s).build();
    }

    private static double mountain(double c,double v,double edge) {
        double f2=1-(1-v)*.5, f3=.5*(1-v), f4=(c+1.17)*.46082947, out=f4*f2-f3;
        return c<edge?Math.max(out,-.2222):Math.max(out,0);
    }

    private static double mountainZero(double v){ double f2=1-(1-v)*.5,f3=.5*(1-v);return f3/(.46082947*f2)-1.17; }

    private static Spline ridgeSpline(double a,double b0,double c,double d,double e,double min) {
        double s=Math.max(.5*(b0-a),min), s1=5*(c-b0);
        return b(RIDGES).p(-1,a,s).p(-.4,b0,Math.min(s,s1)).p(0,c,s1).p(.4,d,2*(d-c)).p(1,e,.7*(e-d)).build();
    }

    private static double slope(double a,double b0,double x0,double x1){return (b0-a)/(x1-x0);}
    private static double lerp(double t,double a,double b0){return a+t*(b0-a);}
    private static Builder b(Coordinate c){return new Builder(c);}

    private static final class Builder {
        final Coordinate coordinate; double[] locations=new double[0],derivatives=new double[0]; Spline[] values=new Spline[0];
        Builder(Coordinate c){coordinate=c;}
        Builder p(double location,double value,double derivative){return p(location,Spline.constant(value),derivative);}
        Builder p(double location,Spline value,double derivative){
            if(locations.length>0&&location<=locations[locations.length-1])throw new IllegalArgumentException("Spline points must ascend");
            int n=locations.length; double[] nl=new double[n+1],nd=new double[n+1];Spline[] nv=new Spline[n+1];
            System.arraycopy(locations,0,nl,0,n);System.arraycopy(derivatives,0,nd,0,n);System.arraycopy(values,0,nv,0,n);
            nl[n]=location;nd[n]=derivative;nv[n]=value;locations=nl;derivatives=nd;values=nv;return this;
        }
        Spline build(){return new Spline(coordinate,locations,values,derivatives);}
    }

    private static final class Spline {
        final Coordinate coordinate;final double[] locations,derivatives;final Spline[] values;
        Spline(Coordinate c,double[] l,Spline[] v,double[] d){coordinate=c;locations=l;values=v;derivatives=d;}
        static Spline constant(double v){return new Spline(null,new double[]{0},new Spline[0],new double[]{v});}
        double apply(Point p){
            if(coordinate==null)return derivatives[0]; double f=coordinate.value(p); int i=0;while(i<locations.length&&f>=locations[i])i++;i--;
            int last=locations.length-1;if(i<0)return values[0].apply(p)+derivatives[0]*(f-locations[0]);if(i==last)return values[last].apply(p)+derivatives[last]*(f-locations[last]);
            double x0=locations[i],x1=locations[i+1],t=(f-x0)/(x1-x0),v0=values[i].apply(p),v1=values[i+1].apply(p),a=derivatives[i]*(x1-x0)-(v1-v0),b=-derivatives[i+1]*(x1-x0)+(v1-v0);
            return lerp(t,v0,v1)+t*(1-t)*lerp(t,a,b);
        }
    }
}