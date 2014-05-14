package com.wikout;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import com.wikout.R;

import android.app.Application;

@ReportsCrashes(
	    formUri = "https://shezek.cloudant.com/acra-myapp/_design/acra-storage/_update/report",
	    reportType = HttpSender.Type.JSON,
	    httpMethod = HttpSender.Method.POST,
	    formUriBasicAuthLogin = "daysmallasedillesevesiou",
	    formUriBasicAuthPassword = "4k4viPYfu6OR1wTWkoxKdiqK",
	    formKey = "", // This is required for backward compatibility but not used
	    customReportContent = {
	            ReportField.APP_VERSION_CODE,
	            ReportField.APP_VERSION_NAME,
	            ReportField.ANDROID_VERSION,
	            ReportField.PACKAGE_NAME,
	            ReportField.REPORT_ID,
	            ReportField.BUILD,
	            ReportField.STACK_TRACE
	    },
	    mode = ReportingInteractionMode.TOAST,
	    resToastText = R.string.toast_crash
	)

public class AcraClass extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(this);
	
        
	}
}