package com.ranglerz.asynctask;

import com.ranglerz.logic.IcCardOrderAPI;
import com.ranglerz.logic.IcCardOrderAPI.Result;
import com.ranglerz.utils.DataUtils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AsynIcCardOrder extends Handler{
private static final int SENDOPERCMD = 0x00;
	
	private IcCardOrderAPI icCardOrderAPI;
	private Handler workerThreadHandler;
	
	private String result;

	public AsynIcCardOrder(Looper looper) 
	{
		Log.i("cy", "Enter function AsynIcCardOrder-AsynIcCardOrder()");
		this.workerThreadHandler = createHandler(looper);	
		this.icCardOrderAPI = new IcCardOrderAPI();
	}
	
	protected Handler createHandler(Looper looper) 
	{
		Log.i("cy", "Enter function AsynIcCardOrder-createHandler()");
		return new WorkerHandler(looper);
	}
	
	public void sendOperCmd(byte[] cmd) 
	{
		Log.i("cy", "Enter function AsynIcCardOrder-sendOperCmd()");
		this.workerThreadHandler.obtainMessage(SENDOPERCMD, cmd).sendToTarget();
	}
	
	protected int sendCmd(byte[] cmd)
	{
		Log.i("cy", "Enter function AsynIcCardOrder-sendCmd()");
		
		byte[] retInfo = AsynIcCardOrder.this.icCardOrderAPI.operateICApdu(cmd, cmd.length);
		if(null == retInfo)
		{
			return Result.FAILURE;
		}
		result = DataUtils.toHexString(retInfo);
		
		return Result.SUCCESS;
		
	}
	
	protected class WorkerHandler extends Handler 
	{
		public WorkerHandler(Looper looper) 
		{
			super(looper);
			Log.i("cy", "Enter function AsynIcCardOrder-WorkerHandler-WorkerHandler()");
		}
		
		public void handleMessage(Message msg) 
		{	
			Log.i("cy", "Enter function AsynIcCardOrder-WorkerHandler-handleMessage()");

			switch (msg.what) 
			{
			case SENDOPERCMD:
				int ret = AsynIcCardOrder.this.sendCmd((byte[])msg.obj);
				AsynIcCardOrder.this.obtainMessage(SENDOPERCMD, ret, -1, result).sendToTarget();
				break;
				
			default:
				break;
				
			}
			
		}
		
	}
	
	public interface OnSendOperCmdListener 
	{	
		void onSendOperCmdSuccess(String result);
		void onSendOperCmdFail(int resultCode);
	}
	
	private OnSendOperCmdListener onSendOperCmdListener;
	
	public void setOnSendOperCmdListener(OnSendOperCmdListener onSendOperCmdListener) 
	{
		Log.i("cy", "Enter function AsynIcCardOrder-setOnSendOperCmdListener()");
		this.onSendOperCmdListener = onSendOperCmdListener;
	}
	
	public void handleMessage(Message msg) 
	{
		Log.i("cy", "Enter function AsynIcCardOrder-handleMessage()");
		
		super.handleMessage(msg);
		switch (msg.what) 
		{		
		case SENDOPERCMD:
			if(null != this.onSendOperCmdListener)
			{
				if(0 != msg.arg1)
				{
					if(null != msg.obj)
					{
						this.onSendOperCmdListener.onSendOperCmdSuccess((String)msg.obj);
					}
				}
				else
				{
					this.onSendOperCmdListener.onSendOperCmdFail(msg.arg1);
				}
			}
			break;
			
		default:
			break;	
		}
	}
}