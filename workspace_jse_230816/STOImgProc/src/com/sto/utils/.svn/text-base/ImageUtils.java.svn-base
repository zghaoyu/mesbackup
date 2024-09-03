package com.sto.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.sto.base.PolePositionItems;
import com.sto.base.SharpChgItems;
import com.sto.data.ImgExtractLine;
import com.sto.data.ProductSpec;

public class ImageUtils {
	//Judge Criteria
	private final double minValidRatio = 0.15;//Very depending on the image quality
	private final double minValidRate = 0.7;//Check it in +/-1 line
	private final double maxInvalidRatio = 0.4;//There should be no much white area around the line
	private final double maxSlopeDelta = 0.1;//The lines should be parallel
	private final double minOffsetDelta = 3.0;//There should be some distance between two near line
	private final double maxNoiseRate = 0.4;//There should be no much noise around the line
	private final double maxOffsetChgRate = 1.4;//Check whether some line is missed
	private final double minOffsetChgRate = 0.7;//Check whether there is noise line
	private final int noiseScanRng = 2;//Two sides scanning
	private final int lineScanRange = 6;//One side scanning
	private int targetLinesQty = 16;//The total lines to extract
	private double lineFitGrayThr = 255 * 0.8;
	private double imgProcRegionMinGray = 0.0;
	private final double noiseGrayThr = 255 * 0.9;
	private final double noiseGrayThr2 = 255 * 0.95;
	private final double noiseGrayThr3 = 255 * 0.15;
	private final double maxGrayVal = 255;
	private final double minBestRsq = 0.991;
	private final double firstSharpChgAngle = 35.0;
	private final int lineIndexBase = 100;
	private final int imgProcRegionWBackoff = 50;
	private final int imgProcRegionHBackoff = 40;
	
	private boolean firstPoleIsLonger = false;
	private boolean firstPoleIsThicker = true;
	
	private int[][] finalData = null;
	private boolean logEnabled = true;
	private boolean evenPoleIsLonger = false;
	private long imgProcTime = 0;
	private long imgStartSavingT = 0;
	
	private ProductSpec productSpec = ProductSpec.getInstance();
	private LinkedHashMap<String, Object> criteria = null;
	
	private ArrayList<ImgExtractLine> allPossibleLines = new ArrayList<ImgExtractLine>();
	private ArrayList<ImgExtractLine> allPotentialLines = new ArrayList<ImgExtractLine>();
	private LinkedHashMap<String, Double> imgProcResultLength = new LinkedHashMap<String, Double>();
	private LinkedHashMap<String, Boolean> imgProcResultOK = new LinkedHashMap<String, Boolean>();
	private LinkedHashMap<String, Double> imgProcResultAngle = new LinkedHashMap<String, Double>();
	private LinkedHashMap<String, String> imgProcResultStr = new LinkedHashMap<String, String>();
	
	private void clearMemory(){
		allPossibleLines.clear();
		allPotentialLines.clear();
		imgProcResultLength.clear();
		imgProcResultOK.clear();
		imgProcResultAngle.clear();
	}
	
	public void setLogEnabled(boolean enabled){
		logEnabled = enabled;
	}
	
	public long getImgProcTime(){
		return imgProcTime;
	}
	
	public long getImgStartSavingTime(){
		return imgStartSavingT;
	}
	
	private int getImgProcRegionMinGray(BufferedImage BI, PolePositionItems polePosition){
		int imgWidth = BI.getWidth(), imgHeight = BI.getHeight();
		int[] fourCornersGray = new int[4];
        int pixel = 0, minGray = 0, avgPts = 10;
        int start = 0, stop = 0, width = 0;
        
		switch(polePosition){
		case TOPLEFT:
			start = (int)(imgHeight*0.25); stop = start-avgPts;
	        width = (int)(imgWidth*0.25);
	        for(int height=start; height>stop; height--){
	        	pixel = BI.getRGB(width, height);
	        	fourCornersGray[0] += (pixel & 0xffffff - 0xffff00);
	        }
	        minGray = fourCornersGray[0] / avgPts;
			break;
		case TOPRIGHT:
			start = (int)(imgHeight*0.25); stop = start-avgPts;
	        width = (int)(imgWidth*0.75);
	        for(int height=start; height>stop; height--){
	        	pixel = BI.getRGB(width, height);
	        	fourCornersGray[1] += (pixel & 0xffffff - 0xffff00);
	        }
	        minGray = fourCornersGray[1] / avgPts;
			break;
		case BOTTOMRIGHT:
			start = (int)(imgHeight*0.75); stop = start+avgPts;
	        width = (int)(imgWidth*0.75);
	        for(int height=start; height<stop; height++){
	        	pixel = BI.getRGB(width, height);
	        	fourCornersGray[2] += (pixel & 0xffffff - 0xffff00);
	        }
	        minGray = fourCornersGray[2] / avgPts;
			break;
		case BOTTOMLEFT:
			start = (int)(imgHeight*0.75); stop = start+avgPts;
	        width = (int)(imgWidth*0.25);
	        for(int height=start; height<stop; height++){
	        	pixel = BI.getRGB(width, height);
	        	fourCornersGray[3] += (pixel & 0xffffff - 0xffff00);
	        }
	        minGray = fourCornersGray[3] / avgPts;
			break;
		}
		
		return minGray;
	}
	
	private PolePositionItems getPolePositionInImage(BufferedImage BI){
		PolePositionItems polePosition = PolePositionItems.TOPLEFT;
		int imgWidth = BI.getWidth(), imgHeight = BI.getHeight();
		double[] fourCornersGray = new double[]{0.0,0.0,0.0,0.0};
        int pixel = 0, minGrayIdx = 0, nearCol = 0, counter = 0;
        double slope = 0.0, offset = 0.0, minGray = 0.0;
        
        int start = 0, stop = (int)(imgWidth*0.5); counter = 0;
        slope = (-1.0)*imgHeight/imgWidth; offset = 0.5*imgHeight;
        for(int width=start; width<stop; width+=10){
        	nearCol = (int)(width*slope+offset);
        	if(nearCol<0) nearCol = 0;
        	if(nearCol>=imgHeight) nearCol = imgHeight-1;
        	pixel = BI.getRGB(width, nearCol);
        	fourCornersGray[0] += (double)(pixel & 0xffffff - 0xffff00);
        	counter++;
        }
        fourCornersGray[0] = fourCornersGray[0] / counter;
        
        start = (int)(imgWidth*0.5); stop = imgWidth; counter = 0;
        slope = (double)imgHeight/imgWidth; offset = (-0.5)*imgWidth*slope;
        for(int width=start; width<stop; width+=10){
        	nearCol = (int)(width*slope+offset);
        	if(nearCol<0) nearCol = 0;
        	if(nearCol>=imgHeight) nearCol = imgHeight-1;
        	pixel = BI.getRGB(width, nearCol);
        	fourCornersGray[1] += (double)(pixel & 0xffffff - 0xffff00);
        	counter++;
        }
        fourCornersGray[1] = fourCornersGray[1] / counter;
        
        start = (int)(imgWidth*0.5); stop = imgWidth; counter = 0;
        slope = (-1.0)*imgHeight/imgWidth; offset = 0.5*imgHeight - slope*imgWidth;
        for(int width=start; width<stop; width+=10){
        	nearCol = (int)(width*slope+offset);
        	if(nearCol<0) nearCol = 0;
        	if(nearCol>=imgHeight) nearCol = imgHeight-1;
        	pixel = BI.getRGB(width, nearCol);
        	fourCornersGray[2] += (double)(pixel & 0xffffff - 0xffff00);
        	counter++;
        }
        fourCornersGray[2] = fourCornersGray[2] / counter;
        
        start = 0; stop = (int)(imgWidth*0.5); counter = 0;
        slope = (double)imgHeight/imgWidth; offset = 0.5*imgHeight;
        for(int width=start; width<stop; width+=10){
        	nearCol = (int)(width*slope+offset);
        	if(nearCol<0) nearCol = 0;
        	if(nearCol>=imgHeight) nearCol = imgHeight-1;
        	pixel = BI.getRGB(width, nearCol);
        	fourCornersGray[3] += (double)(pixel & 0xffffff - 0xffff00);
        	counter++;
        }
        fourCornersGray[3] = fourCornersGray[3] / counter;
        
        minGray = fourCornersGray[0];
        minGrayIdx = 0;
        for(int i=0; i<fourCornersGray.length; i++){
        	if(fourCornersGray[i]<minGray){
        		minGray = fourCornersGray[i];
        		minGrayIdx = i;
        	}
        }
        
        switch(minGrayIdx){
        case 0:
        	polePosition = PolePositionItems.TOPLEFT;
        	break;
        case 1:
        	polePosition = PolePositionItems.TOPRIGHT;
        	break;
        case 2:
        	polePosition = PolePositionItems.BOTTOMRIGHT;
        	break;
        case 3:
        	polePosition = PolePositionItems.BOTTOMLEFT;
        	break;
        }
        imgProcRegionMinGray = getImgProcRegionMinGray(BI, polePosition);
        
		return polePosition;
	}
	
	private LinkedHashMap<PolePositionItems,Integer> getLayersQty(){
		LinkedHashMap<PolePositionItems,Integer> layers = new LinkedHashMap<PolePositionItems,Integer>();
		
		layers.put(PolePositionItems.BOTTOMRIGHT, 
				Integer.parseInt(""+criteria.get("lowerRightCornerLayers")));
		
		layers.put(PolePositionItems.BOTTOMLEFT, 
				Integer.parseInt(""+criteria.get("lowerLeftCornerLayers")));
		
		layers.put(PolePositionItems.TOPRIGHT, 
				Integer.parseInt(""+criteria.get("topRightCornerLayers")));
		
		layers.put(PolePositionItems.TOPLEFT, 
				Integer.parseInt(""+criteria.get("topLeftCornerLayers")));
		
		return layers;
	}
	
	private void setFirstPoleConfig(){
		String c1 = (String) criteria.get("firstPoleIsShorter");
		String c2 = (String) criteria.get("firstPoleIsThicker");
		
		if(null!=c1) firstPoleIsLonger = (1==Integer.parseInt(c1)?false:true);
		if(null!=c2) firstPoleIsThicker = (1==Integer.parseInt(c2)?true:false);
	}
	
	private ArrayList<Object> edgeFoundParas(BufferedImage BI, PolePositionItems polePosition, double baseGF1, double baseGF2, double slopeThr
		, int layersQty, boolean searchWidth
		, int i, int baseAvgPts, int searchStart, int searchStop, int searchPos, int continueCnt1, int continueCnt2, int continueCnt3
		, int continueCntThr, int backOffPixels, int edgeStart, int edgeStop, int maxLen
		, double currGray, double baseG1, double baseG2, double currSlope, double startFactor, double grapFactor, int[] heightRng
		, ArrayList<Double> slopes, double[] searchParas){
		
		ArrayList<Object> edgeParas = new ArrayList<Object>();
		int imgWidth = BI.getWidth();
        int imgHeight = BI.getHeight();
        boolean edgeFound = false, forwardSearch = searchStart>searchStop?false:true;
        
        if(searchWidth){
        	maxLen = imgWidth - 1;
        }else{
        	maxLen = imgHeight - 1;
        }
        
        if(currSlope*(forwardSearch?1:-1)<slopeThr*0.6 && currGray<noiseGrayThr2){
        	continueCnt3++;
        	if(continueCnt3>=continueCntThr) edgeFound = true;
        }else{
        	continueCnt3 = 0;
        }
        
        if(currSlope*(forwardSearch?1:-1)<slopeThr && currGray<baseG2){
        	continueCnt2++;
        	if(continueCnt2>=continueCntThr*0.6) edgeFound = true;
        }else{
        	baseG2 = searchParas[1] * baseGF2;
        	continueCnt2 = 0;
        }
        
		if(!edgeFound && currGray>imgProcRegionMinGray && currGray<baseG1){
    		continueCnt1++;
    		if(continueCnt1>=continueCntThr){//The starting position could be in the pole region, need to enlarge the window
    			if(forwardSearch && searchStart>=i-continueCntThr || !forwardSearch && searchStart<=i+continueCntThr){
    				startFactor = startFactor*(forwardSearch?0.8:1.25);
    				if(searchWidth){
    					searchStart = (int)(imgWidth*startFactor);
    				}else{
    					searchStart = (int)(imgHeight*startFactor);
    				}
    				if(forwardSearch && searchStart>baseAvgPts || !forwardSearch && searchStart<(searchWidth?imgWidth:imgHeight)-baseAvgPts){
    					i = searchStart;
    					searchParas = getEdgeSearchParas(BI, searchStart, baseAvgPts, searchPos, searchWidth, forwardSearch);
    					baseG1 = searchParas[1] * baseGF1;
    					baseG2 = searchParas[1] * baseGF2;
    					slopes.clear();
    					slopes.add(searchParas[2]);
        				continueCnt1=0;
    				}
    			}
    			if(continueCnt1>=continueCntThr) edgeFound = true;
    		}
    		
    		if(searchWidth && !edgeFound && currGray<noiseGrayThr3 && null==heightRng){//Already enter the very dark region(almost search to the picture edge)
    			heightRng = getImgProcEdges(BI,polePosition,layersQty,false,-1,-1,-1);
				searchPos = (heightRng[0]+heightRng[1])/2;
				if(polePosition==PolePositionItems.BOTTOMRIGHT || polePosition==PolePositionItems.TOPRIGHT){
					startFactor = 0.25;
				}else{
					startFactor = 0.75;
				}
				searchStart = getEdgeSearchStart(BI, polePosition, startFactor, baseGF1, searchWidth, searchPos, baseAvgPts);
				
				i = searchStart;
				searchParas = getEdgeSearchParas(BI, searchStart, baseAvgPts, searchPos, searchWidth, searchStart>searchStop?false:true);
				baseG1 = searchParas[1] * baseGF1;
				baseG2 = searchParas[1] * baseGF2;
				slopes.clear();
				slopes.add(searchParas[2]);
				continueCnt1=0;
    		}
    	}else{
    		baseG1 = searchParas[1] * baseGF1;
    		continueCnt1=0;
    	}
		
		if(edgeFound){
			if(forwardSearch){
				edgeStart = i - backOffPixels;
    			if(edgeStart<0) edgeStart = 0;
    			if(searchWidth){
    				edgeStop = edgeStart + (int)(imgWidth*grapFactor);
    			}else{
    				edgeStop = edgeStart + (int)(imgHeight*grapFactor);
    			}
    			if(edgeStop>maxLen) edgeStop = maxLen;
			}else{
				edgeStart = i + backOffPixels;
    			if(edgeStart>maxLen) edgeStart = maxLen;
    			if(searchWidth){
    				edgeStop = edgeStart - (int)(imgWidth*grapFactor);
    			}else{
    				edgeStop = edgeStart - (int)(imgHeight*grapFactor);
    			}
    			if(edgeStop<0) edgeStop = 0;
			}
		}
		
		edgeParas.add(0, edgeFound);
		edgeParas.add(1, continueCnt1);
		edgeParas.add(2, searchStart);
		edgeParas.add(3, edgeStart);
		edgeParas.add(4, edgeStop);
		edgeParas.add(5, baseG1);
		edgeParas.add(6, startFactor);
		edgeParas.add(7, heightRng);
		edgeParas.add(8, continueCnt2);
		edgeParas.add(9, baseG2);
		edgeParas.add(10, continueCnt3);
		
		return edgeParas;
	}
	
	private int getEdgeSearchStart(BufferedImage BI, PolePositionItems polePosition, double startFactor, double baseFactor, boolean searchWidth, int fixPos, int baseAvgPts){
		int searchStart = 0, pixel = 0, gray = 0;
		int imgWidth = BI.getWidth();
        int imgHeight = BI.getHeight();
        double baseGVal = 0.0;
        
		if(searchWidth){
			searchStart = (int)(imgWidth*startFactor);
			if(polePosition==PolePositionItems.BOTTOMRIGHT || polePosition==PolePositionItems.TOPRIGHT){
				while(true){
		        	baseGVal = 0.0;
		        	for(int k=searchStart-baseAvgPts; k<searchStart; k++){
		        		pixel = BI.getRGB(k, fixPos);
		            	gray = (pixel & 0xffffff - 0xffff00);
		        		baseGVal += gray;
		        	}
		        	baseGVal = baseGVal / baseAvgPts;
		        	if(baseGVal>=noiseGrayThr){
		        		break;
		        	}else{
		        		startFactor = startFactor*0.8;
	    				searchStart = (int)(imgWidth*startFactor);
	    				if(searchStart<=baseAvgPts){
	    					searchStart = baseAvgPts;
	    					break;
	    				}
		        	}
	        	}
			}else{
				while(true){
		        	baseGVal = 0.0;
		        	for(int k=searchStart+baseAvgPts; k>searchStart; k--){
		        		pixel = BI.getRGB(k, fixPos);
		            	gray = (pixel & 0xffffff - 0xffff00);
		        		baseGVal += gray;
		        	}
		        	baseGVal = baseGVal / baseAvgPts;
		        	if(baseGVal>=noiseGrayThr){
		        		break;
		        	}else{
		        		startFactor = startFactor*1.25;
		        		searchStart = (int)(imgWidth*startFactor);
	    				if(searchStart>=imgWidth-baseAvgPts){
	    					searchStart = imgWidth-baseAvgPts;
	    					break;
	    				}
		        	}
	        	}
			}
		}else{
			searchStart = (int)(imgHeight*startFactor);
			if(polePosition==PolePositionItems.BOTTOMRIGHT || polePosition==PolePositionItems.BOTTOMLEFT){
				while(true){
		        	baseGVal = 0.0;
		        	for(int k=searchStart-baseAvgPts; k<searchStart; k++){
		        		pixel = BI.getRGB(fixPos, k);
		            	gray = (pixel & 0xffffff - 0xffff00);
		        		baseGVal += gray;
		        	}
		        	baseGVal = baseGVal / baseAvgPts;
		        	if(baseGVal>=noiseGrayThr){
		        		break;
		        	}else{
		        		startFactor = startFactor*0.8;
		        		searchStart = (int)(imgHeight*startFactor);
	    				if(searchStart<=baseAvgPts){
	    					searchStart = baseAvgPts;
	    					break;
	    				}
		        	}
	        	}
			}else{
				while(true){
		        	baseGVal = 0.0;
		        	for(int k=searchStart+baseAvgPts; k>searchStart; k--){
		        		pixel = BI.getRGB(fixPos, k);
		            	gray = (pixel & 0xffffff - 0xffff00);
		        		baseGVal += gray;
		        	}
		        	baseGVal = baseGVal / baseAvgPts;
		        	if(baseGVal>=noiseGrayThr){
		        		break;
		        	}else{
		        		startFactor = startFactor*1.25;
		        		searchStart = (int)(imgHeight*startFactor);
	    				if(searchStart>=imgHeight-baseAvgPts){
	    					searchStart = imgHeight - baseAvgPts;
	    					break;
	    				}
		        	}
	        	}
			}
		}
		
		return searchStart;
	}
	
	private ArrayList<Object> getEdgeSearchCnds(BufferedImage BI, PolePositionItems polePosition, double baseFactor, int baseAvgPts, int layersQty, boolean searchWidth, int grapWidthStart, int grapWidthStop){
		int edgeStart = 0, edgeStop = 0, searchPos = 0;
		int searchStart = 0, searchStop = 0;
		int imgWidth = BI.getWidth();
        int imgHeight = BI.getHeight();
		double grapFactor = 0.0, searchLineFactor = 0.0;
		double startFactor = 0.0;
		ArrayList<Object> rtnParas = new ArrayList<Object>();
		
		if(searchWidth){
			grapFactor = 0.375;
			if(polePosition==PolePositionItems.BOTTOMRIGHT || polePosition==PolePositionItems.TOPRIGHT){
	        	searchLineFactor = 0.85; startFactor = 0.25;
	        	if(polePosition==PolePositionItems.TOPRIGHT) searchLineFactor = 0.15;
	        	searchPos = (int)(imgHeight*searchLineFactor);
	        	searchStart = (int)(imgWidth*startFactor); searchStop = imgWidth-1;
	        	if(grapWidthStart>0 && grapWidthStop>0 && grapWidthStop>grapWidthStart && grapWidthStop<=searchStop){
	        		searchStart = grapWidthStart; searchStop = imgWidth-1;
	        	}
	        	
	        	edgeStart = searchStart;
				edgeStop = searchStart + (int)(imgWidth*grapFactor);
				if(edgeStop>searchStop) edgeStop = searchStop;
				if(grapWidthStart<0 || grapWidthStop<0){
					searchStart = getEdgeSearchStart(BI, polePosition, startFactor, baseFactor, searchWidth, searchPos, baseAvgPts);
				}
			}else{
				searchLineFactor = 0.15; startFactor = 0.75;
	        	if(polePosition==PolePositionItems.BOTTOMLEFT) searchLineFactor = 0.85;
	        	searchPos = (int)(imgHeight*searchLineFactor);
	        	searchStart = (int)(imgWidth*startFactor); searchStop = 0;
	        	if(grapWidthStart>0 && grapWidthStop>0 && grapWidthStart>grapWidthStop && grapWidthStart<imgWidth){
	        		searchStart = grapWidthStart; searchStop = 0;
	        	}
	        	
	        	edgeStart = searchStart;
				edgeStop = searchStart - (int)(imgWidth*grapFactor);
				if(edgeStop<0) edgeStop = 0;
				if(grapWidthStart<0 || grapWidthStop<0){
					searchStart = getEdgeSearchStart(BI, polePosition, startFactor, baseFactor, searchWidth, searchPos, baseAvgPts);
				}
			}
		}else{
			if(layersQty<=8){
				grapFactor = 0.25;
	        }else{
	        	grapFactor = 0.32 / 8.0 * layersQty;
	        }
			
			if(polePosition==PolePositionItems.BOTTOMRIGHT || polePosition==PolePositionItems.BOTTOMLEFT){
	        	searchLineFactor = 0.75; startFactor = 0.375;
	        	if(polePosition==PolePositionItems.BOTTOMLEFT) searchLineFactor = 0.25;
	        	if(grapWidthStart<0 || grapWidthStop<0){
	        		searchPos = (int)(imgWidth*searchLineFactor);
	        	}else{
	        		searchPos = (grapWidthStart + grapWidthStop)/2;
	        	}
	        	searchStart = (int)(imgHeight*startFactor); searchStop = imgHeight-1;
	        	
	        	edgeStart = searchStart;
				edgeStop = searchStart + (int)(imgHeight*grapFactor);
				searchStart = getEdgeSearchStart(BI, polePosition, startFactor, baseFactor, searchWidth, searchPos, baseAvgPts);
			}else{
				searchLineFactor = 0.25; startFactor = 0.625;
				if(polePosition==PolePositionItems.TOPRIGHT) searchLineFactor = 0.75;
				if(grapWidthStart<0 || grapWidthStop<0){
	        		searchPos = (int)(imgWidth*searchLineFactor);
	        	}else{
	        		searchPos = (grapWidthStart + grapWidthStop) / 2;
	        	}
	        	searchStart = (int)(imgHeight*startFactor); searchStop = 0;
	        	
	        	edgeStart = searchStart;
				edgeStop = searchStart - (int)(imgHeight*grapFactor);
				searchStart = getEdgeSearchStart(BI, polePosition, startFactor, baseFactor, searchWidth, searchPos, baseAvgPts);
			}
		}
		
		rtnParas.add(0, searchStart);
		rtnParas.add(1, searchStop);
		rtnParas.add(2, searchPos);
		rtnParas.add(3, edgeStart);
		rtnParas.add(4, edgeStop);
		rtnParas.add(5, startFactor);
		rtnParas.add(6, grapFactor);
		
		return rtnParas;
	}
	
	private int[] getImgProcEdges(BufferedImage BI, PolePositionItems polePosition, int layersQty, boolean searchWidth, int grapWidthStart, int grapWidthStop, int fixedSearchPos){
		int edgeStart = 0, edgeStop = 0, backOffPixels = 0, baseAvgPts = 10, searchPos = 0;
		int continueCnt1 = 0, continueCnt2 = 0, continueCnt3=0, continueCntThr = 5, maxLen = 0, searchStart = 0, searchStop = 0;
		int imgWidth = BI.getWidth();
        int imgHeight = BI.getHeight();
        int[] heightRng = null;
        boolean edgeFound = false;
		double baseG1 = 0.0, baseG2 = 0.0, grapFactor = 0.0, currGray = 0.0, startFactor = 0.0;
		double slopeThr = -1.5, baseGF1 = 0.95, baseGF2 = 0.97;
		double[] searchParas = null;
		String strRawTt = "", strRawDt = "", strSlope = "";
		ArrayList<Double> slopes = new ArrayList<Double>();
		ArrayList<Object> edgeParas = null, searchCnds = null;
		
		if(searchWidth){
			maxLen = imgWidth - 1; backOffPixels = imgProcRegionWBackoff;
		}else{
			maxLen = imgHeight - 1; backOffPixels = imgProcRegionHBackoff;
		}
		
		searchCnds = getEdgeSearchCnds(BI, polePosition, baseGF1, baseAvgPts, layersQty, searchWidth, grapWidthStart, grapWidthStop);
		searchStart = (int) searchCnds.get(0);
		searchStop = (int) searchCnds.get(1);
		searchPos = (int) searchCnds.get(2);
		edgeStart = (int) searchCnds.get(3);
		edgeStop = (int) searchCnds.get(4);
		startFactor = (double) searchCnds.get(5);
		grapFactor = (double) searchCnds.get(6);
		if(fixedSearchPos>0 && fixedSearchPos<imgHeight) searchPos = fixedSearchPos;
		
		searchParas = getEdgeSearchParas(BI, searchStart, baseAvgPts, searchPos, searchWidth, searchStart>searchStop?false:true);
		currGray = searchParas[0];
		baseG1 = searchParas[1] * baseGF1;
		baseG2 = searchParas[1] * baseGF2;
		strRawTt = searchWidth?"X":"Y"; strRawDt = "G"; strSlope = "S";
		
		if(searchStart>searchStop){
        	for(int i=searchStart; i>=searchStop; i--){
        		searchParas = getEdgeSearchParas(BI, i, baseAvgPts, searchPos, searchWidth, false);
        		currGray = searchParas[0];
        		slopes.add(searchParas[2]);
        		if(logEnabled){
	        		strRawTt += "," + i;
	        		strRawDt += "," + currGray;
	        		strSlope += "," + searchParas[2];
        		}
        		
        		edgeParas = edgeFoundParas(BI, polePosition, baseGF1, baseGF2, slopeThr, layersQty, searchWidth
        				, i, baseAvgPts, searchStart, searchStop, searchPos, continueCnt1, continueCnt2, continueCnt3
        				, continueCntThr, backOffPixels, edgeStart, edgeStop, maxLen
        				, currGray, baseG1, baseG2, searchParas[2], startFactor, grapFactor, heightRng
        				, slopes, searchParas);
        		
        		edgeFound = (boolean) edgeParas.get(0);
        		if(edgeFound){
        			edgeStart = (int) edgeParas.get(3);
        			edgeStop = (int) edgeParas.get(4);
        			break;
        		}else{
        			continueCnt1 = (int) edgeParas.get(1);
        			searchStart = (int) edgeParas.get(2);
        			baseG1 = (double) edgeParas.get(5);
        			startFactor = (double) edgeParas.get(6);
        			if(searchWidth) heightRng = (int[]) edgeParas.get(7);
        			continueCnt2 = (int) edgeParas.get(8);
        			baseG2 = (double) edgeParas.get(9);
        			continueCnt3 = (int) edgeParas.get(10);
        		}
        	}
        }else{
        	for(int i=searchStart; i<=searchStop; i++){
        		searchParas = getEdgeSearchParas(BI, i, baseAvgPts, searchPos, searchWidth, true);
        		currGray = searchParas[0];
        		slopes.add(searchParas[2]);
        		if(logEnabled){
	        		strRawTt += "," + i;
	        		strRawDt += "," + currGray;
	        		strSlope += "," + searchParas[2];
        		}
        		
        		edgeParas = edgeFoundParas(BI, polePosition, baseGF1, baseGF2, slopeThr, layersQty, searchWidth
        				, i, baseAvgPts, searchStart, searchStop, searchPos, continueCnt1, continueCnt2, continueCnt3
        				, continueCntThr, backOffPixels, edgeStart, edgeStop, maxLen
        				, currGray, baseG1, baseG2, searchParas[2], startFactor, grapFactor, heightRng
        				, slopes, searchParas);
        		
        		edgeFound = (boolean) edgeParas.get(0);
        		if(edgeFound){
        			edgeStart = (int) edgeParas.get(3);
        			edgeStop = (int) edgeParas.get(4);
        			break;
        		}else{
        			continueCnt1 = (int) edgeParas.get(1);
        			searchStart = (int) edgeParas.get(2);
        			baseG1 = (double) edgeParas.get(5);
        			startFactor = (double) edgeParas.get(6);
        			if(searchWidth) heightRng = (int[]) edgeParas.get(7);
        			continueCnt2 = (int) edgeParas.get(8);
        			baseG2 = (double) edgeParas.get(9);
        			continueCnt3 = (int) edgeParas.get(10);
        		}
        	}
        }
        
		if(logEnabled){
			LogUtils.rawLog("imgProcRegion_", "EdgeStart,EdgeStop,"+strRawTt);
			LogUtils.rawLog("imgProcRegion_", edgeStart+","+edgeStop+","+strRawDt);
			LogUtils.rawLog("imgProcRegion_", "0,0,"+strSlope);
		}
		return new int[]{edgeStart, edgeStop};
	}
	
	private double[] getEdgeSearchParas(BufferedImage BI, int currIdx, int baseAvgPts, int searchPos, boolean searchWidth, boolean forwardSearch){
		ArrayList<Integer> xAxis = new ArrayList<Integer>();
		ArrayList<Integer> yAxis = new ArrayList<Integer>();
		int pixel = 0, gray = 0, start = 0, stop = 0, currGray = 0;
		double avgGray = 0.0;
		double[] coef = null;
		
		if(forwardSearch){
			start = currIdx - baseAvgPts + 1; stop = currIdx;
		}else{
			start = currIdx; stop = currIdx + baseAvgPts - 1;
		}
		
		if(searchWidth){
			pixel = BI.getRGB(currIdx, searchPos);
			currGray = (pixel & 0xffffff - 0xffff00);
		}else{
			pixel = BI.getRGB(searchPos, currIdx);
			currGray = (pixel & 0xffffff - 0xffff00);
		}
		
		for(int i=start; i<=stop; i++){
			if(searchWidth){
    			pixel = BI.getRGB(i, searchPos);
            	gray = (pixel & 0xffffff - 0xffff00);
    		}else{
    			pixel = BI.getRGB(searchPos, i);
            	gray = (pixel & 0xffffff - 0xffff00);
    		}
			xAxis.add(i);
			yAxis.add(gray);
			avgGray += gray;
		}
		
		avgGray = avgGray / baseAvgPts;
		coef = MathUtils.lineFitting(xAxis, yAxis);
		
		return new double[]{(double)currGray,avgGray,coef[0]*coef[0]*(coef[0]<0?-1:1)};
	}
	
	private boolean imgProcRegionIsOK(BufferedImage BI, boolean chkWidthRng, int regionStart, int regionStop, int fixedPos){
		boolean ok = true;
		double stdev = 0.0, okMinStdev = lineScanRange / 2;
		int chkDataPts = lineScanRange * 5;
		int imgWidth = BI.getWidth(), imgHeight = BI.getHeight();
		ArrayList<Integer> data = new ArrayList<Integer>();
		
		if(chkWidthRng){
			if(fixedPos>0 && fixedPos<imgHeight){
				if(regionStart>regionStop){
					regionStart = regionStart - imgProcRegionWBackoff;
					for(int i=regionStart; i>regionStart-chkDataPts; i--){
						if(i<0 || i>imgWidth) break;
						data.add((BI.getRGB(i, fixedPos) & 0xffffff - 0xffff00));
					}
				}else{
					regionStart = regionStart + imgProcRegionWBackoff;
					for(int i=regionStart; i<regionStart+chkDataPts; i++){
						if(i>imgWidth) break;
						if(i<0) continue;
						data.add((BI.getRGB(i, fixedPos) & 0xffffff - 0xffff00));
					}
				}
				
				if(data.size()>(lineScanRange*4)){
					stdev = MathUtils.getStdev(data);
					if(stdev<okMinStdev) ok = false;
				}
			}
		}
		
		return ok;
	}
	
	private int[] getImageProcRegion(BufferedImage BI, PolePositionItems polePosition, int layersQty){
		int[] grapImgStart = new int[4];
		int[] grapImgWidthRng = null, grapImgHeightRng = null, grapImgWidthRng2 = null;
		int fixPos = 0, imgWidth = BI.getWidth();
		boolean widthRngOK = true, needDblCfm = false;
		
		grapImgWidthRng = getImgProcEdges(BI, polePosition, layersQty, true, -1, -1, -1);
		grapImgHeightRng = getImgProcEdges(BI,polePosition,layersQty,false,grapImgWidthRng[0],grapImgWidthRng[1],-1);
		grapImgStart[0] = grapImgHeightRng[0];
        grapImgStart[1] = grapImgHeightRng[1];
        grapImgStart[2] = grapImgWidthRng[0];
        grapImgStart[3] = grapImgWidthRng[1];
        
        //Adjust the width region
		fixPos = (int)((grapImgHeightRng[0]+grapImgHeightRng[1])*0.5);
		widthRngOK = imgProcRegionIsOK(BI, true, grapImgStart[2], grapImgStart[3], fixPos);
		grapImgWidthRng2 = getImgProcEdges(BI, polePosition, layersQty, true, -1, -1, fixPos);
        if(polePosition==PolePositionItems.BOTTOMRIGHT || polePosition==PolePositionItems.TOPRIGHT){
			if(grapImgWidthRng2[0]<grapImgWidthRng[0] || !widthRngOK){
				needDblCfm = true;
			}
		}else{
			if(grapImgWidthRng2[0]>grapImgWidthRng[0] || !widthRngOK){
				needDblCfm = true;
			}
		}
        
        if(needDblCfm){
        	needDblCfm = false;
        	grapImgWidthRng2 = getImgProcEdges(BI, polePosition, layersQty, true, -1, -1, fixPos+30);
            if(polePosition==PolePositionItems.BOTTOMRIGHT || polePosition==PolePositionItems.TOPRIGHT){
    			if(grapImgWidthRng2[0]<grapImgWidthRng[0] || !widthRngOK){
    				needDblCfm = true;
    			}
    		}else{
    			if(grapImgWidthRng2[0]>grapImgWidthRng[0] || !widthRngOK){
    				needDblCfm = true;
    			}
    		}
            
            if(needDblCfm){
	            grapImgWidthRng2 = getImgProcEdges(BI, polePosition, layersQty, true, -1, -1, fixPos-30);
	            if(polePosition==PolePositionItems.BOTTOMRIGHT || polePosition==PolePositionItems.TOPRIGHT){
	    			if(grapImgWidthRng2[0]<grapImgWidthRng[0] || !widthRngOK){
	    				grapImgStart[2] = grapImgWidthRng2[0];
	    		        grapImgStart[3] = grapImgWidthRng2[1];
	    			}
	    		}else{
	    			if(grapImgWidthRng2[0]>grapImgWidthRng[0] || !widthRngOK){
	    				grapImgStart[2] = grapImgWidthRng2[0];
	    		        grapImgStart[3] = grapImgWidthRng2[1];
	    			}
	    		}
            }
        }
        
        //Pint the center search line
        if(logEnabled){
	        System.out.println("widthRng:("+grapImgStart[2]+","+grapImgStart[3]+"),"+(widthRngOK?"OK":"NG")+"("+grapImgWidthRng[0]+","+grapImgWidthRng[1]+")");
	        if(grapImgStart[2]>grapImgStart[3]){
		        for(int i=grapImgStart[2]; i>(grapImgStart[2]-150); i--){
		    		System.out.print((BI.getRGB(i, fixPos) & 0xffffff - 0xffff00)+",");
		    	}
	        }else{
	        	for(int i=grapImgStart[2]; i<(grapImgStart[2]+150); i++){
	        		if(i>=imgWidth) break;
		    		System.out.print((BI.getRGB(i, fixPos) & 0xffffff - 0xffff00)+",");
		    	}
	        }
	        System.out.println("");
        }
        
		return grapImgStart;
	}
	
	public boolean procImage(String imgRootDir, String imgFileName, boolean outImg, String specifiedSpec){
		boolean imgPassed = false;
		
        try {
			imgPassed = procImageEx(imgRootDir, imgFileName, outImg, specifiedSpec);
			clearMemory();
		} catch (Exception e) {
			LogUtils.errorLog("Process "+imgRootDir+File.separator+imgFileName+" error:"+e.getMessage());
		}
        
        return imgPassed;
	}
	
	private boolean procImageEx(String imgRootDir, String imgFileName, boolean outImg, String specifiedSpec){
		String src = imgRootDir + File.separator + imgFileName;
		criteria = productSpec.getData(imgRootDir);
		if(null==criteria){
			if(null!=specifiedSpec && !"".equals(specifiedSpec.trim())) criteria = productSpec.getData(specifiedSpec);
			if(null==criteria){
				System.out.println("Product Spec for "+imgRootDir+" is not set yet!");
				return false;
			}
		}
		
		long startProcTime = System.currentTimeMillis();
		File file = new File(src);
        BufferedImage BI = null;
        try{
            BI = ImageIO.read(file);
            if(null==BI){
            	System.out.println("Read "+src+" failed");
            	return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        
        int imgWidth = BI.getWidth();
        int imgHeight = BI.getHeight();
        int grapHeightStart = 0, grapHeightStop = 0, grapHeight = 0;
        int grapWidthStart = 0, grapWidthStop = 0, grapWidth = 0;
        int[][] imgGrays = null, initVals = null, peakVals = null;
        int pixel = 0, gray = 0;
        
        PolePositionItems polePosition = getPolePositionInImage(BI);
        System.out.println("Pole Position:"+polePosition);
        
        LinkedHashMap<PolePositionItems,Integer> layers = getLayersQty();
        targetLinesQty = layers.get(polePosition)*2;
        if(targetLinesQty<=0) return false;
        LogUtils.clearLog(true);
        
        int[] imgProcRegion = getImageProcRegion(BI,polePosition,targetLinesQty/2);
        grapHeightStart = imgProcRegion[0]; grapHeightStop = imgProcRegion[1];
        grapWidthStart = imgProcRegion[2]; grapWidthStop = imgProcRegion[3];
        grapHeight = Math.abs(grapHeightStart-grapHeightStop);
        grapWidth = Math.abs(grapWidthStart-grapWidthStop);
        imgGrays = new int[grapWidth][grapHeight];
    	initVals = new int[imgGrays.length][imgGrays[0].length];
    	
    	if(grapWidthStop > grapWidthStart){
    		if(grapHeightStop > grapHeightStart){
    			//Bottom-right
    			for(int i=grapWidthStart; i<grapWidthStop; i++){
            		for(int j=grapHeightStart; j<grapHeightStop; j++){
            			pixel = BI.getRGB(i, j);
                    	gray = (pixel & 0xffffff - 0xffff00);
            			imgGrays[i-grapWidthStart][j-grapHeightStart] = gray;
            			initVals[i-grapWidthStart][j-grapHeightStart] = 255;
            		}
            	}
    		}else{
    			//Top-right
    			for(int i=grapWidthStart; i<grapWidthStop; i++){
            		for(int j=grapHeightStart; j>grapHeightStop; j--){
            			pixel = BI.getRGB(i, j);
                    	gray = (pixel & 0xffffff - 0xffff00);
            			imgGrays[i-grapWidthStart][grapHeightStart-j] = gray;
            			initVals[i-grapWidthStart][grapHeightStart-j] = 255;
            		}
            	}
    		}
    	}else{
    		if(grapHeightStop < grapHeightStart){
    			//Top-left
    			for(int i=grapWidthStart; i>grapWidthStop; i--){
            		for(int j=grapHeightStart; j>grapHeightStop; j--){
            			pixel = BI.getRGB(i, j);
                    	gray = (pixel & 0xffffff - 0xffff00);
            			imgGrays[grapWidthStart-i][grapHeightStart-j] = gray;
            			initVals[grapWidthStart-i][grapHeightStart-j] = 255;
            		}
            	}
    		}else{
    			//Bottom-left
    			for(int i=grapWidthStart; i>grapWidthStop; i--){
            		for(int j=grapHeightStart; j<grapHeightStop; j++){
            			pixel = BI.getRGB(i, j);
                    	gray = (pixel & 0xffffff - 0xffff00);
            			imgGrays[grapWidthStart-i][j-grapHeightStart] = gray;
            			initVals[grapWidthStart-i][j-grapHeightStart] = 255;
            		}
            	}
    		}
    	}
    	
        //Start image processing
        boolean imgPassed = false;
        allPossibleLines.clear();
        allPotentialLines.clear();
        imgProcResultLength.clear();
        imgProcResultOK.clear();
        imgProcResultAngle.clear();
        finalData = new int[initVals.length][initVals[0].length];
		for(int i=0; i<initVals.length; i++){
			finalData[i] = initVals[i].clone();
		}
		
        if(null!=imgGrays){
        	if(imgGrays.length-imgProcRegionWBackoff>lineScanRange*3 && imgGrays[0].length-imgProcRegionHBackoff>lineScanRange*3){
	        	peakVals = getPeakVals(imgGrays,initVals);
	            if(null!=peakVals){
	            	setFirstPoleConfig();
	            	ImgExtractLine baseLine = getBaseLine(peakVals,initVals,imgGrays,255);
	            	if(null!=baseLine && baseLine.getLineRSQ()>0){
	            		ArrayList<ImgExtractLine> extractLines = searchLines(peakVals,initVals,imgGrays,255,baseLine);
	            		if(extractLines.size()>0){
	            			imgPassed = checkLines(peakVals,initVals,imgGrays,255,baseLine,extractLines);
	            		}
	            	}else{
	            		System.out.println("getBaseLine failed");
	            	}
	            }
        	}else{
        		System.out.println("getImageProcRegion failed");
        	}
        }
        
        //Save output image
        if(outImg){
        	BI = createBufferedImage(BI);
        	if(null!=finalData && finalData.length>0){
        		int realX = 0, realY = 0;
        		int firstLen = finalData.length, secondLen = finalData[0].length;
        		
        		for(int i=0; i<firstLen; i++){
        			if(grapWidthStart>grapWidthStop){
    					realX = grapWidthStart - i;
    				}else{
    					realX = grapWidthStart + i;
    				}
        			
        			for(int j=0; j<secondLen; j++){
        				if(grapHeightStart>grapHeightStop){
        					realY = grapHeightStart - j;
        				}else{
        					realY = grapHeightStart + j;
        				}
        				
        				if(finalData[i][j]<255){
        					pixel = BI.getRGB(realX, realY);
        					if(4==finalData[i][j]){
        						BI.setRGB(realX, realY, Color.RED.getRGB());
        					}else if(5==finalData[i][j]){
        						BI.setRGB(realX, realY, Color.CYAN.getRGB());
        					}else{
        						if(curLineIsLonger(finalData[i][j],evenPoleIsLonger)){
        							BI.setRGB(realX, realY, Color.ORANGE.getRGB());
        						}else{
        							BI.setRGB(realX, realY, Color.BLUE.getRGB());
        						}
        					}
        				}
        			}
        		}
        	}
        	
	        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("png");//自定义图像格式
	        ImageWriter writer = it.next();
	        ImageOutputStream ios;
			try {
				writeProcResultInfo(imgFileName,BI,polePosition,imgPassed,imgWidth,imgHeight,startProcTime);
				if(imgPassed){
					ios = ImageIO.createImageOutputStream(new File(src+".OK.png"));
				}else{
					ios = ImageIO.createImageOutputStream(new File(src+".NG.png"));
				}
				writer.setOutput(ios);
		        writer.write(BI);
		        BI.flush();
		        ios.flush();
		        ios.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        String[] imgProcRslt = getImgProcRsltData(imgFileName);
        imgProcRslt[0] = imgProcRslt[0]+",imgSavingT(ms)";
        imgProcRslt[1] = imgProcRslt[1]+","+(System.currentTimeMillis()-getImgStartSavingTime());
        setImgProcRsltData(imgFileName,imgProcRslt[0],imgProcRslt[1]);
        
        return imgPassed;
	}
	
	private BufferedImage createBufferedImage(BufferedImage baseBI){
		int width = baseBI.getWidth();
		int height = baseBI.getHeight();
		
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        bi.getGraphics().drawImage(baseBI, 0, 0,width, height, null);
        baseBI = null;
        
        return bi;
	}
	
	private void writeProcResultInfo(String imgFileName, BufferedImage BI, PolePositionItems polePosition, boolean imgPassed, int imgWidth, int imgHeight, long startProcTime){
		int txtX = 0, txtY = 0, txtOffset = 25, txtLine = 0, oriY = 0;
		double minL = 1000.0, maxL = -1000.0, curL = 0.0;
		String lenInfo = "", procRsltTitle = "", procRsltData = "";
		DecimalFormat df = new DecimalFormat("0.000");
		DecimalFormat df1 = new DecimalFormat("0.0");
		Graphics g = BI.getGraphics();
		g.setFont(new Font("Serif",Font.BOLD,54));
		g.setColor(imgPassed?Color.DARK_GRAY:Color.RED);
		if(polePosition==PolePositionItems.TOPLEFT || polePosition==PolePositionItems.BOTTOMLEFT){
			txtX = (int)(imgWidth*0.75);
			txtY = (int)(imgHeight*0.25);
		}else if(polePosition==PolePositionItems.BOTTOMRIGHT || polePosition==PolePositionItems.TOPRIGHT){
			txtX = (int)(imgWidth*0.15);
			txtY = (int)(imgHeight*0.25);
		}
		oriY = txtY;
		
		g.drawString(imgPassed?"OK":"NG", txtX, txtY);
		g.setFont(new Font("Serif",Font.BOLD,18));
		txtLine = 3;
		txtY += txtOffset * txtLine;
		for(String key:imgProcResultLength.keySet()){
			txtY += txtOffset;
			curL = (null!=imgProcResultLength.get(key)?imgProcResultLength.get(key):0.0);
			lenInfo = key+" = "+df.format(curL);
			procRsltTitle += ","+key;
			procRsltData += ","+df.format(curL);
			
			if(null!=imgProcResultAngle.get(key)){
				lenInfo += " ["+df1.format(imgProcResultAngle.get(key))+"deg]";
			}
			
			if(imgProcResultOK.get(key)){
				g.setColor(Color.DARK_GRAY);
			}else{
				g.setColor(Color.RED);
			}
			g.drawString(lenInfo, txtX, txtY);
			if(minL>curL) minL = curL;
			if(maxL<curL) maxL = curL;
		}
		imgStartSavingT = System.currentTimeMillis();
		imgProcTime = imgStartSavingT-startProcTime;
		g.setColor(Color.DARK_GRAY);
		g.drawString("PRO_T = "+imgProcTime+" ms", txtX, oriY+txtOffset);
		g.drawString("MIN_L = "+df.format(minL)+" mm", txtX, oriY+txtOffset*2);
		g.drawString("MAX_L = "+df.format(maxL)+" mm", txtX, oriY+txtOffset*3);
		
		procRsltTitle = "procT(ms),MIN_L,MAX_L"+procRsltTitle;
		procRsltData = imgProcTime+","+df.format(minL)+","+df.format(maxL)+procRsltData;
		setImgProcRsltData(imgFileName,procRsltTitle,procRsltData);
	}
	
	public void clearImgProcRsltData(String imgFileName){
		imgProcResultStr.remove(imgFileName);
	}
	
	private void setImgProcRsltData(String imgFileName, String rsltTitle, String rsltData){
		imgProcResultStr.put(imgFileName, rsltTitle+"\r\n"+rsltData);
	}
	
	public String[] getImgProcRsltData(String imgFileName){
		String[] rsltStr = new String[]{"",""};
		String rslt = imgProcResultStr.get(imgFileName);
		if(null!=rslt) rsltStr = rslt.split("\r\n");
		return rsltStr;
	}
	
	private double getLineAvgOffset(ImgExtractLine exLine, int imgWidth){
		double avgOffset = 0.0, lineOffset = 0.0, lineSlope = 0.0;
		if(null!=exLine){
			lineSlope = exLine.getLineSlope();
			lineOffset = exLine.getLineIntercept() + exLine.getLineInterceptDelta();
			avgOffset = (lineOffset + imgWidth/2*lineSlope+lineOffset + imgWidth*lineSlope+lineOffset)/3;
		}
		return avgOffset;
	}
	
	private ArrayList<Object> getLinesTendencyParas(ArrayList<ImgExtractLine> extractLines, LinkedHashMap<Integer,ImgExtractLine> linesMap, int targetLinesQty, int imgWidth){
		ArrayList<Object> tdcParas = new ArrayList<Object>();
		double[] coef = null, slope = null, offset = null;
		int reliableMaxIdx = 0, evenPoleCnt = 0, oddPoleCnt = 0;
		double evenPoleAvgOffset = 0.0, oddPoleAvgOffset = 0.0, finalRsq = 0.0, avgOffset = 0.0, lineSlope = 0.0, lineOffset = 0.0;
		double evenMinOffset = 0.0, evenMaxOffset = 0.0, oddMinOffset = 0.0, oddMaxOffset = 0.0, curOffset = 0.0;
		
		LinkedHashMap<Double,Double> tendencyDt = new LinkedHashMap<Double,Double>();
		ArrayList<Double> tdcSlope = new ArrayList<Double>();
		ArrayList<Double> tdcOffset = new ArrayList<Double>();
		ArrayList<Double> tdcRsq = new ArrayList<Double>();
		
		if(null!=extractLines && extractLines.size()>0){
			if(null==linesMap){
				linesMap = new LinkedHashMap<Integer,ImgExtractLine>();
			}else if(!linesMap.isEmpty()){
				linesMap.clear();
			}
			
			for(int i=0; i<extractLines.size(); i++){
				if(i<targetLinesQty){
					linesMap.put(i, extractLines.get(i));
					lineSlope = extractLines.get(i).getLineSlope();
					lineOffset = extractLines.get(i).getLineIntercept();
					avgOffset = (lineOffset + imgWidth/2*lineSlope+lineOffset + imgWidth*lineSlope+lineOffset)/3;
					tendencyDt.put((double)i, avgOffset);
					if(i<2){
						tdcSlope.add(i,0.0);
						tdcOffset.add(i,0.0);
						tdcRsq.add(i,1.0);
						reliableMaxIdx = i;
					}else{
						coef = MathUtils.lineFitting(tendencyDt);
						tdcSlope.add(i,coef[0]);
						tdcOffset.add(i,coef[1]);
						tdcRsq.add(i,coef[2]);
						if(coef[2]>minBestRsq){
							reliableMaxIdx = i;
							finalRsq = coef[2];
						}
					}
				}else{
					break;
				}
			}
			
			//Recalculate the tendency
			if(reliableMaxIdx>5 && tdcRsq.get(reliableMaxIdx) < minBestRsq){
				LinkedHashMap<Double,Double> fittingDt = new LinkedHashMap<Double,Double>();
				for(int i=1; i<=reliableMaxIdx; i++){
					fittingDt.put((double)i, extractLines.get(i).getLineIntercept());
				}
				coef = MathUtils.lineFitting(fittingDt);
				finalRsq = coef[2];
			}
			
			//Calculate average offset of even & odd pole to its previous most-closed pole
			evenPoleAvgOffset=0.0; oddPoleAvgOffset=0.0;
			for(int i=1; i<=reliableMaxIdx; i++){
				curOffset = linesMap.get(i).getLineIntercept() - linesMap.get(i-1).getLineIntercept();
				if(0==i%2){
					evenPoleCnt++;
					evenPoleAvgOffset += curOffset;
					if(1==evenPoleCnt){
						evenMinOffset = curOffset; evenMaxOffset = curOffset;
					}else{
						if(evenMinOffset>curOffset) evenMinOffset = curOffset;
						if(evenMaxOffset<curOffset) evenMaxOffset = curOffset;
					}
				}else{
					oddPoleCnt++;
					oddPoleAvgOffset += curOffset;
					if(1==oddPoleCnt){
						oddMinOffset = curOffset; oddMaxOffset = curOffset;
					}else{
						if(oddMinOffset>curOffset) oddMinOffset = curOffset;
						if(oddMaxOffset<curOffset) oddMaxOffset = curOffset;
					}
				}
			}
			
			if(evenPoleCnt>0){
				evenPoleAvgOffset = (evenPoleCnt>2)?(evenPoleAvgOffset-evenMinOffset-evenMaxOffset) / (evenPoleCnt-2):evenPoleAvgOffset / evenPoleCnt;
			}else if(null!=linesMap && linesMap.size()>2){
				evenPoleAvgOffset = linesMap.get(2).getLineIntercept() - linesMap.get(1).getLineIntercept();
			}
			
			if(oddPoleCnt>0){
				oddPoleAvgOffset = (oddPoleCnt>2)?(oddPoleAvgOffset-oddMinOffset-oddMaxOffset) / (oddPoleCnt-2):oddPoleAvgOffset / oddPoleCnt;
			}else if(null!=linesMap && linesMap.size()>1){
				oddPoleAvgOffset = linesMap.get(1).getLineIntercept() - linesMap.get(0).getLineIntercept();
			}
		}
		
		tdcParas.add(0,finalRsq);
		tdcParas.add(1,evenPoleAvgOffset);
		tdcParas.add(2,oddPoleAvgOffset);
		tdcParas.add(3,reliableMaxIdx);
		if(tdcSlope.size()>0){
			slope = new double[tdcSlope.size()];
			offset = new double[tdcOffset.size()];
			for(int i=0; i<tdcSlope.size(); i++){
				slope[i] = tdcSlope.get(i);
				offset[i] = tdcOffset.get(i);
			}
		}
		tdcParas.add(4,slope);
		tdcParas.add(5,offset);
		
		return tdcParas;
	}
	
	private boolean checkLines(int[][] peakVals, int[][] initVals, int[][] imgGrays, int defaultVal, ImgExtractLine myBaseline, ArrayList<ImgExtractLine> extractLines){
		boolean bPassed = false;
		int extLinesQty = 0, bestRsqIdx = 0;
		double oddPoleAvgOffset = 0.0, evenPoleAvgOffset = 0.0;
		double[] slope = null, offset = null;
		
		LinkedHashMap<Integer,ImgExtractLine> linesMap = new LinkedHashMap<Integer,ImgExtractLine>();
		ArrayList<Object> tdcParas = null;
		
		extLinesQty = extractLines.size();
		if(extLinesQty>3){
			tdcParas = getLinesTendencyParas(extractLines, linesMap, targetLinesQty, imgGrays.length);
			if((double)tdcParas.get(0)<minBestRsq){
				System.out.println("RSQ NG:"+(double)tdcParas.get(0)+"(<"+minBestRsq+")");
				return bPassed;
			}
			evenPoleAvgOffset = (double)tdcParas.get(1);
			oddPoleAvgOffset = (double)tdcParas.get(2);
			bestRsqIdx = (int)tdcParas.get(3);
			slope = (double[]) tdcParas.get(4);
			offset = (double[]) tdcParas.get(5);
			
			//Recalculate the missing lines
			if(thereIsMissingLines(linesMap,targetLinesQty)){
				linesMap = searchMissingLines(linesMap,myBaseline,peakVals,initVals,imgGrays,defaultVal,slope,offset,bestRsqIdx,evenPoleAvgOffset,oddPoleAvgOffset,targetLinesQty,false);
			}
			
			//Double check all lines
			evenPoleIsLonger = evenLineIsLonger(linesMap,imgGrays);
			System.out.println("bestIdx/evenOffset/oddOffset:"+bestRsqIdx
					+"/"+evenPoleAvgOffset+"/"+oddPoleAvgOffset
					+"/"+((currToLastOffsetDeltaIsBigger(2,evenPoleIsLonger)?
							((evenPoleAvgOffset>oddPoleAvgOffset)?"Correct":"Wrong"):
							((evenPoleAvgOffset<oddPoleAvgOffset)?"Correct":"Wrong"))));
			
			//Check one more layer inside if the first layer is longer
			if(evenPoleIsLonger && !firstPoleIsLonger){
				targetLinesQty = targetLinesQty + 1;
				linesMap = searchMissingLines(linesMap,myBaseline,peakVals,initVals,imgGrays,defaultVal,slope,offset,bestRsqIdx,evenPoleAvgOffset,oddPoleAvgOffset,targetLinesQty,true);
			}
			
			//Compensate the longer layers
			linesMap = adjustLongerLines(linesMap,imgGrays,peakVals,evenPoleIsLonger,targetLinesQty,true);
			
			//Adjust all layers' index
			linesMap = doubleCheckAllLinesNew(linesMap, peakVals,initVals,imgGrays, evenPoleIsLonger, targetLinesQty,evenPoleAvgOffset,oddPoleAvgOffset,slope[bestRsqIdx],offset[bestRsqIdx]);
			if(thereIsMissingLines(linesMap,targetLinesQty)){
				linesMap = searchMissingLines(linesMap,myBaseline,peakVals,initVals,imgGrays,defaultVal,slope,offset,bestRsqIdx,evenPoleAvgOffset,oddPoleAvgOffset,targetLinesQty,true);
				linesMap = doubleCheckAllLinesNew(linesMap, peakVals,initVals,imgGrays, evenPoleIsLonger, targetLinesQty,evenPoleAvgOffset,oddPoleAvgOffset,slope[bestRsqIdx],offset[bestRsqIdx]);
			}
			if(thereIsMissingLines(linesMap,targetLinesQty)){
				linesMap = addMissingLines(linesMap,myBaseline,peakVals,initVals,imgGrays,defaultVal,slope,offset,bestRsqIdx,evenPoleAvgOffset,oddPoleAvgOffset,targetLinesQty);
				linesMap = doubleCheckAllLinesNew(linesMap, peakVals,initVals,imgGrays, evenPoleIsLonger, targetLinesQty,evenPoleAvgOffset,oddPoleAvgOffset,slope[bestRsqIdx],offset[bestRsqIdx]);
			}
			if(thereIsMissingLines(linesMap,targetLinesQty)){
				linesMap = addMissingLines(linesMap,myBaseline,peakVals,initVals,imgGrays,defaultVal,slope,offset,bestRsqIdx,evenPoleAvgOffset,oddPoleAvgOffset,targetLinesQty);
			}
			
			//Set all lines sharp change position
			linesMap = adjustLongerLines(linesMap,imgGrays,peakVals,evenPoleIsLonger,targetLinesQty,true);
			setLinesSharpChgPosition(linesMap,imgGrays,peakVals,evenPoleIsLonger,targetLinesQty);
			
			//Calculate the final result
			twoNearLayersOffsetCorrect(linesMap,evenPoleIsLonger);
			linesMap = setPolePosition(linesMap,evenPoleIsLonger,imgGrays,peakVals,targetLinesQty);
			searchWrinkleLines(linesMap,imgGrays,peakVals,evenPoleAvgOffset,oddPoleAvgOffset,evenPoleIsLonger);
			bPassed = calculateFinalResult(linesMap,evenPoleIsLonger,imgGrays,defaultVal,targetLinesQty,peakVals,evenPoleAvgOffset,oddPoleAvgOffset);
			calcFinalData(linesMap,initVals,targetLinesQty);
		}else{
			System.out.println("Extract Lines Qty:"+extractLines.size()+" is NG");
		}
		
		System.out.println("Proc Result:"+(bPassed?"OK":"NG"));
		return bPassed;
	}
	
	private LinkedHashMap<Integer, ImgExtractLine> adjustLongerLines(LinkedHashMap<Integer, ImgExtractLine> oriLinesMap, int[][] imgGrays, int[][] peakVals, boolean evenLineLonger, int targetLinesQty, boolean bNotSetInterceptDelta){
		double[] weightOfLine = null;
		for(int lineIdx=0; lineIdx<targetLinesQty; lineIdx++){
			ImgExtractLine exLine = oriLinesMap.get(lineIdx);
			if(null!=exLine && 0==exLine.getXAxisStart()){
				exLine = setLineBoundary(exLine, imgGrays, 255, 0.3);
				oriLinesMap.put(lineIdx, exLine);
			}
			
			if(curLineIsLonger(lineIdx,evenLineLonger)){
				if(null!=exLine){
					weightOfLine = exLine.weightOfBeingLine(oriLinesMap, exLine.getXAxisStop(), exLine.getXAxisStart(), 0, imgGrays, peakVals, 5, lineIdx, targetLinesQty, bNotSetInterceptDelta, curLineIsLonger(lineIdx,evenLineLonger), false);
					oriLinesMap.put(lineIdx, exLine);
					if(weightOfLine[0]<0.5) System.out.println("Weight of line("+lineIdx+"):"+weightOfLine[0]);
				}
			}
		}
		
		return oriLinesMap;
	}
	
	private int getLineResultFlag(LinkedHashMap<Integer, ImgExtractLine> linesMap, int lineIdx, boolean evenLineLonger){
		int rsltFlag = -1;
		double minDistance = 10;//pixels
		double maxDistance = 116;//pixels
		double onePixelLen = 0.012;
		double overhang = 0.0;
		boolean curLineLonger = curLineIsLonger(lineIdx,evenLineLonger);
		int lastPolePos = 0, nextPolePos = 0;
		onePixelLen = Double.parseDouble(""+criteria.get("onePixel"));
		minDistance = Double.parseDouble(""+criteria.get("minPoleDistance"))/onePixelLen;
		maxDistance = Double.parseDouble(""+criteria.get("maxPoleDistance"))/onePixelLen;
		if(lineIdx<0 || lineIdx>=linesMap.size()) return rsltFlag;
		
		ImgExtractLine currLine = linesMap.get(lineIdx);
		if(null==currLine) return rsltFlag;
		
		if(lineIdx>0 && null!=linesMap.get(lineIdx-1)) lastPolePos = linesMap.get(lineIdx-1).getPolePosition();
		if(lineIdx+1<linesMap.size() && null!=linesMap.get(lineIdx+1)) nextPolePos = linesMap.get(lineIdx+1).getPolePosition();
		if(0==lastPolePos && 0==nextPolePos) return rsltFlag;
		
		if(lastPolePos>0){
			overhang = calculateOverhang(currLine,linesMap.get(lineIdx-1),curLineLonger);
			if(overhang<minDistance || overhang>maxDistance){
				rsltFlag = 0;
			}
		}
		
		if(nextPolePos>0){
			overhang = calculateOverhang(linesMap.get(lineIdx+1),currLine,!curLineLonger);
			if(overhang<minDistance || overhang>maxDistance){
				rsltFlag = 1;
			}
		}
		
		return rsltFlag;
	}
	
	private void searchWrinkleLines(LinkedHashMap<Integer, ImgExtractLine> linesMap, int[][] imgGrays, int[][] peakVals, double evenPoleAvgOffset, double oddPoleAvgOffset, boolean evenLineLonger){
		if(null==linesMap || null!=linesMap && linesMap.isEmpty()) return;
		int[] polePosX = getPolesAvgPosX(linesMap,evenLineLonger,targetLinesQty);
		if(polePosX[0]<0 || polePosX[1]<0) return;
		
		ImgExtractLine currLine = null;
		int xDiff1 = 0, xDiff2 = 0, rsltFlag = -1, poleLen = 0, xStart = 0;
		double ratio1 = 0.0, ratio2 = 0.0;
		for(int i=0; i<linesMap.size(); i++){
			currLine = linesMap.get(i);
			if(null==currLine) continue;
			if(curLineIsLonger(i,evenLineLonger)){
				rsltFlag = getLineResultFlag(linesMap,i,evenLineLonger);
				if(i>0 && null!=linesMap.get(i-1)){
					xStart = linesMap.get(i-1).getXAxisStart();
				}else if(i+1<linesMap.size() && null!=linesMap.get(i+1)){
					xStart = linesMap.get(i+1).getXAxisStart();
				}else{
					xStart = currLine.getXAxisStart();
				}
				poleLen = xStart - currLine.getXAxisStop();
				
				xDiff1 = currLine.getPolePosition() - currLine.getXAxisStop();//>0 means actual pole length is shorter than expected
				xDiff2 = currLine.getXAxisStop()-polePosX[0];//>0 means stop position is more inner
				ratio1 = (double)xDiff1/poleLen;
				ratio2 = (double)xDiff2/poleLen;
				
//				if(rsltFlag>=0 || ratio1>0.25 || ratio2>0.25) searchWrinkleLine(linesMap,i,imgGrays,peakVals,evenPoleAvgOffset,oddPoleAvgOffset,evenLineLonger);
				searchWrinkleLine(linesMap,i,imgGrays,peakVals,evenPoleAvgOffset,oddPoleAvgOffset,evenLineLonger);
			}
		}
	}
	
	/**
	 * 
	 * @param currLineSlope
	 * @param crossLineRawDt
	 * @param chkDownSide
	 * @return double[] 0-Slope | 1-Offset | 2-RSQ | 3-CrossAg
	 */
	private double[] calcCrossAngle(double currLineSlope, LinkedHashMap<Double, Double> crossLineRawDt, boolean chkDownSide){
		double crossAg = 90.0, minAgForMaxSlope = 5.0;
		double[] coef = null, coef2 = null, rtnParas = new double[]{crossAg,0,0,0};
		
		int fittingPts = (int)(crossLineRawDt.size()*0.5);
		if(fittingPts<7) fittingPts = 7;
		
		coef = getLineMaxSlopeParas(crossLineRawDt,fittingPts,chkDownSide);
		coef2 = MathUtils.lineFitting(crossLineRawDt);
		if(-1!=currLineSlope*coef[0]){
			crossAg = Math.abs((currLineSlope-coef[0])/(1+currLineSlope*coef[0]));
			crossAg = Math.toDegrees(Math.atan(crossAg));
			if(crossAg<minAgForMaxSlope){
				if(-1!=currLineSlope*coef2[0]){
					crossAg = Math.abs((currLineSlope-coef2[0])/(1+currLineSlope*coef2[0]));
					crossAg = Math.toDegrees(Math.atan(crossAg));
				}
			}
		}
		
		for(int i=0; i<coef.length; i++){
			rtnParas[i] = coef[i];
		}
		rtnParas[2] = coef2[2];
		rtnParas[3] = crossAg;
		
		return rtnParas;
	}
	
	private double[] getLineMaxSlopeParas(LinkedHashMap<Double, Double> wrinkleLineRawDt, int fitPts, boolean chkDownSide){
		double[] coef = null, tmpCoef = null;
		double maxSlope = 0.0, minSlopeThr = 0.25;
		int continueCnt = 0, skipMaxSlopeThr = 10;
		ArrayList<Double> xVals = new ArrayList<Double>();
		ArrayList<Double> yVals = new ArrayList<Double>();
		
		if(wrinkleLineRawDt.size()>fitPts){
			for(double key:wrinkleLineRawDt.keySet()){
				xVals.add(key);
				yVals.add(wrinkleLineRawDt.get(key));
			}
			tmpCoef = MathUtils.lineFitting(wrinkleLineRawDt);
			maxSlope = tmpCoef[0];
			coef = tmpCoef.clone();
			
			for(int i=0; i<=(xVals.size()-fitPts); i++){
				tmpCoef = MathUtils.lineFitting(xVals, yVals, i, i+fitPts-1);
				if(Math.abs(tmpCoef[0])<minSlopeThr && i==continueCnt){
					continueCnt++;
				}
				
				if(continueCnt>=skipMaxSlopeThr){
					coef = MathUtils.lineFitting(wrinkleLineRawDt);
					break;
				}
				if(chkDownSide){
					if(maxSlope>tmpCoef[0]){//Get minimum slope
						maxSlope = tmpCoef[0];
						coef = tmpCoef.clone();
					}
				}else{
					if(maxSlope<tmpCoef[0]){//Get maximum slope
						maxSlope = tmpCoef[0];
						coef = tmpCoef.clone();
					}
				}
			}
		}else{
			coef = MathUtils.lineFitting(wrinkleLineRawDt);
		}
		return coef;
	}
	
	private void searchWrinkleLine(LinkedHashMap<Integer, ImgExtractLine> linesMap, int lineIdx, int[][] imgGrays, int[][] peakVals, double evenPoleAvgOffset, double oddPoleAvgOffset, boolean evenLineLonger){
		ImgExtractLine currLine = null, tempLine = null, lastLine = null, nextLine = null;
		if(null==linesMap || null!=linesMap && linesMap.isEmpty()) return;
		currLine = (lineIdx>=0 && lineIdx<linesMap.size())?linesMap.get(lineIdx):null;
		if(null==currLine) return;
		
		boolean wrinkleUpward = false, curLineLonger = curLineIsLonger(lineIdx,evenLineLonger);
		int minStopX = 0, startX = currLine.getPolePosition()-5;
		minStopX = linesMap.get(0).getXAxisStop();
		for(int i=0; i<linesMap.size(); i++){
			tempLine = linesMap.get(i);
			if(null!=tempLine && minStopX>tempLine.getXAxisStop()) minStopX=tempLine.getXAxisStop();
		}
		
		double lenWeight = 0.0;
		if(curLineLonger){
			lenWeight = (double)(currLine.getXAxisStart() - currLine.getPolePosition()) / (currLine.getXAxisStart() - currLine.getXAxisStop());
			if(lenWeight<0.25) startX = currLine.getXAxisStart()-5;
		}
		
		int imgWidth = imgGrays.length;
		double tmpSlope = 0.0, tmpOffset = 0.0, maxDnChkOffset = 0.0, maxUpChkOffset = 0.0;
		int newXAxisStop = 0, upStartChkPos = 0, dnStartChkPos = 0, dnCrossX = 0, upCrossX = 0;
		double maxPoleAngle = Double.parseDouble(""+criteria.get("maxPoleAngle"));
		double onePixelLen = Double.parseDouble(""+criteria.get("onePixel"));
		double minDistance = Double.parseDouble(""+criteria.get("minPoleDistance"))/onePixelLen;
		double[] dnLineParas = null, upLineParas = null;
		double dnSideAngle = 0.0, upSideAngle = 0.0, dnSideScore = 0.0, upSideScore = 0.0;
		int[] poles = null;
		
		tmpSlope = currLine.getLineSlope(); tmpOffset = currLine.getLineIntercept();
		maxDnChkOffset = 0.0; maxUpChkOffset = 0.0;
		newXAxisStop = -1;
		upStartChkPos = startX; dnStartChkPos = startX; dnCrossX = -1; upCrossX = -1;
		
		maxDnChkOffset = (evenPoleAvgOffset+oddPoleAvgOffset)*0.75;
		maxUpChkOffset = maxDnChkOffset;
		
		ArrayList<Object> lastWrinkleParas = new ArrayList<Object>();
		if(lineIdx>0) lastLine = linesMap.get(lineIdx-1);
		if(lineIdx>0 && null!=lastLine){
			tempLine = linesMap.get(lineIdx-1);
			maxUpChkOffset = getLineAvgOffset(currLine,imgWidth)-getLineAvgOffset(tempLine,imgWidth);
			maxUpChkOffset = maxUpChkOffset*1.5;
			if(maxUpChkOffset<0) maxUpChkOffset = lineScanRange/2;
			upStartChkPos = (tempLine.getXAxisStart()-5)<startX?startX:(tempLine.getXAxisStart()-5);
			lastWrinkleParas = tempLine.getWrinkleParas();
			if((double)lastWrinkleParas.get(3)> 0 && !(boolean)lastWrinkleParas.get(5) && maxUpChkOffset>5) maxUpChkOffset = 5;
		}
		
		if(lineIdx+1<linesMap.size()) nextLine = linesMap.get(lineIdx+1);
		if(lineIdx+1<linesMap.size() && null!=nextLine){
			maxDnChkOffset = getLineAvgOffset(linesMap.get(lineIdx+1),imgWidth) - getLineAvgOffset(currLine,imgWidth);
			maxDnChkOffset = maxDnChkOffset*1.5;
			if(maxDnChkOffset<0) maxDnChkOffset = lineScanRange/2;
			dnStartChkPos = (linesMap.get(lineIdx+1).getXAxisStart()-5)<startX?startX:(linesMap.get(lineIdx+1).getXAxisStart()-5);
		}
		
		if(9==lineIdx){
			System.out.print("");
		}
		
		//Check up side
		LinkedHashMap<Double,Double> upSideWrinkleLine = new LinkedHashMap<Double,Double>();
		upSideWrinkleLine = getWrinklePtsEx(currLine,lastLine,imgGrays,peakVals,upStartChkPos,minStopX,(int)maxUpChkOffset,false);
		//Check down side
		LinkedHashMap<Double,Double> dnSideWrinkleLine = new LinkedHashMap<Double,Double>();
		dnSideWrinkleLine = getWrinklePtsEx(currLine,nextLine,imgGrays,peakVals,dnStartChkPos,minStopX,(int)maxDnChkOffset,true);
		
		dnLineParas = null; upLineParas = null;
		dnSideAngle = 0.0; upSideAngle = 0.0;
		if(dnSideWrinkleLine.size()>=5){
			dnLineParas = calcCrossAngle(tmpSlope,dnSideWrinkleLine,true);
			dnSideAngle = dnLineParas[3];
			if(dnSideAngle<90){
				dnCrossX = (int)((tmpOffset-dnLineParas[1])/(dnLineParas[0]-tmpSlope));
				dnSideScore = currLine.getWrinkleScore(false);
			}else{
				dnCrossX = currLine.getXAxisStart();
			}
		}
		if(upSideWrinkleLine.size()>=5){
			upLineParas = calcCrossAngle(tmpSlope,upSideWrinkleLine,false);
			upSideAngle = upLineParas[3];
			if(upSideAngle<90){
				upCrossX = (int)((tmpOffset-upLineParas[1])/(upLineParas[0]-tmpSlope));
				upSideScore = currLine.getWrinkleScore(true);
			}else{
				upCrossX = currLine.getXAxisStart();
			}
		}
		
		int upStopX = -1, dnStopX = -1;
		if(dnSideAngle>0 || upSideAngle>0){
			if(dnSideAngle>0){
				dnStopX = imgWidth;
				for(double k:dnSideWrinkleLine.keySet()){
					if(k<dnStopX) dnStopX=(int)k;
				}
			}
			
			if(upSideAngle>0){
				upStopX = imgWidth;
				for(double k:upSideWrinkleLine.keySet()){
					if(k<upStopX) upStopX=(int)k;
				}
			}
			
			if(dnSideAngle>maxPoleAngle || upSideAngle>maxPoleAngle){//Angle is out of control spec
				if(dnSideAngle>maxPoleAngle && upSideAngle>maxPoleAngle){
					if(dnSideAngle>maxPoleAngle){//DN side first
						newXAxisStop = dnCrossX;
					}else{
						newXAxisStop = upCrossX;
						wrinkleUpward = true;
					}
				}else if(dnSideAngle>maxPoleAngle){
					if(currLine.getWrinkleLinesQty(false)>=currLine.getWrinkleLinesQty(true)){//If bending lines of downside are more than upside
						newXAxisStop = dnCrossX;
					}else{
						newXAxisStop = upStopX;
						wrinkleUpward = true;
					}
				}else{
					if(currLine.getWrinkleLinesQty(false)<currLine.getWrinkleLinesQty(true)){
						newXAxisStop = upCrossX;
						wrinkleUpward = true;
					}else{
						newXAxisStop = dnStopX;
					}
				}
			}else{//Angle is in control spec
				if(dnSideAngle>0 && upSideAngle>0){
					if(dnSideAngle<2 && upSideAngle<2 && dnStopX>upStopX){
						newXAxisStop = upStopX;
						wrinkleUpward = true;
					}else if(dnSideAngle>upSideAngle || currLine.getWrinkleScore(false)>currLine.getWrinkleScore(true)){
						newXAxisStop = dnStopX;
					}else{
						newXAxisStop = upStopX;
						wrinkleUpward = true;
					}
				}else if(dnSideAngle>0){
					newXAxisStop = dnStopX;
				}else{
					newXAxisStop = upStopX;
					wrinkleUpward = true;
				}
			}
			
			if(dnSideAngle>0 && upSideAngle>0 && dnSideAngle<maxPoleAngle && upSideAngle<maxPoleAngle && dnSideScore<upSideScore){
				dnCrossX = (dnCrossX>currLine.getXAxisStart()?currLine.getXAxisStart():dnCrossX);
				poles = getLineStopPos(currLine, dnLineParas[0], dnLineParas[1], imgGrays, dnStopX, dnCrossX, 2);
				dnStopX = poles[1];
				if((currLine.getXAxisStart()-dnStopX<minDistance)){
					newXAxisStop = upStopX;
					wrinkleUpward = true;
				}
			}
		}
		
		if(newXAxisStop>=0){
			if(wrinkleUpward){
				upCrossX = (upCrossX>currLine.getXAxisStart()?currLine.getXAxisStart():upCrossX);
				poles = getLineStopPos(currLine, upLineParas[0], upLineParas[1], imgGrays, newXAxisStop, upCrossX, 2);
				newXAxisStop = poles[1];
				if(curLineLonger){
					if(upSideAngle>maxPoleAngle){
						currLine.setPolePosition(upCrossX);
					}else{
						currLine.setPolePosition(newXAxisStop);
					}
				}
				
				if(0!=upSideAngle) storeLayerAngle(evenLineLonger, lineIdx, 0.0, upSideAngle);
				currLine.setWrinkleParas(upLineParas[0], upLineParas[1], upSideAngle, upCrossX, minStopX, wrinkleUpward);
				System.out.println("Line_"+lineIdx+(curLineLonger?"L":"S")+"_WParas(Up):"+upSideAngle+"/"+upCrossX+"("+poles[0]+")"+"/"+poles[1]+"("+(curLineLonger?currLine.getPolePosition():currLine.getXAxisStop())+")");
			}else{
				dnCrossX = (dnCrossX>currLine.getXAxisStart()?currLine.getXAxisStart():dnCrossX);
				poles = getLineStopPos(currLine, dnLineParas[0], dnLineParas[1], imgGrays, newXAxisStop, dnCrossX, 2);
				newXAxisStop = poles[1];
				if(curLineLonger){
					if(dnSideAngle>maxPoleAngle){
						currLine.setPolePosition(dnCrossX);
					}else{
						currLine.setPolePosition(newXAxisStop);
					}
				}
				
				if(0!=dnSideAngle) storeLayerAngle(evenLineLonger, lineIdx, dnSideAngle, 0.0);
				currLine.setWrinkleParas(dnLineParas[0], dnLineParas[1], dnSideAngle, dnCrossX, minStopX, wrinkleUpward);
				System.out.println("Line_"+lineIdx+(curLineLonger?"L":"S")+"_WParas(Dn):"+dnSideAngle+"/"+dnCrossX+"("+poles[0]+")"+"/"+poles[1]+"("+(curLineLonger?currLine.getPolePosition():currLine.getXAxisStop())+")");
			}
			
			if(wrinkleUpward && upSideAngle>0 || !wrinkleUpward && dnSideAngle>0){
				currLine.setXAxisStop(newXAxisStop);
				if(currLine.getXAxisStart()<newXAxisStop) currLine.setXAxisStart(newXAxisStop);
			}
		}
	}
	
	/**
	 * 
	 * @param currLine
	 * @param wrinkleLinePts
	 * @return ArrayList<Double> 0-crossAngle,1-crossX,2-crossXDelta,3-crossYDelta,4-maxCrossXDelta,5-maxCrossYDelta,6-crossOK,7-RSQ,8-Slope,9-Offset
	 */
	private ArrayList<Double> calcCrossLineParas(ImgExtractLine currLine, LinkedHashMap<Double,Double> wrinkleLinePts, double maxX, double minX, boolean chkDownSide, double maxYDelta){
		ArrayList<Double> rtnParas = new ArrayList<Double>();
		double[] fittingParas = null;
		double tmpSlope = currLine.getLineSlope(), tmpOffset = currLine.getLineIntercept() + currLine.getLineInterceptDelta();
		double crossAngle = -1.0, crossX = 0.0, maxCrossXDelta = 0.0, crossXDelta = 0.0, crossYDelta = 0.0, crossOK = 0.0;
		double xStart = 0.0, xCheckPos = 0.0, angleDirVal = 0.0;
		boolean angleDirOK = false;
		
		fittingParas = calcCrossAngle(tmpSlope,wrinkleLinePts,chkDownSide);
		crossAngle = fittingParas[3];
		if(crossAngle<90){
			if(maxYDelta<=0) maxYDelta = lineScanRange * 1.5;
			xStart = currLine.getXAxisStart();
			crossX = (tmpOffset-fittingParas[1])/(fittingParas[0]-tmpSlope);
			angleDirVal = fittingParas[1] - tmpOffset;
			if(chkDownSide && angleDirVal>0) angleDirOK = true;
			if(!chkDownSide && angleDirVal<0) angleDirOK = true;
			
			maxCrossXDelta = maxYDelta / Math.tan(Math.toRadians(crossAngle));
			crossXDelta = crossX - xStart;
			xCheckPos = (crossX>xStart?xStart:crossX);
			
			if(chkDownSide){
				crossYDelta = xCheckPos*fittingParas[0]+fittingParas[1] - (xCheckPos*tmpSlope+tmpOffset);
			}else{
				crossYDelta = xCheckPos*tmpSlope+tmpOffset - (xCheckPos*fittingParas[0]+fittingParas[1]);
			}
			
			if(crossYDelta <= maxYDelta 
			  && crossXDelta <= maxCrossXDelta
			  && angleDirOK){
				crossOK = 1.0;
			}
		}
		
		rtnParas.add(0, crossAngle);
		rtnParas.add(1, crossX);
		rtnParas.add(2, crossXDelta);
		rtnParas.add(3, crossYDelta);
		rtnParas.add(4, maxCrossXDelta);
		rtnParas.add(5, maxYDelta);
		rtnParas.add(6, crossOK);
		rtnParas.add(7, fittingParas[2]);
		rtnParas.add(8, fittingParas[0]);
		rtnParas.add(9, fittingParas[1]);
		
		return rtnParas;
	}
	
	/**
	 * 
	 * @param linesMap
	 * @param evenLineLonger
	 * @param targetLinesQty
	 * @return int[0] - Longer pole's average position; int[1] - Shorter pole's average position
	 */
	private int[] getPolesAvgPosX(LinkedHashMap<Integer, ImgExtractLine> linesMap, boolean evenLineLonger, int targetLinesQty){
		double xPositionSum1 = 0.0, xPositionSum2 = 0.0;
		int counter1 = 0, counter2 = 0;
		int[] polePosX = new int[]{-1,-1};
		ImgExtractLine exLine = null;
		for(int lineIdx=0; lineIdx<targetLinesQty; lineIdx++){
			exLine = linesMap.get(lineIdx);
			if(null==exLine) continue;
			if(curLineIsLonger(lineIdx,evenLineLonger)){
				xPositionSum1 += exLine.getXAxisStop();
				counter1++;
			}else{
				xPositionSum2 += exLine.getXAxisStart();
				counter2++;
			}
		}
		if(counter1>0) polePosX[0] = (int)(xPositionSum1/counter1);
		if(counter2>0) polePosX[1] = (int)(xPositionSum2/counter2);
		return polePosX;
	}
	
	private boolean twoNearLayersOffsetCorrect(LinkedHashMap<Integer, ImgExtractLine> linesMap, boolean evenLineLonger){
		boolean correct = true;
		ImgExtractLine currLine = null, lastLine = null;
		double evenPoleOffsetSum = 0.0, oddPoleOffsetSum = 0.0, offsetDelta = 0.0;
		double evenPoleAvgOffset = 0.0, oddPoleAvgOffset = 0.0;
		int evenPoleCnt = 0, oddPoleCnt = 0;
		
		for(Integer idx:linesMap.keySet()){
			currLine = linesMap.get(idx);
			lastLine = linesMap.get(idx-1);
			
			if(null!=currLine && null!=lastLine){
				offsetDelta = currLine.getLineIntercept()+currLine.getLineInterceptDelta();
				offsetDelta = offsetDelta-lastLine.getLineIntercept()-lastLine.getLineInterceptDelta();
				if(0==idx%2){
					evenPoleOffsetSum += offsetDelta;
					evenPoleCnt++;
				}else{
					oddPoleOffsetSum += offsetDelta;
					oddPoleCnt++;
				}
			}
		}
		if(evenPoleCnt>0) evenPoleAvgOffset=evenPoleOffsetSum/evenPoleCnt;
		if(oddPoleCnt>0) oddPoleAvgOffset=oddPoleOffsetSum/oddPoleCnt;
		correct = ((currToLastOffsetDeltaIsBigger(2,evenLineLonger)?
				((evenPoleAvgOffset>oddPoleAvgOffset)?true:false):
				((evenPoleAvgOffset<oddPoleAvgOffset)?true:false)));
		
		System.out.println("evenPoleAvgOffset/oddPoleAvgOffset:"
				+"/"+evenPoleAvgOffset+"/"+oddPoleAvgOffset
				+"/"+(correct?"Correct":"Wrong"));
		
		return correct;
	}
	
	private boolean curLineIsLonger(int lineIdx, boolean evenLineLonger){
		boolean curLineLonger = false;
		if(0==lineIdx%2 && evenLineLonger || 1==lineIdx%2 && !evenLineLonger) curLineLonger = true;
		return curLineLonger;
	}
	
	private boolean currToLastOffsetDeltaIsBigger(int lineIdx, boolean evenLineLonger){
		boolean bigger = false;
		
		if(0==lineIdx%2 && firstPoleIsThicker || 1==lineIdx%2 && !firstPoleIsThicker){
			if(firstPoleIsLonger && evenLineLonger 
				|| !firstPoleIsLonger && !evenLineLonger) bigger=true;
		}
		
		return bigger;
	}
	
	private int[] getWeightChkRange(LinkedHashMap<Integer, ImgExtractLine> oriLinesMap,LinkedHashMap<Integer, ImgExtractLine> newLinesMap,int oriLineIdx,int newLineIdx){
		int[] chkRange = new int[2];
		//Get the max range(Max start and Min stop) of the near three lines
		ImgExtractLine curLine = oriLinesMap.get(oriLineIdx);
		ImgExtractLine lastLine = newLinesMap.get(newLineIdx-1);
		ImgExtractLine nextLine = oriLinesMap.get(oriLineIdx+1);
		
		chkRange[0] = curLine.getXAxisStart();
		chkRange[1] = curLine.getXAxisStop();
		if(null!=lastLine){
			if(lastLine.getXAxisStart()>chkRange[0]) chkRange[0] = lastLine.getXAxisStart();
			if(lastLine.getXAxisStop()<chkRange[1]) chkRange[1] = lastLine.getXAxisStop();
		}
		if(null!=nextLine){
			if(nextLine.getXAxisStart()>chkRange[0]) chkRange[0] = nextLine.getXAxisStart();
			if(nextLine.getXAxisStop()<chkRange[1]) chkRange[1] = nextLine.getXAxisStop();
		}
		
		return chkRange;
	}
	
	private int[] shiftLineIndex(int[] oriLineIdx,int startIdx,int stopIdx,int shiftVal){
		int start = 0, stop = 0, baseVal = 0;
		if(startIdx<0) startIdx = 0;
		if(startIdx>=oriLineIdx.length) startIdx = oriLineIdx.length - 1;
		if(stopIdx<0) stopIdx = 0;
		if(stopIdx>=oriLineIdx.length) stopIdx = oriLineIdx.length - 1;
		
		if(startIdx>stopIdx){
			start = stopIdx;
			stop = startIdx;
		}else{
			start = startIdx;
			stop = stopIdx;
		}
		
		if(shiftVal<0){
			for(int k=start;k<=stop;k++){
				oriLineIdx[k] = oriLineIdx[k] + shiftVal;
			}
		}else{
			baseVal = oriLineIdx[start];
			for(int k=start;k<=stop;k++){
				baseVal = baseVal + shiftVal;
				if(oriLineIdx[k]<baseVal) oriLineIdx[k] = oriLineIdx[k] + shiftVal;
			}
		}
		
		return oriLineIdx;
	}
	
	private int shiftCurrentLineNext(ImgExtractLine currLine, ImgExtractLine lastLine, ImgExtractLine nextLine, int currLineIdx, int targetLinesQty, 
										double[] weightCurLine, double[] weightLastLine, double[] weightNextLine, boolean curLineIsLonger, int[][] imgGrays,
										LinkedHashMap<Integer, ImgExtractLine> linesMap, double evenPoleAvgOffset, double oddPoleAvgOffset){
		boolean curLineIsThicker = false, curLineOffsetIsOK = false, curLineLengthIsOK = false, overhangFailed = false;
		boolean lastLineLengthIsOK = false, nextLineLengthIsOK = false;
		double curLineOffset = 0.0, lastLineOffset = 0.0, nextLineOffset = 0.0;
		double offsetChgR0 = 0.0, offsetChgR1 = 0.0, weightDiff = 0.0;
		int maxValidPtsLineIdxDelta = 0, shiftFlag = 0, peakFlag = 0;
		
		shiftFlag = 0;//Default is no need to shift current line
		curLineOffset = currLine.getLineIntercept()+currLine.getLineInterceptDelta();
		if(null!=lastLine){
			lastLineOffset = lastLine.getLineIntercept()+lastLine.getLineInterceptDelta();
		}else{
			return shiftFlag;
		}
		
		if(1==currLineIdx%2){
			offsetChgR0 = (curLineOffset - lastLineOffset) / oddPoleAvgOffset;
		}else{
			offsetChgR0 = (curLineOffset - lastLineOffset) / evenPoleAvgOffset;
		}
		
		if(null!=nextLine){
			nextLineOffset = nextLine.getLineIntercept()+nextLine.getLineInterceptDelta();
			if(0==(currLineIdx+1)%2){
				offsetChgR1 = (nextLineOffset - curLineOffset) / evenPoleAvgOffset;
			}else{
				offsetChgR1 = (nextLineOffset - curLineOffset) / oddPoleAvgOffset;
			}
		}
		
		curLineLengthIsOK = (curLineIsLonger && weightCurLine[0]>=0.5 || !curLineIsLonger && weightCurLine[0]<0.5);
		curLineOffsetIsOK = (offsetChgR0>minOffsetChgRate && offsetChgR0<maxOffsetChgRate);
		
		lastLineLengthIsOK = (curLineIsLonger && weightLastLine[0]<0.5 || !curLineIsLonger && weightLastLine[0]>=0.5);
		if(null!=nextLine){
			nextLineLengthIsOK = (curLineIsLonger && weightNextLine[0]<0.5 || !curLineIsLonger && weightNextLine[0]>=0.5);
			weightDiff = Math.abs(weightCurLine[0]-weightNextLine[0]);
		}
		
		//Pre-screen conditions
		if(curLineLengthIsOK && lastLineLengthIsOK && nextLineLengthIsOK) return shiftFlag;
		
		if(curLineLengthIsOK && curLineOffsetIsOK){
			if(null!=nextLine && currLineIdx==targetLinesQty-2){
				if(offsetChgR0>maxOffsetChgRate*0.8 || offsetChgR1>maxOffsetChgRate || offsetChgR1<minOffsetChgRate){
					peakFlag = offsetInSamePeakEx(allPossibleLines,(curLineOffset+lastLineOffset)/2,lastLineOffset);
					if(0==peakFlag) peakFlag = offsetInSamePeakEx(allPossibleLines,(curLineOffset+lastLineOffset)/2,curLineOffset);
					if(0==peakFlag){
						shiftFlag = 1;//Current line shift next
					}else if(!curLineIsLonger && weightNextLine[0]>weightCurLine[0] && weightDiff>0.2){
						//Suppose current line is correct
					}else if(offsetChgR0>maxOffsetChgRate*0.8){
						overhangFailed = twoNearLinesOverhangFailed(currLineIdx,currLine,nextLine,weightCurLine[0],weightNextLine[0]);
						if(overhangFailed && weightDiff>0.2){
							maxValidPtsLineIdxDelta = getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+5)
									-getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+30);
							if(0==maxValidPtsLineIdxDelta) shiftFlag = 1;//Current line shift next(Give it a try)
						}
					}
				}
			}
		}else{
			if(curLineLengthIsOK && offsetChgR0<=minOffsetChgRate){
				return shiftFlag;
			}else if(curLineLengthIsOK && offsetChgR0>=maxOffsetChgRate || !curLineLengthIsOK && curLineOffsetIsOK){//Possibly need to shift next
				if(currLineIdx!=targetLinesQty-2){
					if(thereIsPoleBtw(imgGrays,currLine,lastLine,true)){
						shiftFlag = 1;//Current line shift next
					}else{
						return shiftFlag;
					}
				}else{
					curLineIsThicker = lineIsThicker(weightCurLine,imgGrays,currLine,lastLine,offsetChgR0);
					if(firstPoleIsThicker && curLineIsThicker) return shiftFlag;
					
					if(null!=nextLine){
						if(curLineOffsetIsOK && lastLineLengthIsOK && nextLineLengthIsOK && offsetChgR1>minOffsetChgRate && offsetChgR1<maxOffsetChgRate
							&& !curLineIsLonger && weightNextLine[0]-weightCurLine[0]>0.2){
							//Suppose current line is correct
						}else{
							overhangFailed = twoNearLinesOverhangFailed(currLineIdx,currLine,nextLine,weightCurLine[0],weightNextLine[0]);
							if(overhangFailed){
								peakFlag = offsetInSamePeakEx(allPossibleLines,(curLineOffset+lastLineOffset)/2,lastLineOffset);
								if(0==peakFlag) peakFlag = offsetInSamePeakEx(allPossibleLines,(curLineOffset+lastLineOffset)/2,curLineOffset);
								if(offsetChgR1>maxOffsetChgRate || offsetChgR1<minOffsetChgRate){//Next line is out of expectation
									if(0==peakFlag){
										shiftFlag = 1;//Current line shift next
									}else{
										if(!curLineLengthIsOK){
											if(weightDiff>0.2) shiftFlag = 1;//Current line shift next(Give it a try)
										}
									}
								}else{
									if(curLineLengthIsOK){
										if(0==peakFlag) shiftFlag = 1;//Current line shift next
									}else{
										if(weightDiff>0.2) shiftFlag = 1;//Current line shift next(Give it a try)
									}
								}
							}else{
								if(!curLineLengthIsOK){
									if(offsetChgR0>maxOffsetChgRate*0.72){
										if(weightDiff>0.2){
											shiftFlag = 1;//Current line shift next(Give it a try)
										}else{
											maxValidPtsLineIdxDelta = getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+5)
													-getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+30);
											if(0==maxValidPtsLineIdxDelta) shiftFlag = 1;//Current line shift next(Give it a try)
										}
									}
								}
							}
						}
					}else{
						maxValidPtsLineIdxDelta = getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+5)
								-getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+30);
						overhangFailed = twoNearLinesOverhangFailed(currLineIdx-1,lastLine,currLine,weightLastLine[0],weightCurLine[0]);
						if(0==maxValidPtsLineIdxDelta && overhangFailed){//There is no potential line after current line and overhang is NG btw current line and last line
							shiftFlag = 1;//Current line shift next(Give it a try)
						}
					}
				}
			}else if(!curLineLengthIsOK && offsetChgR0<=minOffsetChgRate){//Current line is too closed to last line, should be skipped
				shiftFlag = -1;//Skip current line
			}else if(!curLineLengthIsOK && offsetChgR0>=maxOffsetChgRate){//Possibly need to shift next
				if(currLineIdx==targetLinesQty-2){
					curLineIsThicker = lineIsThicker(weightCurLine,imgGrays,currLine,lastLine,offsetChgR0);
					if(firstPoleIsThicker && curLineIsThicker) return shiftFlag;
					
					peakFlag = offsetInSamePeakEx(allPossibleLines,(curLineOffset+lastLineOffset)/2,lastLineOffset);
					if(0==peakFlag) peakFlag = offsetInSamePeakEx(allPossibleLines,(curLineOffset+lastLineOffset)/2,curLineOffset);
					if(0==peakFlag){
						shiftFlag = 1;//Current line shift next
					}else if(offsetChgR0>maxOffsetChgRate*0.8 && null!=nextLine){
						overhangFailed = twoNearLinesOverhangFailed(currLineIdx,currLine,nextLine,weightCurLine[0],weightNextLine[0]);
						if(overhangFailed && weightDiff>0.2){
							if(offsetChgR0>maxOffsetChgRate && weightDiff>0.5){
								shiftFlag = 1;
							}else{
								maxValidPtsLineIdxDelta = getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+5)
										-getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+30);
								if(0==maxValidPtsLineIdxDelta) shiftFlag = 1;//Current line shift next(Give it a try)
							}
						}
					}
				}
			}
		}
		
		return shiftFlag;
	}
	
	private LinkedHashMap<Integer, ImgExtractLine> doubleCheckAllLinesNew(LinkedHashMap<Integer, ImgExtractLine> oriLinesMap, int[][] peakVals, int[][] initVals, int[][] imgGrays, boolean evenLineLonger, int targetLinesQty, double evenPoleAvgOffset, double oddPoleAvgOffset, double linesTendency_Slope, double linesTendency_Offset){
		LinkedHashMap<Integer, ImgExtractLine> linesMap = new LinkedHashMap<Integer, ImgExtractLine>();
		LinkedHashMap<Integer, ImgExtractLine> newLinesMap = new LinkedHashMap<Integer, ImgExtractLine>();
		ImgExtractLine currLine = null, nextLine = null, lastLine = null;
		
		for(int i=0; i<targetLinesQty; i++){
			if(null!=oriLinesMap.get(i)) linesMap.put(i, oriLinesMap.get(i));
		}
		int[] newLineIdx = new int[linesMap.size()];
		int[] oriLineIdx = new int[linesMap.size()];
		
		int idx = -1, lineIdx = -1, shiftFlag = 0, maxValidPtsLineIdxDelta = 0;
		int[] weightChkRange = null;
		boolean curLineIsLonger = false, curLineIsConfirmed = false, overhangFailed = false;
		boolean curLineLengthIsOK = false, curLineOffsetIsOK = false;
		boolean lastLineLengthIsOK = false, nextLineLengthIsOK = false;
		double[] weightCurLine = null, weightLastLine = null, weightNextLine = null, weight3 = null;
		double offsetChgR0 = 0.0, offsetChgR1 = 0.0, offsetChgR2 = 0.0;
		double nextLineOffset = 0.0, lastLineOffset = 0.0, curLineOffset = 0.0;
		
		for(int key:linesMap.keySet()){
			idx++;
			newLineIdx[idx] = key;//The final line index(will be adjusted in this routine)
			oriLineIdx[idx] = key;//The 1st guessing line index
		}
		
		for(int i=0; i<=idx; i++){
			if(newLineIdx[i]>=targetLinesQty) break;
			offsetChgR0 = 0.0; offsetChgR1 = 0.0; offsetChgR2 = 0.0;
			weightLastLine = null; weightNextLine = null;
			lastLineLengthIsOK = false; nextLineLengthIsOK = false;
			lineIdx = oriLineIdx[i];
			currLine = linesMap.get(lineIdx);//The line to be checked
			lastLine = newLinesMap.get(newLineIdx[i]-1);
			nextLine = linesMap.get(lineIdx+1);
			if(null!=currLine){
				if(currLine.getLineChecked()){
					newLinesMap.put(newLineIdx[i], currLine);
					continue;
				}
				curLineIsLonger = curLineIsLonger(newLineIdx[i],evenLineLonger);
				curLineOffset = currLine.getLineIntercept()+currLine.getLineInterceptDelta();
				if(null!=lastLine){
					lastLineOffset = lastLine.getLineIntercept()+lastLine.getLineInterceptDelta();
				}else{
					lastLineOffset = linesTendency_Slope*(newLineIdx[i]-1)+linesTendency_Offset;
				}
				if(1==newLineIdx[i]%2){
					offsetChgR0 = (curLineOffset - lastLineOffset) / oddPoleAvgOffset;
				}else{
					offsetChgR0 = (curLineOffset - lastLineOffset) / evenPoleAvgOffset;
				}
				if(offsetChgR0<0) continue;
				
				if(null!=nextLine){
					nextLineOffset = nextLine.getLineIntercept()+nextLine.getLineInterceptDelta();
					if(0==(newLineIdx[i]+1)%2){
						offsetChgR1 = (nextLineOffset - curLineOffset) / evenPoleAvgOffset;
						offsetChgR2 = (nextLineOffset - lastLineOffset) / evenPoleAvgOffset;
					}else{
						offsetChgR1 = (nextLineOffset - curLineOffset) / oddPoleAvgOffset;
						offsetChgR2 = (nextLineOffset - lastLineOffset) / oddPoleAvgOffset;
					}
				}
				
				if(5==lineIdx){
					System.out.print("");
				}
				
				weightChkRange = getWeightChkRange(linesMap,newLinesMap,lineIdx,newLineIdx[i]);
				weightCurLine = currLine.weightOfBeingLine(linesMap, weightChkRange[1], weightChkRange[0], 0, imgGrays, peakVals, 5, lineIdx, targetLinesQty, true, curLineIsLonger(newLineIdx[i],evenLineLonger), false);
				if(null!=lastLine){
					weightLastLine = currLine.weightOfBeingLine(linesMap, weightChkRange[1], weightChkRange[0], 0, imgGrays, peakVals, 5, newLineIdx[i]-1, targetLinesQty, true, curLineIsLonger(newLineIdx[i]-1,evenLineLonger), false);
					lastLineLengthIsOK = (curLineIsLonger && weightLastLine[0]<0.5 || !curLineIsLonger && weightLastLine[0]>=0.5);
				}
				if(null!=nextLine){
					weightNextLine = currLine.weightOfBeingLine(linesMap, weightChkRange[1], weightChkRange[0], 0, imgGrays, peakVals, 5, lineIdx+1, targetLinesQty, true, curLineIsLonger(newLineIdx[i]+1,evenLineLonger), false);
					nextLineLengthIsOK = (curLineIsLonger && weightNextLine[0]<0.5 || !curLineIsLonger && weightNextLine[0]>=0.5);
				}
				
				curLineLengthIsOK = (curLineIsLonger && weightCurLine[0]>=0.5 || !curLineIsLonger && weightCurLine[0]<0.5);
				curLineOffsetIsOK = (offsetChgR0>minOffsetChgRate && offsetChgR0<maxOffsetChgRate);
				
				curLineIsConfirmed = false;
				//1.Expected case handling - Length of current pole is in expectation
				if(curLineLengthIsOK){
					//Offset variation of the most inner two poles could be bigger
					if(newLineIdx[i]==targetLinesQty-2){
						if(null!=nextLine){
							if(nextLine.getLineChecked()){//The most inner pole has confirmed
								currLine.setLineChecked(true);
								newLinesMap.put(newLineIdx[i], currLine);
								curLineIsConfirmed = true;
							}
						}
						//Length of current,left and right poles is in expectation
						if(!curLineIsConfirmed && lastLineLengthIsOK && nextLineLengthIsOK
							&& offsetChgR0>minOffsetChgRate*0.75){ //Pole offset meets the lower limit
							currLine.setLineChecked(true);
							newLinesMap.put(newLineIdx[i], currLine);
							curLineIsConfirmed = true;
						}
						//Offset of current pole is in control limit
						if(!curLineIsConfirmed){
							shiftFlag = shiftCurrentLineNext(currLine, lastLine, nextLine, newLineIdx[i], targetLinesQty, 
									weightCurLine, weightLastLine, weightNextLine, curLineIsLonger, imgGrays,
									newLinesMap, evenPoleAvgOffset, oddPoleAvgOffset);
							if(0<=shiftFlag){
								//Shift current line to next line
								if(1==shiftFlag) newLineIdx=shiftLineIndex(newLineIdx,i,linesMap.size()-1,1);
								currLine.setLineChecked(true);
								newLinesMap.put(newLineIdx[i], currLine);
								curLineIsConfirmed = true;
							}
						}
					}else if(null!=lastLine && offsetChgR0>minOffsetChgRate*0.75 //Lower offset control limit could be loser while pole length is in expectation
						&& offsetChgR0<=maxOffsetChgRate //Pole offset is in control limit
						|| 0==newLineIdx[i] //Current line represents the most outer pole(the 1st pole)
						|| null!=lastLine && 1==currLine.getLineFlag() //Last line is fixed and current line is in the expected tendency
						|| null!=lastLine && curLineOffsetIsOK //Pole offset is in control limit
							//Current pole is the most inner pole and last pole is fixed
							&& newLineIdx[i]==targetLinesQty-1){
						//Key conditions:
						//(1)Last pole is fixed and offset of current pole is in ctrl limit
						//(2)Last pole is fixed and current pole is in ctrl tendency
						currLine.setLineChecked(true);
						newLinesMap.put(newLineIdx[i], currLine);
						curLineIsConfirmed = true;
					}
				}else if(0==newLineIdx[i] //Current line represents the most outer pole(the 1st pole)
					&& (offsetChgR0<=maxOffsetChgRate //Pole offset is in control criteria
						|| curLineIsLonger && weightCurLine[0]<0.5 //Length of current pole and next pole is very closed
						//Length of the most outer pole is not expected, which is usually caused by a longer
						//pole clings to the most outer short pole which leads to failed detection of short pole
						|| !curLineIsLonger && weightCurLine[0]>=0.5)){
					currLine.setLineChecked(true);
					newLinesMap.put(newLineIdx[i], currLine);
					curLineIsConfirmed = true;
				}else if(evenLineLonger && 1==newLineIdx[i]
					&& weightCurLine[0]>=0.5
					&& offsetChgR0>minOffsetChgRate*0.6){
					currLine.setLineChecked(true);
					newLinesMap.put(newLineIdx[i], currLine);
					curLineIsConfirmed = true;
				}
				
				//2.Common case handling
				if(!curLineIsConfirmed){//Length of current pole is out of expectation
					//2-1.Try to fix last pole first
					if(null==lastLine){ //Last pole is missing
						if(offsetChgR0<minOffsetChgRate //Current pole is too closed to last pole
							//Length of current pole is not expected
							|| offsetChgR0<maxOffsetChgRate*0.85 && !curLineLengthIsOK
							//Current line represents negative pole and last pole is negative pole
							|| offsetChgR0<maxOffsetChgRate*0.85 
								&& lineIsThicker(weightCurLine,imgGrays,currLine,lastLine,offsetChgR0) //Current line represents negative pole
								&& lineIsNegPole(newLineIdx[i]-1,evenLineLonger,evenPoleAvgOffset,oddPoleAvgOffset) //Last pole is negative pole
							//Current line represents the most inner pole and last pole is negative pole
							|| offsetChgR0<maxOffsetChgRate*0.75 && newLineIdx[i]==targetLinesQty-1 
								&& lineIsNegPole(newLineIdx[i]-1,evenLineLonger,evenPoleAvgOffset,oddPoleAvgOffset) //Last pole is negative pole
							//Current line represents the most inner pole and length of current pole is not expected
							|| offsetChgR0<maxOffsetChgRate*1.5 && newLineIdx[i]==targetLinesQty-1 
								&& (curLineIsLonger && weightCurLine[0]<0.45 //Length of current pole is not expected
									|| !curLineIsLonger && weightCurLine[0]>=0.5)){
							if(!lineIsThicker(weightCurLine,imgGrays,currLine,lastLine,offsetChgR0)
								&& newLineIdx[i]==targetLinesQty-1
								&& curLineIsLonger && weightCurLine[0]>0.5){
								currLine.setLineChecked(true);
								newLinesMap.put(newLineIdx[i], currLine);
								curLineIsConfirmed = true;
							}else{
								//Shift current line to previous line
								newLineIdx=shiftLineIndex(newLineIdx,i,linesMap.size()-1,-1);
								currLine.setLineChecked(true);
								newLinesMap.put(newLineIdx[i], currLine);
								curLineIsConfirmed = true;
							}
						}else if((offsetChgR0>maxOffsetChgRate || newLineIdx[i]==targetLinesQty-1)//Offset of current pole is bigger or current pole is the most inner pole
							&& (curLineIsLonger && weightCurLine[0]>0.45 //Length of current pole is in expectation
								|| !curLineIsLonger && weightCurLine[0]<0.5)){
								//Offset of current pole exceeds upper limit but length of it is in expectation
								if(newLineIdx[i]==targetLinesQty-1 && curLineIsLonger
									&& weightCurLine[0]>=0.5 && lineIsThicker(weightCurLine,imgGrays,currLine,lastLine,offsetChgR0)){
									//Shift current line to previous line
									newLineIdx=shiftLineIndex(newLineIdx,i,linesMap.size()-1,-1);
								}
								currLine.setLineChecked(true);
								newLinesMap.put(newLineIdx[i], currLine);
								curLineIsConfirmed = true;
						}
					}
					//2-2.Refer to next line first since last line is supposed correct
					if(!curLineIsConfirmed && null!=nextLine){
						if(nextLineLengthIsOK){//Length of next pole is in expectation
							if(curLineLengthIsOK && nextLineLengthIsOK){
								//Offset of both current and next poles is on target
								currLine.setLineChecked(true);
								newLinesMap.put(newLineIdx[i], currLine);
								curLineIsConfirmed = true;
							}
						}else{//Length of next pole is out of expectation
							if(offsetChgR1<minOffsetChgRate && offsetChgR2<maxOffsetChgRate){//Next pole is too closed to current pole
								if(newLineIdx[i]==targetLinesQty-2 && nextLine.getLineChecked()){
									currLine.setLineChecked(true);
									newLinesMap.put(newLineIdx[i], currLine);
									curLineIsConfirmed = true;
								}else{
									//Shift next line to current line
									newLineIdx=shiftLineIndex(newLineIdx,i+1,linesMap.size()-1,-1);
									nextLine.setLineChecked(true);
									newLinesMap.put(newLineIdx[i], nextLine);//Shift next line to current line
								}
								curLineIsConfirmed = true;
							}else if((offsetChgR0>maxOffsetChgRate*0.85 || newLineIdx[i]==targetLinesQty-2 && offsetChgR0>minOffsetChgRate*0.6)//Offset of current pole is in expectation
									//Current pole is longer(expect shorter) than next pole
									&& !curLineIsLonger && weightCurLine[0]-weightNextLine[0]>0.15
							){
								//TODO Verification
								shiftFlag = shiftCurrentLineNext(currLine, lastLine, nextLine, newLineIdx[i], targetLinesQty, 
										weightCurLine, weightLastLine, weightNextLine, curLineIsLonger, imgGrays,
										newLinesMap, evenPoleAvgOffset, oddPoleAvgOffset);
								if(0<=shiftFlag){
									//Shift current line to next line
									if(1==shiftFlag) newLineIdx=shiftLineIndex(newLineIdx,i,linesMap.size()-1,1);
									currLine.setLineChecked(true);
									newLinesMap.put(newLineIdx[i], currLine);
								}
								curLineIsConfirmed = true;
							}else if(offsetChgR0>maxOffsetChgRate){
								maxValidPtsLineIdxDelta = getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+5)
										-getMaxValidDataPtsLineIndex(allPossibleLines,curLineOffset-5,curLineOffset+30);
								overhangFailed = twoNearLinesOverhangFailed(newLineIdx[i],currLine,nextLine,weightCurLine[0],weightNextLine[0]);
								if(0==maxValidPtsLineIdxDelta || overhangFailed){
									//Shift current line to next line
									newLineIdx=shiftLineIndex(newLineIdx,i,linesMap.size()-1,1);
								}
								currLine.setLineChecked(true);
								newLinesMap.put(newLineIdx[i], currLine);
								curLineIsConfirmed = true;
							}else if(offsetChgR0>minOffsetChgRate*0.6 && offsetChgR1<maxOffsetChgRate
								&& 1==currLine.getLineFlag()){
								//Offset of next pole is in expectation
								//Offset of current pole meets the lower limit and it is in expecting tendency
								currLine.setLineChecked(true);
								newLinesMap.put(newLineIdx[i], currLine);
								curLineIsConfirmed = true;
							}
						}
					}
					//2-3.Refer to last line
					if(!curLineIsConfirmed && null!=lastLine){
						if(lastLineLengthIsOK){//Length of last pole is in expectation
							if(newLineIdx[i]==targetLinesQty-2){
								if(offsetChgR0>minOffsetChgRate*0.5){
									shiftFlag = shiftCurrentLineNext(currLine, lastLine, nextLine, newLineIdx[i], targetLinesQty, 
											weightCurLine, weightLastLine, weightNextLine, curLineIsLonger, imgGrays,
											newLinesMap, evenPoleAvgOffset, oddPoleAvgOffset);
									if(1==shiftFlag){
										//Shift current line to next line
										newLineIdx=shiftLineIndex(newLineIdx,i,linesMap.size()-1,1);
									}
									currLine.setLineChecked(true);
									newLinesMap.put(newLineIdx[i], currLine);
									curLineIsConfirmed = true;
								}
							}else if(newLineIdx[i]==targetLinesQty-1 && curLineIsLonger){
								if(weightCurLine[0]>0.5 && currLine.getTendencyRSQ()>minBestRsq){
									currLine.setLineChecked(true);
									newLinesMap.put(newLineIdx[i], currLine);
									curLineIsConfirmed = true;
								}else{
									//Enlarge inner side searching range
									double researchOffset = currLine.getLineIntercept()+currLine.getLineInterceptDelta()+3;
									double researchOffsetStop = researchOffset + (evenPoleAvgOffset+oddPoleAvgOffset)/2*3;
									double researchSlope = currLine.getLineSlope();
									ImgExtractLine exLine = null;
									exLine = searchLine(peakVals, initVals, imgGrays, 255, researchOffset, researchOffsetStop, researchSlope, (int)(researchOffsetStop-researchOffset));
									if(null!=exLine){
										linesMap.put(lineIdx+1, exLine);
										weight3 = currLine.weightOfBeingLine(linesMap, weightChkRange[1], weightChkRange[0], 0, imgGrays, peakVals, 5, lineIdx+1, targetLinesQty, true, curLineIsLonger(newLineIdx[i],evenLineLonger), false);
										if(weight3[0]>0.5 && weight3[0]>weightCurLine[0]
											|| weight3[0]>weightCurLine[0] && currLine.getTendencyRSQ()<minBestRsq){
											exLine.setLineChecked(true);
											newLinesMap.put(newLineIdx[i], exLine);
											curLineIsConfirmed = true;
										}
									}
									if(!curLineIsConfirmed){
										currLine.setLineChecked(true);
										newLinesMap.put(newLineIdx[i], currLine);
										curLineIsConfirmed = true;
									}
								}
							}
							
							//Offset of current pole is in a more expecting range
							if(!curLineIsConfirmed && offsetChgR0>minOffsetChgRate*1.1 && offsetChgR0<maxOffsetChgRate){
								currLine.setLineChecked(true);
								newLinesMap.put(newLineIdx[i], currLine);
								curLineIsConfirmed = true;
							}
						}
					}
				}
				
				//3.Abnormal case handling
				if(!curLineIsConfirmed){
					if(null!=lastLine && null!=nextLine){
						//Length of last and next poles is in expectation
						//Offset of last and next poles exceed the upper limit(they are far way from current pole)
						//Length of current pole is out of expectation
						if(offsetChgR0>maxOffsetChgRate && offsetChgR0<maxOffsetChgRate*1.5
							&& offsetChgR1>maxOffsetChgRate && offsetChgR1<maxOffsetChgRate*1.5
							&& (curLineIsLonger && weightCurLine[0]<0.5 
									&& weightLastLine[0]<0.5 && weightNextLine[0]<0.5
								|| !curLineIsLonger && weightCurLine[0]>=0.5
									&& weightLastLine[0]>=0.5 && weightNextLine[0]>=0.5)){
							//Shift current line to next line
							newLineIdx=shiftLineIndex(newLineIdx,i,linesMap.size()-1,1);
							currLine.setLineChecked(true);
							newLinesMap.put(newLineIdx[i], currLine);
							curLineIsConfirmed = true;
						}
					}
				}
			}
		}
		
		return newLinesMap;
	}
	
	private double getPeakGrayThreshold(int refGray){
		double grayThr = maxGrayVal*0.98;
		
		if(refGray<noiseGrayThr){
			grayThr = noiseGrayThr;
		}else if(refGray<noiseGrayThr2){
			grayThr = noiseGrayThr2;
		}
		
		return grayThr;
	}
	
	private ArrayList<Object> thereIsPeakAround(int[][] imgGrays, int[][] peakVals, int refGray, int validGrayDelta, int x, int startY, int stopY, double grayDeltaFactor, double peakGrayThr){
		ArrayList<Object> rtn = new ArrayList<Object>();
		boolean thereIsPeak = false;
		int imgHeight = imgGrays[0].length;
		int minPeakG = (int)maxGrayVal;
		
		for(int y=startY; y<=stopY; y++){
			if(y>=0 && y<imgHeight){
				if((Math.abs(refGray-imgGrays[x][y])<validGrayDelta*grayDeltaFactor || refGray>imgGrays[x][y])
				      && peakVals[x][y]>0 && peakVals[x][y]<peakGrayThr){
					thereIsPeak = true;
					if(minPeakG>peakVals[x][y]) minPeakG = peakVals[x][y];
				}
			}
		}
		
		rtn.add(0, thereIsPeak);
		rtn.add(1, minPeakG);
		
		return rtn;
	}
	
	private int getWrinkleRefGray(ImgExtractLine curLine, int[][] imgGrays, int[][] peakVals, int wrinkleStartChkPos, int wrinkleStopChkPos, int maxChkOffset, boolean chkDownSide){
		int refGray = 0, refAvgPts = 5, counter = 0;
		double tmpSlope = curLine.getLineSlope();
		double tmpOffset = curLine.getLineIntercept()+curLine.getLineInterceptDelta();
		int y = 0, nearCol = 0, imgHeight = peakVals[0].length;
		
		for(int x=wrinkleStartChkPos; x>wrinkleStopChkPos; x--){
			nearCol = (int)(x*tmpSlope+tmpOffset);
			if(nearCol<0 || nearCol>=imgHeight) continue;
			
			for(int n=0; n<=(int)maxChkOffset; n++){
				if(chkDownSide){
					y = nearCol + n;
				}else{
					y = nearCol - n;
				}
				if(y>=0 && y<imgHeight && peakVals[x][y]>0 && peakVals[x][y]<noiseGrayThr){
					refGray += imgGrays[x][y];
					counter++;
					if(counter>=refAvgPts) break;
				}
			}
			if(counter>=refAvgPts) break;
		}
		if(counter>0) refGray = refGray / counter;
		
		return refGray;
	}
	
	private ArrayList<ArrayList<String>> mergeWrinkleLines(ArrayList<ArrayList<String>> wrinkleLines){
		ArrayList<ArrayList<String>> mergeLines = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> tempLines = new ArrayList<ArrayList<String>>();
		
		ArrayList<String> pts = new ArrayList<String>();
		ArrayList<String> initPts = new ArrayList<String>();
		String[] initPt = null, pt = null;
		double[] coef = null;
		int minMergeIdx = -1, maxYDelta = lineScanRange/2, meetCnt = 0, counter = 0;
		int x1 = 0, x2 = 0, yExpected = 0, maxXDelta = lineScanRange*2;
		boolean bMerged = false;
		
		if(wrinkleLines.isEmpty()) return wrinkleLines;
		//Sort the lines
		if(wrinkleLines.size()>1){
			for(int i=0; i<(wrinkleLines.size()-1); i++){
				for(int j=i+1; j<wrinkleLines.size(); j++){
					x1 = Integer.parseInt(wrinkleLines.get(i).get(0).split(",")[0]);
					x2 = Integer.parseInt(wrinkleLines.get(j).get(0).split(",")[0]);
					if(x1<x2){
						pts = wrinkleLines.get(i);
						wrinkleLines.remove(i);
						wrinkleLines.add(i,wrinkleLines.get(j));
						wrinkleLines.remove(j);
						wrinkleLines.add(j,pts);
					}
				}
			}
		}
		
		for(int i=0; i<wrinkleLines.size(); i++){
			pts = wrinkleLines.get(i);
			if(pts.size()<lineScanRange) continue;
			tempLines.add(pts);
		}
		
		if(tempLines.size()>1){
			//Get minimum merge index
			for(int i=0; i<tempLines.size()-1; i++){
				initPts = tempLines.get(i);
				coef = getWrinkleLineFittingParas(initPts,10);
				initPt = initPts.get(initPts.size()-1).split(",");//The last point
				meetCnt = 0; counter = 0;
				for(int j=i+1; j<tempLines.size(); j++){
					pts = tempLines.get(j);
					pt = pts.get(0).split(",");//The first point
					x1 = Integer.parseInt(pt[0]);
					if(coef[2]>0){
						yExpected = (int)(coef[0]*x1 + coef[1] + 0.5);
					}else{
						yExpected = Integer.parseInt(initPt[1]);
					}
					if(Integer.parseInt(pt[0])<Integer.parseInt(initPt[0]) 
					&& (Integer.parseInt(initPt[0])-Integer.parseInt(pt[0]))<=maxXDelta
					&& Math.abs(Integer.parseInt(pt[1])-yExpected)<=maxYDelta){
						meetCnt++;
					}
					counter++;
				}
				if(meetCnt>0 && (0==i || meetCnt==counter)){
					minMergeIdx = i;
					if(meetCnt!=counter) break;
				}else{
					break;
				}
			}
			
			if(minMergeIdx<0){
				mergeLines = tempLines;
			}else{
				initPts = new ArrayList<String>();
				for(int i=0; i<=minMergeIdx; i++){
					pts = tempLines.get(i);
					for(int j=0; j<pts.size(); j++){
						initPts.add(pts.get(j));
					}
				}
				initPt = initPts.get(initPts.size()-1).split(",");
				coef = getWrinkleLineFittingParas(initPts,10);
				
				if(minMergeIdx+1<tempLines.size()){
					bMerged = false;
					for(int i=minMergeIdx+1; i<tempLines.size(); i++){
						pts = tempLines.get(i);
						pt = pts.get(0).split(",");
						x1 = Integer.parseInt(pt[0]);
						if(coef[2]>0){
							yExpected = (int)(coef[0]*x1 + coef[1] + 0.5);
						}else{
							yExpected = Integer.parseInt(initPt[1]);
						}
						
						if(Math.abs(Integer.parseInt(pt[1])-yExpected)<=maxYDelta
						&& (Integer.parseInt(initPt[0])-Integer.parseInt(pt[0]))<=maxXDelta){
							bMerged = true;
							for(int j=0; j<initPts.size(); j++){
								pts.add(initPts.get(j));
							}
						}
						mergeLines.add(pts);
					}
					if(!bMerged) mergeLines.add(initPts);
				}else{
					mergeLines.add(initPts);
				}
			}
		}else{
			mergeLines = wrinkleLines;
		}
		
		return mergeLines;
	}
	
	private ArrayList<ArrayList<String>> screenWrinkleLines(ArrayList<ArrayList<String>> wrinkleLines){
		ArrayList<ArrayList<String>> tempLines = new ArrayList<ArrayList<String>>();
		
		if(wrinkleLines.isEmpty()) return wrinkleLines;
		ArrayList<String> pts = new ArrayList<String>();
		for(int i=0; i<wrinkleLines.size(); i++){
			pts = wrinkleLines.get(i);
			if(pts.size()<lineScanRange) continue;
			tempLines.add(pts);
		}
		
		return tempLines;
	}
	
	/**
	 * @param curLine
	 * @param wrinkleLines
	 * @param startX
	 * @param stopX
	 * @param startY
	 * @param stopY
	 * @return double[]: 0-score, 1-validPtsRatio, 2-RSQ, 3-distanceToOri, 4-yRange, 5-selectIndex, 6-crossAngle, 7-lineDataPts, 8-lineRsq
	 */
	private double[] selectWrinkleLine(ImgExtractLine curLine, ImgExtractLine sideLine, ArrayList<ArrayList<String>> wrinkleLines, int[][] imgGrays, int startX, int stopX, int startY, int stopY, double areaCoverRate, boolean chkDownSide){
		ArrayList<Object> wrinkleScores = new ArrayList<Object>();
		double scorePts = 10, scoreLen = 10, scoreContinuity = 30, scoreRsq = 30, scoreDistance = 10, scoreCrossX = 10;
		double minY = 0.0, maxY = 0.0, xVal = 0.0, yVal = 0.0, selAngle = 0.0;
		double scanAreaLength = Math.sqrt((stopX-startX)*(stopX-startX)+(stopY-startY)*(stopY-startY)), oriDistance = 0.0, currScore = 0.0;
		double minScore = 55.0, minRSQ = 0.5, minX = 0.0, maxX = 0.0, tmpAngle = 0.0, tmpX = 0.0;
		double lineLengthR = 0.0, lineContinuity = 0.0, minLineContinuity = 0.5, minLineLengthR = 0.2;
		double maxPoleAngle = Double.parseDouble(""+criteria.get("maxPoleAngle")), minValidAngle = 1.0;
		double sqr_1 = 0.0, sqr_2 = 0.0, lineRsq = 0.0, selCrossX = 0.0, crossXR = 0.0, distanceR = 0.0, pointsR = 0.0;
		boolean chkMaxCrossX = false;
		int scanAreaDataPts = 0, selectLineIdx = -1, wrinkleLinesQty = 0, invalidFlag = 0, imgWidth = imgGrays.length;
		LinkedHashMap<Double,Double> wrinkleLinePts = new LinkedHashMap<Double,Double>();
		ArrayList<String> pts = null;
		ArrayList<Double> xVals = new ArrayList<Double>(), yVals = new ArrayList<Double>(), crossParas = new ArrayList<Double>();
		String pt[] = null;
		double[] coef = null, coef2 = null, selLineParas = null, lineSmoothParas = null, grayDelta = null, grayThresholds = new double[]{0.5,0.8,0.99,0.99,0.8,0.5};
		double tmpOffset = curLine.getLineIntercept() + curLine.getLineInterceptDelta();
		double tmpSlope = curLine.getLineSlope(), cmpOffset = 0.0, crossX = 0.0;
		
		for(int i=0; i<wrinkleLines.size(); i++){
			scanAreaDataPts += wrinkleLines.get(i).size();
		}
		
		if(Math.abs(tmpOffset-56.661)<0.01 && chkDownSide){
			System.out.print("");
		}else if(Math.abs(tmpOffset-229.959)<0.01 && !chkDownSide){
			System.out.print("");
		}
		
		ImgExtractLine wrinkleLine = new ImgExtractLine();
		for(int i=0; i<wrinkleLines.size(); i++){
			pts = wrinkleLines.get(i);
			pt = pts.get(0).split(",");
			minX = Double.parseDouble(pt[0]); maxX = Double.parseDouble(pt[0]);
			minY = Double.parseDouble(pt[1]); maxY = Double.parseDouble(pt[1]);
			xVals.clear(); yVals.clear(); wrinkleLinePts.clear(); invalidFlag = 0;
			wrinkleLine.clearLineData();
			for(int j=0; j<pts.size(); j++){
				pt = pts.get(j).split(",");
				xVal = Double.parseDouble(pt[0]);
				yVal = Double.parseDouble(pt[1]);
				wrinkleLinePts.put(xVal, yVal);
				xVals.add(xVal);
				yVals.add(yVal);
				if(minY>yVal) minY = yVal;
				if(maxY<yVal) maxY = yVal;
				if(minX>xVal) minX = xVal;
				if(maxX<xVal) maxX = xVal;
				wrinkleLine.addPoint((int)xVal, (int)yVal, imgGrays[(int)xVal][(int)yVal]);
			}
			if(wrinkleLinePts.size()<lineScanRange){
				wrinkleScores.add(i, new double[]{0.0,0.0,0.0,0.0,0.0,(double)i,0.0,wrinkleLinePts.size(),0.0});
				continue;
			}
			coef = MathUtils.lineFitting(xVals, yVals, 0, pts.size()-1);
			oriDistance = Math.sqrt((xVals.get(0)-startX)*(xVals.get(0)-startX)+(yVals.get(0)-startY)*(yVals.get(0)-startY));
			distanceR = oriDistance/scanAreaLength;
			
			//0-crossAngle,1-crossX,2-crossXDelta,3-crossYDelta,4-maxCrossXDelta,5-maxCrossYDelta,6-crossOK,7-RSQ,8-Slope,9-Offset
			crossParas = calcCrossLineParas(curLine, wrinkleLinePts, maxX, minX, chkDownSide, Math.abs(startY-stopY)*0.6);
			coef2 = new double[]{crossParas.get(8),crossParas.get(9),crossParas.get(7)};
			crossX = crossParas.get(1);
			lineRsq = crossParas.get(7);
			lineContinuity = (wrinkleLinePts.size())/(maxX-minX+1);
			sqr_1 = (maxX*coef[0]+coef[1]) - (minX*coef[0]+coef[1]); sqr_1 = sqr_1*sqr_1;
			sqr_2 = (startX*tmpSlope+tmpOffset) - (stopX*tmpSlope+tmpOffset); sqr_2 = sqr_2*sqr_2;
			lineLengthR = Math.sqrt((maxX-minX)*(maxX-minX)+sqr_1)/Math.sqrt((startX-stopX)*(startX-stopX)+sqr_2);
			crossXR = (double)(curLine.getXAxisStart()-crossX)/(curLine.getXAxisStart()-curLine.getXAxisStop());
			pointsR = (double)pts.size()/scanAreaDataPts;
			
			currScore = scoreDistance*(1.0-distanceR);
			currScore += (pointsR<minLineLengthR?0:scorePts*pointsR);
			currScore += (lineLengthR<minLineLengthR || lineLengthR<minLineLengthR*2 && pointsR<minLineLengthR?0:scoreLen*lineLengthR);
			currScore += (lineContinuity<minLineContinuity && (lineLengthR<0.5 || pointsR<0.5)?0:scoreContinuity*lineContinuity);
			currScore += (lineRsq<minRSQ || lineLengthR<minLineLengthR || lineLengthR<minLineLengthR*2 && pointsR<minLineLengthR?0:scoreRsq*lineRsq);
			currScore += (1-Math.abs(crossXR)<0?0:scoreCrossX*(1-Math.abs(crossXR)));
			
			wrinkleScores.add(i, new double[]{currScore,(double)pts.size()/scanAreaDataPts,coef[2],(double)oriDistance/scanAreaLength,maxY-minY,
											(double)i,crossParas.get(0),wrinkleLinePts.size(),crossParas.get(7)});
			
			tmpAngle = crossParas.get(0); lineSmoothParas = null;
			if(crossParas.get(6)<=0){
				invalidFlag = 4;
			}else{
				if(tmpAngle>0){//Double check whether the line is valid or not
					if(tmpAngle<minValidAngle) invalidFlag = 2; //Skip too small bending angle
					if(currScore<minScore) invalidFlag = 3; //Score is too low
					if(invalidFlag>0) tmpAngle = -1.0;
				}
				
				if(tmpAngle>0){//Check whether bending line is a real line or not
					tmpX = curLine.getXAxisStart();
					if(null!=sideLine && tmpX<sideLine.getXAxisStart()) tmpX = sideLine.getXAxisStart();
					if(crossX>0 && crossX<imgWidth) tmpX = (tmpX>crossX?crossX:tmpX);
					cmpOffset = tmpX*tmpSlope+tmpOffset;
					if(cmpOffset<(minX*tmpSlope+tmpOffset)) cmpOffset = minX*tmpSlope+tmpOffset;
					
					grayDelta = getGrayDelta(coef,new double[]{coef[0],coef[1]+(chkDownSide?3:-3)},imgGrays,(int)tmpX,(int)minX,1,0,grayThresholds);
					if(grayDelta[0]>0.5 && grayDelta[0]<0.75 && grayDelta[2]-grayDelta[0]<0.3){//Double confirm
						grayDelta = getGrayDelta(coef,new double[]{coef[0],coef[1]+(chkDownSide?4:-4)},imgGrays,(int)tmpX,(int)minX,1,0,grayThresholds);
					}
					if(grayDelta[0]<0.75 && grayDelta[2]-grayDelta[0]<0.25 || !(grayDelta[0]>0.5 || grayDelta[1]>0.8 || grayDelta[2]>0.99)){
						grayDelta = getGrayDelta(coef,new double[]{coef[0],coef[1]+(chkDownSide?-3:3)},imgGrays,(int)tmpX,(int)minX,1,1,grayThresholds);
						if(grayDelta[0]<0.75 && grayDelta[2]-grayDelta[0]<0.25 || !(grayDelta[0]>0.5 || grayDelta[1]>0.8 || grayDelta[2]>0.99)){
							grayDelta = getGrayDelta(coef,new double[]{tmpSlope,cmpOffset+(chkDownSide?3.0:-3)},imgGrays,(int)tmpX,(int)minX,1,0,grayThresholds);//Compare wrinkle line and original line
							if(grayDelta[0]<0.75 && grayDelta[2]-grayDelta[0]<0.25 || !(grayDelta[0]>0.5 || grayDelta[1]>0.8 || grayDelta[2]>0.99)) invalidFlag = 1;
						}
					}
					
					if(invalidFlag>0){
						tmpAngle = -1.0;
					}else{
						lineSmoothParas = getLineSmoothParas(coef2, imgGrays, (int)minX, (int)maxX, 1, 0.8);
						if(lineSmoothParas[0]<0.05 || lineSmoothParas[1]<0.15 //More than 80% grays of the line are quit closed
						|| (lineLengthR+pointsR)/2<0.5 && lineSmoothParas[2]<0.5 //More than 50% grays of the line are closed and length is shorter
						|| lineLengthR<0.5 && pointsR>0.95 && lineSmoothParas[2]<0.5 //More than 50% grays of the line are closed and length is shorter
						|| (lineLengthR+pointsR)/2<0.5 && lineSmoothParas[0]>0.5 //Grays of the line are in big range and length is shorter
						|| (lineLengthR<0.3 || pointsR<0.3) && lineSmoothParas[0]>0.6 //Grays of the line are in big range and length is shorter
						|| (lineContinuity+lineLengthR+pointsR)/3<0.55 && lineSmoothParas[0]>0.6 //Grays of the line are in big range and length is shorter
						|| (lineContinuity<0.5 || lineLengthR<0.5) && pointsR>0.95 && (lineSmoothParas[0]+lineSmoothParas[1])>1.15 //Grays of the line are in big range and length is shorter
						|| (lineContinuity+lineLengthR+pointsR)/3<0.5 && (lineSmoothParas[0]+lineSmoothParas[1])>1.05 //Grays of the line are in big range and length is shorter
						|| (lineContinuity+lineLengthR+pointsR)/3<0.5 && (lineSmoothParas[0]+lineSmoothParas[2])<1.05 //Grays of the line are closed and length is shorter
						|| ((lineLengthR+pointsR)/2<0.35 || lineLengthR<0.22 || pointsR<0.22) && (lineSmoothParas[0]+lineSmoothParas[2])<0.95 //Grays of the line are closed and length is shorter
						|| ((lineContinuity+lineLengthR)/2<0.5 || (lineContinuity+pointsR)/2<0.5) && (lineSmoothParas[0]+lineSmoothParas[2])<0.85){
							invalidFlag = 5; tmpAngle = -1.0;
						}else if((lineLengthR+pointsR)/2<0.3 || (lineContinuity+lineLengthR+pointsR)/3<0.45){
							grayDelta = getGrayDelta(coef,new double[]{tmpSlope,cmpOffset},imgGrays,(int)tmpX,(int)minX,1,1,grayThresholds);
							if(grayDelta[5]>0.5 || grayDelta[4]>0.8 || grayDelta[3]>0.99){
								invalidFlag = 6; tmpAngle = -1.0;
							}
						}
					}
				}
				
				if(tmpAngle>0){
					wrinkleLinesQty++;
					if(!chkMaxCrossX && tmpAngle>maxPoleAngle) chkMaxCrossX = true;
					if(selectLineIdx<0){
						selectLineIdx = i;
						selAngle = tmpAngle;
						if(chkMaxCrossX) selCrossX = crossX;
					}else{
						if(selAngle<tmpAngle || chkMaxCrossX && tmpAngle>maxPoleAngle){
							if(chkMaxCrossX){
								if(selCrossX<=0){
									selCrossX = crossX;
									selectLineIdx = i;
								}else if(selCrossX<crossX){
									selCrossX = crossX;
									selectLineIdx = i;
								}
							}else{
								selAngle = tmpAngle;
								selectLineIdx = i;
							}
						}
					}
				}
			}
			
			System.out.println(tmpOffset+"("+(chkDownSide?"DN":"UP")+"_"+i+")Wrinkle Paras:"
					+"FLAG="+invalidFlag+(null!=lineSmoothParas?"("+lineSmoothParas[0]+"/"+lineSmoothParas[1]+"/"+lineSmoothParas[2]+")":"")+","
					+"AG="+crossParas.get(0)+","
					+"CS="+currScore+","
					+"LC="+lineContinuity+","
					+"LR="+lineRsq+","
					+"LL="+lineLengthR+","
					+"PR="+pointsR+","
					+"DR="+distanceR+","
					+"XD="+crossParas.get(2)+","
					+"YD="+crossParas.get(3)+","
					+"CXR="+crossXR+","
					+"X1="+curLine.getXAxisStart()+","
					+"X2="+curLine.getXAxisStop()+","
					+"X3="+maxX+","
					+"X4="+minX);
		}
		
		if(selectLineIdx>=0){
			selLineParas = (double[]) wrinkleScores.get(selectLineIdx);
			curLine.setWrinkleLinesQty(!chkDownSide, wrinkleLinesQty);
		}else{
			selLineParas = new double[]{0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0};
		}
		
		return selLineParas;
	}
	
	private double[] getWrinkleLineFittingParas(ArrayList<String> pts, int fittingPts){
		double[] fittingParas = new double[]{0.0, 0.0, 0.0};
		ArrayList<Double> xVals = new ArrayList<Double>();
		ArrayList<Double> yVals = new ArrayList<Double>();
		LinkedHashMap<Double,Double> fitData = new LinkedHashMap<Double,Double>();
		LinkedHashMap<String,String> fitSort = new LinkedHashMap<String,String>();
		int counter = 0, maxIdx = pts.size()-1;
		String[] pt = null;
		
		if(pts.size()>1){
			for(int j=maxIdx; j>=0; j--){
				pt = pts.get(j).split(",");
				if(null==fitSort.get(pt[1])){
					fitData.put(Double.parseDouble(pt[0]), Double.parseDouble(pt[1]));
					fitSort.put(pt[1], pt[0]);
				}
				xVals.add(Double.parseDouble(pt[0]));
				yVals.add(Double.parseDouble(pt[1]));
				counter++;
				if(counter>=fittingPts) break;
			}
			if(fitData.size()>2){
				fittingParas = MathUtils.lineFitting(fitData);
			}else{
				fittingParas = MathUtils.lineFitting(xVals, yVals, 0, counter-1);
			}
		}
		
		return fittingParas;
	}
	
	private ArrayList<ArrayList<String>> addDataPtIntoWrinkleLine(ArrayList<ArrayList<String>> wrinkleLines, int x, ArrayList<Integer> selYVals){
		ArrayList<Integer> newYVals = null;
		ArrayList<String> pts = new ArrayList<String>();
		String[] pt = null;
		int xDelta = 0, yDelta = 0, selYDelta = 0, lastXDelta = 0, yExpected = 0;
		int sumDelta = 0, minSumDelta = 1000, selIdx = -1, y = 0, selLineIdx = -1, lastSelLineIdx = -1;
		double[] coef = null;
		
		if(wrinkleLines.isEmpty()){
			pts.add(x+","+selYVals.get(selYVals.size()-1));
			wrinkleLines.add(pts);
			selYVals.remove(selYVals.size()-1);
		}
		
		if(selYVals.size()>0){
			for(int i=0; i<wrinkleLines.size(); i++){
				if(lastSelLineIdx==i) continue;
				pts = wrinkleLines.get(i);
				coef = getWrinkleLineFittingParas(pts,10);
				pt = pts.get(pts.size()-1).split(",");//The last added point
				
				xDelta = Integer.parseInt(pt[0]) - x;
				if(xDelta>lineScanRange) continue;//last x is too far from current x
				
				selIdx = -1; minSumDelta = 1000;
				if(coef[2]>0) yExpected = (int)(coef[0]*x + coef[1] + 0.5);
				for(int j=0; j<selYVals.size(); j++){
					y = selYVals.get(j);
					if(coef[2]<=0) yExpected = Integer.parseInt(pt[1]);
					
					yDelta = Math.abs(yExpected - y);
					if(yDelta<=lineScanRange/2){//last y is closed to current y
						sumDelta = xDelta + yDelta;
						if(minSumDelta>sumDelta){
							minSumDelta = sumDelta;
							selIdx = j;
						}
					}else{
						lastXDelta = xDelta;
						for(int k=(pts.size()-1); k>=0; k--){//search all points
							pt = pts.get(k).split(",");
							xDelta = Integer.parseInt(pt[0]) - x;
							if(xDelta>lineScanRange) break;
							yDelta = Math.abs(y - Integer.parseInt(pt[1]));
							if(yDelta<=lineScanRange/2){
								sumDelta = xDelta + yDelta;
								if(minSumDelta>sumDelta){
									minSumDelta = sumDelta;
									selIdx = j;
									selYDelta = yDelta;
								}
								break;
							}else if(xDelta!=lastXDelta){
								break;
							}
						}
					}
				}
				
				if(selIdx>=0){
					newYVals = new ArrayList<Integer>();
					selLineIdx = i;
					
					//Check the rest lines
					if(i+1<wrinkleLines.size()){
						for(int j=i+1; j<wrinkleLines.size(); j++){
							pts = wrinkleLines.get(j);
							coef = getWrinkleLineFittingParas(pts,10);
							pt = pts.get(pts.size()-1).split(",");//The last added point
							
							xDelta = Integer.parseInt(pt[0]) - x;
							if(xDelta>lineScanRange) continue;//last x is too far from current x
							
							if(coef[2]>0){
								yExpected = (int)(coef[0]*x + coef[1] + 0.5);
							}else{
								yExpected = Integer.parseInt(pt[1]);
							}
							if(selYDelta >= Math.abs(yExpected - selYVals.get(selIdx))){
								selYDelta = Math.abs(yExpected - selYVals.get(selIdx));
								selLineIdx = j;
							}
						}
					}
					
					lastSelLineIdx = selLineIdx;
					pts = wrinkleLines.get(selLineIdx);
					pts.add(x+","+selYVals.get(selIdx));
					wrinkleLines.remove(selLineIdx);
					wrinkleLines.add(selLineIdx, pts);
					for(int k=0; k<selYVals.size(); k++){
						if(selIdx==k) continue;
						newYVals.add(selYVals.get(k));
					}
					selYVals.clear();
					selYVals = newYVals;
					if(selYVals.isEmpty()){
						break;
					}else{
						i = -1;
					}
				}
			}
			
			if(selYVals.size()>0){
				for(int k=0; k<selYVals.size(); k++){
					pts = new ArrayList<String>();
					pts.add(x+","+selYVals.get(k));
					wrinkleLines.add(pts);
				}
			}
		}
		
		return wrinkleLines;
	}
	
	private LinkedHashMap<Double,Double> getWrinklePtsEx(ImgExtractLine curLine, ImgExtractLine sideLine, int[][] imgGrays, int[][] peakVals, int wrinkleStartChkPos, int wrinkleStopChkPos, int maxChkOffset, boolean chkDownSide){
		LinkedHashMap<Double,Double> wrinklePts = new LinkedHashMap<Double,Double>();
		double tmpSlope = curLine.getLineSlope(), grayDeltaFactor = 2.0, peakGrayThr = 0.0, areaCoverRate = 0.0;
		double tmpOffset = curLine.getLineIntercept()+curLine.getLineInterceptDelta(), singleXMeetsMaxRatio = 0.5;
		int y = 0, nearCol = 0, imgHeight = peakVals[0].length, refGray = 0, validGrayDelta = 3;
		int breakPts = 0, maxBreakPts = 5, singleXMeetsCnt = 0, minX = wrinkleStartChkPos;
		ArrayList<Integer> selYVals = new ArrayList<Integer>();
		
		//Get 1st reference gray
		refGray = getWrinkleRefGray(curLine,imgGrays,peakVals,wrinkleStartChkPos,wrinkleStopChkPos,maxChkOffset,chkDownSide);
		if(0==refGray) return wrinklePts;
		
		boolean thereIsPeak = false, bChkPeakDataOnly = true;
		String[] pt = null;
		int currGray = 0, selG = 0, minGray = 0, chgRefGrayT = 0, refGrayMaxChgT = 2, peakG = 0;
		int minY = imgHeight, maxY = 0, minPeakG = 0;
		int selMinX = -1, selMaxX = -1, selMinY = -1, selMaxY = -1;
		ArrayList<ArrayList<String>> wrinkleLines = new ArrayList<ArrayList<String>>();
		ArrayList<String> pts = new ArrayList<String>();
		ArrayList<Object> peakParas = new ArrayList<Object>();
		String rawTtl = "lineOffset,startX,stopX,xy", tmpRaw1 = "", tmpRaw2 = "", tmpRaw3 = "", tmpRaw4 = "";
		String missedDt1 = "", missedDt2 = "", missedDt3 = "", missedDt4 = "";
		LinkedHashMap<Integer,String> rawGrays = new LinkedHashMap<Integer,String>();
		LinkedHashMap<Integer,String> rawSelGs = new LinkedHashMap<Integer,String>();
		LinkedHashMap<Integer,String> rawRefGs = new LinkedHashMap<Integer,String>();
		LinkedHashMap<Integer,String> rawPeaks = new LinkedHashMap<Integer,String>();
		
		peakGrayThr = noiseGrayThr;//255*0.9=229.5
		for(int x=wrinkleStartChkPos; x>wrinkleStopChkPos; x--){
			nearCol = (int)(x*tmpSlope+tmpOffset);
			if(nearCol<0 || nearCol>=imgHeight) continue;
			if(wrinkleStartChkPos-x<lineScanRange || peakGrayThr<refGray) peakGrayThr = getPeakGrayThreshold(refGray);
			
			rawTtl += "," + x;
			peakParas = thereIsPeakAround(imgGrays,peakVals,refGray,validGrayDelta,x,
					(chkDownSide?nearCol:nearCol-(int)maxChkOffset),
					(chkDownSide?nearCol+(int)maxChkOffset:nearCol),grayDeltaFactor*1.5,peakGrayThr);
			thereIsPeak = (boolean) peakParas.get(0);
			minPeakG = (int) peakParas.get(1);
			minGray = imgGrays[x][nearCol]; singleXMeetsCnt = 0;
			
			selYVals.clear();
			for(int n=0; n<=(int)maxChkOffset; n++){
				currGray = -1; selG = -1; peakG = -1;
				if(chkDownSide){
					y = nearCol + n;
				}else{
					y = nearCol - n;
				}
				if(minY>y) minY = y;
				if(maxY<y) maxY = y;
				if(y<0 || y>=imgHeight) continue;
				
				currGray = imgGrays[x][y];
				if(peakVals[x][y]>0 && peakVals[x][y]<peakGrayThr) peakG = currGray;//Raw data for debugging
				if(minGray>imgGrays[x][y]) minGray = imgGrays[x][y];
				if(68==x && 249==y){
					System.out.print("y="+y);
				}
				if(thereIsPeak){
					if(!bChkPeakDataOnly && (Math.abs(refGray-imgGrays[x][y])<=validGrayDelta || refGray>imgGrays[x][y])
					|| bChkPeakDataOnly && peakVals[x][y]>0 && peakVals[x][y]<peakGrayThr){
						singleXMeetsCnt++;
						selG = currGray;
						if(selMinX<0){
							selMinX = x; selMaxX = x; selMinY = y; selMaxY = y;
						}else{
							if(selMinX>x) selMinX = x;
							if(selMaxX<x) selMaxX = x;
							if(selMinY>y) selMinY = y;
							if(selMaxY<y) selMaxY = y;
						}
						if(minX>x) minX = x;
						selYVals.add(y);
					}
				}
				
				if(logEnabled){
					missedDt1 = ""; missedDt2 = "";; missedDt3 = ""; missedDt4 = "";
					if(null==rawGrays.get(y)){
						if(x<wrinkleStartChkPos){
							for(int k=wrinkleStartChkPos-1; k>=x; k--){
								missedDt1 += "," + imgGrays[x][y];
								missedDt2 += ",-1";
								missedDt3 += ",-1";
								missedDt4 += ",-1";
							}
						}
						tmpRaw1 = tmpOffset+","+wrinkleStartChkPos+","+wrinkleStopChkPos+","+y+missedDt1+","+currGray;
						tmpRaw2 = tmpOffset+","+wrinkleStartChkPos+","+wrinkleStopChkPos+","+y+missedDt2+","+selG;
						tmpRaw3 = tmpOffset+","+wrinkleStartChkPos+","+wrinkleStopChkPos+","+y+missedDt3+","+refGray;
						tmpRaw4 = tmpOffset+","+wrinkleStartChkPos+","+wrinkleStopChkPos+","+y+missedDt4+","+peakG;
					}else{
						tmpRaw1 = rawGrays.get(y)+","+currGray;
						tmpRaw2 = rawSelGs.get(y)+","+selG;
						tmpRaw3 = rawRefGs.get(y)+","+refGray;
						tmpRaw4 = rawPeaks.get(y)+","+peakG;
					}
					
					rawGrays.put(y, tmpRaw1);
					rawSelGs.put(y, tmpRaw2);
					rawRefGs.put(y, tmpRaw3);
					rawPeaks.put(y, tmpRaw4);
				}
			}
			
			if(83==x && chkDownSide && Math.abs(tmpOffset-92.035)<0.01){
				System.out.print("");
			}
			if(selYVals.size()>0) wrinkleLines = addDataPtIntoWrinkleLine(wrinkleLines,x,selYVals);
			
			if((double)singleXMeetsCnt/(maxChkOffset+1)>singleXMeetsMaxRatio){
				if(bChkPeakDataOnly){
					break;
				}else if(wrinkleStartChkPos-x<lineScanRange){
					bChkPeakDataOnly = true;
					rawTtl = "lineOffset,startX,stopX,xy";
					tmpRaw1 = ""; tmpRaw2 = ""; tmpRaw3 = ""; tmpRaw4 = "";
					rawGrays.clear(); rawSelGs.clear(); rawRefGs.clear(); rawPeaks.clear();
					wrinkleLines.clear();
					x = wrinkleStartChkPos + 1;
					continue;
				}
			}
			
			if(thereIsPeak && minGray-refGray>validGrayDelta){
				chgRefGrayT++;
				if(chgRefGrayT>refGrayMaxChgT) chgRefGrayT = refGrayMaxChgT + 1;
			}
			
			if(Math.abs(refGray-minGray)<=validGrayDelta || refGray>minGray || thereIsPeak && chgRefGrayT<=refGrayMaxChgT){
				//Reference gray normal change
				if(refGray<=minGray){
					refGray = minGray;
				}else if(thereIsPeak && refGray<=minPeakG){
					refGray = minPeakG;
				}
			}else if(!thereIsPeak){
				//Reference gray special change
				peakParas = thereIsPeakAround(imgGrays,peakVals,refGray,validGrayDelta,x,
						(chkDownSide?nearCol:nearCol-(int)maxChkOffset),
						(chkDownSide?nearCol+(int)maxChkOffset:nearCol),grayDeltaFactor*2,peakGrayThr);
				thereIsPeak = (boolean) peakParas.get(0);
				minPeakG = (int) peakParas.get(1);
				if(thereIsPeak && chgRefGrayT<=refGrayMaxChgT){//Change limited by refGrayMaxChgT
					if(refGray<=minPeakG){
						chgRefGrayT++;
						refGray = minPeakG;
					}
				}else if(!thereIsPeak){
					breakPts++;
					if(breakPts<maxBreakPts){//Change limited by maxBreakPts
						if(refGray<=minGray) refGray = minGray;
					}else{
						breakPts = maxBreakPts;
					}
				}
			}
		}
		
		if(chkDownSide && Math.abs(tmpOffset-71.96)<0.01){
			System.out.print("");
		}
		double[] selLineParas = null;
		areaCoverRate = (Math.abs((double)(selMaxX-selMinX)/(wrinkleStartChkPos-wrinkleStopChkPos))+Math.abs((double)(selMaxY-selMinY)/(maxY-minY)))/2;
		if(wrinkleLines.size()>1) wrinkleLines = screenWrinkleLines(wrinkleLines);
		if(wrinkleLines.size()>1) wrinkleLines = mergeWrinkleLines(wrinkleLines);
		if(wrinkleLines.size()>0){
			selLineParas = selectWrinkleLine(curLine,sideLine,wrinkleLines,imgGrays,wrinkleStartChkPos,wrinkleStopChkPos,(chkDownSide?minY:maxY),(chkDownSide?maxY:minY),areaCoverRate,chkDownSide);
			if(selLineParas[0]>0){//There is wrinkle line
				//Decode data
				pts = wrinkleLines.get((int)selLineParas[5]);
				for(int j=0; j<pts.size(); j++){
					pt = pts.get(j).split(",");
					wrinklePts.put(Double.parseDouble(pt[0]), Double.parseDouble(pt[1]));//Keep the most outer or inner points only in the same column
				}
				curLine.setWrinkleScores(chkDownSide?-1:selLineParas[0], chkDownSide?selLineParas[0]:-1);
				if(logEnabled){
					logWrinkleLines("wrinkleFnlRaw_",wrinkleLines,tmpOffset,wrinkleStartChkPos,wrinkleStopChkPos,minY,maxY,chkDownSide);
				}
			}
		}
		
		if(logEnabled){
			LogUtils.rawLog("wrinkleSchRaw_", "(refG)"+rawTtl);
			for(int i:rawRefGs.keySet()){
				LogUtils.rawLog("wrinkleSchRaw_", rawRefGs.get(i));
			}
			LogUtils.rawLog("wrinkleSchRaw_", "(rawG)"+rawTtl);
			for(int i:rawGrays.keySet()){
				LogUtils.rawLog("wrinkleSchRaw_", rawGrays.get(i));
			}
			LogUtils.rawLog("wrinkleSchRaw_", "(selG)"+rawTtl);
			for(int i:rawSelGs.keySet()){
				LogUtils.rawLog("wrinkleSchRaw_", rawSelGs.get(i));
			}
			LogUtils.rawLog("wrinkleSchRaw_", "(peak)"+rawTtl);
			for(int i:rawPeaks.keySet()){
				LogUtils.rawLog("wrinkleSchRaw_", rawPeaks.get(i));
			}
			logWrinkleLines("wrinkleSelRaw_",wrinkleLines,tmpOffset,wrinkleStartChkPos,wrinkleStopChkPos,minY,maxY,chkDownSide);
		}
		
		return wrinklePts;
	}
	
	private void logWrinkleLines(String fnPrefix, ArrayList<ArrayList<String>> wrinkleLines, double lineOffset, int startX, int stopX, int minY, int maxY, boolean chkDownSide){
		String rawTtl = (chkDownSide?"(DN)":"(UP)")+"lineOffset,startX,stopX,xy";
		String rawDt1 = lineOffset+","+startX+","+stopX+",";
		String rawDt2 = "", curPt = "";
		ArrayList<String> pts = null;
		int findIdx = -1;
		
		for(int x=startX; x>=stopX; x--){
			rawTtl += "," + x;
		}
		LogUtils.rawLog(fnPrefix, rawTtl);
		
		if(chkDownSide){
			for(int y=minY; y<=maxY; y++){
				rawDt2 = rawDt1 + y;
				for(int x=startX; x>=stopX; x--){
					curPt = x+","+y;
					findIdx = -1;
					for(int i=0; i<wrinkleLines.size(); i++){
						pts = wrinkleLines.get(i);
						for(int j=0; j<pts.size(); j++){
							if(pts.get(j).contains(curPt)){
								findIdx = i; break;
							}
						}
						if(findIdx>=0) break;
					}
					
					if(findIdx<0){
						rawDt2 += ",-1";
					}else{
						rawDt2 += "," + (findIdx+1)*50;
					}
				}
				LogUtils.rawLog(fnPrefix, rawDt2);
			}
		}else{
			for(int y=maxY; y>=minY; y--){
				rawDt2 = rawDt1 + y;
				for(int x=startX; x>=stopX; x--){
					curPt = x+","+y;
					findIdx = -1;
					for(int i=0; i<wrinkleLines.size(); i++){
						pts = wrinkleLines.get(i);
						for(int j=0; j<pts.size(); j++){
							if(pts.get(j).contains(curPt)){
								findIdx = i; break;
							}
						}
						if(findIdx>=0) break;
					}
					
					if(findIdx<0){
						rawDt2 += ",-1";
					}else{
						rawDt2 += "," + (findIdx+1)*50;
					}
				}
				LogUtils.rawLog(fnPrefix, rawDt2);
			}
		}
	}
	
	private String getResultKey(int layerIdx){
		String procRsltKey = "";
		
		if(0==layerIdx%2){
			procRsltKey = "L" + (layerIdx/2) + "_L";
		}else{
			procRsltKey = "L" + ((layerIdx+1)/2) + "_R";
		}
		
		return procRsltKey;
	}
	
	private boolean twoNearLinesOverhangFailed(int curLineIdx, ImgExtractLine curLine, ImgExtractLine nextLine, double curLineWeight, double nextLineWeight){
		boolean failed = true;
		double minDistance = 10;//pixels
		double maxDistance = 116;//pixels
		double onePixelLen = 0.012;
		double overhang = 0.0;
		int refCurrLine = 0, refNextLine = 0;
		
		onePixelLen = Double.parseDouble(""+criteria.get("onePixel"));
		minDistance = Double.parseDouble(""+criteria.get("minPoleDistance"))/onePixelLen;
		maxDistance = Double.parseDouble(""+criteria.get("maxPoleDistance"))/onePixelLen;
		
		if(null!=curLine && null!=nextLine){
			if(evenPoleIsLonger && 0==curLineIdx%2 || !evenPoleIsLonger && 1==curLineIdx%2){
				refCurrLine = (int)(curLine.getXAxisStop() + (curLine.getXAxisStart()-curLine.getXAxisStop())*(1-curLineWeight));
				refNextLine = (int)(nextLine.getXAxisStart() - (nextLine.getXAxisStart()-nextLine.getXAxisStop())*nextLineWeight);
			}else{
				refCurrLine = (int)(curLine.getXAxisStart() - (curLine.getXAxisStart()-curLine.getXAxisStop())*curLineWeight);
				refNextLine = (int)(nextLine.getXAxisStop() + (nextLine.getXAxisStart()-nextLine.getXAxisStop())*(1-nextLineWeight));
			}
			
			if(firstPoleIsLonger){
				overhang = evenPoleIsLonger?refNextLine - refCurrLine:refCurrLine - refNextLine;
			}else{
				overhang = !evenPoleIsLonger?refCurrLine - refNextLine:refNextLine - refCurrLine;
			}
			if(overhang>=minDistance && overhang<=maxDistance) failed = false;
		}
		
		return failed;
	}
	
	private LinkedHashMap<Integer, ImgExtractLine> setPolePosition(LinkedHashMap<Integer, ImgExtractLine> linesMap, boolean evenLineLonger, int[][] imgGrays, int[][] peakVals, int targetLinesQty){
		linesMap = setLongerPolePos(linesMap,evenLineLonger,imgGrays,peakVals,targetLinesQty);
		linesMap = setShorterPolePos(linesMap,evenLineLonger,imgGrays,peakVals,targetLinesQty);
		return linesMap;
	}
	
	private LinkedHashMap<Integer, ImgExtractLine> setLongerPolePos(LinkedHashMap<Integer, ImgExtractLine> linesMap, boolean evenLineLonger, int[][] imgGrays, int[][] peakVals, int targetLinesQty){
		ImgExtractLine exLine = null;
		if(null==linesMap || null!=linesMap && linesMap.isEmpty()) return linesMap;
		int polePos = 0, idx1 = -1, xStart = 0;
		boolean curLineLonger = false;
		ArrayList<Object> trendRaw = null;
		double[] roughTrend = null, grayDelta = null;
		ArrayList<Integer> curLineIdx = new ArrayList<Integer>();
		
		for(int i=0; i<linesMap.size(); i++){
			if(i>=targetLinesQty) break;
			curLineLonger = curLineIsLonger(i,evenLineLonger);
			if(!curLineLonger) continue;
			
			exLine = linesMap.get(i);
			if(null!=exLine){
				if(i>=targetLinesQty-3){
					polePos = exLine.calcPolePosition(evenLineLonger,linesMap,i,imgGrays,peakVals,targetLinesQty);
				}else{
					polePos = exLine.getXAxisStop();
				}
				
				//Special case handling
				if(0==polePos){
					idx1 = -1;
					trendRaw = getLineGrayTrendRawData(linesMap.get(i), imgGrays);
					roughTrend = (double[]) trendRaw.get(0);
					idx1 = MathUtils.getFirstMeetIndex(roughTrend, firstSharpChgAngle, 0, true, true);//Get first sharp change(searching from outer to inner side)
					xStart = exLine.getXAxisStart();
					
					if(idx1>0 && (idx1+10<xStart || 0==xStart)){
						curLineIdx.clear();
						curLineIdx.add(idx1);
						curLineIdx.add(0);
						
						for(int j=0; j<curLineIdx.size()-1; j++){
							grayDelta = getGrayDelta(exLine,linesMap.get(i-1),imgGrays,curLineIdx.get(j),curLineIdx.get(j+1),true,null);
							if(grayDelta[0]>0.5 || grayDelta[1]>0.8 || grayDelta[2]>0.99){
								idx1 = curLineIdx.get(j+1);
							}else{
								break;
							}
						}
						
						polePos = idx1;
						if(0==xStart) exLine.setXAxisStart(idx1);
					}
				}
				
				exLine.setPolePosition(polePos);
			}
		}
		return linesMap;
	}
	
	@SuppressWarnings("unchecked")
	private LinkedHashMap<Integer, ImgExtractLine> setShorterPolePos(LinkedHashMap<Integer, ImgExtractLine> linesMap, boolean evenLineLonger, int[][] imgGrays, int[][] peakVals, int targetLinesQty){
		ImgExtractLine currLine = null, tempLine = null;
		if(null==linesMap || null!=linesMap && linesMap.isEmpty()) return linesMap;
		double[] roughTrend = null, detailsTrend = null, grayDelta = null, grayThreshold = new double[]{0.5,0.8,0.99,0.99,0.8,0.5};
		int polePos = 0, idx1 = 0, idx2 = 0, idx3 = 0, firstIdx = 0, iCfmCnt = 0, iCounter = 0;
		boolean curLineLonger = false;
		double lineSlope = 0.0, lineOffset = 0.0, innerOffset = 0.0, outerOffset = 0.0, tmpLastOffs = 0.0, ratio = 0.0, tmpDelta = 0.0, initDelta = 0.0;
		
		ArrayList<Integer> sharpChgIdx = null;
		ArrayList<Object> trendRaw = null, curLineSharpChgs = null;
		LinkedHashMap<Integer,ArrayList<Object>> linesSharpChgs = new LinkedHashMap<Integer,ArrayList<Object>>();
		for(int i=0; i<linesMap.size(); i++){
			if(i>=targetLinesQty) break;
			curLineLonger = curLineIsLonger(i,evenLineLonger);
			if(curLineLonger) continue;
			if(4==i){
				System.out.print("");
			}
			currLine = linesMap.get(i);
			if(null!=currLine){
				idx1 = -1; idx2 = -1; idx3 = -1; curLineSharpChgs = null; tmpLastOffs = 0.0;
				trendRaw = getLineGrayTrendRawData(linesMap.get(i), imgGrays);
				roughTrend = (double[]) trendRaw.get(0);
				detailsTrend = (double[]) trendRaw.get(1);
				lineSlope = currLine.getLineSlope();
				lineOffset = currLine.getLineIntercept() + currLine.getLineInterceptDelta();
				
				//Get the first sharp change position from inner side
				firstIdx = MathUtils.getFirstMeetIndex(roughTrend, firstSharpChgAngle, roughTrend.length-1, true, false);
				if(firstIdx>0) idx1 = getMaxPeakIndex(roughTrend, firstIdx, 10, 1, true, false, false);
				if(idx1>0){
					curLineSharpChgs = getLineSharpChgPos(detailsTrend, roughTrend[idx1], (idx1 + firstIdx)/2, 10, 0.6, true, false);
					linesSharpChgs.put(i, curLineSharpChgs);
					
					sharpChgIdx = null;
					if(null!=curLineSharpChgs && curLineSharpChgs.size()>0) sharpChgIdx = (ArrayList<Integer>) curLineSharpChgs.get(0);
					
					if(null!=sharpChgIdx && sharpChgIdx.size()>0){
						idx2 = sharpChgIdx.get(0);
						if(sharpChgIdx.size()>1){
							innerOffset = lineOffset + 3.0; outerOffset = lineOffset - 3.0;
							if(i+1<linesMap.size() && null!=linesMap.get(i+1)){
								tempLine = linesMap.get(i+1);
								innerOffset = tempLine.getLineIntercept() + tempLine.getLineInterceptDelta();
								if((innerOffset-lineOffset)>lineScanRange*2){
									innerOffset = lineOffset + lineScanRange;
								}else{
									innerOffset = (lineOffset + tempLine.getLineIntercept() + tempLine.getLineInterceptDelta())/2;
								}
							}
							if(i>0 && null!=linesMap.get(i-1)){
								tempLine = linesMap.get(i-1);
								outerOffset = tempLine.getLineIntercept() + tempLine.getLineInterceptDelta();
								if((lineOffset-outerOffset)>lineScanRange*2){
									outerOffset = outerOffset + lineScanRange;
								}else{
									outerOffset = (lineOffset + tempLine.getLineIntercept() + tempLine.getLineInterceptDelta())/2;
								}
							}
							
							for(int j=0; j<sharpChgIdx.size()-1; j++){
								grayDelta = getGrayDelta(new double[]{lineSlope,tmpLastOffs>0.05?tmpLastOffs:lineOffset},new double[]{lineSlope,outerOffset},imgGrays,sharpChgIdx.get(j),sharpChgIdx.get(j+1),1,0,grayThreshold);
								iCfmCnt = 0;
								if(tmpLastOffs>0.05 || grayDelta[0]>0.5 || grayDelta[1]>0.8 || grayDelta[2]>0.99){
									//Double checking
									iCfmCnt = 0; tmpLastOffs = 0.0; iCounter = 0; initDelta = grayDelta[0];
									for(double offs=innerOffset; offs>outerOffset; offs--){
										iCounter++;
										grayDelta = getGrayDelta(new double[]{lineSlope,offs},new double[]{lineSlope,outerOffset},imgGrays,sharpChgIdx.get(j),sharpChgIdx.get(j+1),0,0,grayThreshold);
										if(grayDelta[0]>0.5 || grayDelta[1]>0.8 || grayDelta[2]>0.99){
											if(tmpLastOffs<0.05 || (offs-tmpLastOffs)<1.05){
												iCfmCnt++; tmpLastOffs = offs;
											}else if(iCfmCnt<3){
												iCfmCnt = 0;
											}else if(iCfmCnt>=3){
												iCfmCnt++;
											}
										}
									}
									if(iCfmCnt<3) break;
									ratio = (double)iCfmCnt/iCounter;
									tmpDelta = calcOnLineGrayDelta(currLine,imgGrays,sharpChgIdx.get(j),sharpChgIdx.get(j)-sharpChgIdx.get(j+1),false);
									if(ratio<0.51 && tmpDelta>10 || tmpDelta>25){
										if(ratio>=0.51 || ratio>0.49 && initDelta>0.9){
											tmpDelta = calcOnLineGrayDelta(currLine,imgGrays,sharpChgIdx.get(j+1),sharpChgIdx.get(j)-sharpChgIdx.get(j+1),false);
											if(tmpDelta<5) break;
										}else{
											break;
										}
									}
									idx2 = sharpChgIdx.get(j+1);
								}else{
									break;
								}
							}
						}
					}
				}
				
				//Get the second sharp change position at outer side
				if(idx2>0) idx3 = MathUtils.getFirstPeakIndex(detailsTrend, idx2, 10, false, false);
				
				polePos = currLine.getXAxisStart();
				if(idx2>0 && idx3>0 && (polePos<idx3 || polePos>idx2)){
					polePos = (idx2+idx3)/2;
					currLine.setXAxisStart(polePos);
				}
				currLine.setPolePosition(polePos);
			}
		}
		return linesMap;
	}
	
	/**
	 * 
	 * @param currLine The line for gray delta calculation
	 * @param imgGrays The gray matrix of the picture
	 * @param cutIdx The separated index for gray delta calculation
	 * @param avgPoints The data points for gray averaging at each side of cutIdx
	 * @param subtractOuter True means inner average gray subtracts outer average gray(False means outer-inner)
	 * @return Delta between outer and inner average gray
	 */
	private double calcOnLineGrayDelta(ImgExtractLine currLine, int[][] imgGrays, int cutIdx, int avgPoints, boolean subtractOuter){
		double grayDelta = 0.0, forwardSum = 0.0, backwardSum = 0.0;
		int imgWidth = imgGrays.length, imgHeight = imgGrays[0].length, col = 0;
		int minIdx = cutIdx - avgPoints, maxIdx = cutIdx + avgPoints;
		int forwardPts = 0, backwardPts = 0;
		double slope = currLine.getLineSlope(), offset = currLine.getLineIntercept()+currLine.getLineInterceptDelta();
		
		if(minIdx<0) minIdx = 0;
		if(maxIdx>=imgWidth) maxIdx = imgWidth - 1;
		
		for(int i=cutIdx; i>=minIdx; i--){
			col = (int)(i*slope + offset);
			if(col<0 || col>=imgHeight) continue;
			backwardPts++;
			backwardSum += imgGrays[i][col];
		}
		
		if(backwardPts>0){
			for(int i=cutIdx; i<=maxIdx; i++){
				col = (int)(i*slope + offset);
				if(col<0 || col>=imgHeight) continue;
				forwardPts++;
				forwardSum += imgGrays[i][col];
			}
			
			if(forwardPts>0){
				if(subtractOuter){
					grayDelta = forwardSum/forwardPts - backwardSum/backwardPts;
				}else{
					grayDelta = backwardSum/backwardPts - forwardSum/forwardPts;
				}
			}
		}
		
		return grayDelta;
	}
	
	/**
	 * 
	 * @return double[5] 0:Delta<=-3 | 1:Delta<=-2 | 2:Delta<=-1 | 3:Delta>=1 | 4:Delta>=2 | 5:Delta>=3
	 */
	private double[] getGrayDelta(ImgExtractLine currLine, ImgExtractLine lastLine, int[][] imgGrays, int xStart, int xStop, boolean chkMidLine, double[] firstHalfMin){
		int imgHeight = imgGrays[0].length-1;
		double curLineMinY = 0.0, lastLineMaxY = 0.0, tmpY = 0.0;
		double[] grayDelta = new double[]{-1.0,-1.0,-1.0,-1.0,-1.0,-1.0};
		double curSlope = 0.0, curOffset = 0.0, lastSlope = 0.0, lastOffset = 0.0;
		
		if(null!=currLine && null!=lastLine){
			curSlope = currLine.getLineSlope();
			curOffset = currLine.getLineIntercept() + currLine.getLineInterceptDelta();
			lastSlope = lastLine.getLineSlope();
			lastOffset = lastLine.getLineIntercept() + lastLine.getLineInterceptDelta();
			
			if(chkMidLine){
				curLineMinY = xStart*curSlope+curOffset;
				tmpY = xStop*curSlope+curOffset;
				curLineMinY = (curLineMinY>tmpY?tmpY:curLineMinY);
				
				lastLineMaxY = xStart*lastSlope+lastOffset;
				tmpY = xStop*lastSlope+lastOffset;
				lastLineMaxY = (lastLineMaxY<tmpY?tmpY:lastLineMaxY);
				
				tmpY = (curLineMinY+lastLineMaxY)/2;
				if(tmpY>0 && tmpY<imgHeight){
					lastOffset = tmpY;
				}else{
					lastOffset = (curOffset+lastOffset)/2;
				}
			}
			
			grayDelta = getGrayDelta(new double[]{curSlope,curOffset},new double[]{lastSlope,lastOffset},imgGrays,xStart,xStop,1,chkMidLine?0:1,firstHalfMin);
		}
		
		return grayDelta;
	}
	
	private double[] calcGrayDeltaScore(double[] grayDelta, double[] grayDeltaThreshold){
		double[] score = new double[]{0.0,0.0};
		double[] scoreWeight = new double[]{50,30,20};
		double negScore = 0.0, posScore = 0.0;
		
		if(null!=grayDelta && null!=grayDeltaThreshold && grayDelta.length>=6 && grayDeltaThreshold.length>=6){
			//Negative delta score
			for(int i=0; i<scoreWeight.length; i++){
				negScore += scoreWeight[i] * grayDelta[i]/grayDeltaThreshold[i];
			}
			
			//Positive delta score
			for(int i=0; i<scoreWeight.length; i++){
				posScore += scoreWeight[i] * grayDelta[i+3]/grayDeltaThreshold[i+3];
			}
			
			score[0] = negScore;
			score[1] = posScore;
		}
		
		return score;
	}
	
	/**
	 * @param coefLine
	 * @param imgGrays
	 * @param xMin
	 * @param xMax
	 * @param grayScanRng
	 * @param refRate
	 * @return double[] 0=Offset>3 | 1=Offset>2 | 2=Offset>1
	 */
	private double[] getLineSmoothParas(double[] coefLine, int[][] imgGrays, int xMin, int xMax, int grayScanRng, double refRate){
		int imgWidth = imgGrays.length, imgHeight = imgGrays[0].length-1, iTemp = 0, refIndex = 0, gray = 0;
		int[] tmpGrays = null, counter = new int[]{0,0,0};
		double refGray = 0.0;
		double[] rtnParas = new double[]{0.0,0.0,0.0};
		ArrayList<Integer> grays = new ArrayList<Integer>();
		
		for(int i=xMin; i<=xMax; i++){
			iTemp = (int)(coefLine[0]*i+coefLine[1]);
			if(iTemp<0 || iTemp>=imgHeight) continue;
			if(i<0 || i>=imgWidth) continue;
			
			gray = imgGrays[i][iTemp];
			if(grayScanRng>0){
				for(int j=1; j<=grayScanRng; j++){
					if(iTemp-j>=0 && gray>imgGrays[i][iTemp-j]) gray=imgGrays[i][iTemp-j];
					if(iTemp+j<imgHeight && gray>imgGrays[i][iTemp+j]) gray=imgGrays[i][iTemp+j];
				}
			}
			
			grays.add(gray);
		}
		
		if(grays.size()>0){
			tmpGrays = new int[grays.size()];
			for(int i=0; i<grays.size(); i++){
				tmpGrays[i] = grays.get(i);
			}
			Arrays.sort(tmpGrays);
			
			refIndex = (int)(grays.size()*refRate);
			if(refIndex<=0) refIndex = 1;
			if(refIndex>grays.size()) refIndex = grays.size();
			for(int i=0; i<refIndex; i++){
				refGray += tmpGrays[i];
			}
			refGray = refGray/refIndex;
			
			for(int i=0; i<grays.size(); i++){
				iTemp = grays.get(i)-(int)refGray;
				if(iTemp>3) counter[0] = counter[0] + 1;
				if(iTemp>2) counter[1] = counter[1] + 1;
				if(iTemp>1) counter[2] = counter[2] + 1;
			}
			
			for(int i=0; i<counter.length; i++){
				rtnParas[i] = (double)counter[i]/grays.size();
			}
		}
		
		return rtnParas;
	}
	
	/**
	 * 
	 * @return double[5] 0:Offset<=-3 | 1:Offset<=-2 | 2:Offset<=-1 | 3:Offset>=1 | 4:Offset>=2 | 5:Offset>=3
	 */
	private double[] getGrayDelta(double[] coefLine_1, double[] coefLine_2, int[][] imgGrays, int xStart, int xStop, int scanRng_1, int scanRng_2, double[] firstHalfGDThreshold){
		int col_1 = 0, col_2 = 0, tmpDelta = 0, gray_1 = 0, gray_2 = 0, xMid = 0;
		int imgWidth = imgGrays.length, imgHeight = imgGrays[0].length-1, totalCnt = 0, totalCnt2 = 0;
		int[] counter = new int[]{0,0,0,0,0,0}, counter2 = new int[]{0,0,0,0,0,0};
		double[] grayDelta = new double[]{-1.0,-1.0,-1.0,-1.0,-1.0,-1.0}, grayDeltaScore = null, tempDelta = new double[]{0.0,0.0,0.0,0.0,0.0,0.0};
		double tmpScore = 0.0, minScore = 60.0;
		
		if(null!=coefLine_1 && null!=coefLine_2 && coefLine_1.length>1 && coefLine_2.length>1){
			if(xStart<xStop){
				col_1 = xStart;
				xStart = xStop;
				xStop = col_1;
			}
			xMid = (xStart+xStop)/2;
			
			if(null==firstHalfGDThreshold || null!=firstHalfGDThreshold && firstHalfGDThreshold.length<6){
				firstHalfGDThreshold = new double[]{0.75,1.0,1.0,1.0,1.0,0.75};
			}
			
			for(int x=xStart; x>=xStop; x--){
				if(x<0 || x>=imgWidth) continue;
				col_1 = (int)(x*coefLine_1[0]+coefLine_1[1]);
				col_2 = (int)(x*coefLine_2[0]+coefLine_2[1]);
				if(col_1>0 && col_1<imgHeight && col_2>0 && col_2<imgHeight){
					totalCnt++;
					gray_1 = imgGrays[x][col_1];
					if(scanRng_1>0){
						for(int i=1; i<=scanRng_1; i++){
							if(col_1-i>=0 && gray_1>imgGrays[x][col_1-i]) gray_1=imgGrays[x][col_1-i];
							if(col_1+i<imgHeight && gray_1>imgGrays[x][col_1+i]) gray_1=imgGrays[x][col_1+i];
						}
					}
					
					gray_2 = imgGrays[x][col_2];
					if(scanRng_2>0){
						for(int i=1; i<=scanRng_2; i++){
							if(col_2-i>=0 && gray_2>imgGrays[x][col_2-i]) gray_2=imgGrays[x][col_2-i];
							if(col_2+i<imgHeight && gray_2>imgGrays[x][col_2+i]) gray_2=imgGrays[x][col_2+i];
						}
					}
					
					tmpDelta = gray_1 - gray_2;
					if(tmpDelta<=-3) counter[0] = counter[0] + 1;
					if(tmpDelta<=-2) counter[1] = counter[1] + 1;
					if(tmpDelta<=-1) counter[2] = counter[2] + 1;
					if(tmpDelta>=1) counter[3] = counter[3] + 1;
					if(tmpDelta>=2) counter[4] = counter[4] + 1;
					if(tmpDelta>=3) counter[5] = counter[5] + 1;
					
					if(x>=xMid){
						totalCnt2++;
						if(tmpDelta<=-3) counter2[0] = counter2[0] + 1;
						if(tmpDelta<=-2) counter2[1] = counter2[1] + 1;
						if(tmpDelta<=-1) counter2[2] = counter2[2] + 1;
						if(tmpDelta>=1) counter2[3] = counter2[3] + 1;
						if(tmpDelta>=2) counter2[4] = counter2[4] + 1;
						if(tmpDelta>=3) counter2[5] = counter2[5] + 1;
					}
				}
			}
			
			if(totalCnt>0){
				for(int i=0; i<counter2.length; i++){
					tempDelta[i] = (double)counter2[i]/totalCnt2;
				}
				grayDeltaScore = calcGrayDeltaScore(tempDelta,firstHalfGDThreshold);
				
				xMid = counter.length/2;
				for(int i=0; i<counter.length; i++){
					if(i<xMid){
						tmpScore = grayDeltaScore[0];
					}else{
						tmpScore = grayDeltaScore[1];
					}
					if(tmpScore>=minScore) grayDelta[i] = (double)counter[i]/totalCnt;
				}
			}
		}
		
		return grayDelta;
	}
	
	private int getMaxPeakIndex(double[] trendRawData, int searchStartIdx, int totalScanPoints, int maxSteps, boolean chkHigherPeak, boolean forwardDirection, boolean checkLastPeak){
		int idx1 = -1, idx2 = -1, idx3 = -1, stepCounter = 1;
		
		if(searchStartIdx>=0 && searchStartIdx<trendRawData.length){
			idx1 = MathUtils.getFirstPeakIndex(trendRawData, searchStartIdx, totalScanPoints, chkHigherPeak, forwardDirection);
			if(idx1>0 && maxSteps>1){
				while(true){
					stepCounter++;
					idx2 = MathUtils.getFirstPeakIndex(trendRawData, forwardDirection?idx1+totalScanPoints:idx1-totalScanPoints, totalScanPoints, chkHigherPeak, forwardDirection);
					if(idx2>0 && trendRawData[idx2]>=trendRawData[idx1]){
						if(checkLastPeak){
							idx3 = MathUtils.getFirstPeakIndex(trendRawData, forwardDirection?idx2+totalScanPoints:idx2-totalScanPoints, totalScanPoints, chkHigherPeak, forwardDirection);
							if(idx3>0 && trendRawData[idx3]/trendRawData[idx2]>0.5){
								idx1 = idx2;
							}else{
								break;
							}
						}else{
							idx1 = idx2;
						}
					}else{
						break;
					}
					if(stepCounter>=maxSteps) break;
				}
			}
		}
		
		return idx1;
	}
	
	private ArrayList<Object> getLineSharpChgPos(double[] trendRawData, double refPeakVal, int searchStartIdx, int totalScanPoints, double peaksMinRatio, boolean chkHigherPeak, boolean forwardDirection){
		int idx1 = -1, idx2 = -1;
		double ratio = 0.0, maxGrad = 0.0;
		ArrayList<Object> sharpChgPos = new ArrayList<Object>();
		ArrayList<Integer> chgIndex = new ArrayList<Integer>();
		ArrayList<Double> chgGradient = new ArrayList<Double>();
		
		if(searchStartIdx>=0 && searchStartIdx<trendRawData.length){
			idx1 = MathUtils.getFirstPeakIndex(trendRawData, searchStartIdx, totalScanPoints, chkHigherPeak, forwardDirection);
			while(idx1>0){
				ratio = trendRawData[idx1]/refPeakVal;
				if(ratio>peaksMinRatio){
					break;
				}else{
					idx1 = MathUtils.getFirstPeakIndex(trendRawData, forwardDirection?idx1+totalScanPoints/2:idx1-totalScanPoints/2, totalScanPoints, chkHigherPeak, forwardDirection);
				}
			}
			
			if(idx1>0 && ratio>peaksMinRatio){
				chgIndex.add(idx1);
				chgGradient.add(trendRawData[idx1]);
				maxGrad = trendRawData[idx1];
				while(true){
					ratio = 0.0;
					idx2 = MathUtils.getFirstPeakIndex(trendRawData, forwardDirection?idx1+totalScanPoints/2:idx1-totalScanPoints/2, totalScanPoints, chkHigherPeak, forwardDirection);
					if(idx2>0){
						if(trendRawData[idx2]>maxGrad) maxGrad = trendRawData[idx2];
						ratio = trendRawData[idx2]/maxGrad;
						if(ratio>peaksMinRatio){
							chgIndex.add(idx2);
							chgGradient.add(trendRawData[idx2]);
						}
						idx1 = idx2;
					}else{
						break;
					}
				}
			}
		}
		
		if(chgIndex.size()>0){
			sharpChgPos.add(0,chgIndex);
			sharpChgPos.add(1, chgGradient);
		}
		return sharpChgPos;
	}
	
	private void calcFinalData(LinkedHashMap<Integer, ImgExtractLine> linesMap, int[][] initVals, int targetLinesQty){
		int imgWidth = initVals.length, imgHeight = initVals[0].length, lineFlag = 0;
		int startX = 0, nearCol = 0, redEdge = 0, crossX = 0, minX = 0, poleX = 0;
		double lineSlope = 0.0, lineOffset = 0.0;
		boolean linePassed = false;
		ArrayList<Object> wrinkleParas = new ArrayList<Object>();
		ImgExtractLine exLine = null;
		
		for(int i=0; i<linesMap.size(); i++){
			if(i>=targetLinesQty) break;
			exLine = linesMap.get(i);
			if(null==exLine) continue;
			
			linePassed = exLine.getLinePassed();
			lineFlag = lineIndexBase+i;
			
			poleX = exLine.getPolePosition(); startX = poleX;
			wrinkleParas = exLine.getWrinkleParas();
			if((double)wrinkleParas.get(2)!=0 && (double)wrinkleParas.get(3)>=0){
				crossX = (int)((double)wrinkleParas.get(3));
				minX = (int)((double)wrinkleParas.get(4));
				if(crossX>poleX) startX = crossX;
			}
			
			lineSlope = exLine.getLineSlope();
			lineOffset = exLine.getLineIntercept() + exLine.getLineInterceptDelta();
			redEdge = imgWidth - 20;
			for(int k=startX; k<imgWidth; k++){ //Solid line
				nearCol = (int)(k*lineSlope+lineOffset);
				if(nearCol<0 || nearCol>(imgHeight-1)) continue;
				finalData[k][nearCol] = (!linePassed && k>redEdge?4:lineFlag);
			}
			
			if((double)wrinkleParas.get(2)!=0 && (double)wrinkleParas.get(3)>=0){
				lineSlope = (double)wrinkleParas.get(0);
				lineOffset = (double)wrinkleParas.get(1);
				
				if(crossX>poleX){
					for(int k=crossX; k>=poleX; k--){//solid line
						nearCol = (int)(k*lineSlope+lineOffset);
						if(nearCol<0 || nearCol>(imgHeight-1)) continue;
						finalData[k][nearCol] = lineFlag;
					}
				}
				
				if(poleX>minX){
					for(int k=poleX; k>=minX; k=k-10){//dash line
						for(int n=5; n<10; n++){
							if(k<n) break;
							nearCol = (int)((k-n)*lineSlope+lineOffset);
							if(nearCol<0 || nearCol>(imgHeight-1)) continue;
							finalData[k-n][nearCol] = lineFlag;
						}
					}
				}
			}
		}
	}
	
	private int calculateOverhang(ImgExtractLine currLine, ImgExtractLine lastLine, boolean curLineLonger){
		double currSlope = currLine.getLineSlope(), currOffset = currLine.getLineIntercept()+currLine.getLineInterceptDelta();
		double lastSlope = lastLine.getLineSlope(), lastOffset = lastLine.getLineIntercept()+lastLine.getLineInterceptDelta();
		double verticalSlope = 0.0, verticalOffset = 0.0, x1 = 0.0, y1 = 0.0, x2 = 0.0, y2 = 0.0, d0 = 0.0, d1 = 0.0, d2 = 0.0;
		int lastPolePos = lastLine.getPolePosition(), currPolePos = currLine.getPolePosition(), overhang = 0;
		if(null==currLine || null==lastLine) return overhang;
		
		if(0==currSlope){
			if(curLineLonger){
				overhang = lastPolePos - currPolePos;
			}else{
				overhang = currPolePos - lastPolePos;
			}
		}else{
			x1 = currPolePos; y1 = x1*currSlope+currOffset;
			x2 = lastPolePos; y2 = x2*lastSlope+lastOffset;
			d1 = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
			
			if(curLineLonger){
				if(0!=currLine.getLineSlope()){
					verticalSlope = -1/currLine.getLineSlope();
					verticalOffset = y2 - verticalSlope*x2;
					x1 = (currOffset-verticalOffset)/(verticalSlope-currSlope);
					y1 = (currOffset*verticalSlope-verticalOffset*currSlope)/(verticalSlope-currSlope);
				}else{
					x1 = x2;
					y1 = y2 + currOffset - lastOffset;
				}
			}else{
				if(0!=lastLine.getLineSlope()){
					verticalSlope = -1/lastLine.getLineSlope();
					verticalOffset = y1 - verticalSlope*x1;
					x2 = (lastOffset-verticalOffset)/(verticalSlope-lastSlope);
					y2 = (lastOffset*verticalSlope-verticalOffset*lastSlope)/(verticalSlope-lastSlope);
				}else{
					x2 = x1;
					y2 = y1 + currOffset - lastOffset;
				}
			}
			d2 = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
			d0 = Math.sqrt(d1*d1-d2*d2);
			
			if(curLineLonger){
				overhang = (int)((lastPolePos - currPolePos<0?-1:1)*d0);
			}else{
				overhang = (int)((currPolePos - lastPolePos<0?-1:1)*d0);
			}
		}
		
		return overhang;
	}
	
	private boolean calculateFinalResult(LinkedHashMap<Integer, ImgExtractLine> linesMap, boolean evenLineLonger, int[][] imgGrays, int defaultVal, int targetLinesQty, int[][] peakVals, double evenPoleAvgOffset, double oddPoleAvgOffset){
		boolean bPassed = false, curLinePassed = false;
		double minDistance = 10;//pixels
		double maxDistance = 116;//pixels
		double maxMissedCheckRate = 0.0, minPassedRate = 1.0, onePixelLen = 0.012, maxBigAgRate = 0.5, minAvgOverhang = 0.0;
		int meetCndQty = 0, missedCheckQty = 0, longerPoleQty = 0, bigAngleQty = 0, validOhQty = 0;
		double missedCheckRate = 0.0, passedRate = 0.0, bigAgRate = 0.0, avgOverhang = 0.0;
		String procRsltKey = "";
		
		onePixelLen = Double.parseDouble(""+criteria.get("onePixel"));
		minDistance = Double.parseDouble(""+criteria.get("minPoleDistance"))/onePixelLen;
		maxDistance = Double.parseDouble(""+criteria.get("maxPoleDistance"))/onePixelLen;
		minAvgOverhang = (minDistance + (maxDistance-minDistance)*0.1)*onePixelLen;
		
		int[] distances = new int[targetLinesQty];
		int lineCounter = 0, currPolePos = 0;
		ImgExtractLine currLine = null, lastLine = null;
		for(int i=0; i<targetLinesQty; i++){
			curLinePassed = false;
			currLine = linesMap.get(i);
			if(null!=currLine){
				currPolePos = currLine.getPolePosition();
				if(currPolePos<0){
					currPolePos = currLine.calcPolePosition(evenLineLonger,linesMap,i,imgGrays,peakVals,targetLinesQty);
					currLine.setPolePosition(currPolePos);
				}
				
				if(curLineIsLonger(i, evenLineLonger)) longerPoleQty++;
				
				if(i>0){
					procRsltKey = getResultKey(i);
					lastLine = linesMap.get(i-1);
					if(null!=lastLine){
						distances[i] = calculateOverhang(currLine,lastLine,curLineIsLonger(i,evenLineLonger));
						if(distances[i]>=minDistance && distances[i]<=maxDistance){
							meetCndQty++;
							curLinePassed = true;
						}
						imgProcResultLength.put(procRsltKey, distances[i]*onePixelLen);
						imgProcResultOK.put(procRsltKey, curLinePassed);
						validOhQty++;
						avgOverhang += imgProcResultLength.get(procRsltKey);
					}else{
						missedCheckQty++;
						imgProcResultLength.put(procRsltKey, 0.0);
						imgProcResultOK.put(procRsltKey, false);
					}
				}
				currLine.setLinePassed(i>0?curLinePassed:true);
				
				lineCounter++;
				if(logEnabled){
					LogUtils.rawLog("sortLineParas_", currLine.printLineParas(i,1==lineCounter?true:false));
				}
			}else{
				if(i>0) missedCheckQty++;
			}
		}
		
		missedCheckRate = (double)missedCheckQty / (targetLinesQty - 1);
		passedRate = (double)meetCndQty / (targetLinesQty - 1 - missedCheckQty);
		if(missedCheckRate<=maxMissedCheckRate && passedRate>=minPassedRate){
			bPassed = true;
		}
		
		//Check all angles
		if(bPassed && imgProcResultAngle.size()>0){
			for(String key:imgProcResultAngle.keySet()){
				if(Math.abs(imgProcResultAngle.get(key))>10) bigAngleQty++;
			}
			if(longerPoleQty>0) bigAgRate = (double)bigAngleQty/longerPoleQty;
			if(bigAgRate>maxBigAgRate) bPassed = false; //Fail worse wrinkle case
		}
		
		//Check average overhang
		if(bPassed && validOhQty>0){
			avgOverhang = avgOverhang / validOhQty;
			if(avgOverhang<minAvgOverhang) bPassed = false; //Fail too small overhang case
		}
		
		return bPassed;
	}

	private void storeLayerAngle(boolean evenLineLonger, int layerIdx, double dnSideAngle, double upSideAngle) {
		if(dnSideAngle>0 || upSideAngle>0){
			String angleKey = "";
			if(0==layerIdx){
				angleKey = "L1";
			}else{
				if(curLineIsLonger(layerIdx, evenLineLonger)){
					if(0==layerIdx%2){
						angleKey = "L" + (layerIdx/2);
					}else{
						angleKey = "L" + ((layerIdx+1)/2);
					}
				}else{
					if(0==(layerIdx-1)%2){
						angleKey = "L" + ((layerIdx-1)/2);
					}else{
						angleKey = "L" + (layerIdx/2);
					}
				}
			}
			angleKey = angleKey + (dnSideAngle>upSideAngle?"_L":"_R");
			imgProcResultAngle.put(angleKey, (dnSideAngle>upSideAngle?dnSideAngle:-upSideAngle));
		}
	}
	
	private LinkedHashMap<Integer, ImgExtractLine> searchMissingLines(LinkedHashMap<Integer, ImgExtractLine> linesMap,
			ImgExtractLine myBaseline,int[][] peakVals, int[][] initVals, int[][]imgGrays, 
			int defaultVal, double[] slope, double[] offset, int bestRsqIdx,
			double evenPoleAvgOffset, double oddPoleAvgOffset, int targetLinesQty, boolean chkMinOffset){
		
		double diff1,diff2,offsetChgR,addLineSlope,addLineOffset,off1,off2,curOffset;
		double researchSlope = myBaseline.getLineSlope(), researchOffset = myBaseline.getLineIntercept();
		double researchOffsetStop = 0;
		
		for(int i=0; i<targetLinesQty; i++){
			ImgExtractLine exLine = linesMap.get(i);
			if(null==exLine){
				if(null!=linesMap.get(i-1)){
					researchOffset = linesMap.get(i-1).getLineIntercept()+linesMap.get(i-1).getLineInterceptDelta();
				}else{
					researchOffset = slope[bestRsqIdx]*(i-1)+offset[bestRsqIdx];
				}
				
				if(null!=linesMap.get(i+1)){
					researchOffsetStop = linesMap.get(i+1).getLineIntercept()+linesMap.get(i+1).getLineInterceptDelta();
				}else{
					researchOffsetStop = slope[bestRsqIdx]*(i+1)+offset[bestRsqIdx];
				}
				off1 = researchOffset; off2 = researchOffsetStop;
				addLineSlope = researchSlope; addLineOffset = (researchOffset+researchOffsetStop)/2;
				if(0==i%2){
					researchOffset += evenPoleAvgOffset*0.9;
					researchOffsetStop -= (oddPoleAvgOffset*0.9>lineScanRange?lineScanRange:oddPoleAvgOffset*0.9);
					if(i==(targetLinesQty-1)) researchOffsetStop = researchOffset + oddPoleAvgOffset*1.5;
				}else{
					researchOffset += oddPoleAvgOffset*0.9;
					researchOffsetStop -= (evenPoleAvgOffset*0.9>lineScanRange?lineScanRange:evenPoleAvgOffset*0.9);
					if(i==(targetLinesQty-1)) researchOffsetStop = researchOffset + evenPoleAvgOffset*1.5;
				}
				if(researchOffsetStop-researchOffset<lineScanRange){
					researchOffset = (researchOffsetStop+researchOffset)/2-lineScanRange/2-1;
					researchOffsetStop = researchOffset+lineScanRange+1;
				}
				
				exLine = searchLine(peakVals, initVals, imgGrays, defaultVal, researchOffset, researchOffsetStop, researchSlope, (int)(researchOffsetStop-researchOffset));
				if(null!=exLine){
					if(null!=linesMap.get(i-2) && null!=linesMap.get(i-3)){
						diff1 = exLine.getLineIntercept()+exLine.getLineInterceptDelta()-researchOffset;
						diff2 = linesMap.get(i-2).getLineIntercept()-linesMap.get(i-3).getLineIntercept();
						diff2 += linesMap.get(i-2).getLineInterceptDelta()-linesMap.get(i-3).getLineInterceptDelta();
						offsetChgR = diff1/diff2;
						if(offsetChgR>maxOffsetChgRate){
							researchOffsetStop = exLine.getLineIntercept()+exLine.getLineInterceptDelta() - lineScanRange/2;
							exLine = searchLine(peakVals, initVals, imgGrays, defaultVal, researchOffset, researchOffsetStop, researchSlope, (int)(researchOffsetStop-researchOffset));
						}
						else if(chkMinOffset && offsetChgR<minOffsetChgRate){
							curOffset = exLine.getLineIntercept()+exLine.getLineInterceptDelta();
							if(off2-curOffset<curOffset-off1){
								exLine = addFixedLine(peakVals,imgGrays,defaultVal,addLineSlope,addLineOffset);
							}
						}
					}
					if(null!=exLine){
						exLine.setLineFlag(3);
						linesMap.put(i, exLine);
					}
				}
				System.out.println((null==exLine?"Missing:":"Insert:")+i);
			}else{
				researchSlope = linesMap.get(i).getLineSlope();
			}
		}
		
		return linesMap;
	}
	
	private boolean thereIsMissingLines(LinkedHashMap<Integer, ImgExtractLine> linesMap, int targetLinesQty){
		boolean missing = false;
		
		for(int i=0; i<targetLinesQty; i++){
			if(null == linesMap.get(i)){
				missing = true;
				break;
			}
		}
		return missing;
	}
	
	private boolean lineIsNegPole(int lineIdx, boolean evenLineLonger, double evenPoleAvgOffset, double oddPoleAvgOffset){
		boolean isNegPole = false;
		
		if(0==lineIdx%2 && !evenLineLonger || 1==lineIdx%2 && evenLineLonger){
			if(evenPoleAvgOffset>oddPoleAvgOffset) isNegPole = true;
		}
		
		return isNegPole;
	}
	
	private ImgExtractLine getMostClosedLine(double offset, double tolerance){
		ImgExtractLine line = null;
		int idx = -1;
		for(int i=0; i<allPossibleLines.size(); i++){
			if(allPossibleLines.get(i).getLineIntercept()>=offset){
				idx = i;
				break;
			}
		}
		if(idx>=0){
			if(allPossibleLines.get(idx).getLineIntercept()-offset<=tolerance){
				line = allPossibleLines.get(idx);
			}else if(idx>0 && allPossibleLines.get(idx-1).getLineIntercept()-offset<=tolerance){
				line = allPossibleLines.get(idx-1);
			}
		}
		
		return line;
	}
	
	private int getLineMeanGray(ImgExtractLine currLine, int[][] imgGrays, int startIdx, int stopIdx){
		if(null==currLine) return -1;
		
		double slope = currLine.getLineSlope(), sumGray = 0.0;
		double offset = currLine.getLineIntercept()+currLine.getLineInterceptDelta();
		int nearCol = 0, imgWidth = imgGrays.length, imgHeight = imgGrays[0].length;
		int meanGray = -1, counter = 0;
		if(startIdx>stopIdx){
			nearCol = startIdx;
			startIdx = stopIdx;
			stopIdx = nearCol;
		}
		if(startIdx<0) startIdx = 0;
		if(stopIdx+1>imgWidth) stopIdx=imgWidth-1;
		
		for(int i=startIdx; i<=stopIdx; i++){
			nearCol = (int)(slope * i + offset);
			if(nearCol>=0 && nearCol<imgHeight){
				sumGray += imgGrays[i][nearCol];
				counter++;
			}
		}
		if(counter>0) meanGray = (int)(sumGray/counter);
		
		return meanGray;
	}
	
	private boolean thereIsPoleBtw(int[][] imgGrays, ImgExtractLine currLine, ImgExtractLine lastLine, boolean supposed){
		boolean poleExisting = supposed;
		
		if(null!=currLine && null!=lastLine){
			poleExisting = false;
			ImgExtractLine tmpLine = null;
			double offset1 = currLine.getLineIntercept()+currLine.getLineInterceptDelta();
			double offset2 = lastLine.getLineIntercept()+lastLine.getLineInterceptDelta();
			poleExisting = offsetInPeakRegion(allPossibleLines,(offset1+offset2)/2);
			
			//If out of expectation
			int gray0 = 0, gray1 = 0, gray2 = 0;
			if(!poleExisting && supposed){
				tmpLine = getMostClosedLine((offset1+offset2)/2,lineScanRange/2);
				if(null!=tmpLine){
					gray0 = getLineMeanGray(tmpLine,imgGrays,imgGrays.length/2,imgGrays.length);
					gray1 = getLineMeanGray(currLine,imgGrays,imgGrays.length/2,imgGrays.length);
					gray2 = getLineMeanGray(lastLine,imgGrays,imgGrays.length/2,imgGrays.length);
					if((double)(gray0-gray1+gray0-gray2)/2+0.5<3){
						poleExisting = supposed;
					}else{
						poleExisting = !supposed;
					}
				}
			}
		}
		
		return poleExisting;
	}
	
	private boolean lineIsThicker(double[] weightOfLine, int[][] imgGrays, ImgExtractLine currLine, ImgExtractLine lastLine, double offsetChgR){
		boolean thicker = false;
		//Total lines appear in Up or Dn side checking region is more than 3
		if(weightOfLine[6]>=0.6 && weightOfLine[8]>=5 
			|| weightOfLine[5]>=0.6 && weightOfLine[7]>=5
			|| weightOfLine[5]*weightOfLine[7]+weightOfLine[6]*weightOfLine[8]>=5){
			if(null==currLine){
				thicker = true;
			}else{
				double slope = currLine.getLineSlope();
				double offsetCurrLine = currLine.getLineIntercept()+currLine.getLineInterceptDelta();
				double offsetLastLine = offsetCurrLine-10;
				if(null!=lastLine) offsetLastLine = lastLine.getLineIntercept() + lastLine.getLineInterceptDelta()-3;
				int scanRange = (int)(offsetCurrLine - offsetLastLine);
				if(scanRange<0) scanRange = 10;
				int stDelta = -5;
				int[] grays = new int[scanRange+1-stDelta];
				int innerAvgGray = 0, outerAvgGray = 0, innerCnt = 0, outerCnt = 0;
				double innerGray = 0.0, outerGray = 0.0;
				
				for(int i=stDelta; i<=scanRange; i++){
					grays[i-stDelta] = currLine.getLineAvgGray(imgGrays, currLine.getXAxisStop(), slope, offsetCurrLine-i);
					if(i<scanRange/2){
						innerGray += grays[i-stDelta];
						innerCnt++;
					}else{
						outerGray += grays[i-stDelta];
						outerCnt++;
					}
				}
				outerAvgGray = (int)(outerGray/outerCnt);
				innerAvgGray = (int)(innerGray/innerCnt);
				
				if(outerAvgGray-innerAvgGray>3 
					|| outerAvgGray-innerAvgGray>=3 && weightOfLine[5]*weightOfLine[7]+weightOfLine[6]*weightOfLine[8]>=5
				){
					thicker = true;
				}
			}
		}
		
		return thicker;
	}
	
	private boolean evenLineIsLonger(LinkedHashMap<Integer, ImgExtractLine> linesMap, int[][] imgGrays){
		boolean evenLineLonger = false;
		int[] refGrayIndex = new int[targetLinesQty];
		double[] refGrays = new double[targetLinesQty];
		int longPoleFirstQty = 0, imgHeight = imgGrays[0].length;
		double weightOfEvenLineLonger = 0.0;
		
		for(int i=0; i<targetLinesQty; i++){
			ImgExtractLine exLine = linesMap.get(i);
			if(null!=exLine){
				if(0==i%2){
					refGrayIndex[i] = exLine.getXAxisStart();
				}else{
					refGrayIndex[i] = refGrayIndex[i-1];
				}
				
				double refGray = 0.0;
				double lineSlope = exLine.getLineSlope();
				double lineOffset = exLine.getLineIntercept();
				int nearCol = 0;
				for(int k=0; k<=refGrayIndex[i]; k++){
					nearCol = (int)(k*lineSlope+lineOffset);
					if(nearCol<0 || nearCol>(imgHeight-1)) continue;
					refGray += imgGrays[k][nearCol];
				}
				exLine.setRefGray(refGray);
				refGrays[i] = refGray;
				
				if(0!=i%2){
					if(refGrays[i]>refGrays[i-1]) longPoleFirstQty++;
				}
			}
		}
		weightOfEvenLineLonger = (double)longPoleFirstQty*2 / targetLinesQty;
		if(weightOfEvenLineLonger>0.5) evenLineLonger = true;
		
		return evenLineLonger;
	}
	
	private LinkedHashMap<Integer, ImgExtractLine> addMissingLines(LinkedHashMap<Integer, ImgExtractLine> linesMap,
			ImgExtractLine myBaseline,int[][] peakVals, int[][] initVals, int[][]imgGrays, 
			int defaultVal, double[] slope, double[] offset, int bestRsqIdx,
			double evenPoleAvgOffset, double oddPoleAvgOffset, int targetLinesQty){
		
		double addLineSlope = 0.0, addLineOffset = 0.0;
		double researchOffset = 0.0, researchOffsetStop = 0.0, researchSlope = 0.0;
		
		for(int i=1; i<targetLinesQty; i++){
			if(null==linesMap.get(i) && null!=linesMap.get(i-1)){
				ImgExtractLine exLine = null;
				addLineSlope = 0.0; addLineOffset = 0.0;
				if(null!=linesMap.get(i+1)){
					addLineSlope = (linesMap.get(i-1).getLineSlope()+linesMap.get(i+1).getLineSlope())/2;
					addLineOffset = (linesMap.get(i-1).getLineIntercept()+linesMap.get(i-1).getLineInterceptDelta()
							+linesMap.get(i+1).getLineIntercept()+linesMap.get(i+1).getLineInterceptDelta())/2;
				}else if(i==(targetLinesQty-1)){
					researchSlope = linesMap.get(i-1).getLineSlope();
					researchOffset = linesMap.get(i-1).getLineIntercept()+linesMap.get(i-1).getLineInterceptDelta()+(evenPoleAvgOffset+oddPoleAvgOffset)/2;
					researchOffsetStop = researchOffset + (evenPoleAvgOffset+oddPoleAvgOffset)/2*3;
					exLine = searchLine(peakVals, initVals, imgGrays, defaultVal, researchOffset, researchOffsetStop, researchSlope, (int)(researchOffsetStop-researchOffset));
					if(null!=exLine){
						exLine.setLineFlag(3);
					}else{
						addLineSlope = linesMap.get(i-1).getLineSlope();
						addLineOffset = linesMap.get(i-1).getLineIntercept()+linesMap.get(i-1).getLineInterceptDelta()
								+(evenPoleAvgOffset+oddPoleAvgOffset)/2;
					}
				}
				if(addLineOffset>0) exLine = addFixedLine(peakVals,imgGrays,defaultVal,addLineSlope,addLineOffset);
				if(null!=exLine) linesMap.put(i, exLine);
			}
		}
		
		return linesMap;
	}
	
	private ImgExtractLine addFixedLine(int[][] peakVals, int[][]imgGrays, 
			int defaultVal, double addLineSlope, double addLineOffset){
		
		ImgExtractLine exLine = null;
		int imgWidth = peakVals.length;
		int imgHeight = peakVals[0].length;
		int nearCol = 0, scanRng = lineScanRange/2, colOffset = 0, maxCnt = 0;
		int[] dataCounters = new int[scanRng*2+1];
		exLine = new ImgExtractLine();
		
		for(int j=(imgWidth-1); j>=0; j--){
			nearCol = (int)(j*addLineSlope+addLineOffset);
			for(int k=-scanRng; k<=scanRng; k++){
				if(nearCol+k>=0 && nearCol+k<imgHeight){
					if(peakVals[j][nearCol+k]<lineFitGrayThr){
						dataCounters[k+scanRng] = dataCounters[k+scanRng]+1;
					}
				}
			}
		}
		
		for(int k=0; k<dataCounters.length; k++){
			if(maxCnt<dataCounters[k]){
				maxCnt = dataCounters[k];
				colOffset = k;
			}
		}
		
		colOffset = colOffset - scanRng;
		for(int j=(imgWidth-1); j>=0; j--){
			nearCol = (int)(j*addLineSlope+addLineOffset);
			if(nearCol+colOffset>=0 && nearCol+colOffset<imgHeight){
				if(peakVals[j][nearCol+colOffset]<lineFitGrayThr){
					exLine.addPoint(j, nearCol, imgGrays[j][nearCol]);
				}
			}
		}
		
		exLine.getLineCoef(false);
		exLine.setLineFlag(3);
		exLine = setLineBoundary(exLine, imgGrays, defaultVal, 0.3);
		
		return exLine;
	}
	
	private void initLineFitGrayThreshold(){
		lineFitGrayThr = 255 * 0.8;
	}
	
	private void adjustLineFitGrayThreshold(int[][] imgGrays, ImgExtractLine myBaseline, int scanOffset){
		double slope = myBaseline.getLineSlope();
		double offset = myBaseline.getLineIntercept();
		int nearRow = 0, imgWidth = imgGrays.length, imgHeight = imgGrays[0].length;
		double grayAvg = 0.0;
		int counter = 0, startCol = (int)(imgWidth*0.5);
		
		for(int col=startCol; col<imgWidth; col++){
			nearRow = (int)(slope*col+offset);
			if(nearRow<0 || nearRow+scanOffset>imgHeight) continue;
			for(int row=0; row<scanOffset; row++){
				if(imgGrays[col][nearRow+row]<noiseGrayThr2){
					counter++;
					grayAvg += imgGrays[col][nearRow+row];
				}
			}
		}
		
		if(counter>0){
			grayAvg = grayAvg / counter * 1.05;
			System.out.print("Line fit threshold:"+lineFitGrayThr+"(ori)/"+grayAvg);
			if(grayAvg>lineFitGrayThr) lineFitGrayThr = grayAvg;
			System.out.println("(avg)=>"+lineFitGrayThr+"(final)");
		}
	}
	
	private ArrayList<ImgExtractLine> searchLines(int[][] peakVals, int[][] initVals, int[][] imgGrays, int defaultVal, ImgExtractLine myBaseline){
		int imgWidth = peakVals.length;
		int imgHeight = peakVals[0].length;
		int validCnt = 0, nearCol = 0, noisePoints = 0, realPoints = 0;
		int invalidCnt = 0, minValidCol = imgWidth*2, maxValidCol = -1;
		int startChkX = (int)(imgWidth*0.4), lastValidX = 0;
		int maxBlankX = (int)(imgWidth*0.05);
		double noiseRate = 0.0, slopeDelta = 0.0, validRate = 0.0;
		double lastLineOffset = 0.0, lastLineSlope = 0.0;
		double offsetDelta1 = 0.0, offsetDelta2 = 0.0, offsetDelta3 = 0.0, offsetDelta = 0.0;
		double invalidRatio = 0.0, maxColDelta = 3.0;
		int startSearchingOffset = 0, currentLineOffset = 0, finalSearchingOffset = 0;
		int lineSearchingRng = lineScanRange / 2;//Two sides searching
		
		int fittingMinPoints = (int)(imgWidth * minValidRatio);
		boolean bLineFound = false;
		
		int[][] sortData = new int[imgWidth][imgHeight];
		int[][] searchingLinesData = new int[imgWidth][imgHeight];
		for(int i=0; i<imgWidth; i++){
			sortData[i] = initVals[i].clone();
			searchingLinesData[i] = peakVals[i].clone();
		}
		double myBaselineSlope = myBaseline.getLineSlope();
		double myBaselineOffset = myBaseline.getLineIntercept();
		startSearchingOffset = (int)(myBaselineSlope*(imgWidth-1)+myBaselineOffset);
		if(startSearchingOffset<0) startSearchingOffset = 0;
		lastLineOffset = -1;
		initLineFitGrayThreshold();
		adjustLineFitGrayThreshold(imgGrays,myBaseline,30);
		
		double[] lineCoef = null;
		ArrayList<ImgExtractLine> extractLines = new ArrayList<ImgExtractLine>();
		ImgExtractLine lastPossibleLine = null;
		ImgExtractLine extractLine = new ImgExtractLine();
		ImgExtractLine.setMinRSQ(0.7f);
		
		if(imgWidth > fittingMinPoints && imgHeight > lineSearchingRng){
			for(int i=startSearchingOffset; i<imgHeight; i++){
				bLineFound = false;
				minValidCol = imgWidth*2; maxValidCol = -1;
				extractLine.clearLineData();
				if(26==i){
					System.out.print("");
				}
				
				for(int j=(imgWidth-1); j>=0; j--){
					if(j<startChkX){
						lineSearchingRng = 1;
					}else{
						lineSearchingRng = lineScanRange / 2;
					}
					nearCol = (int)(j*myBaselineSlope+myBaselineOffset);
					if((nearCol-lineSearchingRng)>=0 && nearCol<(imgHeight-lineSearchingRng)){
						for(int k=-lineSearchingRng; k<lineSearchingRng; k++){
							if(peakVals[j][nearCol+k]<lineFitGrayThr){
								if(j>=startChkX || j<startChkX && lastValidX-j<maxBlankX){
									extractLine.addPoint(j, nearCol+k, peakVals[j][nearCol+k]);
									if((nearCol+k)<minValidCol) minValidCol = nearCol+k;
									if((nearCol+k)>maxValidCol) maxValidCol = nearCol+k;
									if(Math.abs(k)<=1) extractLine.addRealPoints(j, nearCol+k, 1);
									lastValidX = j;
								}
							}
							if(imgGrays[j][nearCol+k]>=noiseGrayThr2) extractLine.addInvalidPoints(1);
						}
					}
				}
				
				validCnt = extractLine.getValidPoints();
				realPoints = extractLine.getRealPoints();
				invalidCnt = extractLine.getInvalidPoints();
				invalidRatio = (double)invalidCnt / (lineSearchingRng*2+1) / imgWidth;
				
				if(extractLines.size()<=0){
					saveSearchingLines(searchingLinesData,myBaselineSlope,myBaselineOffset,i,"debug");
					if(i-startSearchingOffset>(lineScanRange*3)){
						System.out.println("Fail to get 1st layer");
						break;
					}
				}
				
				if((validCnt>=fittingMinPoints || realPoints>=fittingMinPoints*0.8) && invalidRatio<maxInvalidRatio){
					lineCoef = extractLine.getLineCoef(false);
					currentLineOffset = (int)(lineCoef[0]*(imgWidth-1)+lineCoef[1]);
					slopeDelta = Math.abs(myBaselineSlope-extractLine.getLineSlope());
					if(-1==lastLineOffset){
						offsetDelta1 = minOffsetDelta*1.1;
						offsetDelta2 = offsetDelta1; offsetDelta3 = offsetDelta1;
					}else{
						offsetDelta1 = lineCoef[1]-lastLineOffset;
						offsetDelta2 = (lineCoef[0]-lastLineSlope)*imgWidth/2+lineCoef[1]-lastLineOffset;
						offsetDelta3 = (lineCoef[0]-lastLineSlope)*(imgWidth-1)+lineCoef[1]-lastLineOffset;
					}
					offsetDelta = (offsetDelta1+offsetDelta2+offsetDelta3)/3;
					
					if(maxValidCol-minValidCol>maxColDelta){
						validRate = extractLine.getValidRateByYAxis((maxValidCol+minValidCol)/2-1, (maxValidCol+minValidCol)/2+1);
					}else{
						validRate = 1.0;
					}
					
					if(null!=lineCoef && (ImgExtractLine.getMinRSQ()<=lineCoef[2] 
							|| (maxValidCol-minValidCol)<=maxColDelta 
							|| validRate>minValidRate) 
							&& (extractLines.size()>0 && currentLineOffset > 0
								|| extractLines.isEmpty() && currentLineOffset>=0)
							&& slopeDelta<maxSlopeDelta 
							&& offsetDelta1>minOffsetDelta/3 
							&& offsetDelta2>minOffsetDelta/3 
							&& offsetDelta3>minOffsetDelta/3
							&& offsetDelta>minOffsetDelta){
						for(int k=0; k<imgWidth; k++){
							nearCol = (int)(k*lineCoef[0]+lineCoef[1]);
							if(nearCol>noiseScanRng && nearCol<(imgHeight-noiseScanRng)){
								noisePoints = 0;
								for(int n=-noiseScanRng; n<=noiseScanRng; n++){
									if(peakVals[k][nearCol+n]<noiseGrayThr){
										if(Math.abs(n)>1){
											noisePoints++;
										}else{
											extractLine.addOnlinePoint();
										}
									}
								}
								if(noisePoints>0) extractLine.addSideNoise(noisePoints);
							}else{
								break;
							}
						}
						
						noiseRate = maxNoiseRate + 1.0;
						if(extractLine.getOnlinePoints()>0) noiseRate = (double)extractLine.getNoisePoints() / extractLine.getOnlinePoints();
						
						if(extractLine.getOnlinePoints()>=(fittingMinPoints*0.8) 
							&& (noiseRate<maxNoiseRate 
							|| noiseRate>=maxNoiseRate 
							&& validRate>=1.0 
							&& ImgExtractLine.getMinRSQ()<=lineCoef[2])){
							
							if(extractLines.isEmpty()) extractLine.getLineCoef(true);
							lastLineSlope = extractLine.getLineSlope();
							lastLineOffset = extractLine.getLineIntercept();
							
							saveSearchingLines(searchingLinesData,myBaselineSlope,myBaselineOffset,extractLines.size(),"found");
							
							myBaselineSlope = extractLine.getLineSlope();
							myBaselineOffset = extractLine.getLineIntercept()+1;
							
							currentLineOffset = (int)(myBaselineSlope*(imgWidth-1)+myBaselineOffset);
							i = currentLineOffset+lineSearchingRng;
							bLineFound = true;
							extractLine = setLineBoundary(extractLine, imgGrays, defaultVal, 0.3);
							extractLines.add(extractLine.clone());
							if(targetLinesQty==extractLines.size()) finalSearchingOffset = i;
							System.out.println("Line found:"+myBaselineSlope+"/"+myBaselineOffset);
						}
					}
				}
				if(!bLineFound) myBaselineOffset = myBaselineOffset+1;
				
				if(allPossibleLines.size()>0) lastPossibleLine = allPossibleLines.get(allPossibleLines.size()-1);
				if(!bLineFound && validCnt>5 && 0==extractLine.getLineIntercept()) extractLine.getLineCoef(false);
				if(allPossibleLines.size()>0 && validCnt==lastPossibleLine.getValidPoints()
					&& extractLine.getLineIntercept()-lastPossibleLine.getLineIntercept()<5){
					if(bLineFound || extractLine.getLineRSQ()>lastPossibleLine.getLineRSQ()){
						if(!bLineFound) bLineFound = (3==lastPossibleLine.getLineFlag()?true:false);
						allPossibleLines.remove(allPossibleLines.size()-1);
						if(allPossibleLines.size()>0){
							lastPossibleLine = allPossibleLines.get(allPossibleLines.size()-1);
						}else{
							lastPossibleLine = null;
						}
					}
				}
				if(allPossibleLines.isEmpty() || allPossibleLines.size()>0
					&& (validCnt!=lastPossibleLine.getValidPoints()
						|| extractLine.getLineIntercept()-lastPossibleLine.getLineIntercept()>=5)){
					if(bLineFound) extractLine.setLineFlag(3);
					allPossibleLines.add(extractLine.clone());
				}
				
				if(targetLinesQty==extractLines.size()){
					if(i>=finalSearchingOffset+lineScanRange*6) break;
				}
			}
		}
		
		if(extractLines.size()>0) extractLines = adjustAllSearchedLines(allPossibleLines,extractLines,imgGrays,defaultVal);
		saveImgData("graysSortLines_",sortData);
		return extractLines;
	}
	
	private double[] getPoleOffset(ArrayList<ImgExtractLine> lines, int targetLinesQty){
		double[] poleOffset = new double[]{0.0,0.0};
		double oddPoleOffset = 0.0, evenPoleOffset = 0.0, tmpOffset = 0.0;
		double oddMin = 0.0, oddMax = 0.0, evenMin = 0.0, evenMax = 0.0;
		int oddPoleCnt = 1, evenPoleCnt = 1;
		
		if(null!=lines && lines.size()>2){
			oddPoleOffset = lines.get(1).getLineIntercept() - lines.get(0).getLineIntercept();
			evenPoleOffset = lines.get(2).getLineIntercept() - lines.get(1).getLineIntercept();
			oddMin = oddPoleOffset; oddMax = oddMin;
			evenMin = evenPoleOffset; evenMax = evenMin;
			if(lines.size()>3){
				for(int i=3; i<lines.size(); i++){
					tmpOffset = lines.get(i).getLineIntercept() - lines.get(i-1).getLineIntercept();
					if(1==i%2){
						oddPoleCnt++;
						oddPoleOffset += tmpOffset;
						if(oddMin>tmpOffset) oddMin = tmpOffset;
						if(oddMax<tmpOffset) oddMax = tmpOffset;
					}else{
						evenPoleCnt++;
						evenPoleOffset += tmpOffset;
						if(evenMin>tmpOffset) evenMin = tmpOffset;
						if(evenMax<tmpOffset) evenMax = tmpOffset;
					}
				}
			}
			if(oddPoleCnt>2){
				oddPoleOffset = (oddPoleOffset-oddMin-oddMax)/(oddPoleCnt-2);
			}else{
				oddPoleOffset = oddPoleOffset/oddPoleCnt;
			}
			if(evenPoleCnt>2){
				evenPoleOffset = (evenPoleOffset-evenMin-evenMax)/(evenPoleCnt-2);
			}else{
				evenPoleOffset = evenPoleOffset/evenPoleCnt;
			}
			poleOffset[0] = evenPoleOffset;
			poleOffset[1] = oddPoleOffset;
		}
		
		return poleOffset;
	}
	
	private double[] twoLinesAvgOffset(double slope1, double offset1, double slope2, double offset2, double startX, double stopX){
		double sumOffset = 0.0, avgOffset = 0.0, stepX = (startX+stopX)/10, dblTemp = 0.0;
		double minOffset = 0.0, maxOffset = 0.0, offsetFactor = 1.0;
		int counter = 0;
		
		if(startX>stopX){
			dblTemp = startX;
			startX = stopX;
			stopX = dblTemp;
		}
		
		if(0==stepX) stepX = 1.0;
		if(offset2>offset1) offsetFactor = -1.0;
		minOffset = Math.abs(offset2-offset1); maxOffset = minOffset;
		for(double x = startX; x<=stopX; x+=stepX){
			dblTemp = (slope1*x+offset1 - (slope2*x+offset2))*offsetFactor;
			sumOffset += dblTemp;
			counter++;
			if(minOffset>dblTemp) minOffset = dblTemp;
			if(maxOffset<dblTemp) maxOffset = dblTemp;
		}
		
		avgOffset = sumOffset / counter;
		
		return new double[]{avgOffset,minOffset,maxOffset};
	}
	
	private ArrayList<ImgExtractLine> adjustAllSearchedLines(ArrayList<ImgExtractLine> allPossibleLines,ArrayList<ImgExtractLine> extractLines,int[][] imgGrays,int defaultVal){
		double ratio = 0.0, bestRsq = 0.0, offsetDelta = 0.0, off2 = 0.0, ratio2 = 0.0, ratio3 = 0.0;
		double refOffset = 0.0, lastOffset = 0.0, nextOffset = -1.0, tmpOffset = 0.0;
		int bestIdx = 0, refValidPts = 0, lastFoundIdx = -1, size = 0, nextIdx = 0;
		int scanRng = lineScanRange/2, offsetCmpFlag = 0, imgWidth = imgGrays.length;
		boolean swapPoleOffset = false;
		ArrayList<ImgExtractLine> adjLines1 = new ArrayList<ImgExtractLine>();
		ArrayList<ImgExtractLine> adjLines2 = new ArrayList<ImgExtractLine>();
		ArrayList<ImgExtractLine> adjustLines = new ArrayList<ImgExtractLine>();
		ArrayList<ImgExtractLine> adjLines = new ArrayList<ImgExtractLine>();
		ImgExtractLine tmpLine = null, lastLine = null;
		
		//Step_1:Adjust the first found lines
		for(int idx=0; idx<allPossibleLines.size(); idx++){
			if(3!=allPossibleLines.get(idx).getLineFlag()) continue;
			refValidPts = allPossibleLines.get(idx).getValidPoints();
			refOffset = allPossibleLines.get(idx).getLineIntercept();
			bestRsq = allPossibleLines.get(idx).getLineRSQ();
			bestIdx = idx;
			for(int i=-scanRng; i<=scanRng; i++){
				if(idx+i<0 || idx+i>=allPossibleLines.size()) continue;
				tmpLine = allPossibleLines.get(idx+i);
				if(null!=tmpLine && Math.abs(refOffset-tmpLine.getLineIntercept())<5){
					ratio = (double)tmpLine.getValidPoints()/refValidPts;
					if(ratio>0.95 && bestRsq<tmpLine.getLineRSQ()){
						bestRsq = tmpLine.getLineRSQ();
						bestIdx = idx+i;
					}
				}
			}
			tmpLine = allPossibleLines.get(bestIdx);
			offsetDelta = tmpLine.getLineIntercept();
			if(null!=lastLine){
				offsetDelta -= lastLine.getLineIntercept();
				if(offsetDelta<=5){
					adjLines1.remove(adjLines1.size()-1);
					lastLine = null;
				}
			}
			if(null==lastLine || null!=lastLine && offsetDelta>5){
				if(0==tmpLine.getXAxisStart()) tmpLine = setLineBoundary(tmpLine, imgGrays, defaultVal, 0.3);
				adjLines1.add(tmpLine);
				lastLine = tmpLine;
			}
		}
		
		//Step_2:Get all potential lines via peak searching method
		adjLines2 = adjustAllSearchedLinesEx(allPossibleLines,imgGrays,defaultVal);
		adjLines2 = doubleCheckAllPotentialLines(adjLines2,allPossibleLines,imgGrays);
		double[] poleOffset = getPoleOffset(adjLines2,targetLinesQty);
		
		//Step_3:Combine all lines from Step_1 and Step_2
		lastFoundIdx = -1;
		if(adjLines2.isEmpty()){
			for(int i=0; i<adjLines1.size(); i++){
				adjustLines.add(adjLines1.get(i));
			}
		}else{
			for(int i=0; i<adjLines1.size(); i++){//First found lines are more reliable(they are the base)
				bestIdx = -1;
				tmpLine = adjLines1.get(i);
				refOffset = tmpLine.getLineIntercept();
				
				if(2==adjustLines.size()){
					System.out.print("");
				}
				
				for(int j=lastFoundIdx+1; j<adjLines2.size(); j++){
					//Double confirm in all potential lines
					if(Math.abs(refOffset-adjLines2.get(j).getLineIntercept())<3){
						bestIdx = j;
						break;
					}
					if(adjLines2.get(j).getLineIntercept()-refOffset>3) break;
				}
				
				//TODO Verification
				if(bestIdx<0 && adjustLines.isEmpty() && adjLines2.size()>0){
					//Double check the most outer line
					double[] offs = twoLinesAvgOffset(tmpLine.getLineSlope(),tmpLine.getLineIntercept(),adjLines2.get(0).getLineSlope(),adjLines2.get(0).getLineIntercept(),0.0,imgGrays.length);
					if(offs[0]<3 && offs[1]<1) continue;
				}
				
				if(bestIdx>=0){
					if(lastFoundIdx<0 && bestIdx>0){//Suppose adjLines2 is reliable
						for(int j=0; j<bestIdx; j++){
							adjustLines.add(adjLines2.get(j));
							lastFoundIdx = j;
						}
					}
					
					//Found in potential lines
					if(lastFoundIdx>=0 && bestIdx-lastFoundIdx>1){
						//Confirm the missed potential lines
						if(adjustLines.size()>0) refOffset = adjustLines.get(adjustLines.size()-1).getLineIntercept();
						for(int k=(lastFoundIdx+1);k<bestIdx;k++){
							if(adjLines2.get(k).getLineIntercept()<refOffset) continue;
							lastOffset = refOffset;
							if(k>0) lastOffset = adjLines2.get(k-1).getLineIntercept();
							offsetDelta = adjLines2.get(k+1).getLineIntercept()-adjLines2.get(k).getLineIntercept();
							ratio = (double)adjLines2.get(k).getValidPtsInRange((int)(imgWidth*0.6), imgWidth, 1)/adjLines2.get(k).getValidPoints();
							
							if((adjLines2.get(k).getLineIntercept()-lastOffset>=5 || k<2 && adjLines2.get(k).getLineIntercept()-lastOffset>=3)
								&& offsetDelta>=5 || k<2 && ratio>0.4){
								ratio = (double)adjLines2.get(k).getValidPoints()/imgGrays.length;
								if(ratio>=minValidRatio){
									adjustLines.add(adjLines2.get(k));
								}else{
									off2 = adjLines2.get(k).getLineIntercept()-lastOffset;
									ratio = offsetDelta/off2;
									if(ratio<minOffsetChgRate || ratio>maxOffsetChgRate
										|| offsetDelta>5 && off2>5) adjustLines.add(adjLines2.get(k));
								}
							}
						}
					}
					lastFoundIdx = bestIdx;
					tmpLine = adjLines2.get(bestIdx);
				}else if(adjustLines.size()>0 && refOffset>adjLines2.get(0).getLineIntercept()){
					//Not found in potential lines(after the 1st found line)
					if(lastFoundIdx+1<adjLines2.size()){
						offsetDelta = refOffset-adjLines2.get(lastFoundIdx+1).getLineIntercept();
						if(offsetDelta>5){//First found line is behind the potential lines
							for(int j=lastFoundIdx+1; j<adjLines2.size(); j++){
								if(refOffset-adjLines2.get(j).getLineIntercept()>5){
									adjustLines.add(adjLines2.get(j));
									lastFoundIdx = j;
								}else{
									break;
								}
							}
						}else if(offsetDelta<=5){//First found line is very closed to the potential lines
							for(int j=lastFoundIdx+1; j<adjLines2.size(); j++){
								if(refOffset>adjLines2.get(j).getLineIntercept()){//Fetch the potential lines
									adjustLines.add(adjLines2.get(j));
									lastFoundIdx = j;
								}else{
									break;
								}
							}
						}
					}
					
					//Get next potential line
					nextOffset = -1.0; nextIdx = -1;
					for(int j=lastFoundIdx+1; j<adjLines2.size(); j++){
						if(adjLines2.get(j).getLineIntercept()>refOffset){
							nextOffset = adjLines2.get(j).getLineIntercept();
							nextIdx = j;
							break;
						}
					}
					
					//Offset change rate of the first found line
					ratio = -1.0; lastOffset = 0.0; ratio2 = -1.0;
					size = adjustLines.size();
					if(size>0){
						lastOffset = adjustLines.get(size-1).getLineIntercept();
						if(size>2){//Refer to previous data
							ratio = (refOffset - lastOffset)/(adjustLines.get(size-2).getLineIntercept()-adjustLines.get(size-3).getLineIntercept());
							ratio2 = (nextOffset - lastOffset)/(adjustLines.get(size-2).getLineIntercept()-adjustLines.get(size-3).getLineIntercept());
						}else if(nextIdx>0 && nextIdx+3<adjLines2.size()){
							ratio = (refOffset - lastOffset)/(adjLines2.get(nextIdx+3).getLineIntercept()-adjLines2.get(nextIdx+2).getLineIntercept());
							ratio2 = (nextOffset - lastOffset)/(adjLines2.get(nextIdx+3).getLineIntercept()-adjLines2.get(nextIdx+2).getLineIntercept());
						}else{
							if(1==adjustLines.size()%2){//Supposed the first found line is a missing potential line
								if(poleOffset[1]>0){
									ratio = (refOffset - lastOffset)/poleOffset[1];
									ratio2 = (nextOffset - lastOffset)/poleOffset[1];
								}
							}else{
								if(poleOffset[0]>0){
									ratio = (refOffset - lastOffset)/poleOffset[0];
									ratio2 = (nextOffset - lastOffset)/poleOffset[0];
								}
							}
						}
					}
					
					swapPoleOffset = true;
					offsetCmpFlag = offsetInSamePeak(allPossibleLines,refOffset,nextOffset,lastOffset);
					
					if(-1==offsetCmpFlag){
						continue;//Skip because the first found line is not in a valid peak region
					}else if(2==offsetCmpFlag){
						continue;//Skip because the first found line is in the valid peak region of last line
					}else if(1==offsetCmpFlag){
						tmpLine = adjLines2.get(nextIdx);//Replace the first found line with next potential line
						lastFoundIdx = nextIdx;
						swapPoleOffset = false;
					}else if(0==offsetCmpFlag){
						if(!(adjustLines.size()>2 && ratio>minOffsetChgRate)){
							ratio3 = (double)tmpLine.getValidPoints()/imgGrays.length;
							if(ratio3<minValidRatio) continue;
						}
						if(ratio<maxOffsetChgRate*0.6 && ratio2>0 && ratio2<maxOffsetChgRate) continue;
					}
					
					if(swapPoleOffset){
						tmpOffset = poleOffset[0];
						poleOffset[0] = poleOffset[1];
						poleOffset[1] = tmpOffset;
					}
				}else if(refOffset>adjLines2.get(0).getLineIntercept()){
					for(int j=0; j<adjLines2.size(); j++){
						if(refOffset>adjLines2.get(j).getLineIntercept()){
							adjustLines.add(adjLines2.get(j));
							lastFoundIdx = j;
						}else{
							break;
						}
					}
					i = i - 1;
					continue;
				}
				adjustLines.add(tmpLine);
			}
		}
		
		//Step_4:Check whether all layers are found
		if(adjustLines.size()<targetLinesQty){
			refOffset = 0.0;
			if(adjustLines.size()>0) refOffset = adjustLines.get(adjustLines.size()-1).getLineIntercept();
			for(int j=lastFoundIdx+1; j<adjLines2.size(); j++){
				if(adjLines2.get(j).getLineIntercept()>refOffset && adjustLines.size()<targetLinesQty){
					adjustLines.add(adjLines2.get(j));
				}
			}
		}
		
		LinkedHashMap<Double,Double> tendency = new LinkedHashMap<Double,Double>();
		double[] coef = null;
		double avgXStart = 0.0;
		int avgXStartCnt = 0, maxXStart = 0;
		for(int i=0; i<adjustLines.size(); i++){
			tmpLine = adjustLines.get(i);
			tmpLine.setLineIndex(i);
			tendency.put((double)i, tmpLine.getLineIntercept());
			if(tendency.size()<3){
				tmpLine.setTendencyRSQ(1.0);
			}else{
				coef = MathUtils.lineFitting(tendency);
				tmpLine.setTendencyRSQ(coef[2]);
			}
			if(0==tmpLine.getXAxisStart()) tmpLine = setLineBoundary(tmpLine, imgGrays, 255, 0.3);
			
			//TODO Verification
			if(i<(targetLinesQty-3)) tmpLine.setLineChecked(true);
			
			adjLines.add(tmpLine);
			allPotentialLines.add(tmpLine);
			
			if(maxXStart<tmpLine.getXAxisStart()) maxXStart = tmpLine.getXAxisStart();
			avgXStart += tmpLine.getXAxisStart();
			avgXStartCnt++;
		}
		
		//Step_5: Correct abnormal boundary
		if(avgXStartCnt>1){
			avgXStart = (avgXStart - maxXStart) / (avgXStartCnt - 1);
			for(int i=0; i<adjLines.size(); i++){
				tmpLine = adjLines.get(i);
				if((imgWidth - tmpLine.getXAxisStart()) / (imgWidth - avgXStart)<0.5) tmpLine = setLineBoundary(tmpLine, imgGrays, defaultVal, 0.4);
			}
		}
		
		//Add all potential lines
		if(adjLines2.size()>lastFoundIdx+1){
			refOffset = allPotentialLines.get(allPotentialLines.size()-1).getLineIntercept();
			for(int j=lastFoundIdx+1; j<adjLines2.size(); j++){
				if(adjLines2.get(j).getLineIntercept()>refOffset){
					allPotentialLines.add(adjLines2.get(j));
				}
			}
		}
		
		if(logEnabled){
			for(int i=0; i<adjustLines.size(); i++){
				tmpLine = adjustLines.get(i);
				LogUtils.rawLog("adjustLineParas_", tmpLine.printLineParas(i,0==i?true:false));
			}
			for(int i=0; i<adjLines2.size(); i++){
				tmpLine = adjLines2.get(i);
				LogUtils.rawLog("adjustLineParas_", tmpLine.printLineParas(i,false));
			}
			for(int i=0; i<extractLines.size(); i++){
				tmpLine = extractLines.get(i);
				LogUtils.rawLog("adjustLineParas_", tmpLine.printLineParas(i,false));
			}
			for(int i=0; i<allPossibleLines.size(); i++){
				tmpLine = allPossibleLines.get(i);
				LogUtils.rawLog("searchLineParas_", tmpLine.printLineParas(tmpLine.getLineFlag(),0==i?true:false));
			}
			logSelectedLinesGray(adjustLines,imgGrays,5);
		}
		
		return adjLines;
	}
	
	private double[] getLineGrayTrend(ImgExtractLine currLine, int[][] imgGrays, int yOffset){
		double[] trendParas = new double[]{0.0,0.0,0.0};
		double lineSlope = 0.0, lineOffset = 0.0;
		ArrayList<Double> xVals = new ArrayList<Double>();
		ArrayList<Double> yVals = new ArrayList<Double>();
		int startX = (int)(imgGrays.length * 0.5), stopX = imgGrays.length, imgHeight = imgGrays[0].length;
		int col = 0, grayVal = 0;
		
		if(null!=currLine){
			lineSlope = currLine.getLineSlope();
			lineOffset = currLine.getLineIntercept() + currLine.getLineInterceptDelta();
			for(int i=startX; i<stopX; i++){
				col = (int)(lineSlope * i + lineOffset + yOffset);
				if(col>=0 && col<imgHeight){
					grayVal = imgGrays[i][col];
				}else{
					grayVal = 255;
				}
				xVals.add((double)i); yVals.add((double)grayVal);
			}
			if(xVals.size()>2){
				trendParas = MathUtils.lineFitting(xVals, yVals, 0, xVals.size()-1);
			}
		}
		
		return trendParas;
	}
	
	/**
	 * 
	 * @param tmpLine The target line for gray trend calculation
	 * @param imgGrays Gray matrix of the picture
	 * @return The gray trend(both rough and details) indexes from outer to inner side
	 */
	private ArrayList<Object> getLineGrayTrendRawData(ImgExtractLine tmpLine, int[][] imgGrays){
		int imgWidth = imgGrays.length, imgHeight = imgGrays[0].length, col = 0, grayVal = 0;
		int roughTrendPts = 30, detailsTrendPts = 10, rawDtPts = (int)(imgWidth*0.75+roughTrendPts);
		ArrayList<Object> trendRawData = new ArrayList<Object>();
		double lineSlope = 0.0, lineOffset = 0.0, crossAngle = 0.0;
		double[] coef1 = null, coef2 = null;
		double[] rawData1 = new double[rawDtPts];
		double[] rawData2 = new double[rawDtPts];
		
		ArrayList<Double> xVals1 = new ArrayList<Double>();
		ArrayList<Double> yVals1 = new ArrayList<Double>();
		ArrayList<Double> xVals2 = new ArrayList<Double>();
		ArrayList<Double> yVals2 = new ArrayList<Double>();
		
		lineSlope = tmpLine.getLineSlope();
		lineOffset = tmpLine.getLineIntercept();
		coef2 = getLineGrayTrend(tmpLine, imgGrays, 0);
		
		for(int k=0; k<rawDtPts; k++){
			if(k>=imgWidth) break;
			col = (int)(lineSlope * k + lineOffset);
			if(col>=0 && col<imgHeight){
				grayVal = imgGrays[k][col];
			}else{
				grayVal = 255;
			}
			
			if(xVals1.size()<roughTrendPts){
				xVals1.add((double)k); yVals1.add((double)grayVal);
			}else{
				for(int n=0; n<(roughTrendPts-1); n++){
					xVals1.set(n, xVals1.get(n+1));
					yVals1.set(n, yVals1.get(n+1));
				}
				xVals1.set(roughTrendPts-1, (double)k);
				yVals1.set(roughTrendPts-1, (double)grayVal);
			}
			if(xVals1.size()>=roughTrendPts){
				coef1 = MathUtils.lineFitting(xVals1, yVals1, 0, roughTrendPts-1);
				if(-1!=coef1[0]*coef2[0]){
					crossAngle = Math.abs((coef1[0]-coef2[0])/(1+coef1[0]*coef2[0]));
					crossAngle = Math.toDegrees(Math.atan(crossAngle));
				}else{
					crossAngle = 90.0;
				}
			}else{
				crossAngle = 0.0;
			}
			rawData1[k] = crossAngle;
			
			if(xVals2.size()<detailsTrendPts){
				xVals2.add((double)k); yVals2.add((double)grayVal);
			}else{
				for(int n=0; n<(detailsTrendPts-1); n++){
					xVals2.set(n, xVals2.get(n+1));
					yVals2.set(n, yVals2.get(n+1));
				}
				xVals2.set(detailsTrendPts-1, (double)k);
				yVals2.set(detailsTrendPts-1, (double)grayVal);
			}
			if(xVals2.size()>=detailsTrendPts){
				coef1 = MathUtils.lineFitting(xVals2, yVals2, 0, detailsTrendPts-1);
				if(-1!=coef1[0]*coef2[0]){
					crossAngle = Math.abs((coef1[0]-coef2[0])/(1+coef1[0]*coef2[0]));
					crossAngle = Math.toDegrees(Math.atan(crossAngle));
				}else{
					crossAngle = 90.0;
				}
			}else{
				crossAngle = 0.0;
			}
			rawData2[k] = crossAngle;
		}
		
		trendRawData.add(0, rawData1);
		trendRawData.add(1, rawData2);
		
		return trendRawData;
	}
	
	private void logSelectedLinesGray(ArrayList<ImgExtractLine> selectedLines, int[][] imgGrays, int grayScanRng){
		ImgExtractLine tmpLine = null;
		int imgWidth = imgGrays.length, imgHeight = imgGrays[0].length, col = 0, grayVal = 0, xStart = 0;
		int roughTrendPts = 30, detailsTrendPts = 10;
		double lineSlope = 0.0, lineOffset = 0.0, crossAngle = 0.0;
		double[] coef1 = null, coef2 = null;
		ArrayList<Double> xVals1 = new ArrayList<Double>();
		ArrayList<Double> yVals1 = new ArrayList<Double>();
		ArrayList<Double> xVals2 = new ArrayList<Double>();
		ArrayList<Double> yVals2 = new ArrayList<Double>();
		
		String title = "LineIdx,Offset,Col,xStop,xStart,crossAg,X", data = "", dt2 = "", dt3 = "";
		if(null!=selectedLines && selectedLines.size()>0){
			for(int i=0; i<imgWidth; i++){
				title += "," + i;
			}
			LogUtils.rawLog("selectedLinesXY_",title);
			for(int i=0; i<selectedLines.size(); i++){
				tmpLine = selectedLines.get(i);
				lineSlope = tmpLine.getLineSlope(); lineOffset = tmpLine.getLineIntercept(); xStart = tmpLine.getXAxisStart();
				for(int j=-grayScanRng; j<=grayScanRng; j++){
					coef2 = getLineGrayTrend(tmpLine, imgGrays, j);
					
					xVals1.clear();yVals1.clear();
					for(int k=xStart-detailsTrendPts; k<xStart; k++){
						if(k<0) continue;
						col = (int)(lineSlope * k + lineOffset + j);
						if(col>=0 && col<imgHeight){
							grayVal = imgGrays[k][col];
						}else{
							grayVal = 255;
						}
						xVals1.add((double)k); yVals1.add((double)grayVal);
					}
					if(xVals1.size()>2){
						coef1 = MathUtils.lineFitting(xVals1, yVals1, 0, xVals1.size()-1);
						if(-1!=coef1[0]*coef2[0]){
							crossAngle = Math.abs((coef1[0]-coef2[0])/(1+coef1[0]*coef2[0]));
							crossAngle = Math.toDegrees(Math.atan(crossAngle));
						}else{
							crossAngle = 90.0;
						}
					}else{
						crossAngle = 0.0;
					}
					
					data = i + "," + lineOffset + "," + j + "," + tmpLine.getXAxisStop() + "," + tmpLine.getXAxisStart() + "," + crossAngle + ",Y";
					dt2 = data; dt3 = data;
					
					xVals1.clear();yVals1.clear();xVals2.clear();yVals2.clear();
					for(int k=0; k<imgWidth; k++){
						col = (int)(lineSlope * k + lineOffset + j);
						if(col>=0 && col<imgHeight){
							grayVal = imgGrays[k][col];
						}else{
							grayVal = 255;
						}
						data += "," + grayVal;
						
						if(xVals1.size()<roughTrendPts){
							xVals1.add((double)k); yVals1.add((double)grayVal);
						}else{
							for(int n=0; n<(roughTrendPts-1); n++){
								xVals1.set(n, xVals1.get(n+1));
								yVals1.set(n, yVals1.get(n+1));
							}
							xVals1.set(roughTrendPts-1, (double)k);
							yVals1.set(roughTrendPts-1, (double)grayVal);
						}
						if(xVals1.size()>=roughTrendPts){
							coef1 = MathUtils.lineFitting(xVals1, yVals1, 0, roughTrendPts-1);
							if(-1!=coef1[0]*coef2[0]){
								crossAngle = Math.abs((coef1[0]-coef2[0])/(1+coef1[0]*coef2[0]));
								crossAngle = Math.toDegrees(Math.atan(crossAngle));
							}else{
								crossAngle = 90.0;
							}
						}else{
							crossAngle = 0.0;
						}
						if(k<roughTrendPts){
							dt2 += ",0";
						}else{
							dt2 += ","+crossAngle;
						}
						
						if(xVals2.size()<detailsTrendPts){
							xVals2.add((double)k); yVals2.add((double)grayVal);
						}else{
							for(int n=0; n<(detailsTrendPts-1); n++){
								xVals2.set(n, xVals2.get(n+1));
								yVals2.set(n, yVals2.get(n+1));
							}
							xVals2.set(detailsTrendPts-1, (double)k);
							yVals2.set(detailsTrendPts-1, (double)grayVal);
						}
						if(xVals2.size()>=detailsTrendPts){
							coef1 = MathUtils.lineFitting(xVals2, yVals2, 0, detailsTrendPts-1);
							if(-1!=coef1[0]*coef2[0]){
								crossAngle = Math.abs((coef1[0]-coef2[0])/(1+coef1[0]*coef2[0]));
								crossAngle = Math.toDegrees(Math.atan(crossAngle));
							}else{
								crossAngle = 90.0;
							}
						}else{
							crossAngle = 0.0;
						}
						if(k<detailsTrendPts){
							dt3 += ",0";
						}else{
							dt3 += ","+crossAngle;
						}
					}
					
					LogUtils.rawLog("selectedLinesXY_",data);
					if(0==j){
						LogUtils.rawLog("selectedLinesXY_",dt2);
						LogUtils.rawLog("selectedLinesXY_",dt3);
					}
				}
			}
		}
	}
	
	private boolean offsetInPeakRegion(ArrayList<ImgExtractLine> allPossibleLines, double chkOffset){
		boolean inPeakRegion = false;
		double minDelta = 0.0, offsetDelta = 0.0, ratio = 0.0;
		int mostCloseIdx = -1, scanRng = lineScanRange/2, counter = 0, minValidPts = 0;
		int size = allPossibleLines.size(), peakIdx = -1, maxValidPts = 0;
		
		if(size>0){
			//Get the index of chkOffset in allPossibleLines
			minDelta = Math.abs(allPossibleLines.get(0).getLineIntercept()-chkOffset);
			mostCloseIdx = 0;
			for(int i=1; i<allPossibleLines.size(); i++){
				offsetDelta = Math.abs(allPossibleLines.get(i).getLineIntercept()-chkOffset);
				if(minDelta>offsetDelta){
					minDelta = offsetDelta;
					mostCloseIdx = i;
				}
			}
			
			//Fine-tune the index of chkOffset
			peakIdx = -1; maxValidPts = 0;
			for(int i=-scanRng; i<=scanRng; i++){
				if(mostCloseIdx+i>=0 && mostCloseIdx+i<size){
					offsetDelta = Math.abs(allPossibleLines.get(mostCloseIdx+i).getLineIntercept()-chkOffset);
					if(offsetDelta<3){
						if(maxValidPts<allPossibleLines.get(mostCloseIdx+i).getValidPoints()){
							maxValidPts = allPossibleLines.get(mostCloseIdx+i).getValidPoints();
							peakIdx = mostCloseIdx+i;
						}
					}
				}
			}
			
			if(peakIdx>=0){
				//Possibility of being in the peak region
				counter = 0;
				for(int i=scanRng; i>0; i--){
					if(peakIdx-i>=0 && peakIdx+i<size){
						if(allPossibleLines.get(peakIdx).getValidPoints()>=allPossibleLines.get(peakIdx-i).getValidPoints()
							&& allPossibleLines.get(peakIdx).getValidPoints()>=allPossibleLines.get(peakIdx+i).getValidPoints()){
							counter++;
						}
					}
				}
				ratio = (double)counter/scanRng;
				if(ratio>0.66){
					//Check whether current peakIdx represents a valid peak or not
					maxValidPts = allPossibleLines.get(peakIdx).getValidPoints();
					minValidPts = maxValidPts;
					ratio = 0.0;
					//Get the base&max value in last 10 points
					for(int i=peakIdx; i>(peakIdx-10); i--){
						if(i<0) break;
						if(maxValidPts<allPossibleLines.get(i).getValidPoints()) maxValidPts = allPossibleLines.get(i).getValidPoints();
						if(minValidPts>allPossibleLines.get(i).getValidPoints()) minValidPts = allPossibleLines.get(i).getValidPoints();
					}
					if(maxValidPts>minValidPts) ratio = (double)(allPossibleLines.get(peakIdx).getValidPoints()-minValidPts)/(maxValidPts-minValidPts);
					if(ratio>0.2) inPeakRegion = true;
				}
			}
		}
		return inPeakRegion;
	}
	
	private int offsetInSamePeak(ArrayList<ImgExtractLine> allPossibleLines, double chkOffset, double nextOffset, double lastOffset){
		int rsltFlag = -1;
		double minDelta0 = 0.0, offsetDelta = 0.0, minDelta1 = 0.0, minDelta2 = 0.0;
		int chkIdx = -1, nextIdx = -1, lastIdx = -1;
		int size = allPossibleLines.size();
		int[] validPts = null, idx1 = null, idx2 = null;
		
		if(size>0 && chkOffset>0 && (nextOffset>0 || lastOffset>0)){
			minDelta0 = Math.abs(allPossibleLines.get(0).getLineIntercept()-chkOffset);
			minDelta1 = Math.abs(allPossibleLines.get(0).getLineIntercept()-nextOffset);
			minDelta2 = Math.abs(allPossibleLines.get(0).getLineIntercept()-lastOffset);
			for(int i=1; i<allPossibleLines.size(); i++){
				offsetDelta = Math.abs(allPossibleLines.get(i).getLineIntercept()-chkOffset);
				if(minDelta0>offsetDelta){
					minDelta0 = offsetDelta;
					chkIdx = i;
				}
				
				if(nextOffset>0){
					offsetDelta = Math.abs(allPossibleLines.get(i).getLineIntercept()-nextOffset);
					if(minDelta1>offsetDelta){
						minDelta1 = offsetDelta;
						nextIdx = i;
					}
				}
				
				if(lastOffset>0){
					offsetDelta = Math.abs(allPossibleLines.get(i).getLineIntercept()-lastOffset);
					if(minDelta2>offsetDelta){
						minDelta2 = offsetDelta;
						lastIdx = i;
					}
				}
			}
			
			validPts = getPossibleLinesValidDtPts(allPossibleLines);
			idx1 = MathUtils.getPeakCenterIndex(validPts, chkIdx, lineScanRange, true);
			if(idx1[0]>=0){
				rsltFlag = 0;
				if(nextIdx>0){
					idx2 = MathUtils.getPeakCenterIndex(validPts, nextIdx, lineScanRange, true);
					if(idx2[0]>=0){
						if(Math.abs(idx2[0]-idx1[0])<=lineScanRange/2){
							rsltFlag = 1;
						}else if(idx2[0]<allPossibleLines.size() && idx1[0]<allPossibleLines.size()){
							if(allPossibleLines.get(idx2[0]).getLineIntercept()-allPossibleLines.get(idx1[0]).getLineIntercept()<=lineScanRange/2) rsltFlag = 1;
						}
					}
				}
				if(0==rsltFlag && lastIdx>0){
					idx2 = MathUtils.getPeakCenterIndex(validPts, lastIdx, lineScanRange, true);
					if(idx2[0]>=0){
						if(Math.abs(idx2[0]-idx1[0])<=lineScanRange/2){
							rsltFlag = 2;
						}else if(idx2[0]<allPossibleLines.size() && idx1[0]<allPossibleLines.size()){
							if(allPossibleLines.get(idx1[0]).getLineIntercept()-allPossibleLines.get(idx2[0]).getLineIntercept()<=lineScanRange/2) rsltFlag = 2;
						}
					}
				}
			}
		}
		return rsltFlag;
	}
	
	private int offsetInSamePeakEx(ArrayList<ImgExtractLine> allPossibleLines, double offset1, double offset2){
		int rsltFlag = -1;//Not found in all possible lines
		double minDelta0 = 0.0, offsetDelta = 0.0, minDelta1 = 0.0;
		int index1 = -1, index2 = -1;
		int size = allPossibleLines.size();
		int[] validPts = null, idx1 = null, idx2 = null;
		
		if(size>0 && offset1>0 && offset2>0){
			minDelta0 = Math.abs(allPossibleLines.get(0).getLineIntercept()-offset1);
			minDelta1 = Math.abs(allPossibleLines.get(0).getLineIntercept()-offset2);
			for(int i=1; i<allPossibleLines.size(); i++){
				offsetDelta = Math.abs(allPossibleLines.get(i).getLineIntercept()-offset1);
				if(minDelta0>offsetDelta){
					minDelta0 = offsetDelta;
					index1 = i;
				}
				
				offsetDelta = Math.abs(allPossibleLines.get(i).getLineIntercept()-offset2);
				if(minDelta1>offsetDelta){
					minDelta1 = offsetDelta;
					index2 = i;
				}
			}
			
			validPts = getPossibleLinesValidDtPts(allPossibleLines);
			idx1 = MathUtils.getPeakCenterIndex(validPts, index1, lineScanRange, true);
			if(idx1[0]>=0){
				rsltFlag = 0;//offset1 is found
				if(index2>0){
					idx2 = MathUtils.getPeakCenterIndex(validPts, index2, lineScanRange, true);
					if(idx2[0]>=0){
						if(Math.abs(idx2[0]-idx1[0])<=lineScanRange/2){
							rsltFlag = 1;//offset1 and offset2 is in same peak
						}else if(idx2[0]<allPossibleLines.size() && idx1[0]<allPossibleLines.size()){
							if(Math.abs(allPossibleLines.get(idx2[0]).getLineIntercept()-allPossibleLines.get(idx1[0]).getLineIntercept())<=lineScanRange/2) rsltFlag = 1;
						}
						
						if(0==rsltFlag && Math.abs(idx2[1]-idx1[1])<=2){
							rsltFlag = 2;//Left edge of offset1 and offset2 is the same
						}
					}
				}
			}
		}
		return rsltFlag;
	}
	
	private int[] getPossibleLinesValidDtPts(ArrayList<ImgExtractLine> allPossibleLines){
		int[] lineValidDataPts = new int[allPossibleLines.size()];
		for(int i=0; i<allPossibleLines.size(); i++){
			lineValidDataPts[i] = allPossibleLines.get(i).getValidPoints();
		}
		return lineValidDataPts;
	}
	
	private ArrayList<ImgExtractLine> roughCheckAllPotentialLines(ArrayList<ImgExtractLine> potentialLines){
		ArrayList<ImgExtractLine> newPotentialLines = new ArrayList<ImgExtractLine>();
		double currOffset = 0.0, nextOffset = 0.0;
		
		if(null!=potentialLines && potentialLines.size()>0){
			if(potentialLines.size()>1){
				for(int i=0; i<potentialLines.size()-1; i++){
					currOffset = potentialLines.get(i).getLineIntercept();
					nextOffset = potentialLines.get(i+1).getLineIntercept();
					if(currOffset>0 && currOffset<nextOffset){
						newPotentialLines.add(potentialLines.get(i));
					}
				}
				if(currOffset>0 && currOffset<nextOffset){
					newPotentialLines.add(potentialLines.get(potentialLines.size()-1));
				}
			}else{
				newPotentialLines = potentialLines;
			}
		}
		
		return newPotentialLines;
	}
	
	private ArrayList<ImgExtractLine> doubleCheckAllPotentialLines(ArrayList<ImgExtractLine> potentialLines, ArrayList<ImgExtractLine> allPossibleLines, int[][] imgGrays){
		ArrayList<ImgExtractLine> newPotentialLines = new ArrayList<ImgExtractLine>();
		int imgWidth = imgGrays.length;
		
		potentialLines = roughCheckAllPotentialLines(potentialLines);
		if(null!=potentialLines && potentialLines.size()>0){
			int size = potentialLines.size(), newSize = 0;
			int currLineIdx = 0, lastLineIdx = 0;
			int peakFlags = -1;
			double ratio = 0.0;
			ImgExtractLine exLine = null;
			if(size<=1){
				newPotentialLines = potentialLines;
			}else{
				newPotentialLines.add(potentialLines.get(0));
				for(int i=1; i<size; i++){
					newSize = newPotentialLines.size();
					if(13==newSize){
						System.out.print("");
					}
					lastLineIdx = getLineIndex(allPossibleLines,newPotentialLines.get(newSize-1).getLineIntercept());
					currLineIdx = getLineIndex(allPossibleLines,potentialLines.get(i).getLineIntercept());
					if(lastLineIdx>0 && currLineIdx>lastLineIdx){
						ratio = potentialLines.get(i).getLineIntercept()-newPotentialLines.get(newSize-1).getLineIntercept();
						if(newSize>2){//Check offset change rate of current line - see whether need to skip current line
							ratio = ratio/(newPotentialLines.get(newSize-2).getLineIntercept()-newPotentialLines.get(newSize-3).getLineIntercept());
							if(ratio<maxOffsetChgRate*0.6){//Current line is very closed to last line
								ratio = (double)potentialLines.get(i).getValidPoints()/newPotentialLines.get(newSize-1).getValidPoints();
								if(ratio<0.2) continue;//Valid data points of current line is out of expectation
								
								peakFlags = offsetInSamePeakEx(allPossibleLines,newPotentialLines.get(newSize-1).getLineIntercept(),potentialLines.get(i).getLineIntercept());
								if(peakFlags>=1) continue;//Current line and last line is in the same peak
								
								if(i+1<size && newSize>2){//Double check next line
									ratio = potentialLines.get(i+1).getLineIntercept()-newPotentialLines.get(newSize-1).getLineIntercept();
									ratio = ratio/(newPotentialLines.get(newSize-2).getLineIntercept()-newPotentialLines.get(newSize-3).getLineIntercept());
									if(ratio<maxOffsetChgRate){
										if(ratio<maxOffsetChgRate*0.6){
											continue;//Next line is very close to last line, so skip current line
										}else if(newSize>4){
											ratio = potentialLines.get(i+1).getLineIntercept()-newPotentialLines.get(newSize-1).getLineIntercept();
											ratio = ratio/(newPotentialLines.get(newSize-4).getLineIntercept()-newPotentialLines.get(newSize-5).getLineIntercept());
											if(ratio<maxOffsetChgRate) continue;//Offset change rate btw next and last line lower than upper control limit is true
										}
									}
								}
							}
						}else{
							if(ratio<lineScanRange/2) continue;
						}
						
						//Check whether there is line btw current and last line
						exLine = getPotentialLineBtw(allPossibleLines,lastLineIdx,currLineIdx,newPotentialLines.get(newSize-1).getValidPoints());
						
						if(null!=exLine){
							ratio = (double)exLine.getValidPtsInRange((int)(imgWidth*0.6), imgWidth, 1)/exLine.getValidPoints();
							if(ratio>=0.1){
								ratio = exLine.getLineIntercept()-newPotentialLines.get(newSize-1).getLineIntercept();
								if(newSize>2){
									ratio = ratio/(newPotentialLines.get(newSize-2).getLineIntercept()-newPotentialLines.get(newSize-3).getLineIntercept());
								}
								if(ratio>minOffsetChgRate) newPotentialLines.add(exLine);
							}
						}
					}
					newPotentialLines.add(potentialLines.get(i));
				}
			}
		}
		
		return newPotentialLines;
	}
	
	private ImgExtractLine getPotentialLineBtw(ArrayList<ImgExtractLine> allPossibleLines,int startIdx,int stopIdx,int refValidDataPts){
		ImgExtractLine exLine = null;
		int validPts = 0, maxValidPts = -1, maxIdx = -1, peakFlag = 0;
		double ratio = 0.0, startOffset = 0.0, stopOffset = 0.0, foundOffset = 0.0;
		boolean startCheck = false, lineFound = false;
		if(null!=allPossibleLines && startIdx>0 && stopIdx>startIdx && stopIdx<allPossibleLines.size() && refValidDataPts>0){
			startOffset = allPossibleLines.get(startIdx).getLineIntercept();
			stopOffset = allPossibleLines.get(stopIdx).getLineIntercept();
			
			for(int i=startIdx; i<=stopIdx; i++){
				validPts = allPossibleLines.get(i).getValidPoints();
				ratio = (double)validPts/refValidDataPts;
				if(ratio<0.15){
					if(!startCheck){
						startCheck = true;
						maxValidPts = validPts;
						maxIdx = i;
					}else{
						ratio = (double)maxValidPts/refValidDataPts;
						if(ratio>0.2){
							lineFound = true;
							break;
						}
					}
				}
				if(startCheck){
					if(maxValidPts<validPts){
						maxValidPts = validPts;
						maxIdx = i;
					}
				}
			}
			
			if(lineFound){
				foundOffset = allPossibleLines.get(maxIdx).getLineIntercept();
				peakFlag = offsetInSamePeakEx(allPossibleLines,startOffset,foundOffset);
				if(0==peakFlag) peakFlag = offsetInSamePeakEx(allPossibleLines,foundOffset,stopOffset);
				if(0==peakFlag) exLine = allPossibleLines.get(maxIdx);
			}
		}
		
		return exLine;
	}
	
	private int getLineIndex(ArrayList<ImgExtractLine> allPossibleLines, double lineOffset){
		int idx = -1;
		double offsetDelta = 0.0, minDelta = 10000.0;
		if(null!=allPossibleLines && allPossibleLines.size()>0){
			for(int i=0; i<allPossibleLines.size(); i++){
				offsetDelta = allPossibleLines.get(i).getLineIntercept() - lineOffset;
				if(offsetDelta<-5) continue;
				if(offsetDelta>5) break;
				
				offsetDelta = Math.abs(offsetDelta);
				if(minDelta>offsetDelta){
					minDelta = offsetDelta;
					idx = i;
				}
			}
		}
		return idx;
	}
	
	private int getMaxValidDataPtsLineIndex(ArrayList<ImgExtractLine> allPossibleLines, double startOffset, double stopOffset){
		int idx = -1, maxPts = 0;
		double lineOffset = 0.0;
		if(null!=allPossibleLines && allPossibleLines.size()>0){
			for(int i=0; i<allPossibleLines.size(); i++){
				lineOffset = allPossibleLines.get(i).getLineIntercept();
				if(lineOffset>stopOffset) break;
				if(lineOffset<startOffset) continue;
				if(maxPts<allPossibleLines.get(i).getValidPoints()){
					maxPts = allPossibleLines.get(i).getValidPoints();
					idx = i;
				}
			}
		}
		
		return idx;
	}
	
	private ArrayList<ImgExtractLine> adjustAllSearchedLinesEx(ArrayList<ImgExtractLine> allPossibleLines,int[][] imgGrays,int defaultVal){
		double ratio = 0.0, bestRsq = 0.0, refOffset = 0.0, lastOffset = 0.0, baseLine = 0.0, baseThr = 0.0;
		int bestIdx = 0, scanRng = lineScanRange/2, scanStart = -scanRng, scanStop = scanRng, maxVPts = 0, tmpVPts = 0;
		int lastBestIdx = 0, minValidPts = 0, basePts = 0, peakFlag = 0, validPtsInRng = 0;
		int imgWidth = imgGrays.length;
		
		ArrayList<ImgExtractLine> adjustLines = new ArrayList<ImgExtractLine>();
		ArrayList<Integer> maxValidPts = new ArrayList<Integer>();
		ArrayList<Integer> possLinesIdx = new ArrayList<Integer>();
		ImgExtractLine tmpLine = null;
		int[] lineValidDataPts = getPossibleLinesValidDtPts(allPossibleLines);
		
		LinkedHashMap<Integer,Integer> peaks = MathUtils.getPeakValsEx(lineValidDataPts, lineScanRange, true);
		
		for(int idx:peaks.keySet()){
			refOffset = allPossibleLines.get(idx).getLineIntercept();
			bestRsq = allPossibleLines.get(idx).getLineRSQ();
			maxVPts = allPossibleLines.get(idx).getValidPoints();
			bestIdx = idx;
			
			//Get peak index fine tune range
			for(int i=-scanRng; i>=0; i++){
				if(idx+i<0 || idx+i>=allPossibleLines.size()) continue;
				tmpLine = allPossibleLines.get(idx+i);
				if(0==tmpLine.getLineRSQ()) scanStart = i;
			}
			for(int i=0; i<=scanRng; i++){
				if(idx+i<0 || idx+i>=allPossibleLines.size()) continue;
				tmpLine = allPossibleLines.get(idx+i);
				if(0==tmpLine.getLineRSQ()) scanStop = i;
			}
			
			//Fine tune the peak index
			for(int i=scanStart; i<=scanStop; i++){
				if(idx+i<0 || idx+i>=allPossibleLines.size()) continue;
				tmpLine = allPossibleLines.get(idx+i);
				if(null!=tmpLine && Math.abs(refOffset-tmpLine.getLineIntercept())<5){
					tmpVPts = tmpLine.getValidPoints();
					ratio = (double)tmpVPts/peaks.get(idx);
					if(ratio>0.95 && bestRsq<tmpLine.getLineRSQ()){
						bestRsq = tmpLine.getLineRSQ();
						bestIdx = idx+i;
					}
					if(maxVPts<tmpVPts) maxVPts = tmpVPts;
				}
			}
			tmpLine = allPossibleLines.get(bestIdx);
			validPtsInRng = tmpLine.getValidPtsInRange((int)(imgWidth*0.6), imgWidth, 1);
			ratio = (double)validPtsInRng / tmpLine.getValidPoints();
			if(ratio<0.1){
				continue;
			}
			
			if(1==adjustLines.size()){
				System.out.print("");
			}
			
			ratio = 1.0;
			if(adjustLines.size()>0) lastOffset = adjustLines.get(adjustLines.size()-1).getLineIntercept();
			if(adjustLines.size()>1){
				tmpVPts = maxValidPts.get(maxValidPts.size()-1);
				if(tmpVPts>0){
					lastBestIdx = possLinesIdx.get(possLinesIdx.size()-1);
					minValidPts = tmpLine.getValidPoints();
					for(int k=lastBestIdx; k<=bestIdx; k++){
						if(minValidPts>allPossibleLines.get(k).getValidPoints()) minValidPts=allPossibleLines.get(k).getValidPoints();
					}
					if(tmpVPts!=minValidPts){
						baseThr = (tmpVPts-minValidPts)*0.2+minValidPts;
						baseLine = 0.0; basePts = 0;
						for(int k=lastBestIdx; k<=bestIdx; k++){
							if(allPossibleLines.get(k).getValidPoints()<baseThr){
								baseLine += allPossibleLines.get(k).getValidPoints();
								basePts++;
							}
						}
						if(basePts>0){
							baseLine = baseLine / basePts;
							ratio = (double)(tmpLine.getValidPoints()-baseLine)/(tmpVPts-baseLine);
						}else{
							ratio = (double)(tmpLine.getValidPoints()-minValidPts)/(tmpVPts-minValidPts);
						}
					}
				}
			}
			
			if(0==lastOffset || lastOffset>0 && (tmpLine.getLineIntercept()-lastOffset>=5 
			|| 1==adjustLines.size() && getLineAvgOffset(tmpLine,imgWidth) - getLineAvgOffset(adjustLines.get(0),imgWidth)>=3)){
				if(lastOffset>0 && ratio<=0.2){
					peakFlag = offsetInSamePeakEx(allPossibleLines,lastOffset,refOffset);
					if(peakFlag>=1){
						continue;
					}else if(adjustLines.size()>3){
						ratio = (refOffset-lastOffset)/(adjustLines.get(adjustLines.size()-2).getLineIntercept()-adjustLines.get(adjustLines.size()-3).getLineIntercept());
						if(ratio<minOffsetChgRate) continue;
					}
				}
				if(0==tmpLine.getXAxisStart()) tmpLine = setLineBoundary(tmpLine, imgGrays, defaultVal, 0.3);
				adjustLines.add(tmpLine);
				maxValidPts.add(maxVPts);
				possLinesIdx.add(bestIdx);
//				System.out.println("adjLines_"+adjustLines.size()+":"+validPtsInRng+"/"+tmpLine.getValidPoints()+"("+(double)validPtsInRng/tmpLine.getValidPoints()+")");
			}
		}
		
		return adjustLines;
	}
	
	private ImgExtractLine searchLine(int[][] peakVals, int[][] initVals, int[][] imgGrays, int defaultVal, double startSearchOffset, double stopSearchOffset, double mySearchSlope, int maxSearchRange){
		ImgExtractLine finalLine = null;
//		finalLine = getLineBtw(startSearchOffset,stopSearchOffset);
//		if(null!=finalLine){
//			if(0==finalLine.getXAxisStart()) finalLine = setLineBoundary(finalLine, imgGrays, defaultVal);
//			return finalLine;
//		}
		
		boolean bLineFound = false;
		int imgWidth = peakVals.length;
		int imgHeight = peakVals[0].length;
		int validCnt = 0, nearCol = 0, noisePoints = 0, realPoints = 0;
		int invalidCnt = 0, minValidCol = imgWidth*2, maxValidCol = -1;
		double noiseRate = 0.0, slopeDelta = 0.0, validRate = 0.0;
		double offsetDelta1 = 0.0, offsetDelta2 = 0.0, offsetDelta3 = 0.0, offsetDelta = 0.0;
		double invalidRatio = 0.0, maxColDelta = 3.0;
		int searchMinCol = 0, searchMaxCol = 0;
		int lineSearchingRng = lineScanRange / 2;//Two sides searching
		if(maxSearchRange<lineScanRange) lineSearchingRng = maxSearchRange/2;
		if(lineSearchingRng<=0) return null;
		
		int[][] searchingLinesData = new int[imgWidth][imgHeight];
		for(int i=0; i<imgWidth; i++){
			searchingLinesData[i] = peakVals[i].clone();
		}
		
		int fittingMinPoints = (int)(imgWidth * minValidRatio * 0.8);
		double[] lineCoef = null;
		finalLine = new ImgExtractLine();
		
		LinkedHashMap<Integer,ImgExtractLine> possibleLines = new LinkedHashMap<Integer,ImgExtractLine>();
		int possibleLineIdx = -1, bestValidRateIdx = -1;
		double bestValidRate = 0.0;
		
		if(stopSearchOffset>imgHeight) stopSearchOffset=imgHeight;
		if(imgWidth > fittingMinPoints && imgHeight > lineSearchingRng){
			for(int i=lineSearchingRng; i<=(maxSearchRange-lineSearchingRng); i++){
				minValidCol = imgWidth*2; maxValidCol = -1;
				ImgExtractLine extractLine = new ImgExtractLine();
				ImgExtractLine.setMinRSQ(0.7f);
				
				for(int j=(imgWidth-1); j>=0; j--){
					nearCol = (int)(j*mySearchSlope+startSearchOffset)+i;
					searchMinCol = (int)(j*mySearchSlope+startSearchOffset);
					searchMaxCol = (int)(j*mySearchSlope+stopSearchOffset);
					if(searchMinCol<0 || searchMinCol>(imgHeight-1)) continue;
					if(searchMaxCol<0 || searchMaxCol>(imgHeight-1)) continue;
					
					if((nearCol-lineSearchingRng)>=0 && nearCol<=(searchMaxCol-lineSearchingRng)){
						for(int k=-lineSearchingRng; k<=lineSearchingRng; k++){
							if(peakVals[j][nearCol+k]<lineFitGrayThr){
								extractLine.addPoint(j, nearCol+k, peakVals[j][nearCol+k]);
								if((nearCol+k)<minValidCol) minValidCol = nearCol+k;
								if((nearCol+k)>maxValidCol) maxValidCol = nearCol+k;
								if(Math.abs(k)<=1) extractLine.addRealPoints(j, nearCol+k, 1);
							}
							if(imgGrays[j][nearCol+k]>=noiseGrayThr2) extractLine.addInvalidPoints(1);
						}
					}
				}
				
				if(i>=lineSearchingRng){
//					saveSearchingLines(searchingLinesData,mySearchSlope,startSearchOffset,i+500);
				}
				
				validCnt = extractLine.getValidPoints();
				realPoints = extractLine.getRealPoints();
				invalidCnt = extractLine.getInvalidPoints();
				invalidRatio = (double)invalidCnt / (lineSearchingRng*2+1) / imgWidth;
				if((validCnt>=fittingMinPoints 
						|| maxSearchRange<=3 
						|| realPoints>=fittingMinPoints*0.8) 
						&& invalidRatio<maxInvalidRatio){
					lineCoef = extractLine.getLineCoef(false);
					slopeDelta = Math.abs(mySearchSlope-extractLine.getLineSlope());
					offsetDelta1 = lineCoef[1] - startSearchOffset;
					offsetDelta2 = (lineCoef[0]-mySearchSlope)*imgWidth/2+lineCoef[1]-startSearchOffset;
					offsetDelta3 = (lineCoef[0]-mySearchSlope)*(imgWidth-1)+lineCoef[1]-startSearchOffset;
					offsetDelta = (offsetDelta1+offsetDelta2+offsetDelta3)/3;
					
					if(maxValidCol-minValidCol>maxColDelta){
						validRate = extractLine.getValidRateByYAxis((maxValidCol+minValidCol)/2-1, (maxValidCol+minValidCol)/2+1);
					}else{
						validRate = 1.0;
					}
					
					if(slopeDelta<maxSlopeDelta 
							&& offsetDelta1>0 && offsetDelta2>0 && offsetDelta3>0 
							&& offsetDelta>minOffsetDelta*0.5){
						possibleLineIdx++;
						possibleLines.put(possibleLineIdx, extractLine);
						if(bestValidRate<validRate){
							bestValidRate = validRate;
							bestValidRateIdx = possibleLineIdx;
						}
					}
					
					if(null!=lineCoef && (ImgExtractLine.getMinRSQ()<=lineCoef[2] 
							|| (maxValidCol-minValidCol)<=maxColDelta 
							|| validRate>minValidRate) 
							&& slopeDelta<maxSlopeDelta 
							&& offsetDelta1>0 && offsetDelta2>0 && offsetDelta3>0
							&& offsetDelta>minOffsetDelta*0.5){
						for(int k=0; k<imgWidth; k++){
							nearCol = (int)(k*lineCoef[0]+lineCoef[1]);
							if(nearCol>noiseScanRng && nearCol<(imgHeight-noiseScanRng)){
								noisePoints = 0;
								for(int n=-noiseScanRng; n<=noiseScanRng; n++){
									if(peakVals[k][nearCol+n]<noiseGrayThr){
										if(Math.abs(n)>1){
											noisePoints++;
										}else{
											extractLine.addOnlinePoint();
										}
									}
								}
								if(noisePoints>0) extractLine.addSideNoise(noisePoints);
							}else{
								break;
							}
						}
						
						noiseRate = (double)extractLine.getNoisePoints() / extractLine.getOnlinePoints();
						if(extractLine.getOnlinePoints()>=(fittingMinPoints*0.8) 
							&& noiseRate<maxNoiseRate){
							
							extractLine = setLineBoundary(extractLine, imgGrays, defaultVal, 0.3);
							finalLine = extractLine;
							bLineFound = true;
							break;
						}
					}
				}
			}
		}
		
		if(!bLineFound){
			if(possibleLines.size()>0 && bestValidRateIdx>=0){
				finalLine = possibleLines.get(bestValidRateIdx);
				finalLine = setLineBoundary(finalLine, imgGrays, defaultVal, 0.3);
			}else{
				finalLine = null;
			}
		}
		
		if(null!=finalLine){
			if(logEnabled){
				LogUtils.rawLog("researchLineParas_", finalLine.printLineParas(0,true));
			}
		}
		
		return finalLine;
	}
	
	private LinkedHashMap<Integer,Double> getSharpChgPosition(double[] chgs){
		LinkedHashMap<Integer, Double> sharpChgPos = new LinkedHashMap<Integer, Double>();
		
		int col = chgs.length;
		int scanPoints = 4;//Two sides scanning
		int meatCndCnt = 0;
		if(col>8){
			for(int j=scanPoints; j<(col-scanPoints); j++){
				meatCndCnt = 0;
				for(int k=scanPoints; k>0; k--){
					if(0!=chgs[j] && chgs[j]<=chgs[j-k] && chgs[j]<=chgs[j+k]){
						meatCndCnt++;
					}
				}
				if(meatCndCnt==scanPoints){
					sharpChgPos.put(j, chgs[j]);
					j += scanPoints;
				}
			}
		}
		
		return sharpChgPos;
	}
	
	private void setLinesSharpChgPosition(LinkedHashMap<Integer, ImgExtractLine> linesMap, int[][] imgGrays, int[][] peakVals, boolean evenLineLonger, int targetLinesQty){
		ImgExtractLine extLine = null;
		double lineSlope, lineOffset, stdevRatio = 0.0;
		double[] Xs = new double[10], Ys = new double[10], chgs = null, tmp = null;
		int firstChgPos = 0, nearCol = 0, imgHeight = imgGrays[0].length, size = 0;
		int start = -1, stop = -1, chgStopPos = 0;
		int[] grays = null;
		String str1 = "", str2 = "";
		String[] sGrays = new String[11];
		
		for(int lineIdx:linesMap.keySet()){
			extLine = linesMap.get(lineIdx);
			if(null!=extLine){
				lineSlope = extLine.getLineSlope();
				lineOffset = extLine.getLineIntercept()+extLine.getLineInterceptDelta();
				firstChgPos = extLine.getXAxisStart()+10;
				chgStopPos = extLine.getXAxisStop();
				chgs = new double[firstChgPos];
				grays = new int[firstChgPos];
				str1 = ""; str2 = "";
				for(int k=0; k<11; k++){
					sGrays[k] = "";
				}
				
				for(int i=firstChgPos; i>0;i--){
					size = 0;
					for(int k=0; k<10; k++){
						nearCol = (int)(k*lineSlope+lineOffset);
						if(nearCol>=0 && nearCol<imgHeight){
							try {
								Xs[k] = i + k;
								Ys[k] = imgGrays[(int)Xs[k]][nearCol];
								size++;
							} catch (Exception e) {
								System.out.println(Xs[k]+":"+i+"/"+k);
							}
						}
					}
					tmp = MathUtils.lineFitting(Xs, Ys, size);
					chgs[i-1] = tmp[0];
					nearCol = (int)(i*lineSlope+lineOffset);
					if(nearCol>=0 && nearCol<imgHeight){
						if(i<imgGrays.length) grays[i-1] = imgGrays[i][nearCol];
					}
					for(int k=-5; k<=5; k++){
						if((nearCol+k)>=0 && (nearCol+k)<imgHeight && i<imgGrays.length){
							sGrays[k+5] += "," + imgGrays[i][nearCol+k];
						}else{
							sGrays[k+5] += ",0";
						}
					}
					
					str1 += "," + i;
					str2 += "," + chgs[i-1];
				}
				
				LinkedHashMap<Integer,Double> sharpChgPos = getSharpChgPosition(chgs);
				LinkedHashMap<Integer,LinkedHashMap<SharpChgItems,Double>> sharpChgPosParas = new LinkedHashMap<Integer,LinkedHashMap<SharpChgItems,Double>>();
				size = -1;
				for(int key:sharpChgPos.keySet()){
					size++;
					if(start<0){
						start = key;
						continue;
					}else{
						stop = key;
					}
					LinkedHashMap<SharpChgItems,Double> statisticParas = new LinkedHashMap<SharpChgItems,Double>();
					tmp = extLine.getLineStatisticParas(linesMap, start, (start+stop)/2, imgGrays, lineIdx, evenLineLonger);
					stdevRatio = tmp[3];
					if(1==size){
						LinkedHashMap<SharpChgItems,Double> sttParas = new LinkedHashMap<SharpChgItems,Double>();
						double[] weightOfBeingLine = extLine.weightOfBeingLine(linesMap,firstChgPos-10, chgStopPos, 0, imgGrays, peakVals, 5, lineIdx, targetLinesQty, true, curLineIsLonger(lineIdx,evenLineLonger), false);
						sttParas.put(SharpChgItems.STDEV, tmp[3]);
						sttParas.put(SharpChgItems.STDEVRATIO, weightOfBeingLine[0]);
						sttParas.put(SharpChgItems.AREARATIO, tmp[4]);
						
						tmp = extLine.getLineStatisticParas(linesMap, firstChgPos-10, chgStopPos, imgGrays, lineIdx, evenLineLonger);
						sttParas.put(SharpChgItems.INNERGRAYRATIO, tmp[5]);
						sharpChgPosParas.put(start, sttParas);
					}
					tmp = extLine.getLineStatisticParas(linesMap, (start+stop)/2, stop, imgGrays, lineIdx, evenLineLonger);
					stdevRatio = tmp[3] / stdevRatio;
					
					statisticParas.put(SharpChgItems.STDEV, tmp[3]);
					statisticParas.put(SharpChgItems.STDEVRATIO, stdevRatio);
					statisticParas.put(SharpChgItems.AREARATIO, tmp[4]);
					statisticParas.put(SharpChgItems.INNERGRAYRATIO, tmp[5]);
					sharpChgPosParas.put(key, statisticParas);
					start = key;
				}
				extLine.setSharpChgPos(sharpChgPos);
				extLine.setSharpChgPosParas(sharpChgPosParas);
				
				if(logEnabled){
					LogUtils.rawLog("sharpChgGrays_", "X"+lineIdx+str1);
					LogUtils.rawLog("sharpChgGrays_", "S"+lineIdx+str2);
					for(int k=-5; k<=5; k++){
						LogUtils.rawLog("sharpChgGrays_", "G"+lineIdx+"("+k+")"+sGrays[k+5]);
					}
					LogUtils.rawLog("sharpChgPos_", extLine.printSharpChgPos(lineIdx));
				}
			}
		}
	}
	
	private double getBoundaryThreshold(ImgExtractLine extractLine, int[][] imgGrays, int upperMeanGray, int boundaryIdx, int dblCfmDataPts, double thrFactor){
		int imgWidth = imgGrays.length;
		int imgHeight = imgGrays[0].length;
		int counter1 = 0, nearCol = 0;
		
		double sumGray1 = 0.0, grayThr1 = lineFitGrayThr;
		double lineSlope = extractLine.getLineSlope();
		double lineOffset = extractLine.getLineIntercept();
		
		for(int k=boundaryIdx; k<imgWidth; k++){
			nearCol = (int)(k*lineSlope+lineOffset);
			if(k<0 || nearCol<0 || nearCol>=imgHeight) continue;
			if(imgGrays[k][nearCol]<noiseGrayThr){
				counter1++;
				sumGray1 += imgGrays[k][nearCol];
			}
			if(counter1>=dblCfmDataPts) break;
		}
		if(counter1>0){
			grayThr1 = sumGray1 / counter1;
			if(upperMeanGray>grayThr1){
				grayThr1 = grayThr1 + (upperMeanGray - grayThr1)*thrFactor;
			}else{
				grayThr1 = grayThr1 + (maxGrayVal - grayThr1)*0.2;
			}
		}
		
		return grayThr1;
	}
	
	private double getBoundaryThreshold2(ImgExtractLine extractLine, int[][] imgGrays, int boundaryIdx, int dataPts, double thrFactor){
		int imgWidth = imgGrays.length;
		int imgHeight = imgGrays[0].length;
		int counter1 = 0, nearCol = 0;
		
		double sumGray1 = 0.0, grayThr1 = noiseGrayThr2;
		double lineSlope = extractLine.getLineSlope();
		double lineOffset = extractLine.getLineIntercept();
		
		for(int k=boundaryIdx; k<imgWidth; k++){
			nearCol = (int)(k*lineSlope+lineOffset);
			if(k<0 || nearCol<0 || nearCol>=imgHeight) continue;
			if(imgGrays[k][nearCol]<noiseGrayThr){
				counter1++;
				sumGray1 += imgGrays[k][nearCol];
			}
			if(counter1>=dataPts) break;
		}
		if(counter1>0){
			grayThr1 = sumGray1 / counter1 * thrFactor;
		}
		
		return grayThr1;
	}
	
	private boolean boundaryIsOK(ImgExtractLine extractLine, int[][] imgGrays, int defaultVal, int boundaryIdx, int continueCntThr, double grayThr1){
		boolean ok = false;
		int imgHeight = imgGrays[0].length;
		int nearCol = 0, continueCnt1 = 0, minIdx = boundaryIdx-continueCntThr-2;
		
		double minGray = 0.0;
		double lineSlope = extractLine.getLineSlope();
		double lineOffset = extractLine.getLineIntercept();
		
		for(int k=boundaryIdx; k>=minIdx; k--){
			if(k<0) break;
			nearCol = (int)(k*lineSlope+lineOffset);
			if(nearCol<0 || nearCol>=imgHeight) continue;
			
			//Check +/-1 tracks
			minGray = imgGrays[k][nearCol];
			if(nearCol-1>=0 && imgGrays[k][nearCol-1]<minGray) minGray = imgGrays[k][nearCol-1];
			if(nearCol+1<imgHeight && imgGrays[k][nearCol+1]<minGray) minGray = imgGrays[k][nearCol+1];
			
			if(minGray<grayThr1){
				if(continueCnt1<continueCntThr) continueCnt1 = 0;
			}else{
				continueCnt1++;
			}
		}
		
		if(continueCnt1>=continueCntThr) ok = true;
		return ok;
	}
	
	private ImgExtractLine setLineBoundary(ImgExtractLine extractLine, int[][] imgGrays, int defaultVal, double firstThrFactor){
		int[] poles = getLineBoundary(extractLine,imgGrays,defaultVal,firstThrFactor);
		
		extractLine.setXAxisStart(poles[0]);
		extractLine.setXAxisStop(poles[1]);
		
		return extractLine;
	}
	
	private int[] getLineBoundary(ImgExtractLine extractLine, int[][] imgGrays, int defaultVal, double firstThrFactor){
		int[] poles = new int[]{0,0};
		int imgWidth = imgGrays.length;
		int imgHeight = imgGrays[0].length;
		int start = (int)(imgWidth*0.7);
		int counter1 = 0, counter2 = 0, nearCol = 0, contCntThr = 5, baseAvgPts = 30;
		int continueCnt1 = 0, continueCnt2 = 0, minGray = 0, continueCnt3 = 0, continueCnt4 = 0, minIndex = 0;
		double sumGray1 = 0.0, baseG1 = lineFitGrayThr, newBaseG = lineFitGrayThr;
		double sumGray2 = 0.0, baseG2 = lineFitGrayThr, baseG3 = noiseGrayThr2, baseG4 = noiseGrayThr2, meanGray = 150.0;
		ArrayList<Integer> meanGrays = new ArrayList<Integer>();
		
		double lineSlope = extractLine.getLineSlope();
		double lineOffset = extractLine.getLineIntercept();
		
		if(Math.abs(lineOffset-42.817)<0.02){
			System.out.print("");
		}
		
		ArrayList<Object>trendRaw = getLineGrayTrendRawData(extractLine, imgGrays);
		double[] roughTrend = (double[]) trendRaw.get(0);
		int firstIdx = MathUtils.getFirstMeetIndex(roughTrend, firstSharpChgAngle, roughTrend.length-1, true, false);
//		if(firstIdx>0) firstIdx = getMaxPeakIndex(roughTrend, firstIdx, 10, 1, true, false, false);
		if(firstIdx>0) firstIdx = firstIdx + baseAvgPts;
		if(firstIdx>0 && firstIdx<start) start = firstIdx;
		
		for(int k=0; k<=start; k++){
			nearCol = (int)(k*lineSlope+lineOffset);
			if(nearCol<0 || nearCol>=imgHeight){
				meanGrays.add((int)maxGrayVal);
				continue;
			}else{
				counter1++;
				sumGray1 += imgGrays[k][nearCol];
				meanGrays.add((int)(sumGray1/counter1));
			}
		}
		
		minIndex = contCntThr * 2;
		if(start<minIndex) minIndex = 0;
		baseG1 = getBoundaryThreshold(extractLine,imgGrays,meanGrays.get(start-minIndex),start,imgWidth-start-1,firstThrFactor);
		
		for(int k=start; k>minIndex; k--){
			nearCol = (int)(k*lineSlope+lineOffset);
			if(nearCol<0 || nearCol>=imgHeight) continue;
			
			//Check +/-1 tracks
			minGray = imgGrays[k][nearCol];
			if(nearCol-1>=0 && imgGrays[k][nearCol-1]<minGray) minGray = imgGrays[k][nearCol-1];
			if(nearCol+1<imgHeight && imgGrays[k][nearCol+1]<minGray) minGray = imgGrays[k][nearCol+1];
			
			if(minGray<baseG1){
				if(continueCnt1<contCntThr){
					continueCnt1 = 0;//Reset only when the X Axis Start is not found
				}
			}else{
				continueCnt1++;
			}
			if(contCntThr==continueCnt1){
				newBaseG = getBoundaryThreshold(extractLine,imgGrays,meanGrays.get(k-minIndex),k+contCntThr,baseAvgPts,firstThrFactor);//Renew the threshold for a double confirmation
				if(boundaryIsOK(extractLine,imgGrays,defaultVal,k+contCntThr,contCntThr,newBaseG)){
					poles[0] = k+contCntThr;
					extractLine.setMeanGray((int)baseG1);
				}else{
					baseG1 = newBaseG;
					continueCnt1 = 0;
				}
			}
			
			//To find X Axis Stop after X Axis Start is found
			if(contCntThr<=continueCnt1){
				counter2++;
				sumGray2 += imgGrays[k][nearCol];
				meanGray = sumGray2 / counter2;
				baseG2 = (meanGray + defaultVal) / 2;
				
				minGray = imgGrays[k][nearCol];
				if(nearCol-1>=0 && imgGrays[k][nearCol-1]<minGray) minGray = imgGrays[k][nearCol-1];
				if(nearCol+1<imgHeight && imgGrays[k][nearCol+1]<minGray) minGray = imgGrays[k][nearCol+1];
				
				if(counter2>50 && 0==continueCnt4){
					baseG4 = getBoundaryThreshold2(extractLine,imgGrays,k+contCntThr,baseAvgPts,1.1);//Renew the threshold for a double confirmation
				}
				
				if(minGray>=baseG2){
					continueCnt2++;
				}else{
					if(continueCnt2<contCntThr) continueCnt2=0;
				}
				
				if(minGray>=baseG3){
					continueCnt3++;
				}else{
					if(continueCnt3<contCntThr) continueCnt3=0;
				}
				
				if(minGray>=baseG4){
					continueCnt4++;
				}else{
					if(continueCnt4<contCntThr) continueCnt4=0;
				}
				
				if(contCntThr<=continueCnt2 || contCntThr<=continueCnt3 || contCntThr<=continueCnt4){
					poles[1]=k+contCntThr;
					break;
				}
			}
		}
		
		return poles;
	}
	
	private int[] getLineStopPos(ImgExtractLine oriLine, double lineSlope, double lineOffset, int[][] imgGrays, int minIndex, int maxIndex, int scanOffset){
		int imgHeight = imgGrays[0].length, imgWidth = imgGrays.length, nearCol = 0, minGray = 0;
		int[][] tmpGrays = new int[imgWidth][imgHeight];
		
		int[] poles = null;
		double oriSlope = 0.0, oriOffset = 0.0;
		
		for(int i=0; i<imgWidth; i++){
			tmpGrays[i] = imgGrays[i].clone();
		}
		
		if(null!=oriLine){
			oriSlope = oriLine.getLineSlope();
			oriOffset = oriLine.getLineIntercept() + oriLine.getLineInterceptDelta();
		}
		
		if(scanOffset<1) scanOffset = 1;
		for(int k=maxIndex; k>minIndex; k--){
			nearCol = (int)(k*lineSlope+lineOffset);
			if(nearCol<0 || nearCol>=imgHeight) continue;
			
			//Check offset position
			minGray = imgGrays[k][nearCol];
			for(int i=1; i<=scanOffset; i++){
				if(nearCol-i>=0 && imgGrays[k][nearCol-i]<minGray) minGray = imgGrays[k][nearCol-i];
				if(nearCol+i<imgHeight && imgGrays[k][nearCol+i]<minGray) minGray = imgGrays[k][nearCol+i];
			}
			
			//Check online position
			nearCol = (int)(k*oriSlope+oriOffset);
			if(nearCol>=0 && nearCol<imgHeight){
				if(imgGrays[k][nearCol]<minGray) minGray = imgGrays[k][nearCol];
				for(int i=1; i<=scanOffset; i++){
					if(nearCol-i>=0 && imgGrays[k][nearCol-i]<minGray) minGray = imgGrays[k][nearCol-i];
					if(nearCol+i<imgHeight && imgGrays[k][nearCol+i]<minGray) minGray = imgGrays[k][nearCol+i];
				}
			}
			
			tmpGrays[k][nearCol] = minGray;
		}
		
		poles = getLineBoundary(oriLine,tmpGrays,255,0.3);
		
		return poles;
	}
	
	private int getBiggerPts(int[][] imgGrays, double threshold, int startIdx, int stopIdx, int fixIdx, boolean chkCol){
		int imgWidth = imgGrays.length, imgHeight = imgGrays[0].length;
		int iTemp = 0, iCounter = 0;
		
		if(stopIdx<startIdx){
			iTemp = startIdx;
			startIdx = stopIdx;
			stopIdx = iTemp;
		}
		
		if(chkCol){
			if(fixIdx>=0 && fixIdx<imgWidth){
				if(startIdx<0) startIdx = 0;
				if(stopIdx>=imgHeight) stopIdx = imgHeight - 1;
				for(int col=startIdx; col<=stopIdx; col++){
					if(imgGrays[fixIdx][col]>threshold) iCounter++;
				}
			}
		}else{
			if(fixIdx>=0 && fixIdx<imgHeight){
				if(startIdx<0) startIdx = 0;
				if(stopIdx>=imgWidth) stopIdx = imgWidth - 1;
				for(int row=startIdx; row<=stopIdx; row++){
					if(imgGrays[row][fixIdx]>threshold) iCounter++;
				}
			}
		}
		
		return iCounter;
	}
	
	private ImgExtractLine getBaseLine(int[][] peakVals, int[][] initVals, int[][] imgGrays, int defaultVal){
		int imgWidth = peakVals.length;
		int imgHeight = peakVals[0].length;
		int contCnt1 = 0, contCnt2 = 0, minHeight = imgHeight, maxHeight = 0, xVal = 0, yVal = 0;
		int startY = 0, stopY = 0, contCntThr = 5, baseAvgPts = 10, validPts = 0, iTemp = 0;
		int[][] startLine = new int[imgWidth][imgHeight];
		double baseG0 = 0.0, baseG1 = 0.0, baseGF1 = 0.9, baseG2 = 0.0, baseGF2 = 0.95, slopeThr = -1.2;
		double expectedY = 0.0, ratio = 0.0, avgY = 0.0;
		ArrayList<Integer> xAxis = new ArrayList<Integer>();
		ArrayList<Integer> yAxis = new ArrayList<Integer>();
		double[] coef = null;
		String title = "", data = "";
		boolean basePtFound = false, stopSearching = false;
		
		for(int i=0; i<imgWidth; i++){
			startLine[i] = imgGrays[i].clone();
		}
		
		int nearCol = 0;
		ImgExtractLine extractLine = new ImgExtractLine();
		LinkedHashMap<Double,Double> baseLineXY = new LinkedHashMap<Double,Double>();
		
		for(int i=(imgWidth-1); i>(int)(imgWidth*0.4); i-=10){
			baseG0 = 0.0;
			for(int j=0; j<baseAvgPts; j++){
				baseG0 += imgGrays[i][j];
			}
			baseG0 = baseG0 / baseAvgPts;
			
			contCnt1 = 0; contCnt2 = 0;
			baseG1 = baseG0 * baseGF1;
			baseG2 = baseG0 * baseGF2;
			
			basePtFound = false;
			for(int j=baseAvgPts; j<imgHeight; j++){
				//Condition One
				if(imgGrays[i][j]<baseG1){
					contCnt1++;
					if(contCnt1>=contCntThr){
						iTemp = getBiggerPts(imgGrays,baseG0,j,j+baseAvgPts*3,i,true);
						if(iTemp<3){
							basePtFound = true;
						}else{
							contCnt1 = 0;
						}
					}
				}else{
					contCnt1 = 0;
				}
				
				if(0==contCnt1){
					baseG0 = 0.0;
					for(int k=j; k>j-baseAvgPts; k--){
						baseG0 += imgGrays[i][k];
					}
					baseG0 = baseG0 / baseAvgPts;
					baseG1 = baseG0 * baseGF1;
				}
				
				//Condition Two
				if(!basePtFound){
					xAxis.clear();
					yAxis.clear();
					for(int k=j-baseAvgPts+1; k<=j; k++){
						xAxis.add(k);
						yAxis.add(imgGrays[i][k]);
					}
					coef = MathUtils.lineFitting(xAxis, yAxis);
					if(coef[0]<slopeThr && imgGrays[i][j]<baseG2){
						contCnt2++;
						if(contCnt2>=contCntThr){
							iTemp = getBiggerPts(imgGrays,baseG0,j,j+baseAvgPts*3,i,true);
							if(iTemp<3){
								basePtFound = true;
							}else{
								contCnt2 = 0;
							}
						}
					}else{
						contCnt2 = 0;
					}
					
					if(0==contCnt2){
						baseG0 = 0.0;
						for(int k=j; k>j-baseAvgPts; k--){
							baseG0 += imgGrays[i][k];
						}
						baseG0 = baseG0 / baseAvgPts;
						baseG2 = baseG0 * baseGF2;
					}
				}
				
				if(basePtFound){
					if(baseLineXY.size()>5){
						coef = MathUtils.lineFitting(baseLineXY);
						expectedY = coef[0]*i+coef[1];
						ratio = Math.abs(expectedY-j+contCntThr)/expectedY;
						if(16==baseLineXY.size()){
							System.out.print("");
						}
						if(ratio>0.35){
							stopSearching = true;
							break;
						}
					}
					if(minHeight>(j-contCntThr)) minHeight = j-contCntThr;
					if(maxHeight<(j-contCntThr)) maxHeight = j-contCntThr;
					baseLineXY.put((double)i, (double)(j-contCntThr));
					avgY += (j-contCntThr);
					validPts++;
					break;
				}
			}
			if(stopSearching) break;
		}
		
		if(validPts>0){
			avgY = avgY / validPts;
			for(double xKey:baseLineXY.keySet()){
				xVal = (int)xKey;
				yVal = (int)((double)baseLineXY.get(xKey));
				ratio = (baseLineXY.get(xKey) - avgY) / avgY;
				if(Math.abs(ratio)>0.35) continue;
				extractLine.addPoint(xVal, yVal, imgGrays[xVal][yVal]);
			}
		}
		extractLine.getLineCoef(false);
		
		//Fetch base line raw data
		if(logEnabled){
			if(baseLineXY.size()>0){
				startY = minHeight - 20;
				stopY = maxHeight + 30;
				if(startY<0) startY = 0;
				if(stopY>=imgHeight) stopY = imgHeight - 1;
				
				title = "x,y,rawY";
				for(int y=startY; y<=stopY; y++){
					title += "," + y;
				}
				LogUtils.rawLog("baseLineXY_",title);
				for(double key:baseLineXY.keySet()){
					data = key + "," + baseLineXY.get(key) + ",gray";
					for(int y=startY; y<=stopY; y++){
						data += "," + imgGrays[(int)key][y];
					}
					LogUtils.rawLog("baseLineXY_",data);
				}
			}
			
			double startLineSlope = extractLine.getLineSlope();
			double startLineOffset = extractLine.getLineIntercept();
			for(int k=0; k<imgWidth; k++){
				nearCol = (int)(k*startLineSlope+startLineOffset);
				if(nearCol>=0 && nearCol<imgHeight) startLine[k][nearCol] = 0;
			}
			saveImgData("graysStartLine_",startLine);
		}
		
		return extractLine;
	}
	
	private int[][] getPeakVals(int[][] imgGrays, int[][] initVals){
		int[][] peakVals = null;
		if(null != imgGrays){
			int row = imgGrays.length;
			int col = imgGrays[0].length;
			int scanPoints = 3;//Two sides scanning
			int positiveCnt = 0, negativeCnt = 0;
			peakVals = new int[row][col];
			if(col>5){
				for(int i=0; i<row; i++){
					peakVals[i] = initVals[i].clone();
					for(int j=scanPoints; j<col; j++){
						if((j+scanPoints)<col){
							positiveCnt = 0;
							negativeCnt = 0;
							for(int k=scanPoints; k>0; k--){
								if(imgGrays[i][j]>imgGrays[i][j-k] && imgGrays[i][j]>imgGrays[i][j+k]){
									positiveCnt++;
								}
								
								if(imgGrays[i][j]<imgGrays[i][j-k] && imgGrays[i][j]<imgGrays[i][j+k]){
									negativeCnt++;
								}
							}
							if(negativeCnt==scanPoints){//Get the negative change
								peakVals[i][j] = imgGrays[i][j];
								j += scanPoints;
							}
							if(positiveCnt==scanPoints) j += scanPoints;//Skip the positive change
						}else{
							break;
						}
					}
				}
			}
		}
		
		if(null!=peakVals) saveImgData("graysPeakVals_",peakVals);
		return peakVals;
	}
	
	private void saveImgData(String filePrefix, int[][] data){
		if(logEnabled){
			int width = data.length;
			int height = data[0].length;
			String str = "";
			
	    	for (int i = 0; i < width; i++) {
	        	str = "";
	            for (int j = (height-1); j >= 0; j--) {
	                str += data[i][j]+",";
	            }
	            LogUtils.rawLog(filePrefix,str);
	        }
		}
	}
	
	private void saveSearchingLines(int[][] data, double slope, double offset, int index, String flag){
		if(logEnabled){
			int imgWidth = data.length, imgHeight = data[0].length, nearCol = 0;
			for(int k=0; k<imgWidth; k++){
				nearCol = (int)(k*slope+offset);
				if(nearCol>=0 && nearCol<imgHeight){
					if(data[k][nearCol]>230) data[k][nearCol] = 0;
				}
			}
			saveImgData("graysSeachingLine_"+flag+"_"+index+"_",data);
		}
	}
}
