package com.example.autoreply;

import java.util.ArrayList;
import java.util.List;

public class StaticData {
	//Ĭ���Զ��ظ�����Ϣ����
	static String message = "���ñ�����æ����������Ե�  ���Զ��ظ���";
	//΢��6.3.18��������id��΢�Ű汾���º���֮�޸ļ���
	static String qunId = "com.tencent.mm:id/ei";
	static String editId = "com.tencent.mm:id/yq";
	static String sendId = "com.tencent.mm:id/yw";
	//�Ƿ�ָ������
	static boolean isfriend = true;
	//Ĭ��ָ���ĺ����ǳ�
	static String friend = "�ڴ�ָ��һλ����";
	//�Ƿ����Զ��ظ�
	static boolean auto = false;
	//���������Ƿ���ʾ��Ϣ��ϸ����
	static boolean showall = true;
	//�Ƿ����������ͨ���������Ƿ���ʾ��������
	static boolean iscalling = false;
	//��Ϣ����
	static int total = 0;
	//���Զ��ظ�����Ϣ����
	static int replaied = 0;
	//�յ���΢����Ϣ�б�
	static List<String> data = new ArrayList<String>();
}
