package com.sto.utils;

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
	
	public static double dblFormat(double val,int digits){
        double dbl = new BigDecimal(val).setScale(digits, BigDecimal.ROUND_HALF_UP).doubleValue();
		return dbl;
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

        double slope = (0!=sumx2)?sumxy / sumx2:0.0;
        double offset = ymean - slope * xmean;
        double rsq = (0!=sumx2 && sumy2!=0)?sumxy * sumxy / (sumx2 * sumy2):0.0;
        if(0!=sumx2 && 0==sumxy) rsq = 1.0;
        
        result[0] = slope;
        result[1] = offset;
        result[2] = rsq;
        
        return result;
    }
    
    /**
     * 
     * @param x
     * @param y
     * @param startIdx
     * @param stopIdx
     * @return double[] 0-Slope | 1-Offset | 2-RSQ
     */
    public static double[] lineFitting(ArrayList<Double> x, ArrayList<Double> y, int startIdx, int stopIdx) {
        int size = x.size(), iTmp = 0, dataPts = 0;
        double xmean = 0.0, ymean = 0.0;
        double result[] = new double[3];
        double xVal, yVal;
        
        if(startIdx>stopIdx){
        	iTmp = startIdx;
        	startIdx = stopIdx;
        	stopIdx = iTmp;
        }
        if(stopIdx>=size) stopIdx = size - 1;
        if(startIdx>stopIdx) startIdx = stopIdx;
        dataPts = stopIdx - startIdx + 1;
        
        for (int i = startIdx; i <= stopIdx; i++) {
        	xVal = x.get(i);
        	yVal = y.get(i);
        	
            xmean += (double)xVal/dataPts;
            ymean += (double)yVal/dataPts;
        }
        
        double sumx2 = 0.0f, sumy2 = 0.0f, sumxy = 0.0f;
        for (int i = startIdx; i <= stopIdx; i++) {
        	xVal = x.get(i);
        	yVal = y.get(i);
        	
            sumx2 += (xVal - xmean) * (xVal - xmean);
            sumy2 += (yVal - ymean) * (yVal - ymean);
            sumxy += (xVal - xmean) * (yVal - ymean);
        }

        double slope = (0!=sumx2)?sumxy / sumx2:0.0;
        double offset = ymean - slope * xmean;
        double rsq = (0!=sumx2 && sumy2!=0)?sumxy * sumxy / (sumx2 * sumy2):0.0;
        if(0!=sumx2 && 0==sumxy) rsq = 1.0;
        
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

        double slope = (0!=sumx2)?sumxy / sumx2:0.0;
        double offset = ymean - slope * xmean;
        double rsq = (0!=sumx2 && 0!=sumy2)?sumxy * sumxy / (sumx2 * sumy2):0.0;
        
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
    
    public static int getFirstMeetIndex(double[] data, double threshold, int startIndex, boolean chkHigherSide, boolean forwardDirection){
    	int idx = -1;
    	double thrStop = threshold - 10;
    	
    	for(double thr = threshold; thr>=thrStop; thr--){
	    	if(null != data && startIndex>=0 && startIndex<data.length){
	    		int size = data.length;
	    		
	    		if(forwardDirection){
	    			for(int i=startIndex; i<size; i++){
	    				if(chkHigherSide){
	    					if(data[i]>thr){
	    						idx = i;
	    						break;
	    					}
	    				}else{
	    					if(data[i]<thr){
	    						idx = i;
	    						break;
	    					}
	    				}
	    			}
	    		}else{
	    			for(int i=startIndex; i>=0; i--){
	    				if(chkHigherSide){
	    					if(data[i]>thr){
	    						idx = i;
	    						break;
	    					}
	    				}else{
	    					if(data[i]<thr){
	    						idx = i;
	    						break;
	    					}
	    				}
	    			}
	    		}
	    	}
	    	
	    	if(idx>0) break;
    	}
    	return idx;
    }
    
    public static int getFirstPeakIndex(double[] data, int startIndex, int totalScanPoints, boolean chkHigherPeak, boolean forwardDirection){
    	int peakIdx = -1, scanPoints = totalScanPoints/2;//Two sides scanning
    	if(forwardDirection){
    		startIndex = startIndex + scanPoints;
    	}else{
    		startIndex = startIndex - scanPoints;
    	}
    	
		if(null != data && startIndex>=0 && startIndex<data.length){
			int positiveCnt = 0, negativeCnt = 0, size = data.length;
			int validDtPts = 0;
			
			if(forwardDirection){
				validDtPts = size - startIndex;
			}else{
				validDtPts = startIndex + 1;
			}
			
			if(validDtPts>totalScanPoints){
				if(forwardDirection){
					for(int j=startIndex; j<(size-scanPoints); j++){
						if(j-scanPoints<0 || j+scanPoints>=size) continue;
						
						positiveCnt = 0;
						negativeCnt = 0;
						for(int k=scanPoints; k>0; k--){
							if(data[j]>=data[j-k] && data[j]>=data[j+k]){
								positiveCnt++;
							}
							
							if(data[j]<=data[j-k] && data[j]<=data[j+k]){
								negativeCnt++;
							}
						}
						
						if(chkHigherPeak){
							if(positiveCnt==scanPoints){
								peakIdx = j;
								break;
							}
						}else{
							if(negativeCnt==scanPoints){
								peakIdx = j;
								break;
							}
						}
					}
				}else{
					for(int j=startIndex; j>=0; j--){
						if(j-scanPoints<0 || j+scanPoints>=size) continue;
						
						positiveCnt = 0;
						negativeCnt = 0;
						for(int k=scanPoints; k>0; k--){
							if(data[j]>=data[j-k] && data[j]>=data[j+k]){
								positiveCnt++;
							}
							
							if(data[j]<=data[j-k] && data[j]<=data[j+k]){
								negativeCnt++;
							}
						}
						
						if(chkHigherPeak){
							if(positiveCnt==scanPoints){
								peakIdx = j;
								break;
							}
						}else{
							if(negativeCnt==scanPoints){
								peakIdx = j;
								break;
							}
						}
					}
				}
			}
		}
		
		return peakIdx;
	}
    
    public static LinkedHashMap<Integer,Integer> getPeakVals(int[] data, int totalScanPoints, boolean chkHigherPeak){
		LinkedHashMap<Integer,Integer> peaks = new LinkedHashMap<Integer,Integer>();
		if(null != data){
			int scanPoints = totalScanPoints/2;//Two sides scanning
			int positiveCnt = 0, negativeCnt = 0, size = data.length;
			int[] peakIdx = null;
			double ratio = 0.0;
			if(size>totalScanPoints){
				for(int j=scanPoints; j<(size-scanPoints); j++){
					positiveCnt = 0;
					negativeCnt = 0;
					for(int k=scanPoints; k>0; k--){
						if(data[j]>=data[j-k] && data[j]>=data[j+k]){
							positiveCnt++;
						}
						
						if(data[j]<=data[j-k] && data[j]<=data[j+k]){
							negativeCnt++;
						}
					}
					
					if(chkHigherPeak){
						ratio = (double)positiveCnt/scanPoints;
						if(positiveCnt==scanPoints || ratio>0.66){
							//Double check whether it is a real peak or not
							peakIdx = getPeakCenterIndex(data,j,totalScanPoints,chkHigherPeak);
							if(peakIdx[0]>=0 && null==peaks.get(peakIdx[0])){
								j = peakIdx[0];
								peaks.put(j, data[j]);
							}
							j += scanPoints;
						}
					}else{
						if(negativeCnt==scanPoints){
							peaks.put(j, data[j]);
							j += scanPoints;
						}
					}
				}
			}
		}
		
		return peaks;
	}
    
    public static LinkedHashMap<Integer,Integer> getPeakValsEx(int[] data, int totalScanPoints, boolean chkHigherPeak){
		LinkedHashMap<Integer,Integer> peaks = new LinkedHashMap<Integer,Integer>();
		if(null != data){
			int scanPoints = totalScanPoints/2;//Two sides scanning
			int positiveCnt = 0, negativeCnt = 0, size = data.length, peakIdx = 0, peakVal = 0;
			int posInTrend = 0, negInTrend = 0, peakVal2 = 0, peakIdx2 = 0, cndFlag = 0;
			int[] rslts = null;
			double ratio = 0.0;
			
			if(size>totalScanPoints){
				for(int j=scanPoints; j<(size-scanPoints); j++){
					//Case I
					positiveCnt = 0; negativeCnt = 0;
					peakVal = data[j-scanPoints];
					peakIdx = -scanPoints;
					
					//Get peak index
					for(int k=-scanPoints; k<=scanPoints; k++){
						if(chkHigherPeak){
							if(peakVal<data[j+k]){
								peakVal = data[j+k];
								peakIdx = k;
							}
						}else{
							if(peakVal>data[j+k]){
								peakVal = data[j+k];
								peakIdx = k;
							}
						}
					}
					
					//Left side checking
					for(int k=-scanPoints; k<peakIdx; k++){
						if(chkHigherPeak){
							if(data[j+k]<peakVal) positiveCnt++;
						}else{
							if(data[j+k]>peakVal) negativeCnt++;
						}
					}
					
					//Right side checking
					for(int k=scanPoints; k>peakIdx; k--){
						if(chkHigherPeak){
							if(data[j+k]<peakVal) positiveCnt++;
						}else{
							if(data[j+k]>peakVal) negativeCnt++;
						}
					}
					
					cndFlag = 0;
					if((double)(peakIdx+scanPoints)/scanPoints>0.66 && (double)(scanPoints-peakIdx)/scanPoints>0.66){
						if(chkHigherPeak){
							ratio = (double)positiveCnt/(scanPoints*2);
						}else{
							ratio = (double)negativeCnt/(scanPoints*2);
						}
						if(ratio>0.8) cndFlag = 1;
					}
					
					//Case II
					if(0==cndFlag && scanPoints>2){
						posInTrend = 0; negInTrend = 0;
						peakVal2 = data[j-scanPoints+1];
						peakIdx2 = -scanPoints+1;
						
						//Get peak index
						for(int k=(1-scanPoints); k<scanPoints; k++){
							if(chkHigherPeak){
								if(peakVal2<data[j+k]){
									peakVal2 = data[j+k];
									peakIdx2 = k;
								}
							}else{
								if(peakVal2>data[j+k]){
									peakVal2 = data[j+k];
									peakIdx2 = k;
								}
							}
						}
						
						//The peak must be in the middle
						if(0==peakIdx2){
							//Left side checking
							for(int k=(1-scanPoints); k<peakIdx2; k++){
								if(chkHigherPeak){
									if(data[j+k]<data[j+k+1]) posInTrend++;
								}else{
									if(data[j+k]>data[j+k+1]) negInTrend++;
								}
							}
							
							//Right side checking
							for(int k=(scanPoints-1); k>peakIdx2; k--){
								if(chkHigherPeak){
									if(data[j+k]<data[j+k-1]) posInTrend++;
								}else{
									if(data[j+k]>data[j+k-1]) negInTrend++;
								}
							}
							
							if(posInTrend==(scanPoints-1)*2 || negInTrend==(scanPoints-1)*2) cndFlag = 2;
						}
					}
					
					if(1==cndFlag || 2==cndFlag){
						//Double check whether it is a real peak or not
						rslts = getPeakCenterIndex(data,j+(1==cndFlag?peakIdx:peakIdx2),totalScanPoints,chkHigherPeak);
						if(rslts[0]>=0 && null==peaks.get(rslts[0])){
							j = rslts[0];
							peaks.put(j, data[j]);
						}
						j += scanPoints;
					}
				}
			}
		}
		
		return peaks;
	}
    
    public static int[] getPeakCenterIndex(int[] data, int guessIdx, int totalScanPoints, boolean chkHigherPeak){
    	int peakIdx = -1, leftCounter = 0, rightCounter = 0;
    	int size = data.length;
    	int[] rslts = new int[]{-1,-1,-1};
    	int minVal = data[guessIdx], maxVal = data[guessIdx];
    	int start = 0, stop = 0, leftStop = 0, rightStop = 0;
    	int leftIdx = -1, rightIdx = -1, minIdx = -1, maxIdx = -1;
    	double ratio = 0.0, minR_ratio = 0.0, leftAvg = 0.0, rightAvg = 0.0;
    	double edgeThr1 = 0.5, edgeThr2 = 0.75;
    	int minRIdx = -1, maxRIdx = guessIdx, maxLIdx = guessIdx;
    	boolean searchLeftSide = false;
    	
    	start = guessIdx - totalScanPoints*2;
    	stop = guessIdx + totalScanPoints*2;
    	if(start<0) start = 0;
    	if(stop>=size) stop = size-1;
    	
    	if(20<guessIdx){
    		System.out.print("");
    	}
    	
    	if(chkHigherPeak){
    		//Decide which side to search
    		leftStop = guessIdx - totalScanPoints;
    		if(leftStop<0) leftStop = 0;
    		for(int i=guessIdx; i>=leftStop; i--){
	    		leftCounter++;
	    		leftAvg += data[i];
	    	}
    		leftAvg = leftAvg / leftCounter;
    		rightStop = guessIdx + totalScanPoints;
    		if(rightStop>=size) rightStop = size;
    		for(int i=guessIdx; i<rightStop; i++){
    			rightCounter++;
    			rightAvg += data[i];
    		}
    		rightAvg = rightAvg / rightCounter;
    		ratio = (leftAvg<rightAvg)?leftAvg/rightAvg:rightAvg/leftAvg;
    		if(ratio>=0.8){//Guess index is almost in the center of the peak
    			searchLeftSide = true;
    		}else{
    			searchLeftSide = leftAvg>rightAvg?true:false;
    		}
    		
    		//Search base value
	    	for(int i=guessIdx; i>start; i--){
	    		if(minVal>data[i]){
	    			minVal = data[i];
	    			minIdx = i;
	    		}
	    		if(minVal<=0) break;
	    	}
    		
    		if(searchLeftSide){
		    	//Search left peak index
		    	maxVal = data[guessIdx]; maxLIdx = guessIdx;
		    	for(int i=guessIdx; i>start; i--){
		    		if(minIdx==i && (double)minVal/data[guessIdx]>edgeThr2) continue;
		    		ratio = (double)(data[i]-minVal)/(maxVal-minVal);
		    		if(ratio<edgeThr1){
		    			leftIdx = i;
		    			break;
		    		}
		    		if(maxVal<data[i]){
		    			maxVal=data[i];
		    			maxLIdx = i;
		    		}
		    	}
		    	
		    	if(leftIdx>=0){
		    		maxIdx = maxLIdx;
		    		rightStop = maxLIdx + totalScanPoints*2;
		    		if(rightStop>=size) rightStop = size;
		    		
		    		ratio = (double)(data[guessIdx]-minVal)/(maxVal-minVal);
		    		minRIdx = guessIdx; minR_ratio = ratio;
		    		//Right edge searching
			    	for(int i=maxLIdx; i<rightStop; i++){
			    		if(maxVal<data[i]){
			    			maxVal = data[i];
			    			maxIdx = i;
			    		}
			    		
			    		ratio = (double)(data[i]-minVal)/(maxVal-minVal);
			    		if(minR_ratio>ratio){
		    				minRIdx = i;
			    			minR_ratio = ratio;
		    			}
			    		
			    		if(ratio<edgeThr1){
			    			rightIdx = i;
			    			break;
			    		}
			    	}
			    	
			    	if(rightIdx>=0){
			    		peakIdx = (leftIdx+rightIdx)/2;
			    	}else if(minR_ratio<edgeThr2 && minRIdx>=maxIdx && Math.abs(guessIdx-(leftIdx+minRIdx)/2)<=3){
			    		peakIdx = (leftIdx+minRIdx)/2;
			    	}else if(minR_ratio<edgeThr2 && minRIdx>=maxIdx && Math.abs(guessIdx-(leftIdx+minRIdx)/2)<=5 && guessIdx-leftIdx<=3){
			    		peakIdx = guessIdx;
			    	}
		    	}
    		}else{
	    		//Right edge searching
		    	maxVal = data[guessIdx]; maxRIdx = guessIdx; maxIdx = guessIdx;
		    	ratio = (double)(data[guessIdx]-minVal)/(maxVal-minVal);
		    	minRIdx = guessIdx; minR_ratio = ratio;
		    	for(int i=guessIdx; i<stop; i++){
		    		ratio = (double)(data[i]-minVal)/(maxVal-minVal);
		    		if(minR_ratio>ratio){
	    				minRIdx = i;
		    			minR_ratio = ratio;
	    			}
		    		
		    		if(ratio<edgeThr1){
		    			rightIdx = i;
		    			break;
		    		}
		    		
		    		if(maxVal<data[i]){
		    			maxVal = data[i];
		    			maxRIdx = i;
		    		}
		    	}
		    	
		    	//Left edge searching
		    	maxIdx = maxRIdx;
		    	for(int i=maxRIdx; i>start; i--){
		    		if(maxVal<data[i]){
		    			maxVal = data[i];
		    			maxIdx = i;
		    		}
		    		
		    		ratio = (double)(data[i]-minVal)/(maxVal-minVal);
		    		if(ratio<edgeThr1){
		    			leftIdx = i;
		    			break;
		    		}
		    	}
		    	if(leftIdx>=0 && rightIdx>=0){
		    		peakIdx = (leftIdx+rightIdx)/2;
		    	}else if(leftIdx>=0 && leftIdx<=maxIdx && minRIdx>=maxIdx && minR_ratio<edgeThr2 && Math.abs(guessIdx-(leftIdx+minRIdx)/2)<=3){
		    		peakIdx = (leftIdx+minRIdx)/2;
		    	}
	    	}
    	}else{
    		peakIdx = guessIdx;
    	}
    	
    	rslts[0] = peakIdx;
    	rslts[1] = leftIdx;
    	rslts[2] = rightIdx;
    	return rslts;
    }
    
    public static int getPeakCenterIndex_Backup(int[] data, int guessIdx, int totalScanPoints, boolean chkHigherPeak){
    	int peakIdx = -1;
    	int size = data.length;
    	int minVal = data[guessIdx], maxVal = data[guessIdx];
    	int start = 0, stop = 0;
    	int leftIdx = -1, rightIdx = -1;
    	double ratio = 0.0, minR_ratio = 0.0;
    	int minRIdx = -1, maxRIdx = guessIdx;
    	
    	start = guessIdx - totalScanPoints*2;
    	stop = guessIdx + totalScanPoints*2;
    	if(start<0) start = 0;
    	if(stop>=size) stop = size-1;
    	
    	if(chkHigherPeak){
    		//Find the base value by left side searching
	    	for(int i=guessIdx; i>start; i--){
	    		if(minVal>data[i]) minVal = data[i];
	    		if(minVal<=0) break;
	    	}
	    	
	    	//Right edge searching
	    	for(int i=guessIdx; i<stop; i++){
	    		ratio = (double)(data[i]-minVal)/(maxVal-minVal);
	    		if(i==guessIdx){
	    			minRIdx = i;
	    			minR_ratio = ratio;
	    		}else{
	    			if(minR_ratio>ratio){
	    				minRIdx = i;
		    			minR_ratio = ratio;
	    			}
	    		}
	    		if(ratio<0.5){
	    			rightIdx = i;
	    			break;
	    		}
	    		if(maxVal<data[i]){
	    			maxVal = data[i];
	    			maxRIdx = i;
	    		}
	    	}
	    	
	    	//Left edge searching
	    	for(int i=maxRIdx; i>start; i--){
	    		ratio = (double)(data[i]-minVal)/(maxVal-minVal);
	    		if(ratio<0.5){
	    			leftIdx = i;
	    			break;
	    		}
	    	}
	    	if(leftIdx>=0 && rightIdx>=0){
	    		peakIdx = (leftIdx+rightIdx)/2;
	    	}else if(leftIdx>=0 && minR_ratio<0.6 && Math.abs(guessIdx-(leftIdx+minRIdx)/2)<=3){
	    		peakIdx = (leftIdx+minRIdx)/2;
	    	}
    	}else{
    		peakIdx = guessIdx;
    	}
    	
    	return peakIdx;
    }
}
