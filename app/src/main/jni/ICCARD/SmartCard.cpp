/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <strings.h>
#include <jni.h>
#include <fcntl.h>
#include <termios.h>
#include <stdio.h>
#include <android/log.h> 
#include "ISO7816.h"
/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/HelloJni/HelloJni.java
 */
#define  LOG_TAG    "libsmartcard"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
extern "C" {
	JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitClassName(JNIEnv *env,jobject thiz,jbyteArray jclassName,jbyteArray MethodSend,jbyteArray MethodRecv);
	JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_DeInitClassName(JNIEnv *env,jobject thiz);

    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_UartOpen(JNIEnv *env,jobject thiz);

    JNIEXPORT jobject JNICALL Java_com_corewise_logic_SmartCard_UartClose(JNIEnv *env,jobject thiz);

    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitMF(
    JNIEnv* env, jobject thiz,jboolean TransCode,
    jboolean Authority,jboolean FileId,jint NameLen,jbyteArray MFName
    )  ; 
       
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitADF(
    JNIEnv* env, jobject thiz, jchar FileId,jboolean Authority,jint NameLen,jbyteArray ADFName
    )   ;

    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitBEF(
    JNIEnv* env, jobject thiz, jchar FileId,jboolean FileType,jchar Authority,jchar Len
    )   ;

    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitREF(
    JNIEnv* env, jobject thiz, jchar FileId,jboolean FileType,jchar Authority,jchar Len
    )   ;

    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_WriteKEY(
        JNIEnv* env, 
        jobject thiz
    )   ;
    
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_ReadIBAN(
	    JNIEnv* env, 
	    jobject thiz,
	    jint Count,
	    jbyteArray jarray
	)   ;
       
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_ReadBinary(
        JNIEnv* env, 
        jobject thiz,
        jboolean FileId,
        jboolean Offset,
        jint Count,
        jbyteArray jarray
    )   ;
       
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_UpdateBinary(
        JNIEnv* env, 
        jobject thiz,
        jboolean FileId,
        jboolean Offset,
        jint level,
        jbyteArray jarray,
        jint Count
    )   ;
       
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_ReadRecord(
        JNIEnv* env, 
        jobject thiz,
        jboolean FileId,
        jint Index,
        jbyteArray jarray,
        jint Count
    )   ;
      
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_AppendRecord(
        JNIEnv* env, 
        jobject thiz,
        jboolean FileId,
        jint level,
        jbyteArray jarray,
        jint Count
    )   ;
     
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_UpdateRecord(
        JNIEnv* env, 
        jobject thiz,
        jboolean FileId,
        jint level,
        jint Index,
        jbyteArray jarray,
        jint Count
    )   ;
     
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_SelectFile(
        JNIEnv* env, 
        jobject thiz,
        jint FileType,
        jint FileIndex,
        jint SFDatalen,
        jbyteArray FileId
    )   ;
     
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_GetChallenge(
        JNIEnv* env, 
        jobject thiz,
        jint Count,
        jbyteArray jarray
    )   ;
     
    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_GetResponse(
        JNIEnv* env, 
        jobject thiz,
        jint Count,
        jbyteArray jarray
    );    

    JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_DeleteFile(JNIEnv *env,jobject thiz);

};

int  fd;
char *pszclassName = NULL;
char *pszMethodSend = NULL;
char * pszMethodRecv = NULL;

JNIEnv* btenv;
	
static int set_opt(int fd,int nSpeed, int nBits, char nEvent, int nStop)
{
		struct termios newtio,oldtio;
/*闁跨喐鏋婚幏绌僽017d闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗\u017d闁跨喐鏋婚幏鐤槵闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻敓锟�
		if(tcgetattr(fd,&oldtio) != 0) {
			printf("Save Feild! \n");
			return -1;
		}else 
		printf("Save succeed \n");
		bzero(&newtio,sizeof(newtio));
/*\u0152\u20ac闁跨喐鏋婚幏绌僽0178闁跨喐鏋婚幏鐑芥晸缁叉悥017d\u0153闂嗗秹鏁撶徊鎼�153闁跨喐鏋婚幏鐑芥晸缂佺偟娅㈤幏鐑芥晸閿燂拷
		newtio.c_cflag |= CLOCAL | CREAD;
/*闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔活敎閸戙倖瀚筡u017d闁跨喎褰ㄩ敓锟�
		newtio.c_cflag &= ~CSIZE;
		switch(nBits) {
		case 7:
				newtio.c_cflag |= CS7;
				break;
		case 8:
				newtio.c_cflag |= CS8;
				break;
		}
/*闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撶徊鎼�152闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�/
		switch(nEvent) {
			case 'O'://闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�
				newtio.c_cflag |= PARENB;
				newtio.c_cflag |= PARODD;
				newtio.c_iflag |= (INPCK | ISTRIP);
				break;
			case 'E'://闁跨喓绁縰0152闁跨喐鏋婚幏锟�				newtio.c_iflag |= (INPCK | ISTRIP);
				newtio.c_cflag |= PARENB;
				newtio.c_cflag &= ~PARODD;
					break;
			case 'N'://闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔虹タu0152閺嶏繝鏁撻弬銈嗗
				newtio.c_cflag &= ~PARENB;
				break;
		}
/*闁跨喐鏋婚幏鐑芥晸閻偆鐎硊0161闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�/
		switch(nSpeed) {
		case 2400:
				cfsetispeed(&newtio,B2400);
				cfsetospeed(&newtio,B2400);
				break;
		case 4800:
				cfsetispeed(&newtio,B4800);
				cfsetospeed(&newtio,B4800);
				break;
		case 9600:
				cfsetispeed(&newtio,B9600);
				cfsetospeed(&newtio,B9600);
				break;
		case 19200:
				cfsetispeed(&newtio,B19200);
				cfsetospeed(&newtio,B19200);
				break;
		case 115200:
				cfsetispeed(&newtio,B115200);
				cfsetospeed(&newtio,B115200);
				break;
		case 460800:
				cfsetispeed(&newtio,B460800);
				cfsetospeed(&newtio,B460800);
				break;
		default:	//姒涙﹢鏁撻弬銈嗗9600
				cfsetispeed(&newtio,B9600);
				cfsetospeed(&newtio,B9600);
				break;
		}
/*闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归崑婊勵剾娴ｏ拷/
		if(nStop==1)
				newtio.c_cflag &= ~CSTOPB;
		else if (nStop == 2)
				newtio.c_cflag |= CSTOPB;
/*闁跨喐鏋婚幏鐑芥晸閻偆顣幏绌僽017d闁跨喓绮搁惄绉�152闁跨喐鏋婚幏鐑芥晸閺傘倖瀚圭亸寤玼0153闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归崸锟芥晸閿燂拷
		newtio.c_cc[VTIME] = 0;
		newtio.c_cc[VMIN] = 1;
/*\u017d\u0160闁跨喐鏋婚幏鐑芥晸缁叉悥017d\u0153闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归崸锟芥晸閿燂拷
		tcflush(fd,TCIOFLUSH);
/*闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�闁跨喕濮ら懠鍐晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗*/
	newtio.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
	newtio.c_oflag &= OPOST;
	newtio.c_iflag &= ~(INLCR|IGNCR|ICRNL);
	newtio.c_oflag &= ~(ONLCR|OCRNL);
/*\u0152\u20ac闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗*/
		if((tcsetattr(fd,TCSANOW,&newtio)) != 0) {
				printf("Port Set Error \n");
				return 0;
		}
		printf("Port Set Succeed! \n");
		return 1;
}


static int open_port(int fd,const char *Dev)
{
	//char *dev[]={"/dev/ttySAC0","/dev/ttySAC1","/dev/ttySAC2","/dev/ttySAC3"};
	
	fd = open( Dev, O_RDWR|O_NOCTTY|O_NDELAY);
	if (-1 == fd){
		perror("Can't Open Serial Port");
		return(-1);
	}
	
	if(fcntl(fd, F_SETFL, 0)<0)
		printf("fcntl failed!\n");
	else
		printf("fcntl=%d\n",fcntl(fd, F_SETFL,0));
	if(isatty(STDIN_FILENO)==0)
		printf("standard input is not a terminal device\n");
	else
		printf("isatty success!\n");
	printf("fd-open=%d\n",fd);
	return fd;
}

#if 0

JNIEXPORT jint JNICALL Java_android_serialport_SmartCard_open(JNIEnv *env, jobject thiz, jstring path, jint baudrate) {
	/* Opening device */
	int  fd;
	const char *path_utf = path;//(*env)->GetStringUTFChars(env, path, NULL);//&iscopy
	printf("Opening serial port %s", path_utf);

	if((fd=open_port(fd,path_utf))<0)
	{
		
		perror("open_port fd0 error");
		
		return -1;
	}
	else
	{
		if((set_opt(fd,baudrate,8,'N',1))<0)
		{
			perror("set_opt error");
			
			return -1;
		}
		return fd;
	}
}

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_write(JNIEnv *env, jobject thiz, jint fd, jbyteArray buf, jsize count)
{
	const char *buf_utf = buf;//(*env)->GetStringUTFChars(env, buf, NULL);//&iscopy
	//printf("Opening serial buf_utf %s", buf_utf);
	return write(fd,buf_utf,count);
}

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_read(JNIEnv *env, jobject thiz, jint fd, jbyteArray buf, jsize count)
{
	const char *buf_utf = buf;//(*env)->GetStringUTFChars(env, buf, NULL);//&iscopy
	return read(fd,buf_utf,count);
}

JNIEXPORT jobject JNICALL Java_com_corewise_logic_SmartCard_close(JNIEnv *env, jobject thiz, jint fd)
{
	return close(fd);
}
#endif
JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitClassName(JNIEnv *env,jobject thiz,jbyteArray jclassName,jbyteArray jMethodSend,jbyteArray jMethodRecv)
{
	//"com/android/BluetoothChat/BluetoothChat","BluetoothSend","BluetoothRecv"
    if((pszclassName != NULL)&&(pszMethodSend != NULL)&&(pszMethodRecv != NULL))
        return 0;
    LOGI("Java_com_corewise_logic_SmartCard_InitClassName GetMethodID is get %s,%s",pszclassName,pszMethodSend);
	char *psztempName = (char*)env->GetByteArrayElements(jclassName, 0);
	char *psztempMethodSend = (char*)env->GetByteArrayElements(jMethodSend, 0);
	char * psztempMethodRecv = (char*)env->GetByteArrayElements(jMethodRecv, 0);

	int nclassLen = env->GetArrayLength(jclassName);
	int nSendLen = env->GetArrayLength(jMethodSend);
	int nRecvLen = env->GetArrayLength(jMethodRecv);

	if(pszclassName == NULL)
	{
		pszclassName = (char *)malloc(nclassLen+1);
        memset(pszclassName,0,nclassLen+1);
	}

	if(pszclassName != NULL)
	{
		memcpy(pszclassName,psztempName,nclassLen);
	}else
	{
		return 0;
	}

	if(pszMethodSend == NULL)
	{
		pszMethodSend = (char *)malloc(nSendLen+1);
        memset(pszMethodSend,0,nSendLen+1);
	}

	if(pszMethodSend != NULL)
	{
		memcpy(pszMethodSend,psztempMethodSend,nSendLen);
	}
    else
	{
		return 0;
	}

	if(pszMethodRecv == NULL)
	{
		pszMethodRecv = (char *)malloc(nRecvLen+1);
        memset(pszMethodRecv,0,nRecvLen+1);
	}

	if(pszMethodRecv != NULL)
	{
		memcpy(pszMethodRecv,psztempMethodRecv,nRecvLen);
	}else
	{
		return 0;
	}

    env->ReleaseByteArrayElements(jclassName, (jbyte*)psztempName, 0);
    env->ReleaseByteArrayElements(jMethodSend, (jbyte*)psztempMethodSend, 0);
    env->ReleaseByteArrayElements(jMethodRecv, (jbyte*)psztempMethodRecv, 0);

	return 1;
}

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_DeInitClassName(JNIEnv *env,jobject thiz)
{
    LOGI("Java_com_corewise_logic_SmartCard_DeInitClassName GetMethodID is get %s,%s",pszclassName,pszMethodSend);
	
	if(pszclassName != NULL)
	{
		free(pszclassName);
		pszclassName = NULL;
	}
	if(pszMethodSend != NULL)
	{
		free(pszMethodSend);
		pszMethodSend = NULL;
	}
	if(pszMethodRecv != NULL)
	{
		free(pszMethodRecv);
		pszMethodRecv = NULL;
	}
}


JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_UartOpen(JNIEnv *env,jobject thiz)
{
	/* Opening device */
    char path[] = "/dev/ttyMT0";
    int baudrate = 115200;
	const char *path_utf = path;//(*env)->GetStringUTFChars(env, path, NULL);//&iscopy
	printf("Opening serial port %s", path_utf);

	if((fd=open_port(fd,path_utf))<0)
	{
		
		perror("open_port fd0 error");
		
		return -1;
	}
	else
	{
		if((set_opt(fd,baudrate,8,'N',1))<0)
		{
			perror("set_opt error");
			
			return -1;
		}
		return fd;
	}
}

JNIEXPORT jobject JNICALL Java_com_corewise_logic_SmartCard_UartClose(JNIEnv *env,jobject thiz)
{
	close(fd);
}
//JNIEXPORT jint JNICALL Java_com_corewise_logic_SerialPort_Set_Bluetooth_Channel(JNIEnv* env, jobject thiz)
//{
	//jclass clz = env->FindClass("com/example/android/BluetoothChat/BluetoothChat");  
	//闁跨喐鏋婚幏宄板絿clz闁跨喍鑼庨惂鍛婂闁跨喎鐪鹃崙浠嬫晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻幓顓濈串閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏锟�	//jmethodID ctor = env->GetMethodID(clz, "<init>", "()V");
	//jobject obj = env->NewObject(clz, ctor);

	// 闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归崑婊堟晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏鐑芥晸鐟欐帒搴滈幏鐑芥晸缁诧拷闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏绌抧t[] intArray,闁跨喐鏋婚幏鐑芥晸閹恒儻璁ｉ幏鐑芥晸閺傘倖瀚归柨鐔诲▏閻氬不,闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗String[] strArray闁跨喐鏋婚幏宄扮安娑撶Ljava/lang/String;
	//jmethodID BluetoothSend = env->GetMethodID(clz, "BluetoothSend", "([B)I");
	//jmethodID BluetoothRecv = env->GetMethodID(clz, "BluetoothRecv", "([BI)I");

//    return 1;
//}
	
JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitMF(
JNIEnv* env, jobject thiz,jboolean TransCode,
jboolean Authority,jboolean FileId,jint NameLen,jbyteArray MFName
)   
{   
    int ret;
	int i;
	LOGI("NameLen=%d",NameLen);
	unsigned char* pNameData = (unsigned char*)env->GetByteArrayElements(MFName, 0);
	LOGI("MFName=%d,%d,%d,%d,%d,%d",MFName[0],MFName[1],MFName[2],MFName[3],MFName[4],MFName[5]);
    btenv = env;
    CreateFile_MSG* msg = (CreateFile_MSG*)malloc(sizeof(CreateFile_MSG));
    if(msg == NULL)
		return -1;

    msg->SMART_ID = IC_SMARTCOS;
	msg->mf = (CMF *)malloc(sizeof(CMF));
	if(msg->mf == NULL)
		return -1;

	msg->File_Type = MASTER_FILE;

	for(i = 0;i<8;i++)
	{
		msg->mf->TransCode[i] = TransCode; 
	}

	msg->mf->Authority[0] = Authority; 
	msg->mf->FileId[0] = FileId;
	msg->mf->NameLen = NameLen;
	msg->mf->FileName = (unsigned char *)malloc(sizeof(unsigned char)*(msg->mf->NameLen));

	if(msg->mf->FileName != NULL)     
	{
		//char MFNameX[] = {0x31,0x50,0x41,0x59,0x2E,0x53,0x59,0x53,0x2E,0x44,0x44,0x46,0x30,0x31};
		memcpy(msg->mf->FileName,pNameData,msg->mf->NameLen);
	}

    IC_CreateFile(msg);

    ret = IC_CreateFile_End(msg);
    env->ReleaseByteArrayElements(MFName, (jbyte*)pNameData, 0);
    free(msg->mf->FileName);
	msg->mf->FileName = NULL;

	free(msg->mf);
	msg->mf = NULL;

    free(msg);
	msg = NULL;
    return ret;
}   

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitADF(
JNIEnv* env, jobject thiz, jchar FileId,jboolean Authority,jint NameLen,jbyteArray ADFName
)   
{   
    int ret;
    btenv = env;
    unsigned char* pNameData = (unsigned char*)env->GetByteArrayElements(ADFName, 0);
    CreateFile_MSG* msg = (CreateFile_MSG*)malloc(sizeof(CreateFile_MSG));   
    if(msg == NULL)
		return -1;
    msg->SMART_ID = IC_SMARTCOS;
    //unsigned char ADF[] = {0xA0,0x00,0x00,0x00,0x03,0x86,0x98,0x07,0x01};
	msg->df = (CDF *)malloc(sizeof(CDF));
	if(msg->df == NULL)
		return -1;
	msg->File_Type = DEDICATED_FILE;

	msg->df->FileId[0] = FileId>>8;
	msg->df->FileId[1] = FileId&0x00FF;
	msg->df->Authority[0] = Authority;
	msg->df->NameLen = NameLen;
	msg->df->FileName = (unsigned char *)malloc(sizeof(unsigned char)*(msg->df->NameLen));
	if(msg->df->FileName != NULL)
	{
		memcpy(msg->df->FileName,pNameData,msg->df->NameLen);
	}
    
    ret = IC_CreateFile(msg); 

    ret = IC_CreateFile_End(msg);
    env->ReleaseByteArrayElements(ADFName, (jbyte*)pNameData, 0);
    free(msg->df->FileName);
	msg->df->FileName = NULL;
	
	free(msg->df);
	msg->df = NULL;

	free(msg);
	msg = NULL;
    
    return ret;
}

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitBEF(
JNIEnv* env, jobject thiz, jchar FileId,jboolean FileType,jchar Authority,jchar Len
)   
{   
    CreateFile_MSG* msg;
	int ret;
    btenv = env;
	//CString temp;
	
	//unsigned char ret;
	msg = (CreateFile_MSG *)malloc(sizeof(CreateFile_MSG));

	if(msg == NULL)
		return -1;

	msg->SMART_ID = IC_SMARTCOS;

	msg->ef = (CEF *)malloc(sizeof(CEF));
	if(msg->ef == NULL)
		return -1;
	msg->File_Type = ELEMENTARY_FILE;

	msg->ef->FileId[0] = FileId>>8;
	msg->ef->FileId[1] = FileId&0x00FF;
	msg->ef->FileType[0] = FileType;
	msg->ef->Authority1[0] = Authority>>8;
	msg->ef->Authority2[0] = Authority&0x00FF;
	msg->ef->Len1[0] = Len>>8;
	msg->ef->Len2[0] = Len&0x00FF;//0x27;
	ret = IC_CreateFile(msg);

	free(msg->ef);
	msg->ef = NULL;

	free(msg);
	msg = NULL;
    
    return ret;
}

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_InitREF(
JNIEnv* env, jobject thiz, jchar FileId,jboolean FileType,jchar Authority,jchar Len
)   
{   
    CreateFile_MSG* msg;
	int ret;
    btenv = env;

	msg = (CreateFile_MSG *)malloc(sizeof(CreateFile_MSG));

	if(msg == NULL)
		return -1;

	msg->SMART_ID = IC_SMARTCOS;

	msg->ef = (CEF *)malloc(sizeof(CEF));
	if(msg->ef == NULL)
		return -1;
	msg->File_Type = ELEMENTARY_FILE;

	msg->ef->FileId[0] = FileId>>8;
	msg->ef->FileId[1] = FileId&0x00FF; 
	msg->ef->FileType[0] = FileType;
	msg->ef->Authority1[0] = Authority>>8;
	msg->ef->Authority2[0] = Authority&0x00FF;
	msg->ef->Len1[0] = Len>>8;
	msg->ef->Len2[0] = Len&0x00FF;//0x27;
	ret = IC_CreateFile(msg);

	free(msg->ef);
	msg->ef = NULL;

	free(msg);
	msg = NULL;
    
    return ret;
}

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_WriteKEY(
    JNIEnv* env, 
    jobject thiz
)   
{   
    

}   

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_ReadIBAN(
    JNIEnv* env, 
    jobject thiz,
    jint Count,
    jbyteArray jarray
)   
{   
	LOGI("Java_com_corewise_logic_SmartCard_ReadIBAN = %d", Count);
    btenv = env;
    int ret;
    IBAN_MSG* msg;
	msg = (IBAN_MSG *)malloc(sizeof(IBAN_MSG));
	//LOGI("IC_Send_Recv is get %d,%d",dwLen,resplen);
	ret = IC_Read_IBAN(msg);
	if(Count>8)
		Count = 8;
	env->SetByteArrayRegion(jarray, 0, Count, (jbyte*)msg->IBANCode);
    //LOGI("ret = %X",ret); 
    free(msg);
	msg = NULL;
    return ret;

}   

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_ReadBinary(
    JNIEnv* env, 
    jobject thiz,
    jboolean FileId,
    jboolean Offset,
    jint Count,
    jbyteArray jarray
)   
{   
	LOGI("Java_com_corewise_logic_SerialPort_ReadBinary = %d", Count);
    btenv = env;
    int ret;
    BINARY_MSG* msg;
	msg = (BINARY_MSG *)malloc(sizeof(BINARY_MSG));
	msg->FileId = FileId;
	msg->Offset = Offset;
	//msg->level = 0;
	//msg->UBData = ;
	//msg->UBDatalen;
	msg->Count = Count;
	msg->pszRecBuf = (unsigned char *)malloc(Count);
	//LOGI("Java_com_corewise_logic_SerialPort_ReadBinary = %d", Count);
	//ret = IC_Read_Binary(msg);
	ret = IC_Read_Binary(msg);
	//LOGI("IC_Read_Binary = %d,msg->pszRecBuf =%s", ret,msg->pszRecBuf);
    //LOGI("msg->pszRecBuf[0]=0x%x,msg->pszRecBuf[1]=0x%x,msg->pszRecBuf[2]=0x%x,msg->pszRecBuf[3]=0x%x,msg->pszRecBuf[4]=0x%x,msg->pszRecBuf[5]=0x%x,msg->pszRecBuf[6]=0x%x,msg->pszRecBuf[7]=0x%x,msg->pszRecBuf[8]=0x%x,msg->pszRecBuf[9]=0x%x", msg->pszRecBuf[0],msg->pszRecBuf[1],msg->pszRecBuf[2],msg->pszRecBuf[3],msg->pszRecBuf[4],msg->pszRecBuf[5],msg->pszRecBuf[6],msg->pszRecBuf[7],msg->pszRecBuf[8],msg->pszRecBuf[9]);
    env->SetByteArrayRegion(jarray, 0, Count, (jbyte*)msg->pszRecBuf);
    free(msg->pszRecBuf);
	msg->pszRecBuf = NULL;
    free(msg);
	msg = NULL;
    return ret;

}   

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_UpdateBinary(
    JNIEnv* env, 
    jobject thiz,
    jboolean FileId,
    jboolean Offset,
    jint level,
    jbyteArray jarray,
    jint Count
)   
{   
    BINARY_MSG* msg;
	int ret ;
    btenv = env;

	msg = (BINARY_MSG *)malloc(sizeof(BINARY_MSG));

	if(msg == NULL)
		return -1;

	msg->FileId = FileId;
	msg->Offset = Offset;
	msg->level = level;
	
	//msg->UBData = (unsigned char *)malloc(sizeof(unsigned char)*(Count));
	//if(msg->UBData == NULL)
	//	return -1;

	//memset(msg->UBData,0,sizeof(unsigned char)*(Count));
	//memcpy((char *)msg->UBData,RecBuf,Count); 
    msg->UBData = (unsigned char*)env->GetByteArrayElements(jarray, 0);
	msg->UBDatalen = Count;
	ret = IC_Update_Binary(msg);
    env->ReleaseByteArrayElements(jarray, (jbyte*)msg->UBData, 0);
	//free(msg->UBData);
	//msg->UBData = NULL;
	free(msg);
	msg = NULL;

    return ret;
}   

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_ReadRecord(
    JNIEnv* env, 
    jobject thiz,
    jboolean FileId,
    jint Index,
    jbyteArray jarray,
    jint Count
)   
{   
    int ret;
    btenv = env;
    Record_MSG* msg;
	msg = (Record_MSG *)malloc(sizeof(Record_MSG));
	msg->FileId = FileId;
	msg->Index = Index;
	msg->Count = Count;
	msg->pszRecBuf = (unsigned char *)malloc(Count);
	ret = IC_Read_Record(msg);
    env->SetByteArrayRegion(jarray, 0, Count, (jbyte*)msg->pszRecBuf);
    free(msg->pszRecBuf);
	msg->pszRecBuf = NULL;
	free(msg);
	msg = NULL;
    return ret;
}  

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_AppendRecord(
    JNIEnv* env, 
    jobject thiz,
    jboolean FileId,
    jint level,
    jbyteArray jarray,
    jint Count
)   
{   
	int ret;
    btenv = env;
    Record_MSG* msg;

	msg = (Record_MSG *)malloc(sizeof(Record_MSG));

	if(msg == NULL)
		return -1;

	msg->FileId = FileId;
	msg->level = level;
	
	//msg->UBData = (unsigned char *)malloc(sizeof(unsigned char)*(Count));
	//if(msg->UBData == NULL)
	//	return -1;

	//memset(msg->UBData,0,sizeof(unsigned char)*(Count));
	//memcpy((char *)msg->UBData,RecBuf,Count);
    msg->UBData = (unsigned char*)env->GetByteArrayElements(jarray, 0);

	//CharToBCD((const char *)BCDData,(char *)msg->UBData);

	msg->UBDatalen = Count;

	ret = IC_Append_Record(msg);
    env->ReleaseByteArrayElements(jarray, (jbyte*)msg->UBData, 0);
////////////////////////////////////////////////
	//free(msg->UBData);
	//msg->UBData = NULL;
	free(msg);
	msg = NULL;
    return ret;
} 

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_UpdateRecord(
    JNIEnv* env, 
    jobject thiz,
    jboolean FileId,
    jint level,
    jint Index,
    jbyteArray jarray,
    jint Count
)   
{   
	int ret;
    Record_MSG* msg;
    btenv = env;

	msg = (Record_MSG *)malloc(sizeof(Record_MSG));

	if(msg == NULL)
		return -1;

	msg->FileId = FileId;
	msg->level = level;
    msg->Index = Index;
	
	//msg->UBData = (unsigned char *)malloc(sizeof(unsigned char)*(Count));
	//if(msg->UBData == NULL)
	//	return -1;

	//memset(msg->UBData,0,sizeof(unsigned char)*(Count));
	//memcpy((char *)msg->UBData,RecBuf,Count);

	//CharToBCD((const char *)BCDData,(char *)msg->UBData);
	msg->UBData = (unsigned char*)env->GetByteArrayElements(jarray, 0);

	msg->UBDatalen = Count;

	ret = IC_Update_Record(msg);
    env->ReleaseByteArrayElements(jarray, (jbyte*)msg->UBData, 0);
////////////////////////////////////////////////
	//free(msg->UBData);
	//msg->UBData = NULL;
	free(msg);
	msg = NULL;
    return ret;
} 

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_SelectFile(
    JNIEnv* env, 
    jobject thiz,
    jint FileType,
    jint FileIndex,
    jint SFDatalen,
    jbyteArray FileId
)   
{   
	unsigned int i;
    int ret;
    btenv = env;
	SelectFile_MSG* msg;

	msg = (SelectFile_MSG *)malloc(sizeof(SelectFile_MSG));

	if(msg == NULL)
		return -1;

	msg->FileType = FileType;
	msg->FileIndex = FileIndex;

	msg->SFDatalen = SFDatalen;

	//msg->SFData = (unsigned char *)malloc(sizeof(unsigned char)*(msg->SFDatalen));
	//if(msg->SFData == NULL)
	//	return -1;
		
	msg->SFData = (unsigned char*)env->GetByteArrayElements(FileId, 0);
	
	//msg->SFData[0] = 0x00;
	//msg->SFData[1] = FileId;
	
	ret = IC_Select_File(msg);
	
	env->ReleaseByteArrayElements(FileId, (jbyte*)msg->SFData, 0);
////////////////////////////////////////////////
	//free(msg->SFData);
	//msg->SFData = NULL;
	free(msg);
	msg = NULL;
	
    return ret;
} 

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_GetChallenge(
    JNIEnv* env, 
    jobject thiz,
    jint Count,
    jbyteArray jarray
)   
{   
    int ret;
    btenv = env;
	Challenge_MSG* msg;

	msg = (Challenge_MSG *)malloc(sizeof(Challenge_MSG));

	if(msg == NULL)
		return -1;

	msg->Count = Count;
    msg->pszRecBuf = (unsigned char *)malloc(Count);

	ret = IC_Get_Challenge(msg);

    env->SetByteArrayRegion(jarray, 0, Count, (jbyte*)msg->pszRecBuf);
    free(msg->pszRecBuf);
	msg->pszRecBuf = NULL;

	free(msg);
	msg = NULL;
	
    return ret;
} 

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_GetResponse(
    JNIEnv* env, 
    jobject thiz,
    jint Count,
    jbyteArray jarray
)   
{   
    int ret;
    btenv = env;
	Response_MSG* msg;

	msg = (Response_MSG *)malloc(sizeof(Response_MSG));

	if(msg == NULL)
		return -1;

	msg->Count = Count;
    msg->pszRecBuf = (unsigned char *)malloc(Count);

	ret = IC_Get_Response(msg);

    env->SetByteArrayRegion(jarray, 0, Count, (jbyte*)msg->pszRecBuf);
    free(msg->pszRecBuf);
	msg->pszRecBuf = NULL;

	free(msg);
	msg = NULL;
	
    return ret;
} 

JNIEXPORT jint JNICALL Java_com_corewise_logic_SmartCard_DeleteFile(JNIEnv *env,jobject thiz)
{
	//unsigned int i;
	unsigned char bCommandBuf[271];
    unsigned char ReqBuf[271] = {0};
	unsigned char RecBuf[271] = {0};
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;
    char databuf[271] = {0};
    btenv = env;
	
	Ret = SCARD_UNKNOWN;
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);
	
	bCommandBuf[0] = 0x72;
	bCommandBuf[3]= 0x80;//CLA
	bCommandBuf[4]= 0x0E;//INS
	bCommandBuf[5]= 0x00;//P1
	bCommandBuf[6]= 0x00;//P2
	bCommandBuf[7]= 0x08;

    for(int i=0;i<8;i++)
        bCommandBuf[8+i] = 0xFF;

//    bCommandBuf[14]= 0x00;//P2   7月11日
    bCommandBuf[16]= 0x73;
	dwLen = 17;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  

	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{

			if((RecBuf[0] == 0x00)&&(RecBuf[1] == 0x90))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
	}
   return Ret;
}

unsigned int Uart_Send(unsigned char* pData, unsigned int count)
{
	LOGI("Uart_Send is get %s,%s",pszclassName,pszMethodSend);
   
	if((pszclassName == NULL)||(pszMethodSend == NULL))
   		return 0;
    LOGI("enter Uart_Send11!");
    jclass clz = btenv->FindClass(pszclassName);
    if (clz == 0)
    {
    	LOGI("not find class!");
		return 0;
    }
    LOGI("enter Uart_Send22!");
    
    LOGI("GetMethodID is get %s,%s",pszclassName,pszMethodSend);
    // 闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归崑婊堟晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏鐑芥晸鐟欐帒搴滈幏鐑芥晸缁诧拷闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗闁跨喐鏋婚幏绌抧t[] intArray,闁跨喐鏋婚幏鐑芥晸閹恒儻璁ｉ幏鐑芥晸閺傘倖瀚归柨鐔诲▏閻氬不,闁跨喐鏋婚幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗String[] strArray闁跨喐鏋婚幏宄扮安娑撶Ljava/lang/String;
    jmethodID SerialportSend = btenv->GetStaticMethodID(clz, pszMethodSend, "([B)I");
    if(SerialportSend == 0)
	{
		LOGI("not find java method!");
		return 0;
	}
    if (SerialportSend)
    {  
        jbyte *pszdata = (jbyte *)pData;
        jbyteArray jarray = btenv->NewByteArray(count);
        btenv->SetByteArrayRegion(jarray, 0, count, pszdata);
        //jint javaIndex = btenv->CallIntMethod(obj, BluetoothSend, jarray);//锟角撅拷态
        jint javaIndex = btenv->CallStaticIntMethod(clz, SerialportSend, jarray);//锟斤拷态
        LOGI("javaIndex = %d", javaIndex);
        return javaIndex;
    }
    else
    {
    	return 0;
    }
}

unsigned int Uart_Recv(unsigned char* pData, unsigned int* count)
{
    if((pszclassName == NULL)||(pszMethodRecv == NULL))
    	return 0;
    jclass clz = btenv->FindClass(pszclassName);
    jmethodID SerialportRecv = btenv->GetStaticMethodID(clz, pszMethodRecv, "([BI)I");
    if (SerialportRecv)
    {
        jbyteArray jarray = btenv->NewByteArray(*count);
        LOGI("Uart_Recv SerialportRecv =%d,*count = %d", SerialportRecv,*count);
        jint javaIndex = btenv->CallStaticIntMethod(clz, SerialportRecv, jarray,*count); //
        LOGI("javaIndex = %d", javaIndex);
        if(javaIndex>0)
        {
            unsigned char* tmpdata = (unsigned char*)btenv->GetByteArrayElements(jarray, 0);
            if(*count > javaIndex)
                *count = javaIndex;
            memcpy(pData,tmpdata,*count);
            LOGI("Uart_Recv tmpdata =%s,javaIndex = %d", tmpdata,javaIndex);  
            LOGI("Uart_Recv tmpdata =%x,%x,%x,%x,%x,%x,%x,%x", tmpdata[0],tmpdata[1],tmpdata[2],tmpdata[3],tmpdata[4],tmpdata[5],tmpdata[6],tmpdata[7]);
            btenv->ReleaseByteArrayElements(jarray, (jbyte*)tmpdata, 0);
        }
        return javaIndex;
    }
    else
    {
    	return 0;
    } 
}

unsigned int IC_Send_Recv(unsigned char* pRequestData, unsigned int dwReqlen, unsigned char* pResponseData, unsigned int* dwRsplen)
{
    unsigned char ReqBuf[271] = {0};
	unsigned char RecBuf[271] = {0};

    memcpy(ReqBuf,pRequestData,271);
	
    LOGI("IC_Send_Recv ReqBuf =%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x",\
    		ReqBuf[0],ReqBuf[1],ReqBuf[2],ReqBuf[3],ReqBuf[4],ReqBuf[5],ReqBuf[6],ReqBuf[7],ReqBuf[8],ReqBuf[9],\
    		ReqBuf[10],ReqBuf[11],ReqBuf[12],ReqBuf[13],ReqBuf[14],ReqBuf[15],\
    		ReqBuf[16],ReqBuf[17],ReqBuf[18],ReqBuf[19],ReqBuf[20],ReqBuf[21],ReqBuf[22],ReqBuf[23],ReqBuf[24],\
    		ReqBuf[25],ReqBuf[26],ReqBuf[27],ReqBuf[28],ReqBuf[29],ReqBuf[30],ReqBuf[31],ReqBuf[32],ReqBuf[33]);
	if (!Uart_Send(ReqBuf, dwReqlen))
	{
      LOGI("Uart_Send fail"); 
		return ERROR;
	}

	if (!Uart_Recv(RecBuf, dwRsplen))
	{
      LOGI("Uart_Recv fail"); 
		return ERROR;
	}
	LOGI("IC_Send_Recv RecBuf =%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x,%x",\
			RecBuf[0],RecBuf[1],RecBuf[2],RecBuf[3],RecBuf[4],RecBuf[5],RecBuf[6],RecBuf[7],RecBuf[8],RecBuf[9],\
			RecBuf[10],RecBuf[11],RecBuf[12],RecBuf[13],RecBuf[14],RecBuf[15],\
			RecBuf[16],RecBuf[17],RecBuf[18],RecBuf[19],RecBuf[20],RecBuf[21],RecBuf[22],RecBuf[23],RecBuf[24],\
			RecBuf[25],RecBuf[26],RecBuf[27],RecBuf[28],RecBuf[29],RecBuf[30],RecBuf[31],RecBuf[32],RecBuf[33]);
	if(*dwRsplen>0)
		memcpy(pResponseData,RecBuf,*dwRsplen);
    //btenv->SetByteArrayRegion(pResponseData, 0, *dwRsplen-1, (jbyte *)(RecBuf+1));

	return SUCCESS;
}
//闁跨喐鏋婚幏绌婇柨鐔惰寧绾攱瀚�

unsigned char IC_CreateFile(CreateFile_MSG *msg)
{
	unsigned int i; 
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;

    //CreateFile_MSG *msg = (CreateFile_MSG *)structmsg;
	
	Ret = SCARD_UNKNOWN;

	if((msg == NULL))
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);
	
	if(msg->SMART_ID == IC_SMARTCOS)
	{
		if(msg->File_Type == MASTER_FILE)
		{
			bCommandBuf[0]= 0x72;
			bCommandBuf[3]= 0x80;//CLA
			bCommandBuf[4]= 0xE0;//INS
			bCommandBuf[5]= 0x00;//P1
			bCommandBuf[6]= 0x00;//P2
			bCommandBuf[7]= 0x0a+msg->mf->NameLen;//LC

			for(i = 0;i < 8;i++)
			{
				bCommandBuf[8+i] = msg->mf->TransCode[i];
			}

			bCommandBuf[16] = msg->mf->Authority[0];
			bCommandBuf[17] = msg->mf->FileId[0];

			if((msg->mf->FileName == NULL))
			{
				return Ret;
			}

			for(i = 0;i < msg->mf->NameLen;i++)
			{
				bCommandBuf[18+i] = *(msg->mf->FileName+i);
			}

			dwLen = 18+msg->mf->NameLen;
		}
		else if(msg->File_Type == DEDICATED_FILE)
		{
			bCommandBuf[0]= 0x72;
			bCommandBuf[3]= 0x80;//CLA
			bCommandBuf[4]= 0xE0;//INS
			bCommandBuf[5]= 0x01;//P1
			bCommandBuf[6]= 0x00;//P2
			bCommandBuf[7]= 0x04+msg->df->NameLen;//LC

			bCommandBuf[8] = msg->df->FileId[0];
			bCommandBuf[9] = msg->df->FileId[1];
			bCommandBuf[10] = msg->df->Authority[1];
			bCommandBuf[11] = 0x00;

			if((msg->df->FileName == NULL))
			{
				return Ret;
			}

			for(i = 0;i < msg->df->NameLen;i++)
			{
				bCommandBuf[12+i] = *(msg->df->FileName+i);
			}
			dwLen = 12+msg->df->NameLen;
		}
		else if(msg->File_Type == ELEMENTARY_FILE)
		{
			bCommandBuf[0]= 0x72;
			bCommandBuf[3]= 0x80;//CLA
			bCommandBuf[4]= 0xE0;//INS
			bCommandBuf[5]= 0x02;//P1
			bCommandBuf[6]= 0x00;//P2
			bCommandBuf[7]= 0x07;//LC


			bCommandBuf[8]= msg->ef->FileId[0];
			bCommandBuf[9]= msg->ef->FileId[1];
			bCommandBuf[10]= msg->ef->FileType[0];
			bCommandBuf[11]= msg->ef->Authority1[0];
			bCommandBuf[12]= msg->ef->Authority2[0];
			bCommandBuf[13]= msg->ef->Len1[0];
			bCommandBuf[14]= msg->ef->Len2[0];
			//for(i = 0;i < 2;i++)
			//{
			//	bCommandBuf[11+i]= *(msg->FileID+i);//DATA
			//}
			dwLen = 15;
		}
	}
//	bCommandBuf[dwLen]= 0x00;  //7月11
//	dwLen++;
	bCommandBuf[dwLen]= 0x73;
	dwLen++;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	LOGI("RecBuf = %x,%x,Ret=%d",RecBuf[0],RecBuf[1],Ret);
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{
		LOGI("11111 Ret =%d",Ret);
			if((RecBuf[0] == 0x00)&&(RecBuf[1] == 0x90))
			{
				LOGI("22 Ret =%d",Ret);
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[0] == 0x90)&&(RecBuf[1] == 0x00))
			{
				LOGI("333 Ret =%d",Ret);
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x01))
			{
				Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
	}

	LOGI("Ret is Ret %x",Ret);
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

    //CreateFile_MSG* msg = (CreateFile_MSG*)structmsg;
        
	Ret = SCARD_UNKNOWN;

	if(msg == NULL)
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);
	
	if(msg->SMART_ID == IC_SMARTCOS)
	{
		if(msg->File_Type == MASTER_FILE)
		{
			bCommandBuf[0]= 0x72;
			bCommandBuf[3]= 0x80;//CLA
			bCommandBuf[4]= 0xE0;//INS
			bCommandBuf[5]= 0x00;//P1
			bCommandBuf[6]= 0x01;//P2
			bCommandBuf[7]= 0x02;//LC
			bCommandBuf[8]= 0x3F;//DATA
			bCommandBuf[9]= 0x00;//DATA

			dwLen = 10;
		}
		else if(msg->File_Type == DEDICATED_FILE)
		{
			bCommandBuf[0]= 0x72;
			bCommandBuf[3]= 0x80;//CLA
			bCommandBuf[4]= 0xE0;//INS
			bCommandBuf[5]= 0x01;//P1
			bCommandBuf[6]= 0x01;//P2
			bCommandBuf[7]= 0x02;//LC
			bCommandBuf[8]= 0x2F;//DATA
			bCommandBuf[9]= 0x01;//DATA

			//for(i = 0;i < 2;i++)
			//{
			//	bCommandBuf[11+i]= *(msg->FileID+i);//DATA
			//}
			dwLen = 10;
		}
	}
//	bCommandBuf[dwLen]= 0x00;//7月11日
//	dwLen++;
	bCommandBuf[dwLen]= 0x73;
	dwLen++;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{
			if((RecBuf[0] == 0x90)&&(RecBuf[1] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[0] == 0x65)&&(RecBuf[1] == 0x81))
			{
				Ret = SCARD_WRITE_EEPROM_FAIL;
			}
			else if((RecBuf[0] == 0x67)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if(RecBuf[0] == 0x63)
			{
					Ret = RecBuf[1]&0x0F;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x01))
			{
					Ret = SCARD_CREATE_CONDITION_NOT_SATISFIED;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x82))
			{
					Ret = SCARD_SECURITY_CONDITION_NOT_SATISFIED;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x80))
			{
					Ret = SCARD_IDENTIFIER_ALREADY_EXISTS;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x81))
			{
					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x82))
			{
					Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x84))
			{
					Ret = SCARD_NOT_ENOUGH_SPACE;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x86))
			{
					Ret = SCARD_PAREMETER_IS_INCORRECT;
			}
			else if((RecBuf[0] == 0x6D)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_INS_IS_INCORRECT;
			}
			else if((RecBuf[0] == 0x6E)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_CLA_IS_INCORRECT;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
	}
	return Ret;  
}
//
//unsigned char IC_Write_KEY(KEY_MSG* msg)
//{
//	unsigned int i;
//	unsigned char bCommandBuf[271];
//	unsigned char RecBuf[271];
//	unsigned int dwLen;
//	unsigned int resplen = 271;
//	unsigned int Ret;
//	unsigned int PackageLen = 0;
//
//    //KEY_MSG* msg = (KEY_MSG*)structmsg;
//
//	Ret = SCARD_UNKNOWN;
//
//	if((msg == NULL)||(msg->pKEY == NULL))
//	{
//		return SCARD_MALLOC_FAILURE;
//	}
//
//	memset(bCommandBuf,0,271);
//	memset(RecBuf,0,271);
//
//	if(msg->SMART_ID == IC_SMARTCOS)
//	{
//		if(msg->pKEY->KEY_InMode == PROCLAIMED)
//		{
//				bCommandBuf[4]= 0x00;
//				bCommandBuf[5]= 0x05+msg->pKEY->PL->Key_Msglen;//len
//
//				bCommandBuf[6]= 0x80;//CLA
//				bCommandBuf[7]= 0xD4;//INS
//				bCommandBuf[8]= msg->pKEY->PL->KEY_Opt;//P1
//				bCommandBuf[9]= 0x00;//P2
//				bCommandBuf[10]= msg->pKEY->PL->Key_Msglen;//LC LENGTH
//
//				if(msg->pKEY->PL->Key_MsgData == NULL)
//				{
//					return Ret;
//				}
//
//				for(i = 0; i < msg->pKEY->PL->Key_Msglen; i++)
//				{
//					bCommandBuf[11+i] = *(msg->pKEY->PL->Key_MsgData+i);
//				}
//
//				dwLen = 11+msg->pKEY->PL->Key_Msglen;
//		}
//		else if(msg->pKEY->KEY_InMode == CIPHERTEXT)
//		{
//			bCommandBuf[4]= 0x00;
//			bCommandBuf[5]= 0x05+msg->pKEY->Cht->Key_Datalen;//len
//
//			bCommandBuf[6]= 0x84;//CLA
//			bCommandBuf[7]= 0xD4;//INS
//			bCommandBuf[8]= msg->pKEY->Cht->KEY_Type;//P1
//			bCommandBuf[9]= msg->pKEY->Cht->KEY_ID;//P2
//			bCommandBuf[10]= msg->pKEY->Cht->Key_Datalen;//LC LENGTH
//
//			if(msg->pKEY->Cht->Key_InforData == NULL)
//			{
//				return Ret;
//			}
//
//			for(i = 0; i < msg->pKEY->Cht->Key_Datalen; i++)
//			{
//				bCommandBuf[11+i] = *(msg->pKEY->Cht->Key_InforData+i);
//			}
//
//			dwLen = 11+msg->pKEY->Cht->Key_Datalen;
//		}
//	}
//	else if(msg->SMART_ID == IC_TIMECOS)
//	{
//
//	}
//
//	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);
//
//	if (Ret != SUCCESS)
//	{
//		Ret = SCARD_UART_NOT_CONNECTED;
//	}
//	else
//	{      //闁跨喐鏋婚幏鐑芥晸缁夊摜顣幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗閹拷
//		if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x01) && (RecBuf[2] == 0x00) && (RecBuf[3] == 0x1A))
//		{
//			Ret = SCARD_ABSENT;
//		}
//		else if((RecBuf[0]== 0x1B) && (RecBuf[1] == 0x00))
//		{
//			PackageLen = RecBuf[2]*256;
//			PackageLen = PackageLen + RecBuf[3];
//			if((RecBuf[4] == 0x90)&&(RecBuf[5] == 0x00))
//			{
//				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
//			}
//			else if((RecBuf[4] == 0x65)&&(RecBuf[5] == 0x81))
//			{
//				Ret = SCARD_WRITE_EEPROM_FAIL;
//			}
//			else if((RecBuf[4] == 0x67)&&(RecBuf[5] == 0x00))
//			{
//					Ret = SCARD_DATA_LENGTH_ERROR;
//			}
//			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x01))
//			{
//					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
//			}
//			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x81))
//			{
//					Ret = SCARD_CMD_NOT_MATCH_TYPES;
//			}
//			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x82))
//			{
//					Ret = SCARD_SECURITY_CONDITION_NOT_SATISFIED;
//			}
//			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x83))
//			{
//					Ret = SCARD_KEY_LOCK;
//			}
//			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x84))
//			{
//					Ret = SCARD_GET_RANDOM_INVALID;
//			}
//			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x85))
//			{
//					Ret = SCARD_CONDITION_OF_USE_NOT_SATISFIED;
//			}
//			else if((RecBuf[4] == 0x69)&&(RecBuf[5] == 0x88))
//			{
//					Ret = SCARD_MAC_INCORRECT;
//			}
//
//			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x80))
//			{
//					Ret = SCARD_DATA_NOT_CORRECT;
//			}
//			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x81))
//			{
//					Ret = SCARD_CARD_LOCK;
//			}
//			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x82))
//			{
//					Ret = SCARD_FILE_NOT_FOUND;
//			}
//			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x84))
//			{
//					Ret = SCARD_FILE_SPACE_INSUFFICIENT;
//			}
//			else if((RecBuf[4] == 0x6A)&&(RecBuf[5] == 0x86))
//			{
//					Ret = SCARD_P1_AND_P2_NOT_CORRECT;
//			}
//			else if((RecBuf[4] == 0x6D)&&(RecBuf[5] == 0x00))
//			{
//					Ret = SCARD_INS_IS_INCORRECT;
//			}
//			else if((RecBuf[4] == 0x6E)&&(RecBuf[5] == 0x00))
//			{
//					Ret = SCARD_CLA_IS_INCORRECT;
//			}
//			else if((RecBuf[4] == 0x93)&&(RecBuf[5] == 0x03))
//			{
//					Ret = SCARD_APP_PERMANENT_LOCK;
//			}
//			else if((RecBuf[4] == 0x94)&&(RecBuf[5] == 0x03))
//			{
//					Ret = SCARD_KEY_NOT_FOUND;
//			}
//			else
//			{
//				Ret = SCARD_UNKNOWN;
//			}
//		}
//		else
//		{
//			Ret = SCARD_UNKNOWN;
//		}
//
//	}
//	return Ret;
//}

unsigned char IC_Read_IBAN(IBAN_MSG* msg)
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
//	unsigned int resptemplen = 271;
	Ret = SCARD_UNKNOWN;
	
	if(msg == NULL)
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[0]= 0x72;
	bCommandBuf[3]= 0x00;//CLA
	bCommandBuf[4]= 0xb2;//INS
	bCommandBuf[5]= 0x01;//P1
	bCommandBuf[6]= 0x0C;//P2
	bCommandBuf[7]= 0x00;//LE LENGTH
	bCommandBuf[8]= 0x73;
	
	dwLen = 9;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen); 
	//LOGI("resplen = %X",resplen); 
//	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf+resplen-1, &resptemplen);  
//	LOGI("resptemplen = %X",resptemplen); 
//	resplen += resptemplen;
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{
		PackageLen = resplen-2;
		if(PackageLen>0)
		{
			memcpy(msg->IBANCode,&RecBuf[4],8);
		}

		pRecBuf = RecBuf+PackageLen;
		LOGI("PackageLen = %X",PackageLen);
		LOGI("pRecBuf = %X,%X,%X,%X,%X,%X,%X,%X",pRecBuf[0],pRecBuf[1],pRecBuf[2],pRecBuf[3],pRecBuf[4],pRecBuf[5],pRecBuf[6],pRecBuf[7]);

		if((pRecBuf[0] == 0x90)&&(pRecBuf[1] == 0x00))
		{
			Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
		}
		else if((pRecBuf[0] == 0x69)&&(pRecBuf[1] == 0x81))
		{
			Ret = SCARD_NOT_BINARY_FILE;
		}
		else if((pRecBuf[0] == 0x69)&&(pRecBuf[1] == 0x82))
		{
			Ret = SCARD_SECURITY_CONDITION_NOT_SATISFIED;
		}
		else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x81))
		{
			Ret = SCARD_FUNCTION_NOT_SUPPORTED;
		}
		else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x82))
		{
			Ret = SCARD_FILE_NOT_FOUND;
		}
		else if((pRecBuf[0] == 0x6B)&&(pRecBuf[1] == 0x00))
		{
			Ret = SCARD_PAREMETER_IS_INCORRECT;
		}
		else if(pRecBuf[0] == 0x6C)
		{
			Ret = SCARD_DATA_LENGTH_ERROR;
		}
		else
		{
			Ret = SCARD_UNKNOWN;
		}

			 
	}
	return Ret;  
}
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

    //BINARY_MSG* msg = (BINARY_MSG*)structmsg;
        
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[0]= 0x72;
	bCommandBuf[3]= 0x00;//CLA
	bCommandBuf[4]= 0xB0;//INS
	bCommandBuf[5]= 0x80|(0x1F&(msg->FileId));//P1
	bCommandBuf[6]= msg->Offset;//P2
	bCommandBuf[7]= (msg->Count);//<0x110?(msg->Count):0x110;//LE LENGTH
	bCommandBuf[8]= 0x73;
	
	dwLen = 9;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{
            PackageLen = resplen-2;
            if(PackageLen>0)
            {
			    memcpy(msg->pszRecBuf,&RecBuf[0],PackageLen);
            }

			pRecBuf = RecBuf+PackageLen;
            if((pRecBuf[0] == 0x90)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
            else if((pRecBuf[0] == 0x69)&&(pRecBuf[1] == 0x81))
			{
				Ret = SCARD_NOT_BINARY_FILE;
			}
			else if((pRecBuf[0] == 0x69)&&(pRecBuf[1] == 0x82))
			{
				Ret = SCARD_SECURITY_CONDITION_NOT_SATISFIED;
			}
			else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x81))
			{
				Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x82))
			{
				Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((pRecBuf[0] == 0x6B)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_PAREMETER_IS_INCORRECT;
			}
			else if(pRecBuf[0] == 0x6C)
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}

	}
	return Ret;  
}
unsigned char IC_Update_Binary(BINARY_MSG* msg)
{
	unsigned int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;

    //BINARY_MSG* msg = (BINARY_MSG*)structmsg;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[0]= 0x72;
	if(msg->level == 0)
		bCommandBuf[3]= 0x00;//CLA
	else
		bCommandBuf[3]= 0x04;//CLA
	bCommandBuf[4]= 0xD6;//INS
	bCommandBuf[5]= 0x80|(0x1F&(msg->FileId));//P1
	bCommandBuf[6]= msg->Offset;//P2
	bCommandBuf[7]= msg->UBDatalen;//Lc LENGTH
	
	for(i = 0; i < msg->UBDatalen; i++)
	{
		bCommandBuf[8+i] = *(msg->UBData+i);
	}

//	bCommandBuf[6+msg->UBDatalen]= 0x00; //7月11
	bCommandBuf[8+msg->UBDatalen]= 0x73;

	dwLen = 9+msg->UBDatalen;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	LOGI("bCommandBuf = %X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X,%X",bCommandBuf[0],bCommandBuf[1],bCommandBuf[2],bCommandBuf[3],bCommandBuf[4],bCommandBuf[5],bCommandBuf[6],bCommandBuf[7],bCommandBuf[8],bCommandBuf[9],bCommandBuf[10],bCommandBuf[11],bCommandBuf[12],bCommandBuf[13],bCommandBuf[14],bCommandBuf[15]);
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  

    LOGI("RecBuf = %X,%X,%X,%X,%X,%X,%X,%X",RecBuf[0],RecBuf[1],RecBuf[2],RecBuf[3],RecBuf[4],RecBuf[5],RecBuf[6],RecBuf[7]); 
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{
//			PackageLen = PackageLen-2;
			if((RecBuf[0] == 0x90)&&(RecBuf[1] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[0] == 0x65)&&(RecBuf[1] == 0x81))
			{
				Ret = SCARD_WRITE_EEPROM_FAIL;
			}
			else if((RecBuf[0] == 0x67)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x81))
			{
					Ret = SCARD_NOT_BINARY_FILE;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x82))
			{
					Ret = SCARD_CONDITION_OF_CMD_NOT_SATISFIED;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x84))
			{
					Ret = SCARD_GET_RANDOM_INVALID;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x85))
			{
					Ret = SCARD_CONDITION_OF_USE_NOT_SATISFIED;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x88))
			{
					Ret = SCARD_SECURITY_DATA_NOT_CORRECT;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x80))
			{
					Ret = SCARD_DATA_NOT_CORRECT;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x81))
			{
					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x82))
			{
					Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x86))
			{
					Ret = SCARD_P1_AND_P2_NOT_CORRECT;
			}
			else if((RecBuf[0] == 0x6B)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_P1_AND_P2_OUT_OF_GAUGE;
			}
			else if((RecBuf[0] == 0x6D)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_INS_IS_INCORRECT;
			}
			else if((RecBuf[0] == 0x6E)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_CLA_IS_INCORRECT;
			}
			else if((RecBuf[0] == 0x93)&&(RecBuf[1] == 0x02))
			{
					Ret = SCARD_APP_PERMANENT_LOCK;
			}
			else if((RecBuf[0] == 0x93)&&(RecBuf[1] == 0x03))
			{
					Ret = SCARD_KEY_NOT_FOUND;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
	}
	return Ret;  
}
//闁跨喓鑽￠柨鐔兼應閿燂拷
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

    //Record_MSG* msg = (Record_MSG*)structmsg;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[0]= 0x72;
	bCommandBuf[3]= 0x00;//CLA
	bCommandBuf[4]= 0xB2;//INS
	bCommandBuf[5]= msg->Index;//P1
	bCommandBuf[6]= (msg->FileId<<3)|0x4;//P2
	bCommandBuf[7]= (msg->Count)<0x110?(msg->Count):0x110;//LE LENGTH
	bCommandBuf[8]= 0x73;
	
	dwLen = 9;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{
            PackageLen = resplen-2;
            
            if(PackageLen>0)
			    memcpy(msg->pszRecBuf,&RecBuf[0],PackageLen);

			pRecBuf = RecBuf+PackageLen;
			
			if((pRecBuf[0] == 0x90)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((pRecBuf[0] == 0x69)&&(pRecBuf[1] == 0x81))
			{
				Ret = SCARD_CMD_NOT_MATCH_TYPES;
			}
			else if((pRecBuf[0] == 0x69)&&(pRecBuf[1] == 0x82))
			{
				Ret = SCARD_CONDITION_OF_READ_NOT_SATISFIED;
			}
			else if((pRecBuf[0] == 0x69)&&(pRecBuf[1] == 0x86))
			{
				Ret = SCARD_CONDITION_OF_CMD_NOT_SATISFIED;
			}
			else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x81))
			{
				Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x82))
			{
				Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x83))
			{
				Ret = SCARD_RECORD_NOT_FOUND;
			}
			else if(pRecBuf[0] == 0x6C)
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
	}
	return Ret;  
}
//鏉╀粙鏁撻幒銉с�閹峰嘲缍�
unsigned char IC_Append_Record(Record_MSG* msg)
{
	unsigned int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;

    //Record_MSG* msg = (Record_MSG*)structmsg;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[0]= 0x72;
	if(msg->level == 0)
		bCommandBuf[3]= 0x00;//CLA
	else
		bCommandBuf[3]= 0x04;//CLA
	bCommandBuf[4]= 0xE2;//INS
	bCommandBuf[5]= 0x00;//P1
	bCommandBuf[6]= (msg->FileId<<3)|0x0;//P2
	bCommandBuf[7]= msg->UBDatalen;//LC LENGTH


	for(i = 0; i < msg->UBDatalen; i++)
	{
		bCommandBuf[8+i] = *(msg->UBData+i);
	}

//	bCommandBuf[6+msg->UBDatalen]= 0x00;  //7月11
	bCommandBuf[8+msg->UBDatalen]= 0x73;

	dwLen = 9+msg->UBDatalen;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{

			if((RecBuf[0] == 0x90)&&(RecBuf[1] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[0] == 0x65)&&(RecBuf[1] == 0x81))
			{
				Ret = SCARD_WRITE_EEPROM_FAIL;
			}
			else if((RecBuf[0] == 0x67)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x81))
			{
					Ret = SCARD_FILE_NOT_LINEAR_FIXED_FILE;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x82))
			{
					Ret = SCARD_CONDITION_OF_CMD_NOT_SATISFIED;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x84))
			{
					Ret = SCARD_GET_RANDOM_INVALID;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x85))
			{
					Ret = SCARD_APP_TEMPORARY_LOCED;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x88))
			{
					Ret = SCARD_MAC_INCORRECT;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x81))
			{
					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x82))
			{
					Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x83))
			{
					Ret = SCARD_RECORD_NOT_FOUND;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x84))
			{
					Ret = SCARD_FILE_STORAGE_SPACE_NOT_ENOUGH;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x86))
			{
					Ret = SCARD_P1_AND_P2_NOT_CORRECT;
			}
			else if((RecBuf[0] == 0x6D)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_INS_IS_INCORRECT;
			}
			else if((RecBuf[0] == 0x6E)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_CLA_IS_INCORRECT;
			}
			else if((RecBuf[0] == 0x93)&&(RecBuf[1] == 0x02))
			{
					Ret = SCARD_APP_PERMANENT_LOCK;
			}
			else if((RecBuf[0] == 0x93)&&(RecBuf[1] == 0x03))
			{
					Ret = SCARD_KEY_NOT_FOUND;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
	}
	return Ret;  
}
//闁跨喓娼鹃弨鍦�閹峰嘲缍�
unsigned char IC_Update_Record(Record_MSG* msg)
{
	unsigned int i;
	unsigned char bCommandBuf[271];
	unsigned char RecBuf[271];
	unsigned int dwLen;
	unsigned int resplen = 271;
	unsigned int Ret;
	unsigned int PackageLen = 0;

    //Record_MSG* msg = (Record_MSG*)structmsg;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[0]= 0x72;
	if(msg->level == 0)
		bCommandBuf[3]= 0x00;//CLA
	else
		bCommandBuf[3]= 0x04;//CLA
	bCommandBuf[4]= 0xDC;//INS
	bCommandBuf[5]= msg->Index;//P1
	bCommandBuf[6]= (msg->FileId<<3)|0x4;//P2
	bCommandBuf[7]= msg->UBDatalen;//LC LENGTH
	
	for(i = 0; i < msg->UBDatalen; i++)
	{
		bCommandBuf[8+i] = *(msg->UBData+i);
	}

//	bCommandBuf[6+msg->UBDatalen]= 0x00;//7月11
	bCommandBuf[8+msg->UBDatalen]= 0x73;

	dwLen = 9+msg->UBDatalen;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{      //闁跨喐鏋婚幏鐑芥晸缁夊摜顣幏鐑芥晸閺傘倖瀚归柨鐔告灮閹风兘鏁撻弬銈嗗閹拷
			
			if((RecBuf[0] == 0x90)&&(RecBuf[1] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((RecBuf[0] == 0x65)&&(RecBuf[1] == 0x81))
			{
				Ret = SCARD_WRITE_EEPROM_FAIL;
			}
			else if((RecBuf[0] == 0x67)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x81))
			{
					Ret = SCARD_FILE_NOT_LINEAR_FIXED_FILE;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x82))
			{
					Ret = SCARD_CONDITION_OF_CMD_NOT_SATISFIED;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x84))
			{
					Ret = SCARD_GET_RANDOM_INVALID;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x85))
			{
					Ret = SCARD_APP_TEMPORARY_LOCED;
			}
			else if((RecBuf[0] == 0x69)&&(RecBuf[1] == 0x88))
			{
					Ret = SCARD_MAC_INCORRECT;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x81))
			{
					Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x82))
			{
					Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x83))
			{
					Ret = SCARD_RECORD_NOT_FOUND;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x84))
			{
					Ret = SCARD_FILE_STORAGE_SPACE_NOT_ENOUGH;
			}
			else if((RecBuf[0] == 0x6A)&&(RecBuf[1] == 0x86))
			{
					Ret = SCARD_P1_AND_P2_NOT_CORRECT;
			}
			else if((RecBuf[0] == 0x6D)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_INS_IS_INCORRECT;
			}
			else if((RecBuf[0] == 0x6E)&&(RecBuf[1] == 0x00))
			{
					Ret = SCARD_CLA_IS_INCORRECT;
			}
			else if((RecBuf[0] == 0x93)&&(RecBuf[1] == 0x02))
			{
					Ret = SCARD_APP_PERMANENT_LOCK;
			}
			else if((RecBuf[0] == 0x93)&&(RecBuf[1] == 0x03))
			{
					Ret = SCARD_KEY_NOT_FOUND;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
	return Ret;  
}

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

    //SelectFile_MSG* msg = (SelectFile_MSG*)structmsg;
	
	Ret = SCARD_UNKNOWN;

	if(msg == NULL)
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[0]= 0x72;
	bCommandBuf[3]= 0x00;//CLA
	bCommandBuf[4]= 0xA4;//INS
	bCommandBuf[5]= msg->FileType;//P1 0x02;
	bCommandBuf[6]= msg->FileIndex;//(msg->FileIndex<<3)|0x4;//P2   0x00
	bCommandBuf[7]= msg->SFDatalen;//LC LENGTH 0x02
	
	for(i = 0;i < msg->SFDatalen;i++)
	{
		bCommandBuf[8+i] = *(msg->SFData+i);//0x00,0x16
	}
	
//	bCommandBuf[7+msg->SFDatalen]= 0x00;  7月11
	bCommandBuf[8+msg->SFDatalen]= 0x73;

	dwLen = 9+msg->SFDatalen;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen); 
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{
			PackageLen = resplen-2;

//			if(PackageLen>0)
//			{
//				memcpy(msg->pszRecBuf,&RecBuf[4],PackageLen);
//			}

			pRecBuf = RecBuf+PackageLen;
			
			if((pRecBuf[0] == 0x90)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if(pRecBuf[0] == 0x61)
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((pRecBuf[0] == 0x67)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x81))
			{
				Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x82))
			{
				Ret = SCARD_FILE_NOT_FOUND;
			}
			else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x86))
			{
				Ret = SCARD_P1_AND_P2_NOT_CORRECT;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
	return Ret;  
}

unsigned char IC_Credit_For_Load(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Debit_For_Purchase(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Debit_For_Unload(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Get_Balance(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
unsigned char IC_Get_Transaction_Prove(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Initialize_For_Case_Withdraw(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
unsigned char IC_Initial_For_Load(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
unsigned char IC_Initial_For_Purchase(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
unsigned char IC_Initial_For_Unload(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Initial_For_Update(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Update_Overdraw_Limit(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
unsigned char IC_Application_Block(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Application_Unlock(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
unsigned char IC_Card_Block(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_External_authentication(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
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

    //Challenge_MSG* msg = (Challenge_MSG*)structmsg;
	
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[0]= 0x72;
	bCommandBuf[3]= 0x00;//CLA
	bCommandBuf[4]= 0x84;//INS
	bCommandBuf[5]= 0x00;//P1
	bCommandBuf[6]= 0x00;//P2
	bCommandBuf[7]= 0x04;//Le LENGTH
	bCommandBuf[8]= 0x73;
	
	dwLen = 9;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{
			PackageLen = resplen-2;

			msg->Count = PackageLen;
			memcpy(msg->pszRecBuf,&RecBuf[0],PackageLen);

			pRecBuf = RecBuf+PackageLen;
			
			if((pRecBuf[0] == 0x90)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((pRecBuf[0] == 0x67)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((pRecBuf[0] == 0x6A)&&(pRecBuf[1] == 0x81))
			{
				Ret = SCARD_FUNCTION_NOT_SUPPORTED;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
	return Ret;  
}
//閸欐牠鏁撻弬銈嗗鎼达拷
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

    //Response_MSG* msg = (Response_MSG*)structmsg;
        
	Ret = SCARD_UNKNOWN;
	
	if((msg == NULL)||(msg->pszRecBuf == NULL))
	{
		return SCARD_MALLOC_FAILURE;
	}
	
	memset(bCommandBuf,0,271);
	memset(RecBuf,0,271);

	bCommandBuf[0]= 0x72;
	
	bCommandBuf[3]= 0x00;//CLA
	bCommandBuf[4]= 0xC0;//INS
	bCommandBuf[5]= 0x00;//P1
	bCommandBuf[6]= 0x00;//P2
	bCommandBuf[7]= msg->Count;//Le LENGTH
	bCommandBuf[8]= 0x73;
	
	dwLen = 9;
	bCommandBuf[1]= ((dwLen-4)>>8)&0xFF;//D 1
	bCommandBuf[2]= (dwLen-4)&0x00FF;//D 44
	Ret = IC_Send_Recv(bCommandBuf, dwLen, RecBuf, &resplen);  
	
	if (Ret != SUCCESS)
	{	
		Ret = SCARD_UART_NOT_CONNECTED;
	}
	else
	{
			PackageLen = resplen-2;

			memcpy(msg->pszRecBuf,&RecBuf[0],PackageLen);

			pRecBuf = RecBuf+PackageLen;
			
			if((pRecBuf[0] == 0x90)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_COMMAND_EXECUTED_CORRECTLY;
			}
			else if((pRecBuf[0] == 0x67)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_DATA_LENGTH_ERROR;
			}
			else if((pRecBuf[0] == 0x6F)&&(pRecBuf[1] == 0x00))
			{
				Ret = SCARD_NO_DATA_RETURN;
			}
			else
			{
				Ret = SCARD_UNKNOWN;
			}
		}
	return Ret;  
}
unsigned char IC_Internal_Authentication(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_PIN_Change_OR_Unblock(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Verify_PIN(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Change_PIN(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}

unsigned char IC_Reload_PIN(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
unsigned char IC_Crypt(void)
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
unsigned char IC_Generate_KEY()
{
	unsigned int Ret;
	Ret = SCARD_UNKNOWN;

	return Ret;
}
#if 0
JNIEXPORT jobject JNICALL Java_android_serialport_SerialPort_open(JNIEnv *env, jobject thiz, jstring path, jint baudrate) {
	//闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�
	/* Opening device */
	const char *path_utf = (*env)->GetStringUTFChars(env, path, NULL);//&iscopy
	printf("Opening serial port %s", path_utf);
	fd = open(path_utf, O_RDWR | O_DIRECT | O_SYNC);
	printf("open() fd = %d", fd);
	(*env)->ReleaseStringUTFChars(env, path, path_utf);
	//闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�
	/* Configure device */
	//闁跨喐鏋婚幏鐑芥晸閺傘倖瀚�
	struct termios cfg;
	cfmakeraw(&cfg);
	cfsetispeed(&cfg, B115200);
	cfsetospeed(&cfg, B115200);
	return 1;
}
#endif
