package com.hiklife.rfidapi;

import com.ranglerz.utils.LooperBuffer;

public class LoopBuf implements LooperBuffer{

	/**
	 * ���ݲ���������
	 */
	private static Object lock = new Object(); 
	
	/**
	 * ���ջ������
	 */
	private byte[] LocalBuffer = new byte[204800];
	
	/**
	 * ��ʼ����
	 */
	private int startIndex = 0;
	
	/**
	 * ��������
	 */
	private int endIndex = 0;

	/**
	 * ������ݵ�������
	 * @param buf Ҫ��ӵ�����
	 */
    public void add(byte[] buf)
    {
    	try
    	{
    		synchronized(lock)
        	{
        		if (startIndex <= endIndex)
                {
                    if (((LocalBuffer.length - 1) - endIndex) >= buf.length)
                    {
                        // �����㹻���µ�ǰ������
                    	System.arraycopy(buf, 0, LocalBuffer, endIndex, buf.length);
                        endIndex += buf.length;
                    }
                    else if (startIndex + ((LocalBuffer.length - 1) - endIndex) >= buf.length)
                    {
                        // �����㹻���µ�ǰ������
                        int copyLen = ((LocalBuffer.length - 1) - endIndex + 1);
                        System.arraycopy(buf, 0, LocalBuffer, endIndex, copyLen);
                        System.arraycopy(buf, copyLen, LocalBuffer, 0, buf.length - copyLen);
                        endIndex = buf.length - copyLen;
                    }
                    else
                    {
                        // ���治���޷�������ݣ���˵��Ŀǰ�ϲ㴦���ٶȲ��㣬����л�����չ
                    	byte[] newBuf = new byte[LocalBuffer.length + buf.length * 2]; 
                        System.arraycopy(LocalBuffer, 0, newBuf, 0, LocalBuffer.length); 
                        System.arraycopy(buf, 0, newBuf, endIndex, buf.length); 
                        endIndex += buf.length;
                        LocalBuffer = newBuf;
                    }
                }
                else
                {
                    // ���洦��ѭ��״̬
                    if ((startIndex - endIndex) >= buf.length)
                    {
                        // �����㹻���µ�ǰ������
                    	System.arraycopy(buf, 0, LocalBuffer, endIndex, buf.length);
                        endIndex += buf.length;
                    } 
                    else
                    {
                        //���治���޷�������ݣ���˵��Ŀǰ�ϲ㴦���ٶȲ��㣬����л�����չ
                    	byte[] newBuf = new byte[LocalBuffer.length + buf.length * 2]; 
                    	System.arraycopy(LocalBuffer, 0, newBuf, 0, endIndex - 1);
                    	System.arraycopy(buf, 0, newBuf, endIndex, buf.length); 
                        endIndex += buf.length;
                        System.arraycopy(LocalBuffer, startIndex, newBuf, newBuf.length - (LocalBuffer.length - startIndex), LocalBuffer.length - startIndex);
                        startIndex = newBuf.length - (LocalBuffer.length - startIndex);
                        LocalBuffer = newBuf;
                    }
                }
        	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }

    /**
	 * ��ȡһ�������Ľ�����
	 * @return �����������ݰ�������
	 */
    public byte[] getFullPacket()
    {
    	try
    	{
    		synchronized (lock)
            {
        		int findStartIndex = startIndex;
            	int findEndIndex = startIndex;
            	boolean findStart = false;
            	boolean findEnd = false;
            	
            	 // ����������Ұ�ͷ
                if (startIndex < endIndex)
                {
                    while (findStartIndex < endIndex - 1) // ֻʣ�����Ͱ�β���Բ��ؼ��
                    {
                        if ((LocalBuffer[findStartIndex]& 0x00FF) == 0x5A && (LocalBuffer[findStartIndex + 1]& 0x00FF) == 0x55)
                        {
                            // �ҵ��˰�ͷ��������Ұ�β
                            findEndIndex = findStartIndex + 1;
                            findStart = true;
                            break;
                        }

                        findStartIndex++;
                    }

                    if (findStart)
                    {
                        while (findEndIndex < endIndex - 1)
                        {
                            if ((LocalBuffer[findEndIndex]& 0x00FF) == 0x6A && (LocalBuffer[findEndIndex + 1]& 0x00FF) == 0x69)
                            {
                                // �ҵ��˰�β������к�����������֤
                                findEndIndex += 1;
                                findEnd = true;
                                break;
                            }
                            else if ((LocalBuffer[findEndIndex]& 0x00FF) == 0x5A && (LocalBuffer[findEndIndex + 1]& 0x00FF) == 0x55)
                            {
                                findStartIndex = findEndIndex;
                            }

                            findEndIndex++;
                        }

                        if (findEnd)
                        {
                            // �������ҵ��ˣ�����У��
                            byte[] tempbuf = new byte[findEndIndex - findStartIndex + 1];
                            System.arraycopy(LocalBuffer, findStartIndex, tempbuf, 0, findEndIndex - findStartIndex + 1);
                            // ����У��
                            tempbuf = commonFun.Del0x99(tempbuf);
                            
                            if (tempbuf == null)
                            {
                                // ���ݰ������ж�ʧ��������
                                startIndex = findEndIndex + 1;
                                if (startIndex > endIndex)
                                {
                                    startIndex = endIndex;
                                }
                                
                                return null;
                            }
                            else
                            {
                                startIndex = findEndIndex + 1;
                                if (startIndex > endIndex)
                                {
                                    startIndex = endIndex;
                                }
                                
                                return tempbuf;
                            }
                        }
                        else
                        {
                        	return null;
                        }
                    }
                    else
                    {
                        // ��ȥ��������ݰ�
                        if ((LocalBuffer[startIndex]& 0x00FF) != 0x5A)
                        {
                            startIndex++;
                        }
                        
                        return null;
                    }
                }
                else if (startIndex > endIndex)
                {
                    // �Ƚ�buffer��������
                    byte[] tempBuf = new byte[LocalBuffer.length - startIndex + 1 + endIndex + 1];
                    System.arraycopy(LocalBuffer, startIndex, tempBuf, 0, LocalBuffer.length - startIndex);
                    System.arraycopy(LocalBuffer, 0, tempBuf, LocalBuffer.length - startIndex, endIndex + 1);

                    findStartIndex = 0;
                    while (findStartIndex < tempBuf.length - 2) // ֻʣ�����Ͱ�β���Բ��ؼ��
                    {
                        if ((tempBuf[findStartIndex]& 0x00FF) == 0x5A && (tempBuf[findStartIndex + 1]& 0x00FF) == 0x55)
                        {
                            // �ҵ��˰�ͷ��������Ұ�β
                            findEndIndex = findStartIndex + 1;
                            findStart = true;
                            break;
                        }

                        findStartIndex++;
                    }

                    if (findStart)
                    {
                        while (findEndIndex < tempBuf.length - 2)
                        {
                            if ((tempBuf[findEndIndex]& 0x00FF) == 0x6A && (tempBuf[findEndIndex + 1]& 0x00FF) == 0x69)
                            {
                                // �ҵ��˰�β������к�����������֤
                                findEndIndex += 1;
                                findEnd = true;
                                break;
                            }
                            else if ((tempBuf[findEndIndex]& 0x00FF) == 0x5A && (tempBuf[findEndIndex + 1]& 0x00FF) == 0x55)
                            {
                                findStartIndex = findEndIndex;
                            }

                            findEndIndex++;
                        }

                        if (findEnd)
                        {
                            // �������ҵ��ˣ���ô��ʼУ��
                            byte[] tempcheckbuf = new byte[findEndIndex - findStartIndex + 1];
                            System.arraycopy(tempBuf, findStartIndex, tempcheckbuf, 0, findEndIndex - findStartIndex + 1);
                            tempcheckbuf = commonFun.Del0x99(tempcheckbuf);
                            
                            if (tempcheckbuf == null)
                            {
                                // ���ݰ������ж�ʧ��������
                                startIndex += (findEndIndex + 1);
                                if (startIndex > LocalBuffer.length)
                                {
                                    startIndex = startIndex - LocalBuffer.length - 1;
                                    if (startIndex > endIndex)
                                    {
                                        startIndex = endIndex;
                                    }
                                }
                                
                                return null;
                            }
                            else
                            {
                                startIndex += (findEndIndex + 1);
                                if (startIndex > LocalBuffer.length)
                                {
                                    startIndex = startIndex - LocalBuffer.length - 1;
                                    if (startIndex > endIndex)
                                    {
                                        startIndex = endIndex;
                                    }
                                }
                                
                                return tempcheckbuf;
                            }
                        }
                        else
                        {
                        	return null;
                        }
                    }
                    else
                    {
                        // ��ȥ��������ݰ�
                        if ((LocalBuffer[startIndex]& 0x00FF) != 0x5A)
                        {
                            startIndex++;
                            if (startIndex > LocalBuffer.length)
                            {
                                startIndex = 0;
                                if (startIndex > endIndex)
                                {
                                    startIndex = endIndex;
                                }
                            }
                        }

                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    }
}


