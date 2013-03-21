package com.example.recloc;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.recloc.db.DBase;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TextView;

public class MainActivity extends Activity {

	private WifiManager wifiManager;
	private ProgressBar recProg;
	private Button recBtn,testBtn;
	private EditText editAdd;
	private TextView textView;

	DBase dBase = new DBase();

	String strAddlabel = new String();

	final Handler handler = new Handler();

	static final String TAG = "Main Activity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		init();

		recBtn.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				wifiManager.startScan();
				handler.post(wifiRecording);
				strAddlabel = editAdd.getText().toString();
			}
		});

		testBtn.setOnClickListener(new Button.OnClickListener(){

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				DisplayMetrics dm = new DisplayMetrics();
//				getWindowManager().getDefaultDisplay().getMetrics(dm);
//				String strOpt = "手机屏幕分辨率为：" + dm.widthPixels + " × " + dm.heightPixels; 
				
				Date tDate = new Date();
				Calendar calendar = Calendar.getInstance();
				tDate = calendar.getTime();
				textView.setText(tDate.toGMTString());

//				wifiManager.startScan();
//				handler.post(wifiTest);
			}
		});
	}

	private int iSampling = -1;
	private Calc[] calcRSS = new Calc[100];
	private int sizeOfcalcRSS = 0;
	private Runnable wifiRecording = new Runnable(){

		@Override
		public void run() {

			try{
				if(wifiScan()){

					sortCalc();
					filterCalcRSS();

					dBase.UpdataBssid(calcRSS, sizeOfcalcRSS);
					dBase.UpdataRecording(calcRSS, strAddlabel, sizeOfcalcRSS);
					recProg.setProgress(iSampling+1);
					Toast.makeText(MainActivity.this, strAddlabel+"已记录",Toast.LENGTH_SHORT).show();
					updatelistview();

					sizeOfcalcRSS = 0;
					iSampling = -1;
				}else{
					wifiManager.startScan();
					recProg.setProgress(iSampling+1);
					handler.postDelayed(this, 1000);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	};
	
	



	private void filterCalcRSS() {

		int j;
		for(int i = 0; i<sizeOfcalcRSS; i++){
			if(calcRSS[i].var > 20.0 || calcRSS[i].var < 0.1 || calcRSS[i].mean==0.0){
				System.out.println(calcRSS[i].var);

				Log.i(TAG, "delate RSS record");
				for(j = i;j<sizeOfcalcRSS;j++){
					calcRSS[j] = calcRSS[j+1];
				}
				calcRSS[j].delete();
				sizeOfcalcRSS = sizeOfcalcRSS-1;
				i = i-1;
			}
		}
	}



	private Runnable wifiTest = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			try {
				if(wifiScan()){

//					System.out.println("Scan over");

					double[] compRSS = new double[255];
					double[] ctrlRSS = new double[255];
					double similarity = 0;

					boolean fGo;

					String[] columnStrings = new String[]{"_id","bssid","meanRSS","varRSS"};
					Cursor tagsCursor = dBase.getlocTagDB().rawQuery("SELECT * FROM Tags", null);
					Cursor crCursor;
					tagsCursor.moveToFirst();

					sortCalc();
					filterCalcRSS();
					int  indexOfRSSI ;

					while(tagsCursor.moveToNext()){
						// next table

						indexOfRSSI =0;

						// ctrlString is query from RSSIDB, is an table
						crCursor = dBase.getRssiDB().query(tagsCursor.getString(tagsCursor.getColumnIndex("seriesNum")), columnStrings, null, null, null, null, null);
						crCursor.moveToFirst();

						String ctrlString = crCursor.getString(crCursor.getColumnIndex("bssid"));
						String teString;
						String ctrlRSString = "";

						for(int i=0;i<sizeOfcalcRSS;i++){
							teString = calcRSS[i].name;
							crCursor.moveToFirst();
							ctrlString = crCursor.getString(crCursor.getColumnIndex("bssid"));

							fGo = true;
							while(fGo)
							{
								while(!crCursor.isAfterLast()){
									
									ctrlString = crCursor.getString(crCursor.getColumnIndex("bssid"));
									crCursor.moveToNext();
									if(teString.equalsIgnoreCase(ctrlString)){
										fGo = false;
										break;
									}
								}
							}

							if(!fGo){
								crCursor.moveToPrevious();
								ctrlRSString = crCursor.getString(crCursor.getColumnIndex("meanRSS"));
								ctrlRSS[indexOfRSSI] = Double.parseDouble(ctrlRSString);
								compRSS[indexOfRSSI] = calcRSS[i].mean;
								indexOfRSSI = indexOfRSSI + 1;
							}
						}

						similarity = MatrixCalc.similarity(ctrlRSS,compRSS,indexOfRSSI);

						System.out.println(tagsCursor.getString(tagsCursor.getColumnIndex("tag1")));
						System.out.println(similarity);
					}

				}else{
					wifiManager.startScan();
					recProg.setProgress(iSampling+1);
					handler.postDelayed(this, 1000);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	};


	@SuppressLint("NewApi")
	private boolean wifiScan() {
		int iScan = -1;
		int iRSS = -1;

		//		Log.i(TAG, "wifi scaning");

		boolean fScanOver = false;

		List<ScanResult> list = wifiManager.getScanResults();
		int listLen = list.size();
		if(sizeOfcalcRSS<listLen){
			sizeOfcalcRSS = listLen;
		}

		if(0!=listLen){
			iSampling = iSampling +1;

			for(ScanResult scanResult:list){

				iScan = iScan+1;
				iRSS = 0;
				

				if(0 == iSampling){
					calcRSS[iScan].name = scanResult.BSSID;
					calcRSS[iScan].data[iSampling] = scanResult.level;
				}
				else {
					//					System.out.println(iSampling);
					//					System.out.println(scanResult.BSSID);
					while(iRSS<sizeOfcalcRSS){

						if (calcRSS[iRSS].name.equals(scanResult.BSSID)) {
							calcRSS[iRSS].data[iSampling] = scanResult.level;
							//							System.out.println("calcRSS[iRSS].name.equals(scanResult.BSSID)");
							break;
						}			
						else if (calcRSS[iRSS].name.isEmpty()) {
							calcRSS[iRSS].name = scanResult.BSSID;
							calcRSS[iRSS].data[iSampling] = scanResult.level;
							break;
						}
						iRSS = iRSS +1;
					}


					//					System.out.println("iRSS = ");
					//					System.out.println(iRSS);
				}

				if (ConstantsMain.SAMPLING_POINTS - 1 == iSampling){

					calcRSS[iRSS].StatCalc();
					fScanOver = true;
				}
			}
		}

		//		Log.i(TAG, "wifi scan over");
		//		System.out.println(fScanOver);

		list.clear();
		return fScanOver;
	}


	@SuppressLint("NewApi")
	public void updatelistview() {
		ListView lView = (ListView)findViewById(R.id.lView);
		String[] columnStrings = new String[]{"_id","bssid","meanRSS","varRSS", "v1RSS","v2RSS","v3RSS","v4RSS","v5RSS"};

		String str = "SELECT seriesNum FROM Tags WHERE tag1 = ?";
		Cursor tagsCursor = dBase.getlocTagDB().rawQuery(str, new String[]{strAddlabel});

		tagsCursor.moveToFirst();
		String tagString = tagsCursor.getString(tagsCursor.getColumnIndex("seriesNum"));

		Cursor crCursor = dBase.getRssiDB().query(tagString, columnStrings, null, null, null, null, null);

		String[] columnNameStrings = crCursor.getColumnNames();

		@SuppressWarnings("deprecation")
		ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.layout, crCursor,
				columnNameStrings, new int[]{R.id.tv_id,R.id.tv_bssid,R.id.tv_rssiMean,R.id.tv_rssiVar,
				R.id.tv_1RSS,R.id.tv_2RSS,R.id.tv_3RSS,R.id.tv_4RSS,R.id.tv_5RSS});
		System.out.println(columnNameStrings.toString());
		lView.setAdapter(adapter);
	}

	//初始化
	private void init() {

		wifiManager = ((WifiManager)getSystemService("wifi"));

		recBtn 	= (Button)findViewById(R.id.recbtn);
		testBtn = (Button)findViewById(R.id.testbtn);
		editAdd = (EditText)findViewById(R.id.editAdd);
		textView = (TextView)findViewById(R.id.textView1);

		this.recProg = ((ProgressBar)findViewById(R.id.recProgress));
		this.recProg.setIndeterminate(false);
		this.recProg.setMax(ConstantsMain.SAMPLING_POINTS);
		this.recProg.setProgress(5);

		for (int i = 0; i<100 ; i++)
		{
			this.calcRSS[i] = new Calc();
		}
	}

	//对calcRSS数组进行排序
	private void sortCalc() {
		Calc tmpCalc = new Calc();

		System.out.println("sorting");

		BigDecimal MaxRSS; 
		BigDecimal cmpRSS; 

		for (int i = 0; i < sizeOfcalcRSS; i++) {
			MaxRSS = BigDecimal.valueOf(calcRSS[i].mean);

			for (int j = i+1; j < sizeOfcalcRSS; j++) {
				cmpRSS = BigDecimal.valueOf(calcRSS[j].mean);
				if(1 == cmpRSS.compareTo(MaxRSS)){
					//cmpRSS > MaxRSS
					tmpCalc = calcRSS[i];
					calcRSS[i]= calcRSS[j];
					calcRSS[j]= tmpCalc; 		
					MaxRSS=cmpRSS;
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onDestroy(){
		handler.removeCallbacks(wifiRecording);
		handler.removeCallbacks(wifiTest);
		super.onDestroy();
	}

}
