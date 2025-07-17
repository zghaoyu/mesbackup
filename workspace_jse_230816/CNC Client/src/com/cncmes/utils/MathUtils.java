package com.cncmes.utils;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class MathUtils {
	public static String MD5Encode(String str){
		String newStr = "";
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(str.getBytes());
			byte[] arrBytes = md5.digest();
			
			int i;
			StringBuffer buf = new StringBuffer("");
			for(int j=0; j<arrBytes.length; j++){
				i = arrBytes[j];
				if (i < 0) i = i + 256;
				if (i < 16) buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			newStr = buf.toString().toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return newStr;
	}
	
	public static float roundFloat(float fVal,int digits){
		float newVal = new BigDecimal(fVal).setScale(digits, BigDecimal.ROUND_HALF_UP).floatValue();
		return newVal;
	}
	
	public static String calculateCmdConfirmCode(String cmdStr){
		byte[] ascii = cmdStr.getBytes();
		int xor = ascii[0];
		
		for(int i=1; i<ascii.length; i++){
			xor ^= ascii[i];
		}
		
		return toBeautyFormat(Integer.toHexString(xor),2);
	}
	
	private static String toBeautyFormat(String binaryString,int totalBits){
		String s = "";
		int zeros = totalBits - binaryString.length();
		
		if(zeros > 0){
			for(int i=0; i<zeros; i++){
				s += "0";
			}
		}
		
		return (s+binaryString);
	}
	
	public static double[] lineFitting(ArrayList<Integer> x, ArrayList<Integer> y, int fitOrder) {
    	// Collect data.
    	final WeightedObservedPoints obs = new WeightedObservedPoints();
    	for(int i=0; i<x.size(); i++){
    		obs.add(x.get(i), y.get(i));
    	}
    	
    	// Instantiate a third-degree polynomial fitter.
    	final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(fitOrder);

    	// Retrieve fitted parameters (coefficients of the polynomial function).
    	final double[] coeff = fitter.fit(obs.toList());
    	return coeff;
    }
    
	/**
	 * 
	 * @param x
	 * @param y
	 * @return double[0]:slope|double[1]:offset|double[2]:RSQ
	 */
    public static double[] lineFitting(ArrayList<Integer> x, ArrayList<Integer> y) {
        int size = x.size();
        double xmean = 0.0, ymean = 0.0;
        double result[] = new double[3];
        int xVal, yVal;
        
        for (int i = 0; i < size; i++) {
        	xVal = x.get(i);
        	yVal = y.get(i);
        	
            xmean += (double)xVal/size;
            ymean += (double)yVal/size;
        }
        
        double sumx2 = 0.0f, sumy2 = 0.0f, sumxy = 0.0f;
        for (int i = 0; i < size; i++) {
        	xVal = x.get(i);
        	yVal = y.get(i);
        	
            sumx2 += (xVal - xmean) * (xVal - xmean);
            sumy2 += (yVal - ymean) * (yVal - ymean);
            sumxy += (xVal - xmean) * (yVal - ymean);
        }

        double slope = sumxy / sumx2;
        double offset = ymean - slope * xmean;
        double rsq = sumxy * sumxy / (sumx2 * sumy2);
        
        result[0] = slope;
        result[1] = offset;
        result[2] = rsq;
        
        return result;
    }
    
    /**
	 * 
	 * @param x
	 * @param y
	 * @return double[0]:slope|double[1]:offset|double[2]:RSQ|double[3]:Y's STDEV
	 */
    public static double[] lineFitting(double[] x, double[] y, int size) {
        double xmean = 0.0, ymean = 0.0;
        double result[] = new double[4];
        double xVal, yVal;
        
        for (int i = 0; i < size; i++) {
        	xVal = x[i];
        	yVal = y[i];
        	
            xmean += (double)xVal/size;
            ymean += (double)yVal/size;
        }
        
        double sumx2 = 0.0f, sumy2 = 0.0f, sumxy = 0.0f;
        for (int i = 0; i < size; i++) {
        	xVal = x[i];
        	yVal = y[i];
        	
            sumx2 += (xVal - xmean) * (xVal - xmean);
            sumy2 += (yVal - ymean) * (yVal - ymean);
            sumxy += (xVal - xmean) * (yVal - ymean);
        }

        double slope = sumxy / sumx2;
        double offset = ymean - slope * xmean;
        double rsq = sumxy * sumxy / (sumx2 * sumy2);
        double yStdev = Math.sqrt(sumy2/(size-1));
        
        result[0] = slope;
        result[1] = offset;
        result[2] = rsq;
        result[3] = yStdev;
        
        return result;
    }
    
    public static double[] lineFitting(LinkedHashMap<Double,Double> fitData) {
    	int size = fitData.size();
        double xmean = 0.0, ymean = 0.0;
        double result[] = new double[3];
        double yVal;
        
        for (double xVal:fitData.keySet()) {
        	yVal = fitData.get(xVal);
            xmean += (double)xVal/size;
            ymean += (double)yVal/size;
        }
        
        double sumx2 = 0.0f, sumy2 = 0.0f, sumxy = 0.0f;
        for (double xVal:fitData.keySet()) {
        	yVal = fitData.get(xVal);
        	
            sumx2 += (xVal - xmean) * (xVal - xmean);
            sumy2 += (yVal - ymean) * (yVal - ymean);
            sumxy += (xVal - xmean) * (yVal - ymean);
        }

        double slope = sumxy / sumx2;
        double offset = ymean - slope * xmean;
        double rsq = sumxy * sumxy / (sumx2 * sumy2);
        
        result[0] = slope;
        result[1] = offset;
        result[2] = rsq;
        
        return result;
    }
    
    public static double getStdev(ArrayList<Integer> data){
    	double stdev = 0.0;
    	if(data.size()>1){
    		int size = data.size();
    		double avg = 0.0, sumSqr = 0.0f;
            for (int i = 0; i < size; i++) {
                avg += (double)data.get(i)/size;
            }
            for (int i = 0; i < size; i++) {
                sumSqr += (data.get(i) - avg) * (data.get(i) - avg);
            }
            stdev = Math.sqrt(sumSqr/(size-1));
    	}
    	return stdev;
    }
}
