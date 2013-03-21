package com.example.recloc;

public class MatrixCalc {
	
	public static double similarity(double[] matA, double[]  matB, int len) {
		
		double delta = 0.0;
		
		for (int i=0; i<len;i++){
			delta = delta +  Math.abs(matA[i]- matB[i]);
		}
		return 2*len/(2*len+delta);
	}
}
