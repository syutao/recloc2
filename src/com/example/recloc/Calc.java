package com.example.recloc;

public class Calc
{
	public String name = new String();
	public int[] data = new int[ConstantsMain.SAMPLING_POINTS];
	public double mean;
	public double var;

	public Calc()
	{
		for (int i = 0; i<ConstantsMain.SAMPLING_POINTS ; i++)
		{
			this.name = "";
			this.data[i] = 0;
		}
	}
	
	public void StatCalc()
	{
		mean();
		var();
	}


	public int size()
	{
		return this.data.length;
	}
	
	public boolean delete()
	{
		name="";
		for (int i = 0; i<ConstantsMain.SAMPLING_POINTS ; i++)
		{
			this.name = "";
			this.data[i] = 0;
		}
		mean = 0.0;
		var = 0.0;
		
		return true;
	}

	private void var()
	{
		int dataLen = this.data.length;
		Double.valueOf(0.0D);
		this.var = 0.0D;
		for (int j = 0; j<dataLen; j++)
		{
			if (0 == data[j] )
			{
				dataLen = dataLen-1;
				continue;
			}
			Double tmp = Double.valueOf(this.data[j] - this.mean);
			this.var += tmp.doubleValue() * tmp.doubleValue();
		}
		this.var /= (dataLen - 1);
	}
	
	private void mean()
	{
		int dataLen = data.length;
		int tmp = 0;
		
		for (int k = 0; k<dataLen ; k++)
		{
			if (0 == data[k] )
			{
				dataLen = dataLen-1;
				continue;
			}
			tmp += this.data[k];
		}
		this.mean = (tmp / dataLen);
	}
	
}
