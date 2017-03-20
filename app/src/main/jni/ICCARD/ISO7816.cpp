#include "stdafx.h"
#include "ISO7816.h"

HANDLE m_UartHandle;

unsigned int Uart_Open(void)
{
	m_UartHandle = ::CreateFile(L"COM5",//COM1口
                   GENERIC_READ|GENERIC_WRITE, //允许读和写
		   0, //独占方式
		   NULL,
		   OPEN_EXISTING, //打开而不是创建
		   0, //同步方式
		   NULL);
//打开API 
	if (m_UartHandle != INVALID_HANDLE_VALUE) 
	{
		SetupComm(m_UartHandle,1024,1024); //输入缓冲区和输出缓冲区的大小都是1024
		COMMTIMEOUTS TimeOuts;
		//DCB ComDCB; //串口设备控制块
		GetCommTimeouts(m_UartHandle, &TimeOuts);
		//设定读超时
		TimeOuts.ReadIntervalTimeout = 1000;
		TimeOuts.ReadTotalTimeoutMultiplier = 1000;
		TimeOuts.ReadTotalTimeoutConstant = 5000;
		//设定写超时
		TimeOuts.WriteTotalTimeoutMultiplier = 1000;
		TimeOuts.WriteTotalTimeoutConstant = 2000;
		SetCommTimeouts(m_UartHandle, &TimeOuts); //设置超时

		DCB dcb;
		GetCommState(m_UartHandle, &dcb);
		dcb.BaudRate = 115200; //波特率为115200
		dcb.ByteSize = 8; //每个字节有8位
		dcb.Parity = NOPARITY; //无奇偶校验位
		dcb.StopBits = ONE5STOPBITS; //一个停止位
		SetCommState(m_UartHandle, &dcb);

		PurgeComm(m_UartHandle,PURGE_TXCLEAR|PURGE_RXCLEAR);
		return SUCCESS;
	}	
	return ERROR;
}

void Uart_Close(void)
{
	if(NULL != m_UartHandle)
	{
		::CloseHandle(m_UartHandle);
		m_UartHandle = NULL;
	}
}

unsigned int Uart_Send(unsigned char* pData, unsigned int dwLen)
{
	DWORD retlen = 0;
	if(m_UartHandle != NULL)
	{
		COMSTAT ComStat;
		DWORD dwErrorFlags;
		ClearCommError(m_UartHandle,&dwErrorFlags,&ComStat);

		::WriteFile(m_UartHandle,pData,dwLen,&retlen,NULL);

		dwLen = retlen;
	}

	return retlen;
}

unsigned int Uart_Recv(unsigned char* pData, unsigned int* dwLen)
{
	DWORD retlen = 0;
	if(m_UartHandle != NULL)
	{
		::ReadFile(m_UartHandle,pData,*dwLen,&retlen,NULL);
		*dwLen = retlen;

		PurgeComm(m_UartHandle, PURGE_TXABORT|
		PURGE_RXABORT|PURGE_TXCLEAR|PURGE_RXCLEAR);
	}
	return retlen;
}

unsigned int IC_Send_Recv(unsigned char* pRequestData, unsigned int dwReqlen, BYTE* pResponseData, unsigned int* dwRsplen)
{
	unsigned char RecBuf[271] = {0};
	*(pRequestData+0) = 0x1B;
	*(pRequestData+1) = 0x25;
	*(pRequestData+2) = 0x38;
	*(pRequestData+3) = 0x4D;
	
	if (!Uart_Send(pRequestData, dwReqlen)) 
	{
		return ERROR;
	}

	if (!Uart_Recv(RecBuf, dwRsplen)) 
	{
		return ERROR;
	}

	memcpy(pResponseData,RecBuf+1,*dwRsplen);

	return SUCCESS;
}
//建立文件
unsigned char IC_System_Reset(unsigned char* pRecBuf)
{
	//unsigned int i; 
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;

	if(pRecBuf == NULL)
		return Ret;
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);
	
	bCommandBuf[0] = 0x1B;
	bCommandBuf[1] = 0x25;
	bCommandBuf[2] = 0x21;
	bCommandBuf[3] = 0x4D;

	dwLen = 4;
	
	if (!Uart_Send(bCommandBuf, dwLen)) 
	{
		return Ret;
	}

	if (!Uart_Recv(RecBuf, &resplen)) 
	{
		return Ret;
	}

	memcpy(pRecBuf,RecBuf+1,resplen-1);

	Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;

	return Ret;  
}

unsigned char IC_ResetCard(unsigned char* pRecBuf)
{
	//unsigned int i; 
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;

	if(pRecBuf == NULL)
		return Ret;
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);
	
	bCommandBuf[0] = 0x1B;
	bCommandBuf[1] = 0x25;
	bCommandBuf[2] = 0x36;
	bCommandBuf[3] = 0x4D;

	dwLen = 4;
	
	if (!Uart_Send(bCommandBuf, dwLen)) 
	{
		return Ret;
	}

	if (!Uart_Recv(RecBuf, &resplen)) 
	{
		return Ret;
	}

	if((RecBuf[1] == 0x1B) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x00));

	memcpy(pRecBuf,RecBuf+5,*(RecBuf+4));

	Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;

	return Ret;  
}

unsigned char IC_CreateFile(CreateFile_MSG* msg)
{
	unsigned int i; 
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;

	if((msg == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);
	
	if(msg->SMART_ID == IC_SMARTCOS)
	{
		if(msg->File_Type == MASTER_FILE)
		{
			bCommandBuf[4]= 0x00;
			bCommandBuf[5]= 15+msg->mf->NameLen;//len
			bCommandBuf[6]= 0x80;//CLA
			bCommandBuf[7]= 0xE0;//INS
			bCommandBuf[8]= 0x00;//P1
			bCommandBuf[9]= 0x00;//P2
			bCommandBuf[10]= 0x0a+msg->mf->NameLen;//LC

			for(i = 0;i < 8;i++)
			{
				bCommandBuf[11+i] = msg->mf->TransCode[i];
			}

			bCommandBuf[19] = msg->mf->Authority[0];
			bCommandBuf[20] = msg->mf->FileId[0];

			if((msg->mf->FileName == NULL))
			{
				return Ret;
			}

			for(i = 0;i < msg->mf->NameLen;i++)
			{
				bCommandBuf[21+i] = *(msg->mf->FileName+i);
			}

			dwLen = 21+msg->mf->NameLen;
		}
		else if(msg->File_Type == DEDICATED_FILE)
		{
			bCommandBuf[4]= 0x00;
			bCommandBuf[5]= 9+msg->df->NameLen;//len
			bCommandBuf[6]= 0x80;//CLA
			bCommandBuf[7]= 0xE0;//INS
			bCommandBuf[8]= 0x01;//P1
			bCommandBuf[9]= 0x00;//P2
			bCommandBuf[10]= 0x04+msg->df->NameLen;//LC

			bCommandBuf[11] = msg->df->FileId[0];
			bCommandBuf[12] = msg->df->FileId[1];
			bCommandBuf[13] = msg->df->Authority[1];
			bCommandBuf[14] = 0x00;

			if((msg->df->FileName == NULL))
			{
				return Ret;
			}

			for(i = 0;i < msg->df->NameLen;i++)
			{
				bCommandBuf[15+i] = *(msg->df->FileName+i);
			}
			dwLen = 15+msg->df->NameLen;
		}
		else if(msg->File_Type == ELEMENTARY_FILE)
		{
			bCommandBuf[4]= 0x00;
			bCommandBuf[5]= 0x0C;//len
			bCommandBuf[6]= 0x80;//CLA
			bCommandBuf[7]= 0xE0;//INS
			bCommandBuf[8]= 0x02;//P1
			bCommandBuf[9]= 0x00;//P2
			bCommandBuf[10]= 0x07;//LC


			bCommandBuf[11]= msg->ef->FileId[0];
			bCommandBuf[12]= msg->ef->FileId[1];
			bCommandBuf[13]= msg->ef->FileType[0];
			bCommandBuf[14]= msg->ef->Authority1[0];
			bCommandBuf[15]= msg->ef->Authority2[0];
			bCommandBuf[16]= msg->ef->Len1[0];
			bCommandBuf[17]= msg->ef->Len2[0];
			//for(i = 0;i < 2;i++)
			//{
			//	bCommandBuf[11+i]= *(msg->FileID+i);//DATA
			//}
			dwLen = 18;
		}
	}
	else if(msg->SMART_ID == IC_TIMECOS)	
	{
		
	}
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];
			if((RecBuf[4] == 0x90)&&(RecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
	
	}
	return Ret;  
}

unsigned char IC_CreateFile_End(CreateFile_MSG* msg)
{
	unsigned int i; 
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;

	if(msg == NULL)
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);
	
	if(msg->SMART_ID == IC_SMARTCOS)
	{
		if(msg->File_Type == MASTER_FILE)
		{
			bCommandBuf[4]= 0x00;
			bCommandBuf[5]= 0x07;//len
			bCommandBuf[6]= 0x80;//CLA
			bCommandBuf[7]= 0xE0;//INS
			bCommandBuf[8]= 0x00;//P1
			bCommandBuf[9]= 0x01;//P2
			bCommandBuf[10]= 0x02;//LC
			bCommandBuf[11]= 0x3F;//DATA
			bCommandBuf[12]= 0x00;//DATA

			dwLen = 13;
		}
		else if(msg->File_Type == DEDICATED_FILE)
		{
			bCommandBuf[4]= 0x00;
			bCommandBuf[5]= 0x07;//len
			bCommandBuf[6]= 0x80;//CLA
			bCommandBuf[7]= 0xE0;//INS
			bCommandBuf[8]= 0x01;//P1
			bCommandBuf[9]= 0x01;//P2
			bCommandBuf[10]= 0x02;//LC
			bCommandBuf[11]= 0x2F;//DATA
			bCommandBuf[12]= 0x01;//DATA

			//for(i = 0;i < 2;i++)
			//{
			//	bCommandBuf[11+i]= *(msg->FileID+i);//DATA
			//}
			dwLen = 13;
		}
	}
	else if(msg->SMART_ID == IC_TIMECOS)	
	{
		
	}
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];
			if((RecBuf[4] == 0x90)&&(RecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[4] == 0x65)&&(RecBuf[5] == 0x81))
			{
				Ret = SCARD_WRITE_EEPROM_FAIL;
			}
			else if((RecBuf[4] == 0x67)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if(RecBuf[4] == 0x63)
			{
					Ret = RecBuf[5]&0x0F;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x01))
			{
					Ret = SCARD_CREATE_CONDITION_NOT_SATISFIED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_SECURITY_CONDITION_NOT_SATISFIED;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x80))
			{
					Ret = SCARD_IDENTIFIER_ALREADY_EXISTS;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x81))
			{
					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x84))
			{
					Ret = SCARD_NOT_ENOUGH_SPACE;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x86))
			{
					Ret = SCARD_PAREMETER_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x6D)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_INS_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x6E)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_CLA_IS_INCORRECT;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//增加或修改密钥
unsigned char IC_Write_KEY(KEY_MSG* msg)
{
	unsigned int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pKEY == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	if(msg->SMART_ID == IC_SMARTCOS)
	{
		if(msg->pKEY->KEY_InMode == PROCLAIMED)
		{
				bCommandBuf[4]= 0x00;
				bCommandBuf[5]= 0x05+msg->pKEY->PL->Key_Msglen;//len
				
				bCommandBuf[6]= 0x80;//CLA
				bCommandBuf[7]= 0xD4;//INS
				bCommandBuf[8]= msg->pKEY->PL->KEY_Opt;//P1
				bCommandBuf[9]= 0x00;//P2
				bCommandBuf[10]= msg->pKEY->PL->Key_Msglen;//LC LENGTH
				
				if(msg->pKEY->PL->Key_MsgData == NULL)
				{
					return Ret;
				}
				
				for(i = 0; i < msg->pKEY->PL->Key_Msglen; i++)
				{
					bCommandBuf[11+i] = *(msg->pKEY->PL->Key_MsgData+i);
				}
				
				dwLen = 11+msg->pKEY->PL->Key_Msglen;
		}
		else if(msg->pKEY->KEY_InMode == CIPHERTEXT)
		{
			bCommandBuf[4]= 0x00;
			bCommandBuf[5]= 0x05+msg->pKEY->Cht->Key_Datalen;//len
			
			bCommandBuf[6]= 0x80;//CLA
			bCommandBuf[7]= 0xD4;//INS
			bCommandBuf[8]= msg->pKEY->Cht->KEY_Type;//P1
			bCommandBuf[9]= msg->pKEY->Cht->KEY_ID;//P2
			bCommandBuf[10]= msg->pKEY->Cht->Key_Datalen;//LC LENGTH
			
			if(msg->pKEY->Cht->Key_InforData == NULL)
			{
				return Ret;
			}
				
			for(i = 0; i < msg->pKEY->Cht->Key_Datalen; i++)
			{
				bCommandBuf[11+i] = *(msg->pKEY->Cht->Key_InforData+i);
			}
			
			dwLen = 11+msg->pKEY->Cht->Key_Datalen;
		}
	}
	else if(msg->SMART_ID == IC_TIMECOS)	
	{
		
	}
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];
			if((RecBuf[4] == 0x90)&&(RecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[4] == 0x65)&&(RecBuf[5] == 0x81))
			{
				Ret = SCARD_WRITE_EEPROM_FAIL;
			}
			else if((RecBuf[4] == 0x67)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x01))
			{
					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x81))
			{
					Ret = SCARD_CMD_NOT_MATCH_TYPES; 
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_SECURITY_CONDITION_NOT_SATISFIED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x83))
			{
					Ret = SCARD_KEY_LOCK;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x84))
			{
					Ret = SCARD_GET_RANDOM_INVALID;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x85))
			{
					Ret = SCARD_CONDITION_OF_USE_NOT_SATISFIED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x88))
			{
					Ret = SCARD_MAC_INCORRECT;
			}
			
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x80))
			{
					Ret = SCARD_DATA_NOT_CORRECT;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x81))
			{
					Ret = SCARD_CARD_LOCK;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x84))
			{
					Ret = SCARD_FILE_SPACE_INSUFFICIENT;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x86))
			{
					Ret = SCARD_P1_AND_P2_NOT_CORRECT;
			}
			else if((RecBuf[4] == 0x6D)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_INS_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x6E)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_CLA_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x93)&&(RecBuf[5] == 0x03))
			{
					Ret = SCARD_APP_PERMANENT_LOCK;
			}
			else if((RecBuf[4] == 0x94)&&(RecBuf[5] == 0x03))
			{
					Ret = SCARD_KEY_NOT_FOUND;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//读二进制
unsigned char IC_Read_Binary(BINARY_MSG* msg)
{
	//uchar cReturnFlag;  
	//int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned char* pRecBuf;
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[4]= 0x00;
	bCommandBuf[5]= 0x05;//len
	
	bCommandBuf[6]= 0x00;//CLA
	bCommandBuf[7]= 0xB0;//INS
	bCommandBuf[8]= 0x80|(0x1F&(msg->FileId));//P1
	bCommandBuf[9]= msg->Offset;//P2
	bCommandBuf[10]= (msg->Count);//<0x110?(msg->Count):0x110;//LE LENGTH
	
	dwLen = 11;
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];

			memcpy(msg->pszRecBuf,&RecBuf[4],msg->Count);

			pRecBuf = RecBuf+msg->Count;

			if((pRecBuf[4] == 0x69)&&(pRecBuf[5] == 0x81))
			{
				Ret = SCARD_NOT_BINARY_FILE;
			}
			else if((pRecBuf[4] == 0x69)&&(pRecBuf[5] == 0x82))
			{
				Ret = SCARD_SECURITY_CONDITION_NOT_SATISFIED;
			}
			else if((pRecBuf[4] == 0x6A)&&(pRecBuf[5] == 0x81))
			{
				Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((pRecBuf[4] == 0x6A)&&(pRecBuf[5] == 0x82))
			{
				Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((pRecBuf[4] == 0x6B)&&(pRecBuf[5] == 0x00))
			{
				Ret = SCARD_PAREMETER_IS_INCORRECT;
			}
			else if(pRecBuf[4] == 0x6C)
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//修改二进制
unsigned char IC_Update_Binary(BINARY_MSG* msg)
{
	unsigned int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[4]= 0x00;
	bCommandBuf[5]= 0x05+msg->UBDatalen;//len
	if(msg->level == 0)
		bCommandBuf[6]= 0x00;//CLA
	else
		bCommandBuf[6]= 0x04;//CLA
	bCommandBuf[7]= 0xD6;//INS
	bCommandBuf[8]= 0x80|(0x1F&(msg->FileId));//P1
	bCommandBuf[9]= msg->Offset;//P2
	bCommandBuf[10]= msg->UBDatalen;//Lc LENGTH
	
	for(i = 0; i < msg->UBDatalen; i++)
	{
		bCommandBuf[11+i] = *(msg->UBData+i);
	}

	dwLen = 11+msg->UBDatalen;
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];

			if((RecBuf[4] == 0x90)&&(RecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[4] == 0x65)&&(RecBuf[5] == 0x81))
			{
				Ret = SCARD_WRITE_EEPROM_FAIL;
			}
			else if((RecBuf[4] == 0x67)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x81))
			{
					Ret = SCARD_NOT_BINARY_FILE;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_CONDITION_OF_CMD_NOT_SATISFIED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x84))
			{
					Ret = SCARD_GET_RANDOM_INVALID;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x85))
			{
					Ret = SCARD_CONDITION_OF_USE_NOT_SATISFIED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x88))
			{
					Ret = SCARD_SECURITY_DATA_NOT_CORRECT;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x80))
			{
					Ret = SCARD_DATA_NOT_CORRECT;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x81))
			{
					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x86))
			{
					Ret = SCARD_P1_AND_P2_NOT_CORRECT;
			}
			else if((RecBuf[4] == 0x6B)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_P1_AND_P2_OUT_OF_GAUGE;
			}
			else if((RecBuf[4] == 0x6D)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_INS_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x6E)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_CLA_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x93)&&(RecBuf[5] == 0x02))
			{
					Ret = SCARD_APP_PERMANENT_LOCK;
			}
			else if((RecBuf[4] == 0x93)&&(RecBuf[5] == 0x03))
			{
					Ret = SCARD_KEY_NOT_FOUND;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//读记录
unsigned char IC_Read_Record(Record_MSG* msg)
{
	//int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned char* pRecBuf;
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[4]= 0x00;
	bCommandBuf[5]= 0x05;//len
	
	bCommandBuf[6]= 0x00;//CLA
	bCommandBuf[7]= 0xB2;//INS
	bCommandBuf[8]= msg->Index;//P1
	bCommandBuf[9]= (msg->FileId<<3)|0x4;//P2
	bCommandBuf[10]= (msg->Count)<0x110?(msg->Count):0x110;//LE LENGTH
	
	dwLen = 11;
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];

			memcpy(msg->pszRecBuf,&RecBuf[4],msg->Count);

			pRecBuf = RecBuf+msg->Count;
			
			if((pRecBuf[4] == 0x90)&&(pRecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((pRecBuf[4] == 0x69)&&(pRecBuf[5] == 0x81))
			{
				Ret = SCARD_CMD_NOT_MATCH_TYPES;
			}
			else if((pRecBuf[4] == 0x69)&&(pRecBuf[5] == 0x82))
			{
				Ret = SCARD_CONDITION_OF_READ_NOT_SATISFIED;
			}
			else if((pRecBuf[4] == 0x69)&&(pRecBuf[5] == 0x86))
			{
				Ret = SCARD_CONDITION_OF_CMD_NOT_SATISFIED;
			}
			else if((pRecBuf[4] == 0x6A)&&(pRecBuf[5] == 0x81))
			{
				Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((pRecBuf[4] == 0x6A)&&(pRecBuf[5] == 0x82))
			{
				Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((pRecBuf[4] == 0x6A)&&(pRecBuf[5] == 0x83))
			{
				Ret = SCARD_RECORD_NOT_FOUND;
			}
			else if(pRecBuf[4] == 0x6C)
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//追加记录
unsigned char IC_Append_Record(Record_MSG* msg)
{
	unsigned int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[4]= 0x00;
	bCommandBuf[5]= 0x05+msg->UBDatalen;//len
	if(msg->level == 0)
		bCommandBuf[6]= 0x00;//CLA
	else
		bCommandBuf[6]= 0x04;//CLA
	bCommandBuf[7]= 0xE2;//INS
	bCommandBuf[8]= 0x00;//P1
	bCommandBuf[9]= (msg->FileId<<3)|0x0;//P2
	bCommandBuf[10]= msg->UBDatalen;//LC LENGTH

	for(i = 0; i < msg->UBDatalen; i++)
	{
		bCommandBuf[11+i] = *(msg->UBData+i);
	}

	dwLen = 11+msg->UBDatalen;
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];
			
			if((RecBuf[4] == 0x90)&&(RecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[4] == 0x65)&&(RecBuf[5] == 0x81))
			{
				Ret = SCARD_WRITE_EEPROM_FAIL;
			}
			else if((RecBuf[4] == 0x67)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x81))
			{
					Ret = SCARD_FILE_NOT_LINEAR_FIXED_FILE;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_CONDITION_OF_CMD_NOT_SATISFIED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x84))
			{
					Ret = SCARD_GET_RANDOM_INVALID;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x85))
			{
					Ret = SCARD_APP_TEMPORARY_LOCED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x88))
			{
					Ret = SCARD_MAC_INCORRECT;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x81))
			{
					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x83))
			{
					Ret = SCARD_RECORD_NOT_FOUND;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x84))
			{
					Ret = SCARD_FILE_STORAGE_SPACE_NOT_ENOUGH;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x86))
			{
					Ret = SCARD_P1_AND_P2_NOT_CORRECT;
			}
			else if((RecBuf[4] == 0x6D)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_INS_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x6E)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_CLA_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x93)&&(RecBuf[5] == 0x02))
			{
					Ret = SCARD_APP_PERMANENT_LOCK;
			}
			else if((RecBuf[4] == 0x93)&&(RecBuf[5] == 0x03))
			{
					Ret = SCARD_KEY_NOT_FOUND;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//修改记录
unsigned char IC_Update_Record(Record_MSG* msg)
{
	unsigned int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[4]= 0x00;
	bCommandBuf[5]= 0x05+msg->UBDatalen;//len
	if(msg->level == 0)
		bCommandBuf[6]= 0x00;//CLA
	else
		bCommandBuf[6]= 0x04;//CLA
	bCommandBuf[7]= 0xDC;//INS
	bCommandBuf[8]= msg->Index;//P1
	bCommandBuf[9]= (msg->FileId<<3)|0x4;//P2
	bCommandBuf[10]= msg->UBDatalen;//LC LENGTH
	
	for(i = 0; i < msg->UBDatalen; i++)
	{
		bCommandBuf[11+i] = *(msg->UBData+i);
	}

	dwLen = 11+msg->UBDatalen;
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];
			
			if((RecBuf[4] == 0x90)&&(RecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[4] == 0x65)&&(RecBuf[5] == 0x81))
			{
				Ret = SCARD_WRITE_EEPROM_FAIL;
			}
			else if((RecBuf[4] == 0x67)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x81))
			{
					Ret = SCARD_FILE_NOT_LINEAR_FIXED_FILE;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_CONDITION_OF_CMD_NOT_SATISFIED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x84))
			{
					Ret = SCARD_GET_RANDOM_INVALID;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x85))
			{
					Ret = SCARD_APP_TEMPORARY_LOCED;
			}
			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x88))
			{
					Ret = SCARD_MAC_INCORRECT;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x81))
			{
					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x82))
			{
					Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x83))
			{
					Ret = SCARD_RECORD_NOT_FOUND;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x84))
			{
					Ret = SCARD_FILE_STORAGE_SPACE_NOT_ENOUGH;
			}
			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x86))
			{
					Ret = SCARD_P1_AND_P2_NOT_CORRECT;
			}
			else if((RecBuf[4] == 0x6D)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_INS_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x6E)&&(RecBuf[5] == 0x00))
			{
					Ret = SCARD_CLA_IS_INCORRECT;
			}
			else if((RecBuf[4] == 0x93)&&(RecBuf[5] == 0x02))
			{
					Ret = SCARD_APP_PERMANENT_LOCK;
			}
			else if((RecBuf[4] == 0x93)&&(RecBuf[5] == 0x03))
			{
					Ret = SCARD_KEY_NOT_FOUND;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//选择文件
unsigned char IC_Select_File(SelectFile_MSG* msg)
{
	unsigned int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned char* pRecBuf;
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[4]= 0x00;
	bCommandBuf[5]= 0x05+msg->SFDatalen;//len
	
	bCommandBuf[6]= 0x00;//CLA
	bCommandBuf[7]= 0xA4;//INS
	bCommandBuf[8]= msg->FileType;//P1
	bCommandBuf[9]= msg->FileIndex;//(msg->FileIndex<<3)|0x4;//P2
	bCommandBuf[10]= msg->SFDatalen;//LC LENGTH
	
	for(i = 0;i < msg->SFDatalen;i++)
	{
		bCommandBuf[11+i] = *(msg->SFData+i);
	}
	
	dwLen = 11+msg->SFDatalen;
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];
			PackageLen = PackageLen-2;

			if(PackageLen>0)
			{
				memcpy(msg->pszRecBuf,&RecBuf[4],PackageLen);
			}

			pRecBuf = RecBuf+PackageLen;
			
			if((pRecBuf[4] == 0x90)&&(pRecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			if(pRecBuf[4] == 0x61)
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((pRecBuf[4] == 0x67)&&(pRecBuf[5] == 0x00))
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((pRecBuf[4] == 0x6A)&&(pRecBuf[5] == 0x81))
			{
				Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((pRecBuf[4] == 0x6A)&&(pRecBuf[5] == 0x82))
			{
				Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((pRecBuf[4] == 0x6A)&&(pRecBuf[5] == 0x86))
			{
				Ret = SCARD_P1_AND_P2_NOT_CORRECT;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//圈存
unsigned char IC_Credit_For_Load(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//消费/取现
unsigned char IC_Debit_For_Purchase(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//圈提
unsigned char IC_Debit_For_Unload(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//读余额
unsigned char IC_Get_Balance(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//取交易认证
unsigned char IC_Get_Transaction_Prove(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//取现初始化
unsigned char IC_Initialize_For_Case_Withdraw(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//圈存初始化
unsigned char IC_Initial_For_Load(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//消费初始化
unsigned char IC_Initial_For_Purchase(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//圈提初始化
unsigned char IC_Initial_For_Unload(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//修改初始化
unsigned char IC_Initial_For_Update(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//修改透支限额
unsigned char IC_Update_Overdraw_Limit(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//应用锁定
unsigned char IC_Application_Block(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//应用解锁
unsigned char IC_Application_Unlock(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//卡片锁定
unsigned char IC_Card_Block(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//外部认证
unsigned char IC_External_authentication(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//产生随机数
unsigned char IC_Get_Challenge(Challenge_MSG* msg)
{
	//int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned char* pRecBuf;
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[4]= 0x00;
	bCommandBuf[5]= 0x05;//len
	
	bCommandBuf[6]= 0x00;//CLA
	bCommandBuf[7]= 0x84;//INS
	bCommandBuf[8]= 0x00;//P1
	bCommandBuf[9]= 0x00;//P2
	bCommandBuf[10]= 0x04;//Le LENGTH
	
	dwLen = 11;
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];
			PackageLen = PackageLen-2;

			msg->Count = PackageLen;
			memcpy(msg->pszRecBuf,&RecBuf[4],PackageLen);

			pRecBuf = RecBuf+PackageLen;
			
			if((pRecBuf[4] == 0x90)&&(pRecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((pRecBuf[4] == 0x67)&&(pRecBuf[5] == 0x00))
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((pRecBuf[4] == 0x6A)&&(pRecBuf[5] == 0x81))
			{
				Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//取响应
unsigned char IC_Get_Response(Response_MSG* msg)
{
	//int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned char* pRecBuf;
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return Ret;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[4]= 0x00;
	bCommandBuf[5]= 0x05;//len
	
	bCommandBuf[6]= 0x00;//CLA
	bCommandBuf[7]= 0xC0;//INS
	bCommandBuf[8]= 0x00;//P1
	bCommandBuf[9]= 0x00;//P2
	bCommandBuf[10]= msg->Count;//Le LENGTH
	
	dwLen = 11;
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //接收到返回信息
		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
		{
			Ret = SCARD_ABSENT;
		}
		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
		{
			PackageLen = RecBuf[2]*256;
			PackageLen = PackageLen + RecBuf[3];
			PackageLen = PackageLen-2;

			memcpy(msg->pszRecBuf,&RecBuf[4],PackageLen);

			pRecBuf = RecBuf+PackageLen;
			
			if((pRecBuf[4] == 0x90)&&(pRecBuf[5] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((pRecBuf[4] == 0x67)&&(pRecBuf[5] == 0x00))
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((pRecBuf[4] == 0x6F)&&(pRecBuf[5] == 0x00))
			{
				Ret = SCARD_NO_DATA_RETURN;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}
			 
	}
	return Ret;  
}
//内部认证
unsigned char IC_Internal_Authentication(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//修改/解锁PIN
unsigned char IC_PIN_Change_OR_Unblock(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//校验PIN
unsigned char IC_Verify_PIN(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//修改PIN
unsigned char IC_Change_PIN(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//重装PIN
unsigned char IC_Reload_PIN(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//安全模块指令
unsigned char IC_Crypt(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
//生成过程密钥
unsigned char IC_Generate_KEY()
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}