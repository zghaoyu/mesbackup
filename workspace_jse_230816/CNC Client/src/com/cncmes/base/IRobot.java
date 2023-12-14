package com.cncmes.base;

public interface IRobot {
	public boolean moveTo(String ip, String targetPositionName);
	public boolean dockTo(String ip, String dockPositionName);
	public boolean gotoCharge(String ip, boolean enableCharging);
	public boolean pickAndPlace(String ip, String pickFromPose, String placeToPose);
	public boolean adjustGripperPose(String ip, String targetPoseName, String pnpType);
	public String readBarcode(String ip);
	public String getBattery(String ip);
	public String getAgvPosition(String ip);
	public boolean enableCharging(String ip);
	public boolean moveBackward(String ip);
	public boolean pickFromName(String ip, String pickFromPose);
	public boolean placeToName(String ip, String placeToPose);
}
