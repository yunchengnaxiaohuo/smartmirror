/*******************************************************************************
 * Copyright (c) 2016 by Ohio Corporation all right reserved.
 * 2016-8-1 
 * 
 *******************************************************************************/
package com.clean.mirror.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;

import com.clean.mirror.utils.CameraHelper.CameraHelperImpl;
import com.clean.mirror.utils.CameraHelper.CameraInfo2;

/**
 * <pre>
 * ����˵��: 
 * ����:	2016-8-1
 * ������:	cyd
 * 
 * ��ʷ��¼
 *    �޸����ݣ�
 *    �޸���Ա��
 *    �޸����ڣ� 2016-8-1
 * </pre>
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class CameraHelperGB implements CameraHelperImpl {

	public int getNumberOfCameras() {
		// TODO Auto-generated method stub
		return Camera.getNumberOfCameras();
	}

	public Camera openCamera(int id) {
		// TODO Auto-generated method stub
		return Camera.open(id);
	}

	public Camera openDefaultCamera() {
		// TODO Auto-generated method stub
		return Camera.open(1);
	}

	public Camera openCameraFacing(int facing) {
		// TODO Auto-generated method stub
		return Camera.open(getCameraId(facing));
	}

	/**
	 * ����˵���� ����: 2016-8-1 ������: cyd
	 * 
	 * @param facing
	 * @return
	 */
	private int getCameraId(int facing) {
		// TODO Auto-generated method stub
		int numberOfCameras = Camera.getNumberOfCameras();
		CameraInfo info = new CameraInfo();
		for (int id = 0; id < numberOfCameras; id++) {
			Camera.getCameraInfo(id, info);
			if (info.facing == facing) {
				return id;
			}
		}
		return -1;
	}

	public boolean hasCamera(int cameraFacingFront) {
		// TODO Auto-generated method stub
		return getCameraId(cameraFacingFront) != -1;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	public void getCameraInfo(int cameraId, CameraInfo2 cameraInfo) {
		// TODO Auto-generated method stub
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		cameraInfo.facing = info.facing;
		cameraInfo.orientation = info.orientation;
	}

}
