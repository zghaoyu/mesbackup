package com.sto.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.sto.base.SharpChgItems;
import com.sto.utils.MathUtils;

public class ImgExtractLine implements Cloneable {
	private int validPoints = 0;
	private int invalidPoints = 0;
	private int realPoints = 0;
	private int sideNoise = 0;
	private int onlinePoints = 0;
	private int lineIndex = 0;
	private double lineSlope = 0.0;
	private double lineIntercept = 0.0;
	private double lineRSQ = 0.0;
	private double lineInterceptDelta = 0.0;
	private double tendencyRSQ = 0.0;
	private double weightOfLongPole = 0.0;
	private boolean lineIsChecked = false;
	private boolean lineIsPassed = false;
	
	private double wrinkleSlope = 0.0;
	private double wrinkleOffset = 0.0;
	private double wrinkleAngle = 0.0;
	private double wrinkleCrossX = -1.0;
	private double wrinkleMinX = -1.0;
	private boolean wrinkleUpward = false;
	private double wrinkleUpScore = -1.0;
	private double wrinkleDnScore = -1.0;
	private int wrinkleUpQty = 0;
	private int wrinkleDnQty = 0;
	
	private int xAxisStart = 0;
	private int xAxisStop = 0;
	private int crossX = -1;
	private int meanGray = 255;
	private int lineFlag = 0; //0:Not Checked|1:Checked|2:Adjusted|3:Guessing
	private int wrinkleFlag = 0; //0:Not Checked|1:DN side|2:UP side|3:Both side
	private double refGray = 0.0; //The reference gray for Pole judgement
	private int polePosition = -1;//Pole position
	private String xVals = "";
	private String yVals = "";
	
	private static int minValidPoints = 100;
	private static int maxSideNoise = 50;
	private static float minRSQ = 0.85f;
	
	private LinkedHashMap<Double,Double> lineFittingData = new LinkedHashMap<Double,Double>();
	private ArrayList<Integer> lineXAxis = new ArrayList<Integer>();
	private ArrayList<Integer> lineYAxis = new ArrayList<Integer>();
	private ArrayList<Integer> lineGray = new ArrayList<Integer>();
	private LinkedHashMap<String,Integer> points = new LinkedHashMap<String,Integer>();
	private LinkedHashMap<String,Integer> realPts = new LinkedHashMap<String,Integer>();
	private LinkedHashMap<Integer,Double> sharpChgPos = new LinkedHashMap<Integer,Double>();
	private LinkedHashMap<Integer,LinkedHashMap<SharpChgItems,Double>> sharpChgPosParas = new LinkedHashMap<Integer,LinkedHashMap<SharpChgItems,Double>>();
	
	@Override
	public ImgExtractLine clone(){
		try {
			return (ImgExtractLine) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "validPoints,realPoints,polePos,sideNoise,onlinePoints,"
				+ "lineSlope,lineIntercept,offDelta,lineRSQ,tdcRSQ,xStart,xStop,meanGray\r\n"
				+ validPoints + "," + realPoints + "," + polePosition + "," + sideNoise + ","  + onlinePoints + "," 
				+ lineSlope + "," + lineIntercept + "," + lineInterceptDelta + "," 
				+ lineRSQ + "," + tendencyRSQ + "," + xAxisStart + "," + xAxisStop + "," + meanGray;
	}
	
	public String printLineParas(int index, boolean printTitle){
		String title = "Index,validPoints,realPoints,polePos,sideNoise,onlinePoints,"
				+ "lineSlope,lineIntercept,offDelta,lineRSQ,tdcRSQ,xStart,xStop,meanGray\r\n";
		String data = index + "," + validPoints + "," + realPoints + "," + polePosition + "," + sideNoise + ","  + onlinePoints + "," 
				+ lineSlope + "," + lineIntercept + "," + lineInterceptDelta + "," 
				+ lineRSQ + "," + tendencyRSQ + "," + xAxisStart + "," + xAxisStop + "," + meanGray;;
		return (printTitle?title+data:data);
	}
	
	/**
	 * 
	 * @param linesMap All lines data
	 * @param startIdx Current line starting position for gray statistic parameters calculation
	 * @param stopIdx Current line stopping position for gray statistic parameters calculation
	 * @param imgGrays Image's gray data
	 * @param lineIdx Current line index in linesMap
	 * @param evenLineLonger The even index line is longer than odd index line
	 * @return double[0]:slope(Position VS Gray) | double[1]:offset | double[2]:rsq | double[3]:gray stdev | double[4]:Ratio of upSideGray and dnSideGray
	 */
	public double[] getLineStatisticParas(LinkedHashMap<Integer, ImgExtractLine> linesMap,int startIdx, int stopIdx, int[][] imgGrays, int lineIdx, boolean evenLineLonger){
		double[] statisticParas = new double[7], tmpParas = null;
		double upSideGray = 0.0, dnSideGray = 0.0, dnSideGray2 = 0.0;
		double upSideGrayAvg = 0.0, dnSideGrayAvg = 0.0, dnSideRefGrayAvg = 0.0;
		int upSideCnt = 0, dnSideCnt = 0, dnSideCnt2 = 0;
		int imgHeight = imgGrays[0].length;
		int nearCol = 0, dnSideMaxGray = 0, upSideChkOffset = 0;
		int dnSideRefPos = 0, refChkStopPos = 0;
		int[] dnSideRefGray = null, dnSideRefCnt = null;
		
		if(startIdx>stopIdx){
			nearCol = startIdx;
			startIdx = stopIdx;
			stopIdx = nearCol;
		}
		
		//Get the inner reference position
		if(null!=linesMap.get(lineIdx+1)){
			refChkStopPos = (int)(linesMap.get(lineIdx+1).getLineIntercept()-lineIntercept);
		}else{
			refChkStopPos = 7;
		}
		if(refChkStopPos<4) refChkStopPos = 4;
		for(int i=startIdx; i<=stopIdx; i++){
			nearCol = (int)(i*lineSlope+lineIntercept);
			if(nearCol>=0 && nearCol<imgHeight){
				dnSideMaxGray = 0; dnSideRefPos = 0;
				for(int j=(nearCol+4); j<=(nearCol+refChkStopPos); j++){
					if(j<imgHeight){
						if(dnSideMaxGray<imgGrays[i][j]){
							dnSideMaxGray = imgGrays[i][j];
							dnSideRefPos = j-nearCol;
						}
					}
				}
				if(dnSideRefPos>0) break;
			}
		}
		if(dnSideRefPos<=0) dnSideRefPos = 4;
		dnSideRefGray = new int[dnSideRefPos];
		dnSideRefCnt = new int[dnSideRefPos];
		
		//Get the outer checking offset
		if(null!=linesMap.get(lineIdx-1)){
			upSideChkOffset = (int)((lineIntercept - linesMap.get(lineIdx-1).getLineIntercept())/2);
		}else{
			upSideChkOffset = 7;
		}
		if(upSideChkOffset<=0) upSideChkOffset = 4;
		
		double[] linesIndex = null, grays = null;
		LinkedHashMap<Integer,Integer> tmpGrays = new LinkedHashMap<Integer,Integer>();
		for(int i=startIdx; i<=stopIdx; i++){
			nearCol = (int)(i*lineSlope+lineIntercept);
			if(nearCol>0 && nearCol<imgHeight){
				tmpGrays.put(i, imgGrays[i][nearCol]);
				for(int j=-1; j<=1; j++){
					if(nearCol+j<imgHeight){
						dnSideCnt++;
						dnSideGray += imgGrays[i][nearCol+j];
					}
					if(nearCol+j+2<imgHeight){
						dnSideCnt2++;
						dnSideGray2 += imgGrays[i][nearCol+j+2];
					}
					if(nearCol-upSideChkOffset+j>=0){
						upSideCnt++;
						upSideGray += imgGrays[i][nearCol-upSideChkOffset+j];
					}
				}
				
				for(int j=1; j<=dnSideRefPos; j++){
					if((nearCol+dnSideRefPos)<imgHeight){
						dnSideRefCnt[j-1] = dnSideRefCnt[j-1]+1;
						dnSideRefGray[j-1] += imgGrays[i][nearCol+dnSideRefPos];
					}
				}
			}
		}
		
		if(tmpGrays.size()>0){
			dnSideGrayAvg = (double)dnSideGray/dnSideCnt;
			upSideGrayAvg = (double)upSideGray/upSideCnt;
			
			dnSideRefGrayAvg = 0;
			for(int j=0; j<dnSideRefPos; j++){
				dnSideGray = (double)dnSideRefGray[j]/dnSideRefCnt[j];
				if(dnSideRefGrayAvg<dnSideGray) dnSideRefGrayAvg = dnSideGray;
			}
			
			linesIndex = new double[tmpGrays.size()];
			grays = new double[tmpGrays.size()];
			nearCol = -1;
			for(int i:tmpGrays.keySet()){
				nearCol++;
				linesIndex[nearCol] = i;
				grays[nearCol] = tmpGrays.get(i);
			}
			tmpParas = MathUtils.lineFitting(linesIndex, grays, nearCol+1);
			for(int i=0; i<tmpParas.length; i++){
				statisticParas[i] = tmpParas[i];
			}
			statisticParas[4] = upSideGrayAvg/dnSideGrayAvg;
			statisticParas[5] = dnSideRefGrayAvg/dnSideGrayAvg;
			statisticParas[6] = (double)dnSideGray2/dnSideCnt2/dnSideGrayAvg;
		}
		return statisticParas;
	}
	
	public int calcPolePosition(boolean evenLineLonger,LinkedHashMap<Integer, ImgExtractLine> linesMap,int curLineIdx,int[][] imgGrays,int[][] peakVals,int targetLinesQty){
		int polePos = 0, maxChgPos = 0, idx = -1, grayDeltaThr = 5;
		boolean curLineIsShorter = true;
		int[] keys = null;
		double maxChg = 0.0, curWeight = 0.0;
		
		if(0==curLineIdx%2 && evenLineLonger || 1==curLineIdx%2 && !evenLineLonger) curLineIsShorter = false;
		double[] weightOfLine = weightOfBeingLine(linesMap,getXAxisStop(),getXAxisStart(),0,imgGrays,peakVals,grayDeltaThr,curLineIdx,targetLinesQty,true,!curLineIsShorter,true);
		int newXAxisStart = (int)((getXAxisStart()-getXAxisStop())*weightOfLine[0]);
		newXAxisStart = getXAxisStart() - newXAxisStart;
		
		if(curLineIsShorter){
			polePos = newXAxisStart;
		}else{
			if(weightOfLine[0]>0.45){
				maxChgPos = newXAxisStart-5;
			}else if(sharpChgPos.size()>0){
				idx = -1;
				keys = new int[sharpChgPos.size()];
				for(int key:sharpChgPos.keySet()){
					idx++;
					keys[idx] = key;
				}
				idx = -1;
				for(int i=(keys.length-1); i>=0; i--){
					idx++;
					if(1==idx && weightOfLine[0]>0.5) maxChg = 0.0; //Skip the most-inner change
					if(maxChg<Math.abs(sharpChgPos.get(keys[i]))){
						maxChgPos = keys[i];
						maxChg = Math.abs(sharpChgPos.get(keys[i]));
					}
					if(keys[i]<(newXAxisStart+5)) break;
				}
			}
			polePos = maxChgPos+5;
			
			curWeight = (double)(getXAxisStart() - polePos)/(getXAxisStart() - getXAxisStop());
			if(curWeight<weightOfLine[0]){
				polePos = (int)(getXAxisStart()-(getXAxisStart()-getXAxisStop())*weightOfLine[0])+5;
			}
		}
		
		return polePos;
	}
	
	/**
	 * Determine whether there is a line or not between from and to position,
	 * and correct the line position by setting lineInterceptDelta
	 * @param linesMap All lines found
	 * @param from Gray delta calculation starting position
	 * @param to Gray delta calculation stopping position
	 * @param lineOffset Base line offset
	 * @param refOffset Reference line offset
	 * @param imgGrays Image's grays
	 * @param peakVals Image's gray sharp changes
	 * @param grayDeltaThr Line judging threshold
	 * @param lineIdx Current line index
	 * @param targetLinesQty Target lines quantity
	 * @param bNotSetInterceptDelta Do not correct the line intercept
	 * @param curLineIsLonger Current line is longer
	 * @return double[0]:Weight of being a line between from and to position | 
	 * double[1]: Average gray delta between Checked Line and Reference Line | 
	 * double[2]: Offset btw Checked Line and Current Line | 
	 * double[3]: Weight of being line appeared in up side checking region | 
	 * double[4]: Weight of being line appeared in dn side checking region | 
	 * double[5]: Percentage of total lines appeared in up side checking region | 
	 * double[6]: Percentage of total lines appeared in dn side checking region | 
	 * double[7]: Total checking lines' QTY in up side region | 
	 * double[8]: Total checking lines' QTY in dn side region
	 */
	public double[] weightOfBeingLine(LinkedHashMap<Integer, ImgExtractLine> linesMap, int from, int to, int lineOffset, int[][] imgGrays, int[][] peakVals, int grayDeltaThr, int lineIdx, int targetLinesQty, boolean bNotSetInterceptDelta, boolean curLineIsLonger, boolean bCalcPoleDistance){
		double[] rslt = null;
		double ratio = 0.0;
		int newThr = 0;
		if(bCalcPoleDistance && 13==lineIdx){
			System.out.print("");
		}
		rslt = weightOfBeingLineOri(linesMap,from,to,lineOffset,imgGrays,peakVals,grayDeltaThr,lineIdx,targetLinesQty,bNotSetInterceptDelta,curLineIsLonger,bCalcPoleDistance);
		ratio = (double)(lineIdx+1)/targetLinesQty;
		if(bCalcPoleDistance && curLineIsLonger && rslt[0]<0.8 && ratio>0.85){
			for(double thrFactor=0.6; thrFactor>=0.5; thrFactor-=0.1){
				newThr = (int)(grayDeltaThr*thrFactor);
				if(newThr<2) break;
				rslt = weightOfBeingLineOri(linesMap,from,to,lineOffset,imgGrays,peakVals,newThr,lineIdx,targetLinesQty,bNotSetInterceptDelta,curLineIsLonger,bCalcPoleDistance);
				if(rslt[0]>=0.8) break;
			}
		}
		return rslt;
	}
	
	private double[] weightOfBeingLineOri(LinkedHashMap<Integer, ImgExtractLine> linesMap, int from, int to, int lineOffset, int[][] imgGrays, int[][] peakVals, int grayDeltaThr, int lineIdx, int targetLinesQty, boolean bNotSetInterceptDelta, boolean curLineIsLonger, boolean bCalcPoleDistance){
		double[] rslt =  new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
		double currPosBeLineWeight = 0.0, upSideBeLineWeight = 0.0, dnSideBeLineWeight = 0.0;
		double curLineIntercept = 0.0, curLineSlope = 0.0, upPoleChkRatio = 0.0, dnPoleChkRatio = 0.0;
		double upPeaksRatio = 0.0, dnPeaksRatio = 0.0, chkProgress = 0.0, tmpRatio = 0.0, refGray = 0.0;
		double lastLineIntercept = 0.0, lastLineSlope = 0.0, nextLineIntercept = 0.0, nextLineSlope = 0.0;
		int upPeaksMinIdx = 0, dnPeaksMinIdx = 0, refStart = 0, refStop = 0, refCnt = 0, refOffset = 0;
		int lineGrayDeltaCounter = 0, lineGrayDelta = 0, positiveDeltaCounter = 0, minCurCol = 0, maxCurCol = 0;
		int grayDelta = 0, nearCol = 0, lastCol = 0, nextCol = 0, imgHeight = imgGrays[0].length;
		int upSideDeltaCounter = 0, dnSideDeltaCounter = 0, upMinGray = 0, dnMinGray = 0;
		int upSideDeltaSum = 0, dnSideDeltaSum = 0, upSideGrayDelta = 0, dnSideGrayDelta = 0;
		int upSideCnt = 0, dnSideCnt = 0, upSideMaxCnt = 0, dnSideMaxCnt = 0;
		int upSideMaxIdx = 0, dnSideMaxIdx = 0, upPoleChkMaxCnt = 0, dnPoleChkMaxCnt = 0;
		int upSideMaxRng = 10, dnSideMaxRng = 13;//One side searching
		ImgExtractLine currLine = null, lastLine = null, nextLine = null;
		
		try {
			currLine = linesMap.get(lineIdx);
			lastLine = linesMap.get(lineIdx-1);
			nextLine = linesMap.get(lineIdx+1);
		} catch (Exception e) {
		}
		
		ArrayList<Integer> dnSidePeaks = new ArrayList<Integer>();
		ArrayList<Integer> upSidePeaks = new ArrayList<Integer>();
		
		//Get the checking range
		if(null!=currLine){
			curLineIntercept = currLine.getLineIntercept();
			curLineSlope = currLine.getLineSlope();
			if(bNotSetInterceptDelta) curLineIntercept += currLine.getLineInterceptDelta();
		}else{
			curLineIntercept = lineIntercept + (bNotSetInterceptDelta?lineInterceptDelta:0);
			curLineSlope = lineSlope;
		}
		
		nearCol = (int)(from*curLineSlope+curLineIntercept+lineOffset);
		lastCol = (int)(to*curLineSlope+curLineIntercept+lineOffset);
		minCurCol = (nearCol<lastCol)?nearCol:lastCol;
		maxCurCol = (nearCol>lastCol)?nearCol:lastCol;
		
		if(null!=lastLine){
			lastLineIntercept = lastLine.getLineIntercept();
			lastLineSlope = lastLine.getLineSlope();
			if(bNotSetInterceptDelta) lastLineIntercept += lastLine.getLineInterceptDelta();
			nearCol = (int)(to*lastLineSlope+lastLineIntercept+lineOffset);
			lastCol = (int)(from*lastLineSlope+lastLineIntercept+lineOffset);
			lastCol = (nearCol>lastCol)?nearCol:lastCol;//Get the inner col
			upSideMaxRng = minCurCol - (lastCol + 3);
		}
		if(lineIdx<(targetLinesQty-1) && null!=nextLine){
			nextLineIntercept = nextLine.getLineIntercept();
			nextLineSlope = nextLine.getLineSlope();
			if(bNotSetInterceptDelta) nextLineIntercept += nextLine.getLineInterceptDelta();
			nearCol = (int)(to*nextLineSlope+nextLineIntercept+lineOffset);
			nextCol = (int)(from*nextLineSlope+nextLineIntercept+lineOffset);
			nextCol = (nearCol<nextCol)?nearCol:nextCol;//Get the outer col
			dnSideMaxRng = nextCol-maxCurCol-2;
			if(bCalcPoleDistance){
				if(dnSideMaxRng>3) dnSideMaxRng = 3;
			}else{
				if(dnSideMaxRng>9) dnSideMaxRng = 9;
			}
		}
		if(upSideMaxRng<=3) upSideMaxRng = (curLineIsLonger?6:3);
		if(dnSideMaxRng<0) dnSideMaxRng = 0;
		refOffset = -upSideMaxRng;
		
		int[] upSideCounters = new int[upSideMaxRng+1];
		int[] dnSideCounters = new int[dnSideMaxRng+1];
		int[] upPoleChkCounters = new int[upSideMaxRng+1];
		int[] dnPoleChkCounters = new int[dnSideMaxRng+1];
		int tmpDelta = 0, poleChkStopPos = 0;
		
		if(from>to){
			nearCol = from;
			from = to;
			to = nearCol;
		}
		
		poleChkStopPos = (from+to)/2;
		upPeaksMinIdx = -1; dnPeaksMinIdx = -1;
		for(int i=from; i<=to; i++){//Line checking in layer-parallel direction
			nearCol = (int)(i*curLineSlope+curLineIntercept+lineOffset);
			
			//Check gray sharp changes in layer-parallel direction among +/-1 pixels
			if((nearCol-1)>=0 && (nearCol+1)<imgHeight){
				if(peakVals[i][nearCol]>0 && peakVals[i][nearCol]<255){
					dnSidePeaks.add(i);
					upSidePeaks.add(i);
					if(upPeaksMinIdx<0 || upPeaksMinIdx>0 && i-upSidePeaks.get(upSidePeaks.size()-2)>6) upPeaksMinIdx = i;
					if(dnPeaksMinIdx<0 || dnPeaksMinIdx>0 && i-dnSidePeaks.get(dnSidePeaks.size()-2)>6) dnPeaksMinIdx = i;
				}else if(peakVals[i][nearCol+1]>0 && peakVals[i][nearCol+1]<255){
					dnSidePeaks.add(i);
					if(dnPeaksMinIdx<0 || dnPeaksMinIdx>0 && i-dnSidePeaks.get(dnSidePeaks.size()-2)>6) dnPeaksMinIdx = i;
				}else if(peakVals[i][nearCol-1]>0 && peakVals[i][nearCol-1]<255){
					upSidePeaks.add(i);
					if(upPeaksMinIdx<0 || upPeaksMinIdx>0 && i-upSidePeaks.get(upSidePeaks.size()-2)>6) upPeaksMinIdx = i;
				}
			}
			
			//Check gray delta in layer-vertical direction - compare to the refOffset position
			if(nearCol+refOffset>=0 && nearCol+refOffset<imgHeight){
				//Get reference gray
				refGray = 0.0; refCnt = 0;
				refStart = refOffset;
				refStop = refOffset / 2;
				if(refStart>refStop){
					refStart = refStop;
					refStop = refOffset;
				}
				for(int j=refStart; j<refStop; j++){
					if(nearCol+j>=0 && nearCol+j<imgHeight){
						refGray += imgGrays[i][nearCol+j];
						refCnt++;
					}
				}
				if(refCnt>0){
					refGray = refGray/refCnt;
				}else{
					refGray = imgGrays[i][nearCol+refOffset];
				}
				
				if(!(bCalcPoleDistance && curLineIsLonger)){
					refGray = imgGrays[i][nearCol+refOffset];
				}
				
				//Current line checking
				if(nearCol>=0 && nearCol<imgHeight){
					grayDelta = getGrayDelta((int)refGray, imgGrays, i, nearCol, 2);
					lineGrayDeltaCounter++;
					lineGrayDelta += grayDelta;
					if(grayDelta>=grayDeltaThr) positiveDeltaCounter++;
				}
				
				upMinGray = 255; dnMinGray = 255;
				//Up side minimum gray(darkest color) - checking in layer-vertical direction
				for(int j=0; j<=upSideMaxRng; j++){
					tmpDelta = 0;
					if(nearCol-j>=0 && nearCol-j<imgHeight){
						if(upMinGray > imgGrays[i][nearCol-j]) upMinGray = imgGrays[i][nearCol-j];
						tmpDelta = getGrayDelta((int)refGray, imgGrays, i, nearCol-j, 2);
					}
					if(tmpDelta>=grayDeltaThr){
						upSideCounters[j] = upSideCounters[j] + 1;
						if(i<=poleChkStopPos) upPoleChkCounters[j] = upPoleChkCounters[j] + 1;
					}
				}
				
				//Dn side minimum gray(darkest color) - checking in layer-vertical direction
				for(int j=0; j<=dnSideMaxRng; j++){
					tmpDelta = 0;
					if(nearCol+j>=0 && nearCol+j<imgHeight){
						if(dnMinGray > imgGrays[i][nearCol+j]) dnMinGray = imgGrays[i][nearCol+j];
						tmpDelta = getGrayDelta((int)refGray, imgGrays, i, nearCol+j, 2);
					}
					if(tmpDelta>=grayDeltaThr){
						dnSideCounters[j] = dnSideCounters[j] + 1;
						if(i<=poleChkStopPos) dnPoleChkCounters[j] = dnPoleChkCounters[j] + 1;
					}
				}
				
				upSideGrayDelta = (int)(refGray - upMinGray);
				dnSideGrayDelta = (int)(refGray - dnMinGray);
				upSideDeltaSum += upSideGrayDelta;
				dnSideDeltaSum += dnSideGrayDelta;
				upSideCnt++;
				dnSideCnt++;
				if(upSideGrayDelta>=grayDeltaThr) upSideDeltaCounter++;
				if(dnSideGrayDelta>=grayDeltaThr) dnSideDeltaCounter++;
			}
		}
		if(lineGrayDeltaCounter>7) lineGrayDelta = lineGrayDelta/lineGrayDeltaCounter;//Gray delta btw current line and reference line
		if(positiveDeltaCounter>7) currPosBeLineWeight = (double)positiveDeltaCounter/lineGrayDeltaCounter;//Be-line weight of current line
		if(upSideDeltaCounter>7) upSideBeLineWeight = (double)upSideDeltaCounter/lineGrayDeltaCounter;
		if(dnSideDeltaCounter>7) dnSideBeLineWeight = (double)dnSideDeltaCounter/lineGrayDeltaCounter;
		if(upSideCnt>7) upSideGrayDelta = upSideDeltaSum/upSideCnt;
		if(dnSideCnt>7) dnSideGrayDelta = dnSideDeltaSum/dnSideCnt;
		
		//Up Side Be-line Weight Calculation
		upSideMaxCnt = 0; upSideMaxIdx = 0; upPoleChkMaxCnt = 0;
		for(int i=0; i<=upSideMaxRng; i++){
			if(upSideMaxCnt<upSideCounters[i]){
				upSideMaxCnt = upSideCounters[i];
				upSideMaxIdx = i;
			}else if((double)upSideCounters[i]/upSideMaxCnt>0.95){
				upSideMaxIdx = i;
			}
			rslt[3] = (double)upSideCounters[i]/(to-from+1);
			if(rslt[3]>=0.5) rslt[5] = rslt[5] + 1.0;
			
			if(upPoleChkMaxCnt<upPoleChkCounters[i]) upPoleChkMaxCnt = upPoleChkCounters[i];
		}
		rslt[3] = (double)upSideMaxCnt/(to-from+1);//Weight of being line appeared in up side checking region
		rslt[5] = rslt[5]/(upSideMaxRng+1);//Percentage of total lines appeared in up side checking region
		rslt[7] = (double)(upSideMaxRng+1);//Total checking lines' QTY in up side region
		if((poleChkStopPos-from+1)>5) upPoleChkRatio = (double)upPoleChkMaxCnt/(poleChkStopPos-from+1);
		if(upPoleChkRatio>0.9 && upSideBeLineWeight>0.5){
			if(!curLineIsLonger && (double)upSideMaxIdx/upSideMaxRng>0.85){
				upSideBeLineWeight = currPosBeLineWeight-0.001;//Supose upside data is not reliable
			}else{
				upSideBeLineWeight = 1.0;
				if(currPosBeLineWeight>=0.5) currPosBeLineWeight = 1.0;
			}
		}
		
		//Dn Side Be-line Weight Calculation
		dnSideMaxCnt = 0; dnSideMaxIdx = 0; dnPoleChkMaxCnt = 0;
		for(int i=0; i<=dnSideMaxRng; i++){
			if(dnSideMaxCnt<dnSideCounters[i]){
				dnSideMaxCnt = dnSideCounters[i];
				dnSideMaxIdx = i;
			}
			
			rslt[4] = (double)dnSideCounters[i]/(to-from+1);
			if(rslt[4]>=0.5) rslt[6] = rslt[6] + 1.0;
			
			if(dnPoleChkMaxCnt<dnPoleChkCounters[i]) dnPoleChkMaxCnt = dnPoleChkCounters[i];
		}
		rslt[4] = (double)dnSideMaxCnt/(to-from+1);//Weight of being line appeared in dn side checking region
		rslt[6] = rslt[6]/(dnSideMaxRng+1);//Percentage of total lines appeared in dn side checking region
		rslt[8] = (double)(dnSideMaxRng+1);//Total checking lines' QTY in dn side region
		if((poleChkStopPos-from+1)>5) dnPoleChkRatio = (double)dnPoleChkMaxCnt/(poleChkStopPos-from+1);
		if(dnPoleChkRatio>0.9 && dnSideBeLineWeight>0.5){
			dnSideBeLineWeight = 1.0;
			if(currPosBeLineWeight>=0.5) currPosBeLineWeight = 1.0;
		}
		
		//Get the maximum be-line weight
		if(currPosBeLineWeight<0.5 || upSideBeLineWeight>=currPosBeLineWeight){
			if(currPosBeLineWeight<=upSideBeLineWeight){
				currPosBeLineWeight = upSideBeLineWeight;
				lineGrayDelta = upSideGrayDelta;
				if(currPosBeLineWeight>0.5) rslt[2] = lineOffset - upSideMaxIdx;//Relative offset of current line
			}
			if(currPosBeLineWeight<dnSideBeLineWeight){
				currPosBeLineWeight = dnSideBeLineWeight;
				lineGrayDelta = dnSideGrayDelta;
				if(currPosBeLineWeight>0.5) rslt[2] = lineOffset + dnSideMaxIdx;//Relative offset of current line
			}
		}
		
		//TODO Verification
		if(curLineIsLonger && currPosBeLineWeight<1){
			ArrayList<Double> tmpRslts = getWeightAndOffset(lineOffset,rslt[3],rslt[4],upSideMaxIdx,dnSideMaxIdx,upSideCounters,dnSideCounters);
			currPosBeLineWeight = tmpRslts.get(0);
			rslt[2] = tmpRslts.get(1);
		}
		
		rslt[0] = (to-from)<5?0.0:currPosBeLineWeight;//Weight of being line
		rslt[1] = lineGrayDelta;//Gray delta btw the checked line and the reference line
		
		//Calculate the be-line weight via sharp changes data
		if(!bCalcPoleDistance || bCalcPoleDistance && curLineIsLonger){
			chkProgress = (lineIdx + 1.0) / targetLinesQty;
			upPeaksRatio = (double)upSidePeaks.size()/(to-from+1);
			dnPeaksRatio = (double)dnSidePeaks.size()/(to-from+1);
			
			tmpRatio = (double)(to-upPeaksMinIdx+1)/(to-from+1);
			if((upPeaksRatio>0.4 
					|| upPeaksRatio>0.35 && (targetLinesQty-lineIdx)<=2 
					|| rslt[0]>0.2 && upPeaksRatio>0.2 && chkProgress>0.8
					|| rslt[0]>0.6 && upPeaksRatio>0.2)
				&& tmpRatio>upPeaksRatio){
				upPeaksRatio = tmpRatio;
			}
			
			tmpRatio = (double)(to-dnPeaksMinIdx+1)/(to-from+1);
			if((dnPeaksRatio>0.4 
					|| dnPeaksRatio>0.35 && (targetLinesQty-lineIdx)<=2 
					|| rslt[0]>0.2 && dnPeaksRatio>0.2 && chkProgress>0.8
					|| rslt[0]>0.6 && dnPeaksRatio>0.2)
				&& tmpRatio>dnPeaksRatio){
				dnPeaksRatio = tmpRatio;
			}
			
			if(to-from>5 && rslt[0]<upPeaksRatio) rslt[0] = upPeaksRatio;
			if(to-from>5 && rslt[0]<dnPeaksRatio) rslt[0] = dnPeaksRatio;
		}
		if(curLineIsLonger && !bNotSetInterceptDelta) setLineInterceptDelta((to-from)<5?0.0:rslt[2]);//Set the lineInteceptDelta
		if(null!=currLine) currLine.setWeightOfLongPole(rslt[0]);
		
		return rslt;
	}
	
	private ArrayList<Double> getWeightAndOffset(double lineOffset,double upWeight,double dnWeight,int upMaxIdx,int dnMaxIdx,
			int[] upSideCounters,int[] dnSideCounters){
		ArrayList<Double> rslts = new ArrayList<Double>();
		
		int upValidQty = 0, dnValidQty = 0;
		double ratio = (upSideCounters.length>dnSideCounters.length)?(double)dnSideCounters.length/upSideCounters.length:(double)upSideCounters.length/dnSideCounters.length;
		double ratio2 = (upWeight>dnWeight?dnWeight/upWeight:upWeight/dnWeight);
		boolean getDnData = false;
		
		if(ratio>0.7 && (ratio2>0.85 || upWeight<0.3 && dnWeight<0.3)){
			for(int i=0; i<upSideCounters.length; i++){
				ratio = (double)upSideCounters[i]/upSideCounters[upMaxIdx];
				if(ratio>0.85) upValidQty++;
			}
			for(int i=0; i<dnSideCounters.length; i++){
				ratio = (double)dnSideCounters[i]/dnSideCounters[dnMaxIdx];
				if(ratio>0.85) dnValidQty++;
			}
			
			if((double)dnValidQty/dnSideCounters.length>(double)upValidQty/upSideCounters.length){
				getDnData = true;
				if(dnWeight<0.7 && dnSideCounters.length<7) dnMaxIdx = 0;
			}else{
				getDnData = false;
				if(upWeight<0.7 && upSideCounters.length<7) upMaxIdx = 0;
			}
		}else{
			if(dnWeight>upWeight){
				getDnData = true;
			}else{
				getDnData = false;
			}
		}
		
		if(getDnData){
			rslts.add(0, dnWeight);
			if(dnWeight>0.5){
				rslts.add(1, lineOffset - dnMaxIdx);
			}else{
				rslts.add(1, 0.0);
			}
		}else{
			rslts.add(0, upWeight);
			if(upWeight>0.5){
				rslts.add(1, lineOffset - upMaxIdx);
			}else{
				rslts.add(1, 0.0);
			}
		}
		return rslts;
	}
	
	private int getGrayDelta(int refGray, int[][] imgGrays, int row, int col, int colChkRng){
		int grayDelta = 0, delta = 0, imgHeight = imgGrays[0].length, imgWidth = imgGrays.length;
		
		if(imgWidth>0 && row>=0 && row<imgWidth){
			for(int i=-colChkRng/2;i<=colChkRng/2;i++){
				if(col+i>=0 && col+i<imgHeight){
					delta = refGray - imgGrays[row][col+i];
					if(delta>grayDelta) grayDelta = delta;
				}
			}
		}
		
		return grayDelta;
	}
	
	public int getLineAvgGray(int[][] imgGrays, int startX, double slope, double offset){
		double avgGray = 0.0;
		int imgWidth = imgGrays.length, imgHeight = imgGrays[0].length;
		int nearCol = 0, counter = 0;
		
		if(startX<0) startX = xAxisStop;
		if(offset<0){
			slope = lineSlope;
			offset = lineIntercept + lineInterceptDelta;
		}
		for(int i=startX; i<imgWidth; i++){
			nearCol = (int)(i*slope+offset);
			if(nearCol>=0 && nearCol<imgHeight){
				avgGray += imgGrays[i][nearCol];
				counter++;
			}
		}
		
		return (int)(counter>0?avgGray/counter:0);
	}
	
	public void setSharpChgPos(LinkedHashMap<Integer,Double> chgPos){
		sharpChgPos = chgPos;
	}
	
	public LinkedHashMap<Integer,Double> getSharpChgPos(){
		return sharpChgPos;
	}
	
	public void setSharpChgPosParas(LinkedHashMap<Integer,LinkedHashMap<SharpChgItems,Double>> chgPosPara){
		sharpChgPosParas = chgPosPara;
	}
	
	public LinkedHashMap<Integer,LinkedHashMap<SharpChgItems,Double>> getSharpChgPosParas(){
		return sharpChgPosParas;
	}
	
	public String printSharpChgPos(int lineIdx){
		String str = "", str1 = "", str2 = "", str3 = "";
		String str4 = "", str5 = "", str6 = "";
		if(sharpChgPos.size()>0){
			for(int key:sharpChgPos.keySet()){
				str1 += "," + key;
				str2 += "," + sharpChgPos.get(key);
				if(null!=sharpChgPosParas.get(key)){
					str3 += "," + sharpChgPosParas.get(key).get(SharpChgItems.AREARATIO);
					str4 += "," + sharpChgPosParas.get(key).get(SharpChgItems.STDEV);
					str5 += "," + sharpChgPosParas.get(key).get(SharpChgItems.STDEVRATIO);
					str6 += "," + sharpChgPosParas.get(key).get(SharpChgItems.INNERGRAYRATIO);
				}else{
					str3 += ",0.0";
					str4 += ",0.0";
					str5 += ",0.0";
					str6 += ",0.0";
				}
			}
			str = "L_"+lineIdx+str1+str2+",AREARATIO"+str3+",STDEV"+str4+",STDEVRATIO"+str5;
			str += ",InnerGRatio"+str6;
		}
		return str;
	}
	
	public static void setMinValidPoints(int points){
		minValidPoints = points;
	}
	
	public static int getMinValidPoints(){
		return minValidPoints;
	}
	
	public static void setMaxSideNoise(int noiseCnt){
		maxSideNoise = noiseCnt;
	}
	
	public static int getMaxSideNoise(){
		return maxSideNoise;
	}
	
	public static void setMinRSQ(float minRSquare){
		minRSQ = minRSquare;
	}
	
	public static float getMinRSQ(){
		return minRSQ;
	}
	
	public void setLineIndex(int index){
		lineIndex = index;
	}
	
	public int getLineIndex(){
		return lineIndex;
	}
	
	public void setTendencyRSQ(double rsq){
		tendencyRSQ = rsq;
	}
	
	public double getTendencyRSQ(){
		return tendencyRSQ;
	}
	
	public void setWeightOfLongPole(double weight){
		weightOfLongPole = weight;
	}
	
	public double getWeightOfLongPole(){
		return weightOfLongPole;
	}
	
	public void setLinePassed(boolean passed){
		lineIsPassed = passed;
	}
	
	public boolean getLinePassed(){
		return lineIsPassed;
	}
	
	public void setLineChecked(boolean lineChecked){
		lineIsChecked = lineChecked;
	}
	
	public boolean getLineChecked(){
		return lineIsChecked;
	}
	
	public double getValidRateByYAxis(int min_y, int max_y){
		double validRate = 0.0;
		int val_y = 0, counter = 0;
		
		if(lineYAxis.size()>0){
			for(int i=0; i<lineYAxis.size(); i++){
				val_y = lineYAxis.get(i);
				if(val_y>=min_y && val_y<=max_y) counter++;
			}
			
			validRate = (double)counter/lineYAxis.size();
		}
		
		return validRate;
	}
	
	public void setPolePosition(int pos){
		polePosition = pos;
	}
	
	public int getPolePosition(){
		return polePosition;
	}
	
	public void setXAxisStart(int x){
		xAxisStart = x;
	}
	
	public int getXAxisStart(){
		return xAxisStart;
	}
	
	public void setXAxisStop(int x){
		xAxisStop = x;
	}
	
	public int getXAxisStop(){
		return xAxisStop;
	}
	
	public void setWrinkleParas(double slope, double offset, double angle, double crossX, double minX, boolean upside){
		wrinkleSlope = slope;
		wrinkleOffset = offset;
		wrinkleAngle = angle;
		wrinkleCrossX = crossX;
		wrinkleMinX = minX;
		wrinkleUpward = upside;
	}
	
	/**
	 * 
	 * @return 0-Slope | 1-Offset | 2-Angle | 3-CrossX | 4-MinX | 5-Upward
	 */
	public ArrayList<Object> getWrinkleParas(){
		ArrayList<Object> paras = new ArrayList<Object>();
		
		paras.add(0, wrinkleSlope);
		paras.add(1, wrinkleOffset);
		paras.add(2, wrinkleAngle);
		paras.add(3, wrinkleCrossX);
		paras.add(4, wrinkleMinX);
		paras.add(5, wrinkleUpward);
		
		return paras;
	}
	
	public void setWrinkleScores(double upScore, double dnScore){
		if(upScore>0) wrinkleUpScore = upScore;
		if(dnScore>0) wrinkleDnScore = dnScore;
	}
	
	public double getWrinkleScore(boolean bGetUpScore){
		double score = -1;
		if(bGetUpScore){
			score = wrinkleUpScore;
		}else{
			score = wrinkleDnScore;
		}
		
		return score;
	}
	
	public void setWrinkleLinesQty(boolean bUpSide, int qty){
		if(bUpSide){
			wrinkleUpQty = qty;
		}else{
			wrinkleDnQty = qty;
		}
	}
	
	public int getWrinkleLinesQty(boolean bUpSide){
		if(bUpSide){
			return wrinkleUpQty;
		}else{
			return wrinkleDnQty;
		}
	}
	
	public void setCrossX(int x){
		crossX = x;
	}
	
	public int getCrossX(){
		return crossX;
	}
	
	public void setMeanGray(int gray){
		meanGray = gray;
	}
	
	public int getMeanGray(){
		return meanGray;
	}
	
	public void setRefGray(double gray){
		refGray = gray;
	}
	
	public double getRefGray(){
		return refGray;
	}
	
	public int addPoint(int x, int y, int gray){
		int addCnt = 0;
		if(null==points.get(x+":"+y)){
			if("".equals(xVals)){
				xVals = "" + x;
				yVals = "" + y;
			}else{
				xVals += "," + x;
				yVals += "," + y;
			}
			validPoints++;
			lineXAxis.add(validPoints-1, x);
			lineYAxis.add(validPoints-1, y);
			lineGray.add(validPoints-1, gray);
			points.put(x+":"+y, gray);
			addCnt = 1;
		}
		
		return addCnt;
	}
	
	public int getValidPtsInRange(int minX, int maxX, int chkYRng){
		int pts = 0, val = 0, expectY = 0;
		String[] xData = null, yData = null;
		
		xData = xVals.split(",");
		yData = yVals.split(",");
		for(int i=0; i<xData.length; i++){
			val = Integer.parseInt(xData[i]);
			if(minX<=val && val<=maxX){
				expectY = (int)(val * lineSlope + lineIntercept);
				if(Math.abs(Integer.parseInt(yData[i])-expectY)<=chkYRng) pts++;
			}
		}
		
		return pts;
	}
	
	public String getXVals(String splitor){
		return arrayListToString(lineXAxis,splitor);
	}
	
	public String getYVals(String splitor){
		return arrayListToString(lineYAxis,splitor);
	}
	
	public String getGrayVals(String splitor){
		return arrayListToString(lineGray,splitor);
	}
	
	public String getGrayVals(String Xs, String Ys, String splitor, int[][] grayVals){
		String str1 = "G-1";
		String str2 = "G-0";
		String str3 = "G+1";
		String[] x = Xs.split(splitor);
		String[] y = Ys.split(splitor);
		
		for(int i=0; i<x.length; i++){
			str1 += "," + grayVals[Integer.parseInt(x[i])][Integer.parseInt(y[i])-1];
			str2 += "," + grayVals[Integer.parseInt(x[i])][Integer.parseInt(y[i])];
			str3 += "," + grayVals[Integer.parseInt(x[i])][Integer.parseInt(y[i])+1];
		}
		
		return str1+"\r\n"+str2+"\r\n"+str3;
	}
	
	private String arrayListToString(ArrayList<Integer> arrList,String splitor){
		String str = "";
		
		if(arrList.size()>0){
			str = ""+arrList.get(0);
			if(arrList.size()>1){
				for(int i=1; i<arrList.size(); i++){
					str += "," + arrList.get(i);
				}
			}
		}
		
		return str;
	}
	
	public int getValidPoints(){
		return validPoints;
	}
	
	public int getInvalidPoints(){
		return invalidPoints;
	}
	
	public void addInvalidPoints(int invalidCnt){
		invalidPoints += invalidCnt;
	}
	
	public int getRealPoints(){
		return realPoints;
	}
	
	public void addRealPoints(int x, int y, int realCnt){
		if(null==realPts.get(x+":"+y)){
			realPoints += realCnt;
			lineFittingData.put((double)x, (double)y);
			realPts.put(x+":"+y, realCnt);
		}
	}
	
	public void addSideNoise(int sideNoiseCnt){
		sideNoise += sideNoiseCnt;
	}
	
	public int getNoisePoints(){
		return sideNoise;
	}
	
	public void addOnlinePoint(){
		onlinePoints++;
	}
	
	public int getOnlinePoints(){
		return onlinePoints;
	}
	
	public double getLineSlope(){
		return lineSlope;
	}
	
	public double getLineIntercept(){
		return lineIntercept;
	}
	
	public double getLineRSQ(){
		return lineRSQ;
	}
	
	public double getLineInterceptDelta(){
		return lineInterceptDelta;
	}
	
	public void setLineInterceptDelta(double delta){
		lineInterceptDelta = delta;
	}
	
	public void clearLineData(){
		validPoints = 0;
		invalidPoints = 0;
		sideNoise = 0;
		onlinePoints = 0;
		lineXAxis.clear();
		lineYAxis.clear();
		lineGray.clear();
		points.clear();
		lineSlope = 0.0;
		lineIntercept = 0.0;
		lineRSQ = 0.0;
		meanGray = 0;
		xAxisStart = 0;
		xAxisStop = 0;
		crossX = -1;
		realPoints = 0;
		lineFlag = 0;
		wrinkleFlag = 0;
		lineInterceptDelta = 0.0;
		lineIndex = -1;
		tendencyRSQ = 0.0;
		weightOfLongPole = -1.0;
		lineIsChecked = false;
		xVals = "";
		yVals = "";
		wrinkleSlope = 0.0;
		wrinkleOffset = 0.0;
		wrinkleAngle = 0.0;
		wrinkleCrossX = -1.0;
		wrinkleMinX = -1.0;
		wrinkleUpScore = -1.0;
		wrinkleDnScore = -1.0;
		wrinkleUpward = false;
		polePosition = -1;
		lineIsPassed = false;
		wrinkleUpQty = 0;
		wrinkleDnQty = 0;
	}
	
	public double[] getLineCoef(boolean sortFittingData){
		double[] coef = null;
		if(sortFittingData){
			LinkedHashMap<Double,Double> dtSort = sortFittingData();
			if(null!=dtSort && dtSort.size()>4){
				coef = MathUtils.lineFitting(dtSort);
			}else{
				coef = MathUtils.lineFitting(lineXAxis,lineYAxis);
			}
		}else{
			coef = MathUtils.lineFitting(lineXAxis,lineYAxis);
		}
		lineSlope = coef[0];
		lineIntercept = coef[1];
		lineRSQ = coef[2];
		return coef;
	}
	
	private LinkedHashMap<Double,Double> sortFittingData(){
		LinkedHashMap<Double,Double> fittingDt = null;
		ArrayList<LinkedHashMap<Double,Double>> data = new ArrayList<LinkedHashMap<Double,Double>>();
		
		double maxY = 0.0, minY = 0.0, yVal = 0.0;
		int size = lineYAxis.size(), groupFlag = 0, groupMaxSize = -1, groupMaxIdx = -1, len = 0;
		if(size>0){
			minY = (double)lineYAxis.get(0); maxY = minY;
			for(int i=0; i<size; i++){
				yVal = (double)lineYAxis.get(i);
				if(minY>yVal) minY = yVal;
				if(maxY<yVal) maxY = yVal;
			}
			
			for(int i=0; i<size; i++){
				yVal = (double)lineYAxis.get(i);
				groupFlag = -1;
//				System.out.println("fittingDt:"+i);
				if(8==i){
					System.out.print("");
				}
				for(double y=minY; y<=maxY; y+=3.0){
					groupFlag++;
					if(yVal>=y && yVal<=y+3){
						try {
							fittingDt = data.get(groupFlag);
						} catch (Exception e) {
							fittingDt = null;
						}
						if(null==fittingDt) fittingDt = new LinkedHashMap<Double,Double>();
						fittingDt.put((double)lineXAxis.get(i), yVal);
						if(1==fittingDt.size()){
							len = data.size();
							if(len<=groupFlag){
								for(int j=len; j<=groupFlag; j++){
									data.add(j, null);
								}
							}
						}
						data.set(groupFlag, fittingDt);
						break;
					}
				}
			}
			
			fittingDt = null;
			if(data.size()>0){
				groupMaxSize = 0; groupMaxIdx = -1;
				for(int i=0; i<data.size(); i++){
					fittingDt = data.get(i);
					if(null==fittingDt) continue;
					if(groupMaxSize<fittingDt.size()){
						groupMaxSize = fittingDt.size();
						groupMaxIdx = i;
					}
				}
				if(groupMaxIdx>=0) fittingDt = data.get(groupMaxIdx);
			}
		}
		
		return fittingDt;
	}
	
	/**
	 * 
	 * @param flag 0:Not Checked | 1:Checked | 2:Adjusted | 3:Guessing
	 */
	public void setLineFlag(int flag){
		lineFlag = flag;
	}
	
	/**
	 * 
	 * @return 0:Not Checked | 1:Checked | 2:Adjusted | 3:Guessing
	 */
	public int getLineFlag(){
		return lineFlag;
	}
	
	/**
	 * 
	 * @param flag 0:Not Checked | 1:DN side | 2:UP side | 3:Both side
	 */
	public void setWrinkleFlag(int flag){
		wrinkleFlag = flag;
	}
	
	/**
	 * 
	 * @return 0:Not Checked | 1:DN side | 2:UP side | 3:Both side
	 */
	public int getWrinkleFlag(){
		return wrinkleFlag;
	}
}
