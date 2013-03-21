package com.example.recloc.db;

import android.os.Environment;

public class Constants
{
  public static final String BSSID_DB = "bssid.db";
  public static final String DTABASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RecLoc";
  public static final String LOC_LABEL_DB = "locLabel.db";
  public static final String RSS_DB = "fingerprint.db";
}