package com.github.daweizhou89.rollview.utils;

public class StringUtils {
    public static String getEllipsis(String inString, int last) {
        String ret = null;
        if(inString != null) {
            int byteCount = 0;
            int byteLimited = last * 2;
            for (int i = 0; i < inString.length(); ++i) {
                char ch = inString.charAt(i);
                if (ch >= ' ' && ch <= '~') {
                    ++byteCount;
                } else {
                    byteCount += 2;
                }
                if (byteCount > byteLimited) {
                    last = i;
                    break;
                }
            }
            if (byteCount > byteLimited) {
                ret = inString.subSequence(0, last) + "..";
            } else {
                ret = inString;
            }
        }
        return ret;
    }
    
    /***
     * @param axisIn2D  二维坐标系中的旋转方向的法向量（轴旋转）
     * @param positionIn3D  点在三维坐标系中的坐标
     * @param angularVelocity   点绕轴旋转的角速度
     */
    public static float[] roll(float[] axisIn2D, float[] positionIn3D, float angularVelocity)  {
        
        float ax = axisIn2D[0];     // x of axis in 2D
        float ay = axisIn2D[1];     // y of axis in 2D
        
        float px = positionIn3D[0]; // x of position in 3D
        float py = positionIn3D[1]; // y of position in 3D
        float pz = positionIn3D[2]; // z of position in 3D
        
        float angularVelocityOnY = (float)Math.sin(angularVelocity);
        float angularVelocityOnX = (float)Math.cos(angularVelocity);
        
        float lastPx = px * (ax * ax * (1 - angularVelocityOnX) + angularVelocityOnX) + py * ax * ay * (1 - angularVelocityOnX) + pz * ay * angularVelocityOnY;
        float lastPy = px * ax * ay * (1 - angularVelocityOnX) + py * (ay * ay * (1 - angularVelocityOnX) + angularVelocityOnX) - pz * ax * angularVelocityOnY;
        float lastPz = -px * ay * angularVelocityOnY + py * ax * angularVelocityOnY + pz * angularVelocityOnX;
    
        positionIn3D[0] = lastPx;
        positionIn3D[1] = lastPy;
        positionIn3D[2] = lastPz;
        
        return positionIn3D;
    }
}
