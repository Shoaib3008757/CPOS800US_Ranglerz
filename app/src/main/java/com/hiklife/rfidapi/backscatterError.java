package com.hiklife.rfidapi;

public enum backscatterError {
	Ok,

    /* PCֵ�����ڣ�����֧�ֵı�ǩ                                               **/
	PCValueNotExist,

    /* �����ڴ����鱻��ס�򲻿�д                                               **/
    SpecifiedMemoryLocationLocked,

    /* ��ǩû���㹻����������д����                                             **/
    InsufficientPower,

    /* ��ǩ��֧�����������                                                     **/
    NotSupportErrorSpecificCodes
}
