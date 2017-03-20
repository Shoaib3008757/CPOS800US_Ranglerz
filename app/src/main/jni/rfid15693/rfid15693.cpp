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
/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/HelloJni/HelloJni.java
 */



#define  LOG_TAG    "librfid"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

typedef unsigned char BYTE;

extern "C" {
	JNIEXPORT jint JNICALL Java_com_hiklife_rfidapi_Crack_Decrypt(JNIEnv *env,jobject thiz,jbyteArray jencryptIn,jbyteArray jencryptOut,jlong jdatalen);
};

unsigned long RFID_Decrypt(BYTE *Source, BYTE *temp, char *result);

JNIEXPORT jint JNICALL Java_com_hiklife_rfidapi_Crack_Decrypt(JNIEnv *env,jobject thiz,jbyteArray jencryptIn,jbyteArray jencryptOut,jlong jdatalen)
{
	LOGI("Java_com_hiklife_rfidapi_Crack_Decrypt0 \n\t");
	char result[64];
	BYTE temp[0x20];
	long datalen = jdatalen;
	BYTE* pszEncryptIn = NULL;
	char* pszEncryptOut = result;
	memset(result,0,64);
	memset(temp,0,0x20);
	pszEncryptIn = (BYTE*)env->GetByteArrayElements(jencryptIn, 0);
	LOGI("1=%x, %x, %x, %x, %x, %x, %x,%x",pszEncryptIn[0],pszEncryptIn[1],pszEncryptIn[2],pszEncryptIn[3],pszEncryptIn[4],pszEncryptIn[5],pszEncryptIn[6],pszEncryptIn[7]);
	//for(int i=0;i<25;i++)
	//{
	//	LOGI("1=%x,\n\t"+pszEncryptIn[i]);
	//}
	RFID_Decrypt(pszEncryptIn, temp, pszEncryptOut);
	LOGI("2=%d, %d, %d, %d, %d, %d, %d,%d",pszEncryptOut[0],pszEncryptOut[1],pszEncryptOut[2],pszEncryptOut[3],pszEncryptOut[4],pszEncryptOut[5],pszEncryptOut[6],pszEncryptOut[7]);

	LOGI("2=%c, %c, %c, %c, %c, %c, %c,%c",pszEncryptOut[0],pszEncryptOut[1],pszEncryptOut[2],pszEncryptOut[3],pszEncryptOut[4],pszEncryptOut[5],pszEncryptOut[6],pszEncryptOut[7]);
	//for(int j=0;j<25;j++)
	//{
	//	LOGI("2=%x,\n\t"+pszEncryptOut[j]);
	//}
	if(datalen>64)
		datalen = 64;

	env->SetByteArrayRegion(jencryptOut, 0, datalen, (jbyte*)pszEncryptOut);
	LOGI("Java_com_hiklife_rfidapi_Crack_Decrypt 3");
	env->ReleaseByteArrayElements(jencryptIn, (jbyte*)pszEncryptIn, 0);
	return 0;
}

unsigned long RFID_Decrypt(BYTE *Source, BYTE *temp, char *result)
{
	BYTE key_78 = 0;
	BYTE ctl_74 = 0;
	int i = 0;
	ctl_74 = Source[0x11] + Source[0x12] - Source[0x13];
	ctl_74 = ctl_74%0x10;

	key_78 = Source[0x11]^Source[0x13];
	key_78 ^= ~Source[0x12];

	i = 0;

	while( i < 0x10 - ctl_74 )
	{
		temp[i] = Source[ctl_74+i];
		i++;
	}

	if( ctl_74 > 0)
	{
		i = 0;
		while(i<ctl_74)
		{
			temp[0x10 - ctl_74 +i] = Source[i];
			i++;
		}
	}

	temp[0x10] = Source[0x10];
	i = 0;

	while( i < 0x11)
	{
		temp[i] = temp[i]^key_78;
		key_78 = key_78/2 + (key_78%2)*0x80;
		i++;
	}

	memcpy(result,temp,0x11);
	result[0x11] = '\0';

	return 0;
}





